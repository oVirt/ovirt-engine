package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute.CommandCompensationPhase;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromOvaParameters;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromOvaParameters.Phase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineFault;
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
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;
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
            log.error("AddDiskToTemplate failed importing template '{}' ({}), disk alias '{}': {}",
                    getVmTemplateName(),
                    getVmTemplateId(),
                    image.getDiskAlias(),
                    formatActionReturnFailure(actionReturnValue));
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
        switch (getParameters().getImportPhase()) {
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
        log.error("ImportVmTemplateFromOva failed for template '{}' ({}): {}",
                getVmTemplateName(),
                getVmTemplateId(),
                formatActionReturnFailure(getReturnValue()));
        removeVmImages();
        setSucceeded(true);
    }

    protected void removeVmImages() {
        OvaImportManagedBlockSupport.removeDisksAfterFailedOvaImport(
                getVmTemplateId(),
                diskDao.getAllForVm(getVmTemplateId()).stream().map(DiskImage.class::cast).collect(Collectors.toList()),
                cloneContextAndDetachFromParent(),
                image -> OvaImportManagedBlockSupport.buildRemoveManagedBlockDiskParameters(
                        image,
                        isExecutedAsChildCommand(),
                        getActionType(),
                        getParameters()),
                (at, p, c) -> runInternalAction(at, p, c));
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
        List<Guid> createdDiskIds = new ArrayList<>();
        getImages().stream().map(this::adjustDisk).forEach(img -> createdDiskIds.add(createDisk(img)));
        getParameters().setTemplateDiskIdsForOvaExtract(createdDiskIds);
        getParameters().setDiskMappings(getImageMappings());
    }

    protected DiskImage adjustDisk(DiskImage image) {
        if (OvaImportManagedBlockSupport.diskTargetsManagedBlockStorage(
                image,
                diskId -> {
                    Guid k = getOriginalDiskIdMap(diskId);
                    return k != null ? k : diskId;
                },
                getParameters().getImageToDestinationDomainMap(),
                getStoragePoolId(),
                storageDomainDao)) {
            image.setVolumeFormat(VolumeFormat.RAW);
            image.setVolumeType(VolumeType.Preallocated);
            image.setActualSizeInBytes(image.getSize());
            image.setBackup(DiskBackup.None);
        } else {
            image.setVolumeFormat(VolumeFormat.COW);
            image.setVolumeType(VolumeType.Sparse);
        }
        image.setDiskVmElements(image.getDiskVmElements().stream()
                .map(dve -> DiskVmElement.copyOf(dve, image.getId(), getVmTemplateId()))
                .collect(Collectors.toList()));
        return image;
    }

    private void attachManagedBlockVolumesToProxyHostForOvaConversion() {
        if (!OvaImportManagedBlockSupport.hasAnyManagedBlockDestination(
                getParameters().getImageToDestinationDomainMap(),
                getStoragePoolId(),
                storageDomainDao)) {
            return;
        }
        VDS host = OvaImportManagedBlockSupport.resolveOvaProxyHost(getParameters().getProxyHostId(), vdsDao);
        if (host == null) {
            return;
        }
        vmTemplateHandler.updateDisksFromDb(getVmTemplate());
        OvaImportManagedBlockSupport.attachManagedBlockDisksOnProxy(
                managedBlockStorageCommandUtil,
                getVmTemplate().getDiskList(),
                host,
                getVmTemplateId());
    }

    private Map<Guid, Map<String, Object>> preAttachedManagedBlockDeviceMapForOvaChildCommands() {
        if (!OvaImportManagedBlockSupport.hasAnyManagedBlockDestination(
                getParameters().getImageToDestinationDomainMap(),
                getStoragePoolId(),
                storageDomainDao)) {
            return null;
        }
        return OvaImportManagedBlockSupport.preAttachedManagedBlockDevicesByDiskId(getVmTemplate().getDiskList());
    }

    private Map<Guid, Guid> buildOvaSourceImageIdByDiskId() {
        return OvaImportManagedBlockSupport.ovaSourceImageIdByDiskId(
                getVmTemplate().getDiskList(),
                this::getOriginalDiskImageIdMap);
    }

    protected Map<Guid, Guid> getImageMappings() {
        return getImages().stream().collect(Collectors.toMap(
                DiskImage::getImageId,
                d -> getOriginalDiskImageIdMap(d.getId())));
    }

    private void convert() {
        vmTemplateHandler.updateDisksFromDb(getVmTemplate());
        attachManagedBlockVolumesToProxyHostForOvaConversion();
        ActionReturnValue extractRet = runInternalAction(ActionType.ExtractOva,
                buildExtractOvaParameters(),
                createConversionStepContext(StepEnum.EXTRACTING_OVA));
        if (extractRet != null && !extractRet.getSucceeded()) {
            log.error("ExtractOva failed for template '{}' ({}): {}",
                    getVmTemplateName(),
                    getVmTemplateId(),
                    formatActionReturnFailure(extractRet));
        }
    }

    private ConvertOvaParameters buildExtractOvaParameters() {
        ConvertOvaParameters parameters = new ConvertOvaParameters(getVmTemplateId());
        parameters.setOvaPath(getParameters().getOvaPath());
        parameters.setVmName(getVmTemplateName());
        parameters.setDisks(getVmTemplate().getDiskList());
        parameters.setImageMappings(getParameters().getImageMappings());
        parameters.setStoragePoolId(getStoragePoolId());
        Guid extractStorageDomainId = getStorageDomainId();
        if (Guid.isNullOrEmpty(extractStorageDomainId)) {
            Guid mbsDest = OvaImportManagedBlockSupport.firstManagedBlockDestinationDomainId(
                    getParameters().getImageToDestinationDomainMap(),
                    getStoragePoolId(),
                    storageDomainDao);
            if (!Guid.isNullOrEmpty(mbsDest)) {
                extractStorageDomainId = mbsDest;
            }
        }
        parameters.setStorageDomainId(extractStorageDomainId);
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setClusterId(getClusterId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setVmEntityType(VmEntityType.TEMPLATE);
        parameters.setPreAttachedManagedBlockDevicesByDiskId(preAttachedManagedBlockDeviceMapForOvaChildCommands());
        parameters.setOvaSourceImageIdByDiskId(buildOvaSourceImageIdByDiskId());
        parameters.setTemplateDiskIdsForExtract(getParameters().getTemplateDiskIdsForOvaExtract());
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
            log.error("Failed to create command context for ExtractOva (template '{}' {})",
                    getVmTemplateName(), getVmTemplateId(), e);
        }

        return commandCtx;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    private static String formatActionReturnFailure(ActionReturnValue rv) {
        if (rv == null) {
            return "null ActionReturnValue";
        }
        StringBuilder sb = new StringBuilder();
        EngineFault fault = rv.getFaultOrNull();
        if (fault != null) {
            sb.append(formatEngineFault(fault));
        } else {
            sb.append("(no fault set)");
        }
        appendNonEmpty(sb, "description", rv.getDescription());
        appendNonEmptyList(sb, "executeFailedMessages", rv.getExecuteFailedMessages());
        appendNonEmptyList(sb, "validationMessages", rv.getValidationMessages());
        return sb.toString();
    }

    private static void appendNonEmpty(StringBuilder sb, String label, String value) {
        if (StringUtils.isNotEmpty(value)) {
            sb.append(", ").append(label).append('=').append(value);
        }
    }

    private static void appendNonEmptyList(StringBuilder sb, String label, List<String> values) {
        if (CollectionUtils.isNotEmpty(values)) {
            sb.append(", ").append(label).append('=').append(values);
        }
    }

    private static String formatEngineFault(EngineFault fault) {
        if (fault == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        if (fault.getError() != null) {
            sb.append("error=").append(fault.getError());
        }
        if (fault.getMessage() != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("message=").append(fault.getMessage());
        }
        if (fault.getDetails() != null && !fault.getDetails().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("details=").append(fault.getDetails());
        }
        return sb.length() > 0 ? sb.toString() : "(empty fault)";
    }
}
