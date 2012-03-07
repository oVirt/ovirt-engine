package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class UpdateVmTemplateCommand<T extends UpdateVmTemplateParameters> extends VmTemplateCommand<T> {
    private VmTemplate mOldTemplate;

    public UpdateVmTemplateCommand(T parameters) {
        super(parameters);
        setVmTemplate(parameters.getVmTemplateData());
        setVmTemplateId(getVmTemplate().getId());
        setVdsGroupId(getVmTemplate().getvds_group_id());
        if (getVdsGroup() != null) {
            setStoragePoolId(getVdsGroup().getstorage_pool_id() != null ? getVdsGroup().getstorage_pool_id()
                        .getValue() : Guid.Empty);
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = false;
        mOldTemplate = DbFacade.getInstance().getVmTemplateDAO().get(getVmTemplate().getId());
        VmTemplateHandler.UpdateDisksFromDb(mOldTemplate);
        if (mOldTemplate != null) {
            if (VmTemplateHandler.BlankVmTemplateId.equals(mOldTemplate.getId())) {
                addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_EDIT_BLANK_TEMPLATE.toString());
            } else if (!StringHelper.EqOp(mOldTemplate.getname(), getVmTemplate().getname())
                    && isVmTemlateWithSameNameExist(getVmTemplateName())) {
                addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_CREATE_DUPLICATE_NAME);
            } else {
                if (getVdsGroup() == null) {
                    addCanDoActionMessage(VdcBllMessages.VMT_CLUSTER_IS_NOT_VALID);
                } else if (VmHandler.isMemorySizeLegal(mOldTemplate.getos(),
                        mOldTemplate.getmem_size_mb(),
                        getReturnValue()
                                .getCanDoActionMessages(),
                        getVdsGroup().getcompatibility_version().toString())) {
                    if (IsVmPriorityValueLegal(getParameters().getVmTemplateData().getpriority(), getReturnValue()
                            .getCanDoActionMessages())
                            && IsDomainLegal(getParameters().getVmTemplateData().getdomain(), getReturnValue()
                                    .getCanDoActionMessages())) {
                        returnValue = VmTemplateHandler.mUpdateVmTemplate.IsUpdateValid(mOldTemplate, getVmTemplate());
                        if (!returnValue) {
                            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_UPDATE_ILLEGAL_FIELD);
                        }
                    }
                }
            }
        }
        if (returnValue) {
            returnValue = AddVmCommand.CheckCpuSockets(getParameters().getVmTemplateData().getnum_of_sockets(),
                    getParameters().getVmTemplateData().getcpu_per_socket(), getVdsGroup().getcompatibility_version()
                            .toString(), getReturnValue().getCanDoActionMessages());
        }

        return returnValue;
    }

    @Override
    protected boolean validateQuota() {
        Guid quotaId = getVmTemplate().getQuotaId();
        if (quotaId == null) {
            // Set default quota id if storage pool enforcement is disabled.
            getVmTemplate().setQuotaId(QuotaHelper.getInstance().getQuotaIdToConsume(quotaId,
                    getStoragePool()));
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        permissionList =
                QuotaHelper.getInstance().addQuotaPermissionSubject(permissionList,
                        getStoragePool(),
                        getVmTemplate().getQuotaId());
        return permissionList;
    }

    @Override
    protected void executeCommand() {
        if (getVmTemplate() != null) {
            UpdateVmTemplate();
            if (getVmTemplate().getstorage_pool_id() != null
                    && !VmTemplateHandler.BlankVmTemplateId.equals(getVmTemplate().getId())) {
                UpdateTemplateInSpm(
                        getVmTemplate().getstorage_pool_id().getValue(),
                        new java.util.ArrayList<VmTemplate>(java.util.Arrays
                                .asList(new VmTemplate[] { getVmTemplate() })));
            }
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM_TEMPLATE : AuditLogType.USER_FAILED_UPDATE_VM_TEMPLATE;
    }

    private void UpdateVmTemplate() {
        DbFacade.getInstance().getVmTemplateDAO().update(getVmTemplate());
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
    }
}
