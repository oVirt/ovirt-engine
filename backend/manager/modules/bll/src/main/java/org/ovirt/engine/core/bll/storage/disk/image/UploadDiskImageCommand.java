package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.CommandHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.UploadDiskImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.vdscommands.ImageActionsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.PrepareImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturnForXmlRpc;

@NonTransactiveCommandAttribute
public class UploadDiskImageCommand<T extends UploadDiskImageParameters> extends UploadImageCommand<T> implements QuotaStorageDependent {

    private static final String FILE_URL_SCHEME = "file://";

    public UploadDiskImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        VdcReturnValueBase returnValue = CommandHelper.canDoAction(VdcActionType.AddDisk, getAddDiskParameters(),
                getContext().clone().getExecutionContext(), true);
        getReturnValue().setValidationMessages(returnValue.getValidationMessages());
        return returnValue.isValid();
    }

    @Override
    protected void createImage() {
        CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.AddDisk, getAddDiskParameters(), cloneContextAndDetachFromParent());
        persistCommand(getParameters().getParentCommand(), true);
    }

    @Override
    protected String prepareImage(Guid vdsId) {
        VDSReturnValue vdsRetVal = runVdsCommand(VDSCommandType.PrepareImage,
                    getPrepareParameters(vdsId));
        return FILE_URL_SCHEME + ((PrepareImageReturnForXmlRpc) vdsRetVal.getReturnValue()).getImagePath();
    }

    private PrepareImageVDSCommandParameters getPrepareParameters(Guid vdsId) {
        return new PrepareImageVDSCommandParameters(vdsId,
                getStoragePool().getId(),
                getStorageDomainId(),
                getImage().getImage().getDiskId(),
                getImage().getImageId(), true);
    }

    @Override
    protected void tearDownImage(Guid vdsId) {
        VDSReturnValue vdsRetVal = runVdsCommand(VDSCommandType.TeardownImage,
                getImageActionsParameters(vdsId));
        if (!vdsRetVal.getSucceeded()) {
            DiskImage image = (DiskImage) getDiskDao().get(getParameters().getImageId());
            log.warn("Failed to tear down image '{}' for image transfer session: {}",
                    image, vdsRetVal.getVdsError());

            // Invoke log method directly rather than relying on infra, because teardown
            // failure may occur during command execution, e.g. if the upload is paused.
            addCustomValue("DiskAlias", image != null ? image.getDiskAlias() : "(unknown)");
            auditLogDirector.log(this, AuditLogType.UPLOAD_IMAGE_TEARDOWN_FAILED);
        }
    }

    private AddDiskParameters getAddDiskParameters() {
        AddDiskParameters diskParameters = getParameters().getAddDiskParameters();
        diskParameters.setParentCommand(getActionType());
        diskParameters.setParentParameters(getParameters());
        diskParameters.setShouldRemainIllegalOnFailedExecution(true);
        diskParameters.setSkipDomainCheck(true);
        return diskParameters;
    }

    protected ImageActionsVDSCommandParameters getImageActionsParameters(Guid vdsId) {
        return new ImageActionsVDSCommandParameters(vdsId,
                getStoragePool().getId(),
                getStorageDomainId(),
                getImage().getImage().getDiskId(),
                getImage().getImageId());
    }

    @Override
    protected String getImageAlias() {
        return getParameters().getAddDiskParameters().getDiskInfo().getDiskAlias();
    }

    @Override
    protected String getUploadType() {
        return "disk";
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        AddDiskParameters parameters = getAddDiskParameters();
        list.add(new QuotaStorageConsumptionParameter(
                ((DiskImage) parameters.getDiskInfo()).getQuotaId(),
                null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getStorageDomainId(),
                (double) parameters.getDiskInfo().getSize() / SizeConverter.BYTES_IN_GB));

        return list;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> listPermissionSubjects = new ArrayList<>();
        listPermissionSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                    VdcObjectType.Storage,
                    ActionGroup.CREATE_DISK));

        return listPermissionSubjects;
    }
}
