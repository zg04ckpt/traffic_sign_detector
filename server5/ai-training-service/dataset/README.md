# Dataset Directory

Thư mục này chứa dữ liệu (dataset) dùng để huấn luyện mô hình nhận diện biển báo giao thông.

**Cấu trúc thư mục:**
- `images/`: Chứa các file ảnh đầu vào của dataset. (Lưu ý: Các file ảnh đã được ignore trên Git để tránh làm phình to dung lượng repository).
- `labels/`: Chứa các file nhãn (labels) tương ứng với từng ảnh, thường dưới dạng `.txt` (YOLO format) hoặc định dạng tương tự.
- `classes.txt`: Chứa danh sách tên các phân lớp (classes) biển báo giao thông.
- `custom_data.yaml`: File cấu hình định nghĩa đường dẫn tới tập train/val và số lượng/tên các classes dùng cho việc huấn luyện.

Bạn chỉ nên commit các file cấu hình hoặc nhãn (nếu kích thước nhỏ), không nên commit ảnh thô lên Git.
