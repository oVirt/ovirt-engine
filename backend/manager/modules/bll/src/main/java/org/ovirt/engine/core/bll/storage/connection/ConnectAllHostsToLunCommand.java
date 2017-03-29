package org.ovirt.engine.core.bll.storage.connection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetDevicesVisibilityVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@NonTransactiveCommandAttribute
public class ConnectAllHostsToLunCommand<T extends ExtendSANStorageDomainParameters> extends
        StorageDomainCommandBase<T> {

    private static final Logger log = LoggerFactory.getLogger(ConnectAllHostsToLunCommand.class);

    @Inject
    private LunDao lunDao;

    public ConnectAllHostsToLunCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public static class ConnectAllHostsToLunResult implements Serializable {
        private VDS failedVds;
        private LUNs failedLun;

        public VDS getFailedVds() {
            return failedVds;
        }

        public void setFailedVds(VDS failedVds) {
            this.failedVds = failedVds;
        }

        public LUNs getFailedLun() {
            return failedLun;
        }

        public void setFailedLun(LUNs failedLun) {
            this.failedLun = failedLun;
        }
    }

    @Override
    protected void executeCommand() {
        VDS spmVds = getAllRunningVdssInPool().stream()
                .filter(vds -> vds.getSpmStatus() == VdsSpmStatus.SPM).findFirst().orElse(null);

        final List<LUNs> luns = getHostLuns(spmVds);
        final Map<String, LUNs> lunsMap = new HashMap<>();
        for (LUNs lun : luns) {
            lunsMap.put(lun.getLUNId(), lun);
        }
        final List<LUNs> processedLunsList = new ArrayList<>();
        for (String lunId : getParameters().getLunIds()) {
            LUNs lun = lunsMap.get(lunId);
            if (lun == null) {
                //fail
                handleFailure(spmVds, lunDao.get(lunId));
                return;
            }

            lun.setVolumeGroupId(getStorageDomain().getStorage());
            processedLunsList.add(lun);
        }
        // connect all vds in pool (except spm) to lun and getDeviceList
        Pair<Boolean, Map<String, List<Guid>>> result = connectVdsToLun(processedLunsList);
        if (result.getFirst()) {
            getReturnValue().setActionReturnValue(processedLunsList);
            setCommandShouldBeLogged(false);
            setSucceeded(true);
        } else {
            // disconnect all hosts if connection is not in use by other luns
            Map<String, List<Guid>> processed = result.getSecond();
            for (Map.Entry<String, List<Guid>> entry : processed.entrySet()) {
                for (Guid vdsId : entry.getValue()) {
                    LUNs lun = lunsMap.get(entry.getKey());
                    storageHelperDirector.getItem(getStorageDomain().getStorageType())
                            .disconnectStorageFromLunByVdsId(getStorageDomain(), vdsId, lun);
                }
            }
        }
    }

    /**
     * The following method will connect all provided lund to all running host in pool
     *
     * @param luns
     *            - the luns which should be connected
     * @return the map where the key is true/false value which means if connection successes/not successes and value is
     *         map of luns Ids -> connected hosts
     */
    private Pair<Boolean, Map<String, List<Guid>>> connectVdsToLun(List<LUNs> luns) {
        Map<String, List<Guid>> resultMap = new HashMap<>();
        for (VDS vds : getAllRunningVdssInPool()) {
            // try to connect vds to luns and getDeviceList in order to refresh them
            for (LUNs lun : luns) {
                if (!connectStorageToLunByVdsId(vds, lun)) {
                    log.error("Could not connect host '{}' to lun '{}'", vds.getName(), lun.getLUNId());
                    setVds(vds);
                    handleFailure(vds, lun);
                    return new Pair<>(Boolean.FALSE, resultMap);
                } else {
                    List<Guid> hosts = resultMap.get(lun.getLUNId());
                    if (hosts == null) {
                        hosts = new ArrayList<>();
                        resultMap.put(lun.getLUNId(), hosts);
                    }
                    hosts.add(vds.getId());
                }
            }
            // Refresh all connected luns to host
            if (!validateConnectedLuns(vds, getParameters().getLunIds())) {
                return new Pair<>(Boolean.FALSE, resultMap);
            }
        }
        return new Pair<>(Boolean.TRUE, resultMap);
    }

    private boolean connectStorageToLunByVdsId(VDS vds, LUNs lun) {
        try {
            return storageHelperDirector.getItem(getStorageDomain().getStorageType())
                    .connectStorageToLunByVdsId(getStorageDomain(), vds.getId(), lun, Guid.Empty);
        } catch (EngineException e) {
            handleFailure(vds, lun);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private List<LUNs> getHostLuns(VDS vds) {
        try {
            return (List<LUNs>) runVdsCommand(
                    VDSCommandType.GetDeviceList,
                    new GetDeviceListVDSCommandParameters(vds.getId(),
                            getStorageDomain().getStorageType())).getReturnValue();
        } catch (EngineException e) {
            handleFailure(vds);
            throw e;
        }
    }

    /**
     * Verify that all luns are connected to the host.
     *
     * @param vds
     *            - the host
     * @param processedLunIds
     *            - luns ids which we wants to check
     * @return - true if all luns are connected to the host, false otherwise
     *
     * @throws EngineException If the underlying call to VDSM fails
     */
    private boolean validateConnectedLuns(VDS vds, Set<String> processedLunIds) {
        Map<String, Boolean> res;

        try {
            res = (Map<String, Boolean>) runVdsCommand(VDSCommandType.GetDevicesVisibility,
                    new GetDevicesVisibilityVDSCommandParameters(vds.getId(),
                            processedLunIds.toArray(new String[processedLunIds.size()]))).getReturnValue();
        } catch(EngineException e) {
            handleFailure(vds, null);
            throw e;
        }

        for (Map.Entry<String, Boolean> deviceVisibility : res.entrySet()) {
            if (!Boolean.TRUE.equals(deviceVisibility.getValue())) {
                handleFailure(vds, lunDao.get(deviceVisibility.getKey()));
                return false;
            }
        }

        return true;
    }

    private void handleFailure(VDS vds) {
        ConnectAllHostsToLunResult result = new ConnectAllHostsToLunResult();
        result.setFailedVds(vds);
        setActionReturnValue(result);
    }

    private void handleFailure(VDS vds, LUNs lun) {
        ConnectAllHostsToLunResult result = new ConnectAllHostsToLunResult();
        result.setFailedVds(vds);
        result.setFailedLun(lun);
        setActionReturnValue(result);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        // this should return only error, if command succeeded no logging is required
        ConnectAllHostsToLunResult result = getActionReturnValue();

        // For audit logging purposes in case of an error
        setVds(result.getFailedVds());
        return AuditLogType.USER_CONNECT_HOSTS_TO_LUN_FAILED;
    }
}
