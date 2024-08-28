package com.github.kangarooxin.spring.boot.starter.xxl.job.properties;

import lombok.Data;

/**
 * @author pangxin01822
 */
@Data
public class XxlJobAdminProperties {

    /**
     * 调度中心部署根地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。
     * 执行器将会使用该地址进行"执行器心跳注册"和"任务结果回调"；为空则关闭自动注册；
     */
    private String[] addresses = {"http://127.0.0.1:8080/xxl-job-admin"};

}
