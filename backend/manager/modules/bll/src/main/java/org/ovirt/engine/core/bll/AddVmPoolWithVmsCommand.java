package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends CommonVmPoolWithVmsCommand<T> {

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
        boolean returnValue = super.canDoAction();

        if (returnValue && VmTemplateHandler.BlankVmTemplateId.equals(getParameters().getVmStaticData().getVmtGuid())) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_CREATE_FROM_BLANK_TEMPLATE);

        }

        return returnValue;
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
        return getAddVmsSucceded() ? AuditLogType.USER_ADD_VM_POOL_WITH_VMS
                : getSucceeded() ? AuditLogType.USER_ADD_VM_POOL_WITH_VMS_ADD_VDS_FAILED
                        : AuditLogType.USER_ADD_VM_POOL_WITH_VMS_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getParameters().getVmStaticData().getVdsGroupId(),
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getVmTemplateId(), VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));

        return permissionList;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<String, String>();
            VmPool vmPool = getParameters().getVmPool();
            String vmPoolName = vmPool == null ? StringUtils.EMPTY : vmPool.getVmPoolName();
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
}
