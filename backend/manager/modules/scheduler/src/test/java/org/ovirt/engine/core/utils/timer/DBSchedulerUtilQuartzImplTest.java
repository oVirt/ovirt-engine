package org.ovirt.engine.core.utils.timer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.utils.ResourceUtils;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

@Tag("dao")
public class DBSchedulerUtilQuartzImplTest {

    private static DBSchedulerUtilQuartzImpl scheduler;

    @BeforeAll
    public static void init() throws IOException {
        String QUARTZ_DB_TEST_PROPERTIES = "ovirt-db-scheduler-test.properties";
        Properties props = ResourceUtils.loadProperties(SchedulerUtil.class, QUARTZ_DB_TEST_PROPERTIES);

        scheduler = new DBSchedulerUtilQuartzImpl();
        scheduler.setup(props);
    }

    @AfterAll
    public static void tearDown() {
        scheduler.teardown();
    }

    @Test
    public void scheduleAJob() throws InterruptedException, SchedulerException {
        DummyJob dummyJob = new DummyJob();
        String jobName = scheduler.scheduleAOneTimeJob(dummyJob, "dummyScheduleMethod",
                new Class[] { String.class },
                new Object[] { "scheduleAJob" }, 1, TimeUnit.MILLISECONDS);

        Thread.sleep(10);
        try {
            JobDetail job = scheduler.getRawScheduler().getJobDetail(JobKey.jobKey(jobName));
            assertNotNull(job);
        }finally {
            scheduler.deleteJob(jobName);
        }
    }

    @Test
    public void scheduleARecurringJob() throws InterruptedException, SchedulerException {
        DummyJob dummyJob = new DummyJob();
        String jobName = scheduler.scheduleACronJob(dummyJob, "dummyScheduleMethod",
                new Class[] { String.class },
                new Object[] { "scheduleARecurringJob" }, "0/1 * * * * ?");

        TimeUnit.SECONDS.sleep(2);
        try {
            JobDetail job = scheduler.getRawScheduler().getJobDetail(JobKey.jobKey(jobName));
            assertNotNull(job);
            List<? extends Trigger> triggers = scheduler.getRawScheduler().getTriggersOfJob(JobKey.jobKey(jobName));
            // Asserting the next fire time instead of previous fire time. Prevfiretime is based on timing of threads
            // for a recurring job, the next fire time should always be updated
            assertNotNull(triggers.get(0).getNextFireTime());
        } finally {
            scheduler.deleteJob(jobName);
        }
    }

    private static class DummyJob implements Serializable {
        private static final long serialVersionUID = 2288097737673782124L;
        private static String msg;

        public DummyJob() {

        }

        public String getMessage() {
            return msg;
        }

        @OnTimerMethodAnnotation("dummyScheduleMethod")
        public void dummyScheduleMethod(String str) {
            msg = str;
        }
    }
}
