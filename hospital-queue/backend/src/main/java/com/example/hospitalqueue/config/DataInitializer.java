package com.example.hospitalqueue.config;

import com.example.hospitalqueue.domain.Department;
import com.example.hospitalqueue.domain.Ticket;
import com.example.hospitalqueue.domain.TicketStatus;
import com.example.hospitalqueue.domain.TicketPriority;
import com.example.hospitalqueue.domain.User;
import com.example.hospitalqueue.domain.UserRole;
import com.example.hospitalqueue.repository.DepartmentRepository;
import com.example.hospitalqueue.repository.TicketRepository;
import com.example.hospitalqueue.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DepartmentRepository departmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            // Tạo default users trước
            if (userRepository.count() == 0) {
                createDefaultUsers();
                log.info("✅ [Seed] Đã tạo default users");
            }
            
            // Chỉ tạo dữ liệu cơ bản nếu chưa có
            if (departmentRepository.count() == 0) {
                createBasicDepartments();
                log.info("✅ [Seed] Đã tạo departments cơ bản");
            }

            // Tạo tickets mẫu nếu chưa có
            if (ticketRepository.count() == 0) {
                createSampleTickets();
                log.info("✅ [Seed] Đã tạo sample tickets");
            }

            log.info("🏥 Hospital Queue System khởi động thành công!");
            log.info("� Tổng số users: {}", userRepository.count());
            log.info("�📊 Tổng số khoa: {}", departmentRepository.count());
            log.info("🎫 Tổng số tickets: {}", ticketRepository.count());
        };
    }

    private void createDefaultUsers() {
        // Create admin user
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@hospital.com")
                    .fullName("System Administrator")
                    .phone("0123456789")
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
            log.info("Created admin user: {}", admin.getUsername());
        }

        // Create staff user
        if (!userRepository.existsByUsername("staff")) {
            User staff = User.builder()
                    .username("staff")
                    .password(passwordEncoder.encode("staff123"))
                    .email("staff@hospital.com")
                    .fullName("Staff User")
                    .phone("0123456788")
                    .role(UserRole.STAFF)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(staff);
            log.info("Created staff user: {}", staff.getUsername());
        }

        // Create hardware user
        if (!userRepository.existsByUsername("hardware")) {
            User hardware = User.builder()
                    .username("hardware")
                    .password(passwordEncoder.encode("hardware123"))
                    .email("hardware@hospital.com")
                    .fullName("Hardware Device")
                    .role(UserRole.HARDWARE)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(hardware);
            log.info("Created hardware user: {}", hardware.getUsername());
        }
    }

    private void createBasicDepartments() {
        List<Department> basicDepartments = Arrays.asList(
            // Khoa chính
            Department.builder().code("K01").name("Khoa Nội Tổng Hợp").location("Tầng 2 - Khu A").build(),
            Department.builder().code("K02").name("Khoa Ngoại Tổng Hợp").location("Tầng 3 - Khu A").build(),
            Department.builder().code("K03").name("Khoa Nhi").location("Tầng 1 - Khu B").build(),
            Department.builder().code("K04").name("Khoa Sản Phụ Khoa").location("Tầng 4 - Khu B").build(),
            Department.builder().code("K05").name("Khoa Mắt").location("Tầng 2 - Khu C").build(),
            
            // Khoa chuyên khoa
            Department.builder().code("K06").name("Khoa Tim Mạch").location("Tầng 5 - Khu A").build(),
            Department.builder().code("K07").name("Khoa Thần Kinh").location("Tầng 6 - Khu A").build(),
            Department.builder().code("K08").name("Khoa Da Liễu").location("Tầng 1 - Khu C").build(),
            
            // Cấp cứu & VIP
            Department.builder().code("EMG01").name("Phòng Cấp Cứu 1").location("Tầng 1 - Cấp Cứu").build(),
            Department.builder().code("VIP01").name("Phòng Khám VIP").location("Tầng 8 - Khu VIP").build(),
            
            // Dịch vụ
            Department.builder().code("REG01").name("Quầy Đăng Ký").location("Sảnh Chính").build(),
            Department.builder().code("PAY01").name("Quầy Thu Ngân").location("Sảnh Chính").build()
        );
        
        departmentRepository.saveAll(basicDepartments);
    }

    private void createSampleTickets() {
        LocalDateTime now = LocalDateTime.now();
        
        // Lấy một số khoa để tạo tickets mẫu
        Department k01 = departmentRepository.findByCode("K01").orElse(null);
        Department k02 = departmentRepository.findByCode("K02").orElse(null);
        Department k03 = departmentRepository.findByCode("K03").orElse(null);
        
        // Tạo tickets cho Khoa Nội (K01)
        if (k01 != null) {
            createTicketsForDepartment(k01, now, 8);
        }
        
        // Tạo tickets cho Khoa Ngoại (K02)
        if (k02 != null) {
            createTicketsForDepartment(k02, now, 5);
        }
        
        // Tạo tickets cho Khoa Nhi (K03)
        if (k03 != null) {
            createTicketsForDepartment(k03, now, 6);
        }
    }

    private void createTicketsForDepartment(Department dept, LocalDateTime baseTime, int totalTickets) {
        String[] sampleNames = {
            "Nguyễn Văn A", "Trần Thị B", "Lê Văn C", "Phạm Thị D", "Hoàng Văn E",
            "Vũ Thị F", "Đỗ Văn G", "Phan Thị H", "Bùi Văn I", "Đặng Thị K"
        };
        for (int i = 1; i <= totalTickets; i++) {
            TicketStatus status;
            LocalDateTime createdAt = baseTime.minusMinutes(60 - (i * 8));
            LocalDateTime calledAt = null;
            LocalDateTime completedAt = null;
            
            // Phân bố trạng thái thực tế
            if (i <= totalTickets * 0.4) { // 40% đã hoàn thành
                status = TicketStatus.COMPLETED;
                calledAt = createdAt.plusMinutes(15);
                completedAt = calledAt.plusMinutes(10);
            } else if (i <= totalTickets * 0.5) { // 10% đang được gọi
                status = TicketStatus.CALLED;
                calledAt = createdAt.plusMinutes(15);
            } else { // 50% còn lại đang chờ
                status = TicketStatus.WAITING;
            }
            
            Ticket ticket = Ticket.builder()
                .department(dept)
                .number(i)
                .status(status)
                .priority(TicketPriority.NORMAL)
                .createdAt(createdAt)
                .calledAt(calledAt)
                .completedAt(completedAt)
                .holderName(sampleNames[(i - 1) % sampleNames.length])
                .build();
                
            ticketRepository.save(ticket);
        }
    }
}
