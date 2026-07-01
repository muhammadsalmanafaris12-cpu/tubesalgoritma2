package com.example.demo.config;

import com.example.demo.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // Exclude login, register, resources, and console from session checking
        if (path.startsWith("/login") || path.startsWith("/register") || path.startsWith("/css/") || 
            path.startsWith("/js/") || path.startsWith("/images/") || path.equals("/") || 
            path.equals("/error") || path.startsWith("/h2-console")) {
            return true;
        }

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Role-based path authorization
        if (path.startsWith("/admin") && !"ADMIN".equals(user.getRole())) {
            response.sendRedirect("/user/dashboard");
            return false;
        }

        if (path.startsWith("/user") && !"USER".equals(user.getRole())) {
            response.sendRedirect("/admin/dashboard");
            return false;
        }

        return true;
    }
}
