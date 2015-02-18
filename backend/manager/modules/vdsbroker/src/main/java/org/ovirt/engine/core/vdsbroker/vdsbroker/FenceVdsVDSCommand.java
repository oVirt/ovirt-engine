package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.common.utils.FencingPolicyHelper;
import org.ovirt.engine.core.common.vdscommands.FenceVdsVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;

public class FenceVdsVDSCommand<P extends FenceVdsVDSCommandParameters> extends VdsBrokerCommand<P> {
    private FenceStatusReturnForXmlRpc _result;

    /**
     * VDS which acts as fence proxy
     */
    private VDS proxyVds;

    /**
     * VDS which should be fenced
     */
    private VDS targetVds;

    public FenceVdsVDSCommand(P parameters) {
        super(parameters);
    }

    private VDS getProxyVds() {
        if (proxyVds == null) {
            proxyVds = DbFacade.getInstance().getVdsDao().get(getParameters().getVdsId());
        }
        return proxyVds;
    }

    private VDS getTargetVds() {
        if (targetVds == null) {
            targetVds = DbFacade.getInstance().getVdsDao().get(getParameters().getTargetVdsID());
        }
        return targetVds;
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     * @param reason
     *            The reason.
     */
    private void alert(AuditLogType logType, String reason) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getParameters().getTargetVdsID());
        alert.addCustomValue("Reason", reason);
        AlertDirector.Alert(alert, logType);
    }

    /**
     * Alerts if power management status failed.
     *
     * @param reason
     *            The reason.
     */
    protected void alertPowerManagementStatusFailed(String reason) {
        alert(AuditLogType.VDS_ALERT_FENCE_TEST_FAILED, reason);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        // We have to pass here the proxy host cluster compatibility version
        VDS vds = getProxyVds();
        VdsFenceOptions vdsFenceOptions = new VdsFenceOptions(getParameters().getType(),
                getParameters().getOptions(), vds.getVdsGroupCompatibilityVersion().toString());
        String options = vdsFenceOptions.ToInternalString();

        // ignore starting already started host or stopping already stopped host.
        if (getParameters().getAction() == FenceActionType.STATUS
                || !isAlreadyInRequestedStatus(options)) {
            _result =
                    getBroker().fenceNode(getParameters().getIp(),
                            getParameters().getPort() == null ? "" : getParameters().getPort(),
                    getParameters().getType(), getParameters().getUser(),
                    getParameters().getPassword(), getParameters().getAction().getValue(), "", options,
                    convertFencingPolicy()
            );

            getVDSReturnValue().setSucceeded(false);
            if (getParameters().getAction() == FenceActionType.STATUS && _result.power != null) {
                String stat = _result.power.toLowerCase();
                String msg = _result.mStatus.mMessage;
                if ("on".equals(stat) || "off".equals(stat)) {
                    getVDSReturnValue().setSucceeded(true);
                } else {
                    if (!getParameters().getTargetVdsID().equals(Guid.Empty)) {
                        alertPowerManagementStatusFailed(msg);
                    }

                }
                FenceStatusReturnValue fenceStatusReturnValue = new FenceStatusReturnValue(stat, msg);
                setReturnValue(fenceStatusReturnValue);
            } else {
                FenceStatusReturnValue fenceStatusReturnValue = new FenceStatusReturnValue(
                        _result.operationStatus,
                        _result.mStatus.mMessage != null ? _result.mStatus.mMessage : ""
                );
                setReturnValue(fenceStatusReturnValue);
                getVDSReturnValue().setSucceeded(_result.mStatus.mCode == 0);
            }
        } else {
            handleSkippedOperation();
        }
    }

    /**
     * Handles cases where fence operation was skipped (host is already in requested state)
     */
    private void handleSkippedOperation() {
        FenceStatusReturnValue fenceStatusReturnValue = new FenceStatusReturnValue(FenceStatusReturnValue.SKIPPED_DUE_TO_STATUS, "");
        AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.addCustomValue("HostName",
                (DbFacade.getInstance().getVdsDao().get(getParameters().getTargetVdsID())).getName());
        auditLogable.addCustomValue("AgentStatus", getParameters().getAction().getValue());
        auditLogable.addCustomValue("Operation", getParameters().getAction().toString());
        AuditLogDirector.log(auditLogable, AuditLogType.VDS_ALREADY_IN_REQUESTED_STATUS);
        getVDSReturnValue().setSucceeded(true);
        setReturnValue(fenceStatusReturnValue);
    }

    /**
     * Checks if Host is already in the requested status. If Host is Down and a Stop command is issued or if Host is Up
     * and a Start command is issued command should do nothing.
     *
     * @param options
     *            Fence options passed to the agent
     * @return
     */
    private boolean isAlreadyInRequestedStatus(String options) {
        boolean ret = false;
        FenceActionType action = getParameters().getAction();
        _result =
                getBroker().fenceNode(getParameters().getIp(),
                        getParameters().getPort() == null ? "" : getParameters().getPort(),
                getParameters().getType(), getParameters().getUser(),
                getParameters().getPassword(), "status", "", options,
                null
        );
        if (_result.power != null) {
            String powerStatus = _result.power.toLowerCase();
            if ((action == FenceActionType.START && powerStatus.equals("on")) ||
                    action == FenceActionType.STOP && powerStatus.equals("off"))
                ret = true;
        }
        return ret;
    }

    private Map<String, Object> convertFencingPolicy() {
        Map<String, Object> map = null;
        if (getParameters().getFencingPolicy() != null &&
                FencingPolicyHelper.isFencingPolicySupported(getProxyVds().getSupportedClusterVersionsSet())) {
            // fencing policy is entered and proxy supports passing fencing policy parameters
            map = new HashMap<>();
            if (getParameters().getFencingPolicy().isSkipFencingIfSDActive()) {
                // create map STORAGE_DOMAIN_GUID -> HOST_SPM_ID to pass to fence proxy
                map.put(VdsProperties.STORAGE_DOMAIN_HOST_ID_MAP, createStorageDomainHostIdMap());
            }
        }
        return map;
    }

    private Map<Guid, Integer> createStorageDomainHostIdMap() {
        Map<Guid, Integer> map = null;
        if (getParameters().getFencingPolicy().isSkipFencingIfSDActive()) {
            map = new HashMap<>();
            DbFacade dbf = DbFacade.getInstance();

            vds_spm_id_map hostIdRecord = dbf.getVdsSpmIdMapDao().get(
                    getTargetVds().getId()
            );

            // create a map SD_GUID -> HOST_ID
            for (StorageDomain sd : dbf.getStorageDomainDao().getAllForStoragePool(
                    getTargetVds().getStoragePoolId())
            ) {
                if (sd.getStorageStaticData().getStorageDomainType() == StorageDomainType.Master ||
                        sd.getStorageStaticData().getStorageDomainType() == StorageDomainType.Data) {
                    // VDS_SPM_ID identifies the host in sanlock
                    map.put(sd.getId(), hostIdRecord.getvds_spm_id());
                }
            }
        }
        return map;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return (_result.mStatus != null) ? _result.mStatus : new StatusForXmlRpc();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
