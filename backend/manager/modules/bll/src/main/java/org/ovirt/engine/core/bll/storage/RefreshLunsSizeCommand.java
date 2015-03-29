package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetStorageDomainStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResizeStorageDomainPVVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RefreshLunsSizeCommand<T extends ExtendSANStorageDomainParameters> extends
        StorageDomainCommandBase<T> {

    private boolean deviceSizeVisibilityError = false;

    public RefreshLunsSizeCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public RefreshLunsSizeCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected boolean canDoAction() {
        if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        if (!FeatureSupported.refreshLunSupported(getStoragePool().getCompatibilityVersion())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_REFRESH_LUNS_UNSUPPORTED_ACTION);
        }

        if (!(checkStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active))) {
            return false;
        }

        if (!getStorageDomain().getStorageType().isBlockDomain()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        if (!checkLunsInStorageDomain(getParameters().getLunIds(), getStorageDomain())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_LUNS_NOT_PART_OF_STORAGE_DOMAIN);
        }

        return true;
    }

    private boolean checkLunsInStorageDomain(List<String> lunIds, StorageDomain storageDomain) {
        // Get LUNs from DB
        List<LUNs> lunsFromDb = getLunDao().getAllForVolumeGroup(getStorageDomain().getStorage());
        Set<String> lunsSet = new HashSet<>(lunIds);

        for (LUNs lun : lunsFromDb) {
            if (lunsSet.contains(lun.getLUN_id())) {
                    // LUN is part of the storage domain
                    lunsSet.remove(lun.getLUN_id());
            }
        }
        return lunsSet.isEmpty();
    }

    @Override
    protected void executeCommand() {
        changeStorageDomainStatusInTransaction(getStorageDomain().getStoragePoolIsoMapData(),
                StorageDomainStatus.Locked);

        // Call GetDeviceList on specific LUNs on all Hosts
        List<String> lunsToRefresh = getParameters().getLunIds();
        Map<String, List<Pair<VDS, Integer>>> lunToVds = getDeviceListAllVds(lunsToRefresh);

        //Check if all hosts are seeing the same LUNs size.
        Map<String, List<VDS>> lunToFailedVDS = getFailedLuns(lunToVds);

        if (!lunToFailedVDS.isEmpty()) {
            deviceSizeVisibilityError = true;
            List<String> failedVds = new ArrayList<>();
            for (Map.Entry<String, List<VDS>> entry : lunToFailedVDS.entrySet()) {
                String lunId = entry.getKey();
                List<VDS> vdsList = entry.getValue();
                log.error("Failed to refresh device " + lunId + " Not all VDS are seeing the same size " +
                        "VDS :" + vdsList);
                String vdsListString = StringUtils.join(Entities.objectNames(vdsList), ", ");
                failedVds.add("LUN : " + lunId + "VDS: " + vdsListString);
            }

            throw new VdcBLLException(VdcBllErrors.REFRESH_LUN_ERROR,
                    "Failed to refresh LUNs. Not all VDS are seeing the same size: " + failedVds);
        }

        // Call PVs resize on SPM
        resizePVs(lunsToRefresh);

        // Update storage domain size
        updateStorageDomainData();

        changeStorageDomainStatusInTransaction(getStorageDomain().getStoragePoolIsoMapData(),
                StorageDomainStatus.Active);

        setSucceeded(true);
    }

    /**
        This  method calls GetDeviceList with the specified luns on all hosts.
        In VDSM , this call will resize the devices if needed.
        It returns a map of LUN ID to a list of Pair(VDS,Size)
        This map will help to check if all hosts are seeing the same size of the LUNs.
    **/
    private Map<String, List<Pair<VDS, Integer>>> getDeviceListAllVds(List<String> lunsToResize) {
        Map<String, List<Pair<VDS, Integer>>> lunToVds = new HashMap<>();
        for (VDS vds : getAllRunningVdssInPool()) {
            GetDeviceListVDSCommandParameters parameters =
                    new GetDeviceListVDSCommandParameters(vds.getId(),
                            getStorageDomain().getStorageType(),
                            lunsToResize);

            List<LUNs> luns = (List<LUNs>) runVdsCommand(VDSCommandType.GetDeviceList, parameters).getReturnValue();
            for (LUNs lun : luns) {
                MultiValueMapUtils.addToMap(lun.getLUN_id(),
                        new Pair<>(vds, lun.getDeviceSize()), lunToVds);
            }
        }
        return lunToVds;
    }

    private Map<String, List<VDS>> getFailedLuns(Map<String, List<Pair<VDS, Integer>>> lunToVds) {
        Map<String, List<VDS>> failedVds = new HashMap<>();
        for (Map.Entry<String, List<Pair<VDS, Integer>>> entry : lunToVds.entrySet()) {
            List<VDS> vdsList = new ArrayList<>();
            Integer size = -1;
            boolean failed = false;
            for (Pair<VDS, Integer> vdsSizePair : entry.getValue()) {
                vdsList.add(vdsSizePair.getFirst());
                if (size == -1) {
                    size = vdsSizePair.getSecond();
                } else if (!size.equals(vdsSizePair.getSecond())) {
                        failed = true;
                }
            }
            if (failed) {
                failedVds.put(entry.getKey(), vdsList);
            }
        }
        return failedVds;
    }

    private void resizePVs(List<String> lunsToRefresh) {
        for (String lun : lunsToRefresh) {
            Long pvSizeInBytes = resizeStorageDomainPV(lun);
            log.debug("PV size after resize of LUN " + lun + " :" + pvSizeInBytes + " bytes");
        }
    }

    private Long resizeStorageDomainPV(String lunId) {
        return (Long) runVdsCommand(
                VDSCommandType.ResizeStorageDomainPV,
                new ResizeStorageDomainPVVDSCommandParameters(getStoragePoolId(),
                        getStorageDomainId(), lunId)).getReturnValue();
    }

    private void updateStorageDomainData() {
        VDSReturnValue returnValueUpdatedStorageDomain = getStatsForDomain();
        StorageDomain updatedStorageDomain = (StorageDomain) returnValueUpdatedStorageDomain.getReturnValue();
        updateStorageDomain(updatedStorageDomain);
    }

    protected VDSReturnValue getStatsForDomain() {
        VDS vds = LinqUtils.first(getAllRunningVdssInPool());
        return runVdsCommand(VDSCommandType.GetStorageDomainStats,
                new GetStorageDomainStatsVDSCommandParameters(vds.getId(), getParameters().getStorageDomainId()));
    }

    protected void updateStorageDomain(final StorageDomain storageDomainToUpdate) {
        executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                CompensationContext context = getCompensationContext();
                context.snapshotEntity(storageDomainToUpdate.getStorageDynamicData());
                getDbFacade().getStorageDomainDynamicDao().update(storageDomainToUpdate.getStorageDynamicData());
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REFRESH_LUN_STORAGE_DOMAIN
                : deviceSizeVisibilityError ? AuditLogType.USER_REFRESH_LUN_STORAGE_DIFFERENT_SIZE_DOMAIN_FAILED
                        : AuditLogType.USER_REFRESH_LUN_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }
}
