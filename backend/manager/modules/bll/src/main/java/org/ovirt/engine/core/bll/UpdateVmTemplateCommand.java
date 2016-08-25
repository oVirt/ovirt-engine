package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class UpdateVmTemplateCommand<T extends UpdateVmTemplateParameters> extends VmTemplateCommand<T>
        implements QuotaVdsDependent, RenamedEntityInfoProvider{

    private VmTemplate oldTemplate;
    private List<GraphicsDevice> cachedGraphics;

    protected final OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

    public UpdateVmTemplateCommand(T parameters) {
        super(parameters);
        setVmTemplate(parameters.getVmTemplateData());
        setVmTemplateId(getVmTemplate().getId());
        setVdsGroupId(getVmTemplate().getVdsGroupId());
        oldTemplate = DbFacade.getInstance().getVmTemplateDao().get(getVmTemplate().getId());

        if (getVdsGroup() != null) {
            setStoragePoolId(getVdsGroup().getStoragePoolId() != null ? getVdsGroup().getStoragePoolId()
                    : Guid.Empty);
        }

        Version compatibilityVersion = isBlankTemplate() || isInstanceType() ? Version.getLast() : getVdsGroup().getCompatibilityVersion();
        if (getVdsGroup() != null || isBlankTemplate()) {
            getVmPropertiesUtils().separateCustomPropertiesToUserAndPredefined(compatibilityVersion,
                    parameters.getVmTemplateData());
            if (oldTemplate != null) {
                getVmPropertiesUtils().separateCustomPropertiesToUserAndPredefined(compatibilityVersion,
                        oldTemplate);
            }
        }

        VmHandler.autoSelectUsbPolicy(getParameters().getVmTemplateData(), getVdsGroup());
        VmHandler.updateDefaultTimeZone(parameters.getVmTemplateData());
        VmHandler.autoSelectDefaultDisplayType(getVmTemplateId(),
                getParameters().getVmTemplateData(),
                getVdsGroup(),
                getParameters().getGraphicsDevices());
    }

    @Override
    protected boolean canDoAction() {
        boolean isInstanceType = isInstanceType();
        boolean isBlankTemplate = isBlankTemplate();

        if (getVdsGroup() == null && !(isInstanceType || isBlankTemplate)) {
            addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            return false;
        }

        boolean returnValue = false;

        if (oldTemplate == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        if (!isInstanceType && !isBlankTemplate) {
            VmTemplateHandler.updateDisksFromDb(oldTemplate);
        }

        if (!StringUtils.equals(oldTemplate.getName(), getVmTemplate().getName())) {
            if (!getVmTemplate().isBaseTemplate()) {
                // template version should always have the name of the base template
                return failCanDoAction(EngineMessage.VMT_CANNOT_UPDATE_VERSION_NAME);
            } else {
                // validate uniqueness of template name. If template is a regular template, uniqueness
                // is considered in context of the datacenter. If template is an 'Instance-Type', name
                // must be unique also across datacenters.
                if (isInstanceType) {
                    if (isInstanceWithSameNameExists(getVmTemplateName())) {
                        return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                    }
                } else {
                    if (isVmTemlateWithSameNameExist(getVmTemplateName(), isBlankTemplate ? null
                            : getVdsGroup().getStoragePoolId())) {
                        return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                    }
                }
            }
        }

        if (isVmPriorityValueLegal(getParameters().getVmTemplateData().getPriority(), getReturnValue()
                .getCanDoActionMessages()) && checkDomain()) {
            returnValue = VmTemplateHandler.isUpdateValid(oldTemplate, getVmTemplate());
            if (!returnValue) {
                addCanDoActionMessage(EngineMessage.VMT_CANNOT_UPDATE_ILLEGAL_FIELD);
            }
        }

        if(!setAndValidateCpuProfile()) {
            return false;
        }

        if (getParameters().getVmLargeIcon() != null && !validate(IconValidator.validate(
                IconValidator.DimensionsType.LARGE_CUSTOM_ICON,
                getParameters().getVmLargeIcon()))) {
            return false;
        }

        if (getParameters().getVmTemplateData() != null
                && getParameters().getVmTemplateData().getSmallIconId() != null
                && getParameters().getVmLargeIcon() == null // icon id is ignored if large icon is sent
                && !validate(IconValidator.validateIconId(getParameters().getVmTemplateData().getSmallIconId(), "Small"))) {
            return false;
        }

        if (getParameters().getVmTemplateData() != null
                && getParameters().getVmTemplateData().getLargeIconId() != null
                && getParameters().getVmLargeIcon() == null // icon id is ignored if large icon is sent
                && !validate(IconValidator.validateIconId(getParameters().getVmTemplateData().getLargeIconId(), "Large"))) {
            return false;
        }

        if (returnValue && getParameters().getWatchdog() != null) {
            returnValue = validate(new VmWatchdogValidator.VmWatchdogClusterIndependentValidator(
                    getParameters().getWatchdog()).isValid()
            );
        }

        if (!isInstanceType && !isBlankTemplate && returnValue) {
            return doClusterRelatedChecks();
        } else {
            return returnValue;
        }
    }

    private boolean doClusterRelatedChecks() {
        if (oldTemplate.getStatus() == VmTemplateStatus.Locked) {
            return failCanDoAction(EngineMessage.VM_TEMPLATE_IS_LOCKED);
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
            returnValue = validate((new VmWatchdogValidator.VmWatchdogClusterDependentValidator(getParameters().getVmTemplateData().getOsId(),
                    getParameters().getWatchdog(),
                    getVdsGroup().getCompatibilityVersion())).isValid());
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
                    getParameters().getVmTemplateData().getCpuPerSocket(),
                    getParameters().getVmTemplateData().getThreadsPerCpu(), getVdsGroup().getCompatibilityVersion()
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
                    VmDeviceUtils.hasVirtioScsiController(getParameters().getVmTemplateData().getId()),
                    hasWatchdog(getParameters().getVmTemplateData().getId()),
                    VmDeviceUtils.hasMemoryBalloon(getParameters().getVmTemplateData().getId()),
                    isSoundDeviceEnabled(),
                    getReturnValue().getCanDoActionMessages())) {
                returnValue = false;
            }
        }

        if (getParameters().getVmTemplateData().getMinAllocatedMem() > getParameters().getVmTemplateData().getMemSizeMb()) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_MIN_MEMORY_CANNOT_EXCEED_MEMORY_SIZE);
        }

        if (!getVmPropertiesUtils().validateVmProperties(
                getVdsGroup().getCompatibilityVersion(),
                getParameters().getVmTemplateData().getCustomProperties(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        if (returnValue) {
            boolean balloonEnabled = Boolean.TRUE.equals(getParameters().isBalloonEnabled());
            if (balloonEnabled && !osRepository.isBalloonEnabled(getParameters().getVmTemplateData().getOsId(),
                    getVdsGroup().getCompatibilityVersion())) {
                addCanDoActionMessageVariable("clusterArch", getVdsGroup().getArchitecture());
                return failCanDoAction(EngineMessage.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH);
            }
        }

        boolean soundDeviceEnabled = Boolean.TRUE.equals(getParameters().isSoundDeviceEnabled());
        if (soundDeviceEnabled && !osRepository.isSoundDeviceEnabled(getParameters().getVmTemplateData().getOsId(),
                getVdsGroup().getCompatibilityVersion())) {
            addCanDoActionMessageVariable("clusterArch", getVdsGroup().getArchitecture());
            return failCanDoAction(EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
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
                VmDeviceUtils.hasSoundDevice(getParameters().getVmTemplateData().getId());
    }

    @Override
    protected void executeCommand() {
        if (!isInstanceType() && !isBlankTemplate()) {
            VmHandler.warnMemorySizeLegal(getParameters().getVmTemplateData(),
                    getVdsGroup().getCompatibilityVersion());
        }

        getVmStaticDao().incrementDbGeneration(getVmTemplate().getId());
        updateOriginalTemplateNameOnDerivedVms();
        List<Guid> oldIconIds = Collections.emptyList();
        if (isTemplate()) {
            oldIconIds = IconUtils.updateVmIcon(oldTemplate, getVmTemplate(), getParameters().getVmLargeIcon());
        }
        updateVmTemplate();
        IconUtils.removeUnusedIcons(oldIconIds);
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
        if (!isInstanceType()) {
            return;
        }

        // get vms from db
        List<VM> vmsToUpdate = getVmDao().getVmsListByInstanceType(getVmTemplateId());
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
            auditLogDirector.log(logable, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVmTemplate().isTrustedService() && getVdsGroup().supportsTrustedService()) {
            auditLogDirector.log(logable, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM_TEMPLATE : AuditLogType.USER_FAILED_UPDATE_VM_TEMPLATE;
    }

    private void updateOriginalTemplateNameOnDerivedVms() {
        boolean templateNameChanged = !ObjectUtils.equals(oldTemplate.getName(), getVmTemplate().getName());
        if (templateNameChanged) {
            getVmDao().updateOriginalTemplateName(getVmTemplate().getId(), getVmTemplate().getName());
        }
    }

    private void updateVmTemplate() {
        VmHandler.updateVmInitToDB(getVmTemplate());
        DbFacade.getInstance().getVmTemplateDao().update(getVmTemplate());
        // also update the smartcard device
        VmDeviceUtils.updateSmartcardDevice(getVmTemplateId(), getParameters().getVmTemplateData().isSmartcardEnabled());
        // update audio device
        VmDeviceUtils.updateSoundDevice(oldTemplate,
                getVmTemplate(),
                getVdsGroup() != null ? getVdsGroup().getCompatibilityVersion() : null,
                getParameters().isSoundDeviceEnabled());

        VmDeviceUtils.updateConsoleDevice(getVmTemplateId(), getParameters().isConsoleEnabled());
        VmDeviceUtils.updateVirtioScsiController(getVmTemplateId(), getParameters().isVirtioScsiEnabled());
        if (getParameters().isBalloonEnabled() != null) {
            VmDeviceUtils.updateMemoryBalloon(getVmTemplateId(), getParameters().isBalloonEnabled());
        }
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__UPDATE);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        list.add(new QuotaSanityParameter(getParameters().getVmTemplateData().getQuotaId(), null));
        return list;
    }


    @Override
     public String getEntityType() {
        return VdcObjectType.VmTemplate.getVdcObjectTranslation();
     }

     @Override
     public String getEntityOldName() {
        return oldTemplate.getName();
     }

     @Override
     public String getEntityNewName() {
         return getParameters().getVmTemplateData().getName();
     }

    @Override
    public void setEntityId(AuditLogableBase logable) {
        logable.setVmTemplateId(oldTemplate.getId());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        if (getVmTemplate() != null && !isInstanceType() && !isBlankTemplate()) {
            // host-specific parameters can be changed by administration role only
            List<Guid> tmpltDdctHostsLst = getVmTemplate().getDedicatedVmForVdsList();
            List<Guid> prmTmpltDdctHostsLst = getParameters().getVmTemplateData().getDedicatedVmForVdsList();
            // tmpltDdctHostsLst.equals(prmTmpltDdctHostsLs is not good enough, lists order may change
            if (CollectionUtils.isEqualCollection(tmpltDdctHostsLst, prmTmpltDdctHostsLst) == false) {
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
        if (isInstanceType() || isBlankTemplate()) {
            return false;
        }

        return super.isQuotaDependant();
    }

    protected boolean setAndValidateCpuProfile() {
        // cpu profile isn't supported for instance types nor for blank template.
        if (isInstanceType() || isBlankTemplate()) {
            return true;
        }

        return validate(CpuProfileHelper.setAndValidateCpuProfile(getVmTemplate(),
                getVdsGroup().getCompatibilityVersion(), getUserId()));
    }

    private boolean isInstanceType() {
        return getVmTemplate().getTemplateType() == VmEntityType.INSTANCE_TYPE;
    }

    private boolean isTemplate() {
        return VmEntityType.TEMPLATE.equals(getVmTemplate().getTemplateType());
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
