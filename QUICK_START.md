# 🚀 QUICK START - Tách FE/BE trong 30 phút

## 📋 Checklist Chuẩn bị

- [x] Backend đã có REST Controllers
- [x] CORS config đã được thêm
- [x] API Documentation đã có
- [ ] Tạo Frontend project
- [ ] Test API connection
- [ ] Deploy

---

## ⚡ SPEED RUN (30 phút)

### Phút 1-5: Kiểm tra Backend

```bash
cd C:\Users\TUF GAMING\Desktop\demo

# Chạy backend
mvn spring-boot:run

# Test API (terminal khác)
curl http://localhost:8080/api/delays/stored/count
# Expected: {"success":true,"count":150}
```

**✅ Backend OK nếu thấy JSON response**

---

### Phút 6-10: Tạo Frontend (React + Vite)

```bash
# Quay về Desktop
cd C:\Users\TUF GAMING\Desktop

# Tạo project
npm create vite@latest flight-fe -- --template react

cd flight-fe

# Install dependencies
npm install
npm install axios

# Chạy dev server
npm run dev
```

**✅ Frontend OK nếu thấy: http://localhost:5173**

---

### Phút 11-15: Setup API Service

**Tạo file:** `src/services/api.js`

```javascript
import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8080/api'
});

export const delayAPI = {
  fetch: (type, iataCode, minDelay) => 
    API.post('/delays/fetch', null, { params: { type, iataCode, minDelay } }),
  
  getStored: () => 
    API.get('/delays/stored'),
  
  getCacheInfo: () => 
    API.get('/delays/cache-info')
};

export const realtimeAPI = {
  getFlights: (depIata) => 
    API.get('/realtime-flights', { params: { dep_iata: depIata } })
};

export default API;
```

---

### Phút 16-25: Tạo Component đơn giản

**Thay thế:** `src/App.jsx`

```jsx
import { useState } from 'react';
import { delayAPI } from './services/api';
import './App.css';

function App() {
  const [iataCode, setIataCode] = useState('');
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState(null);

  const fetchDelays = async () => {
    setLoading(true);
    try {
      const res = await delayAPI.fetch('departures', iataCode, 30);
      setFlights(res.data.data);
      setStats(res.data);
    } catch (err) {
      alert('Error: ' + (err.response?.data?.error || err.message));
    }
    setLoading(false);
  };

  return (
    <div className="App">
      <h1>✈️ Flight Delays Tracker</h1>
      
      <div className="search-box">
        <input
          type="text"
          placeholder="Airport Code (e.g., SGN)"
          value={iataCode}
          onChange={e => setIataCode(e.target.value.toUpperCase())}
          onKeyPress={e => e.key === 'Enter' && fetchDelays()}
        />
        <button onClick={fetchDelays} disabled={loading}>
          {loading ? '⏳ Loading...' : '🔍 Search'}
        </button>
      </div>

      {stats && (
        <div className="stats">
          ✅ Found {stats.count} flights in {stats.responseTime}
        </div>
      )}

      <div className="results">
        {flights.map((f, i) => (
          <div key={i} className="flight-card">
            <h3>{f.flightIata}</h3>
            <p>
              {f.depIata} → {f.arrIata}
              <br />
              Delay: <strong>{f.delayMinutes} min</strong>
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;
```

---

### Phút 26-28: Quick CSS

**Update:** `src/App.css`

```css
.App {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-box {
  display: flex;
  gap: 10px;
  margin: 20px 0;
}

.search-box input {
  flex: 1;
  padding: 12px;
  font-size: 16px;
  border: 2px solid #ddd;
  border-radius: 8px;
}

.search-box button {
  padding: 12px 24px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
}

.search-box button:disabled {
  background: #ccc;
}

.stats {
  padding: 12px;
  background: #d4edda;
  border-radius: 8px;
  margin: 10px 0;
}

.results {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.flight-card {
  border: 1px solid #ddd;
  padding: 15px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.flight-card h3 {
  margin: 0 0 10px 0;
  color: #007bff;
}

.flight-card strong {
  color: #dc3545;
}
```

---

### Phút 29-30: Test!

```bash
# Terminal 1: Backend đang chạy
# Terminal 2: Frontend đang chạy

# Mở browser: http://localhost:5173
# Nhập: SGN
# Click Search
# ✅ Xem kết quả!
```

---

## 🎯 Kết quả sau 30 phút

✅ Backend API running: `http://localhost:8080/api`  
✅ Frontend running: `http://localhost:5173`  
✅ CORS configured  
✅ API calls working  
✅ Data rendering  

---

## 📚 Tài liệu đã tạo

1. ✅ `API_DOCUMENTATION.md` - API specs
2. ✅ `FRONTEND_MIGRATION_GUIDE.md` - Chi tiết setup FE
3. ✅ `DEPLOYMENT_GUIDE.md` - Hướng dẫn deploy
4. ✅ `CorsConfig.java` - CORS configuration

---

## 🔥 Next Steps (Tùy chọn)

### Ngay lập tức (1 giờ):
- [ ] Add Search functionality
- [ ] Add Realtime Flights page
- [ ] Add routing (React Router)
- [ ] Add loading spinners

### Trong tuần (5-10 giờ):
- [ ] Professional UI (Tailwind/Material-UI)
- [ ] Charts & visualizations
- [ ] Error handling
- [ ] Responsive design
- [ ] Tests

### Trong tháng (20-40 giờ):
- [ ] Authentication
- [ ] User preferences
- [ ] Advanced caching strategies
- [ ] WebSocket real-time updates
- [ ] Mobile app (React Native)
- [ ] Deploy to production

---

## 🆘 Troubleshooting

### ❌ CORS Error
```
Access to XMLHttpRequest has been blocked by CORS policy
```

**Fix:** Kiểm tra `CorsConfig.java` đã có `http://localhost:5173`

---

### ❌ Connection Refused
```
ERR_CONNECTION_REFUSED
```

**Fix:** Backend chưa chạy! `mvn spring-boot:run`

---

### ❌ 404 Not Found
```
GET /api/delays/stored 404
```

**Fix:** Check URL trong `api.js`, phải có `/api` prefix

---

## 📞 Support

- **Backend issues:** Check `FlightDelayRestController.java`
- **CORS issues:** Check `CorsConfig.java`
- **Frontend issues:** Check browser console (F12)
- **API docs:** `API_DOCUMENTATION.md`

---

## 🎉 Congratulations!

Bạn đã tách thành công FE/BE! 

**Kiến trúc hiện tại:**
```
React Frontend (Port 5173)
    ↓ REST API ↓
Spring Boot Backend (Port 8080)
    ↓
PostgreSQL Database
```

**Advantages:**
✅ Frontend có thể deploy riêng (Vercel/Netlify)  
✅ Backend có thể scale độc lập  
✅ Dễ thêm mobile app (cùng API)  
✅ Team FE/BE làm việc song song  

**Next:** Đọc `DEPLOYMENT_GUIDE.md` để deploy production! 🚀
