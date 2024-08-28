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