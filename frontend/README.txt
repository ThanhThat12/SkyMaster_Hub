# Frontend Thuần HTML/CSS/JS - Hướng dẫn sử dụng

## 🚀 Cách chạy

### Cách 1: Sử dụng Live Server (VSCode Extension)
1. Mở VSCode
2. Install extension "Live Server"
3. Click phải vào `index.html` → "Open with Live Server"
4. Browser tự động mở: http://127.0.0.1:5500

### Cách 2: Sử dụng Python HTTP Server
```bash
cd C:\Users\TUF GAMING\Desktop\demo\frontend
python -m http.server 8081
```
Mở browser: http://localhost:8081

### Cách 3: Sử dụng Node.js http-server
```bash
npm install -g http-server
cd C:\Users\TUF GAMING\Desktop\demo\frontend
http-server -p 8081
```

## ⚙️ Điều kiện tiên quyết

**Backend PHẢI đang chạy:**
```bash
cd C:\Users\TUF GAMING\Desktop\demo
mvn spring-boot:run
```

Backend chạy ở: http://localhost:8080

## ✅ Kiểm tra kết nối

Mở Console (F12) trong browser, gõ:
```javascript
fetch('http://localhost:8080/api/delays/stored/count')
  .then(r => r.json())
  .then(d => console.log(d));
```

Nếu thấy response → OK!

## 🎯 Tính năng

### Tab 1: Delays
- Fetch delays từ API theo airport
- Search delays trong database
- Hiển thị kết quả dạng cards

### Tab 2: Realtime Flights  
- Xem vị trí realtime của chuyến bay
- Thông tin GPS, tốc độ, độ cao

### Tab 3: Cache Stats
- Xem thống kê cache
- Hit rate, evictions, size
- Clear cache

## 📁 Cấu trúc files

```
frontend/
├── index.html    # HTML structure
├── style.css     # All styling  
└── app.js        # JavaScript logic
```

## 🔧 Tùy chỉnh API URL

Nếu backend chạy ở port khác, sửa trong `app.js`:
```javascript
const API_BASE_URL = 'http://localhost:XXXX/api';
```

## 🎨 Ưu điểm

✅ Không cần install Node.js/npm
✅ Không cần build process
✅ Load nhanh
✅ Dễ debug (F12)
✅ Dễ customize CSS
✅ Chạy được mọi nơi (chỉ cần browser)

## 📱 Responsive

Hoạt động tốt trên mobile/tablet/desktop!
