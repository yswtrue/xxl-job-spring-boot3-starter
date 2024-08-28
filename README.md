# xxl-job-spring-boot3-starter

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.kangarooxin/xxl-job-spring-boot3-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.kangarooxin/xxl-job-spring-boot3-starter)

## Usage:
1. import dependency in pom.xml
    ```
    <dependency>
        <groupId>io.github.kangarooxin</groupId>
        <artifactId>xxl-job-spring-boot3-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```
2. config in properties
    ```properties
        #xxl-job
        xxl-job.admin.addresses=http://127.0.0.1:8080/xxl-job-admin
        xxl-job.access-token=123456
        xxl-job.executor.app-name=${spring.application.name}
    ```
3. config handler
    ```java
    @Slf4j
    @Component
    public class XxlJobTestHandler {
    
    
        @XxlJob("testHandler")
        public void testHandler() {
            XxlJobContext xxlJobContext = XxlJobContext.getXxlJobContext();
            String jobParam = xxlJobContext.getJobParam();
            log.info("[xxlJob] testHandler, param={}", jobParam);
    
            //打印执行日志
            XxlJobHelper.log("XXL-JOB, Hello World.");
            //默认任务结果为 "成功" 状态，不需要主动设置；
            //如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
            //XxlJobHelper.handleFail();
            //XxlJobHelper.handleSuccess();
        }
    
    }
    ```
4. XxlJobAdminService
   ```java
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
   ```