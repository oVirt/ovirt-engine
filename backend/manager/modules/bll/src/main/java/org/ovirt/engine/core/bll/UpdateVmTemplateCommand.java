package org.ovirt.engine.core.bll;

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
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmInitToOpenStackMetadataAdapter;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNicDao;

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
    private IconUtils iconUtils;
    @Inject
    private OsRepository osRepository;
    @Inject
    private VmInitToOpenStackMetadataAdapter openStackMetadataAdapter;

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
        vmHandler.autoSelectResumeBehavior(getParameters().getVmTemplateData(), getCluster());
    }

    @Override
    protected boolean validate() {
        boolean isInstanceType = isInstanceType();
        boolean isBlankTemplate = isBlankTemplate();

        if (getCluster() == null && !(isInstanceType || isBlankTemplate)) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            return false;
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

        Version effectiveCompatibilityVersion =
                CompatibilityVersionUtils.getEffective(getParameters().getVmTemplateData(), this::getCluster);
        if (getParameters().getVmTemplateData().getVmType() == VmType.HighPerformance
                && !FeatureSupported.isHighPerformanceTypeSupported(effectiveCompatibilityVersion)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_HIGH_PERFORMANCE_IS_NOT_SUPPORTED,
                    String.format("$Version %s", effectiveCompatibilityVersion));
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

        List<EngineMessage> msgs = openStackMetadataAdapter.validate(getParameters().getVmTemplateData().getVmInit());
        if (!CollectionUtils.isEmpty(msgs)) {
            return failValidation(msgs);
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
                    getVmTemplate().getCompatibilityVersion()));
        }

        if (returnValue) {
            returnValue = validate(VmValidator.validateCpuSockets(getParameters().getVmTemplateData(),
                    getVmTemplate().getCompatibilityVersion()));
        }

        if (returnValue && getParameters().getVmTemplateData().getSingleQxlPci() &&
                getParameters().getVmTemplateData().getDefaultDisplayType() != DisplayType.none &&
                !validate(vmHandler.isSingleQxlDeviceLegal(getParameters().getVmTemplateData().getDefaultDisplayType(),
                        getParameters().getVmTemplateData().getOsId()))) {
            returnValue = false;
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
                    getVmDeviceUtils().hasMemoryBalloon(getParameters().getVmTemplateData().getId()),
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

        if (returnValue) {
            boolean balloonEnabled = Boolean.TRUE.equals(getParameters().isBalloonEnabled());
            if (balloonEnabled && !osRepository.isBalloonEnabled(getParameters().getVmTemplateData().getOsId(),
                    getVmTemplate().getCompatibilityVersion())) {
                addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
                return failValidation(EngineMessage.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH);
            }
        }

        boolean soundDeviceEnabled = Boolean.TRUE.equals(getParameters().isSoundDeviceEnabled());
        if (soundDeviceEnabled && !osRepository.isSoundDeviceEnabled(getParameters().getVmTemplateData().getOsId(),
                getVmTemplate().getCompatibilityVersion())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
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

        vmStaticDao.incrementDbGeneration(getVmTemplate().getId());
        updateOriginalTemplateNameOnDerivedVms();
        List<Guid> oldIconIds = Collections.emptyList();
        if (isTemplate()) {
            oldIconIds = iconUtils.updateVmIcon(oldTemplate, getVmTemplate(), getParameters().getVmLargeIcon());
        }
        updateVmTemplate();
        iconUtils.removeUnusedIcons(oldIconIds);
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
            vmDao.updateOriginalTemplateName(getVmTemplate().getId(), getVmTemplate().getName());
        }
    }

    private void updateVmTemplate() {
        vmHandler.updateVmInitToDB(getVmTemplate());
        vmTemplateDao.update(getVmTemplate());
        // also update the smartcard device
        getVmDeviceUtils().updateSmartcardDevice(getVmTemplateId(), getParameters().getVmTemplateData().isSmartcardEnabled());
        // update audio device
        getVmDeviceUtils().updateSoundDevice(oldTemplate,
                getVmTemplate(),
                getVmTemplate().getCompatibilityVersion(),
                getParameters().isSoundDeviceEnabled());

        getVmDeviceUtils().updateConsoleDevice(getVmTemplateId(), getParameters().isConsoleEnabled());
        if (oldTemplate.getUsbPolicy() != getVmTemplate().getUsbPolicy() || oldTemplate.getVmType() != getVmTemplate().getVmType()) {
            getVmDeviceUtils().updateUsbSlots(oldTemplate, getVmTemplate());
        }
        getVmDeviceUtils().updateVirtioScsiController(getVmTemplate(), getParameters().isVirtioScsiEnabled());
        if (getParameters().isBalloonEnabled() != null) {
            getVmDeviceUtils().updateMemoryBalloon(getVmTemplateId(), getParameters().isBalloonEnabled());
        }
        getVmDeviceUtils().updateVideoDevices(oldTemplate, getParameters().getVmTemplateData());
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
                    backend.runInternalAction(ActionType.AddGraphicsDevice, parameters);
                }
            } else {
                if (getParameters().getGraphicsDevices().get(type) == null) {
                    GraphicsParameters parameters = new GraphicsParameters(vmGraphicsDevice);
                    parameters.setVm(false);
                    backend.runInternalAction(ActionType.RemoveGraphicsDevice, parameters);
                } else {
                    getParameters().getGraphicsDevices().get(type).setVmId(getVmTemplateId());
                    GraphicsParameters parameters = new GraphicsParameters(getParameters().getGraphicsDevices().get(type));
                    parameters.setVm(false);
                    backend.runInternalAction(ActionType.UpdateGraphicsDevice, parameters);
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

}
