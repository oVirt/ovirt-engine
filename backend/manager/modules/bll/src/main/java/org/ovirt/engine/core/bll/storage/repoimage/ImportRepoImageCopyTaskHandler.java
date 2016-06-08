package org.ovirt.engine.core.bll.storage.repoimage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.bll.storage.AbstractSPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.vdscommands.DownloadImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class ImportRepoImageCopyTaskHandler
        extends AbstractSPMAsyncTaskHandler<TaskHandlerCommand<? extends ImportRepoImageParameters>> {

    private OpenStackImageProviderProxy providerProxy;

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
        return AsyncTaskType.downloadImage;
    }

    protected OpenStackImageProviderProxy getProviderProxy() {
        if (providerProxy == null) {
            providerProxy = OpenStackImageProviderProxy
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
        if (getEnclosingCommand().getParameters().getImportAsTemplate()) {
            Guid newTemplateId = createTemplate();
            // No reason for this to happen, but checking just to make sure
            if (newTemplateId != null) {
                attachDiskToTemplate(newTemplateId);
            }
        }
        getEnclosingCommand().getParameters().getDiskImage().setImageStatus(ImageStatus.OK);
        ImagesHandler.updateImageStatus(
                getEnclosingCommand().getParameters().getDestinationImageId(), ImageStatus.OK);
        getEnclosingCommand().taskEndSuccessfully();
        getEnclosingCommand().getReturnValue().setSucceeded(true);
    }

    // No need for null checks, as we already tested that the cluster exists in the validate of the parent command
    public Cluster getCluster(Guid clusterId) {
        return DbFacade.getInstance().getClusterDao().get(clusterId);
    }

    private Guid createTemplate() {

        VmTemplate blankTemplate = DbFacade.getInstance().getVmTemplateDao().get(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        VmStatic masterVm = new VmStatic(blankTemplate);
        OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);

        DiskImage templateDiskImage = getEnclosingCommand().getParameters().getDiskImage();
        String vmTemplateName = getEnclosingCommand().getParameters().getTemplateName();
        AddVmTemplateParameters parameters = new AddVmTemplateParameters(masterVm, vmTemplateName, templateDiskImage.getDiskDescription());

        // Setting the user from the parent command, as the session might already be invalid
        parameters.setParametersCurrentUser(getEnclosingCommand().getParameters().getParametersCurrentUser());

        // Setting the cluster ID, and other related properties derived from it
        if (getEnclosingCommand().getParameters().getClusterId() != null) {
            masterVm.setClusterId(getEnclosingCommand().getParameters().getClusterId());
            Cluster cluster = getCluster(masterVm.getClusterId());
            masterVm.setOsId(osRepository.getDefaultOSes().get(cluster.getArchitecture()));
            Pair<GraphicsType, DisplayType> defaultDisplayType =
                    osRepository.getGraphicsAndDisplays(masterVm.getOsId(), cluster.getCompatibilityVersion()).get(0);
            masterVm.setDefaultDisplayType(defaultDisplayType.getSecond());
        }


        parameters.setBalloonEnabled(true);
        VdcReturnValueBase addVmTemplateReturnValue =
                Backend.getInstance().runInternalAction(VdcActionType.AddVmTemplate,
                        parameters,
                        ExecutionHandler.createDefaultContextForTasks(getEnclosingCommand().getContext()));

        // No reason for this to return null, but checking just to make sure, and returning the created template, or null if failed
        return addVmTemplateReturnValue.getActionReturnValue() != null ? (Guid) addVmTemplateReturnValue.getActionReturnValue() : null;
    }

    private void attachDiskToTemplate(Guid templateId) {
        DiskImage templateDiskImage = getEnclosingCommand().getParameters().getDiskImage();
        DiskVmElement dve = new DiskVmElement(templateDiskImage.getId(), templateId);
        dve.setDiskInterface(DiskInterface.VirtIO);
        DbFacade.getInstance().getDiskVmElementDao().save(dve);
        VmDeviceUtils.addDiskDevice(templateId, templateDiskImage.getId());
    }

    @Override
    public void endWithFailure() {
        super.endWithFailure();
        getEnclosingCommand().getParameters().getDiskImage().setImageStatus(ImageStatus.ILLEGAL);
        ImagesHandler.updateImageStatus(
                getEnclosingCommand().getParameters().getDestinationImageId(), ImageStatus.ILLEGAL);
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
