package org.ovirt.engine.core.bll.pm;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VdsArchitectureHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.FencingPolicyHelper;
import org.ovirt.engine.core.common.vdscommands.FenceVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsSpmIdMapDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It manages:
 * <ul>
 *     <li>Selection of fence proxy (it uses {@code FenceProxyLocator})</li>
 *     <li>Preparation of fence agent options (it uses {@code VdsFenceOptions})</li>
 *     <li>Execution of "plain" fence actions (start, stop and status)</li>
 *     <li>Execution retries for failed fence action with different (if available) or same fence proxy</li>
 * </ul>
 */
public class FenceAgentExecutor {
    private static final Logger log = LoggerFactory.getLogger(FenceAgentExecutor.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private VdsSpmIdMapDao vdsSpmIdMapDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private GlusterServerDao glusterServerDao;

    @Inject
    private VdsArchitectureHelper vdsArchitectureHelper;

    private final VDS fencedHost;
    private final FencingPolicy fencingPolicy;
    private FenceProxyLocator proxyLocator;
    private ArchitectureType architectureType;

    public FenceAgentExecutor(VDS fencedHost, FencingPolicy fencingPolicy) {
        this.fencedHost = fencedHost;
        this.fencingPolicy = fencingPolicy;
    }

    /**
     * Executes specified fence action using specified agent
     *
     * @param action
     *            specified fence action
     * @param agent
     *            specified fence agent
     * @return result of fence operation
     */
    public FenceOperationResult fence(FenceActionType action, FenceAgent agent) {
        FenceOperationResult result;

        VDS proxyHost = getProxyLocator().findProxyHost(isRetryEnabled(action));
        if (proxyHost == null) {
            return new FenceOperationResult(
                    Status.ERROR,
                    PowerStatus.UNKNOWN,
                    String.format(
                            "Failed to run %s on host '%s'. No other host was available to serve as proxy for the operation.",
                            getActionText(action),
                            fencedHost.getHostName()));
        }

        try {
            result = executeFenceAction(action, agent, proxyHost);
            if (result.getStatus() == Status.ERROR) {
                log.warn(
                        "Fence action failed using proxy host '{}', trying another proxy",
                        proxyHost.getHostName());
                VDS alternativeProxy = getProxyLocator().findProxyHost(
                        isRetryEnabled(action),
                        proxyHost.getId());
                if (alternativeProxy != null) {
                    result = executeFenceAction(action, agent, alternativeProxy);
                } else {
                    log.warn(
                            "Failed to find another proxy to re-run failed fence action, "
                                    + "retrying with the same proxy '{}'",
                            proxyHost.getHostName());
                    result = executeFenceAction(action, agent, proxyHost);
                }
            }
        } catch (EngineException e) {
            log.debug("Exception", e);
            result = new FenceOperationResult(
                    FenceOperationResult.Status.ERROR,
                    PowerStatus.UNKNOWN,
                    e.getMessage());
        }
        return result;
    }

    private Object getActionText(FenceActionType action) {
        switch (action) {
        case START:
            return "fence action: 'Start'";
        case STOP:
            return "fence action: 'Stop'";
        case STATUS:
            return "fence status-check";
        default:
            return ""; // won't happen
        }
    }

    protected FenceOperationResult executeFenceAction(FenceActionType action, FenceAgent agent, VDS proxyHost) {
        FenceAgent realAgent = createRealAgent(agent, proxyHost);
        auditFenceActionExecution(action, realAgent, proxyHost);

        VDSReturnValue retVal = resourceManager.runVdsCommand(
                VDSCommandType.FenceVds,
                new FenceVdsVDSCommandParameters(
                        proxyHost.getId(),
                        fencedHost.getId(),
                        realAgent,
                        action,
                        convertFencingPolicy(proxyHost)));

        FenceOperationResult result = (FenceOperationResult) retVal.getReturnValue();
        log.debug("Result of '{}' fence action: {}", result);
        if (result == null) {
            log.error(
                    "FenceVdsVDSCommand finished with null return value: succeeded={}, exceptionString='{}'",
                    retVal.getSucceeded(),
                    retVal.getExceptionString());
            log.debug("Exception", retVal.getExceptionObject());
            result = new FenceOperationResult(
                    Status.ERROR,
                    PowerStatus.UNKNOWN,
                    retVal.getExceptionString());
        }

        if (result.getStatus() == Status.ERROR) {
            auditFenceActionFailure(action, realAgent, proxyHost);
        }
        return result;
    }

    /**
     * Returns {@code true} if retrying of specified fence action is enabled, otherwise {@code false}
     */
    protected boolean isRetryEnabled(FenceActionType fenceAction) {
        return fenceAction != FenceActionType.STATUS;
    }

    /**
     * Creates instance of agent with values passed to real agent
     */
    protected FenceAgent createRealAgent(FenceAgent agent, VDS proxyHost) {
        FenceAgent realAgent = new FenceAgent(agent);
        realAgent.setOptions(getRealAgentOptions(agent, proxyHost));
        realAgent.setType(VdsFenceOptions.getRealAgent(agent.getType()));
        return realAgent;
    }

    /**
     * Merges agent specific options with default options for architecture and convert them to string
     */
    protected String getRealAgentOptions(FenceAgent agent, VDS proxyHost) {
        return new VdsFenceOptions(
                agent.getType(),
                VdsFenceOptions.getDefaultAgentOptions(
                        agent.getType(),
                        agent.getOptions() == null ? "" : agent.getOptions(),
                        getArchitectureType()),
                proxyHost.getClusterCompatibilityVersion().toString()).toInternalString();
    }

    /**
     * Converts fencing policy instance into a map which is passed to VDSM
     */
    protected Map<String, Object> convertFencingPolicy(VDS proxyHost) {
        Map<String, Object> map = null;
        if (fencingPolicy != null
                && FencingPolicyHelper.isFencingPolicySupported(proxyHost.getSupportedClusterVersionsSet())) {
            // fencing policy is entered and proxy supports passing fencing policy parameters
            map = new HashMap<>();
            if (fencingPolicy.isSkipFencingIfSDActive()) {
                // create map STORAGE_DOMAIN_GUID -> HOST_SPM_ID to pass to fence proxy
                map.put(VdsProperties.STORAGE_DOMAIN_HOST_ID_MAP, createStorageDomainHostIdMap());
            }
            if (fencedHost.getClusterSupportsGlusterService() && (fencingPolicy.isSkipFencingIfGlusterBricksUp()
                    || fencingPolicy.isSkipFencingIfGlusterQuorumNotMet())) {
                GlusterServer glusterServer = glusterServerDao.getByServerId(fencedHost.getId());
                if (glusterServer != null) {
                    map.put(VdsProperties.GLUSTER_SERVER_UUID, glusterServer.getGlusterServerUuid().toString());
                    if (fencingPolicy.isSkipFencingIfGlusterBricksUp()) {
                        map.put(VdsProperties.SKIP_FENCING_IF_GLUSTER_BRICKS_ARE_UP, true);
                    }
                    if (fencingPolicy.isSkipFencingIfGlusterQuorumNotMet()) {
                        map.put(VdsProperties.SKIP_FENCING_IF_GLUSTER_QUORUM_NOT_MET, true);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Creates a map of sanlock host ids per storage domain
     */
    protected Map<Guid, Integer> createStorageDomainHostIdMap() {
        if (fencingPolicy.isSkipFencingIfSDActive()) {
            VdsSpmIdMap hostIdRecord = vdsSpmIdMapDao.get(fencedHost.getId());
            return storageDomainDao.getAllForStoragePool(fencedHost.getStoragePoolId()).stream()
                    .filter(sd -> sd.getStorageStaticData().getStorageDomainType() == StorageDomainType.Master
                            || sd.getStorageStaticData().getStorageDomainType() == StorageDomainType.Data)
                    .collect(toMap(
                            sd -> sd.getId(),
                            // VDS_SPM_ID identifies the host in sanlock
                            sd -> hostIdRecord.getVdsSpmId()
                    ));
        }
        return null;
    }

    protected void auditFenceActionExecution(FenceActionType action, FenceAgent realAgent, VDS proxyHost) {
        log.debug("Executing fence action '{}', proxy='{}', target='{}', agent='{}', policy='{}'",
                action,
                proxyHost.getHostName(),
                fencedHost.getHostName(),
                realAgent,
                fencingPolicy);
        auditLogDirector.log(
                createAuditLogObject(action, realAgent, proxyHost),
                AuditLogType.FENCE_OPERATION_USING_AGENT_AND_PROXY_STARTED);
    }

    protected void auditFenceActionFailure(FenceActionType action, FenceAgent realAgent, VDS proxyHost) {
        auditLogDirector.log(
                createAuditLogObject(action, realAgent, proxyHost),
                AuditLogType.FENCE_OPERATION_USING_AGENT_AND_PROXY_FAILED);
    }
    private AuditLogable createAuditLogObject(FenceActionType action, FenceAgent agent, VDS proxyHost) {
        AuditLogable alb = new AuditLogableImpl();
        alb.addCustomValue("Action", action.name().toLowerCase());
        alb.addCustomValue("Host", fencedHost.getName() == null ? fencedHost.getId().toString() : fencedHost.getName());
        alb.addCustomValue("AgentType", agent.getType());
        alb.addCustomValue("AgentIp", agent.getIp());
        alb.addCustomValue("ProxyHost", proxyHost.getName());
        alb.setVdsId(fencedHost.getId());
        return alb;
    }

    protected FenceProxyLocator getProxyLocator() {
        if (proxyLocator == null) {
            proxyLocator = new FenceProxyLocator(
                    fencedHost,
                    fencingPolicy);
        }
        return proxyLocator;
    }

    protected ArchitectureType getArchitectureType() {
        if (architectureType == null) {
            architectureType = vdsArchitectureHelper.getArchitecture(fencedHost.getStaticData());
        }
        return architectureType;
    }
}
