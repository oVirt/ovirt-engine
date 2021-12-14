package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.FipsMode;
import org.ovirt.engine.core.common.businessentities.LogMaxMemoryUsedThresholdType;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.network.FirewallType;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.CpuUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.FilteredListModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.HasValidatedTabs;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ModelWithMigrationsOptions;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.ValidationCompleteEvent;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacPoolModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostWithProtocolAndPortAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotNullIntegerValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class ClusterModel extends EntityModel<Cluster> implements HasValidatedTabs, ModelWithMigrationsOptions {
    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();
    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private Map<Guid, PolicyUnit> policyUnitMap;
    private ListModel<ClusterPolicy> clusterPolicy;
    private Map<Guid, Network> defaultManagementNetworkCache = new HashMap<>();
    private Boolean detached;

    private ListModel<MigrationPolicy> migrationPolicies;

    public ListModel<MigrationPolicy> getMigrationPolicies() {
        return migrationPolicies;
    }

    public void setMigrationPolicies(ListModel<MigrationPolicy> migrationPolicies) {
        this.migrationPolicies = migrationPolicies;
    }

    private ListModel<String> glusterTunedProfile;

    public ListModel<String> getGlusterTunedProfile() {
        return glusterTunedProfile;
    }

    public void setGlusterTunedProfile(ListModel<String> glusterTunedProfile) {
        this.glusterTunedProfile = glusterTunedProfile;
    }

    public ListModel<ClusterPolicy> getClusterPolicy() {
        return clusterPolicy;
    }

    public void setClusterPolicy(ListModel<ClusterPolicy> clusterPolicy) {
        this.clusterPolicy = clusterPolicy;
    }

    private KeyValueModel customPropertySheet;

    public KeyValueModel getCustomPropertySheet() {
        return customPropertySheet;
    }

    public void setCustomPropertySheet(KeyValueModel customPropertySheet) {
        this.customPropertySheet = customPropertySheet;
    }

    private int privateServerOverCommit;

    public int getServerOverCommit() {
        return privateServerOverCommit;
    }

    public void setServerOverCommit(int value) {
        privateServerOverCommit = value;
    }

    private int privateDesktopOverCommit;

    public int getDesktopOverCommit() {
        return privateDesktopOverCommit;
    }

    public void setDesktopOverCommit(int value) {
        privateDesktopOverCommit = value;
    }

    private int privateDefaultMemoryOvercommit;

    public int getDefaultMemoryOvercommit() {
        return privateDefaultMemoryOvercommit;
    }

    public void setDefaultMemoryOvercommit(int value) {
        privateDefaultMemoryOvercommit = value;
    }

    private boolean privateIsEdit;

    public boolean getIsEdit() {
        return privateIsEdit;
    }

    public void setIsEdit(boolean value) {
        privateIsEdit = value;
    }

    private boolean isCPUinitialized = false;

    private boolean privateIsNew;

    public boolean getIsNew() {
        return privateIsNew;
    }

    public void setIsNew(boolean value) {
        privateIsNew = value;
    }

    private String privateOriginalName;

    public String getOriginalName() {
        return privateOriginalName;
    }

    public void setOriginalName(String value) {
        privateOriginalName = value;
    }

    private Guid privateClusterId;

    public Guid getClusterId() {
        return privateClusterId;
    }

    public void setClusterId(Guid value) {
        privateClusterId = value;
    }

    private EntityModel<String> privateName;

    public EntityModel<String> getName() {
        return privateName;
    }

    public void setName(EntityModel<String> value) {
        privateName = value;
    }

    private EntityModel<String> privateDescription;

    public EntityModel<String> getDescription() {
        return privateDescription;
    }

    public void setDescription(EntityModel<String> value) {
        privateDescription = value;
    }

    private EntityModel<Integer> logMaxMemoryUsedThreshold;

    public EntityModel<Integer> getLogMaxMemoryUsedThreshold() {
        return logMaxMemoryUsedThreshold;
    }

    public void setLogMaxMemoryUsedThreshold(EntityModel<Integer> value) {
        logMaxMemoryUsedThreshold = value;
    }

    private ListModel<LogMaxMemoryUsedThresholdType> logMaxMemoryUsedThresholdType;

    public ListModel<LogMaxMemoryUsedThresholdType> getLogMaxMemoryUsedThresholdType() {
        return logMaxMemoryUsedThresholdType;
    }

    public void setLogMaxMemoryUsedThresholdType(ListModel<LogMaxMemoryUsedThresholdType> defaultNetworkProvider) {
        this.logMaxMemoryUsedThresholdType = defaultNetworkProvider;
    }

    private EntityModel<String> privateComment;

    public EntityModel<String> getComment() {
        return privateComment;
    }

    public void setComment(EntityModel<String> value) {
        privateComment = value;
    }

    private ListModel<StoragePool> privateDataCenter;

    public ListModel<StoragePool> getDataCenter() {
        return privateDataCenter;
    }

    public void setDataCenter(ListModel<StoragePool> value) {
        privateDataCenter = value;
    }

    private ListModel<Network> managementNetwork;

    public void setManagementNetwork(ListModel<Network> managementNetwork) {
        this.managementNetwork = managementNetwork;
    }

    public ListModel<Network> getManagementNetwork() {
        return managementNetwork;
    }

    private FilteredListModel<ServerCpu> privateCPU;

    public FilteredListModel<ServerCpu> getCPU() {
        return privateCPU;
    }

    public void setCPU(FilteredListModel<ServerCpu> value) {
        privateCPU = value;
    }

    private EntityModel<Boolean> rngHwrngSourceRequired;

    public EntityModel<Boolean> getRngHwrngSourceRequired() {
        return rngHwrngSourceRequired;
    }

    public void setRngHwrngSourceRequired(EntityModel<Boolean> rngHwrngSourceRequired) {
        this.rngHwrngSourceRequired = rngHwrngSourceRequired;
    }

    private ListModel<Version> privateVersion;

    public ListModel<Version> getVersion() {
        return privateVersion;
    }

    public void setVersion(ListModel<Version> value) {
        privateVersion = value;
    }

    private ListModel<SwitchType> switchType;

    public ListModel<SwitchType> getSwitchType() {
        return switchType;
    }

    public void setSwitchType(ListModel<SwitchType> switchType) {
        this.switchType = switchType;
    }

    private ListModel<ArchitectureType> privateArchitecture;

    public ListModel<ArchitectureType> getArchitecture() {
        return privateArchitecture;
    }

    public void setArchitecture(ListModel<ArchitectureType> value) {
        privateArchitecture = value;
    }

    private boolean allowClusterWithVirtGlusterEnabled;

    public boolean getAllowClusterWithVirtGlusterEnabled() {
        return allowClusterWithVirtGlusterEnabled;
    }

    public void setAllowClusterWithVirtGlusterEnabled(boolean value) {
        allowClusterWithVirtGlusterEnabled = value;
    }

    private EntityModel<Boolean> privateEnableOvirtService;

    public EntityModel<Boolean> getEnableOvirtService() {
        return privateEnableOvirtService;
    }

    public void setEnableOvirtService(EntityModel<Boolean> value) {
        this.privateEnableOvirtService = value;
    }

    private EntityModel<Boolean> privateEnableGlusterService;

    public EntityModel<Boolean> getEnableGlusterService() {
        return privateEnableGlusterService;
    }

    public void setEnableGlusterService(EntityModel<Boolean> value) {
        this.privateEnableGlusterService = value;
    }

    private ListModel<List<AdditionalFeature>> additionalClusterFeatures;

    public ListModel<List<AdditionalFeature>> getAdditionalClusterFeatures() {
        return additionalClusterFeatures;
    }

    public void setAdditionalClusterFeatures(ListModel<List<AdditionalFeature>> additionalClusterFeatures) {
        this.additionalClusterFeatures = additionalClusterFeatures;
    }

    private EntityModel<Boolean> isImportGlusterConfiguration;

    public EntityModel<Boolean> getIsImportGlusterConfiguration() {
        return isImportGlusterConfiguration;
    }

    public void setIsImportGlusterConfiguration(EntityModel<Boolean> value) {
        this.isImportGlusterConfiguration = value;
    }

    private EntityModel<String> glusterHostAddress;

    public EntityModel<String> getGlusterHostAddress() {
        return glusterHostAddress;
    }

    public void setGlusterHostAddress(EntityModel<String> glusterHostAddress) {
        this.glusterHostAddress = glusterHostAddress;
    }
    private EntityModel<String> glusterHostSshPublicKey;

    public EntityModel<String> getGlusterHostSshPublicKey(){
        return glusterHostSshPublicKey;
    }
    public void setGlusterHostSshPublicKey(EntityModel<String> glusterHostSshPublicKey){
        this.glusterHostSshPublicKey = glusterHostSshPublicKey;
    }

    private Boolean hostSshPublicKeyVerified;

    public Boolean isHostSshPublicKeyVerified() {
        return hostSshPublicKeyVerified;
    }

    public void setHostSshPublicKeyVerified(Boolean value) {
        this.hostSshPublicKeyVerified = value;
    }

    private EntityModel<String> glusterHostPassword;

    public EntityModel<String> getGlusterHostPassword() {
        return glusterHostPassword;
    }

    public void setGlusterHostPassword(EntityModel<String> glusterHostPassword) {
        this.glusterHostPassword = glusterHostPassword;
    }

    private EntityModel<Integer> privateOptimizationNone;

    public EntityModel<Integer> getOptimizationNone() {
        return privateOptimizationNone;
    }

    public void setOptimizationNone(EntityModel<Integer> value) {
        privateOptimizationNone = value;
    }

    private EntityModel<Integer> privateOptimizationForServer;

    public EntityModel<Integer> getOptimizationForServer() {
        return privateOptimizationForServer;
    }

    public void setOptimizationForServer(EntityModel<Integer> value) {
        privateOptimizationForServer = value;
    }

    private EntityModel<Integer> privateOptimizationForDesktop;

    public EntityModel<Integer> getOptimizationForDesktop() {
        return privateOptimizationForDesktop;
    }

    public void setOptimizationForDesktop(EntityModel<Integer> value) {
        privateOptimizationForDesktop = value;
    }

    private EntityModel<Integer> privateOptimizationCustom;

    public EntityModel<Integer> getOptimizationCustom() {
        return privateOptimizationCustom;
    }

    public void setOptimizationCustom(EntityModel<Integer> value) {
        privateOptimizationCustom = value;
    }

    private EntityModel<Boolean> privateOptimizationNone_IsSelected;

    public EntityModel<Boolean> getOptimizationNone_IsSelected() {
        return privateOptimizationNone_IsSelected;
    }

    public void setOptimizationNone_IsSelected(EntityModel<Boolean> value) {
        privateOptimizationNone_IsSelected = value;
    }

    private EntityModel<Boolean> privateOptimizationForServer_IsSelected;

    public EntityModel<Boolean> getOptimizationForServer_IsSelected() {
        return privateOptimizationForServer_IsSelected;
    }

    public void setOptimizationForServer_IsSelected(EntityModel<Boolean> value) {
        privateOptimizationForServer_IsSelected = value;
    }

    private EntityModel<Boolean> privateOptimizationForDesktop_IsSelected;

    public EntityModel<Boolean> getOptimizationForDesktop_IsSelected() {
        return privateOptimizationForDesktop_IsSelected;
    }

    public void setOptimizationForDesktop_IsSelected(EntityModel<Boolean> value) {
        privateOptimizationForDesktop_IsSelected = value;
    }

    private EntityModel<Boolean> privateOptimizationCustom_IsSelected;

    public EntityModel<Boolean> getOptimizationCustom_IsSelected() {
        return privateOptimizationCustom_IsSelected;
    }

    public void setOptimizationCustom_IsSelected(EntityModel<Boolean> value) {
        privateOptimizationCustom_IsSelected = value;
    }

    private EntityModel<Boolean> privateCountThreadsAsCores;

    public EntityModel<Boolean> getCountThreadsAsCores() {
        return privateCountThreadsAsCores;
    }

    public void setCountThreadsAsCores(EntityModel<Boolean> value) {
        privateCountThreadsAsCores = value;
    }

    private EntityModel<Boolean> privateSmtDisabled;

    public EntityModel<Boolean> getSmtDisabled() {
        return privateSmtDisabled;
    }

    public void setSmtDisabled(EntityModel<Boolean> privateSmtEnabled) {
        this.privateSmtDisabled = privateSmtEnabled;
    }

    /**
     * Mutually exclusive Resilience policy radio button
     * @see #privateMigrateOnErrorOption_YES
     * @see #privateMigrateOnErrorOption_HA_ONLY
     */
    private EntityModel<Boolean> privateMigrateOnErrorOption_NO;

    public EntityModel<Boolean> getMigrateOnErrorOption_NO() {
        return privateMigrateOnErrorOption_NO;
    }

    public void setMigrateOnErrorOption_NO(EntityModel<Boolean> value) {
        privateMigrateOnErrorOption_NO = value;
    }

    /**
     * Mutually exclusive Resilience policy radio button
     * @see #privateMigrateOnErrorOption_NO
     * @see #privateMigrateOnErrorOption_HA_ONLY
     */
    private EntityModel<Boolean> privateMigrateOnErrorOption_YES;

    public EntityModel<Boolean> getMigrateOnErrorOption_YES() {
        return privateMigrateOnErrorOption_YES;
    }

    public void setMigrateOnErrorOption_YES(EntityModel<Boolean> value) {
        privateMigrateOnErrorOption_YES = value;
    }

    /**
     * Mutually exclusive Resilience policy radio button
     * @see #privateMigrateOnErrorOption_YES
     * @see #privateMigrateOnErrorOption_NO
     */
    private EntityModel<Boolean> privateMigrateOnErrorOption_HA_ONLY;

    public EntityModel<Boolean> getMigrateOnErrorOption_HA_ONLY() {
        return privateMigrateOnErrorOption_HA_ONLY;
    }

    public void setMigrateOnErrorOption_HA_ONLY(EntityModel<Boolean> value) {
        privateMigrateOnErrorOption_HA_ONLY = value;
    }

    private EntityModel<Boolean> enableKsm;

    public EntityModel<Boolean> getEnableKsm() {
        return enableKsm;
    }

    public void setEnableKsm(EntityModel<Boolean> enableKsm) {
        this.enableKsm = enableKsm;
    }

    private ListModel<KsmPolicyForNuma> ksmPolicyForNumaSelection;

    public ListModel<KsmPolicyForNuma> getKsmPolicyForNumaSelection() {
        return ksmPolicyForNumaSelection;
    }

    private void setKsmPolicyForNumaSelection(ListModel<KsmPolicyForNuma> value) {
        ksmPolicyForNumaSelection = value;
    }

    private EntityModel<Boolean> enableBallooning;

    public EntityModel<Boolean> getEnableBallooning() {
        return enableBallooning;
    }

    public void setEnableBallooning(EntityModel<Boolean> enableBallooning) {
        this.enableBallooning = enableBallooning;
    }

    private EntityModel<Boolean> optimizeForUtilization;

    public EntityModel<Boolean> getOptimizeForUtilization() {
        return optimizeForUtilization;
    }

    public void setOptimizeForUtilization(EntityModel<Boolean> optimizeForUtilization) {
        this.optimizeForUtilization = optimizeForUtilization;
    }

    private EntityModel<Boolean> optimizeForSpeed;

    public EntityModel<Boolean> getOptimizeForSpeed() {
        return optimizeForSpeed;
    }

    public void setOptimizeForSpeed(EntityModel<Boolean> optimizeForSpeed) {
        this.optimizeForSpeed = optimizeForSpeed;
    }
    private EntityModel<Boolean> guarantyResources;

    public EntityModel<Boolean> getGuarantyResources() {
        return guarantyResources;
    }

    public void setGuarantyResources(EntityModel<Boolean> guarantyResources) {
        this.guarantyResources = guarantyResources;
    }

    private EntityModel<Boolean> allowOverbooking;

    public EntityModel<Boolean> getAllowOverbooking() {
        return allowOverbooking;
    }

    public void setAllowOverbooking(EntityModel<Boolean> allowOverbooking) {
        this.allowOverbooking = allowOverbooking;
    }

    private ListModel<SerialNumberPolicy> serialNumberPolicy;

    public ListModel<SerialNumberPolicy> getSerialNumberPolicy() {
        return serialNumberPolicy;
    }

    public void setSerialNumberPolicy(ListModel<SerialNumberPolicy> serialNumberPolicy) {
        this.serialNumberPolicy = serialNumberPolicy;
    }

    private EntityModel<String> customSerialNumber;

    public EntityModel<String> getCustomSerialNumber() {
        return customSerialNumber;
    }

    public void setCustomSerialNumber(EntityModel<String> customSerialNumberPolicy) {
        this.customSerialNumber = customSerialNumberPolicy;
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

    private EntityModel<Boolean> vncEncryptionEnabled;

    public EntityModel<Boolean> getVncEncryptionEnabled() {
        return vncEncryptionEnabled;
    }

    public void setVncEncryptionEnabled(EntityModel<Boolean> vncEncryptionEnabled) {
        this.vncEncryptionEnabled = vncEncryptionEnabled;
    }

    private MigrateOnErrorOptions migrateOnErrorOption = MigrateOnErrorOptions.values()[0];

    public MigrateOnErrorOptions getMigrateOnErrorOption() {
        if (getMigrateOnErrorOption_NO().getEntity()) {
            return MigrateOnErrorOptions.NO;
        } else if (getMigrateOnErrorOption_YES().getEntity()) {
            return MigrateOnErrorOptions.YES;
        } else if (getMigrateOnErrorOption_HA_ONLY().getEntity()) {
            return MigrateOnErrorOptions.HA_ONLY;
        }
        return MigrateOnErrorOptions.YES;
    }

    public void setMigrateOnErrorOption(MigrateOnErrorOptions value) {
        if (migrateOnErrorOption != value) {
            migrateOnErrorOption = value;

            // webadmin use.
            switch (migrateOnErrorOption) {
            case NO:
                getMigrateOnErrorOption_NO().setEntity(true);
                getMigrateOnErrorOption_YES().setEntity(false);
                getMigrateOnErrorOption_HA_ONLY().setEntity(false);
                break;
            case YES:
                getMigrateOnErrorOption_NO().setEntity(false);
                getMigrateOnErrorOption_YES().setEntity(true);
                getMigrateOnErrorOption_HA_ONLY().setEntity(false);
                break;
            case HA_ONLY:
                getMigrateOnErrorOption_NO().setEntity(false);
                getMigrateOnErrorOption_YES().setEntity(false);
                getMigrateOnErrorOption_HA_ONLY().setEntity(true);
                break;
            default:
                break;
            }
            onPropertyChanged(new PropertyChangedEventArgs("MigrateOnErrorOption")); //$NON-NLS-1$
        }
    }

    private boolean migrationTabAvailable;

    public boolean isMigrationTabAvailable() {
        return migrationTabAvailable;
    }

    public void setMigrationTabAvailable(boolean value) {
        if (isMigrationTabAvailable() != value) {
            migrationTabAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("isMigrationTabAvailable")); //$NON-NLS-1$
        }
    }

    private ListModel<FirewallType> firewallType;

    public ListModel<FirewallType> getFirewallType() {
        return firewallType;
    }

    public void setFirewallType(ListModel<FirewallType> firewallType) {
        this.firewallType = firewallType;
    }

    private ListModel<Provider> defaultNetworkProvider;

    public ListModel<Provider> getDefaultNetworkProvider() {
        return defaultNetworkProvider;
    }

    public void setDefaultNetworkProvider(ListModel<Provider> defaultNetworkProvider) {
        this.defaultNetworkProvider = defaultNetworkProvider;
    }

    private EntityModel<Boolean> privateEnableTrustedService;

    private EntityModel<Boolean> privateEnableHaReservation;

    public EntityModel<Boolean> getEnableHaReservation() {
        return privateEnableHaReservation;
    }

    public void setEnableHaReservation(EntityModel<Boolean> value) {
        this.privateEnableHaReservation = value;
    }

    public EntityModel<Boolean> getEnableTrustedService() {
        return privateEnableTrustedService;
    }

    public void setEnableTrustedService(EntityModel<Boolean> value) {
        this.privateEnableTrustedService = value;
    }

    public int getMemoryOverCommit() {
        if (getOptimizationNone_IsSelected().getEntity()) {
            return getOptimizationNone().getEntity();
        }

        if (getOptimizationForServer_IsSelected().getEntity()) {
            return getOptimizationForServer().getEntity();
        }

        if (getOptimizationForDesktop_IsSelected().getEntity()) {
            return getOptimizationForDesktop().getEntity();
        }

        if (getOptimizationCustom_IsSelected().getEntity()) {
            return getOptimizationCustom().getEntity();
        }

        return AsyncDataProvider.getInstance().getClusterDefaultMemoryOverCommit();
    }

    public String getSchedulerOptimizationInfoMessage() {
        return ConstantsManager.getInstance()
                .getMessages()
                .schedulerOptimizationInfo(AsyncDataProvider.getInstance().getOptimizeSchedulerForSpeedPendingRequests());
    }

    public String getAllowOverbookingInfoMessage() {
        return ConstantsManager.getInstance()
                .getMessages()
                .schedulerAllowOverbookingInfo(AsyncDataProvider.getInstance().getSchedulerAllowOverbookingPendingRequestsThreshold());
    }

    public void setMemoryOverCommit(int value) {
        getOptimizationNone_IsSelected().setEntity(value == getOptimizationNone().getEntity());
        getOptimizationForServer_IsSelected().setEntity(value == getOptimizationForServer().getEntity());
        getOptimizationForDesktop_IsSelected().setEntity(value == getOptimizationForDesktop().getEntity());

        if (!getOptimizationNone_IsSelected().getEntity()
                && !getOptimizationForServer_IsSelected().getEntity()
                && !getOptimizationForDesktop_IsSelected().getEntity()) {
            getOptimizationCustom().setIsAvailable(true);
            getOptimizationCustom().setEntity(value);
            getOptimizationCustom_IsSelected().setIsAvailable(true);
            getOptimizationCustom_IsSelected().setEntity(true);
        }
    }

    private EntityModel<Boolean> fencingEnabledModel;

    public EntityModel<Boolean> getFencingEnabledModel() {
        return fencingEnabledModel;
    }

    public void setFencingEnabledModel(EntityModel<Boolean> fencingEnabledModel) {
        this.fencingEnabledModel = fencingEnabledModel;
    }

    private EntityModel<Boolean> skipFencingIfSDActiveEnabled;

    public EntityModel<Boolean> getSkipFencingIfSDActiveEnabled() {
        return skipFencingIfSDActiveEnabled;
    }

    public void setSkipFencingIfSDActiveEnabled(EntityModel<Boolean> skipFencingIfSDActiveEnabled) {
        this.skipFencingIfSDActiveEnabled = skipFencingIfSDActiveEnabled;
    }

    private EntityModel<Boolean> skipFencingIfGlusterBricksUp;

    public EntityModel<Boolean> getSkipFencingIfGlusterBricksUp() {
        return skipFencingIfGlusterBricksUp;
    }

    public void setSkipFencingIfGlusterBricksUp(EntityModel<Boolean> skipFencingIfGlusterBricksUp) {
        this.skipFencingIfGlusterBricksUp = skipFencingIfGlusterBricksUp;
    }

    private EntityModel<Boolean> skipFencingIfGlusterQuorumNotMet;

    public EntityModel<Boolean> getSkipFencingIfGlusterQuorumNotMet() {
        return skipFencingIfGlusterQuorumNotMet;
    }

    public void setSkipFencingIfGlusterQuorumNotMet(EntityModel<Boolean> skipFencingIfGlusterQuorumNotMet) {
        this.skipFencingIfGlusterQuorumNotMet = skipFencingIfGlusterQuorumNotMet;
    }

    private EntityModel<Boolean> skipFencingIfConnectivityBrokenEnabled;

    public EntityModel<Boolean> getSkipFencingIfConnectivityBrokenEnabled() {
        return skipFencingIfConnectivityBrokenEnabled;
    }

    public void setSkipFencingIfConnectivityBrokenEnabled(EntityModel<Boolean> skipFencingIfConnectivityBrokenEnabled) {
        this.skipFencingIfConnectivityBrokenEnabled = skipFencingIfConnectivityBrokenEnabled;
    }

    private ListModel<Integer> hostsWithBrokenConnectivityThreshold;

    public ListModel<Integer> getHostsWithBrokenConnectivityThreshold() {
        return hostsWithBrokenConnectivityThreshold;
    }

    public void setHostsWithBrokenConnectivityThreshold(ListModel<Integer> value) {
        hostsWithBrokenConnectivityThreshold = value;
    }

    private ListModel<Boolean> autoConverge;

    public ListModel<Boolean> getAutoConverge() {
        return autoConverge;
    }

    public void setAutoConverge(ListModel<Boolean> autoConverge) {
        this.autoConverge = autoConverge;
    }

    private ListModel<Boolean> migrateCompressed;

    public ListModel<Boolean> getMigrateCompressed() {
        return migrateCompressed;
    }

    public void setMigrateCompressed(ListModel<Boolean> migrateCompressed) {
        this.migrateCompressed = migrateCompressed;
    }

    private ListModel<Boolean> migrateEncrypted;

    public ListModel<Boolean> getMigrateEncrypted() {
        return migrateEncrypted;
    }

    public void setMigrateEncrypted(ListModel<Boolean> migrateCompressed) {
        this.migrateEncrypted = migrateCompressed;
    }

    private EntityModel<Integer> customMigrationNetworkBandwidth;

    public EntityModel<Integer> getCustomMigrationNetworkBandwidth() {
        return customMigrationNetworkBandwidth;
    }

    public void setCustomMigrationNetworkBandwidth(EntityModel<Integer> customMigrationNetworkBandwidth) {
        this.customMigrationNetworkBandwidth = customMigrationNetworkBandwidth;
    }

    public ListModel<MigrationBandwidthLimitType> migrationBandwidthLimitType;

    public ListModel<MigrationBandwidthLimitType> getMigrationBandwidthLimitType() {
        return migrationBandwidthLimitType;
    }

    public void setMigrationBandwidthLimitType(ListModel<MigrationBandwidthLimitType> migrationBandwidthLimitType) {
        this.migrationBandwidthLimitType = migrationBandwidthLimitType;
    }

    private ListModel<MacPool> macPoolListModel;

    public ListModel<MacPool> getMacPoolListModel() {
        return macPoolListModel;
    }

    private void setMacPoolListModel(ListModel<MacPool> macPoolListModel) {
        this.macPoolListModel = macPoolListModel;
    }

    private MacPoolModel macPoolModel;

    public MacPoolModel getMacPoolModel() {
        return macPoolModel;
    }

    private void setMacPoolModel(MacPoolModel macPoolModel) {
        this.macPoolModel = macPoolModel;
    }

    private UICommand addMacPoolCommand;

    public UICommand getAddMacPoolCommand() {
        return addMacPoolCommand;
    }

    public void setAddMacPoolCommand(UICommand addMacPoolCommand) {
        this.addMacPoolCommand = addMacPoolCommand;
    }

    private ListModel<BiosType> biosType;

    public ListModel<BiosType> getBiosType() {
        return biosType;
    }

    public void setBiosType(ListModel<BiosType> biosType) {
        this.biosType = biosType;
    }

    private EntityModel<Boolean> changeToQ35;

    public EntityModel<Boolean> getChangeToQ35() {
        return changeToQ35;
    }

    public void setChangeToQ35(EntityModel<Boolean> changeToQ35) {
        this.changeToQ35 = changeToQ35;
    }

    private ListModel<FipsMode> fipsMode;

    public ListModel<FipsMode> getFipsMode() {
        return fipsMode;
    }

    public void setFipsMode(ListModel<FipsMode> value) {
        fipsMode = value;
    }

    @Override
    public void setEntity(Cluster value) {
        super.setEntity(value);
        initSelectedMacPool();
    }

    public ClusterModel() {
        super();
        ListModel<KsmPolicyForNuma> ksmPolicyForNumaSelection = new ListModel<>();
        ksmPolicyForNumaSelection.setItems(Arrays.asList(KsmPolicyForNuma.values()));
        setKsmPolicyForNumaSelection(ksmPolicyForNumaSelection);
        initMacPools();
    }

    private void initMacPools() {
        setMacPoolListModel(new SortedListModel<>(Linq.SharedMacPoolComparator));
        setMacPoolModel(new MacPoolModel());
        getMacPoolModel().setIsChangeable(false);
        getMacPoolListModel().getItemsChangedEvent().addListener(this);
        getMacPoolListModel().getSelectedItemChangedEvent().addListener(this);
        startProgress();
        Frontend.getInstance().runQuery(QueryType.GetAllMacPools,
                new QueryParametersBase(),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    getMacPoolListModel().setItems(returnValue.getReturnValue());
                    stopProgress();
                }));
    }

    public void initTunedProfiles() {
        this.startProgress();
        if (getVersion() == null || getVersion().getSelectedItem() == null) {
            return;
        }
        Version version = getVersion().getSelectedItem();
        Frontend.getInstance().runQuery(QueryType.GetGlusterTunedProfiles, new IdAndNameQueryParameters(null, version.getValue()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    stopProgress();
                    List<String> glusterTunedProfiles = new ArrayList<>();
                    if (returnValue.getSucceeded()) {
                        glusterTunedProfiles.addAll(returnValue.getReturnValue());
                    }
                    final String oldSelectedProfile = glusterTunedProfile.getSelectedItem();
                    glusterTunedProfile.setItems(glusterTunedProfiles);
                    glusterTunedProfile.setIsAvailable(glusterTunedProfile.getItems().size() > 0);
                    String newSelectedItem = null;
                    if (oldSelectedProfile != null) {
                        newSelectedItem = Linq.firstOrNull(glusterTunedProfiles, x -> x.equals(oldSelectedProfile));
                    }
                    if (newSelectedItem != null) {
                        glusterTunedProfile.setSelectedItem(newSelectedItem);
                    } else if (getIsEdit()) {
                        glusterTunedProfile.setSelectedItem(Linq.firstOrNull(glusterTunedProfiles, x -> x.equals(getEntity().getGlusterTunedProfile())));
                    }
                }));
    }

    public void init(final boolean isEdit) {
        setIsEdit(isEdit);
        setName(new EntityModel<>());
        setDescription(new EntityModel<>());
        setComment(new EntityModel<>());
        setEnableTrustedService(new EntityModel<>(false));
        setEnableHaReservation(new EntityModel<>(false));
        setMigrationPolicies(new ListModel<>());
        getMigrationPolicies().getSelectedItemChangedEvent().addListener(this);
        setAllowClusterWithVirtGlusterEnabled(true);
        setGlusterTunedProfile(new ListModel<>());
        setLogMaxMemoryUsedThreshold(new EntityModel<>());
        setLogMaxMemoryUsedThresholdType(new ListModel<>());
        initLogMaxMemoryUsedThresholdType();
        AsyncDataProvider.getInstance().getAllowClusterWithVirtGlusterEnabled(new AsyncQuery<>(this::setAllowClusterWithVirtGlusterEnabled));

        setEnableOvirtService(new EntityModel<>());
        setEnableGlusterService(new EntityModel<>());
        setAdditionalClusterFeatures(new ListModel<>());
        List<List<AdditionalFeature>> additionalFeatures = new ArrayList<>();
        additionalFeatures.add(Collections.emptyList());
        getAdditionalClusterFeatures().setItems(additionalFeatures, null);
        setSpiceProxyEnabled(new EntityModel<>());
        getSpiceProxyEnabled().setEntity(false);
        getSpiceProxyEnabled().getEntityChangedEvent().addListener(this);

        setVncEncryptionEnabled(new EntityModel<>());
        getVncEncryptionEnabled().setEntity(true);

        setSpiceProxy(new EntityModel<>());
        getSpiceProxy().setIsChangeable(false);

        setFencingEnabledModel(new EntityModel<>());
        getFencingEnabledModel().setEntity(true);
        getFencingEnabledModel().getEntityChangedEvent().addListener((ev, sender, args) -> updateFencingPolicyContent(getVersion() == null ? null : getVersion().getSelectedItem()));

        setSkipFencingIfSDActiveEnabled(new EntityModel<>());
        getSkipFencingIfSDActiveEnabled().setEntity(true);

        setSkipFencingIfGlusterBricksUp(new EntityModel<>());
        getSkipFencingIfGlusterBricksUp().setEntity(false);
        getSkipFencingIfGlusterBricksUp().setIsAvailable(false);
        setSkipFencingIfGlusterQuorumNotMet(new EntityModel<>());
        getSkipFencingIfGlusterQuorumNotMet().setEntity(false);
        getSkipFencingIfGlusterQuorumNotMet().setIsAvailable(false);

        setSkipFencingIfConnectivityBrokenEnabled(new EntityModel<>());
        getSkipFencingIfConnectivityBrokenEnabled().setEntity(true);

        setEnableOvirtService(new EntityModel<>());
        setEnableGlusterService(new EntityModel<>());

        setSerialNumberPolicy(new ListModel<>());
        getSerialNumberPolicy().setItems(getSerialNumberPoliciesWithNull());

        setCustomSerialNumber(new EntityModel<>());
        updateCustomSerialNumber();
        getSerialNumberPolicy().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            updateCustomSerialNumber();
        });

        setAutoConverge(new ListModel<>());
        getAutoConverge().setItems(Arrays.asList(null, true, false));
        setMigrateCompressed(new ListModel<>());
        getMigrateCompressed().setItems(Arrays.asList(null, true, false));
        setMigrateEncrypted(new ListModel<>());
        getMigrateEncrypted().setItems(Arrays.asList(null, true, false));
        getEnableOvirtService().getEntityChangedEvent().addListener((ev, sender, args) -> {
            refreshAdditionalClusterFeaturesList();
            if (!getAllowClusterWithVirtGlusterEnabled() && getEnableOvirtService().getEntity()) {
                getEnableGlusterService().setEntity(Boolean.FALSE);
            }
            updateGlusterFencingPolicyAvailability();
            getEnableGlusterService().setIsChangeable(true);
            getEnableTrustedService().setEntity(false);
            if (getEnableOvirtService().getEntity() != null
                    && getEnableOvirtService().getEntity()) {
                if (getEnableGlusterService().getEntity() != null
                        && !getEnableGlusterService().getEntity()) {
                    getEnableTrustedService().setIsChangeable(true);
                } else {
                    getEnableTrustedService().setIsChangeable(false);
                }

            } else {
                getEnableTrustedService().setIsChangeable(false);
            }
        });
        getEnableOvirtService().setEntity(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        getEnableOvirtService().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.VirtOnly
                && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));

        setRngHwrngSourceRequired(new EntityModel<>());
        getRngHwrngSourceRequired().setIsAvailable(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));

        initImportCluster();

        getEnableGlusterService().getEntityChangedEvent().addListener((ev, sender, args) -> {
            refreshAdditionalClusterFeaturesList();
            if (!getAllowClusterWithVirtGlusterEnabled() && getEnableGlusterService().getEntity()) {
                getEnableOvirtService().setEntity(Boolean.FALSE);
            }

            if (!isEdit
                    && getEnableGlusterService().getEntity() != null
                    && getEnableGlusterService().getEntity()) {
                getIsImportGlusterConfiguration().setIsAvailable(true);
                getGlusterHostAddress().setIsAvailable(true);
                getGlusterHostSshPublicKey().setIsAvailable(true);
                getGlusterHostPassword().setIsAvailable(true);
            } else {
                getIsImportGlusterConfiguration().setIsAvailable(false);
                getIsImportGlusterConfiguration().setEntity(false);

                getGlusterHostAddress().setIsAvailable(false);
                getGlusterHostSshPublicKey().setIsAvailable(false);
                getGlusterHostPassword().setIsAvailable(false);
            }
            if (getEnableGlusterService().getEntity() != null
                    && getEnableGlusterService().getEntity()) {
                getEnableTrustedService().setEntity(false);
                getEnableTrustedService().setIsChangeable(false);
            } else {
                if (getEnableOvirtService().getEntity() != null
                        && getEnableOvirtService().getEntity()) {
                    getEnableTrustedService().setIsChangeable(true);
                } else {
                    getEnableTrustedService().setIsChangeable(false);
                }
            }

            getGlusterTunedProfile().setIsAvailable(getEnableGlusterService().getEntity());
            updateGlusterFencingPolicyAvailability();
            if (getEnableGlusterService().getEntity()) {
                initTunedProfiles();
            }
        });

        getEnableTrustedService().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getEnableTrustedService().getEntity() != null
                    && getEnableTrustedService().getEntity()) {
                getEnableGlusterService().setEntity(false);
                getEnableGlusterService().setIsChangeable(false);
            } else {
                getEnableGlusterService().setIsChangeable(true);
            }
        });

        getEnableGlusterService().setEntity(ApplicationModeHelper.getUiMode() == ApplicationMode.GlusterOnly);
        getEnableGlusterService().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly
                && ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly));

        getGlusterTunedProfile().setIsAvailable(getEnableGlusterService().getEntity());

        setOptimizationNone(new EntityModel<>());
        setOptimizationForServer(new EntityModel<>());
        setOptimizationForDesktop(new EntityModel<>());
        setOptimizationCustom(new EntityModel<>());

        EntityModel<Boolean> tempVar = new EntityModel<>();
        tempVar.setEntity(false);
        setOptimizationNone_IsSelected(tempVar);
        getOptimizationNone_IsSelected().getEntityChangedEvent().addListener(this);
        EntityModel<Boolean> tempVar2 = new EntityModel<>();
        tempVar2.setEntity(false);
        setOptimizationForServer_IsSelected(tempVar2);
        getOptimizationForServer_IsSelected().getEntityChangedEvent().addListener(this);
        EntityModel<Boolean> tempVar3 = new EntityModel<>();
        tempVar3.setEntity(false);
        setOptimizationForDesktop_IsSelected(tempVar3);
        getOptimizationForDesktop_IsSelected().getEntityChangedEvent().addListener(this);
        EntityModel<Boolean> tempVar4 = new EntityModel<>();
        tempVar4.setEntity(false);
        tempVar4.setIsAvailable(false);
        setOptimizationCustom_IsSelected(tempVar4);
        getOptimizationCustom_IsSelected().getEntityChangedEvent().addListener(this);

        EntityModel<Boolean> tempVar5 = new EntityModel<>();
        tempVar5.setEntity(false);
        setMigrateOnErrorOption_YES(tempVar5);
        getMigrateOnErrorOption_YES().getEntityChangedEvent().addListener(this);
        EntityModel<Boolean> tempVar6 = new EntityModel<>();
        tempVar6.setEntity(false);
        setMigrateOnErrorOption_NO(tempVar6);
        getMigrateOnErrorOption_NO().getEntityChangedEvent().addListener(this);
        EntityModel<Boolean> tempVar7 = new EntityModel<>();
        tempVar7.setEntity(false);
        setMigrateOnErrorOption_HA_ONLY(tempVar7);
        getMigrateOnErrorOption_HA_ONLY().getEntityChangedEvent().addListener(this);
        // KSM feature
        setEnableKsm(new EntityModel<>());
        getEnableKsm().setEntity(true);
        getKsmPolicyForNumaSelection().setIsChangeable(true);
        getEnableKsm().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getEnableKsm().getEntity() == null){
                return;
            }
            if (getEnableKsm().getEntity()) {
                getKsmPolicyForNumaSelection().setIsChangeable(true);
            }
            if (!getEnableKsm().getEntity()) {
                getKsmPolicyForNumaSelection().setIsChangeable(false);
            }
        });

        setEnableBallooning(new EntityModel<>());
        getEnableBallooning().setEntity(true);
        // Optimization methods:
        // default value =100;
        setDefaultMemoryOvercommit(AsyncDataProvider.getInstance().getClusterDefaultMemoryOverCommit());

        setCountThreadsAsCores(new EntityModel<>(AsyncDataProvider.getInstance().getClusterDefaultCountThreadsAsCores()));
        setSmtDisabled(new EntityModel<>(AsyncDataProvider.getInstance().getClusterDefaultSmtDisabled()));
        getCountThreadsAsCores().setIsChangeable(!getSmtDisabled().getEntity());
        getSmtDisabled().getEntityChangedEvent().addListener(this);

        setOptimizeForUtilization(new EntityModel<>());
        setOptimizeForSpeed(new EntityModel<>());
        getOptimizeForUtilization().setEntity(true);
        getOptimizeForSpeed().setEntity(false);
        getOptimizeForUtilization().getEntityChangedEvent().addListener(this);
        getOptimizeForSpeed().getEntityChangedEvent().addListener(this);

        setGuarantyResources(new EntityModel<>());
        setAllowOverbooking(new EntityModel<>());
        getGuarantyResources().setEntity(true);
        getAllowOverbooking().setEntity(false);
        getAllowOverbooking().getEntityChangedEvent().addListener(this);
        getGuarantyResources().getEntityChangedEvent().addListener(this);

        boolean overbookingSupported = AsyncDataProvider.getInstance().getScheudulingAllowOverbookingSupported();
        getAllowOverbooking().setIsAvailable(overbookingSupported);
        if (overbookingSupported) {
            getOptimizeForSpeed().getEntityChangedEvent().addListener((ev, sender, args) -> {
                Boolean entity = getOptimizeForSpeed().getEntity();
                if (entity) {
                    getGuarantyResources().setEntity(true);
                }
                getAllowOverbooking().setIsChangeable(!entity);
            });
            getAllowOverbooking().getEntityChangedEvent().addListener((ev, sender, args) -> {
                Boolean entity = getAllowOverbooking().getEntity();
                if (entity) {
                    getOptimizeForUtilization().setEntity(true);
                }
                getOptimizeForSpeed().setIsChangeable(!entity);
            });
        }

        setHostsWithBrokenConnectivityThreshold(new ListModel<>());
        getHostsWithBrokenConnectivityThreshold().setIsAvailable(true);
        getHostsWithBrokenConnectivityThreshold().getSelectedItemChangedEvent().addListener(this);
        initHostsWithBrokenConnectivityThreshold();

        AsyncDataProvider.getInstance().getClusterDesktopMemoryOverCommit(new AsyncQuery<>(result -> {
            setDesktopOverCommit(result);
            AsyncDataProvider.getInstance().getClusterServerMemoryOverCommit(new AsyncQuery<>(r -> {
                setServerOverCommit(r);

                // temp is used for conversion purposes
                EntityModel temp;

                temp = getOptimizationNone();
                temp.setEntity(getDefaultMemoryOvercommit());
                // res1, res2 is used for conversion purposes.
                boolean res1 = getDesktopOverCommit() != getDefaultMemoryOvercommit();
                boolean res2 = getServerOverCommit() != getDefaultMemoryOvercommit();
                temp = getOptimizationNone_IsSelected();
                setIsSelected(res1 && res2);
                temp.setEntity(getIsSelected());

                temp = getOptimizationForServer();
                temp.setEntity(getServerOverCommit());
                temp = getOptimizationForServer_IsSelected();
                temp.setEntity(getServerOverCommit() == getDefaultMemoryOvercommit());

                temp = getOptimizationForDesktop();
                temp.setEntity(getDesktopOverCommit());
                temp = getOptimizationForDesktop_IsSelected();
                temp.setEntity(getDesktopOverCommit() == getDefaultMemoryOvercommit());

                temp = getOptimizationCustom();
                temp.setIsAvailable(false);
                temp.setIsChangeable(false);

                if (getIsEdit()) {
                    postInit();
                }

            }));
        }));

        setDataCenter(new ListModel<>());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);

        setArchitecture(new ListModel<>());
        getArchitecture().setIsAvailable(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));

        setManagementNetwork(new ListModel<>());
        if (isEdit && !isClusterDetached()) {
            getManagementNetwork().setIsChangeable(false);
            getManagementNetwork().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getConstants()
                    .prohibitManagementNetworkChangeInEditClusterInfoMessage());
        }

        setCPU(new FilteredListModel<>());
        getCPU().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getCPU().getSelectedItemChangedEvent().addListener(this);

        setBiosType(new ListModel<>());
        setChangeToQ35(new EntityModel<>(false));
        getBiosType().getSelectedItemChangedEvent().addListener(this);
        initBiosType();

        setFipsMode(new ListModel<>());
        initFipsMode();

        setVersion(new ListModel<>());
        getVersion().getSelectedItemChangedEvent().addListener(this);
        setMigrateOnErrorOption(MigrateOnErrorOptions.YES);

        setFirewallType(new ListModel<>());
        initFirewallType();

        setDefaultNetworkProvider(new ListModel<>());
        initDefaultNetworkProvider();

        setSwitchType(new ListModel<>());
        initSwitchType();


        getRngHwrngSourceRequired().setEntity(false);

        setValidTab(TabName.GENERAL_TAB, true);
        setMigrationTabAvailable(true);

        setClusterPolicy(new ListModel<>());
        setCustomPropertySheet(new KeyValueModel());
        getClusterPolicy().getSelectedItemChangedEvent().addListener(this);
        Frontend.getInstance().runQuery(QueryType.GetAllPolicyUnits, new QueryParametersBase(),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    ArrayList<PolicyUnit> policyUnits = returnValue.getReturnValue();
                    policyUnitMap = new LinkedHashMap<>();
                    for (PolicyUnit policyUnit : policyUnits) {
                        policyUnitMap.put(policyUnit.getId(), policyUnit);
                    }
                    Frontend.getInstance().runQuery(QueryType.GetClusterPolicies,
                            new QueryParametersBase(),
                            new AsyncQuery<QueryReturnValue>(retVal -> {
                                        ArrayList<ClusterPolicy> list = retVal.getReturnValue();
                                        getClusterPolicy().setItems(list);
                                        ClusterPolicy defaultClusterPolicy = null;
                                        ClusterPolicy selectedClusterPolicy = null;
                                        for (ClusterPolicy clusterPolicy : list) {
                                            if (getIsEdit() && getEntity() != null
                                                    && clusterPolicy.getId()
                                                    .equals(getEntity().getClusterPolicyId())) {
                                                selectedClusterPolicy = clusterPolicy;
                                            }
                                            if (clusterPolicy.isDefaultPolicy()) {
                                                defaultClusterPolicy = clusterPolicy;
                                            }
                                        }
                                        if (selectedClusterPolicy != null) {
                                            getClusterPolicy().setSelectedItem(selectedClusterPolicy);
                                        } else {
                                            getClusterPolicy().setSelectedItem(defaultClusterPolicy);
                                        }
                                        clusterPolicyChanged();
                                    }));
                }));
        setCustomMigrationNetworkBandwidth(new EntityModel<>());
        setMigrationBandwidthLimitType(new ListModel<>());
    }

    private List<SerialNumberPolicy> getSerialNumberPoliciesWithNull() {
        List<SerialNumberPolicy> policies = new ArrayList<>(Arrays.asList(SerialNumberPolicy.values()));
        policies.add(0, null);
        return policies;
    }

    private void updateCustomSerialNumber() {
        if (getSerialNumberPolicy().getSelectedItem() == null
                && SerialNumberPolicy.CUSTOM.equals(AsyncDataProvider.getInstance().getSerialNumberPolicy())) {
            getCustomSerialNumber().setIsChangeable(false, constants.systemDefaultCustomSerialNumberDisabledReason());
            getCustomSerialNumber().setEntity(AsyncDataProvider.getInstance().getCustomSerialNumber());
        } else if (SerialNumberPolicy.CUSTOM.equals(getSerialNumberPolicy().getSelectedItem())) {
            getCustomSerialNumber().setIsChangeable(true);
        } else {
            getCustomSerialNumber().setIsChangeable(false, constants.customSerialNumberDisabledReason());
            getCustomSerialNumber().setEntity(null);
        }
    }

    private void updateGlusterFencingPolicyAvailability() {
        boolean fencingAvailable = getEnableGlusterService().getEntity() != null && getEnableGlusterService().getEntity()
                && getEnableOvirtService().getEntity() != null && getEnableOvirtService().getEntity();
        getSkipFencingIfGlusterBricksUp().setIsAvailable(fencingAvailable);
        getSkipFencingIfGlusterQuorumNotMet().setIsAvailable(fencingAvailable);
        if (!fencingAvailable) {
            getSkipFencingIfGlusterBricksUp().setEntity(false);
            getSkipFencingIfGlusterQuorumNotMet().setEntity(false);
        }
    }

    private Version getEffectiveVersion() {
        if (getVersion().getSelectedItem() != null) {
            return getVersion().getSelectedItem();
        } else {
            if (getDataCenter().getSelectedItem() != null) {
                return getDataCenter().getSelectedItem().getCompatibilityVersion();
            } else {
                return Version.getLast();
            }
        }
    }

    public void refreshMigrationPolicies() {
        Version version;
        if (getVersion().getSelectedItem() != null) {
            version = getVersion().getSelectedItem();
        } else {
            Cluster cluster = getEntity();
            version = cluster == null ? getEffectiveVersion() : cluster.getCompatibilityVersion();
        }

        Guid selectedPolicyId = null;
        if (getMigrationPolicies() != null && getMigrationPolicies().getSelectedItem() != null) {
            selectedPolicyId = getMigrationPolicies().getSelectedItem().getId();
        }

        List<MigrationPolicy> policies = AsyncDataProvider.getInstance().getMigrationPolicies(version);
        getMigrationPolicies().setItems(policies);

        MigrationPolicy migrationPolicy;
        if (selectedPolicyId == null) {
            migrationPolicy = getIsEdit() ?
                    findMigrationPolicyById(getEntity().getMigrationPolicyId(), policies) :
                    findFirstNonEmptyMigrationPolicy(policies);
        } else {
            migrationPolicy = findMigrationPolicyById(selectedPolicyId, policies);
        }

        if (migrationPolicy == null) {
            if (version.greaterOrEquals(Version.v4_3)) {
                migrationPolicy = findFirstNonEmptyMigrationPolicy(policies);
            } else {
                migrationPolicy = findMigrationPolicyById(NoMigrationPolicy.ID, policies);
            }
        }
        if (migrationPolicy != null) {
            getMigrationPolicies().setSelectedItem(migrationPolicy);
        }

        getMigrationPolicies().setIsChangeable(true);
    }

    private MigrationPolicy findFirstNonEmptyMigrationPolicy(List<MigrationPolicy> policies) {
        for (MigrationPolicy policy : policies) {
            if (!Objects.equals(policy.getId(), NoMigrationPolicy.ID)) {
                return policy;
            }
        }

        return null;
    }

    private MigrationPolicy findMigrationPolicyById(Guid id, List<MigrationPolicy> policies) {
        for (MigrationPolicy policy : policies) {
            if (Objects.equals(policy.getId(), id)) {
                return policy;
            }
        }

        return null;
    }

    public void initBiosType() {
        boolean allowClusterDefault = getEntity() == null || getEntity().getBiosType() == null;
        ArrayList<BiosType> items = AsyncDataProvider.getInstance().getBiosTypeList();
        if (allowClusterDefault) {
            items.add(0, null);
        }
        getBiosType().setItems(items);
        updateBiosType();
    }

    private void initFirewallType() {
        ListModel<FirewallType> firewallType = getFirewallType();

        firewallType.setItems(Arrays.asList(FirewallType.values()));
        firewallType.setIsChangeable(true);
        firewallType.setSelectedItem(FirewallType.FIREWALLD);
    }

    private void initFipsMode() {
        ListModel<FipsMode> fipsModes = getFipsMode();

        fipsModes.setItems(Arrays.asList(FipsMode.values()));
        fipsModes.setIsChangeable(true);
        if (getEntity() != null) {
            fipsModes.setSelectedItem(getEntity().getFipsMode());
        } else {
            fipsModes.setSelectedItem(FipsMode.UNDEFINED);
        }
    }

    private void updateFipsMode(Version version) {
        ListModel<FipsMode> fipsModes = getFipsMode();
        if (AsyncDataProvider.getInstance().isFipsModeSupportedByVersion(version)) {
            fipsModes.setIsAvailable(true);
            fipsModes.setIsChangeable(true);
            if (getEntity() != null) {
                fipsModes.setSelectedItem(getEntity().getFipsMode());
            } else {
                fipsModes.setSelectedItem(FipsMode.UNDEFINED);
            }
        } else {
            fipsModes.setSelectedItem(FipsMode.UNDEFINED);
            fipsModes.setIsAvailable(false);
            fipsModes.setIsChangeable(false);
        }
    }

    private void initLogMaxMemoryUsedThresholdType() {
        ListModel<LogMaxMemoryUsedThresholdType> logMaxMemoryUsedThresholdType = getLogMaxMemoryUsedThresholdType();

        logMaxMemoryUsedThresholdType.setItems(Arrays.asList(LogMaxMemoryUsedThresholdType.values()));
        logMaxMemoryUsedThresholdType.setIsChangeable(true);
        logMaxMemoryUsedThresholdType.setSelectedItem(LogMaxMemoryUsedThresholdType.PERCENTAGE);
    }

    private void updateFirewallTypeUponVersionChange(Version version) {
        ListModel<FirewallType> firewallType = getFirewallType();
        if (version.less(AsyncDataProvider.getInstance().multiFirewallSupportSince())) {
            firewallType.setIsChangeable(false);
            firewallType.setSelectedItem(FirewallType.IPTABLES);
        } else {
            firewallType.setIsChangeable(true);
        }
    }

    private void updateBiosType() {
        ArchitectureType architecture = getArchitecture().getSelectedItem();

        if (architecture == null || architecture.getFamily() != ArchitectureType.x86) {
            getBiosType().setIsChangeable(false, ConstantsManager.getInstance().getMessages().biosTypeSupportedForX86Only());
        } else {
            getBiosType().updateChangeability(ConfigValues.BiosTypeSupported, getEffectiveVersion());
        }

        if (architecture == ArchitectureType.undefined || (!getBiosType().getIsChangable() && getBiosType().getSelectedItem() == null)) {
            getBiosType().setSelectedItem(null);
            return;
        }

        if (getIsNew() &&
                getBiosType().getIsChangable() &&
                getBiosType().getSelectedItem() != null &&
                Version.v4_6.less(getEffectiveVersion())) {
            getBiosType().setSelectedItem(BiosType.Q35_OVMF);
            return;
        }

        if (getIsEdit() &&
                getBiosType().getIsChangable() &&
                getBiosType().getSelectedItem() != null &&
                !getEffectiveVersion().equals(getEntity().getCompatibilityVersion()) &&
                Version.v4_6.less(getEffectiveVersion())) {
            getBiosType().setSelectedItem(BiosType.Q35_OVMF);
            return;
        }

        if (getIsEdit() && architecture.equals(getEntity().getArchitecture())
                && getEffectiveVersion().equals(getEntity().getCompatibilityVersion())) {
            getBiosType().setSelectedItem(getEntity().getBiosType());
            return;
        }
    }

    private void initDefaultNetworkProvider() {
        AsyncDataProvider.getInstance().getAllProvidersByType(new AsyncQuery<>(result -> {
            List<Provider> providers = (List) result;
            Provider noDefaultNetworkProvider = new Provider();
            noDefaultNetworkProvider.setName(
                    ConstantsManager.getInstance().getConstants().clusterNoDefaultNetworkProvider());

            providers.add(0, noDefaultNetworkProvider);
            getDefaultNetworkProvider().setItems(providers);
            Cluster cluster = getEntity();
            if (cluster != null) {
                Provider defaultNetworkProvider = providers.stream()
                        .filter(provider -> cluster.hasDefaultNetworkProviderId(provider.getId()))
                        .findFirst().orElse(noDefaultNetworkProvider);
                getDefaultNetworkProvider().setSelectedItem(defaultNetworkProvider);
            }
        }), ProviderType.OPENSTACK_NETWORK, ProviderType.EXTERNAL_NETWORK);
    }

    private void initSwitchType() {
        ListModel<SwitchType> switchType = getSwitchType();

        switchType.setItems(Arrays.asList(SwitchType.values()));
        switchType.setIsChangeable(false);
        switchType.setSelectedItem(SwitchType.LEGACY);
    }

    private void updateSwitchTypeUponVersionChange(Version version) {
        ListModel<SwitchType> switchType = getSwitchType();

        UIConstants constants = ConstantsManager.getInstance().getConstants();

        switchType.setIsChangeable(!getIsEdit(), constants.clusterSwitchChangeDisabled());
    }

    boolean isClusterDetached() {
        if (detached == null) {
            detached = getEntity().getStoragePoolId() == null;
        }
        return detached;
    }

    private void initSpiceProxy() {
        String proxy = getEntity().getSpiceProxy();
        boolean isProxyAvailable = !StringHelper.isNullOrEmpty(proxy);
        getSpiceProxyEnabled().setEntity(isProxyAvailable);
        getSpiceProxy().setIsChangeable(isProxyAvailable);
        getSpiceProxy().setEntity(proxy);
    }

    private void initImportCluster() {
        setGlusterHostAddress(new EntityModel<>());
        getGlusterHostAddress().getEntityChangedEvent().addListener((ev, sender, args) -> {
            setHostSshPublicKeyVerified(false);
            if (getGlusterHostAddress().getEntity() == null
                    || getGlusterHostAddress().getEntity().trim().length() == 0) {
                getGlusterHostSshPublicKey().setEntity(""); //$NON-NLS-1$
                return;
            }
            fetchHostSshPublicKey(
                    getGlusterHostAddress().getEntity(),
                    VdsStatic.DEFAULT_SSH_PORT);
        });

        setHostSshPublicKeyVerified(false);
        setGlusterHostSshPublicKey(new EntityModel<>());
        getGlusterHostSshPublicKey().setEntity(""); //$NON-NLS-1$
        setGlusterHostPassword(new EntityModel<>());

        setIsImportGlusterConfiguration(new EntityModel<>());
        getIsImportGlusterConfiguration().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getIsImportGlusterConfiguration().getEntity() != null
                    && getIsImportGlusterConfiguration().getEntity()) {
                getGlusterHostAddress().setIsChangeable(true);
                getGlusterHostPassword().setIsChangeable(true);
            } else {
                getGlusterHostAddress().setIsChangeable(false);
                getGlusterHostPassword().setIsChangeable(false);
            }
        });

        getIsImportGlusterConfiguration().setIsAvailable(false);
        getGlusterHostAddress().setIsAvailable(false);
        getGlusterHostSshPublicKey().setIsAvailable(false);
        getGlusterHostPassword().setIsAvailable(false);

        getIsImportGlusterConfiguration().setEntity(false);
    }

    private void fetchHostSshPublicKey(String hostAddress, Integer hostPort) {
        AsyncDataProvider.getInstance().getHostSshPublicKey(new AsyncQuery<>(publicKey -> {
            if (publicKey != null && publicKey.length() > 0) {
                getGlusterHostSshPublicKey().setEntity(publicKey);
                setHostSshPublicKeyVerified(true);
            } else {
                getGlusterHostSshPublicKey().setEntity(ConstantsManager.getInstance()
                        .getConstants()
                        .errorLoadingHostSshPublicKey());
                setHostSshPublicKeyVerified(false);
            }
        }), hostAddress, hostPort);

        getGlusterHostSshPublicKey().setEntity(ConstantsManager.getInstance().getConstants().loadingPublicKey());
    }

    private void postInit() {
        getDescription().setEntity(getEntity().getDescription());
        getComment().setEntity(getEntity().getComment());

        initSpiceProxy();
        getVncEncryptionEnabled().setEntity(getEntity().isVncEncryptionEnabled());
        getFencingEnabledModel().setEntity(getEntity().getFencingPolicy().isFencingEnabled());
        getSkipFencingIfSDActiveEnabled().setEntity(getEntity().getFencingPolicy().isSkipFencingIfSDActive());
        getSkipFencingIfConnectivityBrokenEnabled().setEntity(getEntity().getFencingPolicy()
                .isSkipFencingIfConnectivityBroken());
        getHostsWithBrokenConnectivityThreshold().setSelectedItem(getEntity().getFencingPolicy()
                .getHostsWithBrokenConnectivityThreshold());
        getSkipFencingIfGlusterBricksUp().setEntity(getEntity().getFencingPolicy().isSkipFencingIfGlusterBricksUp());
        getSkipFencingIfGlusterQuorumNotMet().setEntity(getEntity().getFencingPolicy().isSkipFencingIfGlusterQuorumNotMet());

        setMemoryOverCommit(getEntity().getMaxVdsMemoryOverCommit());

        getCountThreadsAsCores().setEntity(getEntity().getCountThreadsAsCores());
        getSmtDisabled().setEntity(getEntity().getSmtDisabled());
        getEnableBallooning().setEntity(getEntity().isEnableBallooning());
        getEnableKsm().setEntity(getEntity().isEnableKsm());

        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(dataCenters -> {
            getDataCenter().setItems(dataCenters);

            getDataCenter().setSelectedItem(null);
            final Guid dataCenterId = getEntity().getStoragePoolId();
            for (StoragePool dataCenter : dataCenters) {
                if (dataCenterId != null && dataCenter.getId().equals(dataCenterId)) {
                    getDataCenter().setSelectedItem(dataCenter);
                    break;
                }
            }
            final StoragePool selectedDataCenter = getDataCenter().getSelectedItem();
            getDataCenter().setIsChangeable(selectedDataCenter == null);

            setMigrateOnErrorOption(getEntity().getMigrateOnError());

            if (!getManagementNetwork().getIsChangable()) {
                loadCurrentClusterManagementNetwork();
            }
        }));
    }

    private void loadCurrentClusterManagementNetwork() {
        AsyncDataProvider.getInstance().getManagementNetwork(new AsyncQuery<>(network -> getManagementNetwork().setSelectedItem(network)), getEntity().getId());
    }

    private void loadDcNetworks(final Guid dataCenterId) {
        if (dataCenterId == null) {
            return;
        }
        final AsyncQuery<List<Network>> getAllDataCenterNetworksQuery = new AsyncQuery<>(new AsyncCallback<List<Network>>() {
            @Override
            public void onSuccess(List<Network> dcNetworks) {
                if (getDataCenter().getSelectedItem() == null) {
                    return;
                }
                getManagementNetwork().setItems(dcNetworks);

                if (defaultManagementNetworkCache.containsKey(dataCenterId)) {
                    final Network defaultManagementNetwork = defaultManagementNetworkCache.get(dataCenterId);
                    setSelectedDefaultManagementNetwork(defaultManagementNetwork);
                } else {
                    final AsyncQuery<Network> getDefaultManagementNetworkQuery =
                            new AsyncQuery<>(defaultManagementNetwork -> {
                                defaultManagementNetworkCache.put(dataCenterId, defaultManagementNetwork);
                                setSelectedDefaultManagementNetwork(defaultManagementNetwork);
                            });
                    AsyncDataProvider.getInstance()
                            .getDefaultManagementNetwork(getDefaultManagementNetworkQuery, dataCenterId);
                }
            }

            private void setSelectedDefaultManagementNetwork(Network defaultManagementNetwork) {
                if (defaultManagementNetwork != null) {
                    getManagementNetwork().setSelectedItem(defaultManagementNetwork);
                }
            }
        });
        AsyncDataProvider.getInstance().getManagementNetworkCandidates(getAllDataCenterNetworksQuery, dataCenterId);
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.itemsChangedEventDefinition)) {
            handleItemsChangedEventDefinition(sender);
        } else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition)) {
            handleSelectedItemChangedEventDefinition(sender);
        } else if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
            handleEntityChangedEventDefinition((EntityModel<Boolean>) sender);
        }
    }

    private void handleItemsChangedEventDefinition(Object sender) {
        if (sender == getMacPoolListModel()) {
            initSelectedMacPool();
        }
    }

    private void handleEntityChangedEventDefinition(EntityModel<Boolean> senderEntityModel) {
        if (senderEntityModel == getSpiceProxyEnabled()) {
            getSpiceProxy().setIsChangeable(getSpiceProxyEnabled().getEntity());
        } else if (senderEntityModel == getSmtDisabled()) {
            getCountThreadsAsCores().setIsChangeable(!getSmtDisabled().getEntity());
            if (getSmtDisabled().getEntity()) {
                // Disable countThreadsAsCores when SMT not enabled
                getCountThreadsAsCores().setEntity(false);
            }
        } else if ((Boolean) senderEntityModel.getEntity()) {
            if (senderEntityModel == getOptimizationNone_IsSelected()) {
                getOptimizationForServer_IsSelected().setEntity(false);
                getOptimizationForDesktop_IsSelected().setEntity(false);
                getOptimizationCustom_IsSelected().setEntity(false);
            } else if (senderEntityModel == getOptimizationForServer_IsSelected()) {
                getOptimizationNone_IsSelected().setEntity(false);
                getOptimizationForDesktop_IsSelected().setEntity(false);
                getOptimizationCustom_IsSelected().setEntity(false);
            } else if (senderEntityModel == getOptimizationForDesktop_IsSelected()) {
                getOptimizationNone_IsSelected().setEntity(false);
                getOptimizationForServer_IsSelected().setEntity(false);
                getOptimizationCustom_IsSelected().setEntity(false);
            } else if (senderEntityModel == getOptimizationCustom_IsSelected()) {
                getOptimizationNone_IsSelected().setEntity(false);
                getOptimizationForServer_IsSelected().setEntity(false);
                getOptimizationForDesktop_IsSelected().setEntity(false);
            } else if (senderEntityModel == getMigrateOnErrorOption_YES()) {
                getMigrateOnErrorOption_NO().setEntity(false);
                getMigrateOnErrorOption_HA_ONLY().setEntity(false);
            } else if (senderEntityModel == getMigrateOnErrorOption_NO()) {
                getMigrateOnErrorOption_YES().setEntity(false);
                getMigrateOnErrorOption_HA_ONLY().setEntity(false);
            } else if (senderEntityModel == getMigrateOnErrorOption_HA_ONLY()) {
                getMigrateOnErrorOption_YES().setEntity(false);
                getMigrateOnErrorOption_NO().setEntity(false);
            } else if (senderEntityModel == getOptimizeForUtilization()) {
                getOptimizeForSpeed().setEntity(false);
            } else if (senderEntityModel == getOptimizeForSpeed()) {
                getOptimizeForUtilization().setEntity(false);
            } else if(senderEntityModel == getGuarantyResources()) {
                getAllowOverbooking().setEntity(false);
            } else if(senderEntityModel == getAllowOverbooking()) {
                getGuarantyResources().setEntity(false);
            }
        }
    }

    private void handleSelectedItemChangedEventDefinition(Object sender) {
        if (sender == getDataCenter()) {
            storagePool_SelectedItemChanged();
        } else if (sender == getVersion()) {
            version_SelectedItemChanged();
        } else if (sender == getClusterPolicy()) {
            clusterPolicyChanged();
        } else if (sender == getCPU()) {
            CPU_SelectedItemChanged();
        } else if (sender == getArchitecture()) {
            architectureSelectedItemChanged();
        } else if (sender == getMacPoolListModel()) {
            getMacPoolModel().setEntity(getMacPoolListModel().getSelectedItem());
        } else if (sender == getMigrationPolicies()) {
            migrationPoliciesChanged();
        } else if (sender == getBiosType()) {
            updateChangeToQ35();
        }
    }

    private void migrationPoliciesChanged() {
        boolean hasMigrationPolicy = getMigrationPolicies().getSelectedItem() != null
                && !NoMigrationPolicy.ID.equals(getMigrationPolicies().getSelectedItem().getId());

        if (getEffectiveVersion() != null && getEffectiveVersion().greaterOrEquals(Version.v4_4)) {
            getAutoConverge().setIsAvailable(false);
            getMigrateCompressed().setIsAvailable(false);
        } else {
            getAutoConverge().setIsAvailable(true);
            getMigrateCompressed().setIsAvailable(true);

            UIConstants constants = ConstantsManager.getInstance().getConstants();
            getAutoConverge().setIsChangeable(!hasMigrationPolicy, constants.availableOnlyWithLegacyPolicy());
            getMigrateCompressed().setIsChangeable(!hasMigrationPolicy, constants.availableOnlyWithLegacyPolicy());
        }
    }

    private void architectureSelectedItemChanged() {
        filterCpuTypeByArchitecture();
        updateBiosType();
    }

    private void filterCpuTypeByArchitecture() {
        final ArchitectureType selectedArchitecture = getArchitecture().getSelectedItem();
        final FilteredListModel.Filter<ServerCpu> filter = selectedArchitecture == null
                || selectedArchitecture.equals(ArchitectureType.undefined)
                ? cpu -> cpu == null || cpu.getLevel() > 0
                : cpu -> cpu != null && selectedArchitecture.equals(cpu.getArchitecture()) && cpu.getLevel() > 0;
        getCPU().filterItems(filter);
    }

    private void CPU_SelectedItemChanged() {
        updateMigrateOnError();
    }

    private void version_SelectedItemChanged() {
        Version version = getEffectiveVersion();

        AsyncDataProvider.getInstance().getCPUList(new AsyncQuery<>(cpus -> {
            if (getIsEdit()) {

                AsyncDataProvider.getInstance().isClusterEmpty(new AsyncQuery<>(isEmpty -> {
                    if (isEmpty) {
                        cpus.add(0, null);
                        populateCPUList(cpus, true);
                    } else {
                        ArrayList<ServerCpu> filteredCpus = new ArrayList<>();

                        for (ServerCpu cpu : cpus) {
                            if (cpu.getArchitecture() == getEntity().getArchitecture()) {
                                filteredCpus.add(cpu);
                            }
                        }

                        populateCPUList(filteredCpus, false);
                    }
                }), getEntity().getId());
            } else {
                cpus.add(0, null);
                populateCPUList(cpus, true);
            }
        }), version);

        setRngSourcesCheckboxes(version);

        updateSwitchTypeUponVersionChange(version);
        updateFirewallTypeUponVersionChange(version);

        updateFencingPolicyContent(version);

        updateMigrateOnError();

        refreshMigrationPolicies();

        updateMigrateEncrypted(version);

        updateFipsMode(version);

        refreshAdditionalClusterFeaturesList();

        if (getEnableGlusterService().getEntity()) {
            initTunedProfiles();
        }

        updateBiosType();
    }

    private void refreshAdditionalClusterFeaturesList() {
        if (getVersion() == null || getVersion().getSelectedItem() == null) {
            return;
        }
        Version version = getVersion().getSelectedItem();

        ApplicationMode category = null;
        if (getEnableGlusterService().getEntity() && getEnableOvirtService().getEntity()) {
            category = ApplicationMode.AllModes;
        } else if (getEnableGlusterService().getEntity()) {
            category = ApplicationMode.GlusterOnly;
        } else if (getEnableOvirtService().getEntity()) {
            category = ApplicationMode.VirtOnly;
        }

        // Get all the addtional features avaivalble for the cluster
        startProgress();
        AsyncDataProvider.getInstance().getClusterFeaturesByVersionAndCategory(new AsyncQuery<>(features -> {
            stopProgress();
            // Get the additional features which are already enabled for cluster. Applicable only in case of edit
            // cluster
            if (getIsEdit() && !features.isEmpty()) {
                startProgress();
                AsyncDataProvider.getInstance().getClusterFeaturesByClusterId(new AsyncQuery<>(clusterFeatures -> {
                    stopProgress();
                    Set<AdditionalFeature> featuresEnabled = new HashSet<>();
                    for (SupportedAdditionalClusterFeature feature : clusterFeatures) {
                        if (feature.isEnabled()) {
                            featuresEnabled.add(feature.getFeature());
                        }
                    }
                    updateAddtionClusterFeatureList(features, featuresEnabled);
                }), getEntity().getId());
            } else {
                updateAddtionClusterFeatureList(features, Collections.emptySet());
            }
        }), version, category);
    }

    private void updateAddtionClusterFeatureList(Set<AdditionalFeature> featuresAvailable,
            Set<AdditionalFeature> featuresEnabled) {
        List<AdditionalFeature> features = new ArrayList<>();
        List<AdditionalFeature> selectedFeatures = new ArrayList<>();
        for (AdditionalFeature feature : featuresAvailable) {
            features.add(feature);
            if (featuresEnabled.contains(feature)) {
                selectedFeatures.add(feature);
            }
        }
        List<List<AdditionalFeature>> clusterFeatureList = new ArrayList<>();
        clusterFeatureList.add(features);
        getAdditionalClusterFeatures().setItems(clusterFeatureList, selectedFeatures);
    }

    private void updateMigrateOnError() {
        ServerCpu cpu = getCPU().getSelectedItem();

        Version version = getVersion().getSelectedItem();

        if (version == null) {
            return;
        }

        if (cpu == null || cpu.getArchitecture() == null) {
            return;
        }

        getMigrateOnErrorOption_NO().setIsAvailable(true);

        if (AsyncDataProvider.getInstance().isMigrationSupported(cpu.getArchitecture(), version)) {
            getMigrateOnErrorOption_YES().setIsAvailable(true);
            getMigrateOnErrorOption_HA_ONLY().setIsAvailable(true);
        } else {
            getMigrateOnErrorOption_YES().setIsAvailable(false);
            getMigrateOnErrorOption_HA_ONLY().setIsAvailable(false);

            setMigrateOnErrorOption(MigrateOnErrorOptions.NO);
        }
    }

    private void updateMigrateEncrypted(Version version) {
        if (version.greaterOrEquals(Version.v4_4)) {
            getMigrateEncrypted().setIsChangeable(true);
        } else {
            getMigrateEncrypted().setIsChangeable(false, messages.availableInVersionOrHigher(Version.v4_4.toString()));
            getMigrateEncrypted().setSelectedItem(null);
        }
    }

    private void setRngSourcesCheckboxes(Version ver) {
        getRngHwrngSourceRequired().setIsChangeable(true);

        String defaultRequiredRngSourcesCsv = defaultClusterRngSourcesCsv(ver);

        getRngHwrngSourceRequired().setEntity(getIsNew()
                ? defaultRequiredRngSourcesCsv.contains(VmRngDevice.Source.HWRNG.name().toLowerCase())
                : getEntity().getRequiredRngSources().contains(VmRngDevice.Source.HWRNG));
    }

    private void updateFencingPolicyContent(Version ver) {
        // skipFencingIfConnectivityBroken option is enabled when fencing is enabled for all cluster versions
        getSkipFencingIfConnectivityBrokenEnabled().setIsChangeable(getFencingEnabledModel().getEntity());
        getHostsWithBrokenConnectivityThreshold().setIsChangeable(getFencingEnabledModel().getEntity());
        getSkipFencingIfGlusterBricksUp().setIsChangeable(getFencingEnabledModel().getEntity());
        getSkipFencingIfGlusterQuorumNotMet().setIsChangeable(getFencingEnabledModel().getEntity());

        if (ver == null) {
            if (!getFencingEnabledModel().getEntity()) {
                // fencing is disabled and cluster version not selected yet, so disable skipFencingIfSDActive
                getSkipFencingIfSDActiveEnabled().setIsChangeable(false);
            }
        } else {
            // skipFencingIfSDActive is enabled for supported cluster level if fencing is not disabled
            getSkipFencingIfSDActiveEnabled().setIsChangeable(getFencingEnabledModel().getEntity());
            if (getEntity() == null) {
                // this can happen when creating new cluster and cluster dialog is shown
                getSkipFencingIfSDActiveEnabled().setEntity(true);
            } else {
                getSkipFencingIfSDActiveEnabled().setEntity(
                        getEntity().getFencingPolicy().isSkipFencingIfSDActive());
            }
        }
    }

    private void populateCPUList(List<ServerCpu> cpus, boolean canChangeArchitecture) {
        // disable CPU Architecture-Type filtering
        getArchitecture().getSelectedItemChangedEvent().removeListener(this);

        ServerCpu oldSelectedCpu = getCPU().getSelectedItem();
        ArchitectureType oldSelectedArch = getArchitecture().getSelectedItem();

        getCPU().setItems(cpus);
        initSupportedArchitectures();

        getCPU().setSelectedItem(oldSelectedCpu != null ?
                Linq.firstOrNull(cpus, new Linq.ServerCpuPredicate(oldSelectedCpu.getCpuName())) : null);

        if (getCPU().getSelectedItem() == null || !isCPUinitialized) {
            initCPU();
        }

        if (getIsEdit()) {
            if (!canChangeArchitecture) {
                getArchitecture().setItems(new ArrayList<>(Collections.singletonList(getEntity().getArchitecture())));
            }

            if (oldSelectedArch != null) {
                getArchitecture().setSelectedItem(oldSelectedArch);
            } else {
                if (getEntity() != null) {
                    getArchitecture().setSelectedItem(getEntity().getArchitecture());
                } else {
                    getArchitecture().setSelectedItem(ArchitectureType.undefined);
                }
            }
        } else {
            getArchitecture().setSelectedItem(ArchitectureType.undefined);
        }

        boolean shouldFindEquivalentCpu = getCPU().getSelectedItem() == null;
        // enable CPU Architecture-Type filtering
        initCpuArchTypeFiltering();

        if (shouldFindEquivalentCpu) {
            String flags;
            if (oldSelectedCpu != null) {
                flags = String.join(",", oldSelectedCpu.getFlags());//$NON-NLS-1$
            } else if (getEntity() != null && getEntity().getCpuFlags() != null){
                flags = getEntity().getCpuFlags();
            } else {
                return;
            }

            AsyncDataProvider.getInstance().getCpuByFlags(new AsyncQuery<>(cpu -> {
                String cpuName = cpu != null ? cpu.getCpuName() : CpuUtils.getCpuNameInVersion(oldSelectedCpu, getEffectiveVersion());

                getCPU().setSelectedItem(cpuName != null ?
                        Linq.firstOrNull(getCPU().getItems(), new Linq.ServerCpuPredicate(cpuName)) : null);

            }), flags, getEffectiveVersion());
        }
    }

    private void initCpuArchTypeFiltering() {
        filterCpuTypeByArchitecture();
        getArchitecture().getSelectedItemChangedEvent().addListener(this);
    }

    private void initSupportedArchitectures() {
        Collection<ArchitectureType> archsWithSupportingCpus = new HashSet<>();
        archsWithSupportingCpus.add(ArchitectureType.undefined);
        for (ServerCpu cpu: getCPU().getItems()) {
            if (cpu != null) {
                archsWithSupportingCpus.add(cpu.getArchitecture());
            }
        }
        getArchitecture().setItems(archsWithSupportingCpus);
    }

    private void initCPU() {
        if (!isCPUinitialized && getIsEdit()) {
            isCPUinitialized = true;
            getCPU().setSelectedItem(null);
            for (ServerCpu a : getCPU().getItems()) {
                if (a == null) {
                    continue;
                }
                if (Objects.equals(a.getCpuName(), getEntity().getCpuName())) {
                    getCPU().setSelectedItem(a);
                    break;
                }
            }
        }
    }

    private void initHostsWithBrokenConnectivityThreshold() {
        ArrayList<Integer> values = new ArrayList<>();
        // populating threshold values with {25, 50, 75, 100}
        for (int i = 25; i <= 100; i += 25) {
            values.add(i);
        }
        getHostsWithBrokenConnectivityThreshold().setItems(values);
        getHostsWithBrokenConnectivityThreshold().setSelectedItem(50);
    }

    private void storagePool_SelectedItemChanged() {
        // possible versions for new cluster (when editing cluster, this event won't occur)
        // are actually the possible versions for the data-center that the cluster is going
        // to be attached to.
        final StoragePool selectedDataCenter = getDataCenter().getSelectedItem();
        if (selectedDataCenter == null) {
            getManagementNetwork().setItems(Collections.emptyList());
            return;
        }
        if (selectedDataCenter.isLocal()) {
            setMigrationTabAvailable(false);
        } else {
            setMigrationTabAvailable(true);
        }

        AsyncDataProvider.getInstance().getDataCenterVersions(new AsyncQuery<>(new AsyncCallback<List<Version>>() {
            @Override
            public void onSuccess(List<Version> versions) {
                Version versionToSelect = calculateNewVersionWhichShouldBeSelected(versions);

                getVersion().setItems(versions, versionToSelect);
            }

            private Version calculateNewVersionWhichShouldBeSelected(List<Version> versions) {
                ListModel<Version> version = getVersion();
                Version selectedVersion = version.getSelectedItem();

                if (!getIsEdit() && (selectedVersion == null || !versions.contains(selectedVersion) ||
                        selectedVersion.compareTo(selectedDataCenter.getCompatibilityVersion()) > 0)) {
                    if (ApplicationModeHelper.getUiMode().equals(ApplicationMode.GlusterOnly)) {
                        return versions.stream().max(Comparator.naturalOrder()).orElse(null);
                    } else {
                        return selectedDataCenter.getCompatibilityVersion();
                    }
                } else if (getIsEdit()) {
                    return Linq.firstOrNull(versions, x -> x.equals(getEntity().getCompatibilityVersion()));
                } else {
                    return selectedVersion;
                }
            }
        }), ApplicationModeHelper.getUiMode().equals(ApplicationMode.GlusterOnly) ? null : selectedDataCenter.getId());

        if (getManagementNetwork().getIsChangable()) {
            loadDcNetworks(selectedDataCenter.getId());
        }
    }

    private void clusterPolicyChanged() {
        ClusterPolicy clusterPolicy = getClusterPolicy().getSelectedItem();
        Map<String, String> policyProperties = new HashMap<>();
        Map<Guid, PolicyUnit> allPolicyUnits = new HashMap<>();
        if (clusterPolicy.getFilters() != null) {
            for (Guid policyUnitId : clusterPolicy.getFilters()) {
                allPolicyUnits.put(policyUnitId, policyUnitMap.get(policyUnitId));
            }
        }
        if (clusterPolicy.getFunctions() != null) {
            for (Pair<Guid, Integer> pair : clusterPolicy.getFunctions()) {
                allPolicyUnits.put(pair.getFirst(), policyUnitMap.get(pair.getFirst()));
            }
        }
        if (clusterPolicy.getBalance() != null) {
            allPolicyUnits.put(clusterPolicy.getBalance(), policyUnitMap.get(clusterPolicy.getBalance()));
        }

        for (PolicyUnit policyUnit : allPolicyUnits.values()) {
            if (policyUnit.getParameterRegExMap() != null) {
                policyProperties.putAll(policyUnit.getParameterRegExMap());
            }
        }
        getCustomPropertySheet().setKeyValueMap(policyProperties);
        if (getIsEdit() &&
                clusterPolicy.getId().equals(getEntity().getClusterPolicyId())) {
            getCustomPropertySheet().deserialize(KeyValueModel.convertProperties(getEntity().getClusterPolicyProperties()));
        } else {
            getCustomPropertySheet().deserialize(KeyValueModel.convertProperties(clusterPolicy.getParameterMap()));
        }
    }

    private void initSelectedMacPool() {
        Collection<MacPool> allMacPools = getMacPoolListModel().getItems();
        Cluster cluster = getEntity();
        if (allMacPools != null && cluster != null) {
            Guid macPoolId = cluster.getMacPoolId();
            for (MacPool macPool : allMacPools) {
                if (macPool.getId().equals(macPoolId)) {
                    getMacPoolListModel().setSelectedItem(macPool);
                    break;
                }
            }
        }
    }


    private void updateChangeToQ35() {
        getChangeToQ35().setIsChangeable(getIsEdit() && getBiosType().getSelectedItem() != null
                && getBiosType().getSelectedItem().getChipsetType() == ChipsetType.Q35);
        if (!getChangeToQ35().getIsChangable()) {
            getChangeToQ35().setEntity(false);
        }
    }

    public boolean validate() {
        // General tab
        validateName();
        getDataCenter().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getVersion().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getManagementNetwork().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getGlusterHostAddress().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getGlusterHostPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });

        boolean generalTabValid = getName().getIsValid()
                && getDataCenter().getIsValid()
                && getManagementNetwork().getIsValid()
                && getVersion().getIsValid()
                && getGlusterHostAddress().getIsValid()
                && getGlusterHostPassword().getIsValid()
                && (!getIsImportGlusterConfiguration().getEntity() || (getGlusterHostAddress().getIsValid()
                && isHostSshPublicKeyVerified()));

        setValidTab(TabName.GENERAL_TAB, generalTabValid);

        // Migration Policy tab
        if (getVersion().getSelectedItem() != null) {
            if (MigrationBandwidthLimitType.CUSTOM.equals(getMigrationBandwidthLimitType().getSelectedItem())) {
                getCustomMigrationNetworkBandwidth().validateEntity(
                        new IValidation[] { new NotNullIntegerValidation(0, Integer.MAX_VALUE) });
            } else {
                getCustomMigrationNetworkBandwidth().setIsValid(true);
            }
        }

        final boolean migrationTabValid =
                getMigrationBandwidthLimitType().getIsValid()
                && getCustomMigrationNetworkBandwidth().getIsValid();

        setValidTab(TabName.MIGRATION_TAB, migrationTabValid);

        // Scheduling tab
        getCustomPropertySheet().setIsValid(getCustomPropertySheet().validate());

        if (SerialNumberPolicy.CUSTOM.equals(getSerialNumberPolicy().getSelectedItem())) {
            getCustomSerialNumber().validateEntity(new IValidation[] { new NotEmptyValidation() });
        } else {
            getCustomSerialNumber().setIsValid(true);
        }

        final boolean schedullingTabValid = getCustomPropertySheet().getIsValid()
                && getCustomSerialNumber().getIsValid();

        setValidTab(TabName.CLUSTER_POLICY_TAB, schedullingTabValid);

        // Console tab
        if (getSpiceProxyEnabled().getEntity()) {
            getSpiceProxy().validateEntity(new IValidation[] { new HostWithProtocolAndPortAddressValidation() });
        } else {
            getSpiceProxy().setIsValid(true);
        }

        boolean consoleTablValid = getSpiceProxy().getIsValid();

        setValidTab(TabName.CONSOLE_TAB, consoleTablValid);

        // MAC Address Pool tab
        getMacPoolModel().validate();

        boolean macPoolTabValid = getMacPoolModel().getIsValid();

        setValidTab(TabName.MAC_POOL_TAB, macPoolTabValid);

        ValidationCompleteEvent.fire(getEventBus(), this);
        return generalTabValid
                && migrationTabValid
                && schedullingTabValid
                && consoleTablValid
                && macPoolTabValid;
    }

    public void validateName() {
        getName().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(40),
                new I18NNameValidation() });
    }

    private String defaultClusterRngSourcesCsv(Version ver) {
        String srcs = (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.ClusterRequiredRngSourcesDefault, ver.toString());
        return (srcs == null)
                ? ""
                : srcs;
    }

    public boolean getKsmPolicyForNuma() {
        switch (getKsmPolicyForNumaSelection().getSelectedItem()) {
        case shareAcrossNumaNodes:
            return true;
        case shareInsideEachNumaNode:
            return false;
        }
        return true;
    }

    public void setKsmPolicyForNuma(Boolean ksmPolicyForNumaFlag) {
        if (ksmPolicyForNumaFlag == null) {
            return;
        }
        KsmPolicyForNuma ksmPolicyForNuma = KsmPolicyForNuma.shareAcrossNumaNodes;
        if (!ksmPolicyForNumaFlag) {
            ksmPolicyForNuma = KsmPolicyForNuma.shareInsideEachNumaNode;
        }

        getKsmPolicyForNumaSelection().setSelectedItem(ksmPolicyForNuma);
    }

    public enum KsmPolicyForNuma {

        shareAcrossNumaNodes(ConstantsManager.getInstance().getConstants().shareKsmAcrossNumaNodes()),
        shareInsideEachNumaNode(ConstantsManager.getInstance().getConstants().shareKsmInsideEachNumaNode());

        private String description;

        private KsmPolicyForNuma(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
