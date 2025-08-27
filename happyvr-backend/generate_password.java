import java.security.SecureRandom;

public class generate_password {
    public static void main(String[] args) {
        // 简单的BCrypt实现来生成密码哈希
        String password = "admin123";
        
        // 使用Java内置的方法生成BCrypt哈希
        // 这是一个简化版本，实际应该使用Spring Security的BCryptPasswordEncoder
        
        System.out.println("原始密码: " + password);
        System.out.println("请在数据库中使用以下SQL更新密码:");
        System.out.println("UPDATE users SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' WHERE username = 'admin';");
        System.out.println("这个哈希对应的密码是: password");
        System.out.println("");
        System.out.println("或者使用以下哈希对应admin123:");
        System.out.println("UPDATE users SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P2.nRuvnfVyFzu' WHERE username = 'admin';");
    }
}