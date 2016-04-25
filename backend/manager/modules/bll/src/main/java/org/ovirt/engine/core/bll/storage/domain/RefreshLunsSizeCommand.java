package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetStorageDomainStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResizeStorageDomainPVVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RefreshLunsSizeCommand<T extends ExtendSANStorageDomainParameters> extends
        StorageDomainCommandBase<T> {

    private boolean deviceSizeVisibilityError = false;

    public RefreshLunsSizeCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        if (!(checkStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active))) {
            return false;
        }

        if (!getStorageDomain().getStorageType().isBlockDomain()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        if (!checkLunsInStorageDomain(getParameters().getLunIds())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_LUNS_NOT_PART_OF_STORAGE_DOMAIN);
        }

        return true;
    }

    private boolean checkLunsInStorageDomain(List<String> lunIds) {
        // Get LUNs from DB
        List<LUNs> lunsFromDb = getLunDao().getAllForVolumeGroup(getStorageDomain().getStorage());
        Set<String> lunsSet = new HashSet<>(lunIds);

        for (LUNs lun : lunsFromDb) {
            if (lunsSet.contains(lun.getLUNId())) {
                    // LUN is part of the storage domain
                    lunsSet.remove(lun.getLUNId());
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
                String vdsListString = vdsList.stream().map(VDS::getName).collect(Collectors.joining(", "));
                failedVds.add("LUN : " + lunId + "VDS: " + vdsListString);
            }

            throw new EngineException(EngineError.REFRESH_LUN_ERROR,
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
                            getStorageDomain().getStorageType(), false,
                            lunsToResize);

            List<LUNs> luns = (List<LUNs>) runVdsCommand(VDSCommandType.GetDeviceList, parameters).getReturnValue();
            for (LUNs lun : luns) {
                MultiValueMapUtils.addToMap(lun.getLUNId(),
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
        if (returnValueUpdatedStorageDomain != null) {
            StorageDomain updatedStorageDomain = (StorageDomain) returnValueUpdatedStorageDomain.getReturnValue();
            updateStorageDomain(updatedStorageDomain);
        } else {
            log.error("Failed to update Storage Domain Data.");
        }
    }

    protected VDSReturnValue getStatsForDomain() {
        Optional<VDS> vds = getAllRunningVdssInPool().stream().findFirst();
        if (!vds.isPresent()) {
            return null;
        }
        return runVdsCommand(VDSCommandType.GetStorageDomainStats,
                new GetStorageDomainStatsVDSCommandParameters(vds.get().getId(), getParameters().getStorageDomainId()));
    }

    protected void updateStorageDomain(final StorageDomain storageDomainToUpdate) {
        executeInNewTransaction(() -> {
            CompensationContext context = getCompensationContext();
            context.snapshotEntity(storageDomainToUpdate.getStorageDynamicData());
            getDbFacade().getStorageDomainDynamicDao().update(storageDomainToUpdate.getStorageDynamicData());
            getCompensationContext().stateChanged();
            return null;
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
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }
}
