package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.pm.PowerManagementHelper;
import org.ovirt.engine.core.bll.pm.PowerManagementHelper.AgentsIterator;
import org.ovirt.engine.core.bll.validator.FenceValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSFenceReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ThreadUtils;

public abstract class FenceVdsBaseCommand<T extends FenceVdsActionParameters> extends VdsCommand<T> {
    private static final int SLEEP_BEFORE_FIRST_ATTEMPT = 5000;
    private static final String INTERNAL_FENCE_USER = "Engine";
    private static final String VDSM_STATUS_UNKONWN = "unknown";
    private static final int UNKNOWN_RESULT_ALLOWED = 3;

    protected FenceValidator fenceValidator;
    protected FenceExecutor fenceExecutor;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected FenceVdsBaseCommand(Guid commandId) {
        super(commandId);
        fenceExecutor = new FenceExecutor(getVds(), getParameters().getFencingPolicy());
        fenceValidator = new FenceValidator();
    }

    public FenceVdsBaseCommand(T parameters) {
        this(parameters, null);
    }

    public FenceVdsBaseCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        fenceExecutor = new FenceExecutor(getVds(), getParameters().getFencingPolicy());
        fenceValidator = new FenceValidator();
    }

    @Override
    protected boolean canDoAction() {
        List<String> messages = getReturnValue().getCanDoActionMessages();
        boolean canDo =
                fenceValidator.isHostExists(getVds(), messages)
                        && fenceValidator.isPowerManagementEnabledAndLegal(getVds(), getVdsGroup(), messages)
                        && fenceValidator.isStartupTimeoutPassed(messages)
                        && isQuietTimeFromLastActionPassed()
                        && fenceValidator.isProxyHostAvailable(getVds(), messages);
        if (!canDo) {
            handleError();
        }
        getReturnValue().setSucceeded(canDo);
        return canDo;
    }

    private boolean isQuietTimeFromLastActionPassed() {
        // Check Quiet time between PM operations, this is done only if command is not internal and parent
        // command is not <Restart>
        int secondsLeftToNextPmOp =
                (isInternalExecution() || (getParameters().getParentCommand() == VdcActionType.RestartVds))
                        ?
                        0
                        :
                        DbFacade.getInstance()
                                .getAuditLogDao()
                                .getTimeToWaitForNextPmOp(getVds().getName(), getRequestedAuditEvent());
        if (secondsLeftToNextPmOp > 0) {
            addCanDoActionMessage(VdcBllMessages.VDS_FENCE_DISABLED_AT_QUIET_TIME);
            addCanDoActionMessageVariable("seconds", secondsLeftToNextPmOp);
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void executeCommand() {
        log.info("Power-Management: {} of host '{}' initiated.", getAction(), getVdsName());
        audit(AuditLogType.FENCE_OPERATION_STARTED);
        VDSStatus lastStatus = getVds().getStatus();
        VDSFenceReturnValue result = null;
        try {
            setup();
            result = fence();
            handleResult(result);
            if (getSucceeded()) {
                log.info("Power-Management: {} host '{}' succeeded.", getAction(), getVdsName());
                audit(AuditLogType.FENCE_OPERATION_SUCCEEDED);
            } else {
                log.info("Power-Management: {} host '{}' failed.", getAction(), getVdsName());
                audit(AuditLogType.FENCE_OPERATION_FAILED);
            }
        } finally {
            if (!getSucceeded()) {
                setStatus(lastStatus);
                if (!wasSkippedDueToPolicy(result)) {
                    // show alert only if command was not skipped due to fencing policy
                    alertIfPowerManagementOperationFailed();
                }
                throw new VdcBLLException(VdcBllErrors.VDS_FENCE_OPERATION_FAILED);
            }
            else {
                teardown();
            }
        }
    }

    private void audit(AuditLogType auditMessage) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("Action", getAction().name());
        logable.addCustomValue("VdsName", getVds().getName());
        logable.setVdsId(getVdsId());
        auditLogDirector.log(logable, auditMessage);
    }

    private void handleResult(VDSFenceReturnValue result) {
        if (wasSkippedDueToPolicy(result)) {
            // when fencing is skipped due to policy we want to suppress command result logging, because
            // we fire an alert in VdsNotRespondingTreatment
            setCommandShouldBeLogged(false);
            setSucceeded(false);
        } else {
            setSucceeded(result.getSucceeded());
        }
        setActionReturnValue(result);
    }

    /**
     * Attempt fencing using agents by order.
     */
    private VDSFenceReturnValue fence() {
        // loop over agents and try to fence.
        AgentsIterator iterator = PowerManagementHelper.getAgentsIterator(getVds().getFenceAgents());
        VDSFenceReturnValue result = null;
        while (iterator.hasNext()) {
            result = fence(iterator.next());
            if (result.getSucceeded()) {
                break;
            }
        }
        return result;
    }

    /**
     * Attempt to fence the host using agent\agents with next order.
     *
     */
    private VDSFenceReturnValue fence(List<FenceAgent> agents) {
        if (agents.size() == 1) {
            return fence(agents.get(0), getFenceRetries());
        } else if (agents.size() > 1) {
            return fenceConcurrently(agents);
        } else { // 0 agents, we never reach here.
            return null;
        }
    }

    /**
     * Creates tasks based on the supplied agents.
     */
    protected List<Callable<VDSFenceReturnValue>> createTasks(List<FenceAgent> agents) {
        List<Callable<VDSFenceReturnValue>> tasks = new ArrayList<Callable<VDSFenceReturnValue>>();
        for (FenceAgent agent : agents) {
            tasks.add(createTask(agent));
        }
        return tasks;
    }

    /**
     * Creates a task based on the supplied agent.
     */
    protected Callable<VDSFenceReturnValue> createTask(final FenceAgent agent) {
        return (new Callable<VDSFenceReturnValue>() {
            @Override
            public VDSFenceReturnValue call() {
                return fence(agent, getFenceRetries());
            }
        });
    }

    private VDSFenceReturnValue fence(FenceAgent fenceAgent, int retries) {
        VDSFenceReturnValue fenceExecutionResult = fenceExecutor.fence(getAction(), fenceAgent);
        if (wasSkippedDueToStatus(fenceExecutionResult)) {
            log.info("Attemp to {} host using fence agent '{}' skipped, host is already at the requested state.",
                    getAction().name().toLowerCase(),
                    fenceAgent.getId());
        } else if (wasSkippedDueToPolicy(fenceExecutionResult)) {
            // fencing execution was skipped due to fencing policy
            return fenceExecutionResult;
        } else {
            if (fenceExecutionResult.getSucceeded()) {
                boolean requiredStatusAchieved = waitForStatus();
                int i = 0;
                while (!requiredStatusAchieved && i < retries) {
                    fenceExecutionResult = fenceExecutor.fence(getAction(), fenceAgent);
                    requiredStatusAchieved = waitForStatus();
                    i++;
                }
                if (requiredStatusAchieved) {
                    handleSpecificCommandActions();
                } else {
                    auditFailure();
                }
                fenceExecutionResult.setSucceeded(requiredStatusAchieved);
            } else {
                logAgentFailure(fenceExecutionResult);
            }
        }
        return fenceExecutionResult;
    }

    private void logAgentFailure(final VDSFenceReturnValue result) {
        if (!wasSkippedDueToPolicy(result)) {
            log.error("Failed to {} host using fence agent {} (if other agents are running, {} may still succeed).",
                    getAction().name().toLowerCase(),
                    result.getFenceAgentUsed().getId() == null ? "New Agent (no ID)" : result.getFenceAgentUsed()
                            .getId(),
                    getAction().name().toLowerCase());
        }
    }

    protected void setStatus() {
        Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVdsStatus,
                        new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Reboot));
        runSleepOnReboot();
    }

    protected void setStatus(VDSStatus status) {
        if (getVds().getStatus() != status) {
            getBackend().getResourceManager().RunVdsCommand(VDSCommandType.SetVdsStatus,
                    new SetVdsStatusVDSCommandParameters(getVds().getId(), status));
        }
    }

    @Override
    public String getUserName() {
        String userName = super.getUserName();
        return StringUtils.isEmpty(userName) ? INTERNAL_FENCE_USER : userName;
    }

    protected boolean waitForStatus() {
        int i = 1;
        int j = 1;
        boolean requiredStatusReached = false;
        String requiredStatus = getRequiredStatus();
        String hostName = getVds().getName();
        log.info("Waiting for host '{}' to reach status '{}'", hostName, requiredStatus);
        // Waiting before first attempt to check the host status.
        // This is done because if we will attempt to get host status immediately
        // in most cases it will not turn from on/off to off/on and we will need
        // to wait a full cycle for it.
        ThreadUtils.sleep(getSleepBeforeFirstAttempt());
        int retries = getWaitForStatusRerties();
        while (!requiredStatusReached && i <= retries) {
            log.info("Attempt {} to get host '{}' status", i, hostName);
            VDSFenceReturnValue returnValue = fenceExecutor.checkHostStatus();
            if (returnValue != null && returnValue.getSucceeded()) {
                String status = ((FenceStatusReturnValue) returnValue.getReturnValue()).getStatus();
                if (status.equalsIgnoreCase(VDSM_STATUS_UNKONWN)) {
                    // Allow command to fail temporarily
                    if (j <= UNKNOWN_RESULT_ALLOWED && i <= retries) {
                        ThreadUtils.sleep(getDelayInSeconds() * 1000);
                        i++;
                        j++;
                    } else {
                        // No need to retry , agent definitions are corrupted
                        log.error("Host '{}' PM Agent definitions are corrupted, aborting fence operation.", hostName);
                        break;
                    }
                }
                else {
                    if (requiredStatus.equalsIgnoreCase(status)) {
                        requiredStatusReached = true;
                        log.info("Host '{}' status is '{}'", hostName, requiredStatus);
                    } else {
                        i++;
                        if (i <= retries) {
                            ThreadUtils.sleep(getDelayInSeconds() * 1000);
                        }
                    }
                }
            } else {
                log.error("Failed to get host '{}' status.", hostName);
                break;
            }
        }
        return requiredStatusReached;
    }

    protected void auditFailure() {
        // Send an Alert
        String actionName = (getParameters().getParentCommand() == VdcActionType.RestartVds) ?
                FenceActionType.RESTART.name() : getAction().name();
        AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.addCustomValue("Host", getVds().getName());
        auditLogable.addCustomValue("Status", actionName);
        auditLogable.setVdsId(getVds().getId());
        auditLogDirector.log(auditLogable, AuditLogType.VDS_ALERT_FENCE_STATUS_VERIFICATION_FAILED);
        log.error("Failed to verify host '{}' {} status. Have retried {} times with delay of {} seconds between each retry.",
                getVds().getName(),
                getAction().name(),
                getWaitForStatusRerties(),
                getDelayInSeconds());

    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return createFenceExclusiveLocksMap(getVdsId());
    }

    public static Map<String, Pair<String, String>> createFenceExclusiveLocksMap(Guid vdsId) {
        return Collections.singletonMap(vdsId.toString(), LockMessagesMatchUtil.makeLockingPair(
                LockingGroup.VDS_FENCE,
                VdcBllMessages.POWER_MANAGEMENT_ACTION_ON_ENTITY_ALREADY_IN_PROGRESS));
    }

    protected void logConcurrentAgentsFailure(FenceActionType action,
            List<FenceAgent> agents,
            VDSFenceReturnValue result) {
        StringBuilder builder = new StringBuilder();
        for (FenceAgent agent : agents) {
            builder.append(agent.getId()).append(", ");
        }
        String agentIds = builder.toString();
        agentIds = agentIds.substring(0, agentIds.length() - 2);
        log.error("Failed to {} host using fence agents '{}' concurrently: {}",
                action.name(),
                agentIds,
                result.getExceptionString());

    }

    protected FenceProxyLocator createProxyHostLocator() {
        return new FenceProxyLocator(getVds(), getParameters().getFencingPolicy());
    }

    protected List<VM> getVmList() {
        return getVmDAO().getAllRunningForVds(getVdsId());
    }

    /**
     * in the specific scenario where this stop/start command is executed in the context of a restart, we interpret
     * 'skipped' as having occurred to due a fencing policy violation.
     */
    private boolean wasSkippedDueToPolicy(VDSFenceReturnValue result) {
        return result != null
                && result.isSkippedDueToPolicy()
                && getParameters().getParentCommand() == VdcActionType.RestartVds;
    }

    /**
     * if stop/start command returned with status=skipped, and the command was NOT run in the context of a restart then
     * we interpret the skip as having occurred because the host is already at the required state.
     */
    private boolean wasSkippedDueToStatus(VDSFenceReturnValue result) {
        return result != null
                && result.isSkippedDueToStatus()
                && getParameters().getParentCommand() != VdcActionType.RestartVds;
    }

    public FenceValidator getFenceValidator() {
        return fenceValidator;
    }

    public void setFenceValidator(FenceValidator fenceValidator) {
        this.fenceValidator = fenceValidator;
    }

    // Exported to a method for mocking purposes (when running unit-tests, we don't want to wait 5 seconds for each
    // test...)
    int getSleepBeforeFirstAttempt() {
        return SLEEP_BEFORE_FIRST_ATTEMPT;
    }

    public FenceExecutor getFenceExecutor() {
        return fenceExecutor;
    }

    public void setFenceExecutor(FenceExecutor fenceExecutor) {
        this.fenceExecutor = fenceExecutor;
    }

    /**
     * get the event to look for in canDoAction() , if we requested to start Host then we should look when we stopped it
     * and vice
     */
    protected abstract String getRequestedAuditEvent();

    protected abstract void handleError();

    protected abstract void setup();

    protected abstract void teardown();

    protected abstract String getRequiredStatus();

    protected abstract void handleSpecificCommandActions();

    /**
     * Gets the number of times to retry a get status PM operation after stop/start PM operation.
     */
    protected abstract int getWaitForStatusRerties();

    /**
     * Attempt to fence the host using several agents with the same 'order' (thus considered 'concurrent'). Return the
     * result of one of the agents (the most relevant one is chosen).
     */
    protected abstract VDSFenceReturnValue fenceConcurrently(List<FenceAgent> agents);

    /**
     * Get the number of time to retry the fence operation, if the first attempt fails.
     */
    protected abstract int getFenceRetries();

    /**
     * Gets the number of seconds to delay between each retry.
     */
    protected abstract int getDelayInSeconds();

    /**
     * Get the fence action
     */
    protected abstract FenceActionType getAction();

    /**
     * Returns numbers of agents for which fencing operation was skipped
     */
    protected int countSkippedOperations(List<VDSFenceReturnValue> results) {
        int numOfSkipped = 0;
        for (VDSFenceReturnValue result : results) {
            if (wasSkippedDueToPolicy(result)) {
                numOfSkipped++;
            }
        }
        return numOfSkipped;
    }
}
