package org.ovirt.engine.core.bll.pm;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.di.Injector;

/**
 * It manages:
 * <ul>
 *     <li>Iteration on specified fence agents using {@code PowerManagementHelper.AgentsIterator}</li>
 *     <li>Execution of fence action using all existing fence agents for the host</li>
 *     <li>Execution of fence action using only specified fence agents</li>
 *     <li>Usage of {@code SingleAgentFenceActionExecutor} and {@code ConcurrentAgentsFenceActionExecutor}
 *         to execute fence action for particular fence agent(s)</li>
 * </ul>
 */
public class HostFenceActionExecutor {
    /**
     * Host which the action is executed for
     */
    private final VDS fencedHost;

    /**
     * Fencing policy applied during action execution
     */
    private FencingPolicy fencingPolicy;

    public FencingPolicy getFencingPolicy() {
        return fencingPolicy;
    }

    public void setFencingPolicy(FencingPolicy fencingPolicy) {
        this.fencingPolicy = fencingPolicy;
    }

    public HostFenceActionExecutor(VDS fencedHost) {
        this(fencedHost, null);
    }

    public HostFenceActionExecutor(VDS fencedHost, FencingPolicy fencingPolicy) {
        this.fencedHost = fencedHost;
        this.fencingPolicy = fencingPolicy;
    }

    /**
     * Executes specified fence action on the {@code fencedHost} using fence agents defined for the host
     *
     * @param fenceAction
     *            specified fence action
     * @return result of the action
     */
    public FenceOperationResult fence(FenceActionType fenceAction) {
        List<FenceAgent> fenceAgents = Injector.get(FenceAgentDao.class).getFenceAgentsForHost(fencedHost.getId());
        if (fenceAgents == null || fenceAgents.isEmpty()) {
            return new FenceOperationResult(
                    Status.ERROR,
                    PowerStatus.UNKNOWN,
                    String.format(
                            "Invalid fence agents defined for host '%s'.",
                            fencedHost.getHostName()));
        }
        return fence(fenceAction, fenceAgents);
    }

    /**
     * Executes status fence action on fence agents defined for {@code fencedHost} to determine if it's powered off
     *
     * @return {@code true} if host is powered off, otherwise {@code false}
     */
    public boolean isHostPoweredOff() {
        FenceOperationResult result = fence(FenceActionType.STATUS);
        return result.getStatus() == FenceOperationResult.Status.SUCCESS
                && result.getPowerStatus() == PowerStatus.OFF;
    }

    /**
     * Executes status fence action on the {@code fencedHost} using only specified fence agent
     *
     * @param fenceAgent
     *            specified fence agent
     * @return result of the action
     */
    public FenceOperationResult getFenceAgentStatus(FenceAgent fenceAgent) {
        if (fenceAgent == null) {
            return new FenceOperationResult(
                    Status.ERROR,
                    PowerStatus.UNKNOWN,
                    "Invalid fence agent specified.");
        }
        return fence(FenceActionType.STATUS, Arrays.asList(fenceAgent));
    }

    /**
     * Executes specified fence action using specified fence agents
     */
    protected FenceOperationResult fence(FenceActionType fenceAction, List<FenceAgent> fenceAgents) {
        PowerManagementHelper.AgentsIterator iterator = createFenceAgentsIterator(fenceAgents);
        FenceOperationResult result = null;
        while (iterator.hasNext()) {
            List<FenceAgent> agents = iterator.next();
            if (fenceAction != FenceActionType.STATUS) {
                result = createFenceActionExecutor(agents).fence(FenceActionType.STATUS);
                // Skip this agent in ERROR only if there is another agent to try
                if (result.getStatus() == Status.ERROR && iterator.hasNext()) {
                    continue;
                }
                // Checks if Host is already in the requested status. If Host is Down and a Stop command is issued
                // or if Host is Up and a Start command is issued then do nothing.
                if (result.getStatus() == Status.SKIPPED_ALREADY_IN_STATUS
                        && result.getPowerStatus() == getRequestedPowerStatus(fenceAction)) {
                    alertActionSkippedAlreadyInStatus(fenceAction, getRequestedPowerStatus(fenceAction));
                    return result;
                }
                // Skip action if fencing is disabled in cluster policy
                if (result.getStatus() == Status.SKIPPED_DUE_TO_POLICY) {
                    alertActionSkippedFencingDisabledInPolicy();
                    return result;
                }
            }
            result = createFenceActionExecutor(agents).fence(fenceAction);
            if (result.getStatus() == Status.SUCCESS) {
                break;
            }
        }
        return result;

    }

    /**
     * Creates fence agents iterator
     */
    protected PowerManagementHelper.AgentsIterator createFenceAgentsIterator(List<FenceAgent> fenceAgents) {
        return PowerManagementHelper.getAgentsIterator(fenceAgents);
    }

    /**
     * gets requseted power status for the given fence action
     * @param fenceAction the fencing action
     * @return PowerStatus
     */

    private PowerStatus getRequestedPowerStatus(FenceActionType fenceAction) {
        return fenceAction == FenceActionType.START ? PowerStatus.ON : PowerStatus.OFF;
    }

    /**
     * Alerts when power management stop was skipped because host is already down.
     */
    protected void alertActionSkippedAlreadyInStatus(FenceActionType fenceActionType, PowerStatus powerStatus) {
        AuditLogable auditLogable = new AuditLogableImpl();
        auditLogable.addCustomValue("HostName", fencedHost.getName());
        auditLogable.addCustomValue("AgentStatus", powerStatus.name());
        auditLogable.addCustomValue("Operation", fenceActionType.getValue());
        Injector.get(AuditLogDirector.class).log(auditLogable, AuditLogType.VDS_ALREADY_IN_REQUESTED_STATUS);
    }

    /**
     * Alerts when power management stop was skipped because host is already down.
     */
    protected void alertActionSkippedFencingDisabledInPolicy() {
        AuditLogable auditLogable = new AuditLogableImpl();
        auditLogable.addCustomValue("VdsName", fencedHost.getName());
        Injector.get(AuditLogDirector.class).log(auditLogable, AuditLogType.VDS_ALERT_FENCE_DISABLED_BY_CLUSTER_POLICY);
    }

    /**
     * Creates instance of {@code FenceActionExecutor} according to specified fence agents
     */
    FenceActionExecutor createFenceActionExecutor(List<FenceAgent> fenceAgents) {
        if (fenceAgents.size() == 1) {
            return new SingleAgentFenceActionExecutor(fencedHost, fenceAgents.get(0), fencingPolicy);
        } else {
            return new ConcurrentAgentsFenceActionExecutor(fencedHost, fenceAgents, fencingPolicy);
        }
    }
}
