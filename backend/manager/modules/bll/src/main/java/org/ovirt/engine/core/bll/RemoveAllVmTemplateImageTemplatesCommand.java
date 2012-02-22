package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.image_vm_map_id;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This command responsible to removing all Image Templates, of a VmTemplate
 * on all domains specified in the parameters
 */

@InternalCommandAttribute
public class RemoveAllVmTemplateImageTemplatesCommand<T extends VmTemplateParametersBase> extends VmTemplateCommand<T> {
    public RemoveAllVmTemplateImageTemplatesCommand(T parameters) {
        super(parameters);
        super.setVmTemplateId(parameters.getVmTemplateId());
    }

    @Override
    protected void executeCommand() {
        List<DiskImage> imageTemplates = DbFacade.getInstance().getDiskImageDAO().getAllForVm(
                getVmTemplateId());
        boolean noImagesRemovedYet = true;
        boolean changeStorageInImagesTable = false;
        for (DiskImage template : imageTemplates) {
            // get disk
            // remove this disk in all domain that were sent
            for (Guid domain : getParameters().getStorageDomainsList()) {
                ImagesContainterParametersBase tempVar = new ImagesContainterParametersBase(template.getId(),
                        template.getinternal_drive_mapping(), getVmTemplateId());
                tempVar.setStorageDomainId(domain);
                tempVar.setStoragePoolId(template.getstorage_pool_id().getValue());
                tempVar.setImageGroupID(template.getimage_group_id().getValue());
                tempVar.setEntityId(getParameters().getEntityId());
                tempVar.setWipeAfterDelete(template.getwipe_after_delete());
                tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
                VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                                VdcActionType.RemoveTemplateSnapshot,
                                tempVar,
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

                if (vdcReturnValue.getSucceeded()) {
                    getReturnValue().getInternalTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                } else {
                    if (noImagesRemovedYet) {
                        setSucceeded(false);
                        getReturnValue().setFault(vdcReturnValue.getFault());
                        return;
                    }

                    log.errorFormat("Can't remove image id: {0} for template id: {1} from domain id: {2} due to: {3}.",
                            template.getId(), getVmTemplateId(), domain,
                            vdcReturnValue.getFault().getMessage());
                }

                // if removing from the domain saved in images table, set value to change it to another domain
                if (!changeStorageInImagesTable) {
                    changeStorageInImagesTable = domain.equals(template.getstorage_ids().get(0));
                }
                DbFacade.getInstance().getStorageDomainDAO().removeImageStorageDomainMap(
                        new image_storage_domain_map(template.getId(), domain));
                noImagesRemovedYet = false;
            }

            // remove images from db only if removing template completely
            if (getParameters().isRemoveTemplateFromDb()) {
                DiskImage diskImage = DbFacade.getInstance().getDiskImageDAO().get(template.getId());
                if (diskImage != null) {
                    DbFacade.getInstance().getDiskDao().remove(diskImage.getimage_group_id());
                    DbFacade.getInstance()
                            .getImageVmMapDAO()
                            .remove(new image_vm_map_id(diskImage.getId(), diskImage.getvm_guid()));
                    DbFacade.getInstance()
                            .getVmDeviceDAO()
                            .remove(new VmDeviceId(diskImage.getId(), diskImage.getvm_guid()));
                    DbFacade.getInstance().getStorageDomainDAO().removeImageStorageDomainMap(diskImage.getId());
                    DbFacade.getInstance().getDiskImageDAO().remove(template.getId());
                }
            }
        }
        setSucceeded(true);
    }

    private static Log log = LogFactory.getLog(RemoveAllVmTemplateImageTemplatesCommand.class);
}
