package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
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
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.ValidationCompleteEvent;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacPoolModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SerialNumberPolicyModel;
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
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class ClusterModel extends EntityModel<Cluster> implements HasValidatedTabs {
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
        if (allowClusterWithVirtGlusterEnabled != value) {
            allowClusterWithVirtGlusterEnabled = value;
            onPropertyChanged(new PropertyChangedEventArgs("AllowClusterWithVirtGlusterEnabled")); //$NON-NLS-1$
        }
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

    private EntityModel<String> glusterHostFingerprint;

    public EntityModel<String> getGlusterHostFingerprint() {
        return glusterHostFingerprint;
    }

    public void setGlusterHostFingerprint(EntityModel<String> glusterHostFingerprint) {
        this.glusterHostFingerprint = glusterHostFingerprint;
    }

    private Boolean isFingerprintVerified;

    public Boolean isFingerprintVerified() {
        return isFingerprintVerified;
    }

    public void setIsFingerprintVerified(Boolean value) {
        this.isFingerprintVerified = value;
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

    private EntityModel<Boolean> privateVersionSupportsCpuThreads;

    public EntityModel<Boolean> getVersionSupportsCpuThreads() {
        return privateVersionSupportsCpuThreads;
    }

    public void setVersionSupportsCpuThreads(EntityModel<Boolean> value) {
        privateVersionSupportsCpuThreads = value;
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

    private SerialNumberPolicyModel serialNumberPolicy;

    public SerialNumberPolicyModel getSerialNumberPolicy() {
        return serialNumberPolicy;
    }

    public void setSerialNumberPolicy(SerialNumberPolicyModel serialNumberPolicy) {
        this.serialNumberPolicy = serialNumberPolicy;
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

    private MigrateOnErrorOptions migrateOnErrorOption = MigrateOnErrorOptions.values()[0];

    public MigrateOnErrorOptions getMigrateOnErrorOption() {
        if (getMigrateOnErrorOption_NO().getEntity() == true) {
            return MigrateOnErrorOptions.NO;
        }
        else if (getMigrateOnErrorOption_YES().getEntity() == true) {
            return MigrateOnErrorOptions.YES;
        }
        else if (getMigrateOnErrorOption_HA_ONLY().getEntity() == true) {
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

    private EntityModel<Boolean> enableOptionalReason;

    public EntityModel<Boolean> getEnableOptionalReason() {
        return enableOptionalReason;
    }

    public void setEnableOptionalReason(EntityModel<Boolean> value) {
        this.enableOptionalReason = value;
    }

    private EntityModel<Boolean> enableHostMaintenanceReason;

    public EntityModel<Boolean> getEnableHostMaintenanceReason() {
        return enableHostMaintenanceReason;
    }

    public void setEnableHostMaintenanceReason(EntityModel<Boolean> value) {
        this.enableHostMaintenanceReason = value;
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
        setMacPoolListModel(new SortedListModel<>(new Linq.SharedMacPoolComparator()));
        setMacPoolModel(new MacPoolModel());
        getMacPoolModel().setIsChangeable(false);
        getMacPoolListModel().getItemsChangedEvent().addListener(this);
        getMacPoolListModel().getSelectedItemChangedEvent().addListener(this);
        startProgress();
        Frontend.getInstance().runQuery(VdcQueryType.GetAllMacPools,
                new VdcQueryParametersBase(),
                new AsyncQuery<>(new AsyncCallback<VdcQueryReturnValue>() {
                    @Override
                    public void onSuccess(VdcQueryReturnValue returnValue) {
                        getMacPoolListModel().setItems((Collection<MacPool>) returnValue.getReturnValue());
                        stopProgress();
                    }
                }));
    }

    public void initTunedProfiles() {
        this.startProgress();
        if (getVersion() == null || getVersion().getSelectedItem() == null) {
            return;
        }
        Version version = getVersion().getSelectedItem();
        Frontend.getInstance().runQuery(VdcQueryType.GetGlusterTunedProfiles, new IdAndNameQueryParameters(null, version.getValue()),
                new AsyncQuery<>(new AsyncCallback<VdcQueryReturnValue>() {
            @Override
            public void onSuccess(VdcQueryReturnValue returnValue) {
                stopProgress();
                List<String> glusterTunedProfiles = new ArrayList<>();
                if (returnValue.getSucceeded()) {
                    glusterTunedProfiles.addAll((List<String>) returnValue.getReturnValue());
                }
                final String oldSelectedProfile = glusterTunedProfile.getSelectedItem();
                glusterTunedProfile.setItems(glusterTunedProfiles);
                glusterTunedProfile.setIsAvailable(glusterTunedProfile.getItems().size() > 0);
                String newSelectedItem = null;
                if (oldSelectedProfile != null) {
                    newSelectedItem =
                            Linq.firstOrNull(glusterTunedProfiles, new Linq.EqualsPredicate(oldSelectedProfile));
                }
                if (newSelectedItem != null) {
                    glusterTunedProfile.setSelectedItem(newSelectedItem);
                } else if (getIsEdit()) {
                    glusterTunedProfile.setSelectedItem(Linq.firstOrNull(glusterTunedProfiles, new Linq.EqualsPredicate(getEntity().getGlusterTunedProfile())));
                }
            }
        }));
    }

    public void init(final boolean isEdit) {
        setIsEdit(isEdit);
        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setComment(new EntityModel<String>());
        setEnableTrustedService(new EntityModel<>(false));
        setEnableHaReservation(new EntityModel<>(false));
        setEnableOptionalReason(new EntityModel<>(false));
        setMigrationPolicies(new ListModel<MigrationPolicy>());
        getMigrationPolicies().getSelectedItemChangedEvent().addListener(this);
        getEnableOptionalReason().setIsAvailable(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        setEnableHostMaintenanceReason(new EntityModel<>(false));
        setAllowClusterWithVirtGlusterEnabled(true);
        setGlusterTunedProfile(new ListModel<String>());
        AsyncDataProvider.getInstance().getAllowClusterWithVirtGlusterEnabled(new AsyncQuery<>(new AsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean returnValue) {
                setAllowClusterWithVirtGlusterEnabled(returnValue);
            }
        }));

        setEnableOvirtService(new EntityModel<Boolean>());
        setEnableGlusterService(new EntityModel<Boolean>());
        setAdditionalClusterFeatures(new ListModel<List<AdditionalFeature>>());
        List<List<AdditionalFeature>> additionalFeatures = new ArrayList<>();
        additionalFeatures.add(Collections.<AdditionalFeature> emptyList());
        getAdditionalClusterFeatures().setItems(additionalFeatures, null);
        setSpiceProxyEnabled(new EntityModel<Boolean>());
        getSpiceProxyEnabled().setEntity(false);
        getSpiceProxyEnabled().getEntityChangedEvent().addListener(this);

        setSpiceProxy(new EntityModel<String>());
        getSpiceProxy().setIsChangeable(false);

        setFencingEnabledModel(new EntityModel<Boolean>());
        getFencingEnabledModel().setEntity(true);
        getFencingEnabledModel().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                 updateFencingPolicyContent(getVersion() == null ? null : getVersion().getSelectedItem());
            }
        });

        setSkipFencingIfSDActiveEnabled(new EntityModel<Boolean>());
        getSkipFencingIfSDActiveEnabled().setEntity(true);

        setSkipFencingIfGlusterBricksUp(new EntityModel<Boolean>());
        getSkipFencingIfGlusterBricksUp().setEntity(false);
        getSkipFencingIfGlusterBricksUp().setIsAvailable(false);
        setSkipFencingIfGlusterQuorumNotMet(new EntityModel<Boolean>());
        getSkipFencingIfGlusterQuorumNotMet().setEntity(false);
        getSkipFencingIfGlusterQuorumNotMet().setIsAvailable(false);

        setSkipFencingIfConnectivityBrokenEnabled(new EntityModel<Boolean>());
        getSkipFencingIfConnectivityBrokenEnabled().setEntity(true);

        setEnableOvirtService(new EntityModel<Boolean>());
        setEnableGlusterService(new EntityModel<Boolean>());

        setSerialNumberPolicy(new SerialNumberPolicyModel());

        setAutoConverge(new ListModel<Boolean>());
        getAutoConverge().setItems(Arrays.asList(null, true, false));
        setMigrateCompressed(new ListModel<Boolean>());
        getMigrateCompressed().setItems(Arrays.asList(null, true, false));

        getEnableOvirtService().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
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
                    }
                    else {
                        getEnableTrustedService().setIsChangeable(false);
                    }

                }
                else {
                    getEnableTrustedService().setIsChangeable(false);
                }
            }
        });
        getEnableOvirtService().setEntity(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        getEnableOvirtService().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.VirtOnly
                && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));

        setRngHwrngSourceRequired(new EntityModel<Boolean>());
        getRngHwrngSourceRequired().setIsAvailable(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));

        initImportCluster(isEdit);

        getEnableGlusterService().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                refreshAdditionalClusterFeaturesList();
                if (!getAllowClusterWithVirtGlusterEnabled() && getEnableGlusterService().getEntity()) {
                    getEnableOvirtService().setEntity(Boolean.FALSE);
                }

                if (!isEdit
                        && getEnableGlusterService().getEntity() != null
                        && getEnableGlusterService().getEntity()) {
                    getIsImportGlusterConfiguration().setIsAvailable(true);
                    getGlusterHostAddress().setIsAvailable(true);
                    getGlusterHostFingerprint().setIsAvailable(true);
                    getGlusterHostPassword().setIsAvailable(true);
                }
                else {
                    getIsImportGlusterConfiguration().setIsAvailable(false);
                    getIsImportGlusterConfiguration().setEntity(false);

                    getGlusterHostAddress().setIsAvailable(false);
                    getGlusterHostFingerprint().setIsAvailable(false);
                    getGlusterHostPassword().setIsAvailable(false);
                }
                if (getEnableGlusterService().getEntity() != null
                        && getEnableGlusterService().getEntity()) {
                    getEnableTrustedService().setEntity(false);
                    getEnableTrustedService().setIsChangeable(false);
                }
                else {
                    if (getEnableOvirtService().getEntity() != null
                            && getEnableOvirtService().getEntity()) {
                        getEnableTrustedService().setIsChangeable(true);
                    }
                    else {
                        getEnableTrustedService().setIsChangeable(false);
                    }
                }

                getGlusterTunedProfile().setIsAvailable(getEnableGlusterService().getEntity());
                updateGlusterFencingPolicyAvailability();
                if (getEnableGlusterService().getEntity()) {
                    initTunedProfiles();
                }
            }
       });

        getEnableTrustedService().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (getEnableTrustedService().getEntity() != null
                        && getEnableTrustedService().getEntity()) {
                    getEnableGlusterService().setEntity(false);
                    getEnableGlusterService().setIsChangeable(false);
                }
                else {
                    getEnableGlusterService().setIsChangeable(true);
                }
            }
        });

        getEnableGlusterService().setEntity(ApplicationModeHelper.getUiMode() == ApplicationMode.GlusterOnly);
        getEnableGlusterService().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly
                && ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly));

        getGlusterTunedProfile().setIsAvailable(getEnableGlusterService().getEntity());

        setOptimizationNone(new EntityModel<Integer>());
        setOptimizationForServer(new EntityModel<Integer>());
        setOptimizationForDesktop(new EntityModel<Integer>());
        setOptimizationCustom(new EntityModel<Integer>());

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
        setEnableKsm(new EntityModel<Boolean>());
        getEnableKsm().setEntity(false);
        getKsmPolicyForNumaSelection().setIsChangeable(false);
        getEnableKsm().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (getEnableKsm().getEntity() == null){
                    return;
                }
                if (getEnableKsm().getEntity() == true){
                    getKsmPolicyForNumaSelection().setIsChangeable(true);
                }
                if (getEnableKsm().getEntity() == false){
                    getKsmPolicyForNumaSelection().setIsChangeable(false);
                }
            }
        });

        setEnableBallooning(new EntityModel<Boolean>());
        getEnableBallooning().setEntity(false);
        // Optimization methods:
        // default value =100;
        setDefaultMemoryOvercommit(AsyncDataProvider.getInstance().getClusterDefaultMemoryOverCommit());

        setCountThreadsAsCores(new EntityModel<>(AsyncDataProvider.getInstance().getClusterDefaultCountThreadsAsCores()));

        setVersionSupportsCpuThreads(new EntityModel<>(true));

        setOptimizeForUtilization(new EntityModel<Boolean>());
        setOptimizeForSpeed(new EntityModel<Boolean>());
        getOptimizeForUtilization().setEntity(true);
        getOptimizeForSpeed().setEntity(false);
        getOptimizeForUtilization().getEntityChangedEvent().addListener(this);
        getOptimizeForSpeed().getEntityChangedEvent().addListener(this);

        setGuarantyResources(new EntityModel<Boolean>());
        setAllowOverbooking(new EntityModel<Boolean>());
        getGuarantyResources().setEntity(true);
        getAllowOverbooking().setEntity(false);
        getAllowOverbooking().getEntityChangedEvent().addListener(this);
        getGuarantyResources().getEntityChangedEvent().addListener(this);

        boolean overbookingSupported = AsyncDataProvider.getInstance().getScheudulingAllowOverbookingSupported();
        getAllowOverbooking().setIsAvailable(overbookingSupported);
        if (overbookingSupported) {
            getOptimizeForSpeed().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    Boolean entity = getOptimizeForSpeed().getEntity();
                    if (entity) {
                        getGuarantyResources().setEntity(true);
                    }
                    getAllowOverbooking().setIsChangeable(!entity);
                }
            });
            getAllowOverbooking().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    Boolean entity = getAllowOverbooking().getEntity();
                    if (entity) {
                        getOptimizeForUtilization().setEntity(true);
                    }
                    getOptimizeForSpeed().setIsChangeable(!entity);
                }
            });
        }

        setHostsWithBrokenConnectivityThreshold(new ListModel<Integer>());
        getHostsWithBrokenConnectivityThreshold().setIsAvailable(true);
        getHostsWithBrokenConnectivityThreshold().getSelectedItemChangedEvent().addListener(this);
        initHostsWithBrokenConnectivityThreshold();

        AsyncDataProvider.getInstance().getClusterDesktopMemoryOverCommit(new AsyncQuery<>(new AsyncCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                setDesktopOverCommit(result);
                AsyncDataProvider.getInstance().getClusterServerMemoryOverCommit(new AsyncQuery<>(new AsyncCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        setServerOverCommit(result);

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

                    }
                }));
            }
        }));

        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);

        setArchitecture(new ListModel<ArchitectureType>());
        getArchitecture().setIsAvailable(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));

        setManagementNetwork(new ListModel<Network>());
        if (isEdit && !isClusterDetached()) {
            getManagementNetwork().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getConstants()
                    .prohibitManagementNetworkChangeInEditClusterInfoMessage());
            getManagementNetwork().setIsChangeable(false);
        }

        setCPU(new FilteredListModel<ServerCpu>());
        getCPU().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getCPU().getSelectedItemChangedEvent().addListener(this);

        setVersion(new ListModel<Version>());
        getVersion().getSelectedItemChangedEvent().addListener(this);
        setMigrateOnErrorOption(MigrateOnErrorOptions.YES);

        setSwitchType(new ListModel<SwitchType>());
        initSwitchType();


        getRngHwrngSourceRequired().setEntity(false);

        setValidTab(TabName.GENERAL_TAB, true);
        setMigrationTabAvailable(true);

        setClusterPolicy(new ListModel<ClusterPolicy>());
        setCustomPropertySheet(new KeyValueModel());
        getClusterPolicy().getSelectedItemChangedEvent().addListener(this);
        Frontend.getInstance().runQuery(VdcQueryType.GetAllPolicyUnits, new VdcQueryParametersBase(), new AsyncQuery<>(
                new AsyncCallback<VdcQueryReturnValue>() {

                    @Override
                    public void onSuccess(VdcQueryReturnValue returnValue) {
                        ArrayList<PolicyUnit> policyUnits = returnValue.getReturnValue();
                        policyUnitMap = new LinkedHashMap<>();
                        for (PolicyUnit policyUnit : policyUnits) {
                            policyUnitMap.put(policyUnit.getId(), policyUnit);
                        }
                        Frontend.getInstance().runQuery(VdcQueryType.GetClusterPolicies,
                                new VdcQueryParametersBase(),
                                new AsyncQuery<>(
                                        new AsyncCallback<VdcQueryReturnValue>() {

                                            @Override
                                            public void onSuccess(VdcQueryReturnValue returnValue) {
                                                ArrayList<ClusterPolicy> list = returnValue.getReturnValue();
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
                                            }
                                        }));
                    }
                }));
        setCustomMigrationNetworkBandwidth(new EntityModel<Integer>());
        setMigrationBandwidthLimitType(new ListModel<MigrationBandwidthLimitType>());
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
        Version version = getEffectiveVersion();

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

        getMigrationPolicies().setSelectedItem(migrationPolicy != null ? migrationPolicy :
                findMigrationPolicyById(NoMigrationPolicy.ID, policies));

        getMigrationPolicies().updateChangeability(ConfigurationValues.MigrationPoliciesSupported, version);
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

    private void initSwitchType() {
        ListModel<SwitchType> switchType = getSwitchType();

        switchType.setItems(Arrays.asList(SwitchType.values()));
        switchType.setIsChangeable(false);
        switchType.setSelectedItem(SwitchType.LEGACY);
    }

    private void updateSwitchTypeUponVersionChange(Version version) {
        ListModel<SwitchType> switchType = getSwitchType();

        boolean ovsSupported = isOvsSupported(version);
        switchType.setIsChangeable(ovsSupported);
        if (!ovsSupported && switchType.getSelectedItem().equals(SwitchType.OVS)) {
            switchType.setSelectedItem(SwitchType.LEGACY);
        }
    }

    private boolean isOvsSupported(Version version) {
        AsyncDataProvider instance = AsyncDataProvider.getInstance();
        return (Boolean) instance.getConfigValuePreConverted(ConfigurationValues.OvsSupported, version.getValue());
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

    private void initImportCluster(boolean isEdit) {
        setGlusterHostAddress(new EntityModel<String>());
        getGlusterHostAddress().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                setIsFingerprintVerified(false);
                if (getGlusterHostAddress().getEntity() == null
                        || getGlusterHostAddress().getEntity().trim().length() == 0) {
                    getGlusterHostFingerprint().setEntity(""); //$NON-NLS-1$
                    return;
                }
                fetchFingerprint(getGlusterHostAddress().getEntity());
            }
        });

        setGlusterHostFingerprint(new EntityModel<String>());
        getGlusterHostFingerprint().setEntity(""); //$NON-NLS-1$
        setIsFingerprintVerified(false);
        setGlusterHostPassword(new EntityModel<String>());

        setIsImportGlusterConfiguration(new EntityModel<Boolean>());
        getIsImportGlusterConfiguration().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (getIsImportGlusterConfiguration().getEntity() != null
                        && getIsImportGlusterConfiguration().getEntity()) {
                    getGlusterHostAddress().setIsChangeable(true);
                    getGlusterHostPassword().setIsChangeable(true);
                }
                else {
                    getGlusterHostAddress().setIsChangeable(false);
                    getGlusterHostPassword().setIsChangeable(false);
                }
            }
        });

        getIsImportGlusterConfiguration().setIsAvailable(false);
        getGlusterHostAddress().setIsAvailable(false);
        getGlusterHostFingerprint().setIsAvailable(false);
        getGlusterHostPassword().setIsAvailable(false);

        getIsImportGlusterConfiguration().setEntity(false);
    }

    private void fetchFingerprint(String hostAddress) {
        AsyncDataProvider.getInstance().getHostFingerprint(new AsyncQuery<>(new AsyncCallback<String>() {
            @Override
            public void onSuccess(String fingerprint) {
                if (fingerprint != null && fingerprint.length() > 0) {
                    getGlusterHostFingerprint().setEntity(fingerprint);
                    setIsFingerprintVerified(true);
                }
                else {
                    getGlusterHostFingerprint().setEntity(ConstantsManager.getInstance()
                            .getConstants()
                            .errorLoadingFingerprint());
                    setIsFingerprintVerified(false);
                }
            }
        }), hostAddress);
        getGlusterHostFingerprint().setEntity(ConstantsManager.getInstance().getConstants().loadingFingerprint());
    }

    private void postInit() {
        getDescription().setEntity(getEntity().getDescription());
        getComment().setEntity(getEntity().getComment());

        initSpiceProxy();
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
        getEnableBallooning().setEntity(getEntity().isEnableBallooning());
        getEnableKsm().setEntity(getEntity().isEnableKsm());

        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(new AsyncCallback<List<StoragePool>>() {
            @Override
            public void onSuccess(List<StoragePool> dataCenters) {
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
            }
        }));
        // inactive KsmPolicyForNuma if KSM disabled
        if (getEnableKsm().getEntity() == false) {
            getKsmPolicyForNumaSelection().setIsChangeable(false);
        }
    }

    private void loadCurrentClusterManagementNetwork() {
        AsyncDataProvider.getInstance().getManagementNetwork(new AsyncQuery<>(new AsyncCallback<Network>() {
            @Override
            public void onSuccess(Network managementNetwork1) {
                getManagementNetwork().setSelectedItem(managementNetwork1);
            }
        }), getEntity().getId());
    }

    private void loadDcNetworks(final Guid dataCenterId) {
        if (dataCenterId == null) {
            return;
        }
        final AsyncQuery getAllDataCenterNetworksQuery = new AsyncQuery<>(new AsyncCallback<List<Network>>() {
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
                    final AsyncQuery getDefaultManagementNetworkQuery =
                            new AsyncQuery<>(new AsyncCallback<Network>() {
                                @Override
                                public void onSuccess(Network defaultManagementNetwork) {
                                    defaultManagementNetworkCache.put(dataCenterId, defaultManagementNetwork);
                                    setSelectedDefaultManagementNetwork(defaultManagementNetwork);
                                }
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
            handleSelectedItemChangedEventDefinition(sender, args);
        } else if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
            handleEntityChangedEventDefinition((EntityModel) sender);
        }
    }

    private void handleItemsChangedEventDefinition(Object sender) {
        if (sender == getMacPoolListModel()) {
            initSelectedMacPool();
        }
    }

    private void handleEntityChangedEventDefinition(EntityModel senderEntityModel) {
        if (senderEntityModel == getSpiceProxyEnabled()) {
            getSpiceProxy().setIsChangeable(getSpiceProxyEnabled().getEntity());
        } else if ((Boolean) senderEntityModel.getEntity()) {
            if (senderEntityModel == getOptimizationNone_IsSelected()) {
                getOptimizationForServer_IsSelected().setEntity(false);
                getOptimizationForDesktop_IsSelected().setEntity(false);
                getOptimizationCustom_IsSelected().setEntity(false);
            }
            else if (senderEntityModel == getOptimizationForServer_IsSelected()) {
                getOptimizationNone_IsSelected().setEntity(false);
                getOptimizationForDesktop_IsSelected().setEntity(false);
                getOptimizationCustom_IsSelected().setEntity(false);
            }
            else if (senderEntityModel == getOptimizationForDesktop_IsSelected()) {
                getOptimizationNone_IsSelected().setEntity(false);
                getOptimizationForServer_IsSelected().setEntity(false);
                getOptimizationCustom_IsSelected().setEntity(false);
            }
            else if (senderEntityModel == getOptimizationCustom_IsSelected()) {
                getOptimizationNone_IsSelected().setEntity(false);
                getOptimizationForServer_IsSelected().setEntity(false);
                getOptimizationForDesktop_IsSelected().setEntity(false);
            }
            else if (senderEntityModel == getMigrateOnErrorOption_YES()) {
                getMigrateOnErrorOption_NO().setEntity(false);
                getMigrateOnErrorOption_HA_ONLY().setEntity(false);
            }
            else if (senderEntityModel == getMigrateOnErrorOption_NO()) {
                getMigrateOnErrorOption_YES().setEntity(false);
                getMigrateOnErrorOption_HA_ONLY().setEntity(false);
            }
            else if (senderEntityModel == getMigrateOnErrorOption_HA_ONLY()) {
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

    private void handleSelectedItemChangedEventDefinition(Object sender, EventArgs args) {
        if (sender == getDataCenter()) {
            storagePool_SelectedItemChanged(args);
        } else if (sender == getVersion()) {
            version_SelectedItemChanged(args);
        } else if (sender == getClusterPolicy()) {
            clusterPolicyChanged();
        } else if (sender == getCPU()) {
            CPU_SelectedItemChanged(args);
        } else if (sender == getArchitecture()) {
            architectureSelectedItemChanged(args);
        } else if (sender == getMacPoolListModel()) {
            getMacPoolModel().setEntity(getMacPoolListModel().getSelectedItem());
        }  else if (sender == getMigrationPolicies()) {
            migrationPoliciesChanged();
        }
    }

    private void migrationPoliciesChanged() {
        boolean hasMigrationPolicy = getMigrationPolicies().getSelectedItem() != null
                && !NoMigrationPolicy.ID.equals(getMigrationPolicies().getSelectedItem().getId());

        UIConstants constants = ConstantsManager.getInstance().getConstants();
        getAutoConverge().setIsChangeable(!hasMigrationPolicy, constants.availableOnlyWithLegacyPolicy());
        getMigrateCompressed().setIsChangeable(!hasMigrationPolicy, constants.availableOnlyWithLegacyPolicy());
    }

    private void architectureSelectedItemChanged(EventArgs args) {
        filterCpuTypeByArchitecture();
    }

    private void filterCpuTypeByArchitecture() {
        final ArchitectureType selectedArchitecture = getArchitecture().getSelectedItem();
        final FilteredListModel.Filter<ServerCpu> filter = selectedArchitecture == null
                || selectedArchitecture.equals(ArchitectureType.undefined)
                ? null
                : new FilteredListModel.Filter<ServerCpu>() {

                    @Override
                    public boolean filter(ServerCpu cpu) {
                        final ArchitectureType cpuArchitecture = cpu.getArchitecture();
                        final boolean showCpu = selectedArchitecture.equals(cpuArchitecture);
                        return showCpu;
                }
        };
        getCPU().filterItems(filter);
    }

    private void CPU_SelectedItemChanged(EventArgs args) {
        updateMigrateOnError();
    }

    private void version_SelectedItemChanged(EventArgs e) {
        Version version = getEffectiveVersion();

        AsyncDataProvider.getInstance().getCPUList(new AsyncQuery<>(new AsyncCallback<List<ServerCpu>>() {
            @Override
            public void onSuccess(final List<ServerCpu> cpus) {
                if (getIsEdit()) {

                    AsyncDataProvider.getInstance().isClusterEmpty(new AsyncQuery<>(new AsyncCallback<Boolean>() {

                        @Override
                        public void onSuccess(Boolean isEmpty) {
                            if (isEmpty) {
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
                        }
                    }), getEntity().getId());
                } else {
                    populateCPUList(cpus, true);
                }
            }
        }), version);

        getVersionSupportsCpuThreads().setEntity(true);
        getEnableBallooning().setChangeProhibitionReason(ConstantsManager.getInstance().getConstants().ballooningNotAvailable());
        getEnableBallooning().setIsChangeable(true);

        setRngSourcesCheckboxes(version);

        updateSwitchTypeUponVersionChange(version);

        updateFencingPolicyContent(version);

        updateKSMPolicy();

        updateMigrateOnError();

        refreshMigrationPolicies();

        refreshAdditionalClusterFeaturesList();

        if (getEnableGlusterService().getEntity()) {
            initTunedProfiles();
        }

    }

    private void updateKSMPolicy() {
        getEnableKsm().setIsChangeable(true);
        getEnableKsm().setChangeProhibitionReason(ConstantsManager.getInstance().getConstants().ksmNotAvailable());

        getKsmPolicyForNumaSelection().setIsAvailable(true);
        getKsmPolicyForNumaSelection().setChangeProhibitionReason(ConstantsManager.getInstance()
                .getConstants()
                .ksmWithNumaAwarnessNotAvailable());
        // enable NUMA aware KSM by default (matching kernel's default)
        setKsmPolicyForNuma(true);
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
        AsyncDataProvider.getInstance().getClusterFeaturesByVersionAndCategory(new AsyncQuery<>(new AsyncCallback<Set<AdditionalFeature>>() {
            @Override
            public void onSuccess(final Set<AdditionalFeature> features) {
                stopProgress();
                // Get the additional features which are already enabled for cluster. Applicable only in case of edit
                // cluster
                if (getIsEdit() && !features.isEmpty()) {
                    startProgress();
                    AsyncDataProvider.getInstance().getClusterFeaturesByClusterId(new AsyncQuery<>(new AsyncCallback<Set<SupportedAdditionalClusterFeature>>() {
                        @Override
                        public void onSuccess(Set<SupportedAdditionalClusterFeature> clusterFeatures) {
                            stopProgress();
                            Set<AdditionalFeature> featuresEnabled = new HashSet<>();
                            for (SupportedAdditionalClusterFeature feature : clusterFeatures) {
                                if (feature.isEnabled()) {
                                    featuresEnabled.add(feature.getFeature());
                                }
                            }
                            updateAddtionClusterFeatureList(features, featuresEnabled);
                        }
                    }), getEntity().getId());
                } else {
                    updateAddtionClusterFeatureList(features,
                            Collections.<AdditionalFeature> emptySet());
                }
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
                getArchitecture().setItems(new ArrayList<>(Arrays.asList(getEntity().getArchitecture())));
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

        // enable CPU Architecture-Type filtering
        initCpuArchTypeFiltering();
    }

    private void initCpuArchTypeFiltering() {
        filterCpuTypeByArchitecture();
        getArchitecture().getSelectedItemChangedEvent().addListener(this);
    }

    private void initSupportedArchitectures() {
        Collection<ArchitectureType> archsWithSupportingCpus = new HashSet<>();
        archsWithSupportingCpus.add(ArchitectureType.undefined);
        for (ServerCpu cpu: getCPU().getItems()) {
            archsWithSupportingCpus.add(cpu.getArchitecture());
        }
        getArchitecture().setItems(archsWithSupportingCpus);
    }

    private void initCPU() {
        if (!isCPUinitialized && getIsEdit()) {
            isCPUinitialized = true;
            getCPU().setSelectedItem(null);
            for (ServerCpu a : getCPU().getItems()) {
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

    private void storagePool_SelectedItemChanged(EventArgs e) {
        // possible versions for new cluster (when editing cluster, this event won't occur)
        // are actually the possible versions for the data-center that the cluster is going
        // to be attached to.
        final StoragePool selectedDataCenter = getDataCenter().getSelectedItem();
        if (selectedDataCenter == null) {
            getManagementNetwork().setItems(Collections.<Network>emptyList());
            return;
        }
        if (selectedDataCenter.isLocal()) {
            setMigrationTabAvailable(false);
        }
        else {
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
                        return Linq.selectHighestVersion(versions);
                    } else {
                        return selectedDataCenter.getCompatibilityVersion();
                    }
                } else if (getIsEdit()) {
                    return Linq.firstOrNull(versions,
                            new Linq.EqualsPredicate(getEntity().getCompatibilityVersion()));
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

    public boolean validate(boolean validateCpu) {
        return validate(true, validateCpu, true);
    }

    public boolean validate(boolean validateStoragePool, boolean validateCpu, boolean validateCustomProperties) {
        validateName();

        if (validateStoragePool) {
            getDataCenter().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        if (validateCpu) {
            validateCPU();
        }
        else {
            getCPU().validateSelectedItem(new IValidation[] {});
        }

        if (validateCustomProperties) {
            getCustomPropertySheet().setIsValid(getCustomPropertySheet().validate());
        }
        setValidTab(TabName.CLUSTER_POLICY_TAB, getCustomPropertySheet().getIsValid());

        final IValidation[] versionValidations = new IValidation[] { new NotEmptyValidation() };
        getVersion().validateSelectedItem(versionValidations);

        getManagementNetwork().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getRngHwrngSourceRequired().setIsValid(true);

        boolean validService = true;
        if (getEnableOvirtService().getIsAvailable() && getEnableGlusterService().getIsAvailable()) {
            validService = getEnableOvirtService().getEntity()
                            || getEnableGlusterService().getEntity();
        }

        getGlusterHostAddress().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getGlusterHostPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });

        if (!validService) {
            setMessage(ConstantsManager.getInstance().getConstants().clusterServiceValidationMsg());
        }
        else if (getIsImportGlusterConfiguration().getEntity() && getGlusterHostAddress().getIsValid()
                && getGlusterHostPassword().getIsValid()
                && !isFingerprintVerified()) {
            setMessage(ConstantsManager.getInstance().getConstants().fingerprintNotVerified());
        }
        else {
            setMessage(null);
        }

        if (getSpiceProxyEnabled().getEntity()) {
            getSpiceProxy().validateEntity(new IValidation[] { new HostWithProtocolAndPortAddressValidation() });
        } else {
            getSpiceProxy().setIsValid(true);
        }
        setValidTab(TabName.CONSOLE_TAB, getSpiceProxy().getIsValid());

        if (getSerialNumberPolicy().getSelectedSerialNumberPolicy() == SerialNumberPolicy.CUSTOM) {
            getSerialNumberPolicy().getCustomSerialNumber().validateEntity(new IValidation[] { new NotEmptyValidation() });
        } else {
            getSerialNumberPolicy().getCustomSerialNumber().setIsValid(true);
        }

        getMacPoolModel().validate();

        boolean generalTabValid = getName().getIsValid() && getDataCenter().getIsValid() && getCPU().getIsValid()
                && getManagementNetwork().getIsValid()
                && getVersion().getIsValid() && validService && getGlusterHostAddress().getIsValid()
                && getRngHwrngSourceRequired().getIsValid()
                && getGlusterHostPassword().getIsValid()
                && (getIsImportGlusterConfiguration().getEntity() ? (getGlusterHostAddress().getIsValid()
                && getGlusterHostPassword().getIsValid()
                && getSerialNumberPolicy().getCustomSerialNumber().getIsValid()
                && isFingerprintVerified()) : true);
        setValidTab(TabName.GENERAL_TAB, generalTabValid);

        if (getVersion().getSelectedItem() != null) {
            if (AsyncDataProvider.getInstance().isMigrationPoliciesSupported(getVersion().getSelectedItem())
                    && MigrationBandwidthLimitType.CUSTOM.equals(getMigrationBandwidthLimitType().getSelectedItem())) {
                getCustomMigrationNetworkBandwidth().validateEntity(
                        new IValidation[] { new NotNullIntegerValidation(0, Integer.MAX_VALUE) });
            } else {
                getCustomMigrationNetworkBandwidth().setIsValid(true);
            }
        }
        final boolean migrationTabValid =
                getMigrationBandwidthLimitType().getIsValid() && getCustomMigrationNetworkBandwidth().getIsValid();
        setValidTab(TabName.MIGRATION_TAB, migrationTabValid);

        boolean macPoolTabValid = getMacPoolModel().getIsValid();
        setValidTab(TabName.MAC_POOL_TAB, macPoolTabValid);

        ValidationCompleteEvent.fire(getEventBus(), this);
        return generalTabValid && macPoolTabValid && getCustomPropertySheet().getIsValid() && getSpiceProxy().getIsValid() && migrationTabValid;
    }

    public void validateName() {
        getName().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(40),
                new I18NNameValidation() });
    }

    public void validateCPU() {
        getCPU().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
    }

    private String defaultClusterRngSourcesCsv(Version ver) {
        String srcs = (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.ClusterRequiredRngSourcesDefault, ver.toString());
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
        if (ksmPolicyForNumaFlag == false) {
            ksmPolicyForNuma = KsmPolicyForNuma.shareInsideEachNumaNode;
        }

        getKsmPolicyForNumaSelection().setSelectedItem(ksmPolicyForNuma);
        return;
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
