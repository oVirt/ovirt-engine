package org.ovirt.engine.core.bll.exportimport;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CreateOvaCommand;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateOvaParameters;
import org.ovirt.engine.core.common.action.ExportOvaParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.dao.VmDeviceDao;

public abstract class ExportOvaCommand<T extends ExportOvaParameters> extends CommandBase<T> {

    @Inject
    private AnsibleExecutor ansibleExecutor;
    @Inject
    private VmDeviceDao vmDeviceDao;

    public ExportOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        if (getEntity() == null) {
            return;
        }
        String path = getParameters().getDirectory();
        if (path != null && path.endsWith("/")) {
            getParameters().setDirectory(path.substring(0, path.length()-1));
        }
        if (StringUtils.isEmpty(getParameters().getName())) {
            getParameters().setName(String.format("%s.ova", getEntity().getName()));
        }
        setVdsId(getParameters().getProxyHostId());
    }

    @Override
    protected boolean validate() {
        if (getParameters().getProxyHostId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROXY_HOST_MUST_BE_SPECIFIED);
        }

        HostValidator hostValidator = HostValidator.createInstance(getVds());
        if (!validate(hostValidator.hostExists())) {
            return false;
        }

        if (!validate(hostValidator.isUp())) {
            return false;
        }

        StoragePoolValidator spValidator = new StoragePoolValidator(getStoragePool());
        if (!validate(spValidator.exists())) {
            return false;
        }

        if (!validate(spValidator.isInStatus(StoragePoolStatus.Up))) {
            return false;
        }

        if (!getStoragePoolId().equals(getVds().getStoragePoolId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROXY_HOST_NOT_IN_VM_DATA_CENTER);
        }

        if (!validate(validateTargetFolder())) {
            return false;
        }

        return true;
    }

    private ValidationResult validateTargetFolder() {
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .hosts(getVds())
                .variable("target_directory", getParameters().getDirectory())
                .variable("validate_only", "True")
                // /var/log/ovirt-engine/ova/ovirt-export-ova-validate-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(CreateOvaCommand.CREATE_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-export-ova-validate-ansible")
                .logFileName(getVds().getHostName())
                .logFileSuffix(getCorrelationId())
                .playAction("Export OVA")
                .playbook(AnsibleConstants.EXPORT_OVA_PLAYBOOK);

        boolean succeeded = ansibleExecutor.runCommand(commandConfig).getAnsibleReturnCode() == AnsibleReturnCode.OK;
        return succeeded ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INVALID_OVA_DESTINATION_FOLDER,
                        String.format("$vdsName %s", getVdsName()),
                        String.format("$directory %s", getParameters().getDirectory()));
    }

    protected void createOva() {
        ActionReturnValue returnValue = runInternalAction(ActionType.CreateOva,
                buildCreateOvaParameters(),
                createOvaCreationStepContext());

        if (!returnValue.getSucceeded()) {
            log.error("Failed to create OVA file");
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }
    }

    private CreateOvaParameters buildCreateOvaParameters() {
        CreateOvaParameters parameters = new CreateOvaParameters();
        parameters.setEntityType(getParameters().getEntityType());
        parameters.setEntityId(getParameters().getEntityId());
        parameters.setDisks(getDisks());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setDirectory(getParameters().getDirectory());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setName(getParameters().getName());
        return parameters;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        Map<String, String> jobProperties = super.getJobMessageProperties();
        jobProperties.put("ovapath", getOvaPath());
        jobProperties.put(VdcObjectType.VDS.name().toLowerCase(), getVdsName());
        return jobProperties;
    }

    /**
     * Used for the execution job
     */
    public String getOvaPath() {
        return String.format("%s/%s", getParameters().getDirectory(), getParameters().getName());
    }

    protected abstract Nameable getEntity();
    protected abstract CommandContext createOvaCreationStepContext();
    protected abstract List<DiskImage> getDisks();
}
