package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.backendinterfaces.BaseHandler;
import org.ovirt.engine.core.common.businessentities.EditableVmField;
import org.ovirt.engine.core.common.businessentities.EditableVmTemplateField;
import org.ovirt.engine.core.common.businessentities.TransientField;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmTemplateHandler implements BackendService {
    private static final Logger log = LoggerFactory.getLogger(VmTemplateHandler.class);

    public static final Guid BLANK_VM_TEMPLATE_ID = Guid.Empty;
    public static final String BLANK_VM_TEMPLATE_NAME = "Blank";

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private DiskDao diskDao;

    @Inject
    private DiskVmElementDao diskVmElementDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private ImagesHandler imagesHandler;

    private ObjectIdentityChecker updateVmTemplate;

    /**
     * Initialize list containers, for identity and permission check. The initialization should be executed
     * before calling ObjectIdentityChecker.
     *
     * @see Backend#initHandlers()
     */
    @PostConstruct
    public void init() {
        final Class<?>[] inspectedClassNames = new Class<?>[]{VmBase.class, VmTemplate.class};
        updateVmTemplate = new ObjectIdentityChecker(VmTemplateHandler.class);

        for (Pair<EditableVmTemplateField, Field> pair : BaseHandler.extractAnnotatedFields(EditableVmTemplateField.class, inspectedClassNames)) {
            String fieldName = pair.getSecond().getName();
            updateVmTemplate.addPermittedFields(fieldName);
        }

        for (Pair<TransientField, Field> pair : BaseHandler.extractAnnotatedFields(TransientField.class, inspectedClassNames)) {
            updateVmTemplate.addTransientFields(pair.getSecond().getName());
        }

        for (Pair<EditableVmField, Field> pair : BaseHandler.extractAnnotatedFields(EditableVmField.class, inspectedClassNames)) {
            EditableVmField annotation = pair.getFirst();
            List<VMStatus> statusList = Arrays.asList(annotation.onStatuses());
            String fieldName = pair.getSecond().getName();

            if (statusList.isEmpty()) {
                updateVmTemplate.addPermittedFields(fieldName);
            }
        }
    }

    public boolean isUpdateValid(VmTemplate source, VmTemplate destination) {
        return updateVmTemplate.isUpdateValid(source, destination);
    }

    public void updateDisksFromDb(VmTemplate vmt) {
        vmt.getDiskTemplateMap().clear();
        vmt.getDiskImageMap().clear();
        vmt.getDiskList().clear();
        List<Disk> diskList = diskDao.getAllForVm(vmt.getId());
        for (Disk dit : diskList) {
            DiskImage diskImage = (DiskImage) dit;
            vmt.getDiskTemplateMap().put(dit.getId(), diskImage);
            vmt.getDiskImageMap().put(dit.getId(), diskImage);

            DiskVmElement dve = diskVmElementDao.get(new VmDeviceId(dit.getId(), vmt.getId()));
            dit.setDiskVmElements(Collections.singletonList(dve));

            vmt.getDiskList().add(diskImage);
        }
    }

    /**
     * Lock the VM template with the given id in a new transaction, handling the compensation data using the given
     * {@link CompensationContext}.
     *
     * @param vmTemplateGuid
     *            The id of the template to lock.
     * @param compensationContext
     *            The compensation context for saving the old status (can't be <code>null</code>).
     */
    public void lockVmTemplateInTransaction(final Guid vmTemplateGuid,
            final CompensationContext compensationContext) {
        TransactionSupport.executeInNewTransaction(() -> {
            setVmTemplateStatus(vmTemplateGuid, VmTemplateStatus.Locked, compensationContext);
            compensationContext.stateChanged();
            return null;
        });
    }

    public void unlockVmTemplate(Guid vmTemplateGuid) {
        setVmTemplateStatus(vmTemplateGuid, VmTemplateStatus.OK, null);
    }

    /**
     * Set the status of the VM template with the given id to the desired status, saving the old status if necessary.
     *
     * @param vmTemplateGuid
     *            The id of the template to set the status for.
     * @param status
     *            The status to set.
     * @param compensationContext
     *            The compensation context for saving the old status (can be <code>null</code> if the old status is not
     *            required to be saved).
     */
    private void setVmTemplateStatus(
            Guid vmTemplateGuid, VmTemplateStatus status, CompensationContext compensationContext) {
        VmTemplate vmTemplate = vmTemplateDao.get(vmTemplateGuid);
        if (vmTemplate != null) {
            if (compensationContext != null) {
                compensationContext.snapshotEntityStatus(vmTemplate);
            }
            vmTemplate.setStatus(status);
            vmTemplateDao.update(vmTemplate);
        } else {
            log.warn(
                    "setVmTemplateStatus: vmTemplate is null, not setting status '{}' to vmTemplate",
                    status);
        }
    }

    public ValidationResult isVmTemplateImagesReady(VmTemplate vmTemplate,
            Guid storageDomainId,
            boolean checkImagesExists,
            boolean checkLocked,
            boolean checkIllegal,
            boolean checkStorageDomain, List<DiskImage> providedVmtImages) {
        List<DiskImage> vmtImages = providedVmtImages;
        if (checkStorageDomain) {
            StorageDomainValidator storageDomainValidator =
                    new StorageDomainValidator(storageDomainDao.getForStoragePool(
                            storageDomainId, vmTemplate.getStoragePoolId()));
            ValidationResult returnValue = storageDomainValidator.isDomainExistAndActive();
            if (!returnValue.isValid()) {
                return returnValue;
            }
        }
        if (checkImagesExists) {
            if (vmtImages == null) {
                vmtImages =
                        DisksFilter.filterImageDisks(diskDao.getAllForVm(vmTemplate.getId()),
                                ONLY_ACTIVE);
            }
            if (vmtImages.size() > 0
                    && !imagesHandler.isImagesExists(vmtImages, vmtImages.get(0).getStoragePoolId())) {
                return new ValidationResult(EngineMessage.TEMPLATE_IMAGE_NOT_EXIST);
            }
        }
        if (checkLocked) {
            if (vmTemplate.getStatus() == VmTemplateStatus.Locked) {
                return new ValidationResult(EngineMessage.VM_TEMPLATE_IMAGE_IS_LOCKED);
            }
            if (vmtImages != null) {
                for (DiskImage image : vmtImages) {
                    if (image.getImageStatus() == ImageStatus.LOCKED) {
                        return new ValidationResult(EngineMessage.VM_TEMPLATE_IMAGE_IS_LOCKED);
                    }
                }
            }
        }
        if (checkIllegal && (vmTemplate.getStatus() == VmTemplateStatus.Illegal)) {
            return new ValidationResult(EngineMessage.VM_TEMPLATE_IMAGE_IS_ILLEGAL);
        }
        return ValidationResult.VALID;
    }
}

