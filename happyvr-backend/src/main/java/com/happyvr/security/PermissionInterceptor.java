package com.happyvr.security;

import com.happyvr.entity.Role;
import com.happyvr.entity.User;
import com.happyvr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    
    private final UserRepository userRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        
        if (requirePermission == null) {
            requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        
        if (requirePermission == null) {
            return true;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("未认证用户");
        }
        
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new AccessDeniedException("用户不存在");
        }
        
        User user = userOpt.get();
        String requiredPermission = requirePermission.value();
        
        boolean hasPermission = user.getRoles().stream()
                .map(Role::getPermissions)
                .filter(permissions -> permissions != null)
                .flatMap(List::stream)
                .anyMatch(permission -> permission.equals(requiredPermission) || permission.equals("*"));
        
        if (!hasPermission) {
            throw new AccessDeniedException("权限不足");
        }
        
        return true;
    }
}