# Phụ lục Dữ liệu: Bộ Dữ liệu Huấn luyện Mô hình Nhận diện Biển báo Giao thông (Traffic Sign Recognition Dataset)

## 1. Giới thiệu chung
Thư mục này đóng vai trò là nơi lưu trữ và quản lý tập dữ liệu (dataset) được sử dụng cho quá trình huấn luyện (training) và kiểm định (validation) mô hình học sâu (Deep Learning) trong bài toán nhận diện biển báo giao thông. Tập dữ liệu được tổ chức theo tiêu chuẩn để đảm bảo tính minh bạch, khả năng tái lập (reproducibility) và tính dễ dàng trong việc tích hợp với các hệ thống huấn luyện tự động.

## 2. Cấu trúc Thư mục và Tổ chức Dữ liệu
Cấu trúc tổ chức dữ liệu được thiết kế nhằm tối ưu hóa quy trình tiền xử lý và nạp dữ liệu (data loading):

* **`images/`**: Thư mục lưu trữ các hình ảnh đầu vào (input images). Để tối ưu hóa không gian lưu trữ trên hệ thống quản lý phiên bản (Version Control System) và tránh hiện tượng phình to kho lưu trữ (repository bloat), toàn bộ tệp tin hình ảnh thô đã được cấu hình loại trừ (ignored).
* **`labels/`**: Thư mục chứa các tệp tin định danh (annotations/labels) tương ứng với mỗi hình ảnh. Các tệp tin này tuân thủ theo định dạng chuẩn (ví dụ: định dạng YOLO), trong đó mỗi dòng chứa thông tin về chỉ mục lớp (class index) và các tọa độ hộp giới hạn (bounding box coordinates) đã được chuẩn hóa.
* **`classes.txt`**: Tệp văn bản định nghĩa không gian nhãn (label space), bao gồm danh sách các phân lớp (classes) của hệ thống biển báo giao thông.
* **`custom_data.yaml`**: Tệp tin cấu hình siêu tham số dữ liệu (data configuration metadata). Tệp này xác định đường dẫn phân bổ tới tập huấn luyện (train set) và tập kiểm định (validation set), đồng thời khai báo số lượng phân lớp và tên tương ứng.

## 3. Lưu ý Kỹ thuật và Quản lý Mã nguồn
* Mọi thay đổi về định dạng hay cấu trúc dữ liệu cần được phản ánh đồng bộ vào tệp `custom_data.yaml`.
* Nhằm tuân thủ các nguyên tắc thực hành tốt (best practices) trong quản lý mã nguồn, chỉ các tệp tin siêu dữ liệu (`.yaml`), danh sách lớp (`.txt`) và các tệp định danh kích thước nhỏ mới được cấp quyền commit. Dữ liệu hình ảnh thô phải được cô lập và quản lý bởi các hệ thống lưu trữ ngoại vi hoặc nền tảng dữ liệu chuyên dụng.
