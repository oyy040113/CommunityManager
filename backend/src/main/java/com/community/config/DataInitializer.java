package com.community.config;

import com.community.entity.Club;
import com.community.entity.ClubMember;
import com.community.entity.User;
import com.community.repository.ClubMemberRepository;
import com.community.repository.ClubRepository;
import com.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 * 系统启动时自动创建测试数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // 创建管理员账号
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .realName("系统管理员")
                    .email("admin@community.com")
                    .role(User.UserRole.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("创建管理员账号: admin / admin123");
        }
        
        // 创建测试用户
        if (!userRepository.existsByUsername("test")) {
            User testUser = User.builder()
                    .username("test")
                    .password(passwordEncoder.encode("test123"))
                    .realName("测试用户")
                    .studentId("2024001")
                    .email("test@community.com")
                    .phone("13800138000")
                    .department("计算机学院")
                    .major("软件工程")
                    .grade("2024")
                    .role(User.UserRole.MEMBER)
                    .enabled(true)
                    .build();
            userRepository.save(testUser);
            log.info("创建测试用户: test / test123");
        }
        
        // 创建社团负责人
        User leader = null;
        if (!userRepository.existsByUsername("leader")) {
            leader = User.builder()
                    .username("leader")
                    .password(passwordEncoder.encode("leader123"))
                    .realName("社团负责人")
                    .studentId("2024002")
                    .email("leader@community.com")
                    .phone("13800138001")
                    .department("计算机学院")
                    .major("计算机科学与技术")
                    .grade("2022")
                    .role(User.UserRole.CLUB_LEADER)
                    .enabled(true)
                    .build();
            leader = userRepository.save(leader);
            log.info("创建社团负责人: leader / leader123");
        } else {
            leader = userRepository.findByUsername("leader").orElse(null);
        }
        
        // 创建示例社团
        if (leader != null && !clubRepository.existsByName("编程爱好者协会")) {
            Club club = Club.builder()
                    .name("编程爱好者协会")
                    .type(Club.ClubType.ACADEMIC)
                    .purpose("提升编程技能，促进技术交流")
                    .description("编程爱好者协会是一个致力于推广编程文化、提升成员编程技能的学术科技类社团。我们定期举办编程比赛、技术分享会、项目实战等活动。")
                    .contactEmail("coding@community.com")
                    .contactPhone("13800138002")
                    .location("计算机楼201室")
                    .leader(leader)
                    .memberCount(1)
                    .activityCount(0)
                    .activityScore(50.0)
                    .status(Club.ClubStatus.ACTIVE)
                    .build();
            club = clubRepository.save(club);
            
            // 添加负责人为社团成员
            ClubMember leaderMember = ClubMember.builder()
                    .user(leader)
                    .club(club)
                    .role(ClubMember.MemberRole.LEADER)
                    .position("社长")
                    .status(ClubMember.MemberStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .activityParticipation(0)
                    .contributionScore(10.0)
                    .build();
            clubMemberRepository.save(leaderMember);
            
            log.info("创建示例社团: 编程爱好者协会");
        }
        
        // 创建更多示例社团
        if (leader != null && !clubRepository.existsByName("篮球社")) {
            Club basketballClub = Club.builder()
                    .name("篮球社")
                    .type(Club.ClubType.SPORTS)
                    .purpose("推广篮球运动，强健体魄")
                    .description("篮球社是学校最具活力的体育社团之一，欢迎所有热爱篮球的同学加入！")
                    .contactEmail("basketball@community.com")
                    .location("体育馆")
                    .leader(leader)
                    .memberCount(1)
                    .activityCount(0)
                    .activityScore(60.0)
                    .status(Club.ClubStatus.ACTIVE)
                    .build();
            clubRepository.save(basketballClub);
            log.info("创建示例社团: 篮球社");
        }
        
        if (leader != null && !clubRepository.existsByName("志愿服务队")) {
            Club volunteerClub = Club.builder()
                    .name("志愿服务队")
                    .type(Club.ClubType.VOLUNTEER)
                    .purpose("奉献爱心，服务社会")
                    .description("志愿服务队致力于组织各类公益活动，培养学生的社会责任感。")
                    .contactEmail("volunteer@community.com")
                    .location("学生活动中心")
                    .leader(leader)
                    .memberCount(1)
                    .activityCount(0)
                    .activityScore(70.0)
                    .status(Club.ClubStatus.ACTIVE)
                    .build();
            clubRepository.save(volunteerClub);
            log.info("创建示例社团: 志愿服务队");
        }
        
        log.info("数据初始化完成！");
    }
}
