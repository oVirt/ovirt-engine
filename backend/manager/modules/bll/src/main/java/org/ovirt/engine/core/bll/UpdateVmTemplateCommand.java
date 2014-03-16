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
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

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
        if (VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVmTemplate().getId())) {
            return failCanDoAction(VdcBllMessages.VMT_CANNOT_EDIT_BLANK_TEMPLATE);
        }
        boolean returnValue = false;
        mOldTemplate = DbFacade.getInstance().getVmTemplateDao().get(getVmTemplate().getId());
        if (mOldTemplate == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        VmTemplateHandler.updateDisksFromDb(mOldTemplate);
        if (mOldTemplate.getStatus() == VmTemplateStatus.Locked) {
            return failCanDoAction(VdcBllMessages.VM_TEMPLATE_IS_LOCKED);
        }

        if (!StringUtils.equals(mOldTemplate.getName(), getVmTemplate().getName())
                && isVmTemlateWithSameNameExist(getVmTemplateName())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
        } else {
            if (getVdsGroup() == null) {
                addCanDoActionMessage(VdcBllMessages.VMT_CLUSTER_IS_NOT_VALID);
            } else if (isVmPriorityValueLegal(getParameters().getVmTemplateData().getPriority(), getReturnValue()
                    .getCanDoActionMessages()) && checkDomain()) {
                returnValue = VmTemplateHandler.isUpdateValid(mOldTemplate, getVmTemplate());
                if (!returnValue) {
                    addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_UPDATE_ILLEGAL_FIELD);
                }
            }
        }

        // Check that the USB policy is legal
        if (returnValue) {
            returnValue = VmHandler.isUsbPolicyLegal(getParameters().getVmTemplateData().getUsbPolicy(), getParameters().getVmTemplateData().getOsId(), getVdsGroup(), getReturnValue().getCanDoActionMessages());
        }

        // Check if the OS type is supported
        if (returnValue) {
            returnValue =
                    VmHandler.isOsTypeSupported(getParameters().getVmTemplateData().getOsId(),
                            getVdsGroup().getArchitecture(),
                            getReturnValue().getCanDoActionMessages());
        }

        // Check if the watchdog model is supported
        if (returnValue && getParameters().getWatchdog() != null) {
            returnValue = validate((new VmWatchdogValidator(getParameters().getVmTemplateData().getOsId(),
                    getParameters().getWatchdog(),
                    getVdsGroup().getcompatibility_version())).isModelCompatibleWithOs());
        }

        // Check if the display type is supported
        if (returnValue) {
            returnValue = VmHandler.isDisplayTypeSupported(getParameters().getVmTemplateData().getOsId(),
                    getParameters().getVmTemplateData().getDefaultDisplayType(),
                    getReturnValue().getCanDoActionMessages(),
                    getVdsGroup().getcompatibility_version());
        }

        if (returnValue) {
            returnValue = AddVmCommand.checkCpuSockets(getParameters().getVmTemplateData().getNumOfSockets(),
                    getParameters().getVmTemplateData().getCpuPerSocket(), getVdsGroup().getcompatibility_version()
                    .toString(), getReturnValue().getCanDoActionMessages());
        }

        if (returnValue && getParameters().getVmTemplateData().getSingleQxlPci() &&
                !VmHandler.isSingleQxlDeviceLegal(getParameters().getVmTemplateData().getDefaultDisplayType(),
                        getParameters().getVmTemplateData().getOsId(),
                        getReturnValue().getCanDoActionMessages(),
                        getVdsGroup().getcompatibility_version())) {
            returnValue = false;
        }

        // Check PCI and IDE limits are ok
        if (returnValue) {

            List<VmNic> interfaces = getVmNicDao().getAllForTemplate(getParameters().getVmTemplateData().getId());

            if (!VmCommand.checkPciAndIdeLimit(getParameters().getVmTemplateData().getOsId(),
                            getVdsGroup().getcompatibility_version(),
                            getParameters().getVmTemplateData().getNumOfMonitors(),
                            interfaces,
                            new ArrayList<DiskImageBase>(getParameters().getVmTemplateData().getDiskList()),
                            VmDeviceUtils.isVirtioScsiControllerAttached(getParameters().getVmTemplateData().getId()),
                            hasWatchdog(getParameters().getVmTemplateData().getId()),
                            VmDeviceUtils.isBalloonEnabled(getParameters().getVmTemplateData().getId()),
                            isSoundDeviceEnabled(),
                            getReturnValue().getCanDoActionMessages())) {
                returnValue = false;
            }
        }

        if (getParameters().getVmTemplateData().getMinAllocatedMem() > getParameters().getVmTemplateData().getMemSizeMb()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_MIN_MEMORY_CANNOT_EXCEED_MEMORY_SIZE);
        }

        return returnValue;
    }

    private boolean checkDomain() {
        if (getParameters().getVmTemplateData().getVmInit() != null &&
                getParameters().getVmTemplateData().getVmInit().getDomain() != null) {
            return isDomainLegal(getParameters().getVmTemplateData().getVmInit().getDomain(),
                    getReturnValue().getCanDoActionMessages());
        }
        return true;
    }

    protected boolean hasWatchdog(Guid templateId) {
        return getParameters().getWatchdog() != null;
    }

    protected boolean isSoundDeviceEnabled() {
        Boolean soundDeviceEnabled = getParameters().isSoundDeviceEnabled();
        return soundDeviceEnabled != null ? soundDeviceEnabled :
                VmDeviceUtils.isSoundDeviceEnabled(getParameters().getVmTemplateData().getId());
    }

    @Override
    protected void executeCommand() {
        VmHandler.warnMemorySizeLegal(getParameters().getVmTemplateData(),
                getVdsGroup().getcompatibility_version());

        if (getVmTemplate() != null) {
            getVmStaticDAO().incrementDbGeneration(getVmTemplate().getId());
            updateOriginalTemplateNameOnDerivedVms();
            UpdateVmTemplate();
            updateWatchdog();
            checkTrustedService();
            setSucceeded(true);
        }
    }

    private void checkTrustedService() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VmTemplateName", getVmTemplateName());
        if (getVmTemplate().isTrustedService() && !getVdsGroup().supportsTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVmTemplate().isTrustedService() && getVdsGroup().supportsTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    private void updateWatchdog() {
        // do not update if this flag is not set
        if (getParameters().isUpdateWatchdog()) {
            Guid templateId = getParameters().getVmTemplateData().getId();
            VdcQueryReturnValue query =
                    getBackend().runInternalQuery(VdcQueryType.GetWatchdog, new IdQueryParameters(templateId));
            List<VmWatchdog> watchdogs = query.getReturnValue();
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

    private void updateOriginalTemplateNameOnDerivedVms() {
        boolean templateNameChanged = !ObjectUtils.equals(mOldTemplate.getName(), getVmTemplate().getName());
        if (templateNameChanged) {
            getVmDAO().updateOriginalTemplateName(getVmTemplate().getId(), getVmTemplate().getName());
        }
    }

    private void UpdateVmTemplate() {
        VmHandler.updateVmInitToDB(getVmTemplate());
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
