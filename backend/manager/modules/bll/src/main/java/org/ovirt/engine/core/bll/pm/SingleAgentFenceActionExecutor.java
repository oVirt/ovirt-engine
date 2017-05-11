package org.ovirt.engine.core.bll.pm;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It manages:
 * <ul>
 *     <li>Execution of "complex" fence actions (start, stop) where we wait until host reaches requested status
 *         using status action</li>
 *     <li>Execution retries for failed "complex" fence action (start, stop)</li>
 *     <li>Execution retries for failed "wait until host status is reached" actions</li>
 *     <li>Usage of {@code FenceAgentExecutor} to execute "simple" fence actions</li>
 * </ul>
 */
public class SingleAgentFenceActionExecutor implements FenceActionExecutor{
    private static final Logger log = LoggerFactory.getLogger(SingleAgentFenceActionExecutor.class);

    private AuditLogDirector auditLogDirector;

    /**
     * Number of ms to wait after host was fenced to fetch host power status
     */
    private static final int SLEEP_BEFORE_FIRST_ATTEMPT = 5000;

    /**
     * Number of allowed {@code PowerStatus.UNKNOWN} status results to determine if fence host operation was
     * successful
     */
    private static final int UNKNOWN_RESULT_LIMIT = 3;

    private final VDS fencedHost;
    private final FenceAgent fenceAgent;
    private final FencingPolicy fencingPolicy;
    private int allowedFenceActionRetries;
    private PowerStatus requestedPowerStatus;
    private int allowedWaitForStatusRetries;
    private long delayBetweenRetries;

    public SingleAgentFenceActionExecutor(VDS fencedHost, FenceAgent fenceAgent, FencingPolicy fencingPolicy) {
        this.fencedHost = fencedHost;
        this.fenceAgent = fenceAgent;
        this.fencingPolicy = fencingPolicy;
    }

    @Override
    public FenceOperationResult fence(FenceActionType fenceAction) {
        setupParams(fenceAction);

        if (fenceAction == FenceActionType.STATUS) {
            return getStatus();
        } else {
            return changeStatus(fenceAction);
        }
    }

    /**
     * Setup parameters for specified fence action
     */
    protected void setupParams(FenceActionType fenceAction) {
        switch (fenceAction) {
            case START:
                requestedPowerStatus = PowerStatus.ON;
                allowedFenceActionRetries = 1;
                allowedWaitForStatusRetries = Config.<Integer>getValue(ConfigValues.FenceStartStatusRetries);
                delayBetweenRetries = TimeUnit.SECONDS.toMillis(
                        Config.<Integer>getValue(ConfigValues.FenceStartStatusDelayBetweenRetriesInSec));
                break;

            case STOP:
                requestedPowerStatus = PowerStatus.OFF;
                allowedFenceActionRetries = 0;
                allowedWaitForStatusRetries = Config.<Integer>getValue(ConfigValues.FenceStopStatusRetries);
                delayBetweenRetries = TimeUnit.SECONDS.toMillis(
                        Config.<Integer>getValue(ConfigValues.FenceStopStatusDelayBetweenRetriesInSec));
                break;

            case STATUS:
                break;
        }
    }

    /**
     * Returns new instance of {@link FenceAgentExecutor}
     */
    protected FenceAgentExecutor createAgentExecutor() {
        return Injector.injectMembers(new FenceAgentExecutor(fencedHost, fencingPolicy));
    }

    /**
     * Fetches power status of the host using specified agent
     */
    protected FenceOperationResult getStatus() {
        return createAgentExecutor().fence(FenceActionType.STATUS, fenceAgent);
    }

    /**
     * Executes start or stop fence operation using specified agent
     */
    protected FenceOperationResult changeStatus(FenceActionType fenceAction) {
        FenceAgentExecutor agentExecutor = createAgentExecutor();
        FenceOperationResult statusResult = null;
        // start at -1 because 1st fence attempt is regular and not a retry
        int fenceRetries = -1;

        do {
            FenceOperationResult result = agentExecutor.fence(fenceAction, fenceAgent);

            if (result.getStatus() == Status.SKIPPED_ALREADY_IN_STATUS) {
                // action skipped already in status, so report it as success with correct power status
                return new FenceOperationResult(
                        Status.SUCCESS,
                        fenceAction == FenceActionType.START ? PowerStatus.ON : PowerStatus.OFF);
            } else if (result.getStatus() == Status.SKIPPED_DUE_TO_POLICY) {
                // skipped due to policy is handled in caller
                return result;
            }

            if (result.getStatus() == Status.SUCCESS) {
                // fence operation was successful, verify if host power status changed
                statusResult = waitForStatus(fenceAction);
                if (isRequestedStatusAchieved(statusResult)) {
                    // requested host power status reached, end with success
                    return statusResult;
                }
            }
            fenceRetries++;
        } while (fenceRetries < allowedFenceActionRetries);

        return new FenceOperationResult(
                Status.ERROR,
                // fail safe, at least one fence attempt should always be executed, so statusResult shouldn't be null
                statusResult == null
                        ? PowerStatus.UNKNOWN
                        : statusResult.getPowerStatus(),
                "Allowed retries to verify host power status exceeded");
    }

