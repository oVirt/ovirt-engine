package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
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
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class UpdateVmTemplateCommand<T extends UpdateVmTemplateParameters> extends VmTemplateCommand<T>
        implements QuotaVdsDependent, RenamedEntityInfoProvider{
    private VmTemplate mOldTemplate;
    private List<GraphicsDevice> cachedGraphics;

    public UpdateVmTemplateCommand(T parameters) {
        super(parameters);
        setVmTemplate(parameters.getVmTemplateData());
        setVmTemplateId(getVmTemplate().getId());
        setVdsGroupId(getVmTemplate().getVdsGroupId());
        mOldTemplate = DbFacade.getInstance().getVmTemplateDao().get(getVmTemplate().getId());
        if (getVdsGroup() != null) {
            setStoragePoolId(getVdsGroup().getStoragePoolId() != null ? getVdsGroup().getStoragePoolId()
                    : Guid.Empty);
            getVmPropertiesUtils().separateCustomPropertiesToUserAndPredefined(getVdsGroup().getCompatibilityVersion(),
                    parameters.getVmTemplateData());
            if (mOldTemplate != null) {
                getVmPropertiesUtils().separateCustomPropertiesToUserAndPredefined(getVdsGroup().getCompatibilityVersion(),
                        mOldTemplate);
            }
        }
        VmHandler.updateDefaultTimeZone(parameters.getVmTemplateData());
    }

    @Override
    protected boolean canDoAction() {
        boolean isInstanceType = getVmTemplate().getTemplateType() == VmEntityType.INSTANCE_TYPE;
        if (getVdsGroup() == null && !isInstanceType) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            return false;
        }

        if (VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVmTemplate().getId())) {
            return failCanDoAction(VdcBllMessages.VMT_CANNOT_EDIT_BLANK_TEMPLATE);
        }
        boolean returnValue = false;

        if (mOldTemplate == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        if (!isInstanceType) {
            VmTemplateHandler.updateDisksFromDb(mOldTemplate);
        }

        if (!StringUtils.equals(mOldTemplate.getName(), getVmTemplate().getName())) {
            if (!getVmTemplate().isBaseTemplate()) {
                // template version should always have the name of the base template
                return failCanDoAction(VdcBllMessages.VMT_CANNOT_UPDATE_VERSION_NAME);
            } else if (isVmTemlateWithSameNameExist(getVmTemplateName())) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            }
        }

        if (isVmPriorityValueLegal(getParameters().getVmTemplateData().getPriority(), getReturnValue()
                .getCanDoActionMessages()) && checkDomain()) {
            returnValue = VmTemplateHandler.isUpdateValid(mOldTemplate, getVmTemplate());
            if (!returnValue) {
                addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_UPDATE_ILLEGAL_FIELD);
            }
        }

        if(!setAndValidateCpuProfile()) {
            return false;
        }

        if (!isInstanceType && returnValue) {
            return doClusterRelatedChecks();
        } else {
            return returnValue;
        }
    }

    private boolean doClusterRelatedChecks() {

        if (mOldTemplate.getStatus() == VmTemplateStatus.Locked) {
            return failCanDoAction(VdcBllMessages.VM_TEMPLATE_IS_LOCKED);
        }

        // Check that the USB policy is legal
        boolean returnValue = VmHandler.isUsbPolicyLegal(getParameters().getVmTemplateData().getUsbPolicy(), getParameters().getVmTemplateData().getOsId(), getVdsGroup(), getReturnValue().getCanDoActionMessages());

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
                    getVdsGroup().getCompatibilityVersion())).isModelCompatibleWithOs());
        }

        // Check if the display type is supported
        if (returnValue) {
            returnValue = VmHandler.isGraphicsAndDisplaySupported(getParameters().getVmTemplateData().getOsId(),
                    VmHandler.getResultingVmGraphics(VmDeviceUtils.getGraphicsTypesOfEntity(getVmTemplateId()), getParameters().getGraphicsDevices()),
                    getParameters().getVmTemplateData().getDefaultDisplayType(),
                    getReturnValue().getCanDoActionMessages(),
                    getVdsGroup().getCompatibilityVersion());
        }

        if (returnValue) {
            returnValue = AddVmCommand.checkCpuSockets(getParameters().getVmTemplateData().getNumOfSockets(),
                    getParameters().getVmTemplateData().getCpuPerSocket(), getVdsGroup().getCompatibilityVersion()
                    .toString(), getReturnValue().getCanDoActionMessages());
        }

        if (returnValue && getParameters().getVmTemplateData().getSingleQxlPci() &&
                !VmHandler.isSingleQxlDeviceLegal(getParameters().getVmTemplateData().getDefaultDisplayType(),
                        getParameters().getVmTemplateData().getOsId(),
                        getReturnValue().getCanDoActionMessages(),
                        getVdsGroup().getCompatibilityVersion())) {
            returnValue = false;
        }

        // Check PCI and IDE limits are ok
        if (returnValue) {

            List<VmNic> interfaces = getVmNicDao().getAllForTemplate(getParameters().getVmTemplateData().getId());

            if (!VmCommand.checkPciAndIdeLimit(getParameters().getVmTemplateData().getOsId(),
                    getVdsGroup().getCompatibilityVersion(),
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

        if (!getVmPropertiesUtils().validateVmProperties(
                getVdsGroup().getCompatibilityVersion(),
                getParameters().getVmTemplateData().getCustomProperties(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
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
        if (getVmTemplate().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            VmHandler.warnMemorySizeLegal(getParameters().getVmTemplateData(),
                    getVdsGroup().getCompatibilityVersion());
        }

        getVmStaticDAO().incrementDbGeneration(getVmTemplate().getId());
        updateOriginalTemplateNameOnDerivedVms();
        UpdateVmTemplate();
        updateWatchdog(getParameters().getVmTemplateData().getId());
        updateRngDevice(getParameters().getVmTemplateData().getId());
        updateGraphicsDevice();
        checkTrustedService();
        updateVmsOfInstanceType();
        setSucceeded(true);
    }

    /**
     * only in case of InstanceType update, update all vms that are bound to it
     */
    private void updateVmsOfInstanceType() {
        if (getVmTemplate().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            return;
        }

        // get vms from db
        List<VM> vmsToUpdate = getVmDAO().getVmsListByInstanceType(getVmTemplateId());
        for (VM vm : vmsToUpdate) {
            VmManagementParametersBase params = new VmManagementParametersBase(vm);
            params.setApplyChangesLater(true);
            runInternalAction(VdcActionType.UpdateVm, params);
        }
    }

    private void checkTrustedService() {
        if (getVdsGroup() == null) {
            return;
        }
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VmTemplateName", getVmTemplateName());
        if (getVmTemplate().isTrustedService() && !getVdsGroup().supportsTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVmTemplate().isTrustedService() && getVdsGroup().supportsTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
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
                getVdsGroup() != null ? getVdsGroup().getCompatibilityVersion() : null,
                getParameters().isSoundDeviceEnabled());

        VmDeviceUtils.updateConsoleDevice(getVmTemplateId(), getParameters().isConsoleEnabled());
        VmDeviceUtils.updateVirtioScsiController(getVmTemplateId(), getParameters().isVirtioScsiEnabled());
        VmDeviceUtils.updateMemoryBalloon(getVmTemplateId(), getParameters().isBalloonEnabled());
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

        if (getVmTemplate() != null && getVmTemplate().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
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

    @Override
    protected boolean isQuotaDependant() {
        if (getVmTemplate().getTemplateType() == VmEntityType.INSTANCE_TYPE) {
            return false;
        }

        return super.isQuotaDependant();
    }

    protected boolean setAndValidateCpuProfile() {
        // cpu profile isn't supported for instance types.
        if (getVmTemplate().getTemplateType() == VmEntityType.INSTANCE_TYPE) {
            return true;
        }
        return validate(CpuProfileHelper.setAndValidateCpuProfile(getVmTemplate(),
                getVdsGroup().getCompatibilityVersion()));
    }

    private VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    private void updateGraphicsDevice() {
        for (GraphicsType type : getParameters().getGraphicsDevices().keySet()) {
            GraphicsDevice vmGraphicsDevice = getGraphicsDevOfType(type);
            if (vmGraphicsDevice == null) {
                if (getParameters().getGraphicsDevices().get(type) != null) {
                    getParameters().getGraphicsDevices().get(type).setVmId(getVmTemplateId());
                    GraphicsParameters parameters = new GraphicsParameters(getParameters().getGraphicsDevices().get(type));
                    parameters.setVm(false);
                    getBackend().runInternalAction(VdcActionType.AddGraphicsDevice, parameters);
                }
            } else {
                if (getParameters().getGraphicsDevices().get(type) == null) {
                    GraphicsParameters parameters = new GraphicsParameters(vmGraphicsDevice);
                    parameters.setVm(false);
                    getBackend().runInternalAction(VdcActionType.RemoveGraphicsDevice, parameters);
                } else {
                    getParameters().getGraphicsDevices().get(type).setVmId(getVmTemplateId());
                    GraphicsParameters parameters = new GraphicsParameters(getParameters().getGraphicsDevices().get(type));
                    parameters.setVm(false);
                    getBackend().runInternalAction(VdcActionType.UpdateGraphicsDevice, parameters);
                }
            }
        }
    }

    // first dev or null
    private GraphicsDevice getGraphicsDevOfType(GraphicsType type) {
        List<GraphicsDevice> graphicsDevices = getGraphicsDevices();

        for (GraphicsDevice dev : graphicsDevices) {
            if (dev.getGraphicsType() == type) {
                return dev;
            }
        }

        return null;
    }

    private List<GraphicsDevice> getGraphicsDevices() {
        if (cachedGraphics == null) {
            cachedGraphics = getBackend()
                    .runInternalQuery(VdcQueryType.GetGraphicsDevices, new IdQueryParameters(getVmTemplateId())).getReturnValue();
        }
        return cachedGraphics;
    }

}
