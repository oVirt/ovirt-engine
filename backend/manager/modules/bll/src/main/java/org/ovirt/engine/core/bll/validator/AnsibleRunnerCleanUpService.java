package org.ovirt.engine.core.bll.validator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AnsibleRunnerCleanUpService implements BackendService {

    private static Logger log = LoggerFactory.getLogger(AnsibleRunnerCleanUpService.class);
    private final String executionDate = "execution_date.txt";

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @PostConstruct
    public void scheduleJob() {
        double interval = Config.<Double>getValue(ConfigValues.ArtifactsOutdatedCheckTimeInHours);
        final int HOURS_TO_MINUTES = 60;
        long intervalInMinutes = Math.round(interval * HOURS_TO_MINUTES);

        executor.schedule(this::checkExecutionTimeStamp,
                intervalInMinutes,
                TimeUnit.MINUTES);
    }

    private void checkExecutionTimeStamp() {
        List<File> uuids = Stream.of(new File(AnsibleConstants.ANSIBLE_RUNNER_PATH.toString()).listFiles())
                .collect(Collectors.toList());
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cal = Calendar.getInstance();
        int artifactsLifeTime = Config.<Integer>getValue(ConfigValues.AnsibleRunnerArtifactsLifetimeInDays);
        String timeStamp = "";
        for (File uuid : uuids) {
            Path executionDatePath = Paths.get(uuid.getAbsolutePath(), this.executionDate);
            try {
                timeStamp = Files.readString(executionDatePath);
                Date executionDate = dateFormatter.parse(timeStamp);
                cal.setTime(executionDate);
                cal.add(Calendar.DAY_OF_MONTH, artifactsLifeTime);
                Date today = dateFormatter.parse(dateFormatter.format(new Date()));
                if (cal.getTime().before(today)) {
                    deleteDir(uuid);
                }
            } catch (IOException e) {
                log.error("Failed to read file: {}", executionDatePath);
                log.debug("Exception: ", e);
            } catch (ParseException e) {
                log.error("Failed to parse timestamp: {}", timeStamp);
                log.debug("Exception: ", e);
            }
        }
    }

    private void deleteDir(File uuid) {
        try {
            Files.walk(Paths.get(uuid.getAbsolutePath()))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch(IOException e) {
            log.error("Failed to delete dir content: {}", uuid.getAbsolutePath());
            log.debug("Exception: ", e);
        }
    }
}
