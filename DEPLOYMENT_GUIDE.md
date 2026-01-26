# DEPLOYMENT GUIDE - FE/BE Separated Architecture

## 🏗️ Architecture Overview

```
Frontend (Static Hosting)          Backend (Container/Server)
    ↓                                      ↓
Vercel/Netlify/S3                    Render/Railway/AWS
Port: 443 (HTTPS)                    Port: 8080

         ↓ REST API Calls ↓
         
         PostgreSQL Database
         (Render/AWS RDS)
```

---

## 🎯 BACKEND DEPLOYMENT

### Option 1: Docker + Render (Recommended)

**1. Update Dockerfile (đã có)**
```dockerfile
# Multi-stage build đã optimize
FROM maven:3.9-eclipse-temurin-17 AS build
...
```

**2. Update CORS for Production**

Edit `CorsConfig.java`:
```java
// Add production URLs
config.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:5173",
    "https://your-frontend.vercel.app",  // ← Thêm này
    "https://your-domain.com"             // ← Hoặc custom domain
));
```

**3. Environment Variables**

Create `.env.production`:
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/airlab
SPRING_DATASOURCE_USERNAME=airlab_user
SPRING_DATASOURCE_PASSWORD=your-password
AIRLABS_API_KEY=your-api-key
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

**4. Deploy to Render**
```bash
# Push to GitHub
git add .
git commit -m "Add CORS and production config"
git push origin main

# Render sẽ auto-deploy từ GitHub
# Hoặc manual: docker push registry.render.com/...
```

---

### Option 2: Railway

**railway.toml**:
```toml
[build]
builder = "DOCKERFILE"

[deploy]
startCommand = ""
healthcheckPath = "/actuator/health"
healthcheckTimeout = 100
```

Deploy:
```bash
railway login
railway up
```

---

## 🎨 FRONTEND DEPLOYMENT

### Option 1: Vercel (Best for Next.js/React)

**1. Create `vercel.json`**:
```json
{
  "version": 2,
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/static-build",
      "config": {
        "distDir": "dist"
      }
    }
  ]
}
```

**2. Environment Variables**

Create `.env.production`:
```env
VITE_API_BASE_URL=https://your-backend.onrender.com/api
```

Update `api.js`:
```javascript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
```

**3. Deploy**:
```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
cd flight-schedules-fe
vercel --prod
```

---

### Option 2: Netlify

**netlify.toml**:
```toml
[build]
  command = "npm run build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200

[build.environment]
  VITE_API_BASE_URL = "https://your-backend.onrender.com/api"
```

Deploy:
```bash
npm run build
netlify deploy --prod
```

---

### Option 3: AWS S3 + CloudFront

**Build and Upload**:
```bash
# Build
npm run build

# Upload to S3
aws s3 sync dist/ s3://your-bucket-name --delete

# Invalidate CloudFront cache
aws cloudfront create-invalidation --distribution-id YOUR_DIST_ID --paths "/*"
```

---

## 🔐 Security Enhancements

### 1. Update Backend CORS (Production)

**CorsConfig.java**:
```java
@Configuration
public class CorsConfig {

    @Value("${cors.allowed.origins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Parse from environment variable
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        
        return new CorsFilter(source);
    }
}
```

**application.properties**:
```properties
# Production
cors.allowed.origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

---

### 2. Add Rate Limiting (Optional)

**pom.xml**:
```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.1.0</version>
</dependency>
```

**RateLimitConfig.java**:
```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
```

---

## 📊 Monitoring Setup

### 1. Add Actuator (Backend)

**pom.xml**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**application.properties**:
```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

Access: `https://your-backend.com/actuator/health`

---

### 2. Frontend Error Tracking (Sentry)

```bash
npm install @sentry/react
```

**main.jsx**:
```javascript
import * as Sentry from "@sentry/react";

Sentry.init({
  dsn: "your-sentry-dsn",
  environment: "production",
});
```

---

## 🚀 CI/CD Pipeline (GitHub Actions)

**`.github/workflows/deploy.yml`**:
```yaml
name: Deploy Backend

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          
      - name: Build with Maven
        run: mvn clean package -DskipTests
        
      - name: Build Docker Image
        run: docker build -t ${{ secrets.DOCKER_USERNAME }}/flight-backend:latest .
        
      - name: Push to Docker Hub
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker push ${{ secrets.DOCKER_USERNAME }}/flight-backend:latest
          
      - name: Deploy to Render
        run: |
          curl -X POST ${{ secrets.RENDER_DEPLOY_HOOK }}
```

---

## ✅ Deployment Checklist

### Backend:
- [ ] Update CORS với production URLs
- [ ] Set environment variables
- [ ] Enable HTTPS
- [ ] Add health check endpoint
- [ ] Configure logging
- [ ] Set up database backups
- [ ] Add monitoring (Actuator)

### Frontend:
- [ ] Update API base URL
- [ ] Build optimization (minify, tree-shake)
- [ ] Add CDN for assets
- [ ] Configure caching headers
- [ ] Add error tracking (Sentry)
- [ ] Test on multiple browsers
- [ ] Add analytics (Google Analytics)

### Database:
- [ ] Enable SSL connection
- [ ] Set up automated backups
- [ ] Configure connection pooling
- [ ] Add read replicas (nếu cần)

---

## 📈 Performance Optimization

### Backend:
```properties
# application.properties
spring.jpa.hibernate.ddl-auto=validate  # Không auto-update schema
spring.jpa.show-sql=false               # Tắt SQL logging
server.compression.enabled=true         # Enable GZIP compression
```

### Frontend:
```javascript
// Code splitting
const Delays = React.lazy(() => import('./components/Delays'));

// Use Suspense
<Suspense fallback={<Loading />}>
  <Delays />
</Suspense>
```

---

## 🔄 Rollback Plan

### Backend:
```bash
# Tag before deploy
git tag -a v1.0.0 -m "Production release"
git push origin v1.0.0

# Rollback
docker pull your-username/flight-backend:v1.0.0
docker run -d -p 8080:8080 your-username/flight-backend:v1.0.0
```

### Frontend:
```bash
# Vercel
vercel rollback

# Netlify
netlify deploy --alias previous-deploy-id
```

---

## 📞 Support & Monitoring URLs

**Production URLs** (sau khi deploy):
```
Frontend: https://flight-app.vercel.app
Backend API: https://flight-api.onrender.com/api
Health Check: https://flight-api.onrender.com/actuator/health
Database: postgres://user@host:5432/db
```

**Monitoring Dashboards**:
- Render: https://dashboard.render.com
- Vercel: https://vercel.com/dashboard
- Database: https://dashboard.render.com (PostgreSQL metrics)

---

## 🎯 Cost Estimation (Monthly)

| Service | Free Tier | Paid |
|---------|-----------|------|
| Render (Backend) | $0 (750 hrs) | $7/month |
| Vercel (Frontend) | $0 (100GB bandwidth) | $20/month |
| PostgreSQL | $0 (limited) | $7/month |
| **Total** | **$0-14** | **$34/month** |

---

Good luck với deployment! 🚀
