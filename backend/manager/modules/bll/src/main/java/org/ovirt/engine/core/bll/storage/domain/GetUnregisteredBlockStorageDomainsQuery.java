package org.ovirt.engine.core.bll.storage.domain;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredBlockStorageDomainsParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GetUnregisteredBlockStorageDomainsQuery<P extends GetUnregisteredBlockStorageDomainsParameters> extends QueriesCommandBase<P> {
    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private LunDao lunDao;

    public GetUnregisteredBlockStorageDomainsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
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
            ActionReturnValue returnValue = executeConnectStorageToVds(
                        new StorageServerConnectionParametersBase(storageConnection, getParameters().getVdsId(), false));

            if (returnValue.getSucceeded()) {
                connectedTargets.add(storageConnection);
            } else {
                log.error("Could not connect to target IQN '{}': {}",
                        storageConnection.getIqn(), returnValue.getFault().getMessage());
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
        QueryReturnValue returnValue =
                executeGetDeviceList(
                new GetDeviceListQueryParameters(getParameters().getVdsId(),
                        getParameters().getStorageType(),
                        false, null, false));

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
        final Set<String> targetIQNs = targets.stream().map(StorageServerConnections::getIqn).collect(toSet());

        return luns.stream()
                .filter(lun -> lun.getLunConnections().stream().anyMatch(c -> targetIQNs.contains(c.getIqn())))
                .collect(Collectors.toList());
    }

    /**
     * Filter out LUNs that are already a part of storage domains exist in the system.
     *
     * @param luns the LUNs list
     *
     * @return the filtered LUNs list
     */
    protected List<LUNs> filterLUNsThatBelongToExistingStorageDomains(List<LUNs> luns) {
        List<StorageDomain> existingStorageDomains = storageDomainDao.getAll();
        final Set<Guid> existingStorageDomainIDs =
                existingStorageDomains.stream().map(StorageDomain::getId).collect(Collectors.toSet());

        return luns.stream().filter(lun -> !existingStorageDomainIDs.contains(lun.getStorageDomainId()))
                .collect(Collectors.toList());
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
            if (!lun.getVolumeGroupId().isEmpty()) {
                vgSet.add(lun.getVolumeGroupId());
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
        Set<String> existingLunIds = lunDao.getAll().stream().map(LUNs::getId).collect(Collectors.toSet());

        for (String vgID : vgIDs) {
            VDSReturnValue returnValue;
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
            if (luns.stream().anyMatch(l -> existingLunIds.contains(l.getId()))) {
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
        VDSReturnValue returnValue;

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

    /* Execute wrappers (for testing/mocking necessities) */

    protected ActionReturnValue executeConnectStorageToVds(StorageServerConnectionParametersBase parameters) {
        return backend.runInternalAction(ActionType.ConnectStorageToVds, parameters);
    }

    protected QueryReturnValue executeGetDeviceList(GetDeviceListQueryParameters parameters) {
        return backend.runInternalQuery(QueryType.GetDeviceList, parameters);
    }

    protected VDSReturnValue executeGetVGInfo(GetVGInfoVDSCommandParameters parameters) {
        return runVdsCommand(VDSCommandType.GetVGInfo, parameters);
    }

    protected VDSReturnValue executeHSMGetStorageDomainInfo(HSMGetStorageDomainInfoVDSCommandParameters parameters) {
        return runVdsCommand(VDSCommandType.HSMGetStorageDomainInfo, parameters);
    }
}
