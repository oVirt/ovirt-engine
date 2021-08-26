package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@Deprecated
public abstract class MoveOrCopyTemplateCommand<T extends MoveOrCopyParameters> extends StorageDomainCommandBase<T> {

    @Inject
    protected VmHandler vmHandler;
    @Inject
    protected VmTemplateHandler vmTemplateHandler;
    @Inject
    private VmStaticDao vmStaticDao;

    protected Map<Guid, Guid> imageToDestinationDomainMap;
    protected Map<Guid, DiskImage> imageFromSourceDomainMap;
    private List<PermissionSubject> permissionCheckSubject;
    private List<DiskImage> templateDisks;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public MoveOrCopyTemplateCommand(Guid commandId) {
        super(commandId);
    }

    public MoveOrCopyTemplateCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public void init() {
        super.init();

        setVmTemplateId(getParameters().getContainerId());
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));
        if (imageToDestinationDomainMap == null) {
            imageToDestinationDomainMap = getParameters().getImageToDestinationDomainMap();
        }
        imageFromSourceDomainMap = new HashMap<>();
    }

    protected List<DiskImage> getTemplateDisks() {
        if (templateDisks == null && getVmTemplate() != null) {
            vmTemplateHandler.updateDisksFromDb(getVmTemplate());
            templateDisks = getVmTemplate().getDiskList();
        }
        return templateDisks;
    }

    protected void moveOrCopyAllImageGroups() {
        moveOrCopyAllImageGroups(getVmTemplateId(), getTemplateDisks());
    }

    protected void moveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (DiskImage disk : disks) {
                    ActionReturnValue vdcRetValue = runInternalActionWithTasksContext(
                            ActionType.CopyImageGroup,
                            buildModeOrCopyImageGroupParameters(containerID, disk));

                    getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
                }
                return null;
            }

            private MoveOrCopyImageGroupParameters buildModeOrCopyImageGroupParameters(
                    final Guid containerID, DiskImage disk) {
                MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(
                        containerID, disk.getId(), disk.getImageId(),
                        getParameters().getStorageDomainId(), ImageOperation.Copy);
                params.setParentCommand(getActionType());
                params.setEntityInfo(getParameters().getEntityInfo());
                params.setAddImageDomainMapping(true);
                params.setSourceDomainId(imageFromSourceDomainMap.get(disk.getId()).getStorageIds().get(0));
                params.setParentParameters(getParameters());
                return params;
            }
        });
    }

    protected void endMoveOrCopyCommand() {
        endActionOnAllImageGroups();
        endVmTemplateRelatedOps();
        setSucceeded(true);
    }

    protected final void endVmTemplateRelatedOps() {
        if (getVmTemplate() != null) {
            getVmDeviceUtils().setVmDevices(getVmTemplate());
            vmHandler.updateVmInitFromDB(getVmTemplate(), true);
            incrementDbGeneration();
            vmTemplateHandler.unlockVmTemplate(getVmTemplateId());
        } else {
            setCommandShouldBeLogged(false);
            log.warn("MoveOrCopyTemplateCommand::EndMoveOrCopyCommand: VmTemplate is null, not performing full endAction");
        }
    }

    protected void incrementDbGeneration() {
        vmStaticDao.incrementDbGeneration(getVmTemplate().getId());
    }

    @Override
    protected void endSuccessfully() {
        endMoveOrCopyCommand();
    }

    @Override
    protected void endWithFailure() {
        endMoveOrCopyCommand();
    }

    protected void endActionOnAllImageGroups() {
        for (ActionParametersBase p : getParameters().getImagesParameters()) {
            backend.endAction(ActionType.CopyImageGroup,
                    p,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
    }

    protected boolean validateSpaceRequirements(Collection<DiskImage> diskImages) {
        MultipleStorageDomainsValidator sdValidator = createMultipleStorageDomainsValidator(diskImages);
        if (!validate(sdValidator.allDomainsExistAndActive())
                || !validate(sdValidator.allDomainsWithinThresholds())) {
            return false;
        }

        if (getParameters().getCopyCollapse()) {
            return validate(sdValidator.allDomainsHaveSpaceForClonedDisks(diskImages));
        }

        return validate(sdValidator.allDomainsHaveSpaceForDisksWithSnapshots(diskImages));
    }

    protected MultipleStorageDomainsValidator createMultipleStorageDomainsValidator(Collection<DiskImage> diskImages) {
        return new MultipleStorageDomainsValidator(getStoragePoolId(),
                ImagesHandler.getAllStorageIdsForImageIds(diskImages));
    }

    protected void ensureDomainMap(Collection<DiskImage> images, Guid defaultDomainId) {
        if (imageToDestinationDomainMap == null) {
            imageToDestinationDomainMap = new HashMap<>();
        }
        if (imageToDestinationDomainMap.isEmpty() && images != null && defaultDomainId != null) {
            for (DiskImage image : images) {
                if (getParameters().isImagesExistOnTargetStorageDomain()) {
                    imageToDestinationDomainMap.put(image.getId(), image.getStorageIds().get(0));
                } else if (!Guid.Empty.equals(defaultDomainId)) {
                    imageToDestinationDomainMap.put(image.getId(), defaultDomainId);
                }
            }
        }
    }

    public boolean checkTemplateInStorageDomain(Guid storagePoolId,
            final Guid tmplId) {
        GetAllFromExportDomainQueryParameters tempVar =
                new GetAllFromExportDomainQueryParameters(storagePoolId, getParameters().getStorageDomainId());
        QueryReturnValue qretVal = backend.runInternalQuery(QueryType.GetTemplatesFromExportDomain,
                tempVar, getContext().getEngineContext());

        if (qretVal.getSucceeded()) {
            if (!VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(tmplId)) {
                Map<VmTemplate, List<DiskImage>> templates = qretVal.getReturnValue();
                return templates.keySet().stream().anyMatch(vmTemplate -> vmTemplate.getId().equals(tmplId));
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permissionCheckSubject == null) {
            if (imageToDestinationDomainMap == null || imageToDestinationDomainMap.isEmpty()) {
                permissionCheckSubject = super.getPermissionCheckSubjects();
            } else {
                permissionCheckSubject = new ArrayList<>();
                Set<PermissionSubject> permissionSet = new HashSet<>();
                for (Guid storageId : imageToDestinationDomainMap.values()) {
                    permissionSet.add(new PermissionSubject(storageId,
                            VdcObjectType.Storage,
                            getActionType().getActionGroup()));
                }
                permissionCheckSubject.addAll(permissionSet);
            }

        }
        return permissionCheckSubject;
    }
}
