package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.validator.CpuPinningValidator.isCpuPinningValid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CloudInitHandler;

public class UpdateVmTemplateCommand<T extends UpdateVmTemplateParameters> extends VmTemplateManagementCommand<T>
        implements QuotaVdsDependent, RenamedEntityInfoProvider{

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private CpuProfileHelper cpuProfileHelper;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private IconUtils iconUtils;
    @Inject
    private OsRepository osRepository;
    @Inject
    private CloudInitHandler cloudInitHandler;

    private VmTemplate oldTemplate;
    private List<GraphicsDevice> cachedGraphics;

    public UpdateVmTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        setVmTemplate(getParameters().getVmTemplateData());
        setVmTemplateId(getVmTemplate().getId());
        setClusterId(getVmTemplate().getClusterId());
        oldTemplate =  vmTemplateDao.get(getVmTemplate().getId());

        if (getCluster() != null) {
            setStoragePoolId(getCluster().getStoragePoolId() != null ? getCluster().getStoragePoolId()
                    : Guid.Empty);
        }

        Version compatibilityVersion = isBlankTemplate() || isInstanceType()
                ? Version.getLast() : CompatibilityVersionUtils.getEffective(getVmTemplate(), this::getCluster);
        if (getCluster() != null || isBlankTemplate()) {
            getVmPropertiesUtils().separateCustomPropertiesToUserAndPredefined(compatibilityVersion,
                    getParameters().getVmTemplateData());
            if (oldTemplate != null) {
                getVmPropertiesUtils().separateCustomPropertiesToUserAndPredefined(compatibilityVersion,
                        oldTemplate);
            }
        }

        vmHandler.autoSelectUsbPolicy(getParameters().getVmTemplateData());
        vmHandler.updateDefaultTimeZone(getParameters().getVmTemplateData());
        vmHandler.autoSelectDefaultDisplayType(getVmTemplateId(),
                getParameters().getVmTemplateData(),
                getCluster(),
                getParameters().getGraphicsDevices());
        vmHandler.autoSelectResumeBehavior(getParameters().getVmTemplateData());

        getVmDeviceUtils().setCompensationContext(getCompensationContextIfEnabledByCaller());
        if (getVmTemplate().getBiosType() == null) {
            getVmTemplate().setBiosType(oldTemplate.getBiosType());
        }
    }

    @Override
    protected boolean validate() {
        boolean isInstanceType = isInstanceType();
        boolean isBlankTemplate = isBlankTemplate();

        if (getCluster() == null && !(isInstanceType || isBlankTemplate)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
        }

        boolean returnValue = false;

        if (oldTemplate == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        if (!StringUtils.equals(oldTemplate.getName(), getVmTemplate().getName())) {
            if (!getVmTemplate().isBaseTemplate()) {
                // template version should always have the name of the base template
                return failValidation(EngineMessage.VMT_CANNOT_UPDATE_VERSION_NAME);
            } else {
                // validate uniqueness of template name. If template is a regular template, uniqueness
                // is considered in context of the datacenter. If template is an 'Instance-Type', name
                // must be unique also across datacenters.
                if (isInstanceType) {
                    if (isInstanceWithSameNameExists(getVmTemplateName())) {
                        return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                    }
                } else {
                    if (isVmTemplateWithSameNameExist(getVmTemplateName(), isBlankTemplate ? null
                            : getCluster().getStoragePoolId())) {
                        return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                    }
                }
            }
        }

        if (vmHandler.isVmPriorityValueLegal(getParameters().getVmTemplateData().getPriority()).isValid() &&
                checkDomain()) {
            returnValue = vmTemplateHandler.isUpdateValid(oldTemplate, getVmTemplate());
            if (!returnValue) {
                addValidationMessage(EngineMessage.VMT_CANNOT_UPDATE_ILLEGAL_FIELD);
            }
        }

        if (!validate(vmHandler.validateSmartCardDevice(getParameters().getVmTemplateData()))) {
            return false;
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

        if (!validate(vmHandler.validateMaxMemorySize(
                getParameters().getVmTemplateData(),
                CompatibilityVersionUtils.getEffective(getParameters().getVmTemplateData(), this::getCluster)))) {
            return false;
        }

        List<EngineMessage> msgs = cloudInitHandler.validate(getParameters().getVmTemplateData().getVmInit());
        if (!CollectionUtils.isEmpty(msgs)) {
            return failValidation(msgs);
        }

        if (getVmTemplate().getClusterId() != null && getVmTemplate().getBiosType() == null) {
            return failValidation(EngineMessage.VM_TEMPLATE_WITH_CLUSTER_WITHOUT_BIOS_TYPE);
        }

        if (getVmTemplate().getClusterId() == null && getVmTemplate().getBiosType() != null) {
            return failValidation(EngineMessage.VM_TEMPLATE_WITHOUT_CLUSTER_WITH_BIOS_TYPE);
        }

        if (!isInstanceType && !isBlankTemplate && returnValue) {
            return doClusterRelatedChecks();
        } else {
            return returnValue;
        }
    }

    private boolean doClusterRelatedChecks() {
        if (oldTemplate.getStatus() == VmTemplateStatus.Locked) {
            return failValidation(EngineMessage.VM_TEMPLATE_IS_LOCKED);
        }

        // Check if the OS type is supported
        boolean returnValue =
                validate(vmHandler.isOsTypeSupported(getParameters().getVmTemplateData().getOsId(),
                        getCluster().getArchitecture()));


        // Check if the watchdog model is supported
        if (returnValue && getParameters().getWatchdog() != null) {
            returnValue = validate(new VmWatchdogValidator.VmWatchdogClusterDependentValidator(getParameters().getVmTemplateData().getOsId(),
                    getParameters().getWatchdog(),
                    getVmTemplate().getCompatibilityVersion()).isValid());
        }

        // Check if the display type is supported
        if (returnValue) {
            returnValue = validate(vmHandler.isGraphicsAndDisplaySupported(getParameters().getVmTemplateData().getOsId(),
                    vmHandler.getResultingVmGraphics(getVmDeviceUtils().getGraphicsTypesOfEntity(getVmTemplateId()), getParameters().getGraphicsDevices()),
                    getParameters().getVmTemplateData().getDefaultDisplayType(),
                    getParameters().getVmTemplateData().getBiosType(),
                    getVmTemplate().getCompatibilityVersion()));
        }

        if (returnValue) {
            returnValue = validate(VmValidator.validateCpuSockets(getParameters().getVmTemplateData(),
                    getVmTemplate().getCompatibilityVersion(),
                    getCluster().getArchitecture(),
                    osRepository));
        }

        // Check PCI and IDE limits are ok
        if (returnValue) {

            List<VmNic> interfaces = vmNicDao.getAllForTemplate(getParameters().getVmTemplateData().getId());
            List<DiskVmElement> diskVmElements = diskVmElementDao.getAllForVm(getVmTemplateId());

            if (!validate(VmValidator.checkPciAndIdeLimit(getParameters().getVmTemplateData().getOsId(),
                    getVmTemplate().getCompatibilityVersion(),
                    getParameters().getVmTemplateData().getNumOfMonitors(),
                    interfaces,
                    diskVmElements,
                    getVmDeviceUtils().hasVirtioScsiController(getParameters().getVmTemplateData().getId()),
                    hasWatchdog(getParameters().getVmTemplateData().getId()),
                    isSoundDeviceEnabled()))) {
                returnValue = false;
            }
        }

        if (getParameters().getVmTemplateData().getMinAllocatedMem() > getParameters().getVmTemplateData().getMemSizeMb()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MIN_MEMORY_CANNOT_EXCEED_MEMORY_SIZE);
        }

        if (!getVmPropertiesUtils().validateVmProperties(
                getVmTemplate().getCompatibilityVersion(),
                getParameters().getVmTemplateData().getCustomProperties(),
                getReturnValue().getValidationMessages())) {
            return false;
        }

        boolean soundDeviceEnabled = Boolean.TRUE.equals(getParameters().isSoundDeviceEnabled());
        if (soundDeviceEnabled && !osRepository.isSoundDeviceEnabled(getParameters().getVmTemplateData().getOsId(),
                getVmTemplate().getCompatibilityVersion())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
        }
        // check cpuPinning
        if (!validate(isCpuPinningValid(getVmTemplate().getCpuPinning(), getVmTemplate()))) {
            return false;
        }

        boolean tpmEnabled = Boolean.TRUE.equals(getParameters().isTpmEnabled());
        if (tpmEnabled && !getVmDeviceUtils().isTpmDeviceSupported(getVmTemplate(), getCluster())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.TPM_DEVICE_REQUESTED_ON_NOT_SUPPORTED_PLATFORM);
        }

        if (!tpmEnabled && osRepository.requiresTpm(getVmTemplate().getOsId())) {
            return failValidation(EngineMessage.TPM_DEVICE_REQUIRED_BY_OS);
        }

        if (!validate(VmValidator.isBiosTypeSupported(getVmTemplate(), getCluster(), osRepository))) {
            return false;
        }

        return returnValue;
    }

    private boolean checkDomain() {
        if (getParameters().getVmTemplateData().getVmInit() != null &&
                getParameters().getVmTemplateData().getVmInit().getDomain() != null) {
            return isDomainLegal(getParameters().getVmTemplateData().getVmInit().getDomain(),
                    getReturnValue().getValidationMessages());
        }
        return true;
    }

    /**
     * Determines whether the specified domain name is legal.
     *
     * @param domainName
     *            Name of the domain.
     * @param reasons
     *            The reasons in case of failure (output parameter).
     * @return <code>true</code> if domain name is legal; otherwise, <code>false</code>.
     */
    private static boolean isDomainLegal(String domainName, ArrayList<String> reasons) {
        boolean result = true;
        char[] illegalChars = new char[] { '&' };
        if (StringUtils.isNotEmpty(domainName)) {
            for (char c : illegalChars) {
                if (domainName.contains(Character.toString(c))) {
                    result = false;
                    reasons.add(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_DOMAIN_NAME.toString());
                    reasons.add(String.format("$Domain %1$s", domainName));
                    reasons.add(String.format("$Char %1$s", c));
                    break;
                }
            }
        }
        return result;
    }

    protected boolean hasWatchdog(Guid templateId) {
        return getParameters().getWatchdog() != null;
    }

    protected boolean isSoundDeviceEnabled() {
        Boolean soundDeviceEnabled = getParameters().isSoundDeviceEnabled();
        return soundDeviceEnabled != null ? soundDeviceEnabled :
                getVmDeviceUtils().hasSoundDevice(getParameters().getVmTemplateData().getId());
    }

    @Override
    protected void executeCommand() {
        if (!isInstanceType() && !isBlankTemplate()) {
            vmHandler.warnMemorySizeLegal(getParameters().getVmTemplateData(),
                    getVmTemplate().getCompatibilityVersion());
        }

        // This cannot be reverted using compensation, but it should not be needed
        vmStaticDao.incrementDbGeneration(getVmTemplate().getId());

        updateOriginalTemplateNameOnDerivedVms();
        List<Guid> oldIconIds = Collections.emptyList();
        if (isTemplate()) {
            oldIconIds = iconUtils.updateVmIcon(oldTemplate, getVmTemplate(), getParameters().getVmLargeIcon());
        }
        updateVmTemplate();
        iconUtils.removeUnusedIcons(oldIconIds, getCompensationContextIfEnabledByCaller());
        updateWatchdog(getParameters().getVmTemplateData().getId());
        updateRngDevice(getParameters().getVmTemplateData().getId());
        updateGraphicsDevice();
        checkTrustedService();
        updateVmsOfInstanceType();
        updateVmDevicesOnChipsetChange();

        compensationStateChanged();
        setSucceeded(true);
    }

    /**
     * only in case of InstanceType update, update all vms that are bound to it
     */
    private void updateVmsOfInstanceType() {
        if (!isInstanceType()) {
            return;
        }

        // Currently, compensation is only used when this command is called from UpdateClusterCommand,
        // and it does not update instances.
        // TODO - Add compensation support if needed.
        throwIfCompensationEnabled();

        // get vms from db
        List<VM> vmsToUpdate = vmDao.getVmsListByInstanceType(getVmTemplateId());
        for (VM vm : vmsToUpdate) {
            VmManagementParametersBase params = new VmManagementParametersBase(vm);
            params.setApplyChangesLater(true);
            runInternalAction(ActionType.UpdateVm, params);
        }
    }

    private void checkTrustedService() {
        if (getCluster() == null) {
            return;
        }
        if (getVmTemplate().isTrustedService() && !getCluster().supportsTrustedService()) {
            auditLogDirector.log(this, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        } else if (!getVmTemplate().isTrustedService() && getCluster().supportsTrustedService()) {
            auditLogDirector.log(this, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM_TEMPLATE : AuditLogType.USER_FAILED_UPDATE_VM_TEMPLATE;
    }

    private void updateOriginalTemplateNameOnDerivedVms() {
        boolean templateNameChanged = !Objects.equals(oldTemplate.getName(), getVmTemplate().getName());
        if (templateNameChanged) {
            // Currently, compensation is only used when this command is called from UpdateClusterCommand,
            // and it does not change the template name.
            // TODO - Add compensation support if needed.
            throwIfCompensationEnabled();
            vmDao.updateOriginalTemplateName(getVmTemplate().getId(), getVmTemplate().getName());
        }
    }

    private void updateVmTemplate() {
        vmHandler.updateVmInitToDB(getVmTemplate(), getCompensationContextIfEnabledByCaller());
        CompensationUtils.updateEntity(getVmTemplate(), vmTemplateDao, getCompensationContextIfEnabledByCaller());
        // also update the smartcard device
        getVmDeviceUtils().updateSmartcardDevice(getVmTemplateId(), getParameters().getVmTemplateData().isSmartcardEnabled());
        // update audio device
        getVmDeviceUtils().updateSoundDevice(oldTemplate,
                getVmTemplate(),
                getVmTemplate().getCompatibilityVersion(),
                getParameters().isSoundDeviceEnabled());

        getVmDeviceUtils().updateTpmDevice(getVmTemplate(), getCluster(), getParameters().isTpmEnabled());
        getVmDeviceUtils().updateConsoleDevice(getVmTemplateId(), getParameters().isConsoleEnabled());
        getVmDeviceUtils().updateUsbSlots(oldTemplate, getVmTemplate(), getCluster());
        getVmDeviceUtils().updateVirtioScsiController(getVmTemplate(), getParameters().isVirtioScsiEnabled());
        getVmDeviceUtils().addMemoryBalloonIfNeeded(getVmTemplateId());
        getVmDeviceUtils().updateVideoDevices(oldTemplate, getParameters().getVmTemplateData());
    }

    @Override
    protected void updateWatchdog(Guid templateId) {
        if (!getParameters().isUpdateWatchdog()) {
            return;
        }

        // Currently, compensation is only used when this command is called from UpdateClusterCommand,
        // and it does not update watchdog.
        // TODO - Add compensation support if needed.
        throwIfCompensationEnabled();
        super.updateWatchdog(templateId);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        list.add(new QuotaSanityParameter(getParameters().getVmTemplateData().getQuotaId()));
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
    public void setEntityId(AuditLogable logable) {
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
            if (!CollectionUtils.isEqualCollection(tmpltDdctHostsLst, prmTmpltDdctHostsLst)) {
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

        return validate(cpuProfileHelper.setAndValidateCpuProfile(
                getVmTemplate(),
                getUserIdIfExternal().orElse(null)));
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
                    parameters.setCompensationEnabled(isCompensationEnabledByCaller());
                    runInternalAction(ActionType.AddGraphicsDevice, parameters, cloneContextWithNoCleanupCompensation());
                }
            } else {
                if (getParameters().getGraphicsDevices().get(type) == null) {
                    GraphicsParameters parameters = new GraphicsParameters(vmGraphicsDevice);
                    parameters.setVm(false);
                    parameters.setCompensationEnabled(isCompensationEnabledByCaller());
                    runInternalAction(ActionType.RemoveGraphicsDevice, parameters, cloneContextWithNoCleanupCompensation());
                } else {
                    getParameters().getGraphicsDevices().get(type).setDeviceId(vmGraphicsDevice.getDeviceId());
                    getParameters().getGraphicsDevices().get(type).setVmId(getVmTemplateId());
                    GraphicsParameters parameters = new GraphicsParameters(getParameters().getGraphicsDevices().get(type));
                    parameters.setVm(false);
                    parameters.setCompensationEnabled(isCompensationEnabledByCaller());
                    runInternalAction(ActionType.UpdateGraphicsDevice, parameters, cloneContextWithNoCleanupCompensation());
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
            cachedGraphics = backend
                    .runInternalQuery(QueryType.GetGraphicsDevices, new IdQueryParameters(getVmTemplateId())).getReturnValue();
        }
        return cachedGraphics;
    }

    private void updateVmDevicesOnChipsetChange() {
        if (isChipsetChanged()) {
            log.info("BIOS chipset type has changed for template: {} ({}), the disks and devices will be converted to new chipset.",
                    getVmTemplate().getName(),
                    getVmTemplate().getId());
            getVmHandler().convertVmToNewChipset(getVmTemplateId(), getVmTemplate().getBiosType().getChipsetType(), getCompensationContextIfEnabledByCaller());
        }
    }

    private boolean isChipsetChanged() {
        BiosType newEffectiveBiosType = getVmTemplate().getBiosType();
        BiosType oldEffectiveBiosType = oldTemplate.getBiosType();
        if (newEffectiveBiosType == null || oldEffectiveBiosType == null) {
            return false;
        }
        return  newEffectiveBiosType.getChipsetType() != oldEffectiveBiosType.getChipsetType();
    }
}
