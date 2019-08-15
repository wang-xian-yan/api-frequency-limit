package com.wuxing.apifrequencylimit.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author xian
 * @date 08/12/2019
 */
@Slf4j
public class ApiCallConfigBuilder {
    public static String builder(String key, Integer totalTimes, Integer remainTimes, Integer time, List<ApiRequestInfo> apiRequestInfos) {
        ApiCallConfig apiCallConfig = new ApiCallConfig();
        apiCallConfig.setKey(key);
        apiCallConfig.setTotalTimes(totalTimes);
        apiCallConfig.setRemainTimes(remainTimes);
        apiCallConfig.setTime(time);
        apiCallConfig.setApiRequestInfo(apiRequestInfos);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(apiCallConfig);
        } catch (JsonProcessingException e) {
            log.error("[builder] error:", e);
            return null;
        }

    }
}
