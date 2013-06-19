package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.provider.OpenstackImageProviderProxy;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.vdscommands.DownloadImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.compat.Guid;


public class ImportRepoImageCopyTaskHandler
        extends AbstractSPMAsyncTaskHandler<TaskHandlerCommand<? extends ImportRepoImageParameters>> {

    private OpenstackImageProviderProxy providerProxy;

    public ImportRepoImageCopyTaskHandler(TaskHandlerCommand<? extends ImportRepoImageParameters> cmd) {
        super(cmd);
    }

    @Override
    protected void beforeTask() {
    }

    @Override
    protected VDSCommandType getVDSCommandType() {
        return VDSCommandType.DownloadImage;
    }

    @Override
    public AsyncTaskType getTaskType() {
        return AsyncTaskType.copyImage;
    }

    protected OpenstackImageProviderProxy getProviderProxy() {
        if (providerProxy == null) {
            providerProxy = OpenstackImageProviderProxy
                    .getFromStorageDomainId(getEnclosingCommand().getParameters().getSourceStorageDomainId());
        }
        return providerProxy;
    }

    @Override
    protected VDSParametersBase getVDSParameters() {
        return new DownloadImageVDSCommandParameters(
                getEnclosingCommand().getParameters().getStoragePoolId(),
                getEnclosingCommand().getParameters().getStorageDomainId(),
                getEnclosingCommand().getParameters().getImageGroupID(),
                getEnclosingCommand().getParameters().getDestinationImageId(),
                new HttpLocationInfo(
                        getProviderProxy().getImageUrl(getEnclosingCommand().getParameters().getSourceRepoImageId()),
                        getProviderProxy().getDownloadHeaders())
        );
    }

    @Override
    protected VdcObjectType getTaskObjectType() {
        return VdcObjectType.Disk;
    }

    @Override
    protected Guid[] getTaskObjects() {
        return new Guid[] { getEnclosingCommand().getParameters().getDestinationImageId() };
    }

    @Override
    protected void revertTask() {
        // nothing to do
    }

    @Override
    public void endSuccessfully() {
        super.endSuccessfully();
        getEnclosingCommand().getParameters().getDiskImage().setImageStatus(ImageStatus.OK);
        ImagesHandler.updateImageStatus(
                getEnclosingCommand().getParameters().getDiskImage().getImageId(), ImageStatus.OK);
    }

    @Override
    public void endWithFailure() {
        super.endWithFailure();
        getEnclosingCommand().getParameters().getDiskImage().setImageStatus(ImageStatus.ILLEGAL);
        ImagesHandler.updateImageStatus(
                getEnclosingCommand().getParameters().getDiskImage().getImageId(), ImageStatus.ILLEGAL);
    }

    @Override
    protected VDSCommandType getRevertVDSCommandType() {
        return null; // nothing to do
    }

    @Override
    public AsyncTaskType getRevertTaskType() {
        return null; // nothing to do
    }

    @Override
    protected VDSParametersBase getRevertVDSParameters() {
        return null; // nothing to do
    }

}
