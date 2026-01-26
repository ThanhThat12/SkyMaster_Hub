// package com.example.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;

// import java.util.Arrays;

// @Configuration
// public class CorsConfig {

//     @Bean
//     public CorsFilter corsFilter() {
//         CorsConfiguration config = new CorsConfiguration();
        
//         // Cho phép frontend gọi API (development)
//         config.setAllowedOrigins(Arrays.asList(
//             "http://localhost:3000",     // React default
//             "http://localhost:5173",     // Vite default
//             "http://localhost:4200",     // Angular default
//             "http://localhost:8081"      // Hoặc port khác bạn chọn
//         ));
        
//         // Cho phép các HTTP methods
//         config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
//         // Cho phép headers
//         config.setAllowedHeaders(Arrays.asList("*"));
        
//         // Cho phép credentials (cookies, authorization headers)
//         config.setAllowCredentials(true);
        
//         // Thời gian cache preflight request (seconds)
//         config.setMaxAge(3600L);
        
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/api/**", config);
        
//         return new CorsFilter(source);
//     }
// }
