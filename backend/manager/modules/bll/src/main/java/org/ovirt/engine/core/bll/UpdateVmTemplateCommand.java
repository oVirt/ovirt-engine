package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;


public class UpdateVmTemplateCommand<T extends UpdateVmTemplateParameters> extends VmTemplateCommand<T>
        implements QuotaVdsDependent, RenamedEntityInfoProvider{
    private VmTemplate mOldTemplate;

    public UpdateVmTemplateCommand(T parameters) {
        super(parameters);
        setVmTemplate(parameters.getVmTemplateData());
        setVmTemplateId(getVmTemplate().getId());
        setVdsGroupId(getVmTemplate().getVdsGroupId());
        if (getVdsGroup() != null) {
            setStoragePoolId(getVdsGroup().getStoragePoolId() != null ? getVdsGroup().getStoragePoolId()
                        .getValue() : Guid.Empty);
        }
    }

    @Override
    protected boolean canDoAction() {
        if (VmTemplateHandler.BlankVmTemplateId.equals(getVmTemplate().getId())) {
            return failCanDoAction(VdcBllMessages.VMT_CANNOT_EDIT_BLANK_TEMPLATE);
        }
        boolean returnValue = false;
        mOldTemplate = DbFacade.getInstance().getVmTemplateDao().get(getVmTemplate().getId());
        if (mOldTemplate == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }
        if (mOldTemplate != null) {
            VmTemplateHandler.UpdateDisksFromDb(mOldTemplate);
            if (mOldTemplate.getstatus() == VmTemplateStatus.Locked) {
                return failCanDoAction(VdcBllMessages.VM_TEMPLATE_IS_LOCKED);
            }
            if (!StringUtils.equals(mOldTemplate.getName(), getVmTemplate().getName())
                    && isVmTemlateWithSameNameExist(getVmTemplateName())) {
                addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_CREATE_DUPLICATE_NAME);
            } else {
                if (getVdsGroup() == null) {
                    addCanDoActionMessage(VdcBllMessages.VMT_CLUSTER_IS_NOT_VALID);
                } else if (VmHandler.isMemorySizeLegal(mOldTemplate.getOs(),
                        mOldTemplate.getMemSizeMb(),
                        getReturnValue()
                                .getCanDoActionMessages(),
                        getVdsGroup().getcompatibility_version().toString())) {
                    if (IsVmPriorityValueLegal(getParameters().getVmTemplateData().getPriority(), getReturnValue()
                            .getCanDoActionMessages())
                            && IsDomainLegal(getParameters().getVmTemplateData().getDomain(), getReturnValue()
                                    .getCanDoActionMessages())) {
                        returnValue = VmTemplateHandler.isUpdateValid(mOldTemplate, getVmTemplate());
                        if (!returnValue) {
                            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_UPDATE_ILLEGAL_FIELD);
                        }
                    }
                }
            }
        }

        // Check that the USB policy is legal
        if (returnValue) {
            returnValue = VmHandler.isUsbPolicyLegal(getParameters().getVmTemplateData().getUsbPolicy(), getParameters().getVmTemplateData().getOs(), getVdsGroup(), getReturnValue().getCanDoActionMessages());
        }

        if (returnValue) {
            returnValue = AddVmCommand.CheckCpuSockets(getParameters().getVmTemplateData().getNumOfSockets(),
                    getParameters().getVmTemplateData().getCpuPerSocket(), getVdsGroup().getcompatibility_version()
                            .toString(), getReturnValue().getCanDoActionMessages());
        }

        return returnValue;
    }

    @Override
    protected void executeCommand() {
        if (getVmTemplate() != null) {
            getVmStaticDAO().incrementDbGeneration(getVmTemplate().getId());
            UpdateVmTemplate();
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM_TEMPLATE : AuditLogType.USER_FAILED_UPDATE_VM_TEMPLATE;
    }

    private void UpdateVmTemplate() {
        DbFacade.getInstance().getVmTemplateDao().update(getVmTemplate());
        // also update the smartcard device
        VmDeviceUtils.updateSmartcardDevice(getVmTemplateId(), getParameters().getVmTemplateData().isSmartcardEnabled());
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

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmTemplateId().toString(), LockMessagesMatchUtil.TEMPLATE);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        list.add(new QuotaSanityParameter(getParameters().getVmTemplateData().getQuotaId(), null));
        return list;
    }


    @Override
     public String getEntityType() {
        return VdcObjectType.VmTemplate.getVdcObjectTranslation();
     }

     @Override
     public String getEntityOldName() {
        return mOldTemplate.getName();
     }

     @Override
     public String getEntityNewName() {
         return getParameters().getVmTemplateData().getName();
     }

    @Override
    public void setEntityId(AuditLogableBase logable) {
        logable.setVmTemplateId(mOldTemplate.getId());
    }
}
