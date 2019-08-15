package com.wuxing.apifrequencylimit.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xian
 * @date 08/12/2019
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiRequestInfo {
    /**
     * get,post
     */
    private String method;

    private String requestUrl;

    private String body;
}
