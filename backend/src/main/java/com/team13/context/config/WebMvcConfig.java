package com.team13.context.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private AuthProperties authProperties;

    @Value("${app.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        AuthProperties.VerifyUpload upload = authProperties.getVerifyUpload();
        String prefix = upload.getUrlPrefix();
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        String location = Path.of(upload.getStorageDir()).toAbsolutePath().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler(prefix + "**").addResourceLocations(location);

        String coachPrefix = "/api/files/coach/";
        String coachLocation = Path.of("./data/coach-uploads").toAbsolutePath().toUri().toString();
        if (!coachLocation.endsWith("/")) {
            coachLocation = coachLocation + "/";
        }
        registry.addResourceHandler(coachPrefix + "**").addResourceLocations(coachLocation);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!jwtEnabled || jwtInterceptor == null) {
            return;
        }
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/scenes",
                        "/api/scenes/**",
                        "/api/coaches",
                        "/api/coaches/**",
                        "/api/expert/**",
                        "/api/match/**",
                        "/api/files/verify/**",
                        "/api/v1/auth/captcha",
                        "/api/v1/auth/sms/**",
                        "/api/v1/auth/login/**",
                        "/api/v1/auth/register/**",
                        "/api/v1/auth/oauth/**",
                        "/api/v1/posts",
                        "/api/v1/posts/**/comments",
                        "/api/v1/community/expert-tips",
                        "/api/v1/practice/questions/categories",
                        "/api/v1/practice/questions",
                        "/api/v1/trtc/recording-callback");
    }
}