    /**
     * Executes status operation until requested host power status is reached or allowed number of retries exceeded
     * to determine of start/stop fence operation was successful
     */
    protected FenceOperationResult waitForStatus(FenceActionType fenceAction) {
        FenceOperationResult statusResult = null;
        // start at -1, because the 1st iteration is regular and not a retry
        int statusRetries = -1;
        int unknownStatusReceived = 0;

        log.info(
                "Waiting for host '{}' to reach status '{}'",
                fencedHost.getHostName(),
                requestedPowerStatus);

        // Waiting before first attempt to check the host status.
        // This is done because if we will attempt to get host status immediately
        // in most cases it will not turn from on/off to off/on and we will need
        // to wait a full cycle for it.
        ThreadUtils.sleep(getSleepBeforeFirstAttempt());

        while (statusRetries < allowedWaitForStatusRetries) {
            log.info("Attempt {} to get host '{}' status", statusRetries + 2, fencedHost.getHostName());
            statusResult = getStatus();
            if (statusResult.getStatus() == Status.SUCCESS) {
                if (statusResult.getPowerStatus() == PowerStatus.UNKNOWN) {
                    if (unknownStatusReceived < getUnknownResultLimit()
                            && statusRetries < allowedWaitForStatusRetries) {
                        // unknown power status received, wait a while and retry
                        ThreadUtils.sleep(delayBetweenRetries);
                        statusRetries++;
                        unknownStatusReceived++;
                    } else {
                        // No need to retry, agent definitions are corrupted
                        log.error(
                                "Host '{}' PM Agent definitions are corrupted, aborting fence operation.",
                                fencedHost.getHostName());
                        return new FenceOperationResult(
                                Status.ERROR,
                                PowerStatus.UNKNOWN,
                                statusResult.getMessage());
                    }
                } else if (statusResult.getPowerStatus() == requestedPowerStatus) {
                    log.info("Host '{}' status is '{}'", fencedHost.getHostName(), requestedPowerStatus);
                    return new FenceOperationResult(
                            Status.SUCCESS,
                            requestedPowerStatus);
                } else {
                    // host is still not in requested power status
                    statusRetries++;
                    if (statusRetries < allowedWaitForStatusRetries) {
                        ThreadUtils.sleep(delayBetweenRetries);
                    }
                }
            } else {
                log.error("Failed to get host '{}' status.", fencedHost.getHostName());
                return statusResult;
            }
        }

        auditVerifyStatusRetryLimitExceeded(fenceAction);
        return new FenceOperationResult(
                Status.ERROR,
                statusResult == null ? PowerStatus.UNKNOWN : statusResult.getPowerStatus(),
                statusResult == null ? "" : statusResult.getMessage());
    }

    protected boolean isRequestedStatusAchieved(FenceOperationResult result) {
        return result.getStatus() == Status.SUCCESS
                && result.getPowerStatus() == requestedPowerStatus;
    }

    protected int getSleepBeforeFirstAttempt() {
        return SLEEP_BEFORE_FIRST_ATTEMPT;
    }

    protected int getUnknownResultLimit() {
        return UNKNOWN_RESULT_LIMIT;
    }

    protected void auditVerifyStatusRetryLimitExceeded(FenceActionType fenceAction) {
        AuditLogable auditLogable = new AuditLogableImpl();
        auditLogable.addCustomValue("Host", fencedHost.getName());
        auditLogable.addCustomValue("Status", fenceAction.name().toLowerCase());
        auditLogable.setVdsId(fencedHost.getId());
        auditLogable.setVdsName(fencedHost.getName());
        getAuditLogDirector().log(auditLogable, AuditLogType.VDS_ALERT_FENCE_STATUS_VERIFICATION_FAILED);
        log.error(
                "Failed to verify host '{}' status after {} action: have retried {} times with delay of {} seconds"
                        + " between each retry.",
                fencedHost.getHostName(),
                fenceAction.name(),
                allowedWaitForStatusRetries,
                delayBetweenRetries);

    }

    // TODO Investigate if injection is possible
    protected AuditLogDirector getAuditLogDirector() {
        if (auditLogDirector == null) {
            auditLogDirector = Injector.get(AuditLogDirector.class);
        }
        return auditLogDirector;
    }
}
