using Microsoft.AspNetCore.SignalR;
using Model.DTOs;
using Model.Entities;
using Server3.Hubs;
using Server3.Interfaces;
using System.Diagnostics;
using System.Text;
using System.Text.RegularExpressions;

namespace Server3.Services
{
    public class HuanLuyenService : IHuanLuyenService
    {
        private readonly IHubContext<TrainingStatusHub> _hubService;
        private readonly IThongTinHLService thongTinHLService;

        private const long SEND_STATUS_DELAY_MS = 4000;
        private int currentEpoch = 0;
        private List<string> newLogs = new();
        private ThongTinHL thongTinHL;

        public HuanLuyenService(
            IHubContext<TrainingStatusHub> hubService, 
            IThongTinHLService thongTinHLService)
        {
            _hubService = hubService;
            this.thongTinHLService = thongTinHLService;
        }

        public async Task StartTrainMoHinhAsync(int thongTinHLId)
        {
            try
            {
                thongTinHL = await thongTinHLService.GetThongTinHLAsync(thongTinHLId);

                string listenerGroupId = thongTinHL.Id + "";

                thongTinHL.TrangThai = "Đang chuẩn bị dataset ...";
                SendStatusAsync();
                await SetupDatasetAsync();

                thongTinHL.TrangThai = "Đang huấn luyện ...";
                thongTinHL.BatDauLuc = DateTime.UtcNow;
                SendStatusAsync();

                var cts = new CancellationTokenSource();
                Stopwatch sw = Stopwatch.StartNew();
                await RunTrainExeAsync(log =>
                {
                    // Remove ANSI escape codes
                    log = Regex.Replace(log, @"\x1B\[[0-9;]*[A-Za-z]", "");
                    newLogs.Add(log);
                    Console.WriteLine(log);

                    ExtractCurrentEpochFromLog(log);
                    ExtractDoChinhXacFromLog(log);
                    ExtractDoNhayFromLog(log);

                    SendStatusAsync();
                    //if (sw.ElapsedMilliseconds > SEND_STATUS_DELAY_MS)
                    //{
                    //    sw.Restart();                    
                    //    SendStatusAsync();
                    //}
                }, cts.Token);

                thongTinHL.TrangThai = "Hoàn thành huấn luyện";

                thongTinHL.DuongDanMoHinhKetQua = Path.Combine(
                    AppContext.BaseDirectory,
                    "training\\runs\\detect\\runs\\train\\traffic_signs_yolov8s\\weights\\best.pt");
            } 
            catch (Exception ex)
            {
                thongTinHL.TrangThai = "Huấn luyện thất bại";
                newLogs.Add("Huấn luyện thất bại: " + ex.Message);
            }
            finally
            {
                SendStatusAsync();

                thongTinHL.KetThucLuc = DateTime.UtcNow;
                await thongTinHLService.SaveThongTinHLAsync(thongTinHL);
            }
        }


        private void ExtractCurrentEpochFromLog(string log)
        {
            if (!log.Contains('/')) return;
            log = log[..14];
            log = Regex.Replace(log, @"\x1B\[[0-9;]*[A-Za-z]", "");
            log = log.Trim();
            try
            {
                currentEpoch = int.Parse(log.Split('/')[0]);
            } catch { }
        }

        private void ExtractDoNhayFromLog(string log)
        {
            if (!log.Contains("- Recall:")) return;
            try
            {
                log = log.Trim();
                thongTinHL.DoNhay = float.Parse(log.Split("- Recall:").Last());
            } catch { }
        }

        private void ExtractDoChinhXacFromLog(string log)
        {
            if (!log.Contains("- Precision:")) return;
            try
            {
                log = log.Trim();
                thongTinHL.DoChinhXac = float.Parse(log.Split("- Precision:").Last());
            } catch { }
        }

        private async Task RunTrainExeAsync(Action<string> onLog, CancellationToken cancellationToken)
        {
            Console.OutputEncoding = Encoding.UTF8;

            var workingDir = Path.Combine(AppContext.BaseDirectory, "training");
            var exePath = Path.Combine(workingDir, "TrainTrafficSigns.exe");

            if (!File.Exists(exePath))
            {
                onLog($"[CRITICAL] Không tìm thấy TrainTrafficSigns.exe tại: {exePath}");
                throw new FileNotFoundException("Không tìm thấy executable", exePath);
            }

            var args = BuildExeArguments();

            onLog($"[INFO] Working Directory : {workingDir}");
            onLog($"[INFO] Exe Path          : {exePath}");
            onLog($"[INFO] Arguments         : {args}");

            var psi = new ProcessStartInfo
            {
                FileName = exePath,
                Arguments = args,
                WorkingDirectory = workingDir,
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false,
                CreateNoWindow = true,
                ErrorDialog = false
            };

            using var process = new Process { StartInfo = psi };

            // Handler rõ ràng hơn
            process.OutputDataReceived += (_, e) =>
            {
                if (!string.IsNullOrEmpty(e.Data))
                    onLog(e.Data);
            };

            process.ErrorDataReceived += (_, e) =>
            {
                if (!string.IsNullOrEmpty(e.Data))
                    onLog("[EXE ERROR] " + e.Data);
            };

            try
            {
                bool started = process.Start();
                if (!started)
                {
                    onLog("[CRITICAL] Process.Start() trả về false - không khởi động được exe");
                    return;
                }

                process.BeginOutputReadLine();
                process.BeginErrorReadLine();

                onLog("[INFO] Exe đã khởi động thành công, đang chờ kết thúc...");

                await process.WaitForExitAsync(cancellationToken);

                onLog($"[INFO] Exe kết thúc với ExitCode = {process.ExitCode}");
            }
            catch (Exception ex)
            {
                onLog($"[CRITICAL EXCEPTION] Không thể chạy TrainTrafficSigns.exe");
                onLog($"[EXCEPTION] {ex.GetType().Name}: {ex.Message}");
                onLog(ex.ToString());
                throw;
            }
        }

