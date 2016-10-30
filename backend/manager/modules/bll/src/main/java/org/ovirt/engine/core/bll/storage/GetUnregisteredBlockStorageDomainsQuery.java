package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.EqualPredicate;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredBlockStorageDomainsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetUnregisteredBlockStorageDomainsQuery<P extends GetUnregisteredBlockStorageDomainsParameters> extends QueriesCommandBase<P> {
    public GetUnregisteredBlockStorageDomainsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<StorageServerConnections> connectedTargets = null;
        List<StorageDomain> storageDomains;

        try {
            // iSCSI protocol requires targets connection (as opposed to FCP)
            if (getParameters().getStorageType() == StorageType.ISCSI) {
                connectedTargets = connectTargets();
            }

            // Fetch LUNs from GetDeviceList and filter-out irrelevant ones
            List<LUNs> lunsFromDeviceList = getDeviceList();
            List<LUNs> filteredLUNs = filterIrrelevantLUNs(lunsFromDeviceList, connectedTargets);

            // Retrieve storage domains by VG-IDs extracted from the LUNs
            List<String> vgIDs = getVolumeGroupIdsByLUNs(filteredLUNs);
            storageDomains = getStorageDomainsByVolumeGroupIds(vgIDs);
        } catch (RuntimeException e) {
            log.error("Failed to retrieve storage domains by connections info: {}", e.getMessage());
            log.debug("Exception", e);
            getQueryReturnValue().setExceptionString(e.getMessage());
            getQueryReturnValue().setSucceeded(false);
            return;
        }

        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValue = new Pair<>(storageDomains, connectedTargets);
        getQueryReturnValue().setReturnValue(returnValue);
    }

    /**
     * Connect to the targets (StorageServerConnections) specified in the parameters.
     *
     * @return A list of targets that's been successfully connected.
     */
    protected List<StorageServerConnections> connectTargets() {
        List<StorageServerConnections> connectedTargets = new ArrayList<>();

        for (StorageServerConnections storageConnection : getParameters().getStorageServerConnections()) {
            VdcReturnValueBase returnValue = executeConnectStorageToVds(
                        new StorageServerConnectionParametersBase(storageConnection, getParameters().getVdsId(), false));

            if (returnValue.getSucceeded()) {
                connectedTargets.add(storageConnection);
            }
            else {
                log.error("Could not connect to target IQN '{}': {}",
                        storageConnection.getiqn(), returnValue.getFault().getMessage());
            }
        }

        if (connectedTargets.isEmpty()) {
            throw new RuntimeException("Couldn't connect to the specified targets");
        }

        return connectedTargets;
    }

    /**
     * Get devices (LUNs) that are visible by the host.
     *
     * @return the list of LUNs.
     */
    protected List<LUNs> getDeviceList() {
        List<LUNs> luns = new ArrayList<>();
        VdcQueryReturnValue returnValue =
                executeGetDeviceList(
                new GetDeviceListQueryParameters(getParameters().getVdsId(),
                        getParameters().getStorageType(),
                        false, null));

        if (returnValue.getSucceeded()) {
            luns.addAll(returnValue.<List<LUNs>> getReturnValue());
        } else {
            throw new RuntimeException(String.format("GetDeviceList execution failed. Exception message: %1$s",
                    returnValue.getExceptionString()));
        }

        return luns;
    }

    /**
     * Filter out LUNs that aren't part of the specified targets.
     *
     * @param luns the LUNs list
     * @param targets the targets list
     *
     * @return the filtered LUNs list
     */
    protected List<LUNs> filterLUNsByTargets(List<LUNs> luns, final List<StorageServerConnections> targets) {
        // Targets should be null only when using StorageType.FCP
        if (targets == null) {
            return luns;
        }

        // For iSCSI domains, filter LUNs by the specified targets
        final Set<String> targetIQNs = Entities.connectionsByIQN(targets).keySet();
        return LinqUtils.filter(luns, new Predicate<LUNs>() {
            @Override
            public boolean eval(LUNs lun) {
                for (StorageServerConnections connection : lun.getLunConnections()) {
                    if (CollectionUtils.exists(targetIQNs, new EqualPredicate(connection.getiqn()))) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Filter out LUNs that are already a part of storage domains exist in the system.
     *
     * @param luns the LUNs list
     *
     * @return the filtered LUNs list
     */
    protected List<LUNs> filterLUNsThatBelongToExistingStorageDomains(List<LUNs> luns) {
        List<StorageDomain> existingStorageDomains = getStorageDomainDao().getAll();
        final List<Guid> existingStorageDomainIDs = Entities.getIds(existingStorageDomains);

        return LinqUtils.filter(luns, new Predicate<LUNs>() {
            @Override
            public boolean eval(LUNs lun) {
                return !existingStorageDomainIDs.contains(lun.getStorageDomainId());
            }
        });
    }

    /**
     * Filter out irrelevant LUNs using filtering methods:
     * 'filterLUNsByTargets' and 'filterLUNsThatBelongToExistingStorageDomains'.
     *
     * @param luns the LUNs list
     * @param connectedTargets the targets list
     * @return  the filtered LUNs list
     */
    protected List<LUNs> filterIrrelevantLUNs(List<LUNs> luns, List<StorageServerConnections> connectedTargets) {
        List<LUNs> filteredLUNs = filterLUNsByTargets(luns, connectedTargets);
        filteredLUNs = filterLUNsThatBelongToExistingStorageDomains(filteredLUNs);

        return filteredLUNs;
    }

    /**
     * Retrieve the volume group ID associated with each LUN in the specified list.
     *
     * @param luns the LUNs list
     *
     * @return volume group IDs
     */
    protected List<String> getVolumeGroupIdsByLUNs(List<LUNs> luns) {
        Set<String> vgSet = new HashSet<>();
        for (LUNs lun : luns) {
            if (!lun.getvolume_group_id().isEmpty()) {
                vgSet.add(lun.getvolume_group_id());
            }
        }
        return new ArrayList<>(vgSet);
    }

    /**
     * Create StorageDomain objects according to the specified VG-IDs list.
     *
     * @param vgIDs the VG-IDs list
     *
     * @return storage domains list
     */
    @SuppressWarnings("unchecked")
    protected List<StorageDomain> getStorageDomainsByVolumeGroupIds(List<String> vgIDs) {
        List<StorageDomain> storageDomains = new ArrayList<>();

        // Get existing PhysicalVolumes.
        List<String> existingLunIds = Entities.getIds(getLunDao().getAll());

        for (String vgID : vgIDs) {
            VDSReturnValue returnValue = null;
            try {
                returnValue = executeGetVGInfo(
                        new GetVGInfoVDSCommandParameters(getParameters().getVdsId(), vgID));
            } catch (RuntimeException e) {
                log.error("Could not get info for VG ID: '{}': {}",
                        vgID, e.getMessage());
                log.debug("Exception", e);
                continue;
            }

            ArrayList<LUNs> luns = (ArrayList<LUNs>) returnValue.getReturnValue();
            List<String> lunIdsOnStorage = Entities.getIds(luns);
            if (CollectionUtils.containsAny(lunIdsOnStorage, existingLunIds)) {
                log.info("There are existing luns in the system which are part of VG id '{}'", vgID);
                continue;
            }

            // Get storage domain ID by a representative LUN
            LUNs lun = luns.get(0);
            Guid storageDomainId = lun.getStorageDomainId();

            // Get storage domain using GetStorageDomainInfo
            StorageDomain storageDomain = getStorageDomainById(storageDomainId);
            if (storageDomain != null) {
                storageDomains.add(storageDomain);
            }
        }
        return storageDomains;
    }

    /**
     * Retrieve a storage domain using a specified storage domain ID.
     *
     * @param storageDomainId the domain's ID
     * @return the storage domain
     */
    @SuppressWarnings("unchecked")
    protected StorageDomain getStorageDomainById(Guid storageDomainId) {
        VDSReturnValue returnValue = null;

        try {
            returnValue = executeHSMGetStorageDomainInfo(
                    new HSMGetStorageDomainInfoVDSCommandParameters(getParameters().getVdsId(), storageDomainId));
        } catch (RuntimeException e) {
            log.error("Could not get info for storage domain ID: '{}': {}",
                    storageDomainId, e.getMessage());
            log.debug("Exception", e);
            return null;
        }

        Pair<StorageDomainStatic, SANState> result = (Pair<StorageDomainStatic, SANState>) returnValue.getReturnValue();
        StorageDomainStatic storageDomainStatic = result.getFirst();
        storageDomainStatic.setStorageType(getParameters().getStorageType());

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setStorageStaticData(storageDomainStatic);

        return storageDomain;
    }

    protected VDSBrokerFrontend getVdsBroker() {
        return Backend.getInstance().getResourceManager();
    }

    protected BackendInternal getBackend() {
        return super.getBackend();
    }

    protected StorageDomainDao getStorageDomainDao() {
        return getDbFacade().getStorageDomainDao();
    }

    protected LunDao getLunDao() {
        return getDbFacade().getLunDao();
    }

    /* Execute wrappers (for testing/mocking necessities) */

    protected VdcReturnValueBase executeConnectStorageToVds(StorageServerConnectionParametersBase parameters) {
        return getBackend().runInternalAction(VdcActionType.ConnectStorageToVds, parameters);
    }

    protected VdcQueryReturnValue executeGetDeviceList(GetDeviceListQueryParameters parameters) {
        return getBackend().runInternalQuery(VdcQueryType.GetDeviceList, parameters);
    }

    protected VDSReturnValue executeGetVGInfo(GetVGInfoVDSCommandParameters parameters) {
        return getVdsBroker().RunVdsCommand(VDSCommandType.GetVGInfo, parameters);
    }

    protected VDSReturnValue executeHSMGetStorageDomainInfo(HSMGetStorageDomainInfoVDSCommandParameters parameters) {
        return getVdsBroker().RunVdsCommand(VDSCommandType.HSMGetStorageDomainInfo, parameters);
    }
}
