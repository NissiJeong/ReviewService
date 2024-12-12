package com.task.review.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/*
테스트 코드 작성 시 @EnableJpaAuditing 으로 인해 jpa bean 을 등록하지 못하여 따로 설정파일 생성
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
