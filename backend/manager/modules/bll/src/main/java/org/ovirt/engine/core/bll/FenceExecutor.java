package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgentOrder;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.vdscommands.FenceVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStopVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;

public class FenceExecutor {
    private final VDS _vds;
    private FenceActionType _action = FenceActionType.forValue(0);
    private Guid proxyHostId;
    private String proxyHostName;

    public FenceExecutor(VDS vds, FenceActionType actionType) {
        // TODO remove if block after UI patch that should set also cluster & proxy preferences in GetNewVdsFenceStatusParameters
        if (! vds.getId().equals(Guid.Empty)) {
            VDS dbVds =  DbFacade.getInstance().getVdsDao().get(vds.getId());
            if (vds.getVdsGroupId() == null) {
                vds.setVdsGroupId(dbVds.getVdsGroupId());
            }
            if (vds.getPmProxyPreferences() == null) {
                vds.setPmProxyPreferences(dbVds.getPmProxyPreferences());
            }
        }
        _vds = vds;
        _action = actionType;
    }

    public boolean findProxyHost() {
        PMProxyOptions proxyOption=null;
        final Guid NO_VDS = Guid.Empty;
        int count = 0;
        // make sure that loop is executed at least once , no matter what is the
        // value in config
        int retries = Math.max(Config.<Integer> GetValue(ConfigValues.FindFenceProxyRetries), 1);
        int delayInMs = 1000 * Config.<Integer> GetValue(ConfigValues.FindFenceProxyDelayBetweenRetriesInSec);
        proxyHostId = NO_VDS;
        VDS proxyHost = null;
        boolean proxyFound = false;
        // get PM Proxy preferences or use defaults if not defined
        String pmProxyPreferences = (_vds.getPmProxyPreferences() == null)
                ?
                Config.<String> GetValue(ConfigValues.FenceProxyDefaultPreferences)
                : _vds.getPmProxyPreferences();
        String[] pmProxyOPtions = pmProxyPreferences.split(",");
        for (String pmProxyOption : pmProxyOPtions) {
            if (pmProxyOption.equalsIgnoreCase(PMProxyOptions.CLUSTER.name())) {
                proxyOption = PMProxyOptions.CLUSTER;
            }
            else if (pmProxyOption.equalsIgnoreCase(PMProxyOptions.DC.name())) {
                proxyOption = PMProxyOptions.DC;
            }
            else {
                log.errorFormat("Illegal value in PM Proxy Preferences string {0}, skipped.", pmProxyOption);
                continue;
            }
            // check if this is a new host, no need to retry , only status is
            // available on new host.
            if (_vds.getId().equals(NO_VDS)) {
                // try first to find a Host in UP status
                proxyHost = getFenceProxy(true, false, proxyOption);
                // trying other Hosts that are not in UP since they can be a proxy for fence operations
                if (proxyHost == null) {
                    proxyHost = getFenceProxy(false, false, proxyOption);
                }
                if (proxyHost != null) {
                    proxyHostId = proxyHost.getId();
                    proxyHostName = proxyHost.getName();
                    proxyFound=true;
                }
            } else {
                // If can not find a proxy host retry and delay between retries
                // as configured.
                while (count < retries) {
                    proxyHost = getFenceProxy(true, true, proxyOption);
                    if (proxyHost == null) {
                        proxyHost = getFenceProxy(false, true, proxyOption);
                    }
                    if (proxyHost != null) {
                        proxyHostId = proxyHost.getId();
                        proxyHostName = proxyHost.getName();
                        proxyFound=true;
                        break;
                    }
                    // do not retry getting proxy for Status operation.
                    if (_action == FenceActionType.Status)
                        break;
                    log.infoFormat("Atempt {0} to find fence proxy host failed...", ++count);
                    try {
                        Thread.sleep(delayInMs);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        break;
                    }
                }
            }
            if (proxyFound) {
                break;
            }
        }
        if (NO_VDS.equals(proxyHostId)) {
            log.errorFormat("Failed to run Power Management command on Host {0}, no running proxy Host was found.",
                    _vds.getName());
        }
        else {
            logProxySelection(proxyHost.getName(), proxyOption.name(), _action.name());
        }
        return !NO_VDS.equals(proxyHostId);
    }

