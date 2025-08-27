package com.happyvr.security;

import com.happyvr.entity.User;
import com.happyvr.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("正在加载用户: {}", usernameOrEmail);
        
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> {
                    logger.warn("用户不存在: {}", usernameOrEmail);
                    return new UsernameNotFoundException("用户不存在: " + usernameOrEmail);
                });
        
        if (!user.isEnabled()) {
            logger.warn("用户已被禁用: {}", usernameOrEmail);
            throw new UsernameNotFoundException("用户已被禁用: " + usernameOrEmail);
        }
        
        logger.debug("成功加载用户: {}, 角色数量: {}", user.getUsername(), user.getRoles().size());
        return UserPrincipal.create(user);
    }
    
    /**
     * 根据用户ID加载用户详情
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        logger.debug("正在根据ID加载用户: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("用户ID不存在: {}", id);
                    return new UsernameNotFoundException("用户ID不存在: " + id);
                });
        
        if (!user.isEnabled()) {
            logger.warn("用户已被禁用: {}", user.getUsername());
            throw new UsernameNotFoundException("用户已被禁用: " + user.getUsername());
        }
        
        logger.debug("成功根据ID加载用户: {}", user.getUsername());
        return UserPrincipal.create(user);
    }
}