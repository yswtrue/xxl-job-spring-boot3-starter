package com.github.justfunxin.spring.boot.starter.xxl.job.properties;

import lombok.Data;

/**
 * 执行器配置
 *
 * @author pangxin01822
 */
@Data
public class XxlJobExecutorProperties {

    /**
     * 执行器AppName
     */
    private String appName;

    /**
     * 执行器名称
     */
    private String appDesc;

    /**
     * 执行器注册地址 [选填]
     *
     * 优先使用该配置作为注册地址，为空时使用内嵌服务 ”IP:PORT“ 作为注册地址。
     *
     * such as "http://127.0.0.1:9999/"
     */
    private String address;

    /**
     * 多网卡时指定注册IP，支持正则或者前缀匹配
     */
    private String[] preferredNetworks = {};

    /**
     * 执行器IP [选填]：默认为空表示自动获取IP，多网卡时可手动设置指定IP，该IP不会绑定Host仅作为通讯实用；地址信息用于 "执行器注册" 和 "调度中心请求并触发任务"；
     */
    private String ip;

    /**
     * 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9999，单机部署多个执行器时，注意要配置不同执行器端口；
     */
    private int port = 9999;

    /**
     * 执行器运行日志文件存储磁盘路径 [选填] ：需要对该路径拥有读写权限；为空则使用默认路径；
     */
    private String logPath = "/data/applogs/xxl-job/jobhandler";

    /**
     * 执行器日志文件保存天数 [选填] ：过期日志自动清理, 限制值大于等于3时生效; 否则, 如-1, 关闭自动清理功能；
     */
    private int logRetentionDays = 30;

    /**
     * 是否自动创建执行器
     */
    private boolean autoCreateJobGroup = true;
}
