# 🔔 Webhook Auto-Deploy Setup Guide

## 📋 Tổng quan

Flow tự động deploy:
```
Push code → GitHub Actions → Build image → Push to Docker Hub → 
Webhook → VM Script → Pull image → Restart containers
```

---

## PHẦN 1: SETUP TRÊN VM

### Bước 1: Copy script lên VM

**Cách 1: Sử dụng SCP (từ máy local)**
```bash
scp webhook-listener-advanced.sh user@VM_IP:~/flight-app/
```

**Cách 2: Tạo trực tiếp trên VM**
```bash
cd ~/flight-app
nano webhook-listener.sh
# Copy nội dung từ webhook-listener-advanced.sh
# Ctrl+O, Enter, Ctrl+X
```

### Bước 2: Đặt secret key

**QUAN TRỌNG:** Tạo 1 secret key mạnh (random string)

```bash
# Tạo random secret
openssl rand -hex 32

# Hoặc
cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 64 | head -n 1
```

Copy kết quả (ví dụ: `a8f3d9e2b7c1f4e6d8a9c2b5e7f1d3a6...`)

Sửa trong script:
```bash
nano ~/flight-app/webhook-listener.sh
```

Tìm dòng:
```bash
WEBHOOK_SECRET="${WEBHOOK_SECRET:-your-secret-key-here-change-this}"
```

Thay thành:
```bash
WEBHOOK_SECRET="${WEBHOOK_SECRET:-a8f3d9e2b7c1f4e6d8a9c2b5e7f1d3a6}"
```

Lưu file.

### Bước 3: Cài đặt netcat (nếu chưa có)

```bash
# Ubuntu/Debian
sudo apt install netcat -y

# CentOS/RHEL
sudo yum install nc -y
```

### Bước 4: Cho phép script chạy

```bash
chmod +x ~/flight-app/webhook-listener.sh
```

### Bước 5: Tạo systemd service

```bash
sudo nano /etc/systemd/system/webhook-deploy.service
```

Copy nội dung (thay YOUR_USERNAME):

```ini
[Unit]
Description=Webhook Auto Deploy Service
After=network.target docker.service
Requires=docker.service

[Service]
Type=simple
User=YOUR_USERNAME
Group=docker
WorkingDirectory=/home/YOUR_USERNAME/flight-app
ExecStart=/bin/bash /home/YOUR_USERNAME/flight-app/webhook-listener.sh
Restart=always
RestartSec=10
Environment="WEBHOOK_SECRET=a8f3d9e2b7c1f4e6d8a9c2b5e7f1d3a6"
Environment="WEBHOOK_PORT=9000"
StandardOutput=append:/var/log/webhook-deploy.log
StandardError=append:/var/log/webhook-deploy.log

[Install]
WantedBy=multi-user.target
```

**Thay:**
- `YOUR_USERNAME` → username Linux của bạn (`whoami`)
- `a8f3d9e2b7c1f4e6d8a9c2b5e7f1d3a6` → secret key bạn tạo ở bước 2

Lưu file.

### Bước 6: Tạo log file

```bash
sudo touch /var/log/webhook-deploy.log
sudo chown $USER:$USER /var/log/webhook-deploy.log
```

### Bước 7: Start service

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable (tự động chạy khi boot)
sudo systemctl enable webhook-deploy.service

# Start service
sudo systemctl start webhook-deploy.service

# Kiểm tra status
sudo systemctl status webhook-deploy.service
```

Nếu thấy **active (running)** màu xanh → OK!

### Bước 8: Mở port 9000

**Ubuntu:**
```bash
sudo ufw allow 9000/tcp
sudo ufw reload
sudo ufw status
```

**CentOS:**
```bash
sudo firewall-cmd --permanent --add-port=9000/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

### Bước 9: Test webhook trên VM

```bash
# Test với secret đúng
curl -X POST http://localhost:9000 \
  -H "X-Webhook-Secret: a8f3d9e2b7c1f4e6d8a9c2b5e7f1d3a6"

# Xem log
tail -f /var/log/webhook-deploy.log
```

Nếu thấy "Valid webhook secret received" → OK!

### Bước 10: Test từ máy ngoài (optional)

Từ máy local:
```bash
curl -X POST http://VM_IP:9000 \
  -H "X-Webhook-Secret: a8f3d9e2b7c1f4e6d8a9c2b5e7f1d3a6"
```

---

## PHẦN 2: SETUP GITHUB SECRETS

### Bước 11: Thêm secrets vào GitHub

1. Vào repo → **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**

**Secret 1:**
- Name: `WEBHOOK_URL`
- Value: `http://YOUR_VM_IP:9000`

**Secret 2:**
- Name: `WEBHOOK_SECRET`  
- Value: `a8f3d9e2b7c1f4e6d8a9c2b5e7f1d3a6` (secret key từ bước 2)

**Secrets đã có:**
- `DOCKER_USERNAME` ✓
- `DOCKER_PASSWORD` ✓

### Bước 12: Cập nhật workflow

