Network Connection Alert System

Học phần: Lập trình mạng máy tính
Khoa: Công nghệ Thông tin – Trường Đại học Công nghệ TP.HCM (HUTECH)
Nhóm thực hiện: Nhóm 07

1. Giới thiệu đề tài

Trong quá trình sử dụng mạng máy tính, việc theo dõi trạng thái kết nối của các máy chủ là rất cần thiết. Nếu máy chủ bị mất kết nối hoặc phản hồi chậm mà không được phát hiện kịp thời, việc sử dụng các dịch vụ mạng có thể bị ảnh hưởng.

Vì vậy, nhóm em thực hiện đề tài Network Connection Alert System nhằm xây dựng một ứng dụng giúp kiểm tra và theo dõi trạng thái kết nối mạng của nhiều máy chủ. Chương trình được viết bằng Java, sử dụng giao diện Swing để hiển thị thông tin, SQLite để lưu dữ liệu và email để thông báo khi phát hiện sự cố.

Các chức năng chính
Kiểm tra kết nối mạng: Sử dụng Ping để kiểm tra khả năng kết nối đến máy chủ và TCP Socket để kiểm tra kết nối đến các cổng như 80, 443.
Kiểm tra nhiều máy chủ cùng lúc: Sử dụng ExecutorService để thực hiện các tác vụ kiểm tra song song, giúp chương trình hoạt động ổn định và không làm đứng giao diện.
Lưu thông tin và lịch sử: Sử dụng SQLite để lưu danh sách máy chủ và kết quả kiểm tra vào file network_monitoring.db.
Hiển thị trạng thái mạng: Giao diện được xây dựng bằng Java Swing, giúp người dùng dễ dàng theo dõi máy chủ đang hoạt động hoặc bị mất kết nối. Chương trình cũng có biểu đồ thể hiện độ trễ mạng.
Gửi email cảnh báo: Khi phát hiện máy chủ chuyển sang trạng thái mất kết nối, chương trình có thể gửi email thông báo để người quản trị kiểm tra và xử lý.

Thông qua đề tài này, nhóm em có cơ hội áp dụng các kiến thức đã học về lập trình mạng, xử lý đa luồng, quản lý cơ sở dữ liệu và xây dựng giao diện bằng Java vào một ứng dụng thực tế.

2. Phân công công việc của các thành viên

Trong quá trình thực hiện, nhóm em chia công việc thành các phần khác nhau để mỗi thành viên phụ trách một chức năng chính. Sau khi hoàn thành, các phần được kết hợp lại thành chương trình hoàn chỉnh.

MSSV	Họ và tên	Công việc thực hiện	Merge Request
2280601960	Trần Bảo Minh (Nhóm trưởng)	Tạo repository trên GitHub, thiết lập nhánh main, cấu hình .gitignore. Thiết kế cơ sở dữ liệu SQLite, xây dựng lớp ServerInfo để lưu thông tin máy chủ và lớp DatabaseManager để làm việc với cơ sở dữ liệu.	MR #5
2380600780	Phạm Tuấn Hùng	Xây dựng chức năng kiểm tra kết nối bằng Ping và TCP Socket. Phát triển chức năng kiểm tra nhiều máy chủ bằng đa luồng và xây dựng phần gửi email cảnh báo khi có sự cố kết nối.	MR #6
2380600740	Phùng Nguyễn Huy Hoàng	Thiết kế giao diện chính bằng Java Swing, hiển thị trạng thái máy chủ bằng màu sắc trên bảng dữ liệu và xây dựng biểu đồ theo dõi độ trễ mạng bằng Java2D.	MR #7

Các thành viên phụ trách những phần riêng nhưng vẫn phối hợp trong quá trình tích hợp và kiểm tra chương trình. Việc sử dụng GitHub giúp nhóm quản lý mã nguồn, theo dõi các thay đổi và kết hợp phần code của từng thành viên thuận tiện hơn.

3. Cấu trúc thư mục dự án

Dự án được tổ chức theo từng lớp chức năng để dễ theo dõi, chỉnh sửa và phát triển thêm. Các file chính được đặt trong thư mục mã nguồn Java của dự án Maven.

Network-Connection-Alert-System/
├── DoAnCuoiKy/
│   ├── src/main/java/com/mycompany/doancuoiky/
│   │   ├── ServerInfo.java
│   │   ├── DatabaseManager.java
│   │   ├── PingService.java
│   │   ├── TcpService.java
│   │   ├── EmailNotifier.java
│   │   ├── MonitoringTask.java
│   │   ├── MonitoringService.java
│   │   ├── StatusCellRenderer.java
│   │   ├── LatencyChartPanel.java
│   │   └── MonitoringFrame.java
│   └── pom.xml
├── .gitignore
└── README.md


Chức năng của các file chính:

ServerInfo.java: Lưu thông tin của máy chủ cần theo dõi.
DatabaseManager.java: Kết nối và thực hiện các thao tác với cơ sở dữ liệu SQLite.
PingService.java: Kiểm tra khả năng kết nối đến máy chủ bằng Ping.
TcpService.java: Kiểm tra kết nối TCP đến các cổng được chỉ định.
EmailNotifier.java: Thực hiện gửi email cảnh báo khi phát hiện sự cố.
MonitoringTask.java: Xử lý việc kiểm tra kết nối của từng máy chủ.
MonitoringService.java: Quản lý các tác vụ kiểm tra song song.
StatusCellRenderer.java: Hiển thị trạng thái máy chủ bằng màu sắc trên bảng.
LatencyChartPanel.java: Vẽ biểu đồ theo dõi độ trễ mạng.
MonitoringFrame.java: Giao diện chính để người dùng theo dõi và sử dụng chương trình.
pom.xml: Khai báo các thư viện và cấu hình cần thiết để xây dựng dự án bằng Maven.

Nhờ cách tổ chức này, mỗi chức năng được đặt trong một file riêng, giúp nhóm dễ quản lý mã nguồn và thuận tiện hơn khi sửa lỗi hoặc bổ sung tính năng.

4. Kết luận

Qua quá trình thực hiện đề tài, nhóm em đã xây dựng ứng dụng theo dõi kết nối mạng với các chức năng kiểm tra trạng thái máy chủ, lưu lịch sử kiểm tra, hiển thị độ trễ và gửi email cảnh báo khi xảy ra sự cố.

Đề tài giúp nhóm vận dụng kiến thức về lập trình mạng máy tính, xử lý đa luồng, cơ sở dữ liệu SQLite và giao diện Java Swing. Bên cạnh đó, quá trình làm việc nhóm trên GitHub cũng giúp các thành viên có thêm kinh nghiệm trong việc quản lý mã nguồn và phối hợp phát triển phần mềm.

Trong thời gian tới, nhóm có thể tiếp tục cải thiện giao diện, bổ sung chức năng thống kê lịch sử kết nối và cho phép người dùng tùy chỉnh thời gian kiểm tra máy chủ.
