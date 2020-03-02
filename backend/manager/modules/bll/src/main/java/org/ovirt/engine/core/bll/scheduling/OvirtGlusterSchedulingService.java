package org.ovirt.engine.core.bll.scheduling;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterJobDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterJobParams;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterJobParamsDao;
import org.ovirt.engine.core.dao.gluster.GlusterSchedulerDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class OvirtGlusterSchedulingService {

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @Inject
    private GlusterSchedulerDao scheduleDao;

    @Resource
    private TimerService timerService;

    @Inject
    private GlusterJobParamsDao jobParamsDao;

    protected final Logger log = LoggerFactory.getLogger(getClass());


    public Guid schedule(String className,
            Guid jobId,
            String methodName,
            List<String> inputTypes,
            List<String> inputParams,
            String cronExpression,
            Date startDate,
            Date endDate,
            String timeZone) {
        log.info("Scheduling a cron job. className: {},methodName : {} , jobId: {}", className, methodName, jobId);
        boolean isNewJob = false;
        if (Guid.isNullOrEmpty(jobId)) {
            jobId = Guid.newGuid();
            isNewJob = true;
        }
        GlusterJobDetails job = new GlusterJobDetails();
        job.setJobId(jobId);
        job.setJobClassName(className);
        job.setJobName(methodName);
        job.setCronSchedule(cronExpression);

        try {
            if (startDate != null) {
                job.setStartDate(convertDate(startDate));
            }
            if (endDate != null) {
                job.setEndDate(convertDate(endDate));
            }

            } catch (ParseException e) {
            log.error("Error while converting the date to simple date format startDate: {} endDate:{}",
                    startDate,
                    endDate);
            }
        if (StringUtils.isNotBlank(timeZone)) {
            job.setTimeZone(timeZone);
        }
        List<GlusterJobParams> params = new ArrayList<>();
        for (int i = 0; i < inputTypes.size(); i++) {
            GlusterJobParams param = new GlusterJobParams();
            param.setJobId(job.getJobId());
            param.setParamsClassName(inputTypes.get(i));
            param.setParamsClassValue(inputParams.get(i));
            params.add(param);
        }
        if (isNewJob) {
            log.info("Scheduling a new cron job. className: {},methodName : {} , jobId: {}",
                    className,
                    methodName,
                    jobId);
            scheduleDao.save(job);
            jobParamsDao.save(job.getJobId(), params);
        }
        TimerConfig config = new TimerConfig();
        config.setInfo(job.getJobId());
        config.setPersistent(false);
        Timer t = timerService.createCalendarTimer(getScheduleExpression(cronExpression, startDate, endDate, timeZone),
                config);
        log.info("timer info {}  next execution Time {}", t.getInfo(), t.getNextTimeout());

        return jobId;
    }

    @Timeout
    public void programmaticTimeout(Timer config) {
        Guid glusterJobId = null;
        log.info("timer String {} ", config.getInfo());
        glusterJobId = Guid.createGuidFromString(config.getInfo().toString());
        GlusterJobDetails job = scheduleDao.getGlusterJobById(glusterJobId);
        List<GlusterJobParams> params = jobParamsDao.getJobParamsById(glusterJobId);
        Class<?>[] parameterTypes = new Class<?>[params.size()];
        String[] parameterValues = new String[params.size()];
        Method methodToRun = null;
        Class<?> clazz = null;
        try {
            for (int i = 0; i < params.size(); i++) {
                parameterTypes[i] = Class.forName(params.get(i).getParamsClassName());
                parameterValues[i] = params.get(i).getParamsClassValue();
                clazz = Class.forName(job.getJobClassName());
            }
            methodToRun = clazz.getDeclaredMethod(job.getJobName(), (Class<?>[]) parameterTypes);
            if(methodToRun == null) {
                log.error("could not find the required method '{}' on instance of {}",
                        job.getJobName(),
                        clazz.getClass().getSimpleName());
                return;
            }
            log.info("Preparing to run method: {} of Class: {} , with arguments {}",
                    methodToRun.getName(),
                    clazz.getSimpleName(),
                    Arrays.toString(parameterValues));
            methodToRun.invoke(clazz.getDeclaredConstructor().newInstance(), (Object[]) parameterValues);
        } catch (Throwable t) {
            log.error("Exception in performing {} Job: {}",
                    clazz.getName(),
                    ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);

        }
    }

    public void deleteScheduledJob(Guid jobId) {
        log.info("Timers : {}", timerService.getTimers());
        timerService.getTimers()
                .stream()
                .filter(t -> t.getInfo().toString().equals(jobId.toString()))
                .findAny()
                .ifPresent(t -> t.cancel());
        scheduleDao.remove(jobId);
        jobParamsDao.remove(jobId);
    }

    public void scheduleAllJobs() {
        timerService.getTimers()
                .stream()
                .forEach(t -> t.cancel());
        List<GlusterJobDetails> jobs = scheduleDao.getAllJobs();
        for (GlusterJobDetails job : jobs) {

            List<GlusterJobParams> params = jobParamsDao.getJobParamsById(job.getJobId());
            List<String> inputTypes = params.stream().map(p -> p.getParamsClassName()).collect(Collectors.toList());
            List<String> inputParams = params.stream().map(p -> p.getParamsClassValue()).collect(Collectors.toList());
            schedule(job.getJobClassName(),
                    job.getJobId(),
                    job.getJobName(),
                    inputTypes,
                    inputParams,
                    job.getCronSchedule(),
                    job.getStartDate(),
                    job.getEndDate(),
                    job.getTimeZone());
        }
    }

    public Guid scheduleAFixedDelayJob(Object instance,
            String methodName,
            Class<?>[] inputTypes,
            Object[] inputParams,
            long initialDelay,
            long taskDelay,
            TimeUnit timeUnit) {
        executor.scheduleWithFixedDelay(getRunnable(instance, methodName, inputTypes, inputParams),
                initialDelay,
                taskDelay,
                TimeUnit.SECONDS);
        return Guid.newGuid();

    }

    public Runnable getRunnable(Object instance, String methodName, Class<?>[] inputTypes, Object[] inputParams) {
        Method methodToRun = null;
        Method[] methods = instance.getClass().getMethods();
        for (Method method : methods) {
            OnTimerMethodAnnotation annotation = method.getAnnotation(OnTimerMethodAnnotation.class);
            if (annotation != null && methodName.equals(annotation.value())) {
                methodToRun = method;
                break;
            }
        }
        final Method finalMethodToRun = methodToRun;
        return new Runnable() {

            @Override
            public void run() {
                try {
                    finalMethodToRun.invoke(instance, inputParams);
                } catch (Throwable t) {
                    log.error("Exception in performing {} Job: {}",
                            instance.getClass().getSimpleName(),
                            ExceptionUtils.getRootCauseMessage(t));
                    log.debug("Exception", t);
                }

            }
        };
    }

    protected ScheduleExpression getScheduleExpression(String cronExpression, Date start, Date end, String timeZone) {
        String[] splittedCronJob = cronExpression.split(" ");

        ScheduleExpression scheduledExpression = new ScheduleExpression();

        scheduledExpression
                .second(splittedCronJob[0])
                .minute(splittedCronJob[1])
                .hour(splittedCronJob[2])
                .dayOfMonth(splittedCronJob[3])
                .month(splittedCronJob[4])
                // dayOfWeek() throws ParseException on processing "?"
                .dayOfWeek(StringUtils.equals(splittedCronJob[5], "?") ? "*" : splittedCronJob[5])
                .year(splittedCronJob[6])
                .start(start);
        if (end != null) {
            scheduledExpression.end(end);
        }
        if (StringUtils.isNotBlank(timeZone)) {
            scheduledExpression.timezone(timeZone);
        }
        return scheduledExpression;
    }

    public Date convertDate(Date inDate) throws ParseException {
        if (inDate == null) {
            return null;
        }
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String formattedDate = format.format(inDate);
        return format.parse(formattedDate);
    }
}
