using Microsoft.AspNetCore.SignalR.Client;
using Model.DTOs;
using Model.Entities;
using System.Data.Common;
using System.Text;
using System.Text.Json;

namespace Client.Services
{
    public class ClientControl
    {
        private const string baseUrl = "https://localhost:7152";
        private static ClientControl? _clientControl;
        private readonly HttpClient _http;
        private HubConnection? connection;
        private readonly JsonSerializerOptions _option = new()
        {
            PropertyNameCaseInsensitive = true,
        };

        private ClientControl()
        {
            _http = new HttpClient
            {
                BaseAddress = new Uri(baseUrl),
                Timeout = TimeSpan.FromSeconds(50)
            };
        }

        public event EventHandler<TrainingStatusDto>? OnReceivedTrainingStatus;

        public static ClientControl GetInstance()
        {
            if (_clientControl is null)
            {
                _clientControl = new ClientControl();
            }
            return _clientControl;
        }

        public async Task<List<MoHinh>> GetAllMoHinhAsync()
        {
            var dsMoHinh = await GetAsync<List<MoHinh>>("/api/mo-hinh");
            return dsMoHinh;
        }

        public async Task<List<TapDuLieu>> GetAllTapDuLieuAsync()
        {
            var dsTapDuLieu = await GetAsync<List<TapDuLieu>>("/api/tap-du-lieu");
            dsTapDuLieu.ForEach(tapDuLieu =>
            {
                tapDuLieu.DsMau.ForEach(m => m.DuongDanAnh = baseUrl + m.DuongDanAnh);
            });
            return dsTapDuLieu;
        }

        public async Task<ThongTinHL> CreateThongTinHLAsync(ThongTinHL thongTinHL)
        {
            var data = await PostAsync<ThongTinHL>("/api/huan-luyen", thongTinHL);
            return data;
        }

        public async Task StartListeningAsync(int trackingId)
        {
            // Xoas connection cux
            if (connection != null)
            {
                await ClearConnectionAsync();
            }

            connection = new HubConnectionBuilder()
                .WithUrl(baseUrl + "/training-status?trackingId=" + trackingId)
                .WithAutomaticReconnect()
                .Build();

            // Bắt status đến cho Listener
            connection.On<TrainingStatusDto>("OnHasNewStatus", status =>
            {
                OnReceivedTrainingStatus?.Invoke(this, status);
            });

            await connection.StartAsync();
        }

        public async Task StartTrainingAsync(int thongTinHLId)
        {
            await PostAsync($"/api/huan-luyen/{thongTinHLId}/bat-dau", new {});
        }

        public async Task<ThongTinHL> GetTrainingResultAsync(int thongTinHLId)
        {
            return await GetAsync<ThongTinHL>($"/api/huan-luyen/{thongTinHLId}/ket-qua");
        }

        public async Task<int> CreatePhienBanFromTrainResult(int thongTinHLId)
        {
            return await PostAsync<int>($"/api/huan-luyen/{thongTinHLId}/luu-phien-ban", new {});
        }

        private async Task ClearConnectionAsync()
        {
            if (connection == null) return;
            await connection.StopAsync();
            await connection.DisposeAsync();
            connection = null;
        }

        private async Task<T> PostAsync<T>(string endpoint, object data)
        {
            var json = JsonSerializer.Serialize(data, _option);
            var content = new StringContent(json, Encoding.UTF8, "application/json");

            HttpResponseMessage response = await _http.PostAsync(endpoint, content);
            response.EnsureSuccessStatusCode();

            string responseJson = await response.Content.ReadAsStringAsync();
            return JsonSerializer.Deserialize<T>(responseJson, _option)!;
        }

        private async Task PostAsync(string endpoint, object data)
        {
            var json = JsonSerializer.Serialize(data, _option);
            var content = new StringContent(json, Encoding.UTF8, "application/json");

            HttpResponseMessage response = await _http.PostAsync(endpoint, content);
            response.EnsureSuccessStatusCode();
        }

        private async Task<T> GetAsync<T>(string endpoint)
        {
            HttpResponseMessage response = await _http.GetAsync(endpoint);
            response.EnsureSuccessStatusCode();
            string json = await response.Content.ReadAsStringAsync();
            return JsonSerializer.Deserialize<T>(json, _option)!;
        }
    }
}
