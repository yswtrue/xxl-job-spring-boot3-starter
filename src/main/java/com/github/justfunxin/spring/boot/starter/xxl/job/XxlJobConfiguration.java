package com.github.justfunxin.spring.boot.starter.xxl.job;

import com.github.justfunxin.spring.boot.starter.xxl.job.properties.XxlJobExecutorProperties;
import com.github.justfunxin.spring.boot.starter.xxl.job.properties.XxlJobProperties;
import com.github.justfunxin.spring.boot.starter.xxl.job.service.XxlJobAdminService;
import com.github.justfunxin.spring.boot.starter.xxl.job.service.impl.XxlJobAdminServiceImpl;
import com.github.justfunxin.spring.boot.starter.xxl.job.utils.InetUtils;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author pangxin001@163.com
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({XxlJobProperties.class})
@ConditionalOnProperty(prefix = "xxl-job", name = "enabled", matchIfMissing = true)
public class XxlJobConfiguration {

    @Autowired
    private XxlJobProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobExecutorProperties executor = properties.getExecutor();
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(StringUtils.arrayToCommaDelimitedString(properties.getAdmin().getAddresses()));
        xxlJobSpringExecutor.setAppname(executor.getAppName());
        if (StringUtils.hasText(executor.getAddress())) {
            xxlJobSpringExecutor.setAddress(executor.getAddress());
            log.info("xxl-job config init with address {}", executor.getAddress());
        } else {
            int port = executor.getPort();
            String ip = getIp(executor);
            xxlJobSpringExecutor.setIp(ip);
            xxlJobSpringExecutor.setPort(port);
            log.info("xxl-job config init with address http://{}:{}/", ip, port);
        }
        xxlJobSpringExecutor.setAccessToken(properties.getAccessToken());
        xxlJobSpringExecutor.setLogPath(executor.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(executor.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }

    @Bean
    @ConditionalOnMissingBean
    public XxlJobAdminService xxlJobAdminService() {
        return new XxlJobAdminServiceImpl(properties);
    }


    private static String getIp(XxlJobExecutorProperties executor) {
        String ip = executor.getIp();
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        if (executor.getPreferredNetworks().length > 0) {
            ip = InetUtils.findFirstNonLoopbackAddress(executor.getPreferredNetworks()).getHostAddress();
        } else {
            ip = IpUtil.getIp();
        }
        return ip;
    }
}
