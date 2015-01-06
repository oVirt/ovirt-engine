package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends CommonVmPoolWithVmsCommand<T>
        implements QuotaVdsDependent {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddVmPoolWithVmsCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmPoolWithVmsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getParameters().getVmStaticData().getVmtGuid())) {
            return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_CREATE_FROM_BLANK_TEMPLATE);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
    }

    @Override
    protected Guid getPoolId() {
        VmPool vmPool = getVmPool();

        DbFacade.getInstance().getVmPoolDao().save(vmPool);

        return vmPool.getVmPoolId();
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
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getParameters().getVmStaticData().getVdsGroupId(),
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getVmTemplateId(), VdcObjectType.VmTemplate,
                                                 ActionGroup.CREATE_VM));

        return permissionList;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<String, String>();
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

    @Override
    public boolean checkDestDomains() {
        return super.checkDestDomains();
    }
}
