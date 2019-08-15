package com.wuxing.apifrequencylimit.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xian
 * @date 08/12/2019
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ApiCallConfig {
    /**
     * 缓存的Key
     */
    private String key;

    /**
     * 以秒作为单位，这个配置用于定时任务读取策略
     */
    private Integer time;

    /**
     * 总次数
     */
    private Integer totalTimes;

    /**
     * 剩余次数
     */
    private Integer remainTimes;


    private List<ApiRequestInfo> apiRequestInfo;

}
