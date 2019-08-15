package com.wuxing.apifrequencylimit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxing.apifrequencylimit.config.ApiCallConfig;
import com.wuxing.apifrequencylimit.config.ApiCallConfigBuilder;
import com.wuxing.apifrequencylimit.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;


/**
 * @author xian
 */
@SpringBootApplication
@EnableScheduling
public class ApiFrequencyLimitApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiFrequencyLimitApplication.class, args);
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    /**
     * 初始化
     */
    @Component
    @Slf4j
    static class InitialApiDataConfig {

        @Autowired
        private RedisUtils redisUtils;

        @PostConstruct
        public void initApiRequestData() {
            String key = "request_mobile_real_name" + DateFormatUtils.format(new Date(), "yyyyMMddHHmm");
            String value = ApiCallConfigBuilder.builder(key, 5, 5, 60, null);
            log.info("[initApiRequestData] 初始化数据 value:{}", value);
            redisUtils.set(key, value);
        }
    }


    @RestController
    @Slf4j
    static class TestController {
        @Autowired
        private CallApiService callApiService;

        @GetMapping("/test")
        public ResponseEntity<?> test() {
            String result = callApiService.call();
            log.info("[test] result:{}", result);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 业务
     */
    @Service
    @Slf4j
    static class CallApiService {
        @Autowired
        private RedisUtils redisUtils;

        public String call() {
            Date now = new Date();
            String key = "request_mobile_real_name" + DateFormatUtils.format(now, "yyyyMMddHHmm");
            String json = redisUtils.getString(key);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            log.info("[call] json:{}", json);
            try {
                if (StringUtils.isNotBlank(json)) {
                    ApiCallConfig config = objectMapper.readValue(json, ApiCallConfig.class);
                    log.info("[call] 请求配置信息:{}", json);
                    if (config.getRemainTimes() > 0) {
                        config.setRemainTimes(config.getRemainTimes() - 1);
                        redisUtils.set(key, objectMapper.writeValueAsString(config));
                        return "调用成功";
                    } else {
                        addNextConfig(now);
                        return "放入缓存,延迟调用";
                    }
                }
                ApiCallConfig apiCallConfig = new ApiCallConfig();
                apiCallConfig.setKey("request_mobile_real_name");
                apiCallConfig.setTotalTimes(5);
                apiCallConfig.setTime(60);
                apiCallConfig.setRemainTimes(5);
                redisUtils.set(key, objectMapper.writeValueAsString(apiCallConfig));
                return "调用成功";
            } catch (IOException e) {
                log.error("[call] error:", e);
                return e.getMessage();
            }

        }

        private void addNextConfig(Date now) {
            ObjectMapper objectMapper = new ObjectMapper();
            Date nextDate = DateUtils.addMinutes(now, 1);
            String nextKey = "request_mobile_real_name" + DateFormatUtils.format(nextDate, "yyyyMMddHHmm");
            String nextJson = redisUtils.getString(nextKey);
            if (StringUtils.isNotBlank(nextJson)) {

                try {
                    ApiCallConfig nextConfig = objectMapper.readValue(nextJson, ApiCallConfig.class);
                    if (nextConfig.getRemainTimes() > 0) {
                        nextConfig.setRemainTimes(nextConfig.getRemainTimes() - 1);
                        redisUtils.set(nextKey, objectMapper.writeValueAsString(nextConfig));
                    } else {
                        addNextConfig(nextDate);
                    }
                } catch (IOException e) {
                    log.error("[addNextConfig] 添加下一个失败", e);
                }
            } else {
                String newJson = ApiCallConfigBuilder.builder(nextKey, 5, 5, 60, null);
                redisUtils.set(nextKey, newJson);
            }

        }

    }


    @Component
    @Slf4j
    static class Task {
        @Autowired
        private RedisUtils redisUtils;

        @Scheduled(fixedRate = 1000 * 60)
        public void callApi() {
            Date now = new Date();
            String key = "request_mobile_real_name" + DateFormatUtils.format(now, "yyyyMMddHHmm");
            String json = redisUtils.getString(key);
            if (StringUtils.isNotBlank(json)) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    ApiCallConfig nextConfig = objectMapper.readValue(json, ApiCallConfig.class);
                    log.info("[callApi] 定时任务处理请求,config:{}", json);
                    if (nextConfig.getApiRequestInfo() != null) {
                        log.info("[callApi] 模拟处理请求 key:{}", key);
                    } else {
                        log.info("[callApi] 当前没有数据，模拟处理请求 key:{}", key);
                    }
                } catch (IOException e) {
                    log.error("[callApi] 定时任务解析失败");
                }
            }
        }

    }


}
