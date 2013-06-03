package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
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
                    : Guid.Empty);
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

        VmTemplateHandler.UpdateDisksFromDb(mOldTemplate);
        if (mOldTemplate.getStatus() == VmTemplateStatus.Locked) {
            return failCanDoAction(VdcBllMessages.VM_TEMPLATE_IS_LOCKED);
        }
        if (!StringUtils.equals(mOldTemplate.getName(), getVmTemplate().getName())
                && isVmTemlateWithSameNameExist(getVmTemplateName())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
        } else {
            if (getVdsGroup() == null) {
                addCanDoActionMessage(VdcBllMessages.VMT_CLUSTER_IS_NOT_VALID);
            } else if (VmHandler.isMemorySizeLegal(mOldTemplate.getOsId(),
                    mOldTemplate.getMemSizeMb(),
                    getReturnValue()
                            .getCanDoActionMessages(),
                    getVdsGroup().getcompatibility_version())) {
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

        // Check that the USB policy is legal
        if (returnValue) {
            returnValue = VmHandler.isUsbPolicyLegal(getParameters().getVmTemplateData().getUsbPolicy(), getParameters().getVmTemplateData().getOsId(), getVdsGroup(), getReturnValue().getCanDoActionMessages());
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
            updateWatchdog();
            setSucceeded(true);
        }
    }

    private void updateWatchdog() {
        // do not update if this flag is not set
        if (getParameters().isUpdateWatchdog()) {
            Guid templateId = getParameters().getVmTemplateData().getId();
            VdcQueryReturnValue query =
                    getBackend().RunQuery(VdcQueryType.GetWatchdog, new IdQueryParameters(templateId));
            @SuppressWarnings("unchecked")
            List<VmWatchdog> watchdogs = (List<VmWatchdog>) query.getReturnValue();
            if (watchdogs.isEmpty()) {
                if (getParameters().getWatchdog() == null) {
                    // nothing to do, no watchdog and no watchdog to create
                } else {
                    WatchdogParameters parameters = new WatchdogParameters();
                    parameters.setVm(false);
                    parameters.setId(templateId);
                    parameters.setAction(getParameters().getWatchdog().getAction());
                    parameters.setModel(getParameters().getWatchdog().getModel());
                    getBackend().runInternalAction(VdcActionType.AddWatchdog, parameters);
                }
            } else {
                WatchdogParameters watchdogParameters = new WatchdogParameters();
                watchdogParameters.setVm(false);
                watchdogParameters.setId(templateId);
                if (getParameters().getWatchdog() == null) {
                    // there is a watchdog in the vm, there should not be any, so let's delete
                    getBackend().runInternalAction(VdcActionType.RemoveWatchdog, watchdogParameters);
                } else {
                    // there is a watchdog in the vm, we have to update.
                    watchdogParameters.setAction(getParameters().getWatchdog().getAction());
                    watchdogParameters.setModel(getParameters().getWatchdog().getModel());
                    getBackend().runInternalAction(VdcActionType.UpdateWatchdog, watchdogParameters);
                }
            }
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
        // update audio device
        VmDeviceUtils.updateAudioDevice(mOldTemplate,
                getVmTemplate(),
                getVdsGroup().getcompatibility_version(),
                getParameters().isSoundDeviceEnabled());
        VmDeviceUtils.updateConsoleDevice(getVmTemplateId(), getParameters().isConsoleEnabled());
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
        return Collections.singletonMap(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
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

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        if (getVmTemplate() != null) {
            // host-specific parameters can be changed by administration role only
            if (!(getVmTemplate().getDedicatedVmForVds() == null ?
                    getParameters().getVmTemplateData().getDedicatedVmForVds() == null :
                    getVmTemplate().getDedicatedVmForVds().equals(getParameters().getVmTemplateData()
                            .getDedicatedVmForVds()))) {
                permissionList.add(
                        new PermissionSubject(getParameters().getVmTemplateId(),
                                VdcObjectType.VmTemplate,
                                ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES));
            }
        }

        return permissionList;
    }

}
