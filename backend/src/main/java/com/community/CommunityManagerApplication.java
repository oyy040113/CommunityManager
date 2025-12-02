package com.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 学生社团信息整理系统 - 主启动类
 * 
 * @author CommunityManager Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class CommunityManagerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CommunityManagerApplication.class, args);
        System.out.println("========================================");
        System.out.println("  学生社团信息整理系统启动成功！");
        System.out.println("  API文档: http://localhost:8080/api/swagger-ui.html");
        System.out.println("  H2控制台: http://localhost:8080/api/h2-console");
        System.out.println("========================================");
    }
}
