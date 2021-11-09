package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ConsoleDisconnectAction;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmResumeBehavior;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.HasValidatedTabs;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.ModelWithMigrationsOptions;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.ValidationCompleteEvent;
import org.ovirt.engine.ui.uicommonweb.models.VirtioMultiQueueType;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.NumaSupportModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VmNumaSupportModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateWithVersion;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.CpuNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.GuidValidation;
import org.ovirt.engine.ui.uicommonweb.validation.I18NExtraNameOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IconWithOsDefaultValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NoTrimmingWhitespacesValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyQuotaValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotNullIntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class UnitVmModel extends Model implements HasValidatedTabs, ModelWithMigrationsOptions {

    public static final int VM_TEMPLATE_AND_INSTANCE_TYPE_NAME_MAX_LIMIT = 40;
    public static final int DESCRIPTION_MAX_LIMIT = 255;

    static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    private boolean privateIsNew;

    private boolean privateIsClone;

    private EntityModel<Boolean> valid;

    public EntityModel<Boolean> getValid() {
        return valid;
    }

    public void setValid(EntityModel<Boolean> valid) {
        this.valid = valid;
    }

    /**
     * All dialogs which want to have the previous advanced/basic mode remembered in local storage need to have
     * a key to local storage set.
     */
    private String isAdvancedModeLocalStorageKey;

    public String getIsAdvancedModeLocalStorageKey() {
        return isAdvancedModeLocalStorageKey;
    }

    public void setIsAdvancedModeLocalStorageKey(String isAdvancedModeLocalStorageKey) {
        this.isAdvancedModeLocalStorageKey = isAdvancedModeLocalStorageKey;
    }

    private EntityModel<Boolean> attachedToInstanceType;

    public EntityModel<Boolean> getAttachedToInstanceType() {
        return attachedToInstanceType;
    }

    public void setAttachedToInstanceType(EntityModel<Boolean> attachedToInstanceType) {
        this.attachedToInstanceType = attachedToInstanceType;
    }

    private EntityModel<String> spiceProxy;

    public EntityModel<String> getSpiceProxy() {
        return spiceProxy;
    }

    public void setSpiceProxy(EntityModel<String> spiceProxy) {
        this.spiceProxy = spiceProxy;
    }

    private EntityModel<Boolean> spiceProxyEnabled;

    public EntityModel<Boolean> getSpiceProxyEnabled() {
        return spiceProxyEnabled;
    }

    public void setSpiceProxyEnabled(EntityModel<Boolean> spiceProxyEnabled) {
        this.spiceProxyEnabled = spiceProxyEnabled;
    }

    private EntityModel<Boolean> editingEnabled;

    public EntityModel<Boolean> getEditingEnabled() {
        return editingEnabled;
    }

    public void setEditingEnabled(EntityModel<Boolean> editingEnabled) {
        this.editingEnabled = editingEnabled;
    }

    public boolean getIsNew() {
        return privateIsNew;
    }

    public void setIsNew(boolean value) {
        privateIsNew = value;
    }

    public boolean getIsClone() {
        return privateIsClone;
    }

    public void setIsClone(boolean value) {
        privateIsClone = value;
    }

    private boolean vmAttachedToPool;

    public boolean isVmAttachedToPool() {
        return vmAttachedToPool;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> isSoundcardEnabled;

    public EntityModel<Boolean> getIsSoundcardEnabled() {
        return isSoundcardEnabled;
    }

    private void setIsSoundcardEnabled(NotChangableForVmInPoolEntityModel<Boolean> isSoundcardEnabled) {
        this.isSoundcardEnabled = isSoundcardEnabled;
    }

    private NotChangableForVmInPoolListModel<DataCenterWithCluster> dataCenterWithClustersList;

    public ListModel<DataCenterWithCluster> getDataCenterWithClustersList() {
        return dataCenterWithClustersList;
    }

    private void setDataCenterWithClustersList(NotChangableForVmInPoolListModel<DataCenterWithCluster> dataCenterWithClustersList) {
        this.dataCenterWithClustersList = dataCenterWithClustersList;
    }

    private VnicInstancesModel nicsWithLogicalNetworks;

    public VnicInstancesModel getNicsWithLogicalNetworks() {
        return nicsWithLogicalNetworks;
    }

    public void setNicsWithLogicalNetworks(VnicInstancesModel nicsWithLogicalNetworks) {
        this.nicsWithLogicalNetworks = nicsWithLogicalNetworks;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> ioThreadsEnabled;

    public EntityModel<Boolean> getIoThreadsEnabled() {
        return ioThreadsEnabled;
    }

    public void setIoThreadsEnabled(NotChangableForVmInPoolEntityModel<Boolean> ioThreadsEnabled) {
        this.ioThreadsEnabled = ioThreadsEnabled;
    }

    private NotChangableForVmInPoolEntityModel<String> numOfIoThreads;

    public EntityModel<String> getNumOfIoThreads() {
        return numOfIoThreads;
    }

    public void setNumOfIoThreads(NotChangableForVmInPoolEntityModel<String> numOfIoThreads) {
        this.numOfIoThreads = numOfIoThreads;
    }

    /**
     * VM icon
     * <p>
     *     It may be null during initialization, it should never be null when {@link #validate()} is called.
     * </p>
     */
    private NotChangableForVmInPoolEntityModel<IconWithOsDefault> icon;

    public EntityModel<IconWithOsDefault> getIcon() {
        return icon;
    }

    public void setIcon(NotChangableForVmInPoolEntityModel<IconWithOsDefault> icon) {
        this.icon = icon;
    }

    /**
     * Note: We assume that this method is called only once, on the creation stage of the model. if this assumption is
     * changed (i.e the VM can attached/detached from a pool after the model is created), this method should be modified
     */
    public void setVmAttachedToPool(boolean value) {
        if (value) {
            // ==Header==
            getDataCenterWithClustersList().setIsChangeable(!value);
            getBaseTemplate().setIsChangeable(false);
            getTemplateWithVersion().setIsChangeable(false);
            getOSType().setIsChangeable(false);
            getInstanceTypes().setIsChangeable(false);
            getVmType().setIsChangeable(false);
            getQuota().setIsChangeable(false);

            // ==General Tab==
            getVmId().setIsChangeable(false);
            getPrestartedVms().setIsChangeable(false);
            getMaxAssignedVmsPerUser().setIsChangeable(false);
            getNumOfDesktops().setIsChangeable(false);

            getIsStateless().setIsChangeable(false);
            getIsRunAndPause().setIsChangeable(false);
            getIsDeleteProtected().setIsChangeable(false);

            // ==System Tab==
            getMemSize().setIsChangeable(false);
            getMaxMemorySize().setIsChangeable(false);
            getMinAllocatedMemory().setIsChangeable(false);
            getTotalCPUCores().setIsChangeable(false);

            getNumOfSockets().setIsChangeable(false);
            getCoresPerSocket().setIsChangeable(false);
            getThreadsPerCore().setIsChangeable(false);
            getEmulatedMachine().setIsChangeable(false);
            getCustomCpu().setIsChangeable(false);
            getCustomCompatibilityVersion().setIsChangeable(false);

            getBiosType().setIsChangeable(false);
            getTimeZone().setIsChangeable(false);
            getSerialNumberPolicy().setIsChangeable(false);

            // ==Initial run Tab==
            getVmInitEnabled().setIsChangeable(false);

            // ==Console Tab==
            getIsHeadlessModeEnabled().setIsChangeable(false);
            getDisplayType().setIsChangeable(false);
            getGraphicsType().setIsChangeable(false);
            getVncKeyboardLayout().setIsChangeable(false);
            getConsoleDisconnectAction().setIsChangeable(false);
            getConsoleDisconnectActionDelay().setIsChangeable(false);
            getNumOfMonitors().setIsChangeable(false);

            getIsUsbEnabled().setIsChangeable(false);
            getIsSmartcardEnabled().setIsChangeable(false);
            getSsoMethodNone().setIsChangeable(false);
            getSsoMethodGuestAgent().setIsChangeable(false);

            getAllowConsoleReconnect().setIsChangeable(false);

            getIsSoundcardEnabled().setIsChangeable(false);
            getSpiceProxyEnabled().setIsChangeable(false);
            getSpiceFileTransferEnabled().setIsChangeable(false);
            getSpiceCopyPasteEnabled().setIsChangeable(false);
            getIsConsoleDeviceEnabled().setIsChangeable(false);

            // ==Host Tab==
            getIsAutoAssign().setIsChangeable(false);
            getDefaultHost().setIsChangeable(false);
            getHostCpu().setIsChangeable(false);
            getTscFrequency().setIsChangeable(false);

            getMigrationMode().setIsChangeable(false);
            getMigrationDowntime().setIsChangeable(false);
            getMigrationPolicies().setIsChangeable(false);
            getAutoConverge().setIsChangeable(false);
            getMigrateCompressed().setIsChangeable(false);
            getMigrateEncrypted().setIsChangeable(false);
            getNumaEnabled().setIsChangeable(false);

            // ==High Availability==
            getIsHighlyAvailable().setIsChangeable(false);
            getLease().setIsChangeable(false);
            getResumeBehavior().setIsChangeable(false);
            getPriority().setIsChangeable(false);
            getWatchdogModel().setIsChangeable(false);
            getWatchdogAction().setIsChangeable(false);

            // ==Resource Allocation Tab==
            getCpuProfiles().setIsChangeable(false);
            getCpuSharesAmountSelection().setIsChangeable(false);
            getCpuSharesAmount().setIsChangeable(false);
            getCpuPinningPolicy().setIsChangeable(false);
            getCpuPinning().setIsChangeable(false);
            getMemoryBalloonEnabled().setIsChangeable(false);
            getTpmEnabled().setIsChangeable(false);
            getIoThreadsEnabled().setIsChangeable(false);
            getNumOfIoThreads().setIsChangeable(false);

            getMultiQueues().setIsChangeable(false);

            getProvisioning().setIsChangeable(false);
            getProvisioningThin_IsSelected().setIsChangeable(false);
            getProvisioningClone_IsSelected().setIsChangeable(false);
            getIsVirtioScsiEnabled().setIsChangeable(false);
            getVirtioScsiMultiQueueTypeSelection().setIsChangeable(false);
            getNumOfVirtioScsiMultiQueues().setIsChangeable(false);

            getDisksAllocationModel().setIsChangeable(false);

            // ==Boot Options Tab==
            getFirstBootDevice().setIsChangeable(false);
            getSecondBootDevice().setIsChangeable(false);
            getCdAttached().setIsChangeable(false);
            getCdImage().setIsChangeable(false);
            getBootMenuEnabled().setIsChangeable(false);
            getKernel_path().setIsChangeable(false);
            getInitrd_path().setIsChangeable(false);
            getKernel_parameters().setIsChangeable(false);

            // ==Random Generator Tab==
            getIsRngEnabled().setIsChangeable(false);
            getRngPeriod().setIsChangeable(false);
            getRngBytes().setIsChangeable(false);
            getRngSourceUrandom().setIsChangeable(false);
            getRngSourceHwrng().setIsChangeable(false);

            // ==Custom Properties Tab==
            getCustomProperties().setIsChangeable(false);
            getCustomPropertySheet().setIsChangeable(false);

            // ==Icon Tab==
            getIcon().setIsChangeable(false);

            // ==Foreman Tab==
            getProviders().setIsChangeable(false);

            vmAttachedToPool = true;
        }
    }
    private boolean isWindowsOS;

    public boolean getIsWindowsOS() {
        return isWindowsOS;
    }

    public void setIsWindowsOS(boolean value) {
        if (isWindowsOS != value) {
            isWindowsOS = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsWindowsOS")); //$NON-NLS-1$
        }
    }

    private boolean isLinuxOS;

    public boolean getIsLinuxOS() {
        return isLinuxOS;
    }

    public void setIsLinuxOS(boolean value) {
        if (isLinuxOS != value) {
            isLinuxOS = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsLinuxOS")); //$NON-NLS-1$
        }
    }

    private boolean isIgnition;

    public boolean getIsIgnition() {
        return isIgnition;
    }

    public void setIsIgnition(boolean value) {
        if (isIgnition != value) {
            isIgnition = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsIgnition")); //$NON-NLS-1$
        }
    }

    private String cpuNotification;

    public String getCPUNotification() {
        return cpuNotification;
    }

    public void setCPUNotification(String value) {
        if (!Objects.equals(cpuNotification, value)) {
            cpuNotification = value;
            onPropertyChanged(new PropertyChangedEventArgs("CPUNotification")); //$NON-NLS-1$
        }
    }

    public boolean isCPUsAmountValid;

    public boolean getIsCPUsAmountValid() {
        return isCPUsAmountValid;
    }

    public void setIsCPUsAmountValid(boolean value) {
        if (isCPUsAmountValid != value) {
            isCPUsAmountValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsCPUsAmountValid")); //$NON-NLS-1$
        }
    }

    private NotChangableForVmInPoolListModel<StorageDomain> privateStorageDomain;

    public ListModel<StorageDomain> getStorageDomain() {
        return privateStorageDomain;
    }

    private void setStorageDomain(NotChangableForVmInPoolListModel<StorageDomain> value) {
        privateStorageDomain = value;
    }

    private NotChangableForVmInPoolListModel<TemplateWithVersion> templateWithVersion;

    public ListModel<TemplateWithVersion> getTemplateWithVersion() {
        return templateWithVersion;
    }

    public void setTemplateWithVersion(NotChangableForVmInPoolListModel<TemplateWithVersion> templateWithVersion) {
        this.templateWithVersion = templateWithVersion;
    }

    private NotChangableForVmInPoolListModel<VmTemplate> baseTemplate;

    public ListModel<VmTemplate> getBaseTemplate() {
        return baseTemplate;
    }

    private void setBaseTemplate(NotChangableForVmInPoolListModel<VmTemplate> value) {
        baseTemplate = value;
    }

    private NotChangableForVmInPoolListModel<InstanceType> instanceTypes;

    public void setInstanceTypes(NotChangableForVmInPoolListModel<InstanceType> instanceTypes) {
        this.instanceTypes = instanceTypes;
    }

    public ListModel<InstanceType> getInstanceTypes() {
        return instanceTypes;
    }

    private InstanceImagesModel instanceImages;

    public InstanceImagesModel getInstanceImages() {
        return instanceImages;
    }

    public void setInstanceImages(InstanceImagesModel instanceImages) {
        this.instanceImages = instanceImages;
    }

    private NotChangableForVmInPoolListModel<VmType> vmType;

    public void setVmType(NotChangableForVmInPoolListModel<VmType> vmType) {
        this.vmType = vmType;
    }

    public ListModel<VmType> getVmType() {
        return vmType;
    }

    private EntityModel<String> privateName;

    public EntityModel<String> getName() {
        return privateName;
    }

    private void setName(EntityModel<String> value) {
        privateName = value;
    }

    private NotChangableForVmInPoolListModel<BiosType> biosType;

    public ListModel<BiosType> getBiosType() {
        return biosType;
    }

    private void setBiosType(NotChangableForVmInPoolListModel<BiosType> value) {
        biosType = value;
    }

    private NotChangableForVmInPoolListModel<String> emulatedMachine;

    public ListModel<String> getEmulatedMachine() {
        return emulatedMachine;
    }

    private void setEmulatedMachine(NotChangableForVmInPoolListModel<String> emulatedMachine) {
        this.emulatedMachine = emulatedMachine;
    }

    private ListModel<Version> customCompatibilityVersion;

    public ListModel<Version> getCustomCompatibilityVersion() {
        return customCompatibilityVersion;
    }

    private void setCustomCompatibilityVersion(ListModel<Version> value) {
        customCompatibilityVersion = value;
    }

    private NotChangableForVmInPoolListModel<String> customCpu;

    public ListModel<String> getCustomCpu() {
        return customCpu;
    }

    private void setCustomCpu(NotChangableForVmInPoolListModel<String> customCpu) {
        this.customCpu = customCpu;
    }

    private NotChangableForVmInPoolListModel<Integer> privateOSType;

    public ListModel<Integer> getOSType() {
        return privateOSType;
    }

    private void setOSType(NotChangableForVmInPoolListModel<Integer> value) {
        privateOSType = value;
    }

    private NotChangableForVmInPoolListModel<Integer> privateNumOfMonitors;

    public ListModel<Integer> getNumOfMonitors() {
        return privateNumOfMonitors;
    }

    private void setNumOfMonitors(NotChangableForVmInPoolListModel<Integer> value) {
        privateNumOfMonitors = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateAllowConsoleReconnect;

    public EntityModel<Boolean> getAllowConsoleReconnect() {
        return privateAllowConsoleReconnect;
    }

    private NotChangableForVmInPoolEntityModel<String> privateVmId;

    public EntityModel<String> getVmId() {
        return privateVmId;
    }

    private void setVmId(NotChangableForVmInPoolEntityModel<String> value) {
        privateVmId = value;
    }

    private void setAllowConsoleReconnect(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateAllowConsoleReconnect = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateDescription;

    public EntityModel<String> getDescription() {
        return privateDescription;
    }

    private void setDescription(NotChangableForVmInPoolEntityModel<String> value) {
        privateDescription = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateComment;

    public EntityModel<String> getComment() {
        return privateComment;
    }

    private void setComment(NotChangableForVmInPoolEntityModel<String> value) {
        privateComment = value;
    }

    private NotChangableForVmInPoolEntityModel<String> templateVersionName;

    public EntityModel<String> getTemplateVersionName() {
        return templateVersionName;
    }

    private void setTemplateVersionName(NotChangableForVmInPoolEntityModel<String> value) {
        templateVersionName = value;
    }

    private NotChangableForVmInPoolEntityModel<Integer> privateMemSize;

    public EntityModel<Integer> getMemSize() {
        return privateMemSize;
    }

    private void setMemSize(NotChangableForVmInPoolEntityModel<Integer> value) {
        privateMemSize = value;
    }

    private NotChangableForVmInPoolEntityModel<Integer> maxMemorySize;

    public EntityModel<Integer> getMaxMemorySize() {
        return maxMemorySize;
    }

    public void setMaxMemorySize(NotChangableForVmInPoolEntityModel<Integer> maxMemorySize) {
        this.maxMemorySize = maxMemorySize;
    }

    private NotChangableForVmInPoolEntityModel<Integer> privateMinAllocatedMemory;

    public EntityModel<Integer> getMinAllocatedMemory() {
        return privateMinAllocatedMemory;
    }

    private void setMinAllocatedMemory(NotChangableForVmInPoolEntityModel<Integer> value) {
        privateMinAllocatedMemory = value;
    }

    private NotChangableForVmInPoolListModel<Quota> privateQuota;

    public ListModel<Quota> getQuota() {
        return privateQuota;
    }

    private void setQuota(NotChangableForVmInPoolListModel<Quota> value) {
        privateQuota = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateUsbEnabled;

    public EntityModel<Boolean> getIsUsbEnabled() {
        return privateUsbEnabled;
    }

    public void setIsUsbEnabled(NotChangableForVmInPoolEntityModel<Boolean> usbEnabled) {
        this.privateUsbEnabled = usbEnabled;
    }

    private NotChangableForVmInPoolListModel<ConsoleDisconnectAction> consoleDisconnectAction;

    public ListModel<ConsoleDisconnectAction> getConsoleDisconnectAction() {
        return consoleDisconnectAction;
    }

    private void setConsoleDisconnectAction(NotChangableForVmInPoolListModel<ConsoleDisconnectAction> value) {
        consoleDisconnectAction = value;
    }

    private NotChangableForVmInPoolEntityModel<Integer> consoleDisconnectActionDelay;

    public EntityModel<Integer> getConsoleDisconnectActionDelay() {
        return consoleDisconnectActionDelay;
    }

    private void setConsoleDisconnectActionDelay(NotChangableForVmInPoolEntityModel<Integer> consoleDisconnectActionDelay) {
        this.consoleDisconnectActionDelay = consoleDisconnectActionDelay;
    }

    private NotChangableForVmInPoolListModel<TimeZoneModel> privateTimeZone;

    public ListModel<TimeZoneModel> getTimeZone() {
        return privateTimeZone;
    }

    private void setTimeZone(NotChangableForVmInPoolListModel<TimeZoneModel> value) {
        privateTimeZone = value;
    }

    private NotChangableForVmInPoolListModel<Integer> privateNumOfSockets;

    public ListModel<Integer> getNumOfSockets() {
        return privateNumOfSockets;
    }

    private void setNumOfSockets(NotChangableForVmInPoolListModel<Integer> value) {
        privateNumOfSockets = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateTotalCPUCores;

    public EntityModel<String> getTotalCPUCores() {
        return privateTotalCPUCores;
    }

    private void setTotalCPUCores(NotChangableForVmInPoolEntityModel<String> value) {
        privateTotalCPUCores = value;
    }

    private NotChangableForVmInPoolListModel<Integer> privateCoresPerSocket;

    public ListModel<Integer> getCoresPerSocket() {
        return privateCoresPerSocket;
    }

    private void setCoresPerSocket(NotChangableForVmInPoolListModel<Integer> value) {
        privateCoresPerSocket = value;
    }

    private NotChangableForVmInPoolListModel<Integer> threadsPerCore;

    public ListModel<Integer> getThreadsPerCore() {
        return threadsPerCore;
    }

    private void setThreadsPerCore(NotChangableForVmInPoolListModel<Integer> value) {
        threadsPerCore = value;
    }

    private NotChangableForVmInPoolListModel<VDS> privateDefaultHost;

    public ListModel<VDS> getDefaultHost() {
        return privateDefaultHost;
    }

    private void setDefaultHost(NotChangableForVmInPoolListModel<VDS> value) {
        privateDefaultHost = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateisSmartcardEnabled;

    public EntityModel<Boolean> getIsSmartcardEnabled() {
        return privateisSmartcardEnabled;
    }

    private void setIsSmartcardEnabled(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateisSmartcardEnabled = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsHeadlessModeEnabled;

    public EntityModel<Boolean> getIsHeadlessModeEnabled() {
        return privateIsHeadlessModeEnabled;
    }

    private void setIsHeadlessModeEnabled(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateIsHeadlessModeEnabled = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> isConsoleDeviceEnabled;

    public void setRngDevice(VmRngDevice dev) {
        maybeSetEntity(rngBytes, dev.getBytes() == null ? null : dev.getBytes());
        maybeSetEntity(rngPeriod, dev.getPeriod() == null ? null : dev.getPeriod());
        maybeSetEntity(rngSourceUrandom, dev.getSource() == VmRngDevice.Source.RANDOM);
        maybeSetEntity(rngSourceHwrng, dev.getSource() == VmRngDevice.Source.HWRNG);

        // post check - at least one source must be selected
        // if, for example, instance type has forbidden source checked, maybeSetEntity doesn't select any source, which
        // is invalid
        if (!Boolean.TRUE.equals(rngSourceUrandom.getEntity()) && !Boolean.TRUE.equals(rngSourceHwrng.getEntity())) {
            getBehavior().deactivateInstanceTypeManager();

            EntityModel[] entityModels = { rngSourceUrandom, rngSourceHwrng };
            for (EntityModel entityModel : entityModels) {
                if (entityModel.getIsAvailable() && entityModel.getIsChangable()) {
                    entityModel.setEntity(Boolean.TRUE); // select first available
                    break;
                }
            }

            getBehavior().activateInstanceTypeManager();
        }
    }

    private <T> void maybeSetEntity(EntityModel<T> entityModel, T value) {
        if (entityModel != null && entityModel.getIsChangable() && entityModel.getIsAvailable()) {
            entityModel.setEntity(value);
        }
    }

    public VmRngDevice generateRngDevice() {
        VmRngDevice dev = new VmRngDevice();
        dev.setBytes(rngBytes.getEntity());
        dev.setPeriod(rngPeriod.getEntity());
        dev.setSource(Boolean.TRUE.equals(rngSourceUrandom.getEntity())
                ? getBehavior().getUrandomOrRandomRngSource()
                : VmRngDevice.Source.HWRNG);
        return dev;
    }

    private NotChangableForVmInPoolEntityModel<Integer> rngPeriod;

    public EntityModel<Integer> getRngPeriod() {
        return rngPeriod;
    }

    public void setRngPeriod(NotChangableForVmInPoolEntityModel<Integer> rngPeriod) {
        this.rngPeriod = rngPeriod;
    }

    public EntityModel<Integer> getRngBytes() {
        return rngBytes;
    }

    public void setRngBytes(NotChangableForVmInPoolEntityModel<Integer> rngBytes) {
        this.rngBytes = rngBytes;
    }

    public EntityModel<Boolean> getRngSourceHwrng() {
        return rngSourceHwrng;
    }

    public void setRngSourceHwrng(NotChangableForVmInPoolEntityModel<Boolean> rngSourceHwrng) {
        this.rngSourceHwrng = rngSourceHwrng;
    }

    public EntityModel<Boolean> getRngSourceUrandom() {
        return rngSourceUrandom;
    }

    public void setRngSourceUrandom(NotChangableForVmInPoolEntityModel<Boolean> rngSourceUrandom) {
        this.rngSourceUrandom = rngSourceUrandom;
    }

    private NotChangableForVmInPoolEntityModel<Integer> rngBytes;

    /**
     * Serves for both `/dev/urandom` (effective cluster level 4.1 and newer)
     *             and `/dev/random`  (effective cluster level 4.0 and older).
     */
    private NotChangableForVmInPoolEntityModel<Boolean> rngSourceUrandom;
    private NotChangableForVmInPoolEntityModel<Boolean> rngSourceHwrng;

    private NotChangableForVmInPoolEntityModel<Boolean> isRngEnabled;

    public EntityModel<Boolean> getIsRngEnabled() {
        return isRngEnabled;
    }

    public void setIsRngEnabled(NotChangableForVmInPoolEntityModel<Boolean> rngEnabled) {
        isRngEnabled = rngEnabled;
    }

    public EntityModel<Boolean> getIsConsoleDeviceEnabled() {
        return isConsoleDeviceEnabled;
    }

    private void setConsoleDeviceEnabled(NotChangableForVmInPoolEntityModel<Boolean> consoleDeviceEnabled) {
        this.isConsoleDeviceEnabled = consoleDeviceEnabled;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsStateless;

    public EntityModel<Boolean> getIsStateless() {
        return privateIsStateless;
    }

    private void setIsStateless(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateIsStateless = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsRunAndPause;

    public EntityModel<Boolean> getIsRunAndPause() {
        return privateIsRunAndPause;
    }

    private void setIsRunAndPause(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateIsRunAndPause = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsDeleteProtected;

    public EntityModel<Boolean> getIsDeleteProtected() {
        return privateIsDeleteProtected;
    }

    public void setIsDeleteProtected(NotChangableForVmInPoolEntityModel<Boolean> deleteProtected) {
        this.privateIsDeleteProtected = deleteProtected;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> ssoMethodNone;

    public EntityModel<Boolean> getSsoMethodNone() {
        return ssoMethodNone;
    }

    public void setSsoMethodNone(NotChangableForVmInPoolEntityModel<Boolean> ssoMethodNone) {
        this.ssoMethodNone = ssoMethodNone;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> ssoMethodGuestAgent;

    public EntityModel<Boolean> getSsoMethodGuestAgent() {
        return ssoMethodGuestAgent;
    }

    public void setSsoMethodGuestAgent(NotChangableForVmInPoolEntityModel<Boolean> ssoMethodGuestAgent) {
        this.ssoMethodGuestAgent = ssoMethodGuestAgent;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> copyPermissions;

    public EntityModel<Boolean> getCopyPermissions() {
        return copyPermissions;
    }

    private void setCopyPermissions(NotChangableForVmInPoolEntityModel<Boolean> copyPermissions) {
        this.copyPermissions = copyPermissions;
    }

    // used in the New template dialog
    private NotChangableForVmInPoolEntityModel<Boolean> sealTemplate;

    public EntityModel<Boolean> getSealTemplate() {
        return sealTemplate;
    }

    private void setSealTemplate(NotChangableForVmInPoolEntityModel<Boolean> sealTemplate) {
        this.sealTemplate = sealTemplate;
    }

    // database value, used only for templates
    private EntityModel<Boolean> isSealed;

    public EntityModel<Boolean> getIsSealed() {
        return isSealed;
    }

    public void setIsSealed(EntityModel<Boolean> isSealed) {
        this.isSealed = isSealed;
    }

    private EntityModel<Boolean> memoryBalloonEnabled;

    public EntityModel<Boolean> getMemoryBalloonEnabled() {
        return memoryBalloonEnabled;
    }

    public void setMemoryBalloonEnabled(EntityModel<Boolean> memoryBalloonEnabled) {
        this.memoryBalloonEnabled = memoryBalloonEnabled;
    }

    private EntityModel<Boolean> multiQueues;

    public EntityModel<Boolean> getMultiQueues() {
        return multiQueues;
    }

    public void setMultiQueues(EntityModel<Boolean> multiQueues) {
        this.multiQueues = multiQueues;
    }

    private NotChangableForVmInPoolEntityModel<Integer> numOfVirtioScsiMultiQueues;

    public EntityModel<Integer> getNumOfVirtioScsiMultiQueues() {
        return numOfVirtioScsiMultiQueues;
    }

    public void setNumOfVirtioScsiMultiQueues(NotChangableForVmInPoolEntityModel<Integer> numOfVirtioScsiMultiQueues) {
        this.numOfVirtioScsiMultiQueues = numOfVirtioScsiMultiQueues;
    }

    private NotChangableForVmInPoolListModel<VirtioMultiQueueType> virtioScsiMultiQueueTypeSelection;

    public ListModel<VirtioMultiQueueType> getVirtioScsiMultiQueueTypeSelection() {
        return virtioScsiMultiQueueTypeSelection;
    }

    public void setVirtioScsiMultiQueueTypeSelection(
            NotChangableForVmInPoolListModel<VirtioMultiQueueType> virtioScsiMultiQueueTypeSelection) {
        this.virtioScsiMultiQueueTypeSelection = virtioScsiMultiQueueTypeSelection;
    }

    private NotChangableForVmInPoolListModel<DisplayType> displayType;

    public ListModel<DisplayType> getDisplayType() {
        return displayType;
    }

    private void setDisplayType(NotChangableForVmInPoolListModel<DisplayType> value) {
        displayType = value;
    }

    private NotChangableForVmInPoolListModel<GraphicsTypes> graphicsType;

    public ListModel<GraphicsTypes> getGraphicsType() {
        return graphicsType;
    }

    /**
     * Enum for representing (possibly multiple) graphics device of a VM.
     */
    public enum GraphicsTypes {
        NONE(),
        SPICE(GraphicsType.SPICE),
        VNC(GraphicsType.VNC),
        SPICE_AND_VNC(GraphicsType.SPICE, GraphicsType.VNC);

        private Set<GraphicsType> backingTypes;

        private GraphicsTypes(GraphicsType ... backingTypes) {
            this.backingTypes = new HashSet<>();
            if (backingTypes != null) {
                for (GraphicsType backingType : backingTypes) {
                    this.backingTypes.add(backingType);
                }
            }
        }

        public Collection<GraphicsType> getBackingGraphicsTypes() {
            return backingTypes;
        }

        public static GraphicsTypes fromGraphicsType(GraphicsType type) {
            switch (type) {
                case SPICE:
                    return SPICE;
                case VNC:
                    return VNC;
            }
            return null;
        }

        public static GraphicsTypes fromGraphicsTypes(Set<GraphicsType> types) {
            for (GraphicsTypes myTypes : values()) {
                if (myTypes.getBackingGraphicsTypes().equals(types)) {
                    return myTypes;
                }
            }

            return NONE;
        }
    }

    private void setGraphicsType(NotChangableForVmInPoolListModel<GraphicsTypes> graphicsType) {
        this.graphicsType = graphicsType;
    }

    /**
     * Template provisioning: clone / thin - how to copy template disk.
     * <ul>
     *     <li>true - Clone</li>
     *     <li>false - Thin</li>
     * </ul>
     * Aggregation of {@link #privateProvisioningThin_IsSelected}
     * and {@link #privateProvisioningClone_IsSelected}.
     */
    private NotChangableForVmInPoolEntityModel<Boolean> privateProvisioning;

    public EntityModel<Boolean> getProvisioning() {
        return privateProvisioning;
    }

    private void setProvisioning(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateProvisioning = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateProvisioningThin_IsSelected;

    public EntityModel<Boolean> getProvisioningThin_IsSelected() {
        return privateProvisioningThin_IsSelected;
    }

    public void setProvisioningThin_IsSelected(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateProvisioningThin_IsSelected = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateProvisioningClone_IsSelected;

    public EntityModel<Boolean> getProvisioningClone_IsSelected() {
        return privateProvisioningClone_IsSelected;
    }

    public void setProvisioningClone_IsSelected(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateProvisioningClone_IsSelected = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> isVirtioScsiEnabled;

    public EntityModel<Boolean> getIsVirtioScsiEnabled() {
        return isVirtioScsiEnabled;
    }

    public void setIsVirtioScsiEnabled(NotChangableForVmInPoolEntityModel<Boolean> virtioScsiEnabled) {
        this.isVirtioScsiEnabled = virtioScsiEnabled;
    }

    private NotChangableForVmInPoolListModel<EntityModel<Integer>> privatePriority;

    public ListModel<EntityModel<Integer>> getPriority() {
        return privatePriority;
    }

    private void setPriority(NotChangableForVmInPoolListModel<EntityModel<Integer>> value) {
        privatePriority = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsHighlyAvailable;

    public EntityModel<Boolean> getIsHighlyAvailable() {
        return privateIsHighlyAvailable;
    }

    private void setIsHighlyAvailable(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateIsHighlyAvailable = value;
    }

    private NotChangableForVmInPoolListModel<StorageDomain> lease;

    public ListModel<StorageDomain> getLease() {
        return lease;
    }

    private void setLease(NotChangableForVmInPoolListModel<StorageDomain> value) {
        lease = value;
    }

    private NotChangableForVmInPoolListModel<VmResumeBehavior> resumeBehavior;

    public ListModel<VmResumeBehavior> getResumeBehavior() {
        return resumeBehavior;
    }

    public void setResumeBehavior(NotChangableForVmInPoolListModel<VmResumeBehavior> resumeBehavior) {
        this.resumeBehavior = resumeBehavior;
    }

    private NotChangableForVmInPoolListModel<EntityModel<BootSequence>> privateFirstBootDevice;

    public ListModel<EntityModel<BootSequence>> getFirstBootDevice() {
        return privateFirstBootDevice;
    }

    private void setFirstBootDevice(NotChangableForVmInPoolListModel<EntityModel<BootSequence>> value) {
        privateFirstBootDevice = value;
    }

    private NotChangableForVmInPoolListModel<EntityModel<BootSequence>> privateSecondBootDevice;

    public ListModel<EntityModel<BootSequence>> getSecondBootDevice() {
        return privateSecondBootDevice;
    }

    private void setSecondBootDevice(NotChangableForVmInPoolListModel<EntityModel<BootSequence>> value) {
        privateSecondBootDevice = value;
    }

    private NotChangableForVmInPoolSortedListModel<RepoImage> privateCdImage;

    public ListModel<RepoImage> getCdImage() {
        return privateCdImage;
    }

    private void setCdImage(NotChangableForVmInPoolSortedListModel<RepoImage> value) {
        privateCdImage = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> isSubTemplate;

    public EntityModel<Boolean> getIsSubTemplate() {
        return isSubTemplate;
    }

    public void setIsSubTemplate(NotChangableForVmInPoolEntityModel<Boolean> value) {
        isSubTemplate = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> cdAttached;

    public EntityModel<Boolean> getCdAttached() {
        return cdAttached;
    }

    public void setCdAttached(NotChangableForVmInPoolEntityModel<Boolean> value) {
        cdAttached = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateInitrd_path;

    public EntityModel<String> getInitrd_path() {
        return privateInitrd_path;
    }

    private void setInitrd_path(NotChangableForVmInPoolEntityModel<String> value) {
        privateInitrd_path = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateKernel_path;

    public EntityModel<String> getKernel_path() {
        return privateKernel_path;
    }

    private void setKernel_path(NotChangableForVmInPoolEntityModel<String> value) {
        privateKernel_path = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateKernel_parameters;

    public EntityModel<String> getKernel_parameters() {
        return privateKernel_parameters;
    }

    private void setKernel_parameters(NotChangableForVmInPoolEntityModel<String> value) {
        privateKernel_parameters = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateCustomProperties;

    public EntityModel<String> getCustomProperties() {
        return privateCustomProperties;
    }

    private void setCustomProperties(NotChangableForVmInPoolEntityModel<String> value) {
        privateCustomProperties = value;
    }

    private EntityModel<Boolean> vmInitEnabled;

    public EntityModel<Boolean> getVmInitEnabled() {
        return vmInitEnabled;
    }

    public void setVmInitEnabled(EntityModel<Boolean> vmInitEnabled) {
        this.vmInitEnabled = vmInitEnabled;
    }

    private EntityModel<Boolean> cloudInitEnabled;

    private EntityModel<Boolean> sysprepEnabled;

    private EntityModel<Boolean> ignitionEnabled;

    public EntityModel<Boolean> getCloudInitEnabled() {
        return cloudInitEnabled;
    }

    public void setCloudInitEnabled(EntityModel<Boolean> cloudInitEnabled) {
        this.cloudInitEnabled = cloudInitEnabled;
    }

    public EntityModel<Boolean> getIgnitionEnabled() {
        return ignitionEnabled;
    }

    public void setIgnitionEnabled(EntityModel<Boolean> ignitionEnabled) {
        this.ignitionEnabled = ignitionEnabled;
    }


    public EntityModel<Boolean> getSysprepEnabled() {
        return sysprepEnabled;
    }

    public void setSysprepEnabled(EntityModel<Boolean> sysprepEnabled) {
        this.sysprepEnabled = sysprepEnabled;
    }

    private VmInitModel vmInitModel;

    public VmInitModel getVmInitModel() {
        return vmInitModel;
    }

    public void setVmInitModel(VmInitModel vmInitModel) {
        this.vmInitModel = vmInitModel;
    }


    private NotChangableForVmInPoolKeyValueModel customPropertySheet;

    public KeyValueModel getCustomPropertySheet() {
        return customPropertySheet;
    }

    public void setCustomPropertySheet(NotChangableForVmInPoolKeyValueModel customPropertySheet) {
        this.customPropertySheet = customPropertySheet;
    }

    private Map<Version, Map<String, String>> privateCustomPropertiesKeysList;

    public Map<Version, Map<String, String>> getCustomPropertiesKeysList() {
        return privateCustomPropertiesKeysList;
    }

    public void setCustomPropertiesKeysList(Map<Version, Map<String, String>> value) {
        privateCustomPropertiesKeysList = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsAutoAssign;

    public EntityModel<Boolean> getIsAutoAssign() {
        return privateIsAutoAssign;
    }

    public void setIsAutoAssign(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateIsAutoAssign = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> hostCpu;

    public EntityModel<Boolean> getHostCpu() {
        return hostCpu;
    }

    public void setHostCpu(NotChangableForVmInPoolEntityModel<Boolean> hostCpu) {
        this.hostCpu = hostCpu;
    }

    private EntityModel<Boolean> tscFrequency;

    public EntityModel<Boolean> getTscFrequency() {
        return tscFrequency;
    }

    public void setTscFrequency(EntityModel<Boolean> tscFrequency) {
        this.tscFrequency = tscFrequency;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> tpmEnabled;

    public EntityModel<Boolean> getTpmEnabled() {
        return tpmEnabled;
    }

    public void setTpmEnabled(NotChangableForVmInPoolEntityModel<Boolean> tpmEnabled) {
        this.tpmEnabled = tpmEnabled;
    }

    private boolean tpmOriginallyEnabled = true;

    public boolean getTpmOriginallyEnabled() {
        return tpmOriginallyEnabled;
    }

    public void setTpmOriginallyEnabled(boolean tpmOriginallyEnabled) {
        this.tpmOriginallyEnabled = tpmOriginallyEnabled;
    }

    private boolean secureBootOriginallyEnabled = true;

    public boolean getSecureBootOriginallyEnabled() {
        return secureBootOriginallyEnabled;
    }

    public void setSecureBootOriginallyEnabled(boolean secureBootOriginallyEnabled) {
        this.secureBootOriginallyEnabled = secureBootOriginallyEnabled;
    }

    private NotChangableForVmInPoolListModel<MigrationSupport> migrationMode;

    public ListModel<MigrationSupport> getMigrationMode() {
        return migrationMode;
    }

    public void setMigrationMode(NotChangableForVmInPoolListModel<MigrationSupport> value) {
        migrationMode = value;
    }

    private ListModelWithClusterDefault<MigrationPolicy> migrationPolicies;

    public ListModel<MigrationPolicy> getMigrationPolicies() {
        return migrationPolicies;
    }

    public void setMigrationPolicies(ListModelWithClusterDefault<MigrationPolicy> migrationPolicies) {
        this.migrationPolicies = migrationPolicies;
    }

    private NotChangableForVmInPoolListModel<Integer> migrationDowntime;

    public ListModel<Integer> getMigrationDowntime() {
        return migrationDowntime;
    }

    private void setMigrationDowntime(NotChangableForVmInPoolListModel<Integer> value) {
        migrationDowntime = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsTemplatePublic;

    public EntityModel<Boolean> getIsTemplatePublic() {
        return privateIsTemplatePublic;
    }

    private void setIsTemplatePublic(NotChangableForVmInPoolEntityModel<Boolean> value) {
        privateIsTemplatePublic = value;
    }

    private boolean privateIsFirstRun;

    public boolean getIsFirstRun() {
        return privateIsFirstRun;
    }

    public void setIsFirstRun(boolean value) {
        privateIsFirstRun = value;
    }

    private List<DiskModel> disks;

    public List<DiskModel> getDisks() {
        return disks;
    }

    public void setDisks(List<DiskModel> value) {
        if (disks != value) {
            disks = value;
            onPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
        }
    }

    private DisksAllocationModel disksAllocationModel;

    public DisksAllocationModel getDisksAllocationModel() {
        return disksAllocationModel;
    }

    private void setDisksAllocationModel(DisksAllocationModel value) {
        disksAllocationModel = value;
    }

    private boolean isDisksAvailable;

    public boolean getIsDisksAvailable() {
        return isDisksAvailable;
    }

    public void setIsDisksAvailable(boolean value) {
        isDisksAvailable = value;
        onPropertyChanged(new PropertyChangedEventArgs("IsDisksAvailable")); //$NON-NLS-1$
    }

    private boolean isCustomPropertiesTabAvailable;

    public boolean getIsCustomPropertiesTabAvailable() {
        return isCustomPropertiesTabAvailable;
    }

    public void setIsCustomPropertiesTabAvailable(boolean value) {
        if (isCustomPropertiesTabAvailable != value) {
            isCustomPropertiesTabAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsCustomPropertiesTabAvailable")); //$NON-NLS-1$
        }
    }

    private final VmModelBehaviorBase behavior;

    public VmModelBehaviorBase getBehavior() {
        return behavior;
    }

    private void setBehavior(VmModelBehaviorBase value) {
    }

    private CpuPinningListModel cpuPinningPolicy;

    public CpuPinningListModel getCpuPinningPolicy() {
        return cpuPinningPolicy;
    }

    public void setCpuPinningPolicy(CpuPinningListModel cpuPinningPolicy) {
        this.cpuPinningPolicy = cpuPinningPolicy;
    }

    private NotChangableForVmInPoolEntityModel<String> cpuPinning;

    public EntityModel<String> getCpuPinning() {
        return cpuPinning;
    }

    public void setCpuPinning(NotChangableForVmInPoolEntityModel<String> cpuPinning) {
        this.cpuPinning = cpuPinning;
    }

    private NotChangableForVmInPoolEntityModel<Integer> cpuSharesAmount;

    public EntityModel<Integer> getCpuSharesAmount() {
        return cpuSharesAmount;
    }

    public void setCpuSharesAmount(NotChangableForVmInPoolEntityModel<Integer> cpuSharesAmount) {
        this.cpuSharesAmount = cpuSharesAmount;
    }

    private NotChangableForVmInPoolListModel<CpuSharesAmount> cpuSharesAmountSelection;

    public ListModel<CpuSharesAmount> getCpuSharesAmountSelection() {
        return cpuSharesAmountSelection;
    }

    public void setCpuSharesAmountSelection(NotChangableForVmInPoolListModel<CpuSharesAmount> cpuSharesAmountSelection) {
        this.cpuSharesAmountSelection = cpuSharesAmountSelection;
    }

    private ListModel<String> vncKeyboardLayout;

    public ListModel<String> getVncKeyboardLayout() {
        return vncKeyboardLayout;
    }

    public void setVncKeyboardLayout(ListModel<String> vncKeyboardLayout) {
        this.vncKeyboardLayout = vncKeyboardLayout;
    }

    private ListModelWithClusterDefault<SerialNumberPolicy> serialNumberPolicy;

    public ListModel<SerialNumberPolicy> getSerialNumberPolicy() {
        return serialNumberPolicy;
    }

    public void setSerialNumberPolicy(ListModelWithClusterDefault<SerialNumberPolicy> value) {
        this.serialNumberPolicy = value;
    }

    private EntityModel<String> customSerialNumber;

    public EntityModel<String> getCustomSerialNumber() {
        return customSerialNumber;
    }

    public void setCustomSerialNumber(EntityModel<String> customSerialNumberPolicy) {
        this.customSerialNumber = customSerialNumberPolicy;
    }

    private EntityModel<Boolean> bootMenuEnabled;

    public EntityModel<Boolean> getBootMenuEnabled() {
        return bootMenuEnabled;
    }

    public void setBootMenuEnabled(EntityModel<Boolean> bootMenuEnabled) {
        this.bootMenuEnabled = bootMenuEnabled;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> spiceFileTransferEnabled;

    public EntityModel<Boolean> getSpiceFileTransferEnabled() {
        return spiceFileTransferEnabled;
    }

    public void setSpiceFileTransferEnabled(NotChangableForVmInPoolEntityModel<Boolean> spiceFileTransferEnabled) {
        this.spiceFileTransferEnabled = spiceFileTransferEnabled;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> spiceCopyPasteEnabled;

    public EntityModel<Boolean> getSpiceCopyPasteEnabled() {
        return spiceCopyPasteEnabled;
    }

    public void setSpiceCopyPasteEnabled(NotChangableForVmInPoolEntityModel<Boolean> spiceCopyPasteEnabled) {
        this.spiceCopyPasteEnabled = spiceCopyPasteEnabled;
    }

    private NotChangableForVmInPoolListModel<CpuProfile> cpuProfiles;

    public ListModel<CpuProfile> getCpuProfiles() {
        return cpuProfiles;
    }

    public void setCpuProfiles(NotChangableForVmInPoolListModel<CpuProfile> cpuProfiles) {
        this.cpuProfiles = cpuProfiles;
    }

    private EntityModel<Boolean> numaEnabled;

    private int initialsNumaNodeCount;

    private NotChangableForVmInPoolEntityModel<Integer> numaNodeCount;

    public EntityModel<Integer> getNumaNodeCount() {
        return numaNodeCount;
    }

    public void setNumaNodeCount(NotChangableForVmInPoolEntityModel<Integer> numaNodeCount) {
        this.numaNodeCount = numaNodeCount;
    }

    private List<VmNumaNode> vmNumaNodes;

    public List<VmNumaNode> getVmNumaNodes() {
        return vmNumaNodes;
    }

    public void setVmNumaNodes(List<VmNumaNode> vmNumaNodes) {
        this.vmNumaNodes = vmNumaNodes;
    }

    private UICommand numaSupportCommand;

    public UICommand getNumaSupportCommand() {
        return numaSupportCommand;
    }

    private void setNumaSupportCommand(UICommand numaSupportCommand) {
        this.numaSupportCommand = numaSupportCommand;
    }

    private boolean numaChanged = false;

    private ListModelWithClusterDefault<Boolean> autoConverge;

    public ListModel<Boolean> getAutoConverge() {
        return autoConverge;
    }

    public void setAutoConverge(ListModelWithClusterDefault<Boolean> autoConverge) {
        this.autoConverge = autoConverge;
    }

    private ListModelWithClusterDefault<Boolean> migrateCompressed;

    public ListModel<Boolean> getMigrateCompressed() {
        return migrateCompressed;
    }

    public void setMigrateCompressed(ListModelWithClusterDefault<Boolean> migrateCompressed) {
        this.migrateCompressed = migrateCompressed;
    }

    private ListModelWithClusterDefault<Boolean> migrateEncrypted;

    // the return value has to be ListModel otherwise the GWT compilation won't work
    public ListModel<Boolean> getMigrateEncrypted() {
        return migrateEncrypted;
    }

    public void setMigrateEncrypted(ListModelWithClusterDefault<Boolean> migrateEncrypted) {
        this.migrateEncrypted = migrateEncrypted;
    }

    private ListModel<Label> labelList;

    public void setLabelList(ListModel<Label> labelList) {
        this.labelList = labelList;
    }

    public ListModel<Label> getLabelList() {
        return labelList;
    }

    private ListModel<AffinityGroup> affinityGroupList;

    public ListModel<AffinityGroup> getAffinityGroupList() {
        return affinityGroupList;
    }

    public void setAffinityGroupList(ListModel<AffinityGroup> affinityGroupList) {
        this.affinityGroupList = affinityGroupList;
    }

    public UnitVmModel(VmModelBehaviorBase behavior, ListModel<?> parentModel) {
        this.behavior = behavior;
        this.behavior.setModel(this);

        setNicsWithLogicalNetworks(new VnicInstancesModel());
        setAdvancedMode(new EntityModel<>(false));
        setValid(new EntityModel<>(true));
        setAttachedToInstanceType(new EntityModel<>(true));
        setStorageDomain(new NotChangableForVmInPoolListModel<StorageDomain>());
        setName(new NotChangableForVmInPoolEntityModel<String>());
        getName().getEntityChangedEvent().addListener(this);
        setNumOfMonitors(new NotChangableForVmInPoolListModel<Integer>());
        setAllowConsoleReconnect(new NotChangableForVmInPoolEntityModel<Boolean>());
        setVmId(new NotChangableForVmInPoolEntityModel<String>());
        setDescription(new NotChangableForVmInPoolEntityModel<String>());
        setComment(new NotChangableForVmInPoolEntityModel<String>());
        setMinAllocatedMemory(new NotChangableForVmInPoolEntityModel<Integer>());
        setIsUsbEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        setConsoleDisconnectAction(new NotChangableForVmInPoolListModel<ConsoleDisconnectAction>());
        setConsoleDisconnectActionDelay(new NotChangableForVmInPoolEntityModel<Integer>());
        getConsoleDisconnectActionDelay().setIsChangeable(false, constants.consoleDisconnectActionDelayDisabledReason());
        getConsoleDisconnectAction().getSelectedItemChangedEvent().addListener(this);
        setIsStateless(new NotChangableForVmInPoolEntityModel<Boolean>());
        setIsRunAndPause(new NotChangableForVmInPoolEntityModel<Boolean>());
        setIsHeadlessModeEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        setIsSmartcardEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        setIsDeleteProtected(new NotChangableForVmInPoolEntityModel<Boolean>());
        setSsoMethodNone(new NotChangableForVmInPoolEntityModel<Boolean>());
        setSsoMethodGuestAgent(new NotChangableForVmInPoolEntityModel<Boolean>());
        setConsoleDeviceEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        setCopyPermissions(new NotChangableForVmInPoolEntityModel<Boolean>());
        setSealTemplate(new NotChangableForVmInPoolEntityModel<>(false));
        setIsSealed(new EntityModel<>(false));
        getIsSealed().setIsAvailable(false);

        //rng
        setIsRngEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        getIsRngEnabled().getEntityChangedEvent().addListener(this);
        setRngBytes(new NotChangableForVmInPoolEntityModel<Integer>());
        setRngPeriod(new NotChangableForVmInPoolEntityModel<Integer>());
        setRngSourceUrandom(new NotChangableForVmInPoolEntityModel<Boolean>());
        setRngSourceHwrng(new NotChangableForVmInPoolEntityModel<Boolean>());

        // by default not available - only for new VM
        getCopyPermissions().setIsAvailable(false);
        getCopyPermissions().setEntity(false);
        setVncKeyboardLayout(new NotChangableForVmInPoolListModel<String>());
        setVmType(new NotChangableForVmInPoolListModel<VmType>());
        getVmType().setItems(Arrays.asList(VmType.values()));
        getVmType().setSelectedItem(VmType.Server);
        getVmType().setIsChangeable(false);
        getVmType().getSelectedItemChangedEvent().addListener(this);

        // element should only appear in webadmin add & edit VM dialogs
        setLabelList(new ListModel<Label>());
        getLabelList().getSelectedItemsChangedEvent().addListener(this);
        getLabelList().setIsAvailable(false);

        setAffinityGroupList(new ListModel<>());
        getAffinityGroupList().getSelectedItemChangedEvent().addListener(this);
        getAffinityGroupList().setIsAvailable(false);

        setCdImage(new NotChangableForVmInPoolSortedListModel<>(new LexoNumericNameableComparator<>()));
        getCdImage().setIsChangeable(false);

        setMemoryBalloonEnabled(new EntityModel<Boolean>());
        getMemoryBalloonEnabled().setEntity(true);
        getMemoryBalloonEnabled().setIsAvailable(false);

        setMultiQueues(new EntityModel<Boolean>(true));

        setSpiceProxyEnabled(new EntityModel<>(false));
        setSpiceProxy(new EntityModel<String>());

        setIsSubTemplate(new NotChangableForVmInPoolEntityModel<>(false));
        getIsSubTemplate().getEntityChangedEvent().addListener(this);
        setTemplateVersionName(new NotChangableForVmInPoolEntityModel<String>());
        setBaseTemplate(new NotChangableForVmInPoolListModel<VmTemplate>());
        getBaseTemplate().getSelectedItemChangedEvent().addListener(this);

        setCdAttached(new NotChangableForVmInPoolEntityModel<Boolean>());
        getCdAttached().getEntityChangedEvent().addListener((ev, sender, args) -> getCdImage().setIsChangeable(getCdAttached().getEntity()));
        getCdAttached().setEntity(false);

        setLease(new NotChangableForVmInPoolListModel<StorageDomain>());
        getLease().getSelectedItemChangedEvent().addListener(this);
        setResumeBehavior(new NotChangableForVmInPoolListModel<VmResumeBehavior>());
        setIsHighlyAvailable(new NotChangableForVmInPoolEntityModel<Boolean>());
        getIsHighlyAvailable().getEntityChangedEvent().addListener(this);
        setIsTemplatePublic(new NotChangableForVmInPoolEntityModel<Boolean>());
        setKernel_parameters(new NotChangableForVmInPoolEntityModel<String>());
        setKernel_path(new NotChangableForVmInPoolEntityModel<String>());
        setInitrd_path(new NotChangableForVmInPoolEntityModel<String>());
        setCustomProperties(new NotChangableForVmInPoolEntityModel<String>());
        setCustomPropertySheet(new NotChangableForVmInPoolKeyValueModel());
        getCustomPropertySheet().setShowInvalidKeys(true);
        setDisplayType(new NotChangableForVmInPoolListModel<DisplayType>());
        setGraphicsType(new NotChangableForVmInPoolListModel<GraphicsTypes>());
        setSecondBootDevice(new NotChangableForVmInPoolListModel<EntityModel<BootSequence>>());
        setBootMenuEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        setPriority(new NotChangableForVmInPoolListModel<EntityModel<Integer>>());
        setVmInitEnabled(new EntityModel<>(false));
        setCloudInitEnabled(new EntityModel<Boolean>());
        setIgnitionEnabled(new EntityModel<Boolean>());
        setSpiceFileTransferEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        setSpiceCopyPasteEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        setSysprepEnabled(new EntityModel<Boolean>());
        getVmInitEnabled().getEntityChangedEvent().addListener(this);
        setVmInitModel(new VmInitModel());
        setTemplateWithVersion(new NotChangableForVmInPoolListModel<TemplateWithVersion>());
        getTemplateWithVersion().getSelectedItemChangedEvent().addListener(this);

        setInstanceTypes(new NotChangableForVmInPoolListModel<InstanceType>());
        getInstanceTypes().getSelectedItemChangedEvent().addListener(this);

        setInstanceImages(new InstanceImagesModel(this, parentModel));

        setQuota(new NotChangableForVmInPoolListModel<Quota>());
        getQuota().setIsAvailable(false);

        setDataCenterWithClustersList(new NotChangableForVmInPoolListModel<DataCenterWithCluster>());
        getDataCenterWithClustersList().getSelectedItemChangedEvent().addListener(this);

        setTpmEnabled(new NotChangableForVmInPoolEntityModel<Boolean>(false));

        setBiosType(new NotChangableForVmInPoolListModel<>());
        getBiosType().getSelectedItemChangedEvent().addListener(this);

        setEmulatedMachine(new NotChangableForVmInPoolListModel<String>());

        setCustomCpu(new NotChangableForVmInPoolListModel<String>());

        setCustomCompatibilityVersion(new NotChangableForVmInPoolListModel<Version>());
        getCustomCompatibilityVersion().getSelectedItemChangedEvent().addListener(this);

        setTimeZone(new NotChangableForVmInPoolListModel<TimeZoneModel>());
        getTimeZone().getSelectedItemChangedEvent().addListener(this);

        setDefaultHost(new NotChangableForVmInPoolListModel<VDS>());
        getDefaultHost().getSelectedItemsChangedEvent().addListener(this);

        setOSType(new NotChangableForVmInPoolListModel<Integer>() {
            @Override
            public void setSelectedItem(Integer value) {
                if (!AsyncDataProvider.getInstance().osNameExists(value)) {
                    DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();
                    Cluster cluster = dataCenterWithCluster == null ? null : dataCenterWithCluster.getCluster();
                    if (cluster == null) {
                        return;
                    }
                    super.setSelectedItem(AsyncDataProvider.getInstance().getDefaultOs(cluster.getArchitecture()));
                } else {
                    super.setSelectedItem(value);
                }
            }
        });

        getOSType().getSelectedItemChangedEvent().addListener(this);

        setFirstBootDevice(new NotChangableForVmInPoolListModel<EntityModel<BootSequence>>());
        getFirstBootDevice().getSelectedItemChangedEvent().addListener(this);

        setProvisioning(new NotChangableForVmInPoolEntityModel<Boolean>());
        getProvisioning().getEntityChangedEvent().addListener(this);

        setMemSize(new NotChangableForVmInPoolEntityModel<Integer>());
        getMemSize().getEntityChangedEvent().addListener(this);

        setMaxMemorySize(new NotChangableForVmInPoolEntityModel<Integer>());
        getMaxMemorySize().getEntityChangedEvent().addListener(this);

        setTotalCPUCores(new NotChangableForVmInPoolEntityModel<String>());
        getTotalCPUCores().getEntityChangedEvent().addListener(this);

        setNumOfSockets(new NotChangableForVmInPoolListModel<Integer>());
        getNumOfSockets().getSelectedItemChangedEvent().addListener(this);

        setCoresPerSocket(new NotChangableForVmInPoolListModel<Integer>());
        getCoresPerSocket().getSelectedItemChangedEvent().addListener(this);

        setThreadsPerCore(new NotChangableForVmInPoolListModel<Integer>());
        getThreadsPerCore().getSelectedItemChangedEvent().addListener(this);

        setSerialNumberPolicy(new ListModelWithClusterDefault<>());
        getSerialNumberPolicy().setItems(new ArrayList<SerialNumberPolicy>(Arrays.asList(SerialNumberPolicy.values())));

        setCustomSerialNumber(new EntityModel<>());
        updateCustomSerialNumber();
        getSerialNumberPolicy().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            updateCustomSerialNumber();
        });

        setMigrationMode(new NotChangableForVmInPoolListModel<MigrationSupport>());
        getMigrationMode().getSelectedItemChangedEvent().addListener(this);

        setMigrationDowntime(new NotChangableForVmInPoolListModel<Integer>());
        getMigrationDowntime().setItems(new ArrayList<Integer>(Collections.singletonList(null)));
        getMigrationDowntime().getSelectedItemChangedEvent().addListener(this);

        setMigrationPolicies(new ListModelWithClusterDefault<MigrationPolicy>());
        getMigrationPolicies().getSelectedItemChangedEvent().addListener(this);

        setHostCpu(new NotChangableForVmInPoolEntityModel<Boolean>());
        getHostCpu().getEntityChangedEvent().addListener(this);

        setTscFrequency(new NotChangableForVmInPoolEntityModel<Boolean>());
        getTscFrequency().getEntityChangedEvent().addListener(this);

        setWatchdogAction(new NotChangableForVmInPoolListModel<VmWatchdogAction>());
        getWatchdogAction().getSelectedItemChangedEvent().addListener(this);
        ArrayList<VmWatchdogAction> watchDogActions = new ArrayList<>();
        for (VmWatchdogAction action : VmWatchdogAction.values()) {
            watchDogActions.add(action);
        }
        getWatchdogAction().setItems(watchDogActions);
        getWatchdogAction().setIsChangeable(false);

        setWatchdogModel(new NotChangableForVmInPoolListModel<VmWatchdogType>());
        getWatchdogModel().getSelectedItemChangedEvent().addListener(this);

        setIsAutoAssign(new NotChangableForVmInPoolEntityModel<Boolean>());
        getIsAutoAssign().getEntityChangedEvent().addListener(this);

        setIsTemplatePublic(new NotChangableForVmInPoolEntityModel<Boolean>());
        getIsTemplatePublic().getEntityChangedEvent().addListener(this);
        setIsCustomPropertiesTabAvailable(true);

        resetTabsValidity();

        // NOTE: This is because currently the auto generated view code tries to register events of
        // pooltype for
        // VM/Template views as this model is shared across VM/Template/Pool models
        setPoolType(new NotChangableForVmInPoolListModel<EntityModel<VmPoolType>>());

        setPoolStateful(new NotChangableForVmInPoolEntityModel<Boolean>());
        getPoolStateful().setEntity(false);
        getPoolStateful().setIsAvailable(false);
        getPoolStateful().setIsChangeable(false);

        setNumOfDesktops(new NotChangableForVmInPoolEntityModel<Integer>());
        getNumOfDesktops().setEntity(0);
        getNumOfDesktops().setIsAvailable(false);

        setAssignedVms(new NotChangableForVmInPoolEntityModel<Integer>());
        getAssignedVms().setEntity(0);
        getAssignedVms().setIsAvailable(false);
        // Assigned VMs count is always read-only.
        getAssignedVms().setIsChangeable(false);

        setPrestartedVms(new NotChangableForVmInPoolEntityModel<Integer>());
        getPrestartedVms().setEntity(0);
        getPrestartedVms().setIsAvailable(false);

        setMaxAssignedVmsPerUser(new NotChangableForVmInPoolEntityModel<Integer>());
        getMaxAssignedVmsPerUser().setEntity(1);
        getMaxAssignedVmsPerUser().setIsAvailable(false);

        setDisksAllocationModel(new DisksAllocationModel());

        setIsVirtioScsiEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        getIsVirtioScsiEnabled().setIsAvailable(false);
        getIsVirtioScsiEnabled().getEntityChangedEvent().addListener(this);

        setProvisioningClone_IsSelected(new NotChangableForVmInPoolEntityModel<Boolean>());
        getProvisioningClone_IsSelected().getEntityChangedEvent().addListener(this);

        setProvisioningThin_IsSelected(new NotChangableForVmInPoolEntityModel<Boolean>());
        getProvisioningThin_IsSelected().getEntityChangedEvent().addListener(this);

        setCpuPinning(new NotChangableForVmInPoolEntityModel<String>());
        getCpuPinning().setEntity("");
        getCpuPinning().setIsChangeable(false);
        getCpuPinning().getEntityChangedEvent().addListener(this);

        setCpuPinningPolicy(new CpuPinningListModel());
        getCpuPinningPolicy().getSelectedItemChangedEvent().addListener(this);

        setCpuSharesAmount(new NotChangableForVmInPoolEntityModel<Integer>());
        getCpuSharesAmount().setIsChangeable(false);

        setCpuSharesAmountSelection(new NotChangableForVmInPoolListModel<CpuSharesAmount>());
        getCpuSharesAmountSelection().setItems(Arrays.asList(CpuSharesAmount.values()));
        getCpuSharesAmountSelection().getSelectedItemChangedEvent().addListener(this);
        getCpuSharesAmountSelection().setSelectedItem(CpuSharesAmount.DISABLED);


        setNumOfVirtioScsiMultiQueues(new NotChangableForVmInPoolEntityModel<Integer>());
        getNumOfVirtioScsiMultiQueues().setIsChangeable(false);

        setVirtioScsiMultiQueueTypeSelection(new NotChangableForVmInPoolListModel<>());
        getVirtioScsiMultiQueueTypeSelection().setItems(Arrays.asList(VirtioMultiQueueType.values()));
        getVirtioScsiMultiQueueTypeSelection().setSelectedItem(VirtioMultiQueueType.DISABLED);
        getVirtioScsiMultiQueueTypeSelection().getSelectedItemChangedEvent().addListener(this);


        setIsSoundcardEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        getIsSoundcardEnabled().setEntity(false);
        getIsSoundcardEnabled().setIsChangeable(false);

        selectSsoMethod(SsoMethod.GUEST_AGENT);

        setEditingEnabled(new EntityModel<Boolean>());
        getEditingEnabled().setEntity(true);

        setCpuProfiles(new NotChangableForVmInPoolListModel<CpuProfile>());
        getCpuProfiles().setIsAvailable(false);

        setNumaNodeCount(new NotChangableForVmInPoolEntityModel<Integer>());
        getNumaNodeCount().setEntity(0);
        setNumaEnabled(new EntityModel<Boolean>());
        getNumaEnabled().setMessage(ConstantsManager.getInstance().getConstants().numaDisabledInfoMessage());

        setNumaSupportCommand(new UICommand("NumaSupport", new ICommandTarget() { //$NON-NLS-1$
                    @Override
                    public void executeCommand(UICommand command) {
                        numaSupport();
                    }

                    @Override
                    public void executeCommand(UICommand uiCommand, Object... parameters) {
                        numaSupport();
                    }
                }));

        setAutoConverge(new ListModelWithClusterDefault<Boolean>());
        getAutoConverge().setItems(Arrays.asList(true, false));

        setMigrateCompressed(new ListModelWithClusterDefault<Boolean>());
        getMigrateCompressed().setItems(Arrays.asList(true, false));

        setMigrateEncrypted(new ListModelWithClusterDefault<Boolean>());
        getMigrateEncrypted().setItems(Arrays.asList(true, false));

        setIcon(new NotChangableForVmInPoolEntityModel<IconWithOsDefault>());

        setIoThreadsEnabled(new NotChangableForVmInPoolEntityModel<>(false));
        setNumOfIoThreads(new NotChangableForVmInPoolEntityModel<>("0")); //$NON-NLS-1$
        getIoThreadsEnabled().getEntityChangedEvent().addListener(this);

        setProviders(new NotChangableForVmInPoolListModel<Provider<OpenstackNetworkProviderProperties>>());
    }

    public void initForemanProviders(final Guid selected) {
        AsyncDataProvider.getInstance().getAllProvidersByType(new AsyncQuery<>(new AsyncCallback<List<Provider<?>>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(List<Provider<?>> result) {
                List<Provider<OpenstackNetworkProviderProperties>> providers = (List) result;
                Provider<OpenstackNetworkProviderProperties> noneProvider = createNoneProvider();
                providers.add(0, noneProvider);
                ListModel<Provider<OpenstackNetworkProviderProperties>> providersListModel = getProviders();
                if (selected != null) {
                    //Find the selected provider.
                    for (Provider<OpenstackNetworkProviderProperties> provider: providers) {
                        if (provider.getId().equals(selected)) {
                            providersListModel.setItems(providers, provider);
                            break;
                        }
                    }
                }
                if (providersListModel.getItems() == null || providersListModel.getItems().isEmpty()) {
                    providersListModel.setItems(providers, providers.get(0));
                }
                providersListModel.setIsChangeable(true);
            }

            private Provider<OpenstackNetworkProviderProperties> createNoneProvider() {
                Provider<OpenstackNetworkProviderProperties> noneProvider = new Provider<>();
                noneProvider.setId(Guid.Empty);
                noneProvider.setName(constants.providerNone());
                return noneProvider;
            }
        }), ProviderType.FOREMAN);
    }

    private void updateAffinityLists() {
        boolean isExistingVmBehavior = getBehavior() instanceof ExistingVmModelBehavior;

        AsyncCallback<List<AffinityGroup>> affinityGroupsCallback = groups -> {
            affinityGroupList.setItems(groups);
            if (isExistingVmBehavior) {
                Guid vmId = ((ExistingVmModelBehavior) getBehavior()).getVm().getId();
                affinityGroupList.setSelectedItems(groups.stream()
                        .filter(ag -> ag.getVmIds().contains(vmId))
                        .collect(Collectors.toList()));
            } else {
                affinityGroupList.setSelectedItems(new ArrayList<>());
            }
        };

        if (getSelectedCluster() == null) {
            // This must be a modifiable list, otherwise an exception is thrown.
            // Once the selected cluster is not null anymore and this list is changed for another one,
            // somewhere the code adds an element to this list, even if it is not used.
            affinityGroupsCallback.onSuccess(new ArrayList<>());
        } else {
            AsyncDataProvider.getInstance().getAffinityGroupsByClusterId(new AsyncQuery<>(affinityGroupsCallback),
                    getSelectedCluster().getId());
        }

        AsyncDataProvider.getInstance().getLabelList(new AsyncQuery<>(labels -> {
            labelList.setItems(labels);
            if (isExistingVmBehavior) {
                Guid vmId = ((ExistingVmModelBehavior) getBehavior()).getVm().getId();
                labelList.setSelectedItems(labels.stream()
                        .filter(label -> label.getVms().contains(vmId))
                        .collect(Collectors.toList()));
            } else {
                labelList.setSelectedItems(new ArrayList<>());
            }
        }));
    }

    public void addAffinityGroup() {
        AffinityGroup group = affinityGroupList.getSelectedItem();

        if (!affinityGroupList.getSelectedItems().contains(group)) {
            affinityGroupList.getSelectedItems().add(group);
            affinityGroupList.getSelectedItemsChangedEvent().raise(affinityGroupList, EventArgs.EMPTY);
        }
    }

    public void addAffinityLabel() {
        Label label = labelList.getSelectedItem();

        if (!labelList.getSelectedItems().contains(label)) {
            labelList.getSelectedItems().add(label);
            labelList.getSelectedItemsChangedEvent().raise(labelList, EventArgs.EMPTY);
        }
    }

    public boolean isHostedEngine() {
        boolean isExistingVmBehavior = getBehavior() instanceof ExistingVmModelBehavior;
        if (isExistingVmBehavior) {
            ExistingVmModelBehavior behavior = (ExistingVmModelBehavior)getBehavior();
            return behavior.getVm() != null ? behavior.getVm().isHostedEngine() : false;
        }
        return false;
    }

    public void initialize() {
        super.initialize();

        getMemSize().setEntity(256);
        getMinAllocatedMemory().setEntity(256);
        getMaxMemorySize().setEntity(VmCommonUtils.getMaxMemorySizeDefault(getMemSize().getEntity()));
        getIsStateless().setEntity(false);
        getIsRunAndPause().setEntity(false);
        getIsHeadlessModeEnabled().setEntity(false);
        getIsSmartcardEnabled().setEntity(false);
        isConsoleDeviceEnabled.setEntity(false);
        getIsHighlyAvailable().setEntity(false);
        getIsAutoAssign().setEntity(true);
        getIsTemplatePublic().setEntity(true);

        isRngEnabled.setEntity(false);
        rngSourceUrandom.setEntity(true);

        getHostCpu().setEntity(false);
        getMigrationMode().setIsChangeable(true);

        getCdImage().setIsChangeable(false);
        getIsVirtioScsiEnabled().setEntity(false);

        initGraphicsAndDisplayListeners();
        initFirstBootDevice();
        initNumOfMonitors();
        initAllowConsoleReconnect();
        initMigrationMode();
        initVncKeyboardLayout();
        initConsoleDisconnectAction();
        updateResumeBehavior();
        updateAffinityLists();
        updateTpmEnabled();

        behavior.initialize();
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition)) {
            if (sender == getVmType()) {
                vmTypeChanged();
            } else if (sender == getDataCenterWithClustersList()) {
                behavior.updateCompatibilityVersion(); // needs to be first because it affects compatibility version
                compatibilityVersionChanged(sender, args);
                behavior.updateEmulatedMachines();
                behavior.updateCustomCpu();
                behavior.updateBiosType();
                updateTscFrequency();
            } else if (sender == getTemplateWithVersion()) {
                templateWithVersion_SelectedItemChanged(sender, args);
            } else if (sender == getInstanceTypes()) {
                behavior.updateBiosType();
            } else if (sender == getTimeZone()) {
                timeZone_SelectedItemChanged(sender, args);
            } else if (sender == getOSType()) {
                getBehavior().deactivateInstanceTypeManager(() -> {
                    if (getBehavior().getInstanceTypeManager() != null && !getBehavior().basedOnCustomInstanceType()) {
                        getBehavior().getInstanceTypeManager().updateFildsAfterOsChanged();
                    }
                });

                oSType_SelectedItemChanged(sender, args);
                getBehavior().oSType_SelectedItemChanged();
                getVmInitModel().osTypeChanged(getOSType().getSelectedItem());
                updateDisplayAndGraphics();
                getBehavior().updateMemoryBalloon();

                headlessModeChanged();
                getBehavior().activateInstanceTypeManager();
            } else if (sender == getFirstBootDevice()) {
                firstBootDevice_SelectedItemChanged(sender, args);
            } else if (sender == getDisplayType()) {
                initGraphicsConsoles();
                initUsbPolicy();
            } else if (sender == getGraphicsType()) {
                upgradeGraphicsRelatedModels();
                initUsbPolicy();
            } else if (sender == getNumOfSockets()) {
                numOfSockets_EntityChanged(sender, args);
            } else if (sender == getCoresPerSocket()) {
                coresPerSocket_EntityChanged(sender, args);
            } else if (sender == getThreadsPerCore()) {
                threadsPerCore_EntityChanged(sender, args);
            } else if (sender == getMigrationMode()) {
                behavior.updateCpuPinningVisibility();
                updateTscFrequency();
            } else if (sender == getMigrationPolicies()) {
                updateMigrationRelatedFields();
            } else if (sender == getCpuSharesAmountSelection()) {
                behavior.updateCpuSharesAmountChangeability();
            } else if (sender == getVirtioScsiMultiQueueTypeSelection()) {
                virtioScsiMultiQueueSelectedItemChanged();
            } else if (sender == getBaseTemplate()) {
                behavior.baseTemplateSelectedItemChanged();
            } else if (sender == getWatchdogModel()) {
                watchdogModelSelectedItemChanged(sender, args);
            } else if (sender == getCustomCompatibilityVersion()) {
                // window must be updated as if a cluster change occurred because feature availability should be reconsidered
                if (behavior.isCustomCompatibilityVersionChangeInProgress()) {
                    return;
                }

                // A workaround for saving the current CustomCompatibilityVersion value for re-setting it after
                // it will be reset by the getTemplateWithVersion event.
                // This is relevant for new VM only
                behavior.setCustomCompatibilityVersionChangeInProgress(true);
                behavior.setSavedCurrentCustomCompatibilityVersion(getCustomCompatibilityVersion().getSelectedItem());

                compatibilityVersionChanged(sender, args);
                headlessModeChanged();
                updateResumeBehavior();
            } else if (sender == getLease()) {
                updateResumeBehavior();
            } else if (sender == getBiosType()) {
                updateDisplayAndGraphics();
                updateTpmEnabled();
            } else if (sender == getCpuPinningPolicy()) {
                cpuPinningPolicyChanged();
            } else if (sender == getConsoleDisconnectAction()){
                consoleDisconnectActionSelectedItemChanged();
            }
        } else if (ev.matchesDefinition(ListModel.selectedItemsChangedEventDefinition)) {
            if (sender == getDefaultHost()) {
                defaultHost_SelectedItemChanged(sender, args);
                behavior.updateNumaEnabled();
                headlessModeChanged();
            }
        } else if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
            if (sender == getVmInitEnabled()) {
                vmInitEnabledChanged();
            } else if (sender == getIoThreadsEnabled()) {
                behavior.updateNumOfIoThreads();
            } else if (sender == getMemSize()) {
                memSize_EntityChanged(sender, args);
            } else if (sender == getTotalCPUCores()) {
                totalCPUCores_EntityChanged(sender, args);
            } else if (sender == getIsAutoAssign()) {
                behavior.updateUseHostCpuAvailability();
                behavior.updateCpuPinningVisibility();
                behavior.updateNumaEnabled();
                updateCpuPinningPolicy();
            } else if (sender == getProvisioning()) {
                provisioning_SelectedItemChanged(sender, args);
            } else if (sender == getProvisioningThin_IsSelected()) {
                if (getProvisioningThin_IsSelected().getEntity()) {
                    getProvisioning().setEntity(false);
                }
            } else if (sender == getProvisioningClone_IsSelected()) {
                if (getProvisioningClone_IsSelected().getEntity()) {
                    getProvisioning().setEntity(true);
                }
            } else if (sender == getIsHighlyAvailable()) {
                updateResumeBehavior();
            } else if (sender == getIsSubTemplate()) {
                behavior.isSubTemplateEntityChanged();
            } else if (sender == getHostCpu()) {
                behavior.useHostCpuValueChanged();
            } else if (sender == getName()) {
                autoSetHostname();
            } else if (sender == getIsHeadlessModeEnabled()) {
                headlessModeChanged();
            } else if (sender == getCpuPinning()) {
                cpuPinningChanged();
            } else if (sender == getIsVirtioScsiEnabled()) {
                virtioScsiChanged();
            }
        }
    }

    private void virtioScsiChanged() {
        boolean isVirtioScsiEnabled = getIsVirtioScsiEnabled().getEntity();
        if (!isVirtioScsiEnabled) {
            getVirtioScsiMultiQueueTypeSelection().setSelectedItem(VirtioMultiQueueType.DISABLED);
        }
        getVirtioScsiMultiQueueTypeSelection()
                .setIsChangeable(isVirtioScsiEnabled, messages.virtioScsiRequired());
    }

    private void enableCpuFields() {
        getTotalCPUCores().setIsChangeable(true);
        getNumOfSockets().setIsChangeable(true);
        getCoresPerSocket().setIsChangeable(true);
        getThreadsPerCore().setIsChangeable(true);
    }

    private void disableCpuFields() {
        getTotalCPUCores().setIsChangeable(false, constants.cpuChangesConflictWithAutoPin());
        getNumOfSockets().setIsChangeable(false, constants.cpuChangesConflictWithAutoPin());
        getCoresPerSocket().setIsChangeable(false, constants.cpuChangesConflictWithAutoPin());
        getThreadsPerCore().setIsChangeable(false, constants.cpuChangesConflictWithAutoPin());
    }

    private void compatibilityVersionChanged(Object sender, EventArgs args) {
        dataCenterWithClusterSelectedItemChanged(sender, args);
        updateDisplayAndGraphics();
        behavior.updateNumOfIoThreads();
        initUsbPolicy();
        updateMultiQueues();
        updateMigrateEncrypted();
        updateSerialNumberPolicy();
        updateTpmEnabled();
    }

    private void updateMultiQueues() {
        Version compatibilityVersion = getCompatibilityVersion();

        if (compatibilityVersion == null) {
            return;
        }

        getMultiQueues().setIsAvailable(true);
    }

    private void virtioScsiMultiQueueSelectedItemChanged() {
        getNumOfVirtioScsiMultiQueues()
                .setEntity(null);

        // if the user choose CUSTOM and wrote invalid value, then switched back for example to DISABLED
        // the input should not appear with red border
        getNumOfVirtioScsiMultiQueues().setIsValid(true);

        boolean changeable =
                getVirtioScsiMultiQueueTypeSelection().getSelectedItem() == VirtioMultiQueueType.CUSTOM;
        getNumOfVirtioScsiMultiQueues().setIsChangeable(changeable);
    }

    private void vmInitEnabledChanged() {
        if(!getVmInitEnabled().getEntity()) {
            getSysprepEnabled().setEntity(false);
            getCloudInitEnabled().setEntity(false);
            getIgnitionEnabled().setEntity(false);
        } else {
            getSysprepEnabled().setEntity(getIsWindowsOS());
            if(getIsIgnition()) {
                // because the usage of the same flow panel order matters
                getCloudInitEnabled().setEntity(!getIsWindowsOS() && !getIsIgnition());
                getIgnitionEnabled().setEntity(getIsIgnition());
            } else {
                // for the "other" also use cloud init
                getIgnitionEnabled().setEntity(getIsIgnition());
                getCloudInitEnabled().setEntity(!getIsWindowsOS() && !getIsIgnition());
            }
            autoSetHostname();

            if (getSysprepEnabled().getEntity()) {
                getVmInitModel().updateSysprepDomain(getVmInitModel().getSysprepDomain().getSelectedItem());
            }
        }
    }

    private void autoSetHostname() {
        if(getVmInitEnabled().getEntity()) {
            getVmInitModel().autoSetHostname(getName().getEntity());
        }
    }

    private void vmTypeChanged() {
        behavior.vmTypeChanged(getVmType().getSelectedItem());
        updateTscFrequency();
    }

    private void watchdogModelSelectedItemChanged(Object sender, EventArgs args) {
        if (getWatchdogModel().getSelectedItem() == null) {
            getWatchdogAction().setIsChangeable(false);
        } else {
            getWatchdogAction().setIsChangeable(true);
        }
    }

    private void consoleDisconnectActionSelectedItemChanged() {
        if (ConsoleDisconnectAction.SHUTDOWN.equals(consoleDisconnectAction.getSelectedItem())) {
            getConsoleDisconnectActionDelay().setIsChangeable(true);
        } else {
            getConsoleDisconnectActionDelay().setIsChangeable(false, constants.consoleDisconnectActionDelayDisabledReason());
        }
    }

    protected void initNumOfMonitors() {
        AsyncDataProvider.getInstance().getNumOfMonitorList(new AsyncQuery<>(
                numOfMonitors -> {

                    Integer oldNumOfMonitors = null;
                    if (getNumOfMonitors().getSelectedItem() != null) {
                        oldNumOfMonitors = getNumOfMonitors().getSelectedItem();
                    }
                    getNumOfMonitors().setItems(numOfMonitors);
                    if (oldNumOfMonitors != null) {
                        getNumOfMonitors().setSelectedItem(oldNumOfMonitors);
                    }

                }));

    }

    protected void initAllowConsoleReconnect() {
        getAllowConsoleReconnect().setEntity(getVmType().getSelectedItem() == VmType.Server);
    }

    private void initConsoleDisconnectAction() {
        getConsoleDisconnectAction().setItems(Arrays.asList(ConsoleDisconnectAction.values()));
    }

    public void updateResumeBehavior() {
        if (getSelectedCluster() == null) {
            return;
        }

        getResumeBehavior().setIsChangeable(true);

        if (!getResumeBehavior().getIsChangable()) {
            getResumeBehavior().setSelectedItem(null);
            return;
        }

        VmResumeBehavior prevSelected = getResumeBehavior().getSelectedItem();
        boolean haWithLease = getIsHighlyAvailable().getEntity() != null &&
                getIsHighlyAvailable().getEntity() &&
                getLease().getIsAvailable() &&
                getLease().getSelectedItem() != null;

        if (haWithLease) {
            getResumeBehavior().setItems(Arrays.asList(VmResumeBehavior.KILL), VmResumeBehavior.KILL);
        } else {
            getResumeBehavior().setItems(
                    Arrays.asList(VmResumeBehavior.values()),
                    prevSelected);
        }
    }

    private void initUsbPolicy() {
        GraphicsTypes graphicsTypes = getGraphicsType().getSelectedItem();

        if (graphicsTypes == null) {
            return;
        }

        getIsUsbEnabled().setIsChangeable(true);

        if (!graphicsTypes.getBackingGraphicsTypes().contains(GraphicsType.SPICE)) {
            getIsUsbEnabled().setIsChangeable(false);
        }
    }

    private void updateMigrationOptions() {
        DataCenterWithCluster dataCenterWithCluster =
                getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return;
        }

        Cluster cluster = dataCenterWithCluster.getCluster();
        Version version = getCompatibilityVersion();

        // test migration support for VM/cluster level along with the cluster architecture
        Boolean isMigrationSupported =
                AsyncDataProvider.getInstance().isMigrationSupported(cluster.getArchitecture(), version);

        if (isMigrationSupported) {
            getMigrationMode().setItems(Arrays.asList(MigrationSupport.values()));
        } else {
            getMigrationMode().setItems(Arrays.asList(MigrationSupport.PINNED_TO_HOST));
        }
    }

    private void initGraphicsAndDisplayListeners() {
        getIsHeadlessModeEnabled().getEntityChangedEvent().addListener(this);
        getDisplayType().getSelectedItemChangedEvent().addListener(this);
        getGraphicsType().getSelectedItemChangedEvent().addListener(this);
    }

    private void updateTscFrequency() {
        if (getSelectedCluster() == null || getVmType() == null || getMigrationMode() == null) {
            return;
        }

        boolean isChangeable = AsyncDataProvider.getInstance().isTscSupportedByVersion(getSelectedCluster().getCompatibilityVersion())
                && ArchitectureType.x86.equals(getSelectedCluster().getArchitecture().getFamily())
                && VmType.HighPerformance.equals(getVmType().getSelectedItem())
                && (MigrationSupport.MIGRATABLE.equals(getMigrationMode().getSelectedItem())
                 || MigrationSupport.IMPLICITLY_NON_MIGRATABLE.equals(getMigrationMode().getSelectedItem()));
        // If it's editable, set it to true. Users might override the value manually if needed
        tscFrequency.setIsChangeable(isChangeable);
        tscFrequency.setEntity(isChangeable);
    }

    private void updateDisplayAndGraphics() {
        Cluster cluster = getSelectedCluster();
        Integer osType = getOSType() != null ? getOSType().getSelectedItem() : null;
        BiosType biosType = getBiosType() != null ? getBiosType().getSelectedItem() : null;

        if (cluster == null || osType == null || biosType == null) {
            return;
        }

        initDisplayModels(getSupportedDisplayTypes(osType, getBiosType().getSelectedItem()));
    }

    private Set<DisplayType> getSupportedDisplayTypes(int osId, BiosType biosType) {
        if (biosType == null) {
            return Collections.emptySet();
        }
        return AsyncDataProvider.getInstance().getGraphicsAndDisplays(osId, getCompatibilityVersion()).stream()
                .map(Pair::getSecond)
                .filter(dt -> dt != DisplayType.none)
                .filter(dt -> biosType.isOvmf() || dt != DisplayType.bochs)
                .collect(Collectors.toSet());
    }

    public void initDisplayModels(Set<DisplayType> displayTypes) {
        DisplayType selectedDisplayType = getDisplayType().getSelectedItem();
        initDisplayModels(displayTypes, selectedDisplayType);
    }

    public void initDisplayModels(Set<DisplayType> displayTypes, DisplayType selectedDisplayType) {
        // set items and set selected one
        if (displayTypes.contains(selectedDisplayType)) {
            getDisplayType().setItems(displayTypes);
            getDisplayType().setSelectedItem(selectedDisplayType);
        } else if (displayTypes.size() > 0) {
            getDisplayType().setItems(displayTypes);
            getDisplayType().setSelectedItem(displayTypes.iterator().next());
        }
    }

    private void initFirstBootDevice() {
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().hardDiskTitle());
        tempVar.setEntity(BootSequence.C);
        EntityModel hardDiskOption = tempVar;

        List<EntityModel<BootSequence>> firstBootDeviceItems = new ArrayList<>();
        firstBootDeviceItems.add(hardDiskOption);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cdromTitle());
        tempVar2.setEntity(BootSequence.D);
        firstBootDeviceItems.add(tempVar2);
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().networkPXETitle());
        tempVar3.setEntity(BootSequence.N);
        firstBootDeviceItems.add(tempVar3);
        getFirstBootDevice().setItems(firstBootDeviceItems);
        getFirstBootDevice().setSelectedItem(hardDiskOption);
    }

    private void initMigrationMode() {
        getMigrationMode().setItems(Arrays.asList(MigrationSupport.values()));
    }

    private void initVncKeyboardLayout() {
        final List<String> layouts =
                (List<String>) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.VncKeyboardLayoutValidValues);
        final ArrayList<String> vncKeyboardLayoutItems = new ArrayList<>();
        vncKeyboardLayoutItems.add(null); // null value means the global VncKeyboardLayout from vdc_options will be used
        vncKeyboardLayoutItems.addAll(layouts);
        getVncKeyboardLayout().setItems(vncKeyboardLayoutItems);

        GraphicsTypes graphicsTypes = getGraphicsType().getSelectedItem();
        if (graphicsTypes != null) {
            getVncKeyboardLayout().setIsAvailable(graphicsTypes.getBackingGraphicsTypes().contains(GraphicsType.VNC));
        }
    }

    private void dataCenterWithClusterSelectedItemChanged(Object sender, EventArgs args) {
        String selectedTimeZone = "";
        Integer selectedOsId = null;

        if (getTimeZone().getSelectedItem() != null) {
            selectedTimeZone = getBehavior().getSelectedTimeZone();
        }

        if (getOSType().getSelectedItem() != null) {
            selectedOsId = getBehavior().getSelectedOSType();
        }

        behavior.dataCenterWithClusterSelectedItemChanged();
        refreshMigrationPolicies();
        updateMigrationRelatedFields();
        updateClusterMigrationRelatedFields();
        DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster != null && dataCenterWithCluster.getDataCenter() != null) {
            getDisksAllocationModel().setQuotaEnforcementType(dataCenterWithCluster.getDataCenter()
                                                                      .getQuotaEnforcementType());
        }

        updateMigrationOptions();
        updateSpiceFeatures();

        updateWatchdogModels();
        updateBootMenu();
        getInstanceImages().updateActionsAvailability();

        initGraphicsConsoles();

        updateSoundCard();
        updateResumeBehavior();
        updateTpmEnabled();

        getBehavior().updateOSValue(selectedOsId);

        if (!selectedTimeZone.isEmpty()) {
            getBehavior().updateTimeZone(selectedTimeZone);
        }

        updateAffinityLists();
    }

    private void updateBootMenu() {
        if (getSelectedCluster() != null) {
            getBootMenuEnabled().setIsChangeable(true);
        }
    }

    private void updateSpiceFeatures() {
        if (getSelectedCluster() != null) {
            GraphicsTypes selectedGraphics = getGraphicsType().getSelectedItem();
            boolean isSpiceUsed = selectedGraphics != null
                    && selectedGraphics.getBackingGraphicsTypes().contains(GraphicsType.SPICE);
            if (!isSpiceUsed) {
                setSpiceFeatureProhibitionReason(getSpiceFileTransferEnabled());
                setSpiceFeatureProhibitionReason(getSpiceCopyPasteEnabled());
            }
            getSpiceFileTransferEnabled().setIsChangeable(isSpiceUsed);
            getSpiceCopyPasteEnabled().setIsChangeable(isSpiceUsed);
        }
    }

    private void setSpiceFeatureProhibitionReason(EntityModel<Boolean> checkbox) {
        checkbox.setChangeProhibitionReason(ConstantsManager.getInstance().getMessages().optionRequiresSpiceEnabled());
    }

    private void templateWithVersion_SelectedItemChanged(Object sender, EventArgs args) {
        behavior.templateWithVersion_SelectedItemChanged();
    }

    private void timeZone_SelectedItemChanged(Object sender, EventArgs args) {
    }

    private void defaultHost_SelectedItemChanged(Object sender, EventArgs args) {
        behavior.defaultHost_SelectedItemChanged();
        updateCpuPinningPolicy();
    }

    private void oSType_SelectedItemChanged(Object sender, EventArgs args) {
        Integer osType = getOSType().getSelectedItem();

        setIsWindowsOS(AsyncDataProvider.getInstance().isWindowsOsType(osType));
        setIsLinuxOS(AsyncDataProvider.getInstance().isLinuxOsType(osType));
        setIsIgnition(AsyncDataProvider.getInstance().isIgnition(osType));

        getInitrd_path().setIsChangeable(getIsLinuxOS());
        getInitrd_path().setIsAvailable(getIsLinuxOS());

        getKernel_path().setIsChangeable(getIsLinuxOS());
        getKernel_path().setIsAvailable(getIsLinuxOS());

        getKernel_parameters().setIsChangeable(getIsLinuxOS());
        getKernel_parameters().setIsAvailable(getIsLinuxOS());

        getBehavior().updateDefaultTimeZone();

        updateSpiceFeatures();

        updateWatchdogModels(osType);

        vmInitEnabledChanged();
        getInstanceImages().updateActionsAvailability();

        updateIconAccordingToOs();

        initGraphicsConsoles();

        updateSoundCard();
        updateTpmEnabled();
        getBehavior().updateMemory();
        getBehavior().updateTotalCpuCores();
    }

    private void updateIconAccordingToOs() {
        final Integer osId = getOSType().getSelectedItem();
        final Guid largeOsIconId = AsyncDataProvider.getInstance().getOsDefaultIconId(osId, false);
        final Guid smallOsIconId = AsyncDataProvider.getInstance().getSmallByLargeOsDefaultIconId(largeOsIconId);
        if (getIcon().getEntity() == null) {
            IconWithOsDefault.create(largeOsIconId, smallOsIconId, instance -> getIcon().setEntity(instance));
        } else {
            getIcon().getEntity().withDifferentOsIcon(largeOsIconId, smallOsIconId,
                    instance -> getIcon().setEntity(instance));
        }
    }

    private void updateWatchdogModels() {
        updateWatchdogModels(getOSType().getSelectedItem());
    }

    private void updateWatchdogModels(Integer osType) {
        Cluster cluster = getSelectedCluster();
        if (osType != null && cluster != null && getWatchdogModel() != null) {
            AsyncDataProvider.getInstance().getVmWatchdogTypes(osType, getCompatibilityVersion(), new AsyncQuery<>(
                    returnValue -> {
                        getBehavior().deactivateInstanceTypeManager();

                        updateWatchdogItems((HashSet<VmWatchdogType>) returnValue.getReturnValue());

                        getBehavior().activateInstanceTypeManager();

                    }));
        }
    }

    public void updateWatchdogItems(Set<VmWatchdogType> vmWatchdogTypes) {
        List<VmWatchdogType> watchDogModels = new ArrayList<>();
        for (VmWatchdogType vmWatchdogType : vmWatchdogTypes) {
            watchDogModels.add(vmWatchdogType);
        }

        watchDogModels.add(0, null);
        VmWatchdogType oldWatchdogSelected = getWatchdogModel().getSelectedItem();
        if (watchDogModels.contains(getWatchdogModel().getSelectedItem())) {
            getWatchdogModel().setItems(watchDogModels, getWatchdogModel().getSelectedItem());
        } else {
            getWatchdogModel().setItems(watchDogModels);
        }

        if (watchDogModels.contains(oldWatchdogSelected)) {
            getWatchdogModel().setSelectedItem(oldWatchdogSelected);
        }
    }

    private void firstBootDevice_SelectedItemChanged(Object sender, EventArgs args) {
        EntityModel<BootSequence> entityModel = getFirstBootDevice().getSelectedItem();
        BootSequence firstDevice = entityModel.getEntity();
        EntityModel<BootSequence> prevItem = null;

        List<EntityModel<BootSequence>> list = new ArrayList<>();
        for (EntityModel<BootSequence> item : getFirstBootDevice().getItems()) {
            if (item.getEntity() != firstDevice) {
                list.add(item);
                if (getSecondBootDevice().getSelectedItem() != null && item.getEntity() == getSecondBootDevice().getSelectedItem().getEntity()) {
                    prevItem = item;
                }
            }
        }

        EntityModel<BootSequence> tempVar = new EntityModel<>();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().noneTitle());
        EntityModel<BootSequence> noneOption = tempVar;

        list.add(0, noneOption);

        getSecondBootDevice().setItems(list);
        if (prevItem != null) {
            getSecondBootDevice().setSelectedItem(prevItem);
        } else {
            getSecondBootDevice().setSelectedItem(noneOption);
        }
    }

    private void provisioning_SelectedItemChanged(Object sender, EventArgs args) {
        behavior.provisioning_SelectedItemChanged();
    }

    private void updateSoundCard() {
        if (getOSType().getSelectedItem() != null && getSelectedCluster() != null) {
            int osType = getOSType().getSelectedItem();
            boolean soundCardEnabled = AsyncDataProvider.getInstance().isSoundDeviceEnabled(osType,
                    getSelectedCluster().getCompatibilityVersion());
            getIsSoundcardEnabled().setIsChangeable(soundCardEnabled, constants.soundDeviceUnavailable());
        }
    }

    protected void initGraphicsConsoles() {
        Cluster cluster = getSelectedCluster();
        Integer osType = getOSType().getSelectedItem();

        if (cluster == null || osType == null) {
            return;
        }

        initGraphicsConsoles(osType, getCompatibilityVersion());
    }

    protected void initGraphicsConsoles(int osType, Version compatibilityVersion) {
        Set<GraphicsTypes> graphicsTypes = new LinkedHashSet<>();
        List<Pair<GraphicsType, DisplayType>> graphicsAndDisplays = AsyncDataProvider.getInstance().getGraphicsAndDisplays(osType, compatibilityVersion);

        for (Pair<GraphicsType, DisplayType> graphicsAndDisplay : graphicsAndDisplays) {
            if (graphicsAndDisplay.getSecond() == getDisplayType().getSelectedItem()) {
                graphicsTypes.add(GraphicsTypes.fromGraphicsType(graphicsAndDisplay.getFirst()));
            }
        }

        if (graphicsTypes.contains(GraphicsTypes.SPICE) && graphicsTypes.contains(GraphicsTypes.VNC)) {
            graphicsTypes.add(GraphicsTypes.SPICE_AND_VNC);
        }

        GraphicsTypes prevSelected = getGraphicsType().getSelectedItem();

        if (prevSelected != null && graphicsTypes.contains(prevSelected)) {
            getGraphicsType().setItems(graphicsTypes, prevSelected);
        } else {
            getGraphicsType().setItems(graphicsTypes);
        }

        upgradeGraphicsRelatedModels();
    }

    private void upgradeGraphicsRelatedModels() {
        DisplayType display = getDisplayType().getSelectedItem();
        GraphicsTypes graphics = getGraphicsType().getSelectedItem();

        if (display == null || graphics == null) {
            return;
        }

        if (display != DisplayType.qxl || !graphics.getBackingGraphicsTypes().contains(GraphicsType.SPICE)) {
            getIsUsbEnabled().setEntity(false);
            getIsSmartcardEnabled().setEntity(false);
        }

        updateSpiceFeatures();
        getIsUsbEnabled().setIsChangeable(graphics.getBackingGraphicsTypes().contains(GraphicsType.SPICE));
        getIsSmartcardEnabled().setIsChangeable(graphics.getBackingGraphicsTypes().contains(GraphicsType.SPICE));
        getVncKeyboardLayout().setIsAvailable(graphics.getBackingGraphicsTypes().contains(GraphicsType.VNC));
        updateNumOfMonitors();
    }

    private void headlessModeChanged() {
        boolean isHeadlessEnabled = Boolean.TRUE.equals(getIsHeadlessModeEnabled().getEntity());
        getDisplayType().setIsChangeable(!isHeadlessEnabled);
        getGraphicsType().setIsChangeable(!isHeadlessEnabled);
        getVncKeyboardLayout().setIsChangeable(!isHeadlessEnabled);
        getConsoleDisconnectAction().setIsChangeable(!isHeadlessEnabled);
        getSsoMethodNone().setIsChangeable(!isHeadlessEnabled);
        getSsoMethodGuestAgent().setIsChangeable(!isHeadlessEnabled);
        getAllowConsoleReconnect().setIsChangeable(!isHeadlessEnabled);
        getSpiceProxyEnabled().setIsChangeable(!isHeadlessEnabled);
        getSpiceProxy().setIsChangeable(!isHeadlessEnabled && getSpiceProxyEnabled().getEntity());
        getIsSoundcardEnabled().setIsChangeable(!isHeadlessEnabled);

        if (isHeadlessEnabled) {
            getIsUsbEnabled().setIsChangeable(!isHeadlessEnabled);
            getNumOfMonitors().setIsChangeable(!isHeadlessEnabled);
            getIsSmartcardEnabled().setIsChangeable(!isHeadlessEnabled);
            getSpiceFileTransferEnabled().setIsChangeable(!isHeadlessEnabled);
            getSpiceCopyPasteEnabled().setIsChangeable(!isHeadlessEnabled);
        } else {
            upgradeGraphicsRelatedModels();
            updateSoundCard();
        }
    }

    private void memSize_EntityChanged(Object sender, EventArgs args) {
        behavior.updateMinAllocatedMemory();
        behavior.updateMaxMemory();
    }

    private void totalCPUCores_EntityChanged(Object sender, EventArgs args) {
        // do not listen on changes while the totalCpuCoresChanged is adjusting them
        removeCPUListeners();

        behavior.totalCpuCoresChanged();

        // start listening again
        addCPUListeners();
    }

    private void removeCPUListeners() {
        getTotalCPUCores().getEntityChangedEvent().removeListener(this);
        getNumOfSockets().getSelectedItemChangedEvent().removeListener(this);
        getCoresPerSocket().getSelectedItemChangedEvent().removeListener(this);
        getThreadsPerCore().getSelectedItemChangedEvent().removeListener(this);
    }

    private void addCPUListeners() {
        getTotalCPUCores().getEntityChangedEvent().addListener(this);
        getNumOfSockets().getSelectedItemChangedEvent().addListener(this);
        getCoresPerSocket().getSelectedItemChangedEvent().addListener(this);
        getThreadsPerCore().getSelectedItemChangedEvent().addListener(this);
    }

    private void numOfSockets_EntityChanged(Object sender, EventArgs args) {
        removeCPUListeners();
        behavior.numOfSocketChanged();
        addCPUListeners();
    }

    private void coresPerSocket_EntityChanged(Object sender, EventArgs args) {
        removeCPUListeners();
        behavior.coresPerSocketChanged();
        addCPUListeners();
    }

    private void threadsPerCore_EntityChanged(Object sender, EventArgs args) {
        removeCPUListeners();
        behavior.threadsPerCoreChanged();
        addCPUListeners();
    }

    private void updateNumOfMonitors() {
        if (getDisplayType().getSelectedItem() == DisplayType.qxl) {
            getNumOfMonitors().setIsChangeable(true);
        } else {
            getNumOfMonitors().setSelectedItem(1);
            getNumOfMonitors().setIsChangeable(false);
        }
    }

    public BootSequence getBootSequence() {
        EntityModel<BootSequence> firstSelectedItem = getFirstBootDevice().getSelectedItem();
        EntityModel<BootSequence> secondSelectedItem = getSecondBootDevice().getSelectedItem();

        String firstSelectedString =
                firstSelectedItem.getEntity() == null ? "" : firstSelectedItem.getEntity().toString(); //$NON-NLS-1$
        String secondSelectedString =
                secondSelectedItem.getEntity() == null ? "" : secondSelectedItem.getEntity().toString(); //$NON-NLS-1$

        return BootSequence.valueOf(firstSelectedString + secondSelectedString);
    }

    public void setBootSequence(BootSequence value) {
        ArrayList<BootSequence> items = new ArrayList<>();
        for (char a : value.toString().toCharArray()) {
            items.add(BootSequence.valueOf(String.valueOf(a)));
        }

        EntityModel<BootSequence> firstBootDevice = null;
        for (EntityModel<BootSequence> item : getFirstBootDevice().getItems()) {
            if (item.getEntity() == Linq.firstOrNull(items)) {
                firstBootDevice = item;
            }
        }
        getFirstBootDevice().setSelectedItem(firstBootDevice);

        Iterable<EntityModel<BootSequence>> secondDeviceOptions = getSecondBootDevice().getItems();

        if (items.size() > 1) {
            BootSequence last = items.get(items.size() - 1);
            for (EntityModel<BootSequence> a : secondDeviceOptions) {
                if (a.getEntity() != null && a.getEntity() == last) {
                    getSecondBootDevice().setSelectedItem(a);
                    break;
                }
            }
        } else {
            for (EntityModel<BootSequence> a : secondDeviceOptions) {
                if (a.getEntity() == null) {
                    getSecondBootDevice().setSelectedItem(a);
                    break;
                }
            }
        }
    }

    public void setDataCentersAndClusters(UnitVmModel model,
            List<StoragePool> dataCenters,
            List<Cluster> clusters,
            Guid selectedCluster) {

        setupDataCenterWithClusters(model, dataCenters, clusters, selectedCluster);
    }

    protected void setupDataCenterWithClusters(UnitVmModel model,
            List<StoragePool> dataCenters,
            List<Cluster> clusters,
            Guid selectedCluster) {

        Map<Guid, List<Cluster>> dataCenterToCluster = new HashMap<>();
        for (Cluster cluster : clusters) {
            if (cluster.getStoragePoolId() == null) {
                continue;
            }

            if (!dataCenterToCluster.containsKey(cluster.getStoragePoolId())) {
                dataCenterToCluster.put(cluster.getStoragePoolId(), new ArrayList<Cluster>());
            }
            dataCenterToCluster.get(cluster.getStoragePoolId()).add(cluster);
        }

        List<DataCenterWithCluster> dataCentersWithClusters = new ArrayList<>();

        for (StoragePool dataCenter : dataCenters) {
            if (dataCenterToCluster.containsKey(dataCenter.getId())) {
                for (Cluster cluster : dataCenterToCluster.get(dataCenter.getId())) {
                    dataCentersWithClusters.add(new DataCenterWithCluster(dataCenter, cluster));
                }
            }
        }
        selectDataCenterWithCluster(selectedCluster, dataCentersWithClusters);
    }

    protected void selectDataCenterWithCluster(Guid selectedCluster, List<DataCenterWithCluster> dataCentersWithClusters) {
        DataCenterWithCluster selectedDataCenterWithCluster =
                (selectedCluster == null) ? Linq.firstOrNull(dataCentersWithClusters)
                        : Linq.firstOrNull(dataCentersWithClusters,
                                new Linq.DataCenterWithClusterAccordingClusterPredicate(selectedCluster));
        getDataCenterWithClustersList().setItems(dataCentersWithClusters, selectedDataCenterWithCluster);
    }

    private StoragePool findDataCenterById(List<StoragePool> list, Guid id) {
        if (id == null) {
            return null;
        }

        for (StoragePool dc : list) {
            if (dc.getId().equals(id)) {
                return dc;
            }
        }

        return null;
    }

    public boolean validate() {
        return this.validate(true);
    }

    public boolean validate(boolean templateWithVersionRequired) {
        resetTabsValidity();

        getInstanceTypes().setIsValid(true);
        getInstanceTypes().validateSelectedItem(new IValidation[]{new NotEmptyValidation()});

        getDataCenterWithClustersList().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getOSType().validateSelectedItem(new NotEmptyValidation[] { new NotEmptyValidation() });

        DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();

        StoragePool dataCenter =
                dataCenterWithCluster == null ? null : dataCenterWithCluster.getDataCenter();
        if (dataCenter != null && dataCenter.getQuotaEnforcementType() == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT) {
            getQuota().validateSelectedItem(new IValidation[] { new NotEmptyQuotaValidation() });
        }

        if (getOSType().getIsValid()) {
            validateNaming();

            getVmId().setIsValid(true);
            if (getVmId().getIsAvailable() && !StringHelper.isNullOrEmpty(getVmId().getEntity())) {
                getVmId().validateEntity(new IValidation[] { new GuidValidation() });
            }

            getComment().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

            setValidTab(TabName.GENERAL_TAB, isValidTab(TabName.GENERAL_TAB)
                    && getVmId().getIsValid()
                    && getComment().getIsValid());
        }

        if (templateWithVersionRequired) {
            getTemplateWithVersion().validateSelectedItem(
                    new IValidation[]{new NotEmptyValidation(), createEachDiskAHasStorageDomainValidation()});
        }
        getDisksAllocationModel().validateEntity(new IValidation[]{});

        getCdImage().setIsValid(true);
        if (getCdImage().getIsChangable()) {
            getCdImage().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        if (getIsLinuxOS()) {
            getKernel_path().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getInitrd_path().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getKernel_parameters().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });

            // initrd path and kernel params require kernel path to be filled
            if (StringHelper.isNullOrEmpty(getKernel_path().getEntity())) {

                if (!StringHelper.isNullOrEmpty(getInitrd_path().getEntity())) {
                    getInitrd_path().getInvalidityReasons().add(constants.initrdPathInvalid());
                    getInitrd_path().setIsValid(false);
                    getKernel_path().getInvalidityReasons().add(constants.initrdPathInvalid());
                    getKernel_path().setIsValid(false);
                }

                if (!StringHelper.isNullOrEmpty(getKernel_parameters().getEntity())) {
                    getKernel_parameters().getInvalidityReasons().add(constants.kernelParamsInvalid());
                    getKernel_parameters().setIsValid(false);
                    getKernel_path().getInvalidityReasons().add(constants.kernelParamsInvalid());
                    getKernel_path().setIsValid(false);
                }
            }
        }

        if (!getBehavior().isBlankTemplateBehavior()) {
            setValidTab(TabName.GENERAL_TAB, isValidTab(TabName.GENERAL_TAB)
                    && getDataCenterWithClustersList().getIsValid()
                    && getTemplateWithVersion().getIsValid());
        }

        setValidTab(TabName.HOST_TAB, isValidTab(TabName.HOST_TAB) && getMigrationDowntime().getIsValid());

        getCpuPinningPolicy().validateSelectedItem();

        if (getCpuPinningPolicy().getSelectedItem().getPolicy() == CpuPinningPolicy.MANUAL) {
            getCpuPinning().validateEntity(new IValidation[]{new NotEmptyValidation()});
        } else {
            getCpuPinning().setIsValid(true);
        }
        boolean diskAliasesValid = getDisksAllocationModel().getIsValid();

        setValidTab(TabName.RESOURCE_ALLOCATION_TAB,
                isValidTab(TabName.RESOURCE_ALLOCATION_TAB)
                && getCpuSharesAmount().getIsValid()
                && getCpuPinningPolicy().getIsValid()
                && getCpuPinning().getIsValid()
                && diskAliasesValid);

        setValidTab(TabName.BOOT_OPTIONS_TAB, isValidTab(TabName.BOOT_OPTIONS_TAB) &&
                getCdImage().getIsValid() && getKernel_path().getIsValid());

        boolean vmInitIsValid = getVmInitModel().validate();
        setValidTab(TabName.INITIAL_RUN_TAB, isValidTab(TabName.INITIAL_RUN_TAB) && vmInitIsValid);

        getIcon().validateEntity(new IValidation[] { new IconWithOsDefaultValidation() });
        setValidTab(TabName.ICON_TAB, isValidTab(TabName.ICON_TAB) && getIcon().getIsValid());

        boolean hwPartValid = validateHwPart();

        boolean isValid = hwPartValid && vmInitIsValid && allTabsValid();
        getValid().setEntity(isValid);
        fireValidationCompleteEvent();
        return isValid;
    }

    /**
     * It validates that each selected disk has its storage domain accessible from frontend. I may happen that there is
     * a missing permission on storage domain that causes diskModel.getStorageDomain().getSelectedItem() to be null and
     * consequently a frontend NPE.
     */
    private IValidation createEachDiskAHasStorageDomainValidation() {
        return value -> {
            if (getDisksAllocationModel() == null
                || getDisksAllocationModel().getDisks() == null) {
                return ValidationResult.ok();
            }
            for (DiskModel diskModel : getDisksAllocationModel().getDisks()) {
                final StorageDomain storageDomain = diskModel.getStorageDomain().getSelectedItem();
                if (storageDomain == null) {
                    final String diskName = diskModel.getDisk().getDiskAlias();
                    final String errorMessage = ConstantsManager.getInstance().getMessages()
                            .storageDomainOfDiskCannotBeAccessed(diskName);
                    return ValidationResult.fail(errorMessage);
                }
            }
            return ValidationResult.ok();
        };
    }

    public void fireValidationCompleteEvent() {
        ValidationCompleteEvent.fire(getEventBus(), this);
    }

    private boolean validateNaming() {
        getName().validateEntity(
                new IValidation[] {
                        new NotEmptyValidation(),
                        new LengthValidation(getBehavior().getMaxNameLength()),
                        getBehavior().getNameAllowedCharactersIValidation()
                });

        getDescription().validateEntity(
                new IValidation[] {
                        new LengthValidation(DESCRIPTION_MAX_LIMIT),
                        new SpecialAsciiI18NOrNoneValidation()
                });

        final boolean isValid = getName().getIsValid() && getDescription().getIsValid();
        setValidTab(TabName.GENERAL_TAB, isValidTab(TabName.GENERAL_TAB) && isValid);
        return isValid;
    }

    public boolean validateInstanceTypePart() {
        resetTabsValidity();

        final boolean isNamingValid = validateNaming();
        boolean isValid = validateHwPart() && isNamingValid;

        getValid().setEntity(isValid);
        ValidationCompleteEvent.fire(getEventBus(), this);
        return isValid;
    }

    public boolean validateHwPart() {
        getTotalCPUCores().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new IntegerValidation(1, behavior.maxCpus),
                new TotalCpuCoresComposableValidation() });

        if (getIsAutoAssign().getEntity() != null && !getIsAutoAssign().getEntity()) {
            getDefaultHost().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        } else {
            getDefaultHost().setIsValid(true);
        }

        if (getCpuSharesAmount().getIsAvailable()) {
            getCpuSharesAmount().validateEntity(new IValidation[] {new NotEmptyValidation()
                    , new IntegerValidation(0, 262144)});
        }

        if (getNumOfVirtioScsiMultiQueues().getIsAvailable()) {
            if (getVirtioScsiMultiQueueTypeSelection().getSelectedItem() == VirtioMultiQueueType.CUSTOM) {
                getNumOfVirtioScsiMultiQueues().validateEntity(
                        new IValidation[] { new NotEmptyValidation(), new IntegerValidation(1, 262144) });
            } else {
                getNumOfVirtioScsiMultiQueues().setIsValid(true);
            }
        }

        setValidTab(TabName.CUSTOM_PROPERTIES_TAB, isValidTab(TabName.CUSTOM_PROPERTIES_TAB) &&
                getCustomPropertySheet().validate());

        if (getSerialNumberPolicy().getSelectedItem() == SerialNumberPolicy.CUSTOM) {
            getCustomSerialNumber().validateEntity(new IValidation[] { new NotEmptyValidation() });
        } else {
            getCustomSerialNumber().setIsValid(true);
        }

        getEmulatedMachine().validateSelectedItem(new IValidation[] { new I18NExtraNameOrNoneValidation(), new LengthValidation(
                BusinessEntitiesDefinitions.VM_EMULATED_MACHINE_SIZE)});
        getCustomCpu().validateSelectedItem(new IValidation[] { new CpuNameValidation(), new LengthValidation(BusinessEntitiesDefinitions.VM_CPU_NAME_SIZE)});

        if (getConsoleDisconnectAction().getSelectedItem() == ConsoleDisconnectAction.SHUTDOWN) {
            getConsoleDisconnectActionDelay().validateEntity(new IValidation[]{new IntegerValidation(0, Integer.MAX_VALUE)});
        } else {
            getConsoleDisconnectActionDelay().setIsValid(true);
        }

        setValidTab(TabName.CONSOLE_TAB, isValidTab(TabName.CONSOLE_TAB) &&
                getIsUsbEnabled().getIsValid() && getNumOfMonitors().getIsValid() && getSpiceProxy().getIsValid() && getConsoleDisconnectActionDelay().getIsValid());

        setValidTab(TabName.HOST_TAB, isValidTab(TabName.HOST_TAB) && getMigrationDowntime().getIsValid());

        getRngBytes().validateEntity(new IValidation[]{new IntegerValidation(0, Integer.MAX_VALUE), new RngDevValidation()});
        getRngPeriod().validateEntity(new IValidation[]{new IntegerValidation(0, Integer.MAX_VALUE)});

        setValidTab(TabName.TAB_RNG, isValidTab(TabName.TAB_RNG) && getRngBytes().getIsValid() && getRngPeriod().getIsValid());

        if (getIoThreadsEnabled().getEntity()) {
            getNumOfIoThreads().validateEntity(new IValidation[] {
                    new NotNullIntegerValidation(1, AsyncDataProvider.getInstance().getMaxIoThreadsPerVm())
            });
        }

        boolean virtioScsiMultiQueuesValid = getNumOfVirtioScsiMultiQueues().getIsValid();

        setValidTab(TabName.RESOURCE_ALLOCATION_TAB, isValidTab(TabName.RESOURCE_ALLOCATION_TAB) &&
                getNumOfIoThreads().getIsValid() && virtioScsiMultiQueuesValid);

        // Minimum 'Physical Memory Guaranteed' is 1MB
        validateMemorySize(getMemSize(), Integer.MAX_VALUE, 1);
        if (getMemSize().getIsValid()) {
            validateMemorySize(getMinAllocatedMemory(), getMemSize().getEntity(), 1);
        }
        validateMaxMemorySize();
        validateMemoryAlignment(getMemSize());

        setValidTab(TabName.SYSTEM_TAB, isValidTab(TabName.SYSTEM_TAB) &&
                getMemSize().getIsValid() &&
                getMaxMemorySize().getIsValid() &&
                getMinAllocatedMemory().getIsValid() &&
                getTotalCPUCores().getIsValid() &&
                getCustomSerialNumber().getIsValid() &&
                getEmulatedMachine().getIsValid() &&
                getCustomCpu().getIsValid());

        /*
         * This should be run at very end of the validation process otherwise general validation can override more
         * strict checks in behaviors
         */
        boolean behaviorValid = behavior.validate();

        boolean isValid = behaviorValid && allTabsValid();
        return isValid;
    }

    private void resetTabsValidity() {
        clearTabsValidity();
        setIsCustomPropertiesTabAvailable(true);
        getValid().setEntity(true);
    }

    private class RngDevValidation implements IValidation {

        @Override
        public ValidationResult validate(Object value) {
            ValidationResult res = new ValidationResult();
            res.setSuccess(!(rngBytes.getEntity() == null && rngPeriod.getEntity() != null));
            res.setReasons(Arrays.asList(ConstantsManager.getInstance().getConstants().rngRateInvalid()));
            return res;
        }
    }

    public SsoMethod extractSelectedSsoMethod() {
        return Boolean.TRUE.equals(getSsoMethodGuestAgent().getEntity())
                ? SsoMethod.GUEST_AGENT
                : SsoMethod.NONE;
    }

    public void selectSsoMethod(SsoMethod ssoMethod) {
        getSsoMethodNone().setEntity(SsoMethod.NONE.equals(ssoMethod));
        getSsoMethodGuestAgent().setEntity(SsoMethod.GUEST_AGENT.equals(ssoMethod));
    }

    public void selectMigrationPolicy(Guid id) {
        if (getMigrationPolicies().getItems() == null) {
            return;
        }

        for (MigrationPolicy policy : getMigrationPolicies().getItems()) {
            if (policy != null && Objects.equals(policy.getId(), id)) {
                getMigrationPolicies().setSelectedItem(policy);
                break;
            }
        }
    }

    class TotalCpuCoresComposableValidation implements IValidation {

        @Override
        public ValidationResult validate(Object value) {
            boolean isOk = behavior.isNumOfSocketsCorrect(Integer.parseInt(getTotalCPUCores().getEntity()));
            ValidationResult res = new ValidationResult();
            res.setSuccess(isOk);
            res.setReasons(Arrays.asList(ConstantsManager.getInstance()
                    .getMessages()
                    .incorrectVCPUNumber()));
            return res;

        }

    }

    private void validateMemorySize(EntityModel<Integer> model, int maxMemSize, int minMemSize) {
        boolean isValid = false;

        int memSize = model.getEntity();

        if (memSize == 0) {
            model.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .memSizeBetween(minMemSize, maxMemSize));
        } else if (memSize > maxMemSize) {
            model.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .maxMemSizeIs(maxMemSize));
        } else if (memSize < minMemSize) {
            model.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .minMemSizeIs(minMemSize));
        } else {
            isValid = true;
        }

        model.setIsValid(isValid);
    }

    private void validateMaxMemorySize() {
        getMaxMemorySize().setIsValid(true);

        final int maxMemSize = getMaxMemorySize().getEntity();
        if (maxMemSize < getMemSize().getEntity()) {
            getMaxMemorySize().getInvalidityReasons().add(
                    ConstantsManager.getInstance().getConstants().maxMemoryHasToBeLargerThanMemorySize());
            getMaxMemorySize().setIsValid(false);
            return;
        }

        final int maxMaxMemorySize = AsyncDataProvider.getInstance()
                .getMaxMaxMemorySize(getOSType().getSelectedItem(), getCompatibilityVersion());
        if (maxMemSize > maxMaxMemorySize) {
            final String errorMessage = getOSType().getSelectedItem() != null
                    ? ConstantsManager.getInstance().getMessages().maxMaxMemoryForSelectedOsIs(maxMaxMemorySize)
                    : ConstantsManager.getInstance().getMessages().maxMaxMemoryIs(maxMaxMemorySize);
            getMaxMemorySize().getInvalidityReasons().add(errorMessage);
            getMaxMemorySize().setIsValid(false);
            return;
        }
    }

    private void validateMemoryAlignment(EntityModel<Integer> model) {
        if (!model.getIsValid()) {
            return;
        }

        int memSize = model.getEntity();

        DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return;
        }
        Cluster cluster = dataCenterWithCluster.getCluster();
        ArchitectureType architectureFamily = cluster.getArchitecture().getFamily();

        if (architectureFamily == ArchitectureType.ppc && memSize % 256 != 0) {
            model.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .memSizeMultipleOf(architectureFamily.toString(), 256));
            model.setIsValid(false);
        }
    }

    private NotChangableForVmInPoolListModel<EntityModel<VmPoolType>> poolType;

    public ListModel<EntityModel<VmPoolType>> getPoolType() {
        return poolType;
    }

    protected void setPoolType(NotChangableForVmInPoolListModel<EntityModel<VmPoolType>> value) {
        poolType = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> poolStateful;

    public EntityModel<Boolean> getPoolStateful() {
        return poolStateful;
    }

    protected void setPoolStateful(NotChangableForVmInPoolEntityModel<Boolean> poolStateful) {
        this.poolStateful = poolStateful;
    }

    private NotChangableForVmInPoolEntityModel<Integer> numOfDesktops;

    public EntityModel<Integer> getNumOfDesktops() {
        return numOfDesktops;
    }

    protected void setNumOfDesktops(NotChangableForVmInPoolEntityModel<Integer> value) {
        numOfDesktops = value;
    }

    private NotChangableForVmInPoolEntityModel<Integer> assignedVms;

    public EntityModel<Integer> getAssignedVms() {
        return assignedVms;
    }

    public void setAssignedVms(NotChangableForVmInPoolEntityModel<Integer> value) {
        assignedVms = value;
    }

    private NotChangableForVmInPoolEntityModel<Integer> prestartedVms;

    public EntityModel<Integer> getPrestartedVms() {
        return prestartedVms;
    }

    protected void setPrestartedVms(NotChangableForVmInPoolEntityModel<Integer> value) {
        prestartedVms = value;
    }

    private String prestartedVmsHint;

    public String getPrestartedVmsHint() {
        return prestartedVmsHint;
    }

    public void setPrestartedVmsHint(String value) {
        if (!Objects.equals(prestartedVmsHint, value)) {
            prestartedVmsHint = value;
            onPropertyChanged(new PropertyChangedEventArgs("PrestartedVmsHint")); //$NON-NLS-1$
        }
    }

    private NotChangableForVmInPoolEntityModel<Integer> maxAssignedVmsPerUser;

    public EntityModel<Integer> getMaxAssignedVmsPerUser() {
        return maxAssignedVmsPerUser;
    }

    public void setMaxAssignedVmsPerUser(NotChangableForVmInPoolEntityModel<Integer> maxAssignedVmsPerUser) {
        this.maxAssignedVmsPerUser = maxAssignedVmsPerUser;
    }

    private class NotChangableForVmInPoolListModel<T> extends ListModel<T> {
        @Override
        public ListModel<T> setIsChangeable(boolean value) {
            if (!isVmAttachedToPool()) {
                super.setIsChangeable(value);
            }
            return this;
        }

        @Override
        public Model setIsChangeable(boolean value, String reason) {
            if (!isVmAttachedToPool()) {
                super.setIsChangeable(value, reason);
            }
            return this;
        }
    }

    public class ListModelWithClusterDefault<T> extends NotChangableForVmInPoolListModel<T> {

        public static final String CLUSTER_VALUE_EVENT = "ClusterValue";//$NON-NLS-1$

        // item that should be added to the items list to represent the cluster value,
        // it is usually null but e.g. for BiosType it is a special enum value
        private T clusterValuePlaceholder;

        private T clusterValue;

        public ListModelWithClusterDefault() {
            this(null);
        }

        public ListModelWithClusterDefault(T clusterValuePlaceholder) {
            this.clusterValuePlaceholder = clusterValuePlaceholder;
        }

        @Override
        public void setItems(Collection<T> value) {
            this.setItems(value, null);
        }

        @Override
        public void setItems(Collection<T> value, T selectedItem) {
            List<T> items = value == null ? new ArrayList<>() : new ArrayList<>(value);
            if (!containsPlaceholder(items)) {
                items.add(0, clusterValuePlaceholder);
            }

            super.setItems(items, selectedItem);
        }

        public void setClusterValue(T clusterValue) {
            this.clusterValue = clusterValue;
            onPropertyChanged(new PropertyChangedEventArgs(CLUSTER_VALUE_EVENT));
        }

        public T getClusterValue() {
            return clusterValue;
        }

        private boolean containsPlaceholder(List<T> items) {
            if (items.isEmpty()) {
                return false;
            }

            return Objects.equals(items.get(0), clusterValuePlaceholder);
        }
    }

    private class NotChangableForVmInPoolSortedListModel<T> extends SortedListModel<T> {
        public NotChangableForVmInPoolSortedListModel(Comparator<? super T> comparator) {
            super(comparator);
        }

        @Override
        public ListModel<T> setIsChangeable(boolean value) {
            if (!isVmAttachedToPool()) {
                super.setIsChangeable(value);
            }
            return this;
        }

        @Override
        public Model setIsChangeable(boolean value, String reason) {
            if (!isVmAttachedToPool()) {
                super.setIsChangeable(value, reason);
            }
            return this;
        }
    }

    private class NotChangableForVmInPoolEntityModel<T> extends EntityModel<T> {
        public NotChangableForVmInPoolEntityModel() {
        }

        public NotChangableForVmInPoolEntityModel(T entity) {
            super(entity);
        }

        @Override
        public EntityModel<T> setIsChangeable(boolean value) {
            if (!isVmAttachedToPool()) {
                super.setIsChangeable(value);
            }
            return this;
        }

        @Override
        public EntityModel<T> setIsChangeable(boolean value, String reason) {
            if (!isVmAttachedToPool()) {
                super.setIsChangeable(value, reason);
            }
            return this;
        }
    }

    private class NotChangableForVmInPoolKeyValueModel extends KeyValueModel {

        @Override
        public KeyValueModel setIsChangeable(boolean value) {
            if (!isVmAttachedToPool()) {
                super.setIsChangeable(value);
            }
            return this;
        }

        @Override
        public KeyValueModel setIsChangeable(boolean value, String reason) {
            if (!isVmAttachedToPool()) {
                super.setIsChangeable(value, reason);
            }
            return this;
        }
    }

    private ListModel<VmWatchdogType> watchdogModel;

    public ListModel<VmWatchdogType> getWatchdogModel() {
        return watchdogModel;
    }

    public void setWatchdogModel(ListModel<VmWatchdogType> watchdogModel) {
        this.watchdogModel = watchdogModel;
    }

    private ListModel<VmWatchdogAction> watchdogAction;

    public ListModel<VmWatchdogAction> getWatchdogAction() {
        return watchdogAction;
    }

    public void setWatchdogAction(ListModel<VmWatchdogAction> watchdogAction) {
        this.watchdogAction = watchdogAction;
    }

    public StoragePool getSelectedDataCenter() {
        DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return null;
        }

        return dataCenterWithCluster.getDataCenter();
    }

    public Cluster getSelectedCluster() {
        DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return null;
        }

        return dataCenterWithCluster.getCluster();
    }

    public void disableEditing(String msg) {
        getDefaultCommand().setIsExecutionAllowed(false);
        getEditingEnabled().setMessage(msg);
        getEditingEnabled().setEntity(false);
    }

    public static enum CpuSharesAmount {
        DISABLED(0), LOW(512), MEDIUM(1024), HIGH(2048), CUSTOM(-1);

        private int value;
        private CpuSharesAmount(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public Integer getSelectedMigrationDowntime() {
        return getMigrationDowntime().getSelectedItem();
    }

    public Guid getSelectedMigrationPolicy() {
        if (getMigrationPolicies().getSelectedItem() == null) {
            return null;
        }
        return getMigrationPolicies().getSelectedItem().getId();
    }

    public void setSelectedMigrationDowntime(Integer value) {
        getMigrationDowntime().setSelectedItem(value);
    }

    public boolean isCreateInstanceOnly() {
        return ((CurrentUserRole) TypeResolver.getInstance().resolve(CurrentUserRole.class)).isCreateInstanceOnly();
    }

    private void numaSupport() {
        setNumaChanged(true);
        getBehavior().numaSupport();
    }

    public EntityModel<Boolean> getNumaEnabled() {
        return numaEnabled;
    }

    public void setNumaEnabled(EntityModel<Boolean> numaEnabled) {
        this.numaEnabled = numaEnabled;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (NumaSupportModel.SUBMIT_NUMA_SUPPORT.equals(command.getName())) {
            onNumaSupport();
        }
    }

    private void onNumaSupport() {
        if (getWindow() == null) {
            return;
        }
        VmNumaSupportModel model = (VmNumaSupportModel) getWindow();
        setVmNumaNodes(model.getNumaNodes(model.getVm().getId()));
        model.getVm().setvNumaNodeList(getVmNumaNodes());

        behavior.useNumaPinningChanged(getVmNumaNodes());
    }

    public void setNumaChanged(boolean numaChanged) {
        this.numaChanged = numaChanged;
    }

    public boolean isNumaChanged() {
        return numaChanged || initialsNumaNodeCount != getNumaNodeCount().getEntity();
    }

    public void updateNodeCount(int size) {
        initialsNumaNodeCount = size;
        getNumaNodeCount().setEntity(size);
    }

    private NotChangableForVmInPoolListModel<Provider<OpenstackNetworkProviderProperties>> providers;

    public ListModel<Provider<OpenstackNetworkProviderProperties>> getProviders() {
        return providers;
    }

    protected void setProviders(NotChangableForVmInPoolListModel<Provider<OpenstackNetworkProviderProperties>> value) {
        providers = value;
    }

    public Version getCompatibilityVersion() {
        ListModel<Version> customCompatibilityVersion = getCustomCompatibilityVersion();
        if (customCompatibilityVersion != null && customCompatibilityVersion.getSelectedItem() != null) {
            return customCompatibilityVersion.getSelectedItem();
        }

        Cluster cluster = getSelectedCluster();
        if (cluster != null) {
            return cluster.getCompatibilityVersion();
        }

        return null;
    }

    private void refreshMigrationPolicies() {
        Version version = getCompatibilityVersion();
        Cluster selectedCluster = getSelectedCluster();

        if (version == null || selectedCluster == null) {
            return;
        }

        Guid selectedPolicyId = null;
        if (getMigrationPolicies() != null && getMigrationPolicies().getSelectedItem() != null) {
            selectedPolicyId = getMigrationPolicies().getSelectedItem().getId();
        }

        List<MigrationPolicy> policies = AsyncDataProvider.getInstance().getMigrationPolicies(version);

        Guid clusterMigrationPolicyID = selectedCluster.getMigrationPolicyId();
        for (MigrationPolicy policy : policies) {
            if (policy != null && policy.getId().equals(clusterMigrationPolicyID)) {
                migrationPolicies.setClusterValue(policy);
                break;
            }
        }

        getMigrationPolicies().setItems(policies);

        if (selectedPolicyId != null) {
            for (MigrationPolicy policy : policies) {
                if (policy != null && Objects.equals(policy.getId(), selectedPolicyId)) {
                    getMigrationPolicies().setSelectedItem(policy);
                    break;
                }
            }
        }
    }

    private void updateMigrationRelatedFields() {
        Cluster cluster = getSelectedCluster();

        boolean hasMigrationPolicy = true;

        if (getMigrationPolicies().getSelectedItem() == null) {
            if (cluster == null) {
                // for non-cluster entities (e.g. blank template, instance types)
                hasMigrationPolicy = false;
            } else if (cluster.getMigrationPolicyId() == null
                    || cluster.getMigrationPolicyId().equals(NoMigrationPolicy.ID)) {
                // explicitly selected the empty
                hasMigrationPolicy = false;
            }
        } else if (NoMigrationPolicy.ID.equals(getMigrationPolicies().getSelectedItem().getId())){
            hasMigrationPolicy = false;
        }

        Version version = getCompatibilityVersion();
        if (version == null || version.greaterOrEquals(Version.v4_4)) {
            getMigrationDowntime().setIsAvailable(false);
            getAutoConverge().setIsAvailable(false);
            getMigrateCompressed().setIsAvailable(false);
        } else {
            getMigrationDowntime().setIsAvailable(true);
            getAutoConverge().setIsAvailable(true);
            getMigrateCompressed().setIsAvailable(true);

            getMigrationDowntime().setIsChangeable(!hasMigrationPolicy, constants.availableOnlyWithLegacyPolicy());
            getAutoConverge().setIsChangeable(!hasMigrationPolicy, constants.availableOnlyWithLegacyPolicy());
            getMigrateCompressed().setIsChangeable(!hasMigrationPolicy, constants.availableOnlyWithLegacyPolicy());
        }
    }

    private void updateClusterMigrationRelatedFields() {
        Cluster cluster = getSelectedCluster();

        Boolean useAutoConverge = cluster == null ? null : cluster.getAutoConverge();
        if (useAutoConverge == null) {
            useAutoConverge = AsyncDataProvider.getInstance().getAutoConverge();
        }
        autoConverge.setClusterValue(useAutoConverge);

        Boolean compressed = cluster == null ? null : cluster.getMigrateCompressed();
        if (compressed == null) {
            compressed = AsyncDataProvider.getInstance().getMigrateCompressed();
        }
        migrateCompressed.setClusterValue(compressed);

        Boolean encrypt = cluster == null ? null : cluster.getMigrateEncrypted();
        if (encrypt == null) {
            encrypt = AsyncDataProvider.getInstance().getMigrateEncrypted();
        }
        migrateEncrypted.setClusterValue(encrypt);
    }

    private void updateMigrateEncrypted() {
        Version version = getCompatibilityVersion();
        if (version == null || version.greaterOrEquals(Version.v4_4)) {
            getMigrateEncrypted().setIsChangeable(true);
        } else {
            getMigrateEncrypted().setIsChangeable(false, messages.availableInVersionOrHigher(Version.v4_4.toString()));
            getMigrateEncrypted().setSelectedItem(null);
        }
    }

    private void updateSerialNumberPolicy() {
        Cluster cluster = getSelectedCluster();

        SerialNumberPolicy policy = cluster == null ? null : cluster.getSerialNumberPolicy();

        if (policy == null) {
            policy = AsyncDataProvider.getInstance().getSerialNumberPolicy();
        }

        serialNumberPolicy.setClusterValue(policy);
        updateCustomSerialNumber();
    }

    private void updateCustomSerialNumber() {
        if (SerialNumberPolicy.CUSTOM.equals(serialNumberPolicy.getSelectedItem())) {
            getCustomSerialNumber().setIsChangeable(true);
        } else if (serialNumberPolicy.getSelectedItem() == null
                && SerialNumberPolicy.CUSTOM.equals(serialNumberPolicy.getClusterValue())) {
            Cluster cluster = getSelectedCluster();
            String customNumber = cluster == null ? null : cluster.getCustomSerialNumber();

            if (customNumber == null) {
                customNumber = AsyncDataProvider.getInstance().getCustomSerialNumber();
            }

            getCustomSerialNumber().setEntity(customNumber);
            getCustomSerialNumber().setIsChangeable(false, constants.clusterDefaultCustomSerialNumberDisabledReason());
        } else {
            getCustomSerialNumber().setEntity(null);
            getCustomSerialNumber().setIsChangeable(false, constants.customSerialNumberDisabledReason());
        }
    }

    public boolean secureBootEnabled() {
        return getBiosType().getSelectedItem() == BiosType.Q35_SECURE_BOOT;
    }

    private void updateTpmEnabled() {
        Cluster cluster = getSelectedCluster();
        Version version = getCompatibilityVersion();
        if (version != null && version.less(Version.v4_5)) {
            getTpmEnabled().setIsChangeable(false, messages.availableInVersionOrHigher(Version.v4_5.toString()));
            getTpmEnabled().setEntity(false);
        } else if (!AsyncDataProvider.getInstance().isTpmAllowedForOs(getOSType().getSelectedItem())) {
            getTpmEnabled().setIsChangeable(false, constants.guestOsVersionNotSupported());
            getTpmEnabled().setEntity(false);
        } else if (AsyncDataProvider.getInstance().isTpmRequiredForOs(getOSType().getSelectedItem())) {
            if (getBiosType().getSelectedItem() == null || !getBiosType().getSelectedItem().isOvmf()) {
                getBiosType().setSelectedItem(BiosType.Q35_OVMF);
            }
            getTpmEnabled().setIsChangeable(false, constants.tpmDeviceRequired());
            getTpmEnabled().setEntity(true);
        } else if (cluster == null || cluster.getArchitecture() == null
                || cluster.getArchitecture().getFamily() == ArchitectureType.x86
                        && (getBiosType().getSelectedItem() == null || !getBiosType().getSelectedItem().isOvmf())) {
            getTpmEnabled().setIsChangeable(false, constants.uefiRequired());
            getTpmEnabled().setEntity(false);
        } else {
            getTpmEnabled().setIsChangeable(true);
        }
    }

    protected void updateCpuPinningPolicy() {
        boolean defaultHostSelected =  Boolean.FALSE.equals(getIsAutoAssign().getEntity()) &&
                getDefaultHost().getSelectedItems() != null &&
                getDefaultHost().getSelectedItems().size() > 0;
        cpuPinningPolicy.setCpuPolicyEnabled(CpuPinningPolicy.MANUAL, defaultHostSelected);
    }

    protected void cpuPinningPolicyChanged() {
        updateCpuFields();
        behavior.updateCpuPinningVisibility();
    }

    protected void updateCpuFields() {
        if (getCpuPinningPolicy().getSelectedItem().getPolicy() == CpuPinningPolicy.RESIZE_AND_PIN_NUMA) {
            disableCpuFields();
        } else {
            enableCpuFields();
        }
    }

    protected void cpuPinningChanged() {
        // Set migration mode
        if (behavior.isVmHpOrPinningConfigurationEnabled()) {
            getMigrationMode().setSelectedItem(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
        }
    }
}
