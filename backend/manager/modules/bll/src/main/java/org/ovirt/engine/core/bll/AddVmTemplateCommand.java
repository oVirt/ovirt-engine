package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.DiskHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.bll.validator.storage.CinderDisksValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleDiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters.Phase;
import org.ovirt.engine.core.common.action.CreateAllTemplateDisksParameters;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SealVmTemplateParameters;
import org.ovirt.engine.core.common.action.UpdateAllTemplateDisksParameters;
import org.ovirt.engine.core.common.action.UpdateVmVersionParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmTemplateCommand<T extends AddVmTemplateParameters> extends VmTemplateManagementCommand<T>
        implements QuotaStorageDependent, QuotaVdsDependent, SerialChildExecutingCommand {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private CpuProfileHelper cpuProfileHelper;
    @Inject
    private DiskHandler diskHandler;
    @Inject
    private DiskProfileHelper diskProfileHelper;
    @Inject
    protected SnapshotsValidator snapshotsValidator;
    @Inject
    private VmDao vmDao;
    @Inject
    private PermissionDao permissionDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private OsRepository osRepository;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;
    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService schedulerService;
    @Inject
    protected ImagesHandler imagesHandler;

    @Inject
    private MultiLevelAdministrationHandler multiLevelAdministrationHandler;

    @Inject
    private IconUtils iconUtils;

    protected final List<DiskImage> images = new ArrayList<>();
    private Guid[] targetDiskIds;
    private List<PermissionSubject> permissionCheckSubject;
    protected Map<Guid, DiskImage> diskInfoDestinationMap;
    private Map<Guid, List<DiskImage>> sourceImageDomainsImageMap;
    private boolean isVmInDb;
    private boolean pendingAsyncTasks;

    private static final String BASE_TEMPLATE_VERSION_NAME = "base version";
    private static Map<Guid, String> updateVmsJobHashMap = new ConcurrentHashMap<>();
    private static Map<Guid, ScheduledFuture> updateVmsJobMap = new ConcurrentHashMap<>();

    private VmTemplate cachedBaseTemplate;
    private List<CinderDisk> cinderDisks;
    private List<ManagedBlockStorageDisk> managedBlockStorageDisks;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected AddVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        if (Guid.isNullOrEmpty(getParameters().getVmTemplateId())) {
            getParameters().setVmTemplateId(Guid.newGuid());
        }
        setVmTemplateId(getParameters().getVmTemplateId());
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));
        setVmTemplateName(getParameters().getName());
        VmStatic masterVm = getParameters().getMasterVm();
        if (masterVm != null) {
            setVmId(masterVm.getId());
            setClusterId(masterVm.getClusterId());

            // API backward compatibility
            if (getVmDeviceUtils().shouldOverrideSoundDevice(
                    masterVm,
                    getMasterVmCompatibilityVersion(),
                    getParameters().isSoundDeviceEnabled())) {
                getParameters().setSoundDeviceEnabled(true);
            }

            if (getParameters().isSoundDeviceEnabled() == null) {
                getParameters().setSoundDeviceEnabled(false);
            }

            if (getParameters().isTpmEnabled() == null) {
                getParameters().setTpmEnabled(false);
            }
            if (getParameters().isConsoleEnabled() == null) {
                getParameters().setConsoleEnabled(false);
            }
            vmHandler.updateDefaultTimeZone(masterVm);
            vmHandler.autoSelectUsbPolicy(masterVm);

            vmHandler.autoSelectDefaultDisplayType(getVmId(),
                    masterVm,
                    getCluster(),
                    getParameters().getGraphicsDevices());

            vmHandler.autoSelectGraphicsDevice(getVmId(),
                    masterVm,
                    getCluster(),
                    getParameters().getGraphicsDevices(),
                    getMasterVmCompatibilityVersion());

            vmHandler.autoSelectResumeBehavior(masterVm);

            separateCustomProperties(masterVm);
        }
        if (getVm() != null) {
            // template from vm
            updateVmDevices();
            images.addAll(getVmDisksFromDB());
            setStoragePoolId(getVm().getStoragePoolId());
            isVmInDb = true;
            masterVm.setBiosType(getVm().getBiosType());
        } else if (getCluster() != null && masterVm != null) {
            // template from image
            VM vm = new VM(masterVm, new VmDynamic(), null);
            vm.setClusterCompatibilityVersion(getCluster().getCompatibilityVersion());
            vm.setClusterBiosType(getCluster().getBiosType());
            setVm(vm);

            setStoragePoolId(getCluster().getStoragePoolId());
            if (getCluster().getBiosType() != null && getCluster().getBiosType().getChipsetType() == ChipsetType.Q35) {
                masterVm.setBiosType(BiosType.Q35_SEA_BIOS);
            } else {
                masterVm.setBiosType(BiosType.I440FX_SEA_BIOS);
            }
        } else {
            // instance types
            masterVm.setBiosType(null);
        }
        updateDiskInfoDestinationMap();
        generateTargetDiskIds();
        getParameters().setUseCinderCommandCallback(!getCinderDisks().isEmpty());
    }

    protected void separateCustomProperties(VmStatic parameterMasterVm) {
        if (getCluster() != null && parameterMasterVm.isManaged()) {
            // Parses the custom properties field that was filled by frontend to
            // predefined and user defined fields
            VmPropertiesUtils.getInstance().separateCustomPropertiesToUserAndPredefined(
                    getMasterVmCompatibilityVersion(), parameterMasterVm);
        }
    }

    private void updateDiskInfoDestinationMap() {
        diskInfoDestinationMap = getParameters().getDiskInfoDestinationMap();
        if (diskInfoDestinationMap == null) {
            diskInfoDestinationMap = new HashMap<>();
        }
        sourceImageDomainsImageMap = new HashMap<>();
        for (DiskImage image : images) {
            sourceImageDomainsImageMap.computeIfAbsent(image.getStorageIds().get(0), k -> new ArrayList<>()).add(image);
            if (!diskInfoDestinationMap.containsKey(image.getId())) {
                // The volume's format and type were not specified and thus should be null.
                image.setVolumeFormat(null);
                image.setVolumeType(null);
                diskInfoDestinationMap.put(image.getId(), image);
            }
        }
    }

    private void generateTargetDiskIds() {
        targetDiskIds = Stream.generate(Guid::newGuid).limit(images.size()).toArray(Guid[]::new);
    }

    private void updateVmDevices() {
        getVmDeviceUtils().setVmDevices(getVm().getStaticData());
    }

    protected List<DiskImage> getVmDisksFromDB() {
        vmHandler.updateDisksFromDb(getVm());
        vmHandler.filterImageDisksForVM(getVm());
        return getVm().getDiskList();
    }

    private List<CinderDisk> getCinderDisks() {
        if (cinderDisks == null) {
            cinderDisks = DisksFilter.filterCinderDisks(images);
        }
        return cinderDisks;
    }

    private List<ManagedBlockStorageDisk> getManagedBlockStorageDisks() {
        if (managedBlockStorageDisks == null) {
            managedBlockStorageDisks = DisksFilter.filterManagedBlockStorageDisks(images);
        }
        return managedBlockStorageDisks;
    }

    @Override
    protected void executeCommand() {
        // get vm status from db to check its really down before locking
        // relevant only if template created from vm
        if (isVmInDb) {
            VmDynamic vmDynamic = vmDynamicDao.get(getVmId());
            if (!isVmStatusValid(vmDynamic.getStatus())) {
                throw new EngineException(EngineError.IRS_IMAGE_STATUS_ILLEGAL);
            }
            lockOps(vmDynamic, getCompensationContext());
        }
        setActionReturnValue(Guid.Empty);

        // set template id as base for new templates
        if (!isTemplateVersion()) {
            getParameters().setBaseTemplateId(getVmTemplateId());
            if (StringUtils.isEmpty(getParameters().getTemplateVersionName())) {
                getParameters().setTemplateVersionName(BASE_TEMPLATE_VERSION_NAME);
            }
        } else {
            // template version name should be the same as the base template name
            setVmTemplateName(getBaseTemplate().getName());
            String jobId = updateVmsJobHashMap.remove(getParameters().getBaseTemplateId());
            if (!StringUtils.isEmpty(jobId)) {
                log.info("Cancelling current running update for vms for base template id '{}'", getParameters().getBaseTemplateId());
                try {
                    updateVmsJobMap.remove(getParameters().getBaseTemplateId()).cancel(true);
                } catch (Exception e) {
                    log.warn("Failed deleting job '{}' at cancelRecoveryJob", jobId);
                }
            }
        }


        TransactionSupport.executeInNewTransaction(() -> {
            addVmTemplateToDb();
            getCompensationContext().stateChanged();
            return null;
        });

        if (!getParameters().getMasterVm().isManaged()) {
            TransactionSupport.executeInNewTransaction(() -> {
                addPermission();
                return null;
            });
            endSuccessfullySynchronous();
            return;
        }

        final Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping = addAllTemplateDisks();
        srcDeviceIdToTargetDeviceIdMapping
                .forEach((oldImageId, newImageId) -> addTemplateDiskVmElement(newImageId, oldImageId));

        TransactionSupport.executeInNewTransaction(() -> {
            addPermission();
            addVmInterfaces(srcDeviceIdToTargetDeviceIdMapping);
            Set<GraphicsType> graphicsToSkip = getParameters().getGraphicsDevices().keySet();
            if (isVmInDb) {
                getVmDeviceUtils().copyVmDevices(getVmId(),
                        getVmTemplateId(),
                        srcDeviceIdToTargetDeviceIdMapping,
                        getParameters().isSoundDeviceEnabled(),
                        getParameters().isTpmEnabled(),
                        getParameters().isConsoleEnabled(),
                        getParameters().isVirtioScsiEnabled(),
                        graphicsToSkip,
                        false,
                        getEffectiveCompatibilityVersion());
                getVmDeviceUtils().copyVmExternalData(getVmId(), getVmTemplateId());
            } else {
                // for instance type and new template without a VM
                getVmDeviceUtils().copyVmDevices(VmTemplateHandler.BLANK_VM_TEMPLATE_ID,
                        getVmTemplateId(),
                        srcDeviceIdToTargetDeviceIdMapping,
                        getParameters().isSoundDeviceEnabled(),
                        getParameters().isTpmEnabled(),
                        getParameters().isConsoleEnabled(),
                        getParameters().isVirtioScsiEnabled(),
                        graphicsToSkip,
                        false,
                        getEffectiveCompatibilityVersion());
            }

            updateWatchdog(getVmTemplateId());
            updateRngDevice(getVmTemplateId());
            addGraphicsDevice();

            setSucceeded(true);
            return null;
        });

        if (getParameters().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            vmHandler.warnMemorySizeLegal(getVmTemplate(), getVm().getCompatibilityVersion());
        }

        // means that there are no asynchronous tasks to execute and that we can
        // end the command synchronously
        pendingAsyncTasks = !getReturnValue().getVdsmTaskIdList().isEmpty() ||
                !commandCoordinatorUtil.getChildCommandIds(getCommandId()).isEmpty();
        if (!pendingAsyncTasks) {
            endSuccessfullySynchronous();
        }
    }

    /**
     * Execute {@link org.ovirt.engine.core.bll.storage.disk.CreateAllTemplateDisksCommand} to create all disks
     * of the template.
     *
     * @return a map where keys are IDs of the disks that were copied and values are IDs of the corresponding disks
     *         of the template that were created
     */
    protected Map<Guid, Guid> addAllTemplateDisks() {
        ActionReturnValue returnValue = runInternalAction(
                getAddAllTemplateDisksActionType(),
                buildCreateAllTemplateDisksParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!returnValue.getSucceeded()) {
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }
        return returnValue.getActionReturnValue();
    }

    protected CreateAllTemplateDisksParameters buildCreateAllTemplateDisksParameters() {
        CreateAllTemplateDisksParameters parameters =
                new CreateAllTemplateDisksParameters(getVm() != null ? getVmId() : Guid.Empty);
        fillCreateAllTemplateDisksParameters(parameters);
        return parameters;
    }

    protected void fillCreateAllTemplateDisksParameters(CreateAllTemplateDisksParameters parameters) {
        parameters.setVmTemplateId(getVmTemplateId());
        parameters.setVmTemplateName(getVmTemplateName());
        parameters.setDiskInfoDestinationMap(diskInfoDestinationMap);
        parameters.setTargetDiskIds(targetDiskIds);
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));
        if (getParameters().isSealTemplate()) {
            parameters.setCopyVolumeType(CopyVolumeType.LeafVol);
        }
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
    }

    protected ActionType getAddAllTemplateDisksActionType() {
        return ActionType.CreateAllTemplateDisks;
    }

    private Version getMasterVmCompatibilityVersion() {
        return getVm() == null
                ? CompatibilityVersionUtils.getEffective(getParameters().getMasterVm(), getCluster())
                : getVm().getCompatibilityVersion();
    }

    private Version getEffectiveCompatibilityVersion() {
        return CompatibilityVersionUtils.getEffective(getParameters().getMasterVm(), this::getCluster);
    }

    /**
     * Add graphics based on parameters.
     */
    private void addGraphicsDevice() {
        for (GraphicsDevice graphicsDevice : getParameters().getGraphicsDevices().values()) {
            if (graphicsDevice == null) {
                continue;
            }

            graphicsDevice.setVmId(getVmTemplateId());
            GraphicsParameters parameters = new GraphicsParameters(graphicsDevice).setVm(false);
            backend.runInternalAction(ActionType.AddGraphicsDevice, parameters);
        }
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (!getParameters().isSealTemplate()) {
            return false;
        }

        restoreCommandState();

        switch (getParameters().getPhase()) {
            case CREATE_TEMPLATE:
                getParameters().setPhase(Phase.ASSIGN_ILLEGAL);
                break;

            case ASSIGN_ILLEGAL:
                getParameters().setPhase(Phase.SEAL);
                break;

            case SEAL:
                getParameters().setPhase(Phase.ASSIGN_LEGAL_SHARED);
                break;

            case ASSIGN_LEGAL_SHARED:
                return false;
        }
        persistCommandIfNeeded();
        executeNextOperation();
        return true;
    }

    private void executeNextOperation() {
        switch (getParameters().getPhase()) {
            case ASSIGN_ILLEGAL:
                assignLegalAndShared(false);
                break;

            case SEAL:
                sealVmTemplate();
                break;

            case ASSIGN_LEGAL_SHARED:
                assignLegalAndShared(true);
                break;
        }
    }

    private void assignLegalAndShared(boolean legalAndShared) {
        ActionReturnValue returnValue = runInternalAction(ActionType.UpdateAllTemplateDisks,
                buildUpdateAllTemplateDisksParameters(legalAndShared),
                ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!returnValue.getSucceeded()) {
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }
    }

    private UpdateAllTemplateDisksParameters buildUpdateAllTemplateDisksParameters(boolean legalAndShared) {
        UpdateAllTemplateDisksParameters parameters = new UpdateAllTemplateDisksParameters(getVmTemplateId(),
                legalAndShared,
                legalAndShared ? true : null);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return parameters;
    }

    private void sealVmTemplate() {
        ActionReturnValue returnValue = runInternalAction(ActionType.SealVmTemplate,
                buildSealVmTemplateParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!returnValue.getSucceeded()) {
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }
    }

    private SealVmTemplateParameters buildSealVmTemplateParameters() {
        SealVmTemplateParameters parameters = new SealVmTemplateParameters();
        parameters.setVmTemplateId(getVmTemplateId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return parameters;
    }

    @Override
    protected boolean validate() {
        if (isInternalExecution() && !getParameters().getMasterVm().isManaged()) {
            return setAndValidateCpuProfile();
        }

        boolean isInstanceType = getParameters().getTemplateType() == VmEntityType.INSTANCE_TYPE;
        if (getCluster() == null && !isInstanceType) {
            return failValidation(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
        }

        if (!validate(vmHandler.isVmPriorityValueLegal(getParameters().getMasterVm().getPriority()))) {
            return false;
        }

        if (isVmInDb && !isVmStatusValid(getVm().getStatus())) {
            return failValidation(EngineMessage.VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM);
        }
        // validate uniqueness of template name. If template is a regular template, uniqueness
        // is considered in context of the datacenter. If template is an 'Instance' name must
        // be unique also across datacenters.
        if (!isTemplateVersion()) {
            if (isInstanceType) {
                if (isInstanceWithSameNameExists(getVmTemplateName())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                }
            } else {
                if (isVmTemplateWithSameNameExist(getVmTemplateName(), getCluster().getStoragePoolId())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                }
            }
        }

        if (isTemplateVersion()) {
            VmTemplate userSelectedBaseTemplate = getBaseTemplate();
            if (userSelectedBaseTemplate == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            } else if (!userSelectedBaseTemplate.isBaseTemplate()) {
                // currently template version cannot be base template
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_VERSION_CANNOT_BE_BASE_TEMPLATE);

            }
        }

        if (isTemplateVersion() && getBaseTemplate().isBlank()) {
            return failValidation(EngineMessage.BLANK_TEMPLATE_CANT_HAVE_SUBTEMPLATES);
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        if (!setAndValidateCpuProfile()) {
            return false;
        }

        if (!isDisksAliasNotEmpty()) {
            return false;
        }

        if (getParameters().getVmLargeIcon() != null && !validate(IconValidator.validate(
                IconValidator.DimensionsType.LARGE_CUSTOM_ICON,
                getParameters().getVmLargeIcon()))) {
            return false;
        }

        if (getParameters().getMasterVm().getSmallIconId() != null
                && getParameters().getVmLargeIcon() == null // icon id is ignored if large icon is sent
                && !validate(IconValidator.validateIconId(getParameters().getMasterVm().getSmallIconId(), "Small"))) {
            return false;
        }

        if (getParameters().getMasterVm().getLargeIconId() != null
                && getParameters().getVmLargeIcon() == null // icon id is ignored if large icon is sent
                && !validate(IconValidator.validateIconId(getParameters().getMasterVm().getLargeIconId(), "Large"))) {
            return false;
        }

        if (getParameters().getWatchdog() != null) {
            if (!validate(new VmWatchdogValidator.VmWatchdogClusterIndependentValidator(
                            getParameters().getWatchdog()).isValid())) {
                return false;
            }
        }

        if (!validate(vmHandler.validateMaxMemorySize(
                getParameters().getMasterVm(),
                CompatibilityVersionUtils.getEffective(getParameters().getMasterVm(), this::getCluster)))) {
            return false;
        }

        if (getParameters().isSealTemplate() && vmHandler.isWindowsVm(getVm())) {
            return failValidation(EngineMessage.VM_TEMPLATE_CANNOT_SEAL_WINDOWS);
        }

        if (getParameters().getMasterVm().getClusterId() != null && getParameters().getMasterVm().getBiosType() == null) {
            return failValidation(EngineMessage.VM_TEMPLATE_WITH_CLUSTER_WITHOUT_BIOS_TYPE);
        }

        if (isInstanceType) {
            return true;
        }

        return validateCluster()
                && validateImages()
                && validate(VmValidator.validateCpuSockets(
                        getParameters().getMasterVm(),
                        getVm().getCompatibilityVersion(),
                        getCluster().getArchitecture(),
                        osRepository));
    }

    protected boolean isVmStatusValid(VMStatus status) {
        return status == VMStatus.Down;
    }

    protected boolean setAndValidateCpuProfile() {
        // cpu profile isn't supported for instance types.
        if (getParameters().getTemplateType() == VmEntityType.INSTANCE_TYPE) {
            return true;
        }
        return validate(cpuProfileHelper.setAndValidateCpuProfile(
                getParameters().getMasterVm(),
                getUserIdIfExternal().orElse(null)));
    }

    protected boolean isDisksAliasNotEmpty() {
        // Check that all the template's allocated disk's aliases are not an empty string.
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            if (StringUtils.isEmpty(diskImage.getDiskAlias())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_CANNOT_BE_CREATED_WITH_EMPTY_DISK_ALIAS);
            }
        }
        return true;
    }

    protected boolean setAndValidateDiskProfiles() {
        if (diskInfoDestinationMap != null && !diskInfoDestinationMap.isEmpty()) {
            Map<DiskImage, Guid> map = diskInfoDestinationMap.values().stream()
                    .filter(DisksFilter.ONLY_IMAGES)
                    .collect(Collectors.toMap(Function.identity(), d -> d.getStorageIds().get(0)));
            return validate(diskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
        }
        return true;
    }

    private VmTemplate getBaseTemplate() {
        if (cachedBaseTemplate == null) {
            cachedBaseTemplate = vmTemplateDao.get(getParameters().getBaseTemplateId());
        }
        return cachedBaseTemplate;
    }

    private boolean isTemplateVersion() {
        return getParameters().getBaseTemplateId() != null;
    }

    private boolean validateCluster() {
        // A Template cannot be added in a cluster without a defined architecture
        if (getCluster().getArchitecture() == ArchitectureType.undefined) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
        }

        if (!validate(vmHandler.isOsTypeSupported(getParameters().getMasterVm().getOsId(),
                getCluster().getArchitecture()))) {
            return false;
        }

        // Check if the display type is supported
        Guid srcId = isVmInDb ? getVmId() : VmTemplateHandler.BLANK_VM_TEMPLATE_ID;
        if (!validate(vmHandler.isGraphicsAndDisplaySupported(getParameters().getMasterVm().getOsId(),
                vmHandler.getResultingVmGraphics(getVmDeviceUtils().getGraphicsTypesOfEntity(srcId),
                        getParameters().getGraphicsDevices()),
                getParameters().getMasterVm().getDefaultDisplayType(),
                getVm().getBiosType(),
                getVm().getCompatibilityVersion()))) {
            return false;
        }

        if (!validate(vmHandler.validateSmartCardDevice(getParameters().getMasterVm()))) {
            return false;
        }

        // Check if the watchdog model is supported
        if (getParameters().getWatchdog() != null) {
            if (!validate(new VmWatchdogValidator.VmWatchdogClusterDependentValidator(getParameters().getMasterVm().getOsId(),
                    getParameters().getWatchdog(),
                    getVm().getCompatibilityVersion()).isValid())) {
                return false;
            }
        }

        // Disallow cross-DC template creation
        if (!getStoragePoolId().equals(getCluster().getStoragePoolId())) {
            addValidationMessage(EngineMessage.VDS_CLUSTER_ON_DIFFERENT_STORAGE_POOL);
            return false;
        }

        if (!VmPropertiesUtils.getInstance().validateVmProperties(
                getVm().getCompatibilityVersion(),
                getParameters().getMasterVm().getCustomProperties(),
                getReturnValue().getValidationMessages())) {
            return false;
        }

        return true;
    }

    protected boolean validateImages() {
        // images related checks
        if (!images.isEmpty()) {
            if (!validateVmNotDuringSnapshot()) {
                return false;
            }

            if (!validate(new StoragePoolValidator(getStoragePool()).existsAndUp())) {
                return false;
            }

            List<CinderDisk> cinderDisks = getCinderDisks();
            CinderDisksValidator cinderDisksValidator = new CinderDisksValidator(cinderDisks);
            if (!validate(cinderDisksValidator.validateCinderDiskLimits())) {
                return false;
            }

            if (!validate(isPassDiscardSupportedForImagesDestSds())) {
                return false;
            }

            List<DiskImage> diskImagesToCheck = DisksFilter.filterImageDisks(images, ONLY_NOT_SHAREABLE, ONLY_ACTIVE);
            diskImagesToCheck.addAll(cinderDisks);
            diskImagesToCheck.addAll(getManagedBlockStorageDisks());
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(diskImagesToCheck);
            if (!validate(diskImagesValidator.diskImagesNotIllegal()) ||
                    !validate(diskImagesValidator.diskImagesNotLocked())) {
                return false;
            }

            MultipleStorageDomainsValidator storageDomainsValidator =
                    getStorageDomainsValidator(getStoragePoolId(), sourceImageDomainsImageMap.keySet());
            if (!validate(storageDomainsValidator.allDomainsExistAndActive())) {
                return false;
            }

            if (!validate(storageDomainsValidator.isSupportedByManagedBlockStorageDomains(getActionType()))) {
                return false;
            }

            Set<Guid> destImageDomains = getStorageGuidSet();
            destImageDomains.removeAll(sourceImageDomainsImageMap.keySet());
            for (Guid destImageDomain : destImageDomains) {
                StorageDomain storage = storageDomainDao.getForStoragePool(destImageDomain, getVm().getStoragePoolId());
                if (storage == null) {
                    // if storage is null then we need to check if it doesn't exist or
                    // domain is not in the same storage pool as the vm
                    if (storageDomainStaticDao.get(destImageDomain) == null) {
                        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
                    } else {
                        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_IN_STORAGE_POOL);
                    }
                    return false;
                }
                if (storage.getStatus() == null || storage.getStatus() != StorageDomainStatus.Active) {
                    addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
                    return false;
                }

                if (storage.getStorageDomainType().isIsoOrImportExportDomain()) {

                    addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                    return false;
                }
            }
            return validateSpaceRequirements();
        }
        return true;
    }

    protected ValidationResult isPassDiscardSupportedForImagesDestSds() {
        Map<Disk, DiskVmElement> diskToDiskVmElement = diskHandler.getDiskToDiskVmElementMap(
                getVm().getId(), diskInfoDestinationMap);
        Map<Guid, Guid> diskIdToDestSdId = diskInfoDestinationMap.values().stream()
                .collect(Collectors.toMap(DiskImage::getId, diskImage -> diskImage.getStorageIds().get(0)));

        MultipleDiskVmElementValidator multipleDiskVmElementValidator =
                createMultipleDiskVmElementValidator(diskToDiskVmElement);
        return multipleDiskVmElementValidator.isPassDiscardSupportedForDestSds(diskIdToDestSdId);
    }

    protected MultipleDiskVmElementValidator createMultipleDiskVmElementValidator(
            Map<Disk, DiskVmElement> diskToDiskVmElement) {
        return new MultipleDiskVmElementValidator(diskToDiskVmElement);
    }

    protected boolean validateSpaceRequirements() {
        // update vm snapshots for storage free space check
        imagesHandler.fillImagesBySnapshots(getVm());
        List<DiskImage>  disksList =  DisksFilter.filterImageDisks(getVm().getDiskMap().values(), ONLY_NOT_SHAREABLE,
                ONLY_ACTIVE);
        List<DiskImage> disksListForStorageChecks = createDiskDummiesForSpaceValidations(disksList);
        MultipleStorageDomainsValidator multipleSdValidator = getStorageDomainsValidator(
                getVm().getStoragePoolId(), getStorageGuidSet());

        return validate(multipleSdValidator.allDomainsWithinThresholds())
                && validate(multipleSdValidator.allDomainsHaveSpaceForClonedDisks(disksListForStorageChecks));
    }

    protected MultipleStorageDomainsValidator getStorageDomainsValidator(Guid spId, Set<Guid> disks) {
        return new MultipleStorageDomainsValidator(spId, disks);
    }

    protected boolean validateVmNotDuringSnapshot() {
        return validate(snapshotsValidator.vmNotDuringSnapshot(getVmId()));
    }

    private Set<Guid> getStorageGuidSet() {
        return diskInfoDestinationMap.values().stream().map(d -> d.getStorageIds().get(0)).collect(Collectors.toSet());
    }

    /**
     * Space Validations are done using data extracted from the disks. The disks in question in this command
     * don't have all the needed data, and in order not to contaminate the command's data structures, an alter
     * one is created specifically for this validation - hence dummy.
     */
    private List<DiskImage> createDiskDummiesForSpaceValidations(Collection<DiskImage> disksList) {
        List<DiskImage> dummies = new ArrayList<>(disksList.size());
        for (DiskImage image : disksList) {
            Guid targetSdId = diskInfoDestinationMap.get(image.getId()).getStorageIds().get(0);
            DiskImage dummy = imagesHandler.createDiskImageWithExcessData(image, targetSdId);
            dummies.add(dummy);
        }
        return dummies;
    }

    private void addVmTemplateToDb() {
        // TODO: add timezone handling
        setVmTemplate(
                new VmTemplate(
                        0,
                        new Date(),
                        getParameters().getDescription(),
                        getParameters().getMasterVm().getComment(),
                        getParameters().getMasterVm().getMemSizeMb(),
                        getParameters().getMasterVm().getMaxMemorySizeMb(),
                        getVmTemplateName(),
                        getParameters().getMasterVm().getNumOfSockets(),
                        getParameters().getMasterVm().getCpuPerSocket(),
                        getParameters().getMasterVm().getThreadsPerCpu(),
                        getParameters().getMasterVm().getOsId(),
                        getParameters().getMasterVm().getClusterId(),
                        getVmTemplateId(),
                        getParameters().getMasterVm().getNumOfMonitors(),
                        VmTemplateStatus.Locked.getValue(),
                        getParameters().getMasterVm().getUsbPolicy().getValue(),
                        getParameters().getMasterVm().getTimeZone(),
                        getParameters().getMasterVm().getNiceLevel(),
                        getParameters().getMasterVm().getCpuShares(),
                        getParameters().getMasterVm().getDefaultBootSequence(),
                        getParameters().getMasterVm().getVmType(),
                        getParameters().getMasterVm().isSmartcardEnabled(),
                        getParameters().getMasterVm().isDeleteProtected(),
                        getParameters().getMasterVm().getSsoMethod(),
                        getParameters().getMasterVm().getTunnelMigration(),
                        getParameters().getMasterVm().getVncKeyboardLayout(),
                        getParameters().getMasterVm().getMinAllocatedMem(),
                        getParameters().getMasterVm().isStateless(),
                        getParameters().getMasterVm().isRunAndPause(),
                        getUserId(),
                        getParameters().getTemplateType(),
                        getParameters().getMasterVm().isAutoStartup(),
                        getParameters().getMasterVm().getPriority(),
                        getParameters().getMasterVm().getDefaultDisplayType(),
                        getParameters().getMasterVm().getInitrdUrl(),
                        getParameters().getMasterVm().getKernelUrl(),
                        getParameters().getMasterVm().getKernelParams(),
                        getParameters().getMasterVm().getQuotaId(),
                        getParameters().getMasterVm().getDedicatedVmForVdsList(),
                        getParameters().getMasterVm().getMigrationSupport(),
                        getParameters().getMasterVm().isAllowConsoleReconnect(),
                        getParameters().getMasterVm().getIsoPath(),
                        getParameters().getMasterVm().getMigrationDowntime(),
                        getParameters().getBaseTemplateId(),
                        getParameters().getTemplateVersionName(),
                        getParameters().getMasterVm().getSerialNumberPolicy(),
                        getParameters().getMasterVm().getCustomSerialNumber(),
                        getParameters().getMasterVm().isBootMenuEnabled(),
                        getParameters().getMasterVm().isSpiceFileTransferEnabled(),
                        getParameters().getMasterVm().isSpiceCopyPasteEnabled(),
                        getParameters().getMasterVm().getCpuProfileId(),
                        getParameters().getMasterVm().getAutoConverge(),
                        getParameters().getMasterVm().getMigrateCompressed(),
                        getParameters().getMasterVm().getMigrateEncrypted(),
                        getParameters().getMasterVm().getUserDefinedProperties(),
                        getParameters().getMasterVm().getPredefinedProperties(),
                        getParameters().getMasterVm().getCustomProperties(),
                        getParameters().getMasterVm().getCustomEmulatedMachine(),
                        getParameters().getMasterVm().getCustomCpuName(),
                        getParameters().getMasterVm().isUseHostCpuFlags(),
                        getParameters().getMasterVm().getSmallIconId(),
                        getParameters().getMasterVm().getLargeIconId(),
                        getParameters().getMasterVm().getNumOfIoThreads(),
                        getParameters().getMasterVm().getConsoleDisconnectAction(),
                        getParameters().getMasterVm().getConsoleDisconnectActionDelay(),
                        getParameters().getMasterVm().getCustomCompatibilityVersion(),
                        getParameters().getMasterVm().getMigrationPolicyId(),
                        getParameters().getMasterVm().getLeaseStorageDomainId(),
                        getParameters().getMasterVm().getResumeBehavior(),
                        getParameters().getMasterVm().isMultiQueuesEnabled(),
                        getParameters().getMasterVm().getUseTscFrequency(),
                        getParameters().getMasterVm().getCpuPinning(),
                        getParameters().getMasterVm().getVirtioScsiMultiQueues(),
                        getParameters().getMasterVm().isBalloonEnabled(),
                        getParameters().getMasterVm().getBiosType(),
                        getParameters().getMasterVm().getCpuPinningPolicy()));
        getVmTemplate().setOrigin(getParameters().getMasterVm().getOrigin());
        updateVmIcons();
        getVmTemplate().setSealed(getParameters().isSealTemplate());
        vmTemplateDao.save(getVmTemplate());
        getCompensationContext().snapshotNewEntity(getVmTemplate());
        setActionReturnValue(getVmTemplate().getId());
        // Load Vm Init from DB and set it to the template
        vmHandler.updateVmInitFromDB(getParameters().getMasterVm(), false);
        getVmTemplate().setVmInit(getParameters().getMasterVm().getVmInit());
        vmHandler.addVmInitToDB(getVmTemplate().getVmInit());
    }

    private void updateVmIcons() {
        if (getParameters().getVmLargeIcon() != null) {
            final VmIconIdSizePair iconIdPair = iconUtils.ensureIconPairInDatabase(getParameters().getVmLargeIcon());
            getVmTemplate().setSmallIconId(iconIdPair.getSmall());
            getVmTemplate().setLargeIconId(iconIdPair.getLarge());
        }
    }

    private void addVmInterfaces(Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping) {
        List<VmNic> interfaces = vmNicDao.getAllForVm(getParameters().getMasterVm().getId());
        for (VmNic iface : interfaces) {
            VmNic iDynamic = new VmNic();
            iDynamic.setId(Guid.newGuid());
            iDynamic.setVmId(getVmTemplateId());
            iDynamic.setName(iface.getName());
            iDynamic.setVnicProfileId(iface.getVnicProfileId());
            iDynamic.setSpeed(VmInterfaceType.forValue(iface.getType()).getSpeed());
            iDynamic.setType(iface.getType());
            iDynamic.setLinked(iface.isLinked());
            vmNicDao.save(iDynamic);
            srcDeviceIdToTargetDeviceIdMapping.put(iface.getId(), iDynamic.getId());
        }
    }

    private void addTemplateDiskVmElement(Guid newDiskId, Guid oldDiskId) {
        DiskVmElement oldDve = diskVmElementDao.get(new VmDeviceId(oldDiskId, getVmId()));
        DiskVmElement newDve = DiskVmElement.copyOf(oldDve);
        newDve.setId(new VmDeviceId(newDiskId, getVmTemplateId()));
        diskVmElementDao.save(newDve);
    }

    private Guid getVmIdFromImageParameters(){
        return getParameters().getMasterVm().getId();
    }

    private void restoreCommandState() {
        setVmId(getVmIdFromImageParameters());
        isVmInDb = !getVmId().equals(Guid.Empty) && getVm() != null;
    }

    @Override
    protected void endSuccessfully() {
        restoreCommandState();

        vmStaticDao.incrementDbGeneration(getVmTemplateId());
        if (reloadVmTemplateFromDB() != null) {
            endDefaultOperations();
        }
        checkTrustedService();
        setSucceeded(true);
    }

    private void checkTrustedService() {
        if (!isVmInDb) {
            return;
        }
        if (getVm().isTrustedService() && !getVmTemplate().isTrustedService()) {
            auditLogDirector.log(this, AuditLogType.USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        } else if (!getVm().isTrustedService() && getVmTemplate().isTrustedService()) {
            auditLogDirector.log(this, AuditLogType.USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    private void endSuccessfullySynchronous() {
        if (reloadVmTemplateFromDB() != null) {
            endDefaultOperations();
        }
        setSucceeded(true);
    }

    private void endDefaultOperations() {
        endUnlockOps();

        // in case of new version of a template, update vms marked to use latest
        if (isTemplateVersion()) {
            updateVmsJobHashMap.put(getParameters().getBaseTemplateId(), StringUtils.EMPTY);
            ScheduledFuture job = schedulerService.schedule(this::updateVmVersion, 0, TimeUnit.SECONDS);
            updateVmsJobMap.put(getParameters().getBaseTemplateId(), job);
            updateVmsJobHashMap.put(getParameters().getBaseTemplateId(), Integer.toString(job.hashCode()));
        }
    }

    private void updateVmVersion() {
        for (Guid vmId : vmDao.getVmIdsForVersionUpdate(getParameters().getBaseTemplateId())) {
            // if the job was removed, stop executing, we probably have new version creation going on
            if (!updateVmsJobHashMap.containsKey(getParameters().getBaseTemplateId())) {
                break;
            }
            UpdateVmVersionParameters params = new UpdateVmVersionParameters(vmId);
            params.setSessionId(getParameters().getSessionId());
            backend.runInternalAction(ActionType.UpdateVmVersion, params, cloneContextAndDetachFromParent());
        }
        updateVmsJobHashMap.remove(getParameters().getBaseTemplateId());
        updateVmsJobMap.remove(getParameters().getBaseTemplateId());
    }

    protected void endUnlockOps() {
        if (isVmInDb) {
            unLockVm(getVm());
        }
        vmTemplateHandler.unlockVmTemplate(getVmTemplateId());
    }

    protected void lockOps(VmDynamic vmDynamic, CompensationContext context) {
        vmHandler.lockVm(vmDynamic, context);
    }

    private VmTemplate reloadVmTemplateFromDB() {
        // set it to null to reload the template from the db
        setVmTemplate(null);
        return getVmTemplate();
    }

    @Override
    protected void endWithFailure() {
        // We evaluate 'VmTemplate' so it won't be null in the last 'if'
        // statement.
        // (a template without images doesn't exist in the 'vm_template_view').
        restoreCommandState();

        if (commandCoordinatorUtil.getCommandExecutionStatus(getParameters().getCommandId()) == CommandExecutionStatus.EXECUTED) {
            // if template exist in db remove it
            if (getVmTemplate() != null) {
                vmTemplateDao.remove(getVmTemplateId());
                removeNetwork();
            }
        }

        if (!getVmId().equals(Guid.Empty) && getVm() != null) {
            unLockVm(getVm());
        }

        setSucceeded(true);
    }

    protected void unLockVm(VM vm) {
        vmHandler.unLockVm(vm);
    }

    /**
     * in case of non-existing cluster the backend query will return a null
     */
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permissionCheckSubject == null) {
            permissionCheckSubject = new ArrayList<>();
            if (getParameters().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
                Guid storagePoolId = getCluster() == null ? null : getCluster().getStoragePoolId();
                permissionCheckSubject.add(new PermissionSubject(storagePoolId,
                        VdcObjectType.StoragePool,
                        getActionType().getActionGroup()));

                // host-specific parameters can be changed by administration role only
                if (!new HashSet<>(getParameters().getMasterVm().getDedicatedVmForVdsList())
                        .equals(new HashSet<>(getVm().getDedicatedVmForVdsList()))
                        || !StringUtils.isEmpty(getParameters().getMasterVm().getCpuPinning())) {
                    permissionCheckSubject.add(
                            new PermissionSubject(storagePoolId,
                                    VdcObjectType.StoragePool,
                                    ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES));
                }
            } else {
                permissionCheckSubject.add(new PermissionSubject(Guid.SYSTEM,
                        VdcObjectType.System,
                        getActionType().getActionGroup()));
            }
        }

        return permissionCheckSubject;
    }

    private void addPermission() {
        UniquePermissionsSet permissionsToAdd = new UniquePermissionsSet();
        if (getCurrentUser() == null) {
            setCurrentUser(getParameters().getParametersCurrentUser());
        }

        // current user can be null for KubeVirt templates added internally by TemplatesMonitoring
        if (getCurrentUser() != null) {
            addPermissionForTemplate(permissionsToAdd, getCurrentUser().getId(), PredefinedRoles.TEMPLATE_OWNER);
        }

        // if the template is for public use, set EVERYONE as a TEMPLATE_USER.
        if (getParameters().isPublicUse()) {
            addPermissionForTemplate(permissionsToAdd, MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID, PredefinedRoles.TEMPLATE_USER);
        } else {
            addPermissionForTemplate(permissionsToAdd, getCurrentUser().getId(), PredefinedRoles.TEMPLATE_USER);
        }

        copyVmPermissions(permissionsToAdd);

        if (!permissionsToAdd.isEmpty()) {
            List<Permission> permissionsList = permissionsToAdd.asPermissionList();
            multiLevelAdministrationHandler.addPermission(permissionsList.toArray(new Permission[permissionsList.size()]));
        }
    }

    private void copyVmPermissions(UniquePermissionsSet permissionsToAdd) {
        if (!isVmInDb || !getParameters().isCopyVmPermissions()) {
            return;
        }

        List<Permission> vmPermissions = permissionDao.getAllForEntity(getVmId(), getEngineSessionSeqId(), false);

        for (Permission vmPermission : vmPermissions) {
            permissionsToAdd.addPermission(vmPermission.getAdElementId(), vmPermission.getRoleId(),
                    getParameters().getVmTemplateId(), VdcObjectType.VmTemplate);
        }

    }

    private void addPermissionForTemplate(UniquePermissionsSet permissionsToAdd, Guid userId, PredefinedRoles role) {
        permissionsToAdd.addPermission(userId, role.getId(), getParameters().getVmTemplateId(), VdcObjectType.VmTemplate);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
            case EXECUTE:
                if (isVmInDb) {
                    if (pendingAsyncTasks) {
                        return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE : AuditLogType.USER_FAILED_ADD_VM_TEMPLATE;
                    } else {
                        return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_SUCCESS : getAuditLogFailureType();
                    }
                } else {
                    return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE_SUCCESS : AuditLogType.USER_ADD_VM_TEMPLATE_FAILURE;
                }

            case END_SUCCESS:
                if (getParameters().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
                    return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_SUCCESS : getAuditLogFailureType();
                }
                return AuditLogType.UNASSIGNED;

            default:
                return getAuditLogFailureType();
        }
    }

    private AuditLogType getAuditLogFailureType() {
        switch (getParameters().getPhase()) {
            case CREATE_TEMPLATE:
                return AuditLogType.USER_ADD_VM_TEMPLATE_CREATE_TEMPLATE_FAILURE;
            case ASSIGN_ILLEGAL:
                return AuditLogType.USER_ADD_VM_TEMPLATE_ASSIGN_ILLEGAL_FAILURE;
            case SEAL:
                return AuditLogType.USER_ADD_VM_TEMPLATE_SEAL_FAILURE;
            case ASSIGN_LEGAL_SHARED:
            default:
                return AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_FAILURE;
        }
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("phase", getParameters().getPhase().name());
        }
        return jobProperties;
    }

    @Override
    protected boolean isQuotaDependant() {
        if (getParameters().getTemplateType() == VmEntityType.INSTANCE_TYPE) {
            return false;
        }

        return super.isQuotaDependant();
    }

    private Guid getQuotaIdForDisk(DiskImage diskImage) {
        // If the DiskInfoDestinationMap is available and contains information about the disk
        if (getParameters().getDiskInfoDestinationMap() != null
                && getParameters().getDiskInfoDestinationMap().get(diskImage.getId()) != null) {
            return  getParameters().getDiskInfoDestinationMap().get(diskImage.getId()).getQuotaId();
        }
        return diskImage.getQuotaId();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        return getVm().getDiskList()
                .stream()
                .map(disk -> new QuotaStorageConsumptionParameter(
                        getQuotaIdForDisk(disk),
                        QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
                        disk.getStorageIds().get(0),
                        (double) disk.getSizeInGigabytes()))
                .collect(Collectors.toList());
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        Guid quotaId = getQuotaManager().getFirstQuotaForUser(
                getParameters().getMasterVm().getQuotaId(),
                getStoragePoolId(),
                getCurrentUser());

        List<QuotaConsumptionParameter> list = new ArrayList<>();
        list.add(new QuotaSanityParameter(quotaId));
        return list;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        if (getVmTemplateName() != null && !isTemplateVersion()) {
            locks.put(getVmTemplateName(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE_NAME,
                            EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NAME_IS_USED));
        }
        locks.put(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE,
                        EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_CREATED));
        Arrays.stream(targetDiskIds)
                .forEach(id -> locks.put(id.toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK,
                                EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_CREATED)));
        return locks;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();

        if (isTemplateVersion()) {
            locks.put(getParameters().getBaseTemplateId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE,
                            EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_VERSION_IS_BEING_CREATED));
        }
        if (!Guid.isNullOrEmpty(getParameters().getVm().getId())) {
            locks.put(getParameters().getVm().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM,
                            EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_CREATED_FROM_VM));
        }

        return locks;
    }

    @Override
    public CommandCallback getCallback() {
        return getParameters().getMasterVm().isManaged() ? callbackProvider.get() : null;
    }

}
