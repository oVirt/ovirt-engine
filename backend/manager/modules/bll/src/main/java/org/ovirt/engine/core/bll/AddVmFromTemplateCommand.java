package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.CreateCloneOfTemplateParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

/**
 * This class adds a cloned VM from a template (Deep disk copy)
 */
public class AddVmFromTemplateCommand<T extends AddVmParameters> extends AddVmCommand<T> {
    private Map<Guid, Guid> diskInfoSourceMap;
    private Map<Guid, Set<Guid>> validDisksDomains;

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
        List<CinderDisk> cinderDisks =
                ImagesHandler.filterDisksBasedOnCinder(getVmTemplate().getDiskTemplateMap().values());
        parameters.setUseCinderCommandCallback(!cinderDisks.isEmpty());
    }

    @Override
    protected void executeVmCommand() {
        super.executeVmCommand();
        getVm().setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        getVm().getStaticData().setQuotaId(getParameters().getVmStaticData().getQuotaId());
        DbFacade.getInstance().getVmStaticDao().update(getVm().getStaticData());
        // if there are no tasks, we can end the command right away.
        if (getTaskIdList().isEmpty()) {
            endSuccessfully();
        }
        checkTrustedService();
    }

    private void checkTrustedService() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VmName", getVmName());
        logable.addCustomValue("VmTemplateName", getVmTemplateName());
        if (getVmTemplate().isTrustedService() && !getVm().isTrustedService()) {
            auditLogDirector.log(logable, AuditLogType.USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVmTemplate().isTrustedService() && getVm().isTrustedService()) {
            auditLogDirector.log(logable, AuditLogType.USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    /**
     * TODO: need to see why those checks are not executed
     * for this command
     */
    @Override
    protected boolean checkTemplateImages(List<String> reasons) {
        return true;
    }

    @Override
    protected VdcActionType getDiskCreationCommandType() {
        return VdcActionType.CreateCloneOfTemplate;
    }

    @Override
    protected void lockVM() {
        VmHandler.lockVm(getVm().getDynamicData(), getCompensationContext());
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
        params.setParentCommand(VdcActionType.AddVmFromTemplate);
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

        List<DiskImage> templateDiskImages = ImagesHandler.filterImageDisks(
                getVmTemplate().getDiskTemplateMap().values(), true, false, false);
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
            List<DiskImage> snapshots = getAllImageSnapshots(diskImage);
            diskImage.getSnapshots().addAll(snapshots);
        }
        return validate(storageDomainValidator.hasSpaceForClonedDisks(disksList));
    }

    protected List<DiskImage> getAllImageSnapshots(DiskImage diskImage) {
        return ImagesHandler.getAllImageSnapshots(diskImage.getImageId());
    }

    @Override
    protected boolean verifySourceDomains() {
        Map<Guid, StorageDomain> poolDomainsMap = Entities.businessEntitiesById(getPoolDomains());
        EnumSet<StorageDomainStatus> validDomainStatuses = EnumSet.of(StorageDomainStatus.Active);
        List<DiskImage> templateDiskImages = ImagesHandler.filterImageDisks(
                getImagesToCheckDestinationStorageDomains(), true, false, false);
        validDisksDomains =
                ImagesHandler.findDomainsInApplicableStatusForDisks(templateDiskImages,
                        poolDomainsMap,
                        validDomainStatuses);
        return validate(new DiskImagesValidator(templateDiskImages).diskImagesOnAnyApplicableDomains(
                validDisksDomains, poolDomainsMap,
                EngineMessage.ACTION_TYPE_FAILED_NO_VALID_DOMAINS_STATUS_FOR_TEMPLATE_DISKS, validDomainStatuses));

    }

    @Override
    protected void chooseDisksSourceDomains() {
        diskInfoSourceMap = new HashMap<>();
        List<DiskImage> templateDiskImages = ImagesHandler.filterImageDisks(
                getImagesToCheckDestinationStorageDomains(), true, false, false);
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

    private Guid retrieveDestinationDomainForDisk(Guid id) {
        return diskInfoDestinationMap.get(id).getStorageIds().get(0);
    }

    @Override
    protected boolean isVirtioScsiEnabled() {
        return getParameters().isVirtioScsiEnabled() != null ?
                super.isVirtioScsiEnabled() : isVirtioScsiControllerAttached(getVmTemplateId());
    }

    @Override
    public CommandCallback getCallback() {
        return getParameters().isUseCinderCommandCallback() ? new ConcurrentChildCommandsExecutionCallback() : null;
    }
}