        private string BuildExeArguments()
        {
            var args = new StringBuilder();
            args.Append($"--epochs {thongTinHL.Epochs} ");
            args.Append($"--batch-size {thongTinHL.BatchSize} ");
            args.Append($"--learning-rate {thongTinHL.LearningRate.ToString(System.Globalization.CultureInfo.InvariantCulture)} ");
            args.Append($"--image-size {thongTinHL.KichThuocAnh} ");
            args.Append($"--patience {thongTinHL.EarlyStoppingPatience} ");
            args.Append($"--device {thongTinHL.LoaiThietBi} ");
            args.Append($"--optimizer {thongTinHL.Optimizer}");

            args.Append($" --model-path \"{Path.Combine(AppContext.BaseDirectory, thongTinHL.PhienBanHL.DuongDanMH)}\"");
            return args.ToString();
        }

        private async Task SetupDatasetAsync()
        {
            // Chia 80% train 10% test và 10% val
            var rng = new Random(42);
            List<int> indexs = Enumerable.Range(0, thongTinHL.DsMauHL.Count)
                .OrderBy(_ => rng.Next())
                .ToList();
            int trainCount = (int)(indexs.Count * 0.8);
            int testCount = (int)(indexs.Count * 0.1);
            HashSet<int> trainIdx = indexs.Take(trainCount).ToHashSet();
            HashSet<int> testIdx = indexs.Skip(trainCount).Take(testCount).ToHashSet();

            List<(string, List<string>)> trainSet = new();
            List<(string, List<string>)> testSet = new();
            List<(string, List<string>)> valSet = new();
            for (int i = 0; i < thongTinHL.DsMauHL.Count; i++)
            {
                var imgName = Path.GetFileName(thongTinHL.DsMauHL[i].ThongTin.DuongDanAnh);
                if (testIdx.Contains(i))
                {
                    testSet.Add((
                        imgName,
                        GenerateLabel(thongTinHL.DsMauHL[i].ThongTin.DsBien))
                    );
                }
                else if (trainIdx.Contains(i))
                {
                    trainSet.Add((
                        imgName,
                        GenerateLabel(thongTinHL.DsMauHL[i].ThongTin.DsBien))
                    );
                }
                else
                {
                    valSet.Add((
                        imgName,
                        GenerateLabel(thongTinHL.DsMauHL[i].ThongTin.DsBien))
                    );
                }
            }

            // Đẩy ảnh vào thư mục images
            ClearAndCopyImageToFolder("training/dataset/images/test", testSet.Select(e => e.Item1).ToList());
            ClearAndCopyImageToFolder("training/dataset/images/train", trainSet.Select(e => e.Item1).ToList());
            ClearAndCopyImageToFolder("training/dataset/images/val", valSet.Select(e => e.Item1).ToList());

            // Đẩy label vào thư mục labels
            GenerateLabelFiles("training/dataset/labels/test", testSet);
            GenerateLabelFiles("training/dataset/labels/train", trainSet);
            GenerateLabelFiles("training/dataset/labels/val", valSet);
        }

        private void GenerateLabelFiles(string destFolderPath, List<(string, List<string>)> set)
        {
            var path = Path.Combine(AppContext.BaseDirectory, destFolderPath);

            if (Directory.Exists(path))
                Directory.Delete(path, recursive: true);
            Directory.CreateDirectory(path);

            set.ForEach(e =>
            {
                string filePath = Path.Combine(path, Path.GetFileNameWithoutExtension(e.Item1) + ".txt");
                File.WriteAllLines(filePath, e.Item2);
            }); 
        }

        private List<string> GenerateLabel(List<KhungNhanDang> khungs)
        {
            var lines = new List<string>();
            khungs.ForEach(k =>
            {
                lines.Add($"{k.Bien.Id} {k.XCenter} {k.YCenter} {k.W} {k.H}");
            });
            return lines;
        }

        private void ClearAndCopyImageToFolder(string destFolderPath, List<string> imgNames)
        {
            if (imgNames == null || imgNames.Count == 0)
                return;

            var srcPath = Path.Combine(AppContext.BaseDirectory, "uploads");
            var destPath = Path.Combine(AppContext.BaseDirectory, destFolderPath);

            if (Directory.Exists(destPath)) 
                Directory.Delete(destPath, recursive: true);
            Directory.CreateDirectory(destPath);

            var nameSet = new HashSet<string>(imgNames, StringComparer.OrdinalIgnoreCase);

            var filesToCopy = Directory.EnumerateFiles(srcPath)
                                       .Where(file => nameSet.Contains(Path.GetFileName(file)))
                                       .ToArray();  

            if (filesToCopy.Length == 0)
                return;

            Parallel.ForEach(filesToCopy, new ParallelOptions
            {
                MaxDegreeOfParallelism = 4 
            },
            file =>
            {
                string destFile = Path.Combine(destPath, Path.GetFileName(file));
                File.Copy(file, destFile, overwrite: true);
            });
        }

        private async void SendStatusAsync()
        {
            await _hubService.Clients.Group(thongTinHL.Id + "").SendAsync("OnHasNewStatus", new TrainingStatusDto
            {
                CurrentEpochs = currentEpoch,
                NewLogs = newLogs,
                TrainingStatus = thongTinHL.TrangThai
            });

            newLogs.Clear();
        }
    }
}
