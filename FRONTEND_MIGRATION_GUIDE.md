# FRONTEND MIGRATION GUIDE

## 📦 Tạo Frontend Project (Chọn 1 trong các options)

### Option 1: React + Vite (Recommended)
```bash
# Tạo project
npm create vite@latest flight-schedules-fe -- --template react

cd flight-schedules-fe
npm install

# Install dependencies
npm install axios
npm install react-router-dom

# Run dev server
npm run dev
# → http://localhost:5173
```

---

### Option 2: Next.js (React Framework)
```bash
npx create-next-app@latest flight-schedules-fe
cd flight-schedules-fe
npm install axios
npm run dev
# → http://localhost:3000
```

---

### Option 3: Vue 3
```bash
npm create vue@latest flight-schedules-fe
cd flight-schedules-fe
npm install
npm install axios
npm run dev
```

---

## 🔧 Setup API Service (React Example)

### 1. Tạo API service file

**File:** `src/services/api.js`

```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Axios instance với config mặc định
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor để log requests
apiClient.interceptors.request.use(
  (config) => {
    console.log('📤 API Request:', config.method.toUpperCase(), config.url);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor để handle responses
apiClient.interceptors.response.use(
  (response) => {
    console.log('✅ API Response:', response.data);
    return response;
  },
  (error) => {
    console.error('❌ API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default apiClient;
```

---

### 2. Tạo Delays Service

**File:** `src/services/delayService.js`

```javascript
import apiClient from './api';

export const delayService = {
  
  // Fetch delays từ API
  fetchDelays: async (type, iataCode, minDelay = 30) => {
    try {
      const response = await apiClient.post('/delays/fetch', null, {
        params: { type, iataCode, minDelay }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Search delays trong DB
  searchDelays: async (iataCode, airline, depIata) => {
    try {
      const response = await apiClient.get('/delays/search', {
        params: { iataCode, airline, depIata }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Get tất cả stored delays
  getStoredDelays: async () => {
    try {
      const response = await apiClient.get('/delays/stored');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Get cache info
  getCacheInfo: async () => {
    try {
      const response = await apiClient.get('/delays/cache-info');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Clear cache
  clearCache: async () => {
    try {
      const response = await apiClient.post('/delays/clear-cache');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  }
};
```

---

### 3. Tạo Realtime Flights Service

**File:** `src/services/realtimeService.js`

```javascript
import apiClient from './api';

export const realtimeService = {
  
  // Get realtime flights
  getRealtimeFlights: async (depIata) => {
    try {
      const response = await apiClient.get('/realtime-flights', {
        params: { dep_iata: depIata }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Get cache info
  getCacheInfo: async () => {
    try {
      const response = await apiClient.get('/realtime-flights/cache-info');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  // Clear cache
  clearCache: async () => {
    try {
      const response = await apiClient.post('/realtime-flights/clear-cache');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  }
};
```

---

## 🎨 Component Examples

### Example 1: Delays Component

**File:** `src/components/Delays.jsx`

```jsx
import React, { useState } from 'react';
import { delayService } from '../services/delayService';

function Delays() {
  const [type, setType] = useState('departures');
  const [iataCode, setIataCode] = useState('');
  const [minDelay, setMinDelay] = useState(30);
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [cacheInfo, setCacheInfo] = useState(null);

  const handleFetch = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const result = await delayService.fetchDelays(type, iataCode, minDelay);
      
      if (result.success) {
        setFlights(result.data);
        console.log(`✅ Fetched ${result.count} flights in ${result.responseTime}`);
      }
    } catch (err) {
      setError(err.error || 'Failed to fetch delays');
    } finally {
      setLoading(false);
    }
  };

  const loadCacheInfo = async () => {
    try {
      const result = await delayService.getCacheInfo();
      setCacheInfo(result.cache);
    } catch (err) {
      console.error('Failed to load cache info:', err);
    }
  };

  return (
    <div className="delays-container">
      <h1>Flight Delays</h1>

      {/* Fetch Form */}
      <form onSubmit={handleFetch}>
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="departures">Departures</option>
          <option value="arrivals">Arrivals</option>
        </select>

        <input
          type="text"
          placeholder="IATA Code (e.g., SGN)"
          value={iataCode}
          onChange={(e) => setIataCode(e.target.value)}
          required
        />

        <input
          type="number"
          placeholder="Min Delay (minutes)"
          value={minDelay}
          onChange={(e) => setMinDelay(e.target.value)}
          min="0"
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Fetching...' : 'Fetch Delays'}
        </button>
      </form>

      {/* Error Message */}
      {error && <div className="error">{error}</div>}

      {/* Results */}
      {flights.length > 0 && (
        <div className="results">
          <h2>Results: {flights.length} flights</h2>
          <table>
            <thead>
              <tr>
                <th>Flight</th>
                <th>Airline</th>
                <th>From</th>
                <th>To</th>
                <th>Dep Time</th>
                <th>Delay (min)</th>
              </tr>
            </thead>
            <tbody>
              {flights.map((flight) => (
                <tr key={flight.id}>
                  <td>{flight.flightIata}</td>
                  <td>{flight.airlineIata}</td>
                  <td>{flight.depIata}</td>
                  <td>{flight.arrIata}</td>
                  <td>{flight.depTime}</td>
                  <td className="delay">{flight.delayMinutes}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Cache Info */}
      <div className="cache-section">
        <button onClick={loadCacheInfo}>Show Cache Info</button>
        
        {cacheInfo && (
          <div className="cache-info">
            <h3>Cache Statistics</h3>
            <p>Size: {cacheInfo.size}/{cacheInfo.capacity}</p>
            <p>Hit Rate: {cacheInfo.hitRate}</p>
            <p>Evictions: {cacheInfo.evictionCount}</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default Delays;
```

