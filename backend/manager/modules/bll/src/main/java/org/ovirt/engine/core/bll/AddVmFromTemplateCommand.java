package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.CreateCloneOfTemplateParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmStaticDao;

/**
 * This class adds a cloned VM from a template (Deep disk copy)
 */
public class AddVmFromTemplateCommand<T extends AddVmParameters> extends AddVmCommand<T> {
    private Map<Guid, Guid> diskInfoSourceMap;
    private Map<Guid, Set<Guid>> validDisksDomains;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private DiskImageDao diskImageDao;

    public AddVmFromTemplateCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected AddVmFromTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected boolean validateIsImagesOnDomains() {
        return true;
    }

    @Override
    protected void init() {
        super.init();
        T parameters = getParameters();
        List<CinderDisk> cinderDisks = DisksFilter.filterCinderDisks(getVmTemplate().getDiskTemplateMap().values());
        parameters.setUseCinderCommandCallback(!cinderDisks.isEmpty());
    }

    @Override
    protected void executeVmCommand() {
        super.executeVmCommand();
        getVm().setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        getVm().getStaticData().setQuotaId(getParameters().getVmStaticData().getQuotaId());
        vmStaticDao.update(getVm().getStaticData());
        checkTrustedService();
    }

    private void checkTrustedService() {
        if (getVmTemplate().isTrustedService() && !getVm().isTrustedService()) {
            auditLogDirector.log(this, AuditLogType.USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED);
        } else if (!getVmTemplate().isTrustedService() && getVm().isTrustedService()) {
            auditLogDirector.log(this, AuditLogType.USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    /**
     * TODO: need to see why those checks are not executed
     * for this command
     */
    @Override
    protected boolean checkTemplateImages() {
        return true;
    }

    @Override
    protected ActionType getDiskCreationCommandType() {
        return ActionType.CreateCloneOfTemplate;
    }

    @Override
    protected void lockVM() {
        vmHandler.lockVm(getVm().getDynamicData(), getCompensationContext());
    }

    @Override
    protected Collection<DiskImage> getImagesToCheckDestinationStorageDomains() {
        return getVmTemplate().getDiskTemplateMap().values();
    }

    @Override
    protected CreateCloneOfTemplateParameters buildDiskCreationParameters(DiskImage disk) {
        DiskImageBase diskInfo = getParameters().getDiskInfoDestinationMap().get(disk.getId());
        CreateCloneOfTemplateParameters params = new CreateCloneOfTemplateParameters(disk.getImageId(),
                getParameters().getVmStaticData().getId(), diskInfo);
        params.setStorageDomainId(diskInfoSourceMap.get(disk.getId()));
        params.setDestStorageDomainId(retrieveDestinationDomainForDisk(disk.getId()));
        params.setDiskAlias(diskInfoDestinationMap.get(disk.getId()).getDiskAlias());
        params.setVmSnapshotId(getVmSnapshotId());
        params.setParentCommand(ActionType.AddVmFromTemplate);
        params.setParentParameters(getParameters());
        params.setEntityInfo(getParameters().getEntityInfo());
        params.setQuotaId(diskInfoDestinationMap.get(disk.getId()).getQuotaId() != null ?
                diskInfoDestinationMap.get(disk.getId()).getQuotaId() : null);
        params.setDiskProfileId(diskInfoDestinationMap.get(disk.getId()).getDiskProfileId());
        return params;
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        List<DiskImage> templateDiskImages = DisksFilter.filterImageDisks(getVmTemplate().getDiskTemplateMap().values(),
                ONLY_NOT_SHAREABLE);
        for (DiskImage dit : templateDiskImages) {
            DiskImage diskImage = diskInfoDestinationMap.get(dit.getId());
            if (!ImagesHandler.checkImageConfiguration(
                    destStorages.get(diskImage.getStorageIds().get(0)).getStorageStaticData(),
                    diskImage,
                    getReturnValue().getValidationMessages())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean validateFreeSpace(StorageDomainValidator storageDomainValidator, List<DiskImage> disksList) {
        for (DiskImage diskImage : disksList) {
            List<DiskImage> snapshots = diskImageDao.getAllSnapshotsForLeaf(diskImage.getImageId());
            diskImage.getSnapshots().addAll(snapshots);
        }
        return validate(storageDomainValidator.hasSpaceForClonedDisks(disksList));
    }

    @Override
    protected boolean verifySourceDomains() {
        Map<Guid, StorageDomain> poolDomainsMap = Entities.businessEntitiesById(getPoolDomains());
        List<DiskImage> templateDiskImages = DisksFilter.filterImageDisks(getImagesToCheckDestinationStorageDomains(),
                ONLY_NOT_SHAREABLE);
        validDisksDomains = findDomainsInApplicableStatusForDisks(templateDiskImages, poolDomainsMap);

        StringBuilder disksInfo = new StringBuilder();
        for (DiskImage diskImage : templateDiskImages) {
            Set<Guid> applicableDomains = validDisksDomains.get(diskImage.getId());
            if (!applicableDomains.isEmpty()) {
                continue;
            }

            List<String> nonApplicableStorageInfo = new LinkedList<>();
            for (Guid id : diskImage.getStorageIds()) {
                StorageDomain domain = poolDomainsMap.get(id);
                nonApplicableStorageInfo.add(String.format("%s - %s", domain.getName(), domain.getStatus().toString()));
            }

            disksInfo.append(String.format("%s (%s) %n",
                    diskImage.getDiskAlias(),
                    StringUtils.join(nonApplicableStorageInfo, " / ")));
        }

        if (disksInfo.length() > 0) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_VALID_DOMAINS_STATUS_FOR_TEMPLATE_DISKS,
                    String.format("$disksInfo %s",
                            disksInfo.toString()),
                    String.format("$applicableStatus %s", StorageDomainStatus.Active.toString()));
        }

        return true;
    }

    private static Map<Guid, Set<Guid>> findDomainsInApplicableStatusForDisks
            (List<DiskImage> diskImages, Map<Guid, StorageDomain> storageDomains) {
        return diskImages
                .stream()
                .collect(Collectors.toMap(
                        DiskImage::getId,
                        diskImage -> diskImage
                                .getStorageIds()
                                .stream()
                                .filter(sdId -> storageDomains.get(sdId).getStatus() == StorageDomainStatus.Active)
                                .collect(Collectors.toSet())
                ));
    }


    @Override
    protected void chooseDisksSourceDomains() {
        diskInfoSourceMap = new HashMap<>();
        List<DiskImage> templateDiskImages = DisksFilter.filterImageDisks(getImagesToCheckDestinationStorageDomains(),
                ONLY_NOT_SHAREABLE);
        for (DiskImage disk : templateDiskImages) {
            Guid diskId = disk.getId();
            Set<Guid> validDomainsForDisk = validDisksDomains.get(diskId);
            Guid destinationDomain = retrieveDestinationDomainForDisk(diskId);

            // if the destination domain is one of the valid source domains, we can
            // choose the same domain as the source domain for
            // possibly faster operation, otherwise we'll choose random valid domain as the source.
            if (validDomainsForDisk.contains(destinationDomain)) {
                diskInfoSourceMap.put(diskId, destinationDomain);
            } else {
                diskInfoSourceMap.put(diskId, validDomainsForDisk.iterator().next());
            }
        }
    }

    @Override
    protected boolean isDisksVolumeFormatValid() {
        return true;
    }

    private Guid retrieveDestinationDomainForDisk(Guid id) {
        return diskInfoDestinationMap.get(id).getStorageIds().get(0);
    }

    @Override
    protected boolean isVirtioScsiEnabled() {
        return getParameters().isVirtioScsiEnabled() != null ?
                super.isVirtioScsiEnabled() : isVirtioScsiControllerAttached(getVmTemplateId());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return AuditLogType.USER_ADD_VM_STARTED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_ADD_VM_FINISHED_SUCCESS
                    : AuditLogType.USER_ADD_VM_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_ADD_VM_FINISHED_FAILURE;
        }
    }
}
