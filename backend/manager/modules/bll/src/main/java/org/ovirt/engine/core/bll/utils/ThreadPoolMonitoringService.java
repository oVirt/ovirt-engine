package org.ovirt.engine.core.bll.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ThreadPoolMonitoringService implements BackendService {

    private static Logger log = LoggerFactory.getLogger(ThreadPoolMonitoringService.class);

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineThreadMonitoringThreadPool)
    private ManagedScheduledExecutorService executor;

    private ThreadMXBean threadMXBean;

    private Map<String, ThreadPoolInfo> threadPoolInfoMap = new TreeMap<>();

    @PostConstruct
    public void init() {
        try {
            threadMXBean = ManagementFactory.getThreadMXBean();
        } catch (Exception e) {
            throw new IllegalStateException("Problem getting ThreadMXBean:" + e);
        }

        log.info("Initializing Thread Monitoring Service");
        int threadPoolMonitoringIntervalInSeconds =
                EngineLocalConfig.getInstance().getInteger("THREAD_POOL_MONITORING_INTERVAL_IN_SECONDS");
        if (threadPoolMonitoringIntervalInSeconds <= 0) {
            log.info("Thread Monitoring Service is disabled.");
        } else {
            executor.scheduleWithFixedDelay(this::monitorEngineThreadPools,
                    0,
                    threadPoolMonitoringIntervalInSeconds,
                    TimeUnit.SECONDS);
            log.info("Thread Monitoring Service initialized");
        }
    }

    private void monitorEngineThreadPools() {
        try {
            threadPoolInfoMap.clear();
            Arrays.asList(threadMXBean.getAllThreadIds())
                    .stream()
                    .forEach(threadId -> processThread(threadMXBean.getThreadInfo(threadId)));
            threadPoolInfoMap.entrySet()
                    .stream()
                    .forEach(entry -> log.info(entry.getValue().toString()));
        } catch (Exception ex) {
            log.info("Error fetching thread pools data: {}", ex.getMessage());
            log.debug("Exception", ex);
        }
    }

    private void processThread(ThreadInfo[] threadInfo) {
        Arrays.asList(threadInfo)
                .stream()
                .filter(tInfo -> tInfo.getThreadName().startsWith("EE")) // In Wildfly 11 the managed threads start with EE
                .filter(tInfo -> tInfo.getThreadName().split("-").length > 2)
                .forEach(tInfo -> processThread(tInfo));
    }

    private void processThread(ThreadInfo threadInfo) {
        String[] threadName = threadInfo.getThreadName().split("-");
        threadPoolInfoMap.putIfAbsent(threadName[2], new ThreadPoolInfo(threadName[2]));
        threadPoolInfoMap.get(threadName[2]).processThreadInfo(threadInfo);
    }
}
