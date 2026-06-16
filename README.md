# Hệ thống Phát hiện và Nhận diện Biển báo Giao thông (Traffic Sign Detection System)

## 1. Giới thiệu Tổng quan
Dự án **Traffic Sign Detector** là một hệ thống phần mềm phân tán được thiết kế theo kiến trúc Vi dịch vụ (Microservices Architecture). Mục tiêu cốt lõi của hệ thống là cung cấp một nền tảng toàn diện cho việc tự động hóa quy trình quản lý tập dữ liệu, cấu hình, và huấn luyện các mô hình học sâu (Deep Learning) chuyên biệt trong bài toán nhận diện biển báo giao thông. Hệ thống phục vụ cho các nghiên cứu về hệ thống giao thông thông minh (Intelligent Transportation Systems - ITS) và xe tự hành.

## 2. Kiến trúc Hệ thống (System Architecture)
Hệ thống được thiết kế theo mô hình client-server với backend được phân rã thành các dịch vụ độc lập, đảm bảo tính mở rộng (scalability), tính sẵn sàng cao (high availability) và khả năng bảo trì (maintainability).

### 2.1. Lớp Tương tác Người dùng (Client-Side)
- **`client3/`**: Nền tảng Frontend được phát triển trên hệ sinh thái **Vue.js** kết hợp với **Vite**. Cung cấp giao diện tương tác (UI) trực quan cho phép người dùng (nhà nghiên cứu/kỹ sư dữ liệu) tương tác với hệ thống, quản lý cấu hình huấn luyện và theo dõi kết quả.

### 2.2. Lớp Dịch vụ Lõi (Backend Microservices - `server5/`)
Được phát triển dựa trên nền tảng **Java Spring Boot / Spring Cloud**, bao gồm các dịch vụ thành phần:
- **`api-gateway/`**: Cổng giao tiếp API, định tuyến (routing) và kiểm soát truy cập từ phía client tới các dịch vụ nội bộ.
- **`discovery-server/`**: Máy chủ khám phá dịch vụ (Service Registry), hỗ trợ cơ chế service discovery cho kiến trúc phân tán.
- **`config-server/`**: Máy chủ quản lý cấu hình tập trung (Centralized Configuration).
- **`dataset-service/`**: Dịch vụ quản lý vòng đời của các tập dữ liệu (dataset) dùng cho huấn luyện.
- **`aimodel-service/`**: Dịch vụ quản lý siêu dữ liệu và các phiên bản của mô hình AI.
- **`training-orchestrator-service/`**: Dịch vụ điều phối quá trình huấn luyện, quản lý luồng sự kiện và đồng bộ trạng thái giữa backend và AI worker.

### 2.3. Lớp Xử lý Tính toán AI (AI Processing Worker)
- **`ai-training-service/`**: Một module độc lập được viết bằng **Python**. Đóng vai trò là worker thực thi các tác vụ huấn luyện mô hình học sâu (như YOLO) dựa trên dữ liệu đầu vào. Worker này tương tác với hệ thống thông qua các cơ chế bất đồng bộ hoặc REST API.

## 3. Công nghệ và Công cụ (Technologies & Stack)
- **Frontend**: Vue.js, TypeScript, Vite.
- **Backend**: Java 17+, Spring Boot, Spring Cloud (Gateway, Eureka, Config).
- **AI/Machine Learning**: Python, PyTorch/YOLO (hoặc tương đương).
- **Triển khai (Deployment)**: Docker hóa các dịch vụ, hỗ trợ tự động hóa triển khai trên cụm **Kubernetes (K8s)** (cấu hình nằm tại `server5/k8s/`).

## 4. Hướng dẫn Cài đặt và Vận hành
*Lưu ý: Chi tiết về cấu hình cài đặt cục bộ (local) cho từng vi dịch vụ được mô tả bên trong tệp `README` của từng thư mục tương ứng.*
1. Yêu cầu hệ thống: Docker, Kubernetes cluster (nếu triển khai production), Java JDK 17, Node.js, Python 3.x.
2. Quá trình build có thể thực hiện thông qua script `build-local-images.bat`.
3. Triển khai các dịch vụ lên Kubernetes sử dụng các kịch bản trong thư mục `k8s`.