File `.github/workflows/docker-build-push.yml` đã được tôi cập nhật.

**Nhớ sửa:**
```yaml
DOCKER_IMAGE_NAME: your-dockerhub-username/flight-schedules
```
→ Thay `your-dockerhub-username`

### Bước 13: Push code

```bash
git add .
git commit -m "Add webhook auto-deploy"
git push
```

---

## PHẦN 3: KIỂM TRA

### Xem logs realtime trên VM

```bash
# Xem webhook logs
tail -f /var/log/webhook-deploy.log

# Xem service status
sudo systemctl status webhook-deploy.service

# Xem Docker logs
docker compose logs -f
```

### Test flow hoàn chỉnh

1. Sửa code bất kỳ (ví dụ trong controller)
2. Push lên GitHub
3. Vào **Actions** tab → xem workflow chạy
4. Trên VM, xem log:
   ```bash
   tail -f /var/log/webhook-deploy.log
   ```
5. Sau 2-3 phút, kiểm tra app đã update chưa:
   ```bash
   docker compose ps
   curl http://localhost:8080
   ```

---

## 🔧 QUẢN LÝ SERVICE

### Xem status
```bash
sudo systemctl status webhook-deploy.service
```

### Stop service
```bash
sudo systemctl stop webhook-deploy.service
```

### Start service
```bash
sudo systemctl start webhook-deploy.service
```

### Restart service
```bash
sudo systemctl restart webhook-deploy.service
```

### Xem logs
```bash
# Logs từ systemd
sudo journalctl -u webhook-deploy.service -f

# Logs từ file
tail -f /var/log/webhook-deploy.log

# Xem 100 dòng cuối
tail -100 /var/log/webhook-deploy.log
```

### Disable auto-start
```bash
sudo systemctl disable webhook-deploy.service
```

---

## 🐛 TROUBLESHOOTING

### Service không start

```bash
# Xem lỗi chi tiết
sudo journalctl -u webhook-deploy.service -n 50

# Kiểm tra script syntax
bash -n ~/flight-app/webhook-listener.sh

# Test chạy thủ công
bash ~/flight-app/webhook-listener.sh
```

### Port 9000 bị chiếm

```bash
# Kiểm tra port đang dùng
sudo lsof -i :9000
sudo netstat -tulpn | grep 9000

# Đổi port (nếu cần)
# Sửa trong service file và script
```

### Webhook không nhận được

```bash
# Kiểm tra firewall
sudo ufw status
sudo iptables -L -n

# Test từ VM
curl -v http://localhost:9000

# Test từ ngoài
curl -v http://VM_IP:9000
```

### Deployment fail

```bash
# Xem logs chi tiết
tail -100 /var/log/webhook-deploy.log

# Kiểm tra Docker
docker compose ps
docker compose logs

# Test manual deploy
cd ~/flight-app
docker compose pull
docker compose up -d
```

### Logs quá nhiều (disk full)

```bash
# Xóa logs cũ
sudo truncate -s 0 /var/log/webhook-deploy.log

# Hoặc setup log rotation
sudo nano /etc/logrotate.d/webhook-deploy
```

Thêm:
```
/var/log/webhook-deploy.log {
    daily
    rotate 7
    compress
    missingok
    notifempty
}
```

---

## 🔒 BẢO MẬT

### Khuyến nghị:

1. **Dùng secret key mạnh** (64+ ký tự random)
2. **Không share secret** ra ngoài
3. **Giới hạn IP** (nếu GitHub Actions có IP cố định):
   ```bash
   # Chỉ cho phép từ IP cụ thể
   sudo ufw allow from GITHUB_IP to any port 9000
   ```
4. **Dùng HTTPS** (setup nginx reverse proxy với SSL)
5. **Monitor logs** thường xuyên
6. **Rotate secrets** định kỳ

### Setup HTTPS (Optional - Production)

```bash
# Cài nginx
sudo apt install nginx -y

# Config reverse proxy với SSL
sudo nano /etc/nginx/sites-available/webhook
```

---

## 📊 MONITORING

### Check service health

```bash
# Service status
systemctl is-active webhook-deploy.service

# Disk usage
df -h

# Memory usage
free -h

# Docker stats
docker stats
```

### Alert script (optional)

Tạo script gửi email khi deploy fail:
```bash
nano ~/flight-app/alert.sh
```

---

## ✅ CHECKLIST

- [ ] Script webhook đã copy lên VM
- [ ] Secret key đã set (mạnh, random)
- [ ] netcat đã cài
- [ ] systemd service đã tạo
- [ ] Service đang chạy (`systemctl status`)
- [ ] Port 9000 đã mở
- [ ] Test webhook từ localhost OK
- [ ] GitHub secrets đã thêm (WEBHOOK_URL, WEBHOOK_SECRET)
- [ ] Workflow đã cập nhật
- [ ] Test push code → auto deploy thành công

---

**Xong! Bây giờ mỗi lần push code, VM sẽ tự động deploy!** 🚀
