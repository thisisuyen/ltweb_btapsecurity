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

      Role roleUser = roleRepository.findByName("ROLE_USER").orElse(null);
      if (roleUser == null) {
        Role r = new Role();
        r.setName("ROLE_USER");
        roleUser = roleRepository.save(r);
      }

      Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElse(null);
      if (roleAdmin == null) {
        Role r = new Role();
        r.setName("ROLE_ADMIN");
        roleAdmin = roleRepository.save(r);
      }

      if (userRepository.findByUsername("user01").isEmpty()) {
        User u = new User();
        u.setUsername("user01");
        u.setEmail("user01@gmail.com");
        u.setPassword(passwordEncoder.encode("123456"));
        u.setFullName("Nguyễn Hữu Trung");
        u.setImages("/images/user.png");
        u.setEnabled(true);

        // nếu User bạn là roles (Set<Role>) thì dùng add role
        u.getRoles().add(roleUser);

        userRepository.save(u);
      }

      if (userRepository.findByUsername("admin01").isEmpty()) {
        User a = new User();
        a.setUsername("admin01");
        a.setEmail("admin01@gmail.com");
        a.setPassword(passwordEncoder.encode("123456"));
        a.setFullName("System Admin");
        a.setImages("/images/avatar-default.png");
        a.setEnabled(true);

        a.getRoles().add(roleAdmin);

        userRepository.save(a);
      }
    };
  }
}