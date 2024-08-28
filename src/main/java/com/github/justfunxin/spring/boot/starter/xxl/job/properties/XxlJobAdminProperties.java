package com.github.justfunxin.spring.boot.starter.xxl.job.properties;

import lombok.Data;

/**
 * @author pangxin001@163.com
 */
@Data
public class XxlJobAdminProperties {

    /**
     * 调度中心部署根地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。
     * 执行器将会使用该地址进行"执行器心跳注册"和"任务结果回调"；为空则关闭自动注册；
     */
    private String[] addresses = {"http://127.0.0.1:8080/xxl-job-admin"};

    /**
     * 调度中心用户名 [选填]，通过 {@link com.github.justfunxin.spring.boot.starter.xxl.job.service.XxlJobAdminService} 管理服务时需要
     */
    private String username = "admin";

    /**
     * 调度中心密码 [选填]，通过 {@link com.github.justfunxin.spring.boot.starter.xxl.job.service.XxlJobAdminService} 管理服务时需要
     */
    private String password = "admin";

}
