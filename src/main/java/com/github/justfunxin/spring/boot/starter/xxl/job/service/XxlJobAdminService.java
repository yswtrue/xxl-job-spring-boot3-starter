package com.github.justfunxin.spring.boot.starter.xxl.job.service;


import com.github.justfunxin.spring.boot.starter.xxl.job.model.XxlJobGroup;
import com.github.justfunxin.spring.boot.starter.xxl.job.model.XxlJobInfo;

import java.util.List;

/**
 * @author pangxin001@163.com
 */
public interface XxlJobAdminService {

    /**
     * 创建任务
     *
     * @param jobDesc 任务描述
     * @param cron    执行Cron表达式
     * @param handler 处理Handler
     * @param param   执行参数
     * @return
     */
    int createJob(String jobDesc, String cron, String handler, String param);

    /**
     * 创建任务
     *
     * @param appName 执行器appName
     * @param jobDesc 任务描述
     * @param cron    执行Cron表达式
     * @param handler 处理Handler
     * @param param   执行参数
     * @return
     */
    int createJob(String appName, String jobDesc, String cron, String handler, String param);

    /**
     * 创建任务
     *
     * @param jobInfo 任务信息
     * @return
     */
    int createJob(XxlJobInfo jobInfo);

    /**
     * 更新任务
     *
     * @param jobId 任务ID
     * @param cron  执行Cron表达式
     * @param param 执行参数
     */
    void updateJob(int jobId, String cron, String param);

    /**
     * 更新任务
     *
     * @param jobInfo 任务信息
     */
    void updateJob(XxlJobInfo jobInfo);

    /**
     * 删除任务
     *
     * @param jobId 任务ID
     */
    void removeJob(int jobId);

    /**
     * 启动任务
     *
     * @param jobId 任务ID
     */
    void startJob(int jobId);

    /**
     * 停止任务
     *
     * @param jobId 任务ID
     */
    void stopJob(int jobId);

    /**
     * 触发一次任务
     *
     * @param jobId         任务ID
     * @param executorParam 执行参数
     */
    void triggerJob(int jobId, String executorParam);

    /**
     * 获取任务信息
     *
     * @param jobId 任务ID
     */
    XxlJobInfo getJob(int jobId);
    /**
     * 获取任务信息
     *
     * @param handler 任务handler
     */
    List<XxlJobInfo> getJobs(String handler);

    /**
     * 创建执行器
     *
     * @param appName 执行器appName
     * @param title   执行器标题
     */
    void createJobGroup(String appName, String title);

    /**
     * 通过appName获取执行器
     *
     * @param appName 执行器appName
     * @return
     */
    XxlJobGroup getJobGroup(String appName);
}
