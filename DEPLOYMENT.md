# Hướng dẫn Deploy Application lên VM với Docker

## 📋 Tổng quan Flow

```
GitHub Push → GitHub Actions → Build Docker Image → Docker Hub → VM Pull & Run
```

## 🚀 Các bước thực hiện

### 1. Setup Docker Hub và GitHub Secrets

#### A. Tạo tài khoản Docker Hub
- Truy cập: https://hub.docker.com
- Đăng ký/Đăng nhập
- Ghi nhớ username của bạn

#### B. Thêm Secrets vào GitHub Repository
1. Vào repository GitHub của bạn
2. Settings → Secrets and variables → Actions
3. Thêm 2 secrets:
   - `DOCKER_USERNAME`: username Docker Hub của bạn
   - `DOCKER_PASSWORD`: password hoặc Access Token Docker Hub

**Lấy Docker Hub Access Token (khuyến nghị):**
- Docker Hub → Account Settings → Security → New Access Token
- Tạo token với quyền Read & Write
- Copy token và thêm vào GitHub Secrets

#### C. Cập nhật tên Docker Image
Sửa file `.github/workflows/docker-build-push.yml`:
```yaml
env:
  DOCKER_IMAGE_NAME: your-dockerhub-username/flight-schedules
```
→ Thay `your-dockerhub-username` bằng username Docker Hub của bạn

### 2. Test Build Docker Image Locally (Optional)

```bash
# Build image
docker build -t flight-schedules:test .

# Test chạy
docker run -p 8080:8080 flight-schedules:test
```

### 3. Push Code lên GitHub

```bash
git add .
git commit -m "Add Docker and GitHub Actions config"
git push origin branch_of_nhat
```

GitHub Actions sẽ tự động:
- Build project với Maven
- Tạo Docker image
- Push lên Docker Hub

### 4. Setup VM

#### A. Cài đặt Docker trên VM

**Ubuntu/Debian:**
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Cài Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Thêm user vào docker group (không cần sudo)
sudo usermod -aG docker $USER
newgrp docker

# Cài Docker Compose
sudo apt install docker-compose-plugin -y

# Kiểm tra
docker --version
docker compose version
```

**CentOS/RHEL:**
```bash
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
```

#### B. Tạo thư mục project trên VM

```bash
mkdir -p ~/flight-app
cd ~/flight-app
```

#### C. Tạo file docker-compose.yml trên VM

```bash
nano docker-compose.yml
```

Copy nội dung từ file `docker-compose.yml` và **CHÚ Ý**:
- Thay `your-dockerhub-username` bằng username Docker Hub
- Đặt password MySQL nếu cần
- Sửa `SPRING_DATASOURCE_PASSWORD` cho khớp

#### D. Tải schema database

```bash
nano database_schema.sql
```
Copy nội dung từ file `database_schema.sql` vào

### 5. Deploy trên VM

#### Pull và chạy lần đầu:
```bash
cd ~/flight-app

# Pull image mới nhất từ Docker Hub
docker compose pull

# Chạy containers
docker compose up -d

# Xem logs
docker compose logs -f app
```

#### Update khi có code mới:
```bash
cd ~/flight-app

# Pull image mới
docker compose pull

# Restart containers
docker compose down
docker compose up -d

# Hoặc gọn hơn:
docker compose pull && docker compose up -d
```

### 6. Kiểm tra

```bash
# Xem trạng thái containers
docker compose ps

# Xem logs
docker compose logs -f

# Xem logs từng service
docker compose logs -f app
docker compose logs -f mysql

# Truy cập vào container
docker compose exec app bash
docker compose exec mysql bash
```

**Truy cập ứng dụng:**
- http://VM_IP:8080
- http://VM_IP:8080/schedules
- http://VM_IP:8080/delays

### 7. Tự động hóa Pull & Deploy (Optional)

Tạo script tự động pull image mới:

```bash
nano ~/flight-app/update.sh
```

```bash
#!/bin/bash
cd ~/flight-app
echo "Pulling latest image..."
docker compose pull
echo "Restarting services..."
docker compose up -d
echo "Done!"
docker compose ps
```

```bash
chmod +x ~/flight-app/update.sh
```

Chạy khi cần update:
```bash
~/flight-app/update.sh
```

### 8. Cấu hình Firewall (nếu cần)

```bash
# Ubuntu (UFW)
sudo ufw allow 8080/tcp
sudo ufw allow 3306/tcp
sudo ufw reload

# CentOS (Firewalld)
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=3306/tcp
sudo firewall-cmd --reload
```

## 🔧 Troubleshooting

### Lỗi kết nối MySQL:
```bash
# Kiểm tra MySQL đã ready chưa
docker compose logs mysql

# Restart MySQL
docker compose restart mysql
```

### Lỗi port đã sử dụng:
```bash
# Kiểm tra port đang dùng
sudo lsof -i :8080
sudo lsof -i :3306

# Stop service đang dùng hoặc đổi port trong docker-compose.yml
```

### Xem chi tiết logs:
```bash
docker compose logs --tail=100 -f app
```

### Clean up và restart từ đầu:
```bash
docker compose down -v
docker compose up -d
```

## 📊 Monitoring

```bash
# Xem resource usage
docker stats

# Xem disk usage
docker system df

# Clean unused images
docker image prune -a
```

## 🎯 Workflow hoàn chỉnh

1. **Developer** push code lên GitHub
2. **GitHub Actions** tự động build và push Docker image
3. **VM Admin** chạy `docker compose pull && docker compose up -d`
4. **Application** tự động update và restart

---

## 📝 Notes

- Thay tất cả `your-dockerhub-username` bằng username thật của bạn
- Đặt password MySQL mạnh cho production
- Cân nhắc dùng nginx reverse proxy cho production
- Setup backup cho MySQL data volume
- Dùng Docker secrets hoặc .env file cho sensitive data
