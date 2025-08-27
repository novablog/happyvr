package com.happyvr.repository;

import com.happyvr.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户Repository测试类
 */
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testFindByUsername() {
        // 创建测试用户
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        entityManager.persistAndFlush(user);
        
        // 测试查找用户
        User found = userRepository.findByUsername("testuser").orElse(null);
        
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser");
        assertThat(found.getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    public void testExistsByUsername() {
        // 创建测试用户
        User user = new User();
        user.setUsername("existsuser");
        user.setEmail("exists@example.com");
        user.setPassword("password");
        entityManager.persistAndFlush(user);
        
        // 测试用户名是否存在
        boolean exists = userRepository.existsByUsername("existsuser");
        boolean notExists = userRepository.existsByUsername("notexists");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    @Test
    public void testFindByUsernameOrEmail() {
        // 创建测试用户
        User user = new User();
        user.setUsername("testuser2");
        user.setEmail("test2@example.com");
        user.setPassword("password");
        entityManager.persistAndFlush(user);
        
        // 测试通过用户名查找
        User foundByUsername = userRepository.findByUsernameOrEmail("testuser2").orElse(null);
        assertThat(foundByUsername).isNotNull();
        assertThat(foundByUsername.getUsername()).isEqualTo("testuser2");
        
        // 测试通过邮箱查找
        User foundByEmail = userRepository.findByUsernameOrEmail("test2@example.com").orElse(null);
        assertThat(foundByEmail).isNotNull();
        assertThat(foundByEmail.getEmail()).isEqualTo("test2@example.com");
    }
}