package org.ovirt.engine.core.common.utils;

import java.util.Date;

import javax.enterprise.concurrent.LastExecution;
import javax.enterprise.concurrent.Trigger;

import org.springframework.scheduling.support.CronSequenceGenerator;

public class EngineCronTrigger implements Trigger {

    private CronSequenceGenerator generator;

    public EngineCronTrigger(String cronExpression) {
        generator = new CronSequenceGenerator(cronExpression);
    }

    @Override
    public Date getNextRunTime(LastExecution lastExecution, Date date) {
        return generator.next(new Date());
    }

    @Override
    public boolean skipRun(LastExecution lastExecution, Date date) {
        return false;
    }
}
