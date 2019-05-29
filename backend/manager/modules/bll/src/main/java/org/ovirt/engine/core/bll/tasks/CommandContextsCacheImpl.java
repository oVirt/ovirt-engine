package org.ovirt.engine.core.bll.tasks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandContextsCache;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.utils.PersistedCommandContext;
import org.ovirt.engine.core.compat.Guid;

@Singleton
public class CommandContextsCacheImpl implements CommandContextsCache {

    @Inject
    private CommandsCache commandsCache;

    @Inject
    private JobRepository jobRepository;

    private ConcurrentMap<Guid, CommandContext> contextsMap;

    @PostConstruct
    public void initContextsMap() {
        contextsMap = new ConcurrentHashMap<>();
        for (Guid cmdId : commandsCache.keySet()) {
            contextsMap.put(cmdId, buildCommandContext(commandsCache.get(cmdId)));
        }
    }

    private CommandContext buildCommandContext(CommandEntity cmdEntity) {
        ExecutionContext executionContext = new ExecutionContext();
        PersistedCommandContext persistedCommandContext = cmdEntity.getCommandContext();
        if (!Guid.isNullOrEmpty(persistedCommandContext.getJobId())) {
            executionContext.setJob(jobRepository.getJobWithSteps(persistedCommandContext.getJobId()));
        } else if (!Guid.isNullOrEmpty(persistedCommandContext.getStepId())) {
            executionContext.setStep(jobRepository.getStep(persistedCommandContext.getStepId(), false));
        }
        executionContext.setExecutionMethod(persistedCommandContext.getExecutionMethod());
        executionContext.setCompleted(persistedCommandContext.isCompleted());
        executionContext.setJobRequired(persistedCommandContext.isJobRequired());
        executionContext.setMonitored(persistedCommandContext.isMonitored());
        executionContext.setShouldEndJob(persistedCommandContext.shouldEndJob());
        executionContext.setTasksMonitored(persistedCommandContext.isTasksMonitored());
        return new CommandContext(new EngineContext()).withExecutionContext(executionContext);
    }

    @Override
    public CommandContext get(Guid commandId) {
        return contextsMap.get(commandId);
    }

    @Override
    public void remove(final Guid commandId) {
        contextsMap.remove(commandId);
    }

    @Override
    public void put(final Guid cmdId, final CommandContext context) {
        contextsMap.put(cmdId, context);
    }

}
