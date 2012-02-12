package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetDevicesVisibilityVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.Pair;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@SuppressWarnings("serial")
public class ConnectAllHostsToLunCommand<T extends ExtendSANStorageDomainParameters> extends
        StorageDomainCommandBase<T> {

    public ConnectAllHostsToLunCommand(T parameters) {
        super(parameters);
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

    protected VdcReturnValueBase CreateReturnValue() {
        return new ConnectAllHostsToLunCommandReturnValue();
    }


    @Override
    protected void executeCommand() {
        java.util.ArrayList<LUNs> processedLunsList = new java.util.ArrayList<LUNs>();
        boolean operationSucceeded = true;

        // VDS spmVds = null; // LINQ AllRunningVdssInPool.Where(vds =>
        // vds.spm_status == VdsSpmStatus.SPM).First();
        VDS spmVds = (VDS) LinqUtils.filter(getAllRunningVdssInPool(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS vds) {
                return vds.getspm_status() == VdsSpmStatus.SPM;
            }
        }).get(0);

        @SuppressWarnings("unchecked")
        List<LUNs> luns =
                    (List<LUNs>) Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.GetDeviceList,
                                    new GetDeviceListVDSCommandParameters(spmVds.getvds_id(),
                                            getStorageDomain().getstorage_type()))
                            .getReturnValue();
        Map<String, LUNs> lunsMap = new HashMap<String, LUNs>();
        for (LUNs lun : luns) {
            lunsMap.put(lun.getLUN_id(), lun);
        }
        for (String lunId : getParameters().getLunIds()) {
            LUNs lun = lunsMap.get(lunId);
            if (lun == null) {
                operationSucceeded = false;
                break;
            }

            lun.setvolume_group_id(getStorageDomain().getstorage());
            processedLunsList.add(lun);
        }
        if (operationSucceeded) {
            // connect all vds in pool (except spm) to lun and getDeviceList
            Pair<Boolean, Map<String, List<Guid>>> result = ConnectVdsToLun(processedLunsList);
            if (result.getFirst()) {
                getReturnValue().setActionReturnValue(processedLunsList);
                setCommandShouldBeLogged(false);
                setSucceeded(true);
            } else {
                // disconnect all hosts if connection is not in use by other luns
                Map<String, List<Guid>> processed = result.getSecond();
                for (String lunId : processed.keySet()) {
                    for (Guid vdsId : processed.get(lunId)) {
                        LUNs lun = lunsMap.get(lunId);
                        StorageHelperDirector.getInstance().getItem(getStoragePool().getstorage_pool_type())
                                .DisconnectStorageFromLunByVdsId(getStorageDomain(), vdsId, lun);
                    }
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
        // all vdss in pool except spm (already connected)
        Map<String, List<Guid>> resultMap = new HashMap<String, List<Guid>>();
        for (VDS vds : getAllRunningVdssInPool()) {
            if (vds.getspm_status() == VdsSpmStatus.SPM)
                continue;

            // try to connect vds to luns and getDeviceList in order to refresh them
            for (LUNs lun : luns) {
                if (!StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                        .ConnectStorageToLunByVdsId(getStorageDomain(), vds.getvds_id(), lun)) {
                    log.errorFormat("Could not connect host {0} to lun {1}", vds.getvds_name(), lun.getLUN_id());
                    setVds(vds);
                    ((ConnectAllHostsToLunCommandReturnValue)getReturnValue()).setFailedVds(vds);
                    ((ConnectAllHostsToLunCommandReturnValue)getReturnValue()).setFailedLun(lun);
                    return new Pair<Boolean, Map<String, List<Guid>>>(Boolean.FALSE, resultMap);
                } else {
                    List<Guid> hosts = resultMap.get(lun.getLUN_id());
                    if (hosts == null) {
                        hosts = new ArrayList<Guid>();
                        resultMap.put(lun.getLUN_id(), hosts);
                    }
                    hosts.add(vds.getvds_id());
                }
            }
            // Refresh all connected luns to host
            if (!Config.<Boolean> GetValue(ConfigValues.SupportGetDevicesVisibility,
                    vds.getvds_group_compatibility_version().getValue())) {
                Set<String> hostsLunsIds = new HashSet<String>();
                @SuppressWarnings("unchecked")
                List<LUNs> hostLuns = (List<LUNs>) Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.GetDeviceList,
                                new GetDeviceListVDSCommandParameters(vds.getvds_id(),
                                        getStorageDomain().getstorage_type())).getReturnValue();
                for (LUNs lun : hostLuns) {
                    hostsLunsIds.add(lun.getLUN_id());
                }
                for (LUNs lun : luns) {
                    if (!hostsLunsIds.contains(lun.getLUN_id())) {
                        return new Pair<Boolean, Map<String, List<Guid>>>(Boolean.FALSE, resultMap);
                    }
                }
            } else if (!validateConnectedLuns(vds, getParameters().getLunIds())) {
                return new Pair<Boolean, Map<String, List<Guid>>>(Boolean.FALSE, resultMap);
            }
        }
        return new Pair<Boolean, Map<String, List<Guid>>>(Boolean.TRUE, resultMap);
    }

    /**
     * The following method will check which luns were successfully connected to vds
     *
     * @param vds
     *            - the host
     * @param processedLunIds
     *            - luns ids which we wants to check
     * @return - true if all connections successes, false otherwise
     */
    private boolean validateConnectedLuns(VDS vds, List<String> processedLunIds) {
        @SuppressWarnings("unchecked")
        Map<String, Boolean> returnValue = (Map<String, Boolean>) Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.GetDevicesVisibility,
                        new GetDevicesVisibilityVDSCommandParameters(vds.getvds_id(),
                                processedLunIds.toArray(new String[processedLunIds.size()]))).getReturnValue();
        for (Map.Entry<String, Boolean> returnValueEntry : returnValue.entrySet()) {
            if (!Boolean.TRUE.equals(returnValueEntry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        // this should return only error, if command succeeded no logging is
        // required
        return AuditLogType.USER_CONNECT_HOSTS_TO_LUN_FAILED;
    }

    private static Log log = LogFactory.getLog(ConnectAllHostsToLunCommand.class);
}
