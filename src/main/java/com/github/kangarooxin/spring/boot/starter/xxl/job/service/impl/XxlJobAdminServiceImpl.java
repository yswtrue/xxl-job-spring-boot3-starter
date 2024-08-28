package com.github.kangarooxin.spring.boot.starter.xxl.job.service.impl;

import com.github.kangarooxin.spring.boot.starter.xxl.job.exception.XxlJobServiceException;
import com.github.kangarooxin.spring.boot.starter.xxl.job.model.XxlJobGroup;
import com.github.kangarooxin.spring.boot.starter.xxl.job.model.XxlJobInfo;
import com.github.kangarooxin.spring.boot.starter.xxl.job.properties.XxlJobProperties;
import com.github.kangarooxin.spring.boot.starter.xxl.job.service.XxlJobAdminService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.GsonTool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pangxin01822
 */
@Slf4j
public class XxlJobAdminServiceImpl implements XxlJobAdminService {
    private static Gson gson;

    static {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    private XxlJobProperties properties;

    private String adminAddress;

    private HttpClient httpClient;

    private CookieManager cookieManager;

    public XxlJobAdminServiceImpl(XxlJobProperties properties) {
        this.properties = properties;
        this.adminAddress = properties.getAdmin().getAddresses()[0];
        if (!this.adminAddress.endsWith("/")) {
            this.adminAddress = this.adminAddress + "/";
        }
        this.cookieManager = new CookieManager();
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();
    }

    @Override
    public int createJob(String jobDesc, String cron, String handler, String param) {
        return createJob(properties.getExecutor().getAppName(), jobDesc, cron, handler, param);
    }

    @Override
    public int createJob(String appName, String jobDesc, String cron, String handler, String param) {
        Assert.hasText(appName, "请输入执行器appName");
        XxlJobGroup jobGroup = getJobGroup(appName);
        if (jobGroup == null) {
            if (properties.getExecutor().isAutoCreateJobGroup()) {
                createJobGroup(properties.getExecutor().getAppName(), properties.getExecutor().getAppDesc());
                jobGroup = getJobGroup(appName);
            } else {
                throw new XxlJobServiceException("can not find job group " + appName);
            }
        }
        int jobGroupId = jobGroup.getId();
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setJobGroup(jobGroupId)
                .setJobDesc(jobDesc)
                .setAuthor(properties.getJobAuthor())
                .setScheduleType("CRON")
                .setScheduleConf(cron)
                .setGlueType("BEAN")
                .setExecutorHandler(handler)
                .setExecutorParam(param)
                .setExecutorRouteStrategy("FIRST")
                .setMisfireStrategy("DO_NOTHING")
                .setExecutorBlockStrategy("SERIAL_EXECUTION");
        return createJob(xxlJobInfo);
    }

    @Override
    public int createJob(XxlJobInfo jobInfo) {
        return postForm("jobinfo/add", parseToMap(jobInfo), Integer.class);
    }

    @Override
    public void updateJob(int jobId, String cron, String param) {
        XxlJobInfo jobInfo = getJob(jobId);
        jobInfo.setScheduleConf(cron)
                .setExecutorParam(param);
        updateJob(jobInfo);
    }

    @Override
    public void updateJob(XxlJobInfo jobInfo) {
        jobInfo.setAddTime(null)
                .setGlueUpdatetime(null)
                .setUpdateTime(null);
        postForm("jobinfo/update", parseToMap(jobInfo), Void.class);
    }

    @Override
    public void removeJob(int jobId) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(jobId));
        postForm("jobinfo/remove", params, Void.class);
    }

    @Override
    public void startJob(int jobId) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(jobId));
        postForm("jobinfo/start", params, Void.class);
    }

    @Override
    public void stopJob(int jobId) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(jobId));
        postForm("jobinfo/stop", params, Void.class);
    }

    @Override
    public void triggerJob(int jobId, String executorParam) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(jobId));
        params.put("executorParam", executorParam);
        postForm("jobinfo/trigger", params, Void.class);
    }

    @Override
    public XxlJobInfo getJob(int jobId) {
        Map<String, String> params = new HashMap<>();
        params.put("jobGroup", "-1");
        params.put("triggerStatus", "-1");
        params.put("start", "0");
        params.put("length", "10000");
        JobInfoPageList list = postFormWithoutReturnT("jobinfo/pageList", params, JobInfoPageList.class);
        if (CollectionUtils.isEmpty(list.getData())) {
            return null;
        }
        return list.getData().stream()
                .filter(x -> x.getId() == jobId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void createJobGroup(String appName, String title) {
        Map<String, String> params = new HashMap<>();
        params.put("appname", appName);
        params.put("title", StringUtils.hasLength(title) ? title : appName);
        params.put("addressType", "0");
        postForm("jobgroup/save", params, Void.class);
    }

    @Override
    public XxlJobGroup getJobGroup(String appName) {
        Map<String, String> params = new HashMap<>();
        params.put("appname", appName);
        params.put("start", "0");
        params.put("length", "10");
        JobGroupPageList list = postFormWithoutReturnT("jobgroup/pageList", params, JobGroupPageList.class);
        if (CollectionUtils.isEmpty(list.getData())) {
            return null;
        }
        return list.getData().stream()
                .filter(x -> x.getAppname().equals(appName))
                .findFirst()
                .orElse(null);
    }

    private void doLogin() {
        Map<String, String> params = new HashMap<>();
        params.put("userName", properties.getAdmin().getUsername());
        params.put("password", properties.getAdmin().getPassword());
        HttpResponse<String> response = postForm(adminAddress + "login", params);
        ReturnT<?> ret = gson.fromJson(response.body(), ReturnT.class);
        if (ret.getCode() == 200) {
            log.info("xxl job admin login success");
        } else {
            throw new XxlJobServiceException("xxlJob登录失败：" + ret.getMsg());
        }
    }

    private HttpResponse<String> postForm(String url, Map<String, String> params) {
        // 构建表单数据
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!postData.isEmpty()) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            postData.append('=');
            postData.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        // 创建 HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postData.toString()))
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new XxlJobServiceException("xxl job service request failed", e);
        }
    }

    private boolean checkDoLogin(HttpResponse<String> response) {
        if (response.statusCode() == 302 && response.headers().firstValue("location").get().endsWith("/toLogin")) {
            doLogin();
            return true;
        }
        return false;
    }

    private <T> T postForm(String url, Map<String, String> params, Class<T> clazz) {
        HttpResponse<String> response = postForm(adminAddress + url, params);
        if (checkDoLogin(response)) {
            response = postForm(adminAddress + url, params);
        }
        ReturnT<T> result = GsonTool.fromJson(response.body(), ReturnT.class, clazz);
        if (result == null) {
            throw new XxlJobServiceException("xxl job service invoke failed");
        }
        if (result.getCode() != ReturnT.SUCCESS_CODE) {
            throw new XxlJobServiceException("xxl job service op failed：" + result.getMsg() + "(" + result.getCode() + ")");
        }
        return result.getContent();
    }

    private <T> T postFormWithoutReturnT(String url, Map<String, String> params, Class<T> clazz) {
        HttpResponse<String> response = postForm(adminAddress + url, params);
        if (checkDoLogin(response)) {
            response = postForm(adminAddress + url, params);
        }
        T result = GsonTool.fromJson(response.body(), clazz);
        if (result == null) {
            throw new XxlJobServiceException("xxl job service invoke failed");
        }
        return result;
    }

    private Map<String, String> parseToMap(XxlJobInfo jobInfo) {
        return gson.fromJson(gson.toJson(jobInfo), new TypeToken<Map<String, String>>() {
        }.getType());
    }

    @Data
    public static class JobGroupPageList {
        private Integer recordsTotal;
        private Integer recordsFiltered;
        private List<XxlJobGroup> data;
    }

    @Data
    public static class JobInfoPageList {
        private Integer recordsTotal;
        private Integer recordsFiltered;
        private List<XxlJobInfo> data;
    }
}
