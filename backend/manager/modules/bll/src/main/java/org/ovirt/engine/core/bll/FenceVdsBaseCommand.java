package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgentOrder;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.pm.FenceConfigHelper;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public abstract class FenceVdsBaseCommand<T extends FenceVdsActionParameters> extends VdsCommand<T> {
    private static final int SLEEP_BEFORE_FIRST_ATTEMPT = 5000;
    private static final String INTERNAL_FENCE_USER = "Engine";
    protected FenceExecutor executor;
    protected List<VM> mVmList = null;
    private boolean privateFenceSucceeded;
    private FenceExecutor primaryExecutor;
    private FenceExecutor secondaryExecutor;
    private FenceInvocationResult primaryResult;
    private FenceInvocationResult secondaryResult;

    protected boolean skippedDueToFencingPolicy;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected FenceVdsBaseCommand(Guid commandId) {
        super(commandId);
        skippedDueToFencingPolicy = false;
    }

    public FenceVdsBaseCommand(T parameters) {
        this(parameters, null);
    }

    public FenceVdsBaseCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        mVmList = getVmDAO().getAllRunningForVds(getVdsId());
        skippedDueToFencingPolicy = false;
    }

    /**
     * Gets the number of times to retry a get status PM operation after stop/start PM operation.
     *
     * @return
     */
    protected abstract int getRerties();

    /**
     * Gets the number of seconds to delay between each retry.
     *
     * @return
     */
    protected abstract int getDelayInSeconds();

    protected boolean getFenceSucceeded() {
        return privateFenceSucceeded;
    }

    protected void setFenceSucceeded(boolean value) {
        privateFenceSucceeded = value;
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = false;
        String event;
        if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
            return false;
        }
        // get the event to look for , if we requested to start Host then we should look when we stopped it and vice
        // versa.
        if (getParameters().getAction() == FenceActionType.Start) {
            event = AuditLogType.USER_VDS_STOP.name();
        }
        else {
            event = AuditLogType.USER_VDS_START.name();
        }
        if (getVds().getpm_enabled()
                && IsPowerManagementLegal(getVds().getStaticData(), getVdsGroup().getcompatibility_version().toString())) {
            // check if we are in the interval of X seconds from startup
            // if yes , system is still initializing , ignore fence operations
            Date waitTo =
                    Backend.getInstance()
                            .getStartedAt()
                            .addSeconds((Integer) Config.getValue(ConfigValues.DisableFenceAtStartupInSec));
            Date now = new Date();
            if (waitTo.before(now) || waitTo.equals(now)) {
                // Check Quiet time between PM operations, this is done only if command is not internal and parent command is not <Restart>
                int secondsLeftToNextPmOp = (isInternalExecution() || (getParameters().getParentCommand() == VdcActionType.RestartVds))
                        ?
                        0
                        :
                        DbFacade.getInstance().getAuditLogDao().getTimeToWaitForNextPmOp(getVds().getName(), event);
                if (secondsLeftToNextPmOp <= 0) {
                    // Check for proxy
                    executor = createExecutorForProxyCheck();
                    if (executor.findProxyHost()) {
                        retValue = true;
                    }
                    else {
                        addCanDoActionMessage(VdcBllMessages.VDS_NO_VDS_PROXY_FOUND);
                    }
                } else {
                    addCanDoActionMessage(VdcBllMessages.VDS_FENCE_DISABLED_AT_QUIET_TIME);
                    addCanDoActionMessageVariable("seconds", secondsLeftToNextPmOp);
                }
            } else {
                addCanDoActionMessage(VdcBllMessages.VDS_FENCE_DISABLED_AT_SYSTEM_STARTUP_INTERVAL);
            }
            // retry operation only when fence is enabled on Host.
            if (!retValue) {
                handleError();
            }
        }
        else {
            addCanDoActionMessage(VdcBllMessages.VDS_FENCE_DISABLED);
            handleError();
        }
        getReturnValue().setSucceeded(retValue);
        return retValue;
    }

    @Override
    protected void executeCommand() {
        VDSStatus lastStatus = getVds().getStatus();
        VDSReturnValue vdsReturnValue = null;
        try {
            // Set status immediately to prevent a race (BZ 636950/656224)
            // Skip setting status if action is manual Start and Host was in Maintenance
            if (! (getParameters().getAction() == FenceActionType.Start && lastStatus == VDSStatus.Maintenance)) {
                setStatus();
            }
            // Check which fence invocation pattern to invoke
            // Regular (no secondary agent) , multiple sequential agents or multiple concurrent agents
            if (StringUtils.isEmpty(getVds().getPmSecondaryIp())){
                handleSingleAgent(lastStatus, vdsReturnValue);
            }
            else {
                if (getVds().isPmSecondaryConcurrent()){
                    handleMultipleConcurrentAgents(lastStatus, vdsReturnValue);
                }
                else {
                    handleMultipleSequentialAgents(lastStatus, vdsReturnValue);
                }
            }
            setSucceeded(getFenceSucceeded());
        } finally {
            if (!getSucceeded()) {
                setStatus(lastStatus);
                if (!skippedDueToFencingPolicy) {
                    // show alert only if command was not skipped due to fencing policy
                    AlertIfPowerManagementOperationFailed();
                }
            }

            // Successful fencing with reboot or shutdown op. Clear the power management policy flag
            else if ((getParameters().getAction() == FenceActionType.Restart
                      || getParameters().getAction() == FenceActionType.Stop)
                    && getParameters().getKeepPolicyPMEnabled() == false){
                getVds().setPowerManagementControlledByPolicy(false);
                getDbFacade().getVdsDynamicDao().updateVdsDynamicPowerManagementPolicyFlag(
                        getVdsId(),
                        getVds().getDynamicData().isPowerManagementControlledByPolicy());
            }
        }
    }

    /**
     * Handling the case of a single fence agent
     * @param lastStatus
     */
    private void handleSingleAgent(VDSStatus lastStatus, VDSReturnValue vdsReturnValue) {
        executor = createFenceExecutor(getParameters().getAction());
        if (executor.findProxyHost()) {
            vdsReturnValue = executor.fence();
            setFenceSucceeded(vdsReturnValue.getSucceeded());
            if (getFenceSucceeded()) {
                if (wasSkippedDueToPolicy(vdsReturnValue.getReturnValue())) {
                    // fencing execution was skipped due to fencing policy
                    handleFencingSkippedDueToPolicy(vdsReturnValue);
                    return;
                } else {
                    executor = createFenceExecutor(FenceActionType.Status);
                    if (waitForStatus(getVds().getName(), getParameters().getAction(), FenceAgentOrder.Primary)) {
                        handleSpecificCommandActions();
                    } else {
                        handleWaitFailure(lastStatus, FenceAgentOrder.Primary);
                    }
                }
            } else {
                handleError(lastStatus, vdsReturnValue, FenceAgentOrder.Primary);
            }
        }
        else {
            setFenceSucceeded(false);
            vdsReturnValue.setSucceeded(false);
        }
    }

    /**
     * Handling the case of a multiple sequential fence agents
     * If operation fails on Primary agent, the Secondary agent is used.
     * @param lastStatus
     */
    private void handleMultipleSequentialAgents(VDSStatus lastStatus, VDSReturnValue vdsReturnValue) {
        executor = createFenceExecutor(getParameters().getAction());
        if (executor.findProxyHost()) {
            vdsReturnValue = executor.fence(FenceAgentOrder.Primary);
            setFenceSucceeded(vdsReturnValue.getSucceeded());
            if (getFenceSucceeded()) {
                if (wasSkippedDueToPolicy(vdsReturnValue.getReturnValue())) {
                    // fencing execution was skipped due to fencing policy
                    handleFencingSkippedDueToPolicy(vdsReturnValue);
                    return;
                } else {
                    executor = createFenceExecutor(FenceActionType.Status);
                    if (waitForStatus(getVds().getName(), getParameters().getAction(), FenceAgentOrder.Primary)) {
                        handleSpecificCommandActions();
                    } else {
                        // set the executor to perform the action
                        executor = createFenceExecutor(getParameters().getAction());
                        tryOtherSequentialAgent(lastStatus, vdsReturnValue);
                    }
                }
            } else {
                tryOtherSequentialAgent(lastStatus, vdsReturnValue);
            }
        }
        else {
            setFenceSucceeded(false);
            vdsReturnValue.setSucceeded(false);
        }
    }

    /**
     * fence the Host via the secondary agent if primary fails
     * @param lastStatus
     */
    private void tryOtherSequentialAgent(VDSStatus lastStatus, VDSReturnValue vdsReturnValue) {
        executor = createFenceExecutor(getParameters().getAction());
        if (executor.findProxyHost()) {
            vdsReturnValue = executor.fence(FenceAgentOrder.Secondary);
            setFenceSucceeded(vdsReturnValue.getSucceeded());
            if (getFenceSucceeded()) {
                if (wasSkippedDueToPolicy(vdsReturnValue.getReturnValue())) {
                    // fencing execution was skipped due to fencing policy
                    handleFencingSkippedDueToPolicy(vdsReturnValue);
                    return;
                } else {
                    executor = createFenceExecutor(FenceActionType.Status);
                    if (waitForStatus(getVds().getName(), getParameters().getAction(), FenceAgentOrder.Secondary)) {
                        // raise an alert that secondary agent was used
                        AuditLogableBase logable = new AuditLogableBase();
                        logable.setVdsId(getVds().getId());
                        logable.addCustomValue("Operation", getParameters().getAction().name());
                        AuditLogDirector.log(logable, AuditLogType.VDS_ALERT_SECONDARY_AGENT_USED_FOR_FENCE_OPERATION);
                        handleSpecificCommandActions();
                    } else {
                        handleWaitFailure(lastStatus, FenceAgentOrder.Secondary);
                    }
                }
            }
            else {
                handleError(lastStatus, vdsReturnValue, FenceAgentOrder.Secondary);
            }
        }
        else {
            setFenceSucceeded(false);
            vdsReturnValue.setSucceeded(false);
        }
    }

    /**
     * Handling the case of a multiple concurrent fence agents
     * for Stop we should have two concurrent threads and wait for both to succeed
     * for Start  we should have two concurrent threads and wait for one to succeed
     * @param lastStatus
     */
    private void handleMultipleConcurrentAgents(VDSStatus lastStatus, VDSReturnValue vdsReturnValue) {
        primaryExecutor = createFenceExecutor(getParameters().getAction());
        secondaryExecutor = createFenceExecutor(getParameters().getAction());
        if (primaryExecutor.findProxyHost() && secondaryExecutor.findProxyHost()) {
            primaryResult = new FenceInvocationResult();
            secondaryResult = new FenceInvocationResult();
            List<Callable<FenceInvocationResult>> tasks = new ArrayList<Callable<FenceInvocationResult>>();
            Future<FenceInvocationResult> f1 = null;
            Future<FenceInvocationResult> f2 = null;
            tasks.add(new Callable<FenceInvocationResult>() {
                @Override
                public FenceInvocationResult call() {
                    return run(primaryExecutor, FenceAgentOrder.Primary);
                }
            });
            tasks.add(new Callable<FenceInvocationResult>() {
                @Override
                public FenceInvocationResult call() {
                    return run(secondaryExecutor, FenceAgentOrder.Secondary);
                }
            });
            try {
                ExecutorCompletionService<FenceInvocationResult> ecs = ThreadPoolUtil.createCompletionService(tasks);
                switch (getParameters().getAction()) {
                case Start:
                    try {
                        f1 = ecs.take();
                        setResult(f1);
                        if (primaryResult.isSucceeded() || secondaryResult.isSucceeded()) {
                            handleSpecificCommandActions();
                            setFenceSucceeded(true);
                        } else {
                            tryOtherConcurrentAgent(lastStatus, ecs);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        tryOtherConcurrentAgent(lastStatus, ecs);
                    }

                    break;
                case Stop:
                    f1 = ecs.take();
                    f2 = ecs.take();

                    if (f1.get().getOrder() == FenceAgentOrder.Primary) {
                        primaryResult = f1.get();
                        secondaryResult = f2.get();
                    } else {
                        primaryResult = f2.get();
                        secondaryResult = f1.get();
                    }
                    if (primaryResult.isSucceeded() && secondaryResult.isSucceeded()) {
                        boolean primarySkipped = wasSkippedDueToPolicy(primaryResult.getValue());
                        boolean secondarySkipped = wasSkippedDueToPolicy(secondaryResult.getValue());
                        if (primarySkipped && secondarySkipped) {
                            // fencing execution was skipped due to fencing policy
                            handleFencingSkippedDueToPolicy(vdsReturnValue);
                            return;
                        } else if (primarySkipped || secondarySkipped) {
                            // fence execution on one agents was skipped and on the other executed
                            handleError(lastStatus,
                                    primarySkipped ? primaryResult.getValue() : secondaryResult.getValue(),
                                    primarySkipped ? FenceAgentOrder.Primary : FenceAgentOrder.Secondary);
                        } else {
                            handleSpecificCommandActions();
                            setFenceSucceeded(true);
                        }
                    } else {
                        handleError(lastStatus,
                                !primaryResult.isSucceeded() ? primaryResult.getValue() : secondaryResult.getValue(),
                                !primaryResult.isSucceeded() ? FenceAgentOrder.Primary : FenceAgentOrder.Secondary);
                    }
                    break;
                default:
                    setFenceSucceeded(true);
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error(e);
            }
        }
        else {
            setFenceSucceeded(false);
            vdsReturnValue.setSucceeded(false);
        }
    }

    private void tryOtherConcurrentAgent(VDSStatus lastStatus, ExecutorCompletionService<FenceInvocationResult> ecs)
            throws InterruptedException, ExecutionException {
        Future<FenceInvocationResult> f2;
        f2 = ecs.take();
        setResult(f2);
        if (primaryResult.isSucceeded() || secondaryResult.isSucceeded()) {
            handleSpecificCommandActions();
            setFenceSucceeded(true);
        } else {
            handleError(lastStatus, primaryResult.getValue(), FenceAgentOrder.Primary);
            handleError(lastStatus, secondaryResult.getValue(), FenceAgentOrder.Secondary);
        }
    }

    private void setResult(Future<FenceInvocationResult> f) throws InterruptedException, ExecutionException {
        if (f.get().getOrder() == FenceAgentOrder.Primary) {
            primaryResult = f.get();
        }
        else {
            secondaryResult = f.get();
        }
    }

    private FenceInvocationResult run(FenceExecutor fenceExecutor, FenceAgentOrder order) {
        FenceInvocationResult fenceInvocationResult = new FenceInvocationResult();
        fenceInvocationResult.setOrder(order);
        fenceInvocationResult.setValue(fenceExecutor.fence(order));
        if (fenceInvocationResult.getValue().getSucceeded()) {
            if (!wasSkippedDueToPolicy(fenceInvocationResult.getValue().getReturnValue())) {
                // execution was not skipped due to policy, get status
                this.executor = createFenceExecutor(FenceActionType.Status);
                fenceInvocationResult.setSucceeded(waitForStatus(getVds().getName(), getParameters().getAction(), order));
            }
        }
        return fenceInvocationResult;
    }
    private void handleWaitFailure(VDSStatus lastStatus, FenceAgentOrder order) {
        VDSReturnValue vdsReturnValue;
        // since there is a chance that Agent & Host use the same power supply and
        // a Start command had failed (because we just get success on the script
        // invocation and not on its result), we have to try the Start command again
        // before giving up
        if (getParameters().getAction() == FenceActionType.Start) {
            executor = createFenceExecutor(FenceActionType.Start);
            vdsReturnValue = executor.fence(order);
            setFenceSucceeded(vdsReturnValue.getSucceeded());
            if (getFenceSucceeded()) {
                executor = createFenceExecutor(FenceActionType.Status);
                if (waitForStatus(getVds().getName(), FenceActionType.Start, order)) {
                    handleSpecificCommandActions();
                } else {
                    setFenceSucceeded(false);
                }
            } else {
                handleError(lastStatus, vdsReturnValue, order);
            }

        } else {
            // We reach this if we wait for on/off status after start/stop as defined in configurable delay/retries and
            // did not reach the desired on/off status.We assume that fence operation didn't complete successfully
            // Setting this flag will cause the appropriate Alert to pop and to restore host status to it's previous
            // value as appears in the finally block.
            setFenceSucceeded(false);
        }
    }

    private void handleError(VDSStatus lastStatus, final VDSReturnValue vdsReturnValue, FenceAgentOrder order) {
        if (!((FenceStatusReturnValue) (vdsReturnValue.getReturnValue())).getIsSkipped()) {
            // Since this is a non-transactive command , restore last status
            setSucceeded(false);
            log.errorFormat("Failed to {0} VDS using {1} Power Management agent", getParameters().getAction()
                    .name()
                    .toLowerCase(), order.name());
            AlertIfPowerManagementOperationSkipped(getParameters().getAction().name(), vdsReturnValue.getExceptionObject());
            throw new VdcBLLException(VdcBllErrors.VDS_FENCE_OPERATION_FAILED);
        } else { // Fence operation was skipped because Host is already in the requested state.
            setStatus(lastStatus);
        }
    }

    /**
     * Create the executor used in the can do action check. The executor created does not do retries to find a proxy
     * host, so that clients calling the can do action will get a quick response, and don't risk timing out.
     *
     * @return An executor used to check the availability of a proxy host.
     */
    protected FenceExecutor createExecutorForProxyCheck() {
        return new FenceExecutor(getVds(), FenceActionType.Status);
    }

    protected void setStatus() {
        runVdsCommand(VDSCommandType.SetVdsStatus,
                        new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Reboot));
        RunSleepOnReboot();
    }

    protected void handleError() {
    }

    @Override
    public String getUserName() {
        String userName = super.getUserName();
        return StringUtils.isEmpty(userName)? INTERNAL_FENCE_USER: userName;
    }



    protected boolean waitForStatus(String vdsName, FenceActionType actionType, FenceAgentOrder order) {
        final String FENCE_CMD = (actionType == FenceActionType.Start) ? "on" : "off";
        final String ACTION_NAME = actionType.name().toLowerCase();
        int i = 1;
        boolean statusReached = false;
        log.infoFormat("Waiting for vds {0} to {1}", vdsName, ACTION_NAME);

        // Waiting before first attempt to check the host status.
        // This is done because if we will attempt to get host status immediately
        // in most cases it will not turn from on/off to off/on and we will need
        // to wait a full cycle for it.
        ThreadUtils.sleep(getSleep(actionType, order));
        while (!statusReached && i <= getRerties()) {
            log.infoFormat("Attempt {0} to get vds {1} status", i, vdsName);
            if (executor.findProxyHost()) {
                VDSReturnValue returnValue = executor.fence(order);
                if (returnValue != null && returnValue.getReturnValue() != null) {
                    FenceStatusReturnValue value = (FenceStatusReturnValue) returnValue.getReturnValue();
                    if (value.getStatus().equalsIgnoreCase("unknown")) {
                        // No need to retry , agent definitions are corrupted
                        log.warnFormat("Host {0} {1} PM Agent definitions are corrupted, Waiting for Host to {2} aborted.", vdsName, order.name(), actionType.name());
                        break;
                    }
                    else {
                        if (FENCE_CMD.equalsIgnoreCase(value.getStatus())) {
                            statusReached = true;
                            log.infoFormat("vds {0} status is {1}", vdsName, FENCE_CMD);
                        } else {
                            i++;
                            if (i <= getRerties())
                                ThreadUtils.sleep(getDelayInSeconds() * 1000);
                        }
                    }
                } else {
                    log.errorFormat("Failed to get host {0} status.", vdsName);
                    break;
                }
            } else {
                break;
            }
        }
        if (!statusReached) {
            // Send an Alert
            String actionName = (getParameters().getParentCommand() == VdcActionType.RestartVds) ?
                    FenceActionType.Restart.name() : ACTION_NAME;
            AuditLogableBase auditLogable = new AuditLogableBase();
            auditLogable.addCustomValue("Host", vdsName);
            auditLogable.addCustomValue("Status", actionName);
            auditLogable.setVdsId(getVds().getId());
            AuditLogDirector.log(auditLogable, AuditLogType.VDS_ALERT_FENCE_STATUS_VERIFICATION_FAILED);
            log.errorFormat("Failed to verify host {0} {1} status. Have retried {2} times with delay of {3} seconds between each retry.",
                    vdsName,
                    ACTION_NAME,
                    getRerties(),
                    getDelayInSeconds());

        }
        return statusReached;
    }

    private int getSleep(FenceActionType actionType, FenceAgentOrder order) {
        if (actionType != FenceActionType.Stop) {
            return SLEEP_BEFORE_FIRST_ATTEMPT;
        }
        // We have to find out if power off delay was used and add this to the wait time
        // since otherwise the command will return immediately with 'off' status and
        // subsequent 'on' command issued during this delay will be overridden by the actual shutdown
        String agent = (order == FenceAgentOrder.Primary) ? getVds().getPmType() : getVds().getPmSecondaryType();
        String options =  (order == FenceAgentOrder.Primary) ? getVds().getPmOptions() : getVds().getPmSecondaryOptions();
        options = VdsFenceOptions.getDefaultAgentOptions(agent, options);
        HashMap<String, String> optionsMap = VdsStatic.pmOptionsStringToMap(options);
        String powerWaitParamSettings = FenceConfigHelper.getFenceConfigurationValue(ConfigValues.FencePowerWaitParam.name(), ConfigCommon.defaultConfigurationVersion);
        String powerWaitParam = VdsFenceOptions.getAgentPowerWaitParam(agent, powerWaitParamSettings);
        if (powerWaitParam == null) {
            // no power wait for this agent
            return SLEEP_BEFORE_FIRST_ATTEMPT;
        }
        if (optionsMap.containsKey(powerWaitParam)) {
            try {
                Integer powerWaitValueInSec = Integer.parseInt(optionsMap.get(powerWaitParam));
                return SLEEP_BEFORE_FIRST_ATTEMPT + (int)TimeUnit.SECONDS.toMillis(powerWaitValueInSec);
            }
            catch(NumberFormatException nfe) {
                // illegal value
                return SLEEP_BEFORE_FIRST_ATTEMPT;
            }
        }
        return SLEEP_BEFORE_FIRST_ATTEMPT;
    }

    protected void setStatus(VDSStatus status) {
        if (getVds().getStatus() != status) {
            runVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(getVds().getId(), status));
        }
    }

    protected abstract void handleSpecificCommandActions();

    public static class FenceInvocationResult {

        private VDSReturnValue value;
        private boolean succeeded=false;
        private FenceAgentOrder order;

        public FenceAgentOrder getOrder() {
            return order;
        }

        public void setOrder(FenceAgentOrder order) {
            this.order = order;
        }

        public FenceInvocationResult() {
        }

        public VDSReturnValue getValue() {
            return value;
        }

        public void setValue(VDSReturnValue value) {
            this.value = value;
        }

        public boolean isSucceeded() {
            return succeeded;
        }

        public void setSucceeded(boolean succeeded) {
            this.succeeded = succeeded;
        }
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

    /**
     * Creates {@code FenceExecutor} instance with default VDS and specified fence action type
     */
    private FenceExecutor createFenceExecutor(FenceActionType actionType) {
        return new FenceExecutor(
                getVds(),
                actionType,
                getParameters().getFencingPolicy()
        );
    }

    /**
     * Returns {@code true}, if fencing execution was skipped due to fencing policy
     */
    protected boolean wasSkippedDueToPolicy(Object returnValue) {
        FenceStatusReturnValue fenceResult = null;
        if (returnValue instanceof FenceStatusReturnValue) {
            fenceResult = (FenceStatusReturnValue) returnValue;
        }
        return fenceResult != null && fenceResult.getIsSkipped();
    }

    protected void handleFencingSkippedDueToPolicy(VDSReturnValue vdsReturnValue) {
        skippedDueToFencingPolicy = true;
        setFenceSucceeded(false);
        vdsReturnValue.setSucceeded(false);
        setActionReturnValue(vdsReturnValue.getReturnValue());
        // when fencing is skipped we want to suppress command result logging, because
        // we fire an alert in VdsNotRespondingTreatment
        setCommandShouldBeLogged(false);
    }
}
