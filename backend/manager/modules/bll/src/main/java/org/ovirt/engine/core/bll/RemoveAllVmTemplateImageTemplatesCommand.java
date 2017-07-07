package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.VmTemplateManagementParameters;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMapId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

/**
 * This command responsible to removing all Image Templates, of a VmTemplate
 * on all domains specified in the parameters
 */

@InternalCommandAttribute
public class RemoveAllVmTemplateImageTemplatesCommand<T extends VmTemplateManagementParameters> extends VmTemplateManagementCommand<T> {

    @Inject
    private DiskDao diskDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private ImageDao imageDao;

    public RemoveAllVmTemplateImageTemplatesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        super.setVmTemplateId(parameters.getVmTemplateId());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void executeCommand() {
        List<DiskImage> imageTemplates = DisksFilter.filterImageDisks(diskDao.getAllForVm(getVmTemplateId()),
                ONLY_ACTIVE);
        for (DiskImage template : imageTemplates) {
            // remove this disk in all domain that were sent
            for (Guid domain : (Collection<Guid>)CollectionUtils.intersection(getParameters().getStorageDomainsList(), template.getStorageIds())) {
                ImagesContainterParametersBase tempVar = new ImagesContainterParametersBase(template.getImageId(),
                        getVmTemplateId());
                tempVar.setStorageDomainId(domain);
                tempVar.setStoragePoolId(template.getStoragePoolId());
                tempVar.setImageGroupID(template.getId());
                tempVar.setEntityInfo(getParameters().getEntityInfo());
                tempVar.setWipeAfterDelete(template.isWipeAfterDelete());
                tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
                tempVar.setParentCommand(getActionType());
                tempVar.setParentParameters(getParameters());
                ActionReturnValue actionReturnValue = runInternalActionWithTasksContext(
                                ActionType.RemoveTemplateSnapshot,
                                tempVar);

                if (actionReturnValue.getSucceeded()) {
                    getReturnValue().getInternalVdsmTaskIdList().addAll(actionReturnValue.getInternalVdsmTaskIdList());
                } else {
                    log.error("Can't remove image id '{}' for template id '{}' from domain id '{}' due to: {}.",
                            template.getImageId(), getVmTemplateId(), domain,
                            actionReturnValue.getFault().getMessage());
                }

                imageStorageDomainMapDao.remove(new ImageStorageDomainMapId(template.getImageId(), domain));
            }

            DiskImage diskImage = diskImageDao.get(template.getImageId());
            if (diskImage != null) {
                baseDiskDao.remove(template.getId());
                vmDeviceDao.remove(new VmDeviceId(diskImage.getImageId(), getVmTemplateId()));
                imageStorageDomainMapDao.remove(diskImage.getImageId());
                imageDao.remove(template.getImageId());
            }
        }
        setSucceeded(true);
    }
}
