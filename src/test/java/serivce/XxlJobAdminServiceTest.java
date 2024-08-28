package serivce;

import com.github.kangarooxin.spring.boot.starter.xxl.job.model.XxlJobInfo;
import com.github.kangarooxin.spring.boot.starter.xxl.job.properties.XxlJobProperties;
import com.github.kangarooxin.spring.boot.starter.xxl.job.service.XxlJobAdminService;
import com.github.kangarooxin.spring.boot.starter.xxl.job.service.impl.XxlJobAdminServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class XxlJobAdminServiceTest {

    private static XxlJobAdminService xxlJobAdminService;

    private static final String appName = "test-app";

    @BeforeAll
    public static void init() {
        XxlJobProperties properties = new XxlJobProperties();
        properties.getAdmin().setAddresses(new String[]{"http://127.0.0.1:8800/xxl-job-admin"});
        properties.getAdmin().setUsername("admin");
        properties.getAdmin().setPassword("123456");
        properties.getExecutor().setAppName(appName);
        properties.setAccessToken("123456");
        xxlJobAdminService = new XxlJobAdminServiceImpl(properties);
    }

    @Test
    public void testGetJob() {
        XxlJobInfo jobInfo = xxlJobAdminService.getJob(2);
        Assertions.assertNotNull(jobInfo);
    }

    @Test
    public void testCreateJob() {
        int jobId = xxlJobAdminService.createJob("test-job",
                "0/5 * * * * ?",
                "testJobHandler",
                "");
        XxlJobInfo jobInfo = xxlJobAdminService.getJob(jobId);
        Assertions.assertNotNull(jobInfo);

        xxlJobAdminService.startJob(jobId);
        jobInfo = xxlJobAdminService.getJob(jobId);
        Assertions.assertEquals(1, jobInfo.getTriggerStatus());

        xxlJobAdminService.stopJob(jobId);
        jobInfo = xxlJobAdminService.getJob(jobId);
        Assertions.assertEquals(0, jobInfo.getTriggerStatus());

        xxlJobAdminService.triggerJob(jobId, "test");

        xxlJobAdminService.removeJob(jobId);
        jobInfo = xxlJobAdminService.getJob(jobId);
        Assertions.assertNull(jobInfo);
    }


    @Test
    public void testJobGroup() {
        xxlJobAdminService.createJobGroup(appName, appName);
        System.out.println(xxlJobAdminService.getJobGroup(appName));
    }
}
