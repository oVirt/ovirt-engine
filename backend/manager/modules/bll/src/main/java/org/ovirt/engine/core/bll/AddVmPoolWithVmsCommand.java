package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends CommonVmPoolWithVmsCommand<T>
        implements QuotaVdsDependent {

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected AddVmPoolWithVmsCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmPoolWithVmsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getParameters().getVmStaticData().getVmtGuid())) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_CREATE_FROM_BLANK_TEMPLATE);
        }

        if (getParameters().getVmLargeIcon() != null && !validate(IconValidator.validate(
                IconValidator.DimensionsType.LARGE_CUSTOM_ICON,
                getParameters().getVmLargeIcon()))) {
            return false;
        }

        return true;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getVmPool().getName(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_POOL_NAME, getVmPoolIsBeingCreatedMessage()));
    }

    private String getVmPoolIsBeingCreatedMessage() {
        StringBuilder builder = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_VM_POOL_IS_BEING_CREATED.name());
        if (getVmPoolName() != null) {
            builder.append(String.format("$VmPoolName %1$s", getVmPoolName()));
        }
        return builder.toString();
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
    }

    @Override
    protected Guid getPoolId() {
        VmPool vmPool = getVmPool();

        getVmPoolDao().save(vmPool);

        return vmPool.getVmPoolId();
    }

    @Override
    protected void onNoVmsAdded(Guid poolId) {
        getVmPoolDao().remove(poolId);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (isAddVmsSucceded()) {
            return AuditLogType.USER_ADD_VM_POOL_WITH_VMS;
        }

        return getSucceeded() ? AuditLogType.USER_ADD_VM_POOL_WITH_VMS_ADD_VDS_FAILED
                : AuditLogType.USER_ADD_VM_POOL_WITH_VMS_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getVmStaticData().getClusterId(),
                VdcObjectType.Cluster,
                getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getVmTemplateId(), VdcObjectType.VmTemplate,
                                                 ActionGroup.CREATE_VM));

        return permissionList;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<>();
            VmPool vmPool = getParameters().getVmPool();
            String vmPoolName = vmPool == null ? StringUtils.EMPTY : vmPool.getName();
            jobProperties.put(VdcObjectType.VmPool.name().toLowerCase(), vmPoolName);
            Guid vmTemplateId = getVmTemplateId();
            String templateName = getVmTemplateName();
            if (StringUtils.isEmpty(templateName)) {
                templateName = vmTemplateId == null ? StringUtils.EMPTY : vmTemplateId.toString();
            }
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(), templateName);
        }
        return jobProperties;
    }

    private Guid getQuotaId() {
        return getParameters().getVmStaticData().getQuotaId();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        return Arrays.<QuotaConsumptionParameter>asList(new QuotaSanityParameter(getQuotaId(), null));
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

}
