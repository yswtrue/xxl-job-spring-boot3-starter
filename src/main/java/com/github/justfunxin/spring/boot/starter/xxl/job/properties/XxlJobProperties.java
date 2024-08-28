package com.github.justfunxin.spring.boot.starter.xxl.job.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author pangxin01822
 */
@Data
@ConfigurationProperties(prefix = "xxl-job")
public class XxlJobProperties {

    private boolean enabled = true;

    /**
     * 调度中心配置
     */
    @NestedConfigurationProperty
    private XxlJobAdminProperties admin = new XxlJobAdminProperties();

    /**
     * 执行器配置
     */
    @NestedConfigurationProperty
    private XxlJobExecutorProperties executor = new XxlJobExecutorProperties();

    /**
     * 执行器通讯TOKEN [选填]：非空时启用；
     */
    private String accessToken;

    /**
     * 创建任务时默认执行人
     */
    private String jobAuthor = "admin";

}
