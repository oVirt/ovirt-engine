package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.CreateSnapshotFromTemplateParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.IImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This command responsible to creating new snapshot. Usually it will be called
 * during new vm creation. In the case of create snapshot from template new
 * image created from master image aka image template so new created image
 * it_guid will be equal to master image guid.
 *
 * Parameters: Guid imageId - id of ImageTemplate, snapshot will be created from
 * Guid containerId - id of VmTemplate, contains ImageTemplate
 */

@InternalCommandAttribute
public class CreateSnapshotFromTemplateCommand<T extends CreateSnapshotFromTemplateParameters> extends
        CreateSnapshotCommand<T> {
    private DiskImageTemplate mTemplate;

    /**
     * Image in this command is ImageTemplate
     */
    @Override
    protected IImage getImage() {
        switch (getActionState()) {
        case EXECUTE:
            if (mTemplate == null) {
                VM vm = DbFacade.getInstance().getVmDAO().getById(getImageContainerId());
                mTemplate =
                        DbFacade.getInstance()
                                .getDiskImageTemplateDAO()
                                .getByVmTemplateAndId(vm.getvmt_guid(), getImageId());
            }
            return mTemplate;

            // in case of EndAction, the base definition of Image is the
            // correct one:
        default:
            return super.getImage();
        }
    }

    public CreateSnapshotFromTemplateCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getVmId());
        super.setImageContainerId(parameters.getVmId());
        setImageGroupId(Guid.NewGuid());
    }

    /**
     * Old image not have to be changed
     */
    @Override
    protected void ProcessOldImageFromDb() {
    }

    @Override
    protected DiskImage CloneDiskImage(Guid newImageGuid) {
        DiskImage returnValue = super.CloneDiskImage(newImageGuid);
        returnValue.setit_guid(getImage().getId());
        return returnValue;
    }

    @Override
    protected Guid getDestinationStorageDomainId() {
        Guid storageDomainId = ((CreateSnapshotFromTemplateParameters) getParameters()).getStorageDomainId();
        storageDomainId = (storageDomainId == null) ? Guid.Empty : storageDomainId;
        return (!storageDomainId.equals(Guid.Empty)) ? storageDomainId : super.getDestinationStorageDomainId();
    }

    @Override
    protected void executeCommand() {
        setDiskImage(DbFacade.getInstance().getDiskImageDAO().getSnapshotById(getImage().getId()));
        super.executeCommand();
    }

    @Override
    protected void EndWithFailure() {
        if (getDestinationDiskImage() != null) {
            DbFacade.getInstance().getDiskDao().remove(getDestinationDiskImage().getimage_group_id());
            if (DbFacade.getInstance().getDiskImageDynamicDAO().get(getDestinationDiskImage().getId()) != null) {
                DbFacade.getInstance().getDiskImageDynamicDAO().remove(getDestinationDiskImage().getId());
            }
        }

        super.EndWithFailure();
    }
}
