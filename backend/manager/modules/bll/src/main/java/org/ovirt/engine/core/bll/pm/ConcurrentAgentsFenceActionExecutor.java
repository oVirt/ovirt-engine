package org.ovirt.engine.core.bll.pm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It manages:
 * <ul>
 *     <li>Execution of fence action for multiple concurrent fence agents</li>
 *     <li>Execution of fence action for specified fence agents concurrently using
 *         {@code ExecutorCompletionService}</li>
 *     <li>Evaluation of results of executed actions to result for whole concurrent action execution</li>
 * </ul>
 */
public class ConcurrentAgentsFenceActionExecutor implements FenceActionExecutor {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentAgentsFenceActionExecutor.class);

    private final VDS fencedHost;
    private final List<FenceAgent> fenceAgents;
    private final FencingPolicy fencingPolicy;

    /**
     * Processor which manages action execution
     */
    protected BaseTaskProcessor tasksProcessor;

    public ConcurrentAgentsFenceActionExecutor(
            VDS fencedHost,
            List<FenceAgent> fenceAgents,
            FencingPolicy fencingPolicy
    ) {
        this.fencedHost = fencedHost;
        this.fenceAgents = fenceAgents;
        this.fencingPolicy = fencingPolicy;
    }

    @Override
    public FenceOperationResult fence(FenceActionType fenceAction) {
        setupParams(fenceAction);
        return fenceConcurrently(fenceAction);
    }

    /**
     * Setup parameters for specified fence action
     */
    protected void setupParams(FenceActionType fenceAction) {
        switch (fenceAction) {
            case START:
                tasksProcessor = new StartActionTaskProcessor(fencedHost);
                break;

            case STOP:
                tasksProcessor = new StopActionTaskProcessor(fencedHost);
                break;

            case STATUS:
                tasksProcessor = new StatusActionTaskProcessor(fencedHost);
                break;
        }
    }

    /**
     * Creates instance of fence executor for specified agent
     */
    protected FenceActionExecutor createFenceActionExecutor(FenceAgent fenceAgent) {
        return new SingleAgentFenceActionExecutor(fencedHost, fenceAgent, fencingPolicy);
    }

    /**
     * Creates task which executes specified fence action using specified fence executor
     */
    protected Callable<FenceOperationResult> createTask(
            final FenceActionExecutor executor,
            final FenceActionType fenceAction
    ) {
        return () -> executor.fence(fenceAction);
    }

    /**
     * Creates list of tasks to execute specified fence actions on all fence agents
     */
    protected List<Callable<FenceOperationResult>> createTasks(FenceActionType fenceAction) {
        List<Callable<FenceOperationResult>> tasks = new ArrayList<>();
        for (FenceAgent fenceAgent : fenceAgents) {
            tasks.add(createTask(createFenceActionExecutor(fenceAgent), fenceAction));
        }
        return tasks;
    }

    /**
     * Executes specified fence action on all fence agents concurrently
     */
    protected FenceOperationResult fenceConcurrently(FenceActionType fenceAction) {
        List<FenceOperationResult> results = new ArrayList<>(fenceAgents.size());
        FenceOperationResult taskResult;

        ExecutorCompletionService<FenceOperationResult> tasksExecutor = ThreadPoolUtil.createCompletionService();
        List<Future<FenceOperationResult>> futures =
                ThreadPoolUtil.submitTasks(tasksExecutor, createTasks(fenceAction));

        for (int i = 0; i < fenceAgents.size(); ++i) {
            try {
                taskResult = tasksExecutor.take().get();
            } catch (ExecutionException | InterruptedException e) {
                taskResult = new FenceOperationResult(
                        Status.ERROR,
                        PowerStatus.UNKNOWN,
                        e.getMessage());
                log.error("Error getting task result: {}", e.getMessage());
                log.debug("Exception", e);
            }

            results.add(taskResult);
            if (tasksProcessor.isGoalReached(taskResult)) {
                // action goal is reach, cancel all remaining tasks
                for (Future<FenceOperationResult> future : futures) {
                    future.cancel(true);
                }
                break;
            }
        }
        return tasksProcessor.createActionResult(results);
    }

    /**
     * Base class to manage processing of fence action for concurrent agents
     */
    protected abstract static class BaseTaskProcessor {
        protected final VDS fencedHost;

        public BaseTaskProcessor(VDS fencedHost) {
            this.fencedHost = fencedHost;
        }

        /**
         * Checks if fence action goal is reached
         * @param result
         *               result of a finished task
         * @return {@code true} if fence action goal was reached, otherwise {@code false}
         */
        public abstract boolean isGoalReached(FenceOperationResult result);

        /**
         * Creates a result the whole fence action on concurrent agents
         * @param taskResults
         *               list of results of all tasks
         * @return result the whole fence action on concurrent agents
         */
        public abstract FenceOperationResult createActionResult(List<FenceOperationResult> taskResults);
    }

    /**
     * Manages processing of fence status action for concurrent agents
     */
    protected static class StatusActionTaskProcessor extends BaseTaskProcessor {
        public StatusActionTaskProcessor(VDS fencedHost) {
            super(fencedHost);
        }

        /**
         * If at least one agent reports status {@code PowerStatus.ON}, the host is on (so the goal of status
         * action is reached), otherwise and we have to continue with processing
         */
        @Override
        public boolean isGoalReached(FenceOperationResult result) {
            return result.getPowerStatus() == PowerStatus.ON;
        }

        @Override
        public FenceOperationResult createActionResult(List<FenceOperationResult> taskResults) {
            FenceOperationResult successfulResult = null;
            int statusOffReported = 0;
            int errorReported = 0;
            for (FenceOperationResult result : taskResults) {
                if (result.getPowerStatus() == PowerStatus.ON) {
                    // one task reported power status on, so the host should be on
                    return result;
                }

                if (result.getStatus() == Status.SUCCESS) {
                    // save successful task result, so we know that at least one status attempt was successful with
                    // power status off, so we can return it
                    successfulResult = result;
                    if (result.getPowerStatus() == PowerStatus.OFF) {
                        // note that we received off status
                        statusOffReported++;
                    }
                } else {
                    errorReported++;
                }
            }

            if (statusOffReported > 0) {
                if (errorReported > 0) {
                    // we received at least one error and at least one power off status reported, so we cannot determine
                    // if host is really powered off
                    return new FenceOperationResult(
                            Status.ERROR,
                            PowerStatus.UNKNOWN,
                            String.format(
                                   "Unable to determine host '%s' power status: at least one agent failed to get"
                                           + "status and at least one agent reported host is powered off.",
                                    fencedHost));
                } else {
                    // no errors received, report successful result
                    return successfulResult;
                }
            }

            // all tasks returned error, so the whole status action failed
            return new FenceOperationResult(
                    Status.ERROR,
                    PowerStatus.UNKNOWN,
                    "All agents failed to get host power status.");
        }
    }

    /**
     * Manages processing of fence start action for concurrent agents
     */
    protected static class StartActionTaskProcessor extends BaseTaskProcessor {
        public StartActionTaskProcessor(VDS fencedHost) {
            super(fencedHost);
        }

        /**
         * If at least one agent reports status {@code PowerStatus.ON}, the host is on (so the goal of start
         * action is reached), otherwise and we have to continue with processing
         */
        @Override
        public boolean isGoalReached(FenceOperationResult result) {
            return result.getPowerStatus() == PowerStatus.ON;
        }

        @Override
        public FenceOperationResult createActionResult(List<FenceOperationResult> taskResults) {
            for (FenceOperationResult result : taskResults) {
                if (result.getPowerStatus() == PowerStatus.ON) {
                    return result;
                }
            }

            // no task reported status ON, so whole start operation has to fail
            return new FenceOperationResult(Status.ERROR, PowerStatus.UNKNOWN);
        }
    }

    /**
     * Manages processing of fence stop action for concurrent agents
     */
    protected static class StopActionTaskProcessor extends BaseTaskProcessor {
        public StopActionTaskProcessor(VDS fencedHost) {
            super(fencedHost);
        }

        /**
         * For stop action all agents have to report status {@code PowerStatus.OFF}, so we have to continue with
         * processing until all agents reported its result
         */
        @Override
        public boolean isGoalReached(FenceOperationResult result) {
            return false;
        }

        @Override
        public FenceOperationResult createActionResult(List<FenceOperationResult> taskResults) {
            int skippedDueToPolicy = 0;
            FenceOperationResult skippedDueToPolicyResult = null;
            for (FenceOperationResult result : taskResults) {
                if (result.getStatus() == Status.SKIPPED_DUE_TO_POLICY) {
                    skippedDueToPolicy++;
                    skippedDueToPolicyResult = result;
                } else if (result.getStatus() != Status.SUCCESS) {
                    // stop action on one agent failed, so the whole action has to fail also
                    return result;
                }
            }

            if (skippedDueToPolicy == taskResults.size()) {
                // all agents reported skipped due to policy, return it as a whole operation result
                return skippedDueToPolicyResult;
            } else if (skippedDueToPolicy > 0) {
                // only some agents reported skipped due to policy, return error
                return new FenceOperationResult(
                        Status.ERROR,
                        PowerStatus.UNKNOWN,
                        "Fence action was skipped due to fencing policy only on some of fence agents, but not all");
            }

            // all tasks returned status off, so the whole action is successful
            return new FenceOperationResult(Status.SUCCESS, PowerStatus.OFF);
        }
    }
}
