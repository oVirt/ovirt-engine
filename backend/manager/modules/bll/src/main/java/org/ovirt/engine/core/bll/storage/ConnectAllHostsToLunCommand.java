package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetDevicesVisibilityVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@NonTransactiveCommandAttribute
public class ConnectAllHostsToLunCommand<T extends ExtendSANStorageDomainParameters> extends
        StorageDomainCommandBase<T> {

    private static final Logger log = LoggerFactory.getLogger(ConnectAllHostsToLunCommand.class);

    public ConnectAllHostsToLunCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public ConnectAllHostsToLunCommand(T parameters) {
        this(parameters, null);
    }

    public static class ConnectAllHostsToLunCommandReturnValue extends VdcReturnValueBase {
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
    protected VdcReturnValueBase createReturnValue() {
        return new ConnectAllHostsToLunCommandReturnValue();
    }

    private ConnectAllHostsToLunCommandReturnValue getResult() {
        return (ConnectAllHostsToLunCommandReturnValue) getReturnValue();
    }

    @Override
    protected void executeCommand() {
        VDS spmVds = LinqUtils.first(LinqUtils.filter(getAllRunningVdssInPool(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS vds) {
                return vds.getSpmStatus() == VdsSpmStatus.SPM;
            }
        }));

        final List<LUNs> luns = getHostLuns(spmVds);
        final Map<String, LUNs> lunsMap = new HashMap<String, LUNs>();
        for (LUNs lun : luns) {
            lunsMap.put(lun.getLUN_id(), lun);
        }
        final List<LUNs> processedLunsList = new ArrayList<LUNs>();
        for (String lunId : getParameters().getLunIds()) {
            LUNs lun = lunsMap.get(lunId);
            if (lun == null) {
                //fail
                handleFailure(spmVds, getDbFacade().getLunDao().get(lunId));
                return;
            }

            lun.setvolume_group_id(getStorageDomain().getStorage());
            processedLunsList.add(lun);
        }
        // connect all vds in pool (except spm) to lun and getDeviceList
        Pair<Boolean, Map<String, List<Guid>>> result = ConnectVdsToLun(processedLunsList);
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
                    StorageHelperDirector.getInstance().getItem(getStorageDomain().getStorageType())
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
    private Pair<Boolean, Map<String, List<Guid>>> ConnectVdsToLun(List<LUNs> luns) {
        Map<String, List<Guid>> resultMap = new HashMap<String, List<Guid>>();
        for (VDS vds : getAllRunningVdssInPool()) {
            // try to connect vds to luns and getDeviceList in order to refresh them
            for (LUNs lun : luns) {
                if (!connectStorageToLunByVdsId(vds, lun)) {
                    log.error("Could not connect host '{}' to lun '{}'", vds.getName(), lun.getLUN_id());
                    setVds(vds);
                    handleFailure(vds, lun);
                    return new Pair<Boolean, Map<String, List<Guid>>>(Boolean.FALSE, resultMap);
                } else {
                    List<Guid> hosts = resultMap.get(lun.getLUN_id());
                    if (hosts == null) {
                        hosts = new ArrayList<Guid>();
                        resultMap.put(lun.getLUN_id(), hosts);
                    }
                    hosts.add(vds.getId());
                }
            }
            // Refresh all connected luns to host
            if (!validateConnectedLuns(vds, getParameters().getLunIds())) {
                return new Pair<Boolean, Map<String, List<Guid>>>(Boolean.FALSE, resultMap);
            }
        }
        return new Pair<Boolean, Map<String, List<Guid>>>(Boolean.TRUE, resultMap);
    }

    private boolean connectStorageToLunByVdsId(VDS vds, LUNs lun) {
        try {
            return StorageHelperDirector.getInstance()
                    .getItem(getStorageDomain().getStorageType())
                    .connectStorageToLunByVdsId(getStorageDomain(), vds.getId(), lun, Guid.Empty);
        } catch (VdcBLLException e) {
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
        } catch (VdcBLLException e) {
            getResult().setFailedVds(vds);
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
     * @throws VdcBLLException
     */
    private boolean validateConnectedLuns(VDS vds, List<String> processedLunIds) {
        Map<String, Boolean> res;

        try {
            res = (Map<String, Boolean>) runVdsCommand(VDSCommandType.GetDevicesVisibility,
                    new GetDevicesVisibilityVDSCommandParameters(vds.getId(),
                            processedLunIds.toArray(new String[processedLunIds.size()]))).getReturnValue();
        } catch(VdcBLLException e) {
            handleFailure(vds, null);
            throw e;
        }

        for (Map.Entry<String, Boolean> deviceVisibility : res.entrySet()) {
            if (!Boolean.TRUE.equals(deviceVisibility.getValue())) {
                handleFailure(vds, getDbFacade().getLunDao().get(deviceVisibility.getKey()));
                return false;
            }
        }

        return true;
    }

    private void handleFailure(VDS vds, LUNs lun) {
        ConnectAllHostsToLunCommandReturnValue result = getResult();
        result.setFailedVds(vds);
        result.setFailedLun(lun);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        // this should return only error, if command succeeded no logging is
        // required
        setVds(getResult().getFailedVds()); // For audit logging purposes in case of an error
        return AuditLogType.USER_CONNECT_HOSTS_TO_LUN_FAILED;
    }
}