---

### Example 2: Realtime Flights Component

**File:** `src/components/RealtimeFlights.jsx`

```jsx
import React, { useState, useEffect } from 'react';
import { realtimeService } from '../services/realtimeService';

function RealtimeFlights() {
  const [depIata, setDepIata] = useState('');
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchFlights = async () => {
    if (!depIata) return;

    setLoading(true);
    try {
      const result = await realtimeService.getRealtimeFlights(depIata);
      
      if (result.success) {
        setFlights(result.data);
      }
    } catch (err) {
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Auto-refresh every 30 seconds
  useEffect(() => {
    if (depIata) {
      fetchFlights();
      const interval = setInterval(fetchFlights, 30000);
      return () => clearInterval(interval);
    }
  }, [depIata]);

  return (
    <div>
      <h1>Realtime Flights</h1>
      
      <input
        type="text"
        placeholder="Departure Airport (e.g., SGN)"
        value={depIata}
        onChange={(e) => setDepIata(e.target.value.toUpperCase())}
      />
      
      <button onClick={fetchFlights} disabled={loading}>
        {loading ? 'Loading...' : 'Fetch Flights'}
      </button>

      {flights.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>Flight</th>
              <th>Status</th>
              <th>Altitude</th>
              <th>Speed</th>
              <th>Position</th>
            </tr>
          </thead>
          <tbody>
            {flights.map((flight) => (
              <tr key={flight.id}>
                <td>{flight.flightIata}</td>
                <td>{flight.status}</td>
                <td>{flight.alt} ft</td>
                <td>{flight.speed} kts</td>
                <td>{flight.lat}, {flight.lng}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default RealtimeFlights;
```

---

## 🚀 Running Both Projects

### Terminal 1: Backend
```bash
cd demo
mvn spring-boot:run
# → http://localhost:8080
```

### Terminal 2: Frontend
```bash
cd flight-schedules-fe
npm run dev
# → http://localhost:5173
```

---

## ✅ Testing API Connection

Trong browser console (F12):
```javascript
// Test fetch delays
fetch('http://localhost:8080/api/delays/fetch?type=departures&iataCode=SGN&minDelay=30', {
  method: 'POST'
})
  .then(res => res.json())
  .then(data => console.log(data));

// Test realtime flights
fetch('http://localhost:8080/api/realtime-flights?dep_iata=SGN')
  .then(res => res.json())
  .then(data => console.log(data));
```

---

## 📝 Next Steps

1. ✅ Setup CORS trong backend (đã làm)
2. ✅ Tạo frontend project
3. ✅ Install axios
4. ✅ Tạo API services
5. ✅ Tạo components
6. 🎨 Styling với CSS/Tailwind/Material-UI
7. 🗺️ Add routing với React Router
8. 📊 Add charts với Recharts/Chart.js
9. 🔐 Add authentication (nếu cần)
10. 🚀 Deploy frontend lên Vercel/Netlify

---

## 🎯 Advantages of Separation

✅ **Independent Scaling**: FE và BE scale riêng  
✅ **Technology Freedom**: Dễ thay đổi frontend framework  
✅ **Better Performance**: Static hosting cho FE (CDN)  
✅ **Team Collaboration**: FE/BE teams làm song song  
✅ **Mobile Ready**: Dùng chung API cho web + mobile  

---

## ⚠️ Important Notes

- Backend phải run trước khi test frontend
- Kiểm tra CORS config nếu gặp lỗi preflight
- Dùng environment variables cho API URL
- Add loading states và error handling
- Implement retry logic cho API calls failed
