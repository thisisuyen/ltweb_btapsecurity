package vn.iotstar.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Configuration
public class DataInitializer {

  @Bean
  CommandLineRunner init(RoleRepository roleRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
    return args -> {
      Role roleUser = roleRepository.findByName("ROLE_USER")
          .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

      Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
          .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

      if (userRepository.findByUsername("user01").isEmpty()) {
        User u = new User();
        u.setUsername("user01");
        u.setEmail("user01@gmail.com");
        u.setPassword(passwordEncoder.encode("123456"));
        u.setFullName("Nguyễn Hữu Trung");
        u.setImages("/images/user.png");
        u.setRole(roleUser);
        u.setEnabled(true);
        userRepository.save(u);
      }

      if (userRepository.findByUsername("admin01").isEmpty()) {
        User a = new User();
        a.setUsername("admin01");
        a.setEmail("admin01@gmail.com");
        a.setPassword(passwordEncoder.encode("123456"));
        a.setFullName("System Admin");
        a.setImages("/images/avatar-default.png");
        a.setRole(roleAdmin);
        a.setEnabled(true);
        userRepository.save(a);
      }
    };
  }
}