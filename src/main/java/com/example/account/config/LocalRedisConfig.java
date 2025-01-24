package com.example.account.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import redis.embedded.RedisServer;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


@Configuration
public class LocalRedisConfig {
    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() throws IOException {
        // ARM Mac 환경인지 확인
        if (isArmMac()) {
            // ARM Mac 환경에서 Redis 실행 파일을 명시적으로 지정
            redisServer = new RedisServer(Objects.requireNonNull(getRedisFileForArmMac()), redisPort);
        } else {
            // 다른 환경에서는 기본 방식으로 실행
            redisServer = new RedisServer(redisPort);
        }

        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    // 현재 시스템이 ARM Mac 환경인지 확인
    private boolean isArmMac() {
        return Objects.equals(System.getProperty("os.arch"), "aarch64") &&
                Objects.equals(System.getProperty("os.name"), "Mac OS X");
    }

    // ARM Mac용 Redis 실행 파일을 반환
    private File getRedisFileForArmMac() {
        try {
            return new ClassPathResource("binary/redis/redis-mac-arm64").getFile();
        } catch (Exception e) {
            throw new RuntimeException("ARM Mac Redis 실행 파일을 찾을 수 없습니다.", e);
        }
    }
}
