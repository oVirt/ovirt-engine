package org.ovirt.engine.core.bll.pm;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;

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
    private final FencingPolicy fencingPolicy;

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
        if (fencedHost.getFenceAgents() == null || fencedHost.getFenceAgents().isEmpty()) {
            return new FenceOperationResult(
                    Status.ERROR,
                    PowerStatus.UNKNOWN,
                    String.format(
                            "Invalid fence agents defined for host '%s'.",
                            fencedHost.getHostName()));
        }
        return fence(fenceAction, fencedHost.getFenceAgents());
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
            result = createFenceActionExecutor(iterator.next()).fence(fenceAction);
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
