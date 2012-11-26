package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.FenceActionType;
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
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.pm.VdsFencingOptions;

public class FencingExecutor {
    private final VDS _vds;
    private FenceActionType _action = FenceActionType.forValue(0);
    private Guid _vdsToRunId;
    private String _vdsToRunName;

    public FencingExecutor(VDS vds, FenceActionType actionType) {
        _vds = vds;
        _action = actionType;
    }

    public boolean FindVdsToFence() {
        final Guid NO_VDS = Guid.Empty;
        int count = 0;
        // make sure that loop is executed at least once , no matter what is the
        // value in config
        int retries = Math.max(Config.<Integer> GetValue(ConfigValues.FindFenceProxyRetries), 1);
        int delayInMs = 1000 * Config.<Integer> GetValue(ConfigValues.FindFenceProxyDelayBetweenRetriesInSec);
        _vdsToRunId = NO_VDS;
        VDS vdsToRun = null;
        List<VDS> hosts = DbFacade.getInstance().getVdsDao().getAll();
        // check if this is a new host, no need to retry , only status is
        // available on new host.
        if (_vds.getId().equals(NO_VDS)) {
            // try first to find a Host in UP status
            vdsToRun = LinqUtils.firstOrNull(hosts, new Predicate<VDS>() {
                @Override
                public boolean eval(VDS vds) {
                    return vds.getstatus() == VDSStatus.Up
                            && vds.getStoragePoolId().equals(_vds.getStoragePoolId());
                }
            });
            // trying other Hosts that are not in UP since they can be a proxy for fencing operations
            if (vdsToRun == null) {
                vdsToRun = LinqUtils.firstOrNull(hosts, new Predicate<VDS>() {
                    @Override
                    public boolean eval(VDS vds) {
                        return !isHostNetworkUnreacable(vds) &&
                                vds.getStoragePoolId().equals(_vds.getStoragePoolId());
                    }
                });
            }
            if (vdsToRun != null) {
                _vdsToRunId = vdsToRun.getId();
                _vdsToRunName = vdsToRun.getvds_name();
            }
        } else {
            // If can not find a proxy host retry and delay between retries
            // as configured.
            while (count < retries) {

                vdsToRun = LinqUtils.firstOrNull(hosts, new Predicate<VDS>() {
                    @Override
                    public boolean eval(VDS vds) {
                        return !vds.getId().equals(_vds.getId())
                                && vds.getStoragePoolId().equals(_vds.getStoragePoolId())
                                && vds.getstatus() == VDSStatus.Up;
                    }
                });
                if (vdsToRun == null) {
                    vdsToRun = LinqUtils.firstOrNull(hosts, new Predicate<VDS>() {
                        @Override
                        public boolean eval(VDS vds) {
                            return !isHostNetworkUnreacable(vds) &&
                                    !vds.getId().equals(_vds.getId()) && vds.getStoragePoolId().equals(_vds.getStoragePoolId());
                        }
                    });
                }
                if (vdsToRun != null) {
                    _vdsToRunId = vdsToRun.getId();
                    _vdsToRunName = vdsToRun.getvds_name();
                    break;
                }
                // do not retry getting proxy for Status operation.
                if (_action == FenceActionType.Status)
                    break;
                log.infoFormat("Atempt {0} to find fencing proxy host failed...", ++count);
                try {
                    Thread.sleep(delayInMs);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    break;
                }
            }
        }
        if (NO_VDS.equals(_vdsToRunId)) {
            log.errorFormat("Failed to run Power Management command on Host {0}, no running proxy Host was found.",
                    _vds.getvds_name());
        }
        return !NO_VDS.equals(_vdsToRunId);
    }

    public VDSReturnValue Fence() {
        VDSReturnValue retValue = null;
        try {
            // skip following code in case of testing a new host status
            if (_vds.getId() != null && !_vds.getId().equals(Guid.Empty)) {
                // get the host spm status again from the database in order to test it's current state.
                _vds.setspm_status((DbFacade.getInstance().getVdsDao().get(_vds.getId()).getspm_status()));
                // try to stop SPM if action is Restart or Stop and the vds is SPM
                if ((_action == FenceActionType.Restart || _action == FenceActionType.Stop)
                        && (_vds.getspm_status() != VdsSpmStatus.None)) {
                    Backend.getInstance()
                            .getResourceManager()
                            .RunVdsCommand(VDSCommandType.SpmStop,
                                    new SpmStopVDSCommandParameters(_vds.getId(), _vds.getStoragePoolId()));
                }
            }
            retValue = runFencingAction(_action);
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
        return runFencingAction(FenceActionType.Status).getSucceeded();
    }


    /**
     * Run the specified fencing action.
     * @param actionType The action to run.
     * @return The result of running the fencing command.
     */
    private VDSReturnValue runFencingAction(FenceActionType actionType) {
        String managementPort = "";
        if (_vds.getpm_port() != null && _vds.getpm_port() != 0) {
            managementPort = _vds.getpm_port().toString();
        }
        // get real agent and default parameters
        String agent = VdsFencingOptions.getRealAgent(_vds.getpm_type());
        String managementOptions = VdsFencingOptions.getDefaultAgentOptions(_vds.getpm_type(),_vds.getpm_options());
        log.infoFormat("Executing <{0}> Power Management command, Proxy Host:{1}, "
                + "Agent:{2}, Target Host:{3}, Management IP:{4}, User:{5}, Options:{6}", actionType, _vdsToRunName,
                agent, _vds.getvds_name(), _vds.getManagmentIp(), _vds.getpm_user(), managementOptions);
        return Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.FenceVds,
                        new FenceVdsVDSCommandParameters(_vdsToRunId, _vds.getId(), _vds.getManagmentIp(),
                                    managementPort, agent, _vds.getpm_user(), _vds.getpm_password(),
                                    managementOptions, actionType));
    }

    private boolean isHostNetworkUnreacable(VDS vds) {
        VdsDynamic vdsDynamic = vds.getDynamicData();
        return (vdsDynamic.getstatus() == VDSStatus.NonOperational &&
            vdsDynamic.getNonOperationalReason() == NonOperationalReason.NETWORK_UNREACHABLE);
    }

    private static Log log = LogFactory.getLog(FencingExecutor.class);
}
