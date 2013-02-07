package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation=true)
public class AddImageFromScratchCommand<T extends AddImageFromScratchParameters> extends CreateSnapshotCommand<T> {

    public AddImageFromScratchCommand(T parameters) {
        super(parameters);
        setVmId(getParameters().getMasterVmId());
    }

    protected AddImageFromScratchCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        setImageGroupId(getParameters().getDiskInfo().getId());
        setDestinationImageId(Guid.NewGuid());

        mNewCreatedDiskImage = new DiskImage();
        mNewCreatedDiskImage.setImageId(getDestinationImageId());
        mNewCreatedDiskImage.setBoot(getParameters().getDiskInfo().isBoot());
        mNewCreatedDiskImage.setDiskInterface(getParameters().getDiskInfo().getDiskInterface());
        mNewCreatedDiskImage.setPropagateErrors(getParameters().getDiskInfo().getPropagateErrors());
        mNewCreatedDiskImage.setWipeAfterDelete(getParameters().getDiskInfo().isWipeAfterDelete());
        mNewCreatedDiskImage.setDiskAlias(getParameters().getDiskInfo().getDiskAlias());
        mNewCreatedDiskImage.setDiskDescription(getParameters().getDiskInfo().getDiskDescription());
        mNewCreatedDiskImage.setShareable(getParameters().getDiskInfo().isShareable());
        mNewCreatedDiskImage.setId(getImageGroupId());
        mNewCreatedDiskImage.setstorage_pool_id(getParameters().getStoragePoolId());
        mNewCreatedDiskImage.setstorage_ids(new ArrayList<Guid>(Arrays.asList(getParameters().getStorageDomainId())));
        mNewCreatedDiskImage.setsize(getParameters().getDiskInfo().getsize());
        mNewCreatedDiskImage.setvolume_type(getParameters().getDiskInfo().getvolume_type());
        mNewCreatedDiskImage.setvolume_format(getParameters().getDiskInfo().getvolume_format());
        mNewCreatedDiskImage.setdescription("");
        mNewCreatedDiskImage.setcreation_date(new Date());
        mNewCreatedDiskImage.setlastModified(new Date());
        mNewCreatedDiskImage.setactive(true);
        mNewCreatedDiskImage.setImageStatus(ImageStatus.LOCKED);
        mNewCreatedDiskImage.setvm_snapshot_id(getParameters().getVmSnapshotId());
        mNewCreatedDiskImage.setQuotaId(getParameters().getQuotaId());

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                addDiskImageToDb(mNewCreatedDiskImage, getCompensationContext());
                return null;
            }
        });
        freeLock();
        processImageInIrs();
        getReturnValue().setActionReturnValue(mNewCreatedDiskImage);
        setSucceeded(true);
    }

    protected boolean processImageInIrs() {
        VDSReturnValue vdsReturnValue = runVdsCommand(
                        VDSCommandType.CreateImage,
                        new CreateImageVDSCommandParameters(getParameters().getStoragePoolId(), getParameters()
                                .getStorageDomainId(), getImageGroupId(), getParameters().getDiskInfo().getsize(),
                                getParameters().getDiskInfo().getvolume_type(), getParameters().getDiskInfo()
                                        .getvolume_format(), getDestinationImageId(), "", getStoragePool()
                                        .getcompatibility_version().toString()));
        if (vdsReturnValue.getSucceeded()) {
            getParameters().setTaskIds(new ArrayList<Guid>());
            getParameters().getTaskIds().add(
                    createTask(vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));
            getReturnValue().getInternalTaskIdList().add(getParameters().getTaskIds().get(0));

            return true;
        }

        return false;
    }

    @Override
    protected DiskImage getImage() {
        return null;
    }

    @Override
    protected void endWithFailure() {
        if (getDestinationDiskImage() != null) {
            DbFacade.getInstance().getDiskImageDynamicDao().remove(getDestinationDiskImage().getImageId());
        }
        super.endWithFailure();
    }
}
