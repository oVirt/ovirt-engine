package org.ovirt.engine.core.utils.timer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ovirt.engine.core.utils.ResourceUtils;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

public class DBSchedulerUtilQuartzImplTest {

    private static DBSchedulerUtilQuartzImpl scheduler;

    @BeforeClass
    public static void init() {
        String QUARTZ_DB_TEST_PROPERTIES = "ovirt-db-scheduler-test.properties";
        Properties props = null;
        try {
            props = ResourceUtils.loadProperties(SchedulerUtil.class, QUARTZ_DB_TEST_PROPERTIES);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Can't load properties from resource \"" +
                            QUARTZ_DB_TEST_PROPERTIES + "\".", exception);
        }
        scheduler = new DBSchedulerUtilQuartzImpl();
        scheduler.setup(props);
    }

    @AfterClass
    public static void tearDown() {
        scheduler.teardown();
    }

    @Test
    public void scheduleAJob() throws InterruptedException {
        DummyJob dummyJob = new DummyJob();
        String jobName = scheduler.scheduleAOneTimeJob(dummyJob, "dummyScheduleMethod",
                new Class[] { String.class },
                new Object[] { "scheduleAJob" }, 1, TimeUnit.MILLISECONDS);

        Thread.sleep(10);
        try {
            JobDetail job = scheduler.getRawScheduler().getJobDetail(JobKey.jobKey(jobName));
            assertNotNull(job);
        } catch (SchedulerException e) {
            fail("Unexpected exception occured -" + e.getMessage());
            e.printStackTrace();
        } finally {
            scheduler.deleteJob(jobName);
        }
    }

    @Test
    public void scheduleARecurringJob() throws InterruptedException {
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
        } catch (SchedulerException e) {
            fail("Unexpected exception occured -" + e.getMessage());
            e.printStackTrace();
        } finally {
            scheduler.deleteJob(jobName);
        }
    }


}

class DummyJob implements Serializable {
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
