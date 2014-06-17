package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.provider.OpenStackImageProviderProxy;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
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
                getEnclosingCommand().getParameters().getDiskImage().getImageId(), ImageStatus.OK);
        getEnclosingCommand().getReturnValue().setSucceeded(true);
    }

    // No need for null checks, as we already tested that the cluster exists in the canDoAction of the parent command
    public VDSGroup getVdsGroup(Guid vdsGroupId) {
        return DbFacade.getInstance().getVdsGroupDao().get(vdsGroupId);
    }

    private Guid createTemplate() {

        VmTemplate blankTemplate = DbFacade.getInstance().getVmTemplateDao().get(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        VmStatic masterVm = new VmStatic(blankTemplate);
        OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

        DiskImage templateDiskImage = getEnclosingCommand().getParameters().getDiskImage();
        // Following the same convention as the glance disk name, using a GlanceTemplate prefix, followed by a short identifier
        String vmTemplateName = "GlanceTemplate-" + Guid.newGuid().toString().substring(0, 7);
        AddVmTemplateParameters parameters = new AddVmTemplateParameters(masterVm, vmTemplateName, templateDiskImage.getDiskDescription());

        // Setting the user from the parent command, as the session might already be invalid
        parameters.setParametersCurrentUser(getEnclosingCommand().getParameters().getParametersCurrentUser());

        // Setting the cluster ID, and other related properties derived from it
        if (getEnclosingCommand().getParameters().getClusterId() != null) {
            masterVm.setVdsGroupId(getEnclosingCommand().getParameters().getClusterId());
            VDSGroup vdsGroup = getVdsGroup(masterVm.getVdsGroupId());
            masterVm.setOsId(osRepository.getDefaultOSes().get(vdsGroup.getArchitecture()));
            DisplayType defaultDisplayType =
                    osRepository.getDisplayTypes(masterVm.getOsId(), vdsGroup.getcompatibility_version()).get(0);
            masterVm.setDefaultDisplayType(defaultDisplayType);
        }

        VdcReturnValueBase addVmTemplateReturnValue =
                Backend.getInstance().runInternalAction(VdcActionType.AddVmTemplate,
                        parameters,
                        ExecutionHandler.createDefaultContextForTasks(getEnclosingCommand().getContext()));

        // No reason for this to return null, but checking just to make sure, and returning the created template, or null if failed
        return addVmTemplateReturnValue.getActionReturnValue() != null ? (Guid) addVmTemplateReturnValue.getActionReturnValue() : null;
    }

    private void attachDiskToTemplate(Guid templateId) {
        DiskImage templateDiskImage = getEnclosingCommand().getParameters().getDiskImage();
        VmDeviceUtils.addManagedDevice(new VmDeviceId(templateDiskImage.getId(),
                templateId),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK,
                null,
                true,
                Boolean.FALSE,
                null);
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
