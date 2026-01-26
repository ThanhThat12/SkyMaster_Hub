# API DOCUMENTATION - Flight Schedules Backend

## Base URL
```
http://localhost:8080/api
```

---

## 🛫 FLIGHT DELAYS API

### 1. Fetch Delayed Flights
Lấy danh sách chuyến bay bị delay từ API/Cache/DB

**Endpoint:** `POST /api/delays/fetch`

**Parameters:**
| Param | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| type | string | Yes | departures hoặc arrivals | departures |
| iataCode | string | Yes | Mã sân bay IATA | SGN |
| minDelay | int | No (default: 30) | Delay tối thiểu (phút) | 30 |

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/delays/fetch?type=departures&iataCode=SGN&minDelay=30"
```

**Response:**
```json
{
  "success": true,
  "count": 15,
  "responseTime": "245ms",
  "data": [
    {
      "id": 123,
      "flightIata": "VJ123",
      "airlineIata": "VJ",
      "depIata": "SGN",
      "depTime": "2025-01-26 14:30",
      "arrIata": "HAN",
      "arrTime": "2025-01-26 16:45",
      "delayMinutes": 45
    }
  ]
}
```

---

### 2. Search Delays
Tìm kiếm delays trong database

**Endpoint:** `GET /api/delays/search`

**Parameters:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| iataCode | string | No | Mã sân bay đích |
| airline | string | No | Mã hãng hàng không |
| depIata | string | No | Mã sân bay khởi hành |

**Request Example:**
```bash
curl "http://localhost:8080/api/delays/search?airline=VJ"
```

**Response:**
```json
{
  "success": true,
  "count": 25,
  "searchParams": {
    "iataCode": "null",
    "airline": "VJ",
    "depIata": "null"
  },
  "data": [...]
}
```

---

### 3. Get All Stored Delays
Lấy tất cả delays đã lưu trong DB

**Endpoint:** `GET /api/delays/stored`

**Response:**
```json
{
  "success": true,
  "count": 150,
  "data": [...]
}
```

---

### 4. Get Stored Count
Đếm số lượng delays trong DB

**Endpoint:** `GET /api/delays/stored/count`

**Response:**
```json
{
  "success": true,
  "count": 150
}
```

---

### 5. Get Cache Info
Xem thông tin cache (monitoring)

**Endpoint:** `GET /api/delays/cache-info`

**Response:**
```json
{
  "success": true,
  "cache": {
    "size": 3,
    "capacity": 3,
    "hitCount": 125,
    "missCount": 15,
    "hitRate": "89.29%",
    "evictionCount": 2,
    "utilizationPercent": 100.0,
    "entries": [
      {
        "key": "departures|SGN|30",
        "flightCount": 15,
        "sizeKB": 7.5,
        "hitCount": 45
      }
    ]
  }
}
```

---

### 6. Clear Cache
Xóa toàn bộ cache (admin)

**Endpoint:** `POST /api/delays/clear-cache`

**Response:**
```json
{
  "success": true,
  "message": "Cache cleared"
}
```

---

## ✈️ REALTIME FLIGHTS API

### 1. Get Realtime Flights
Lấy vị trí realtime của chuyến bay

**Endpoint:** `GET /api/realtime-flights`

**Parameters:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| dep_iata | string | Yes | Mã sân bay khởi hành |

**Request Example:**
```bash
curl "http://localhost:8080/api/realtime-flights?dep_iata=SGN"
```

**Response:**
```json
{
  "success": true,
  "count": 12,
  "responseTime": "189ms",
  "depIata": "SGN",
  "data": [
    {
      "id": 1,
      "flightIata": "VJ123",
      "lat": 10.8231,
      "lng": 106.6297,
      "alt": 35000,
      "speed": 450,
      "dir": 270.5,
      "status": "en-route",
      "updated": 1706256000
    }
  ]
}
```

---

### 2. Get Cache Info
**Endpoint:** `GET /api/realtime-flights/cache-info`

### 3. Clear Cache
**Endpoint:** `POST /api/realtime-flights/clear-cache`

### 4. Invalidate Cache Key
**Endpoint:** `POST /api/realtime-flights/invalidate-cache?depIata=SGN`

---

## 🔒 Error Responses

**Bad Request (400):**
```json
{
  "success": false,
  "error": "Invalid IATA code format"
}
```

**Server Error (500):**
```json
{
  "success": false,
  "error": "Database connection failed"
}
```

---

## 📊 Response Time Benchmarks

| Scenario | Response Time |
|----------|---------------|
| Cache HIT | < 50ms |
| DB HIT | 50-200ms |
| API call | 200-500ms |

---

## 🔄 CORS Configuration

Allowed Origins:
- `http://localhost:3000` (React)
- `http://localhost:5173` (Vite)
- `http://localhost:4200` (Angular)

Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