    private void logProxySelection(String proxy, String origin, String command) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("Proxy", proxy);
        logable.addCustomValue("Origin", origin);
        logable.addCustomValue("Command", command);
        logable.setVdsId(_vds.getId());
        AuditLogDirector.log(logable, AuditLogType.PROXY_HOST_SELECTION);
        log.infoFormat("Using Host {0} from {1} as proxy to execute {2} command on Host {3}",
                proxy, origin, command, _vds.getName());
    }

    public VDSReturnValue Fence() {
        return Fence(FenceAgentOrder.Primary);
    }

    public VDSReturnValue Fence(FenceAgentOrder order) {
        VDSReturnValue retValue = null;
        try {
            // skip following code in case of testing a new host status
            if (_vds.getId() != null && !_vds.getId().equals(Guid.Empty)) {
                // get the host spm status again from the database in order to test it's current state.
                _vds.setSpmStatus((DbFacade.getInstance().getVdsDao().get(_vds.getId()).getSpmStatus()));
                // try to stop SPM if action is Restart or Stop and the vds is SPM
                if ((_action == FenceActionType.Restart || _action == FenceActionType.Stop)
                        && (_vds.getSpmStatus() != VdsSpmStatus.None)) {
                    Backend.getInstance()
                            .getResourceManager()
                            .RunVdsCommand(VDSCommandType.SpmStop,
                                    new SpmStopVDSCommandParameters(_vds.getId(), _vds.getStoragePoolId()));
                }
            }
            retValue = runFenceAction(_action, order);
        } catch (VdcBLLException e) {
            retValue = new VDSReturnValue();
            retValue.setReturnValue(new FenceStatusReturnValue("unknown", e.getMessage()));
            retValue.setExceptionString(e.getMessage());
            retValue.setSucceeded(false);
        }
        return retValue;
    }

    /**
     * Check if the proxy can be used to fence the host successfully.
     * @return Whether the proxy host can be used to fence the host successfully.
     */
    public boolean checkProxyHostConnectionToHost() {
        return runFenceAction(FenceActionType.Status, FenceAgentOrder.Primary).getSucceeded();
    }


    /**
     * Run the specified fence action.
     * @param actionType The action to run.
     * @return The result of running the fence command.
     */
    private VDSReturnValue runFenceAction(FenceActionType actionType, FenceAgentOrder order) {
        String managementIp = getManagementIp(order);
        String managementPort = getManagementPort(order);
        String managementAgent = getManagementAgent(order);
        String managementUser = getManagementUser(order);
        String managementPassword = getManagementPassword(order);
        String managementOptions = getManagementOptions(order);

        log.infoFormat("Executing <{0}> Power Management command, Proxy Host:{1}, "
                + "Agent:{2}, Target Host:{3}, Management IP:{4}, User:{5}, Options:{6}", actionType, proxyHostName,
                managementAgent, _vds.getName(), managementIp, managementUser, managementOptions);
        return Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.FenceVds,
                        new FenceVdsVDSCommandParameters(proxyHostId, _vds.getId(), managementIp,
                                    managementPort, managementAgent, managementUser, managementPassword,
                                    managementOptions, actionType));
    }

    private String getManagementOptions(FenceAgentOrder order) {
        String managementOptions = "";
        if (order == FenceAgentOrder.Primary) {
            managementOptions = VdsFenceOptions.getDefaultAgentOptions(_vds.getPmType(), _vds.getPmOptions());
        }
        else if (order == FenceAgentOrder.Secondary) {
            managementOptions =
                    VdsFenceOptions.getDefaultAgentOptions(_vds.getPmSecondaryType(), _vds.getPmSecondaryOptions());
        }
        return managementOptions;
    }

    private String getManagementPassword(FenceAgentOrder order) {
        String managementPassword = "";
        if (order == FenceAgentOrder.Primary) {
            managementPassword = _vds.getPmPassword();
        }
        else if (order == FenceAgentOrder.Secondary) {
            managementPassword = _vds.getPmSecondaryPassword();
        }
        return managementPassword;
    }

    private String getManagementUser(FenceAgentOrder order) {
        String managementUser = "";
        if (order == FenceAgentOrder.Primary) {
            managementUser = _vds.getPmUser();
        }
        else if (order == FenceAgentOrder.Secondary) {
            managementUser = _vds.getPmSecondaryUser();
        }
        return managementUser;
    }

    private String getManagementAgent(FenceAgentOrder order) {
        String agent = "";
     // get real agent and default parameters
        if (order == FenceAgentOrder.Primary) {
            agent = VdsFenceOptions.getRealAgent(_vds.getPmType());
        }
        else if (order == FenceAgentOrder.Secondary) {
            agent = VdsFenceOptions.getRealAgent(_vds.getPmSecondaryType());
        }
        return agent;
    }

    private String getManagementPort(FenceAgentOrder order) {
        String managementPort = "";
        if (order == FenceAgentOrder.Primary) {
            if (_vds.getPmPort() != null && _vds.getPmPort() != 0) {
                managementPort = _vds.getPmPort().toString();
            }
        }
        else if (order == FenceAgentOrder.Secondary) {
            if (_vds.getPmSecondaryPort() != null && _vds.getPmSecondaryPort() != 0) {
                managementPort = _vds.getPmSecondaryPort().toString();
            }
        }
        return managementPort;
    }

    private String getManagementIp(FenceAgentOrder order) {
        String managementIp = "";
        if (order == FenceAgentOrder.Primary) {
            managementIp = _vds.getManagementIp();
        }
        else if (order == FenceAgentOrder.Secondary) {
            managementIp = _vds.getPmSecondaryIp();
        }
        return managementIp;
    }

    private boolean isHostNetworkUnreacable(VDS vds) {
        VdsDynamic vdsDynamic = vds.getDynamicData();
        return ((vdsDynamic.getstatus() == VDSStatus.Reboot) || (vdsDynamic.getstatus() == VDSStatus.NonOperational && vdsDynamic.getNonOperationalReason() == NonOperationalReason.NETWORK_UNREACHABLE));
    }

    private VDS getFenceProxy(final boolean onlyUpHost, final boolean filterSelf, final PMProxyOptions proxyOptions) {
        List<VDS> hosts = DbFacade.getInstance().getVdsDao().getAll();
        VDS proxyHost = LinqUtils.firstOrNull(hosts, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS vds) {
                if (proxyOptions == PMProxyOptions.CLUSTER) {
                    if (onlyUpHost) {
                        if (filterSelf) {
                            return !vds.getId().equals(_vds.getId())
                                    && vds.getVdsGroupId().equals(_vds.getVdsGroupId())
                                    && vds.getStatus() == VDSStatus.Up;
                        }
                        else {
                            return vds.getStatus() == VDSStatus.Up
                                    && vds.getVdsGroupId().equals(_vds.getVdsGroupId());
                        }
                    }
                    else {
                        if (filterSelf) {
                            return !isHostNetworkUnreacable(vds) &&
                                    !vds.getId().equals(_vds.getId()) && vds.getVdsGroupId().equals(_vds.getVdsGroupId());
                        }
                        else {
                            return !isHostNetworkUnreacable(vds) &&
                                    vds.getVdsGroupId().equals(_vds.getVdsGroupId());

                        }
                    }
                }
                else if (proxyOptions == PMProxyOptions.DC) {
                    if (onlyUpHost) {
                        if (filterSelf) {
                            return !vds.getId().equals(_vds.getId())
                                    && vds.getStoragePoolId().equals(_vds.getStoragePoolId())
                                    && vds.getStatus() == VDSStatus.Up;
                        }
                        else {
                            return vds.getStatus() == VDSStatus.Up
                                    && vds.getStoragePoolId().equals(_vds.getStoragePoolId());
                        }
                    }
                    else {
                        if (filterSelf) {
                            return !isHostNetworkUnreacable(vds) &&
                                    !vds.getId().equals(_vds.getId()) && vds.getStoragePoolId().equals(_vds.getStoragePoolId());
                        }
                        else {
                            return !isHostNetworkUnreacable(vds) &&
                                    vds.getStoragePoolId().equals(_vds.getStoragePoolId());

                        }
                    }
                }
                return false;
            }
        });
        return proxyHost;
    }
    private static Log log = LogFactory.getLog(FenceExecutor.class);
    private enum PMProxyOptions {CLUSTER,DC;};
}
