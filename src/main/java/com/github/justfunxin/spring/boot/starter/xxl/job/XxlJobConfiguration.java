package com.github.justfunxin.spring.boot.starter.xxl.job;

import com.github.justfunxin.spring.boot.starter.xxl.job.annotation.XxlRegister;
import com.github.justfunxin.spring.boot.starter.xxl.job.model.XxlJobInfo;
import com.github.justfunxin.spring.boot.starter.xxl.job.properties.XxlJobExecutorProperties;
import com.github.justfunxin.spring.boot.starter.xxl.job.properties.XxlJobProperties;
import com.github.justfunxin.spring.boot.starter.xxl.job.service.XxlJobAdminService;
import com.github.justfunxin.spring.boot.starter.xxl.job.service.impl.XxlJobAdminServiceImpl;
import com.github.justfunxin.spring.boot.starter.xxl.job.utils.InetUtils;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author pangxin001@163.com
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({XxlJobProperties.class})
@ConditionalOnProperty(prefix = "xxl-job", name = "enabled", matchIfMissing = true)
public class XxlJobConfiguration implements ApplicationListener<ApplicationReadyEvent>,
        ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Autowired
    private XxlJobProperties properties;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //注册任务
        addJobInfo();
    }

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


    private void addJobInfo() {
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);

            Map<Method, XxlJob> annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                    new MethodIntrospector.MetadataLookup<XxlJob>() {
                        @Override
                        public XxlJob inspect(Method method) {
                            return AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class);
                        }
                    });
            for (Map.Entry<Method, XxlJob> methodXxlJobEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodXxlJobEntry.getKey();
                XxlJob xxlJob = methodXxlJobEntry.getValue();

                //自动注册
                if (executeMethod.isAnnotationPresent(XxlRegister.class)) {
                    XxlRegister xxlRegister = executeMethod.getAnnotation(XxlRegister.class);
                    List<XxlJobInfo> jobInfo = xxlJobAdminService().getJobs(xxlJob.value());
                    if (jobInfo.isEmpty()) {
                        createXxlJobInfo(xxlJob, xxlRegister);
                    } else {
                        XxlJobInfo job = jobInfo.stream().findFirst().orElse(null);
                        updateXxlJobInfo(xxlJob, xxlRegister, job);
                    }
                }
            }
        }
    }

    private void createXxlJobInfo(XxlJob xxlJob, XxlRegister xxlRegister) {
        var id = xxlJobAdminService().createJob(
                xxlRegister.jobDesc(),
                xxlRegister.cron(),
                xxlJob.value(),
                xxlRegister.params()
        );

        if (xxlRegister.triggerStatus() == 1) {
            xxlJobAdminService().startJob(id);
        }
        if (!xxlRegister.executorRouteStrategy().equals("FIRST")) {
            var job = xxlJobAdminService().getJob(id);
            job.setExecutorRouteStrategy(xxlRegister.executorRouteStrategy());
            xxlJobAdminService().updateJob(job);
        }
    }

    private void updateXxlJobInfo(XxlJob xxlJob, XxlRegister xxlRegister, XxlJobInfo jobInfo) {
        jobInfo.setJobDesc(xxlRegister.jobDesc());
        jobInfo.setScheduleConf(xxlRegister.cron());
        jobInfo.setExecutorParam(xxlRegister.params());
        jobInfo.setExecutorRouteStrategy(xxlRegister.executorRouteStrategy());
        xxlJobAdminService().updateJob(jobInfo);
        if (xxlRegister.triggerStatus() == 1) {
            xxlJobAdminService().startJob(jobInfo.getId());
        } else {
            xxlJobAdminService().stopJob(jobInfo.getId());
        }
    }
}
