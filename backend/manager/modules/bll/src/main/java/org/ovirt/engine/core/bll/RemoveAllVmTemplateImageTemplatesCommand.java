package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.image_group_storage_domain_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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
        List<DiskImageTemplate> imageTemplates = DbFacade.getInstance().getDiskImageTemplateDAO().getAllByVmTemplate(
                getVmTemplateId());
        boolean noImagesRemovedYet = true;
        boolean changeStorageInImagesTable = false;
        for (DiskImageTemplate template : imageTemplates) {
            // get disk
            DiskImage disk = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(template.getId());
            // remove this disk in all domain that were sent
            for (Guid domain : getParameters().getStorageDomainsList()) {
                ImagesContainterParametersBase tempVar = new ImagesContainterParametersBase(template.getit_guid(),
                        disk.getinternal_drive_mapping(), getVmTemplateId());
                tempVar.setStorageDomainId(domain);
                tempVar.setStoragePoolId(disk.getstorage_pool_id().getValue());
                tempVar.setImageGroupID(disk.getimage_group_id().getValue());
                tempVar.setEntityId(getParameters().getEntityId());
                tempVar.setWipeAfterDelete(disk.getwipe_after_delete());
                tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
                VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                        VdcActionType.RemoveTemplateSnapshot, tempVar);

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
                    changeStorageInImagesTable = domain.equals(disk.getstorage_id());
                }

                DbFacade.getInstance().getStorageDomainDAO().removeImageGroupStorageDomainMap(
                        new image_group_storage_domain_map(disk.getimage_group_id().getValue(), domain));
                noImagesRemovedYet = false;
            }

            // remove images from db only if removing template completely
            if (getParameters().isRemoveTemplateFromDb()) {
                DiskImage diskImage = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(template.getId());
                if (diskImage != null) {
                    DbFacade.getInstance().getDiskDao().remove(diskImage.getimage_group_id());
                }

                DbFacade.getInstance().getDiskImageDAO().remove(template.getId());
                DbFacade.getInstance().getDiskImageTemplateDAO().remove(template.getit_guid());
            }
            else {
                // change the domain saved in images table to another one
                // assuming there is one since isRemoveTemplateFromDb = false
                if (changeStorageInImagesTable) {
                    // get all other domains that still has this images
                    List<image_group_storage_domain_map> imageDomainsMap = DbFacade.getInstance().getStorageDomainDAO().getAllImageGroupStorageDomainMapsForImage(disk.getimage_group_id().getValue());
                    image_group_storage_domain_map domainForImages = imageDomainsMap.get(0);

                    disk.setstorage_id(domainForImages.getstorage_domain_id());
                    // update images table
                    DbFacade.getInstance().getDiskImageDAO().update(disk);
                    // remove from map
                    DbFacade.getInstance().getStorageDomainDAO().removeImageGroupStorageDomainMap(domainForImages);
                }
            }
        }
        setSucceeded(true);
    }

    private static LogCompat log = LogFactoryCompat.getLog(RemoveAllVmTemplateImageTemplatesCommand.class);
}
