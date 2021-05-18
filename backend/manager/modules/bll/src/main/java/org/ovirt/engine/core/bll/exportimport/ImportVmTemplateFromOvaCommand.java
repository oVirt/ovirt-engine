package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute.CommandCompensationPhase;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromOvaParameters.Phase;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDao;

@NonTransactiveCommandAttribute(forceCompensation = true, compensationPhase = CommandCompensationPhase.END_COMMAND)
public class ImportVmTemplateFromOvaCommand<T extends ImportVmTemplateFromOvaParameters> extends ImportVmTemplateCommandBase<T>
        implements SerialChildExecutingCommand {

    @Inject
    private VdsDao vdsDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public ImportVmTemplateFromOvaCommand(Guid commandId) {
        super(commandId);
    }

    public ImportVmTemplateFromOvaCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (getParameters().getProxyHostId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROXY_HOST_MUST_BE_SPECIFIED);
        }

        return super.validate();
    }

    @Override
    protected void initImportClonedTemplateDisks() {
        for (DiskImage image : getImages()) {
            if (getParameters().isImportAsNewEntity()) {
                generateNewDiskId(image);
            } else {
                originalDiskIdMap.put(image.getId(), image.getId());
                originalDiskImageIdMap.put(image.getId(), image.getImageId());
            }
        }
    }

    protected Guid createDisk(DiskImage image) {
        ActionReturnValue actionReturnValue = runInternalActionWithTasksContext(
                ActionType.AddDiskToTemplate,
                buildAddDiskParameters(image));

        if (!actionReturnValue.getSucceeded()) {
            throw new EngineException(actionReturnValue.getFault().getError(),
                    "Failed to create disk!");
        }

        getTaskIdList().addAll(actionReturnValue.getInternalVdsmTaskIdList());
        return actionReturnValue.getActionReturnValue();
    }

    protected AddDiskParameters buildAddDiskParameters(DiskImage image) {
        AddDiskParameters diskParameters = new AddDiskParameters(image.getDiskVmElementForVm(getVmTemplateId()), image);
        Guid originalId = getOriginalDiskIdMap(image.getId());
        diskParameters.setStorageDomainId(getParameters().getImageToDestinationDomainMap().get(originalId));
        diskParameters.setParentCommand(getActionType());
        diskParameters.setParentParameters(getParameters());
        diskParameters.setShouldRemainIllegalOnFailedExecution(true);
        diskParameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);

        diskParameters.setUsePassedDiskId(true);
        diskParameters.setUsePassedImageId(true);
        return diskParameters;
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        switch(getParameters().getImportPhase()) {
        case CREATE_DISKS:
            getParameters().setImportPhase(Phase.CONVERT);
            if (getParameters().getProxyHostId() == null) {
                getParameters().setProxyHostId(selectProxyHost());
            }
            break;

        case CONVERT:
            return false;

        default:
        }

        persistCommandIfNeeded();
        executeNextOperation();
        return true;
    }

    @SuppressWarnings("incomplete-switch")
    private void executeNextOperation() {
        switch (getParameters().getImportPhase()) {
        case CONVERT:
            convert();
            break;
        }
    }

    private Guid selectProxyHost() {
        Iterator<VDS> activeHostsIterator = vdsDao.getAllForStoragePoolAndStatus(getStoragePoolId(), VDSStatus.Up).iterator();
        return activeHostsIterator.hasNext() ? activeHostsIterator.next().getId() : null;
    }

    @Override
    protected void endWithFailure() {
        removeVmImages();
        setSucceeded(true);
    }

    protected void removeVmImages() {
        runInternalAction(ActionType.RemoveAllVmImages,
                buildRemoveAllVmImagesParameters(),
                cloneContextAndDetachFromParent());
    }

    private RemoveAllVmImagesParameters buildRemoveAllVmImagesParameters() {
        return new RemoveAllVmImagesParameters(
                getVmTemplateId(),
                diskDao.getAllForVm(getVmTemplateId()).stream().map(DiskImage.class::cast).collect(Collectors.toList()));
    }

    @Override
    protected boolean validateSourceStorageDomain() {
        getParameters().setImages(getVmTemplate().getDiskList());
        getParameters().getImages().forEach(image -> image.setStorageIds(new ArrayList<>(
                Collections.singleton(getParameters().getImageToDestinationDomainMap().get(image.getId())))));
        return true;
    }

    @Override
    protected void addDisksToDb() {
        // we cannot trigger AddDiskToTemplate here because we're inside a transaction
        // so the disks would be added to DB and attached as part of copyImagesToTargetDomain
    }

    @Override
    protected void copyImagesToTargetDomain() {
        getImages().stream().map(this::adjustDisk).forEach(this::createDisk);
        getParameters().setDiskMappings(getImageMappings());
    }

    protected DiskImage adjustDisk(DiskImage image) {
        image.setVolumeFormat(VolumeFormat.COW);
        image.setVolumeType(VolumeType.Sparse);
        image.setDiskVmElements(image.getDiskVmElements().stream()
                .map(dve -> DiskVmElement.copyOf(dve, image.getId(), getVmTemplateId()))
                .collect(Collectors.toList()));
        return image;
    }

    protected Map<Guid, Guid> getImageMappings() {
        return getImages().stream().collect(Collectors.toMap(
                DiskImage::getImageId,
                d -> getOriginalDiskImageIdMap(d.getId())));
    }

    private void convert() {
        runInternalAction(ActionType.ExtractOva,
                buildExtractOvaParameters(),
                createConversionStepContext(StepEnum.EXTRACTING_OVA));
    }

    private ConvertOvaParameters buildExtractOvaParameters() {
        ConvertOvaParameters parameters = new ConvertOvaParameters(getVmTemplateId());
        parameters.setOvaPath(getParameters().getOvaPath());
        parameters.setVmName(getVmTemplateName());
        parameters.setDisks(getVmTemplate().getDiskList());
        parameters.setImageMappings(getParameters().getImageMappings());
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setClusterId(getClusterId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setVmEntityType(VmEntityType.TEMPLATE);
        return parameters;
    }

    protected CommandContext createConversionStepContext(StepEnum step) {
        CommandContext commandCtx = null;

        try {
            Map<String, String> values = Collections.singletonMap(VdcObjectType.VmTemplate.name().toLowerCase(),
                    getVmTemplateName());

            Step conversionStep = executionHandler.addSubStep(getExecutionContext(),
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                    step,
                    ExecutionMessageDirector.resolveStepMessage(step, values));

            ExecutionContext ctx = new ExecutionContext();
            ctx.setStep(conversionStep);
            ctx.setMonitored(true);

            commandCtx = cloneContext().withoutCompensationContext().withExecutionContext(ctx).withoutLock();

        } catch (RuntimeException e) {
            log.error("Failed to create command context of converting template '{}': {}",
                    getVmTemplateName(), e.getMessage());
            log.debug("Exception", e);
        }

        return commandCtx;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
