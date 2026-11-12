# Worldfone PBX v3.0 - JACCS (Asterisk 20)

Dự án **Worldfone PBX v3.0** (Java Backend: FastAGI & AMI Event Listener) được tùy chỉnh và nâng cấp phục vụ khách hàng **JACCS**, tương thích với hệ thống tổng đài **Asterisk 20**.

---

## 1. Tổng quan kiến trúc

Dự án bao gồm 2 module chính chạy trên **Apache Tomcat**:

1. **`AGI_PBX_v3.0` (FastAGI Server)**
   - Lắng nghe kết nối FastAGI từ Asterisk (mặc định port `4573`).
   - Xử lý kịch bản cuộc gọi: định tuyến cuộc gọi vào/ra (`crminbound`, `crmoutbound`, `crmoutbound_acd`, `outbound`, `lcr`), IVR (`ivrMenu`, `ivrPlayback`), hàng đợi (`queueAfterTime`, `queueFail`, `queue_status`), kiểm tra DNC/VIP (`dncVip`), ghi âm (`recording`), xác thực (`authenlogin`, `pincode`) và tích hợp External CRM (`check_externalcrm`).
2. **`eCRM_PBX_v3.0` (AMI Event Listener & eCRM Service)**
   - Kết nối tới Asterisk Manager Interface (AMI) thông qua `WFManagerEventListener` & `ManagerEventListenerThread`.
   - Thu thập và chuẩn hóa sự kiện cuộc gọi trên **Asterisk 20** (Dial, Bridge, Hangup, Queue, CDR, UserEvent...).
   - Đẩy sự kiện real-time qua **Beanstalkd** (`JobExternal`, `JobInternal`, `JobCdrEvent`, `JobAcsEvent`) và lưu trạng thái trên **Redis** / **Memcached** để đồng bộ với hệ thống CRM của JACCS.

---

## 2. Cấu trúc thư mục

```text
dev-java-v3-asterisk20/
├── AGI_PBX_v3.0/                  # Module FastAGI Server (NetBeans / Ant Web Project)
│   ├── src/java/com/worldfone/
│   │   ├── access/                # Kết nối MySQL, Redis, Redis DNC, DBSelectionSet
│   │   ├── agi/                   # Các script xử lý FastAGI (crmoutbound, crminbound, lcr,...)
│   │   ├── cache/                 # Memcached client
│   │   ├── callback/              # Xử lý gọi lại & HTTP callback
│   │   ├── queue/                 # Producer đẩy job vào Beanstalkd
│   │   └── utils/                 # Thư viện tiện ích, mã hóa, license
│   └── build.xml                  # Ant build script
├── eCRM_PBX_v3.0/                 # Module AMI Listener & eCRM Integration
│   ├── src/java/com/worldfonecc/
│   │   ├── admin/                 # Kết nối MySQL, Redis, DBSelectionSet
│   │   ├── ami/                   # Lắng nghe và xử lý sự kiện AMI Asterisk 20
│   │   ├── cache/                 # Memcached client
│   │   ├── callback/              # Callback tích hợp CRM
│   │   └── queue/                 # Các worker/job đẩy sự kiện sang Beanstalkd
│   └── build.xml                  # Ant build script
├── AGI_PBX_Config.properties      # File cấu hình mẫu cho AGI_PBX_v3.0
├── ECRM_PBX_Config.properties     # File cấu hình mẫu cho eCRM_PBX_v3.0
└── README.md                      # Tài liệu hướng dẫn dự án
```

---

## 3. Yêu cầu hệ thống

- **Java**: JDK 8
- **Application Server**: Apache Tomcat 9 (`/opt/tomcat9`)
- **Tổng đài**: Asterisk 20 (kích hoạt FastAGI và AMI)
- **Cơ sở dữ liệu & Cache / Queue**:
  - **MySQL**: Lưu cấu hình PBX, định tuyến, CDR, thông tin kết nối CRM
  - **Redis**: Lưu trạng thái kênh, cuộc gọi, DNC (mặc định port `6799`)
  - **Memcached**: Cache dữ liệu truy vấn nhanh (mặc định port `11211`)
  - **Beanstalkd**: Message Queue xử lý sự kiện bất đồng bộ (mặc định port `11300`)

---

## 4. Cấu hình hệ thống

Cả hai module đã được tách cấu hình ra ngoài thư mục triển khai của Tomcat để thuận tiện vận hành mà không cần build lại code:

- File cấu hình AGI: `/opt/tomcat9/webapps/AGI_PBX_Config.properties`
- File cấu hình eCRM: `/opt/tomcat9/webapps/ECRM_PBX_Config.properties`

### Các thông số chính trong file `.properties`:

```properties
# Log4j
log4j.enable=true

# AGI Server Config
agi.ip=127.0.0.1
agi.port=4573

# MySQL Database Connector Config
db.host=127.0.0.1
db.port=3306
db.name=pbx
db.user=root
db.pass=<MAT_KHAU_DB>

# Redis Connector Config
redis.host=127.0.0.1
redis.port=6799
redis.timeout=30000
redis.pwd=<MAT_KHAU_REDIS>

# Beanstalkd
beanstalkd.host=127.0.0.1
beanstalkd.port=11300

# Memcached
HOST=localhost:11211
```

> **Lưu ý**: Nếu không tìm thấy file cấu hình tại `/opt/tomcat9/webapps/`, ứng dụng sẽ ghi log cảnh báo và sử dụng cấu hình mặc định trong code (`MysqlConnection.java`).

---

## 5. Hướng dẫn Build & Triển khai (Deployment)

### Build bằng NetBeans hoặc Apache Ant

1. Mở từng project `AGI_PBX_v3.0` và `eCRM_PBX_v3.0` bằng **NetBeans IDE** (hoặc chạy `ant dist` trong từng thư mục).
2. Sau khi Clean & Build thành công, file `.war` sẽ được sinh ra tại:
   - `AGI_PBX_v3.0/dist/AGI_PBX_v3.0.war`
   - `eCRM_PBX_v3.0/dist/eCRM_PBX_v3.0.war`

### Triển khai lên máy chủ Tomcat

1. Copy 2 file cấu hình `AGI_PBX_Config.properties` và `ECRM_PBX_Config.properties` vào `/opt/tomcat9/webapps/` và cập nhật thông tin kết nối thực tế của môi trường **JACCS**.
2. Copy các file `.war` vào `/opt/tomcat9/webapps/`.
3. Khởi động lại dịch vụ Tomcat và kiểm tra log tại `/opt/tomcat9/logs/catalina.out`.

---

## 6. Các điểm cập nhật chính cho JACCS (Asterisk 20)

- **Tương thích Asterisk 20 AMI & FastAGI**: Cập nhật logic bắt sự kiện kênh, bridge, dial status và xử lý biến kênh trong `WFManagerEventListener.java` và `crmoutbound.java`.
- **Tích hợp External CRM**: Bổ sung kiểm tra và đẩy sự kiện cuộc gọi outbound/inbound cho CRM trong `check_externalcrm.java` và `crmoutbound.java`.
- **Tách file cấu hình độc lập**: Đọc trực tiếp từ `/opt/tomcat9/webapps/AGI_PBX_Config.properties` và `/opt/tomcat9/webapps/ECRM_PBX_Config.properties` kèm log kiểm tra trạng thái kết nối rõ ràng khi khởi động.

---

## 7. Thông tin phát triển

- **Khách hàng**: JACCS
- **Nền tảng tổng đài**: Asterisk 20
- **Đơn vị phát triển**: South Telecom / Worldfone (Developer: Nguyen Qui Ho)
