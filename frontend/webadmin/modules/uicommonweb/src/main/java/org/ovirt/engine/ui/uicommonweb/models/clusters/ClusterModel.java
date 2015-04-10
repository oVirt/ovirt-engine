package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SerialNumberPolicyModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostWithProtocolAndPortAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class ClusterModel extends EntityModel<VDSGroup>
{
    private Map<Guid, PolicyUnit> policyUnitMap;
    private ListModel<ClusterPolicy> clusterPolicy;

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

    public int getServerOverCommit()
    {
        return privateServerOverCommit;
    }

    public void setServerOverCommit(int value)
    {
        privateServerOverCommit = value;
    }

    private int privateDesktopOverCommit;

    public int getDesktopOverCommit()
    {
        return privateDesktopOverCommit;
    }

    public void setDesktopOverCommit(int value)
    {
        privateDesktopOverCommit = value;
    }

    private int privateDefaultMemoryOvercommit;

    public int getDefaultMemoryOvercommit()
    {
        return privateDefaultMemoryOvercommit;
    }

    public void setDefaultMemoryOvercommit(int value)
    {
        privateDefaultMemoryOvercommit = value;
    }

    private boolean privateIsEdit;

    public boolean getIsEdit()
    {
        return privateIsEdit;
    }

    public void setIsEdit(boolean value)
    {
        privateIsEdit = value;
    }

    private boolean isCPUinitialized = false;

    private boolean privateIsNew;

    public boolean getIsNew()
    {
        return privateIsNew;
    }

    public void setIsNew(boolean value)
    {
        privateIsNew = value;
    }

    private String privateOriginalName;

    public String getOriginalName()
    {
        return privateOriginalName;
    }

    public void setOriginalName(String value)
    {
        privateOriginalName = value;
    }

    private Guid privateClusterId;

    public Guid getClusterId()
    {
        return privateClusterId;
    }

    public void setClusterId(Guid value)
    {
        privateClusterId = value;
    }

    private EntityModel<String> privateName;

    public EntityModel<String> getName()
    {
        return privateName;
    }

    public void setName(EntityModel<String> value)
    {
        privateName = value;
    }

    private EntityModel<String> privateDescription;

    public EntityModel<String> getDescription()
    {
        return privateDescription;
    }

    public void setDescription(EntityModel<String> value)
    {
        privateDescription = value;
    }

    private EntityModel<String> privateComment;

    public EntityModel<String> getComment()
    {
        return privateComment;
    }

    public void setComment(EntityModel<String> value)
    {
        privateComment = value;
    }

    private ListModel<StoragePool> privateDataCenter;

    public ListModel<StoragePool> getDataCenter()
    {
        return privateDataCenter;
    }

    public void setDataCenter(ListModel<StoragePool> value)
    {
        privateDataCenter = value;
    }

    private ListModel<ServerCpu> privateCPU;

    public ListModel<ServerCpu> getCPU()
    {
        return privateCPU;
    }

    public void setCPU(ListModel<ServerCpu> value)
    {
        privateCPU = value;
    }

    private EntityModel<Boolean> rngRandomSourceRequired;

    public EntityModel<Boolean> getRngRandomSourceRequired() {
        return rngRandomSourceRequired;
    }

    public void setRngRandomSourceRequired(EntityModel<Boolean> rngRandomSourceRequired) {
        this.rngRandomSourceRequired = rngRandomSourceRequired;
    }

    private EntityModel<Boolean> rngHwrngSourceRequired;

    public EntityModel<Boolean> getRngHwrngSourceRequired() {
        return rngHwrngSourceRequired;
    }

    public void setRngHwrngSourceRequired(EntityModel<Boolean> rngHwrngSourceRequired) {
        this.rngHwrngSourceRequired = rngHwrngSourceRequired;
    }

    private ListModel<Version> privateVersion;

    public ListModel<Version> getVersion()
    {
        return privateVersion;
    }

    public void setVersion(ListModel<Version> value)
    {
        privateVersion = value;
    }

    private ListModel<ArchitectureType> privateArchitecture;

    public ListModel<ArchitectureType> getArchitecture()
    {
        return privateArchitecture;
    }

    public void setArchitecture(ListModel<ArchitectureType> value)
    {
        privateArchitecture = value;
    }

    private boolean allowClusterWithVirtGlusterEnabled;

    public boolean getAllowClusterWithVirtGlusterEnabled()
    {
        return allowClusterWithVirtGlusterEnabled;
    }

    public void setAllowClusterWithVirtGlusterEnabled(boolean value)
    {
        allowClusterWithVirtGlusterEnabled = value;
        if (allowClusterWithVirtGlusterEnabled != value)
        {
            allowClusterWithVirtGlusterEnabled = value;
            onPropertyChanged(new PropertyChangedEventArgs("AllowClusterWithVirtGlusterEnabled")); //$NON-NLS-1$
        }
    }

    private EntityModel<Boolean> privateEnableOvirtService;

    public EntityModel<Boolean> getEnableOvirtService()
    {
        return privateEnableOvirtService;
    }

    public void setEnableOvirtService(EntityModel<Boolean> value)
    {
        this.privateEnableOvirtService = value;
    }

    private EntityModel<Boolean> privateEnableGlusterService;

    public EntityModel<Boolean> getEnableGlusterService() {
        return privateEnableGlusterService;
    }

    public void setEnableGlusterService(EntityModel<Boolean> value) {
        this.privateEnableGlusterService = value;
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

    public EntityModel<Integer> getOptimizationNone()
    {
        return privateOptimizationNone;
    }

    public void setOptimizationNone(EntityModel<Integer> value)
    {
        privateOptimizationNone = value;
    }

    private EntityModel<Integer> privateOptimizationForServer;

    public EntityModel<Integer> getOptimizationForServer()
    {
        return privateOptimizationForServer;
    }

    public void setOptimizationForServer(EntityModel<Integer> value)
    {
        privateOptimizationForServer = value;
    }

    private EntityModel<Integer> privateOptimizationForDesktop;

    public EntityModel<Integer> getOptimizationForDesktop()
    {
        return privateOptimizationForDesktop;
    }

    public void setOptimizationForDesktop(EntityModel<Integer> value)
    {
        privateOptimizationForDesktop = value;
    }

    private EntityModel<Integer> privateOptimizationCustom;

    public EntityModel<Integer> getOptimizationCustom()
    {
        return privateOptimizationCustom;
    }

    public void setOptimizationCustom(EntityModel<Integer> value)
    {
        privateOptimizationCustom = value;
    }

    private EntityModel<Boolean> privateOptimizationNone_IsSelected;

    public EntityModel<Boolean> getOptimizationNone_IsSelected()
    {
        return privateOptimizationNone_IsSelected;
    }

    public void setOptimizationNone_IsSelected(EntityModel<Boolean> value)
    {
        privateOptimizationNone_IsSelected = value;
    }

    private EntityModel<Boolean> privateOptimizationForServer_IsSelected;

    public EntityModel<Boolean> getOptimizationForServer_IsSelected()
    {
        return privateOptimizationForServer_IsSelected;
    }

    public void setOptimizationForServer_IsSelected(EntityModel<Boolean> value)
    {
        privateOptimizationForServer_IsSelected = value;
    }

    private EntityModel<Boolean> privateOptimizationForDesktop_IsSelected;

    public EntityModel<Boolean> getOptimizationForDesktop_IsSelected()
    {
        return privateOptimizationForDesktop_IsSelected;
    }

    public void setOptimizationForDesktop_IsSelected(EntityModel<Boolean> value)
    {
        privateOptimizationForDesktop_IsSelected = value;
    }

    private EntityModel<Boolean> privateOptimizationCustom_IsSelected;

    public EntityModel<Boolean> getOptimizationCustom_IsSelected()
    {
        return privateOptimizationCustom_IsSelected;
    }

    public void setOptimizationCustom_IsSelected(EntityModel<Boolean> value)
    {
        privateOptimizationCustom_IsSelected = value;
    }

    private EntityModel<Boolean> privateCountThreadsAsCores;

    public EntityModel<Boolean> getCountThreadsAsCores()
    {
        return privateCountThreadsAsCores;
    }

    public void setCountThreadsAsCores(EntityModel<Boolean> value)
    {
        privateCountThreadsAsCores = value;
    }

    private EntityModel<Boolean> privateVersionSupportsCpuThreads;

    public EntityModel<Boolean> getVersionSupportsCpuThreads()
    {
        return privateVersionSupportsCpuThreads;
    }

    public void setVersionSupportsCpuThreads(EntityModel<Boolean> value)
    {
        privateVersionSupportsCpuThreads = value;
    }

    private EntityModel<Boolean> privateMigrateOnErrorOption_NO;

    public EntityModel<Boolean> getMigrateOnErrorOption_NO()
    {
        return privateMigrateOnErrorOption_NO;
    }

    public void setMigrateOnErrorOption_NO(EntityModel<Boolean> value)
    {
        privateMigrateOnErrorOption_NO = value;
    }

    private EntityModel<Boolean> privateMigrateOnErrorOption_YES;

    public EntityModel<Boolean> getMigrateOnErrorOption_YES()
    {
        return privateMigrateOnErrorOption_YES;
    }

    public void setMigrateOnErrorOption_YES(EntityModel<Boolean> value)
    {
        privateMigrateOnErrorOption_YES = value;
    }

    private EntityModel<Boolean> privateMigrateOnErrorOption_HA_ONLY;

    public EntityModel<Boolean> getMigrateOnErrorOption_HA_ONLY()
    {
        return privateMigrateOnErrorOption_HA_ONLY;
    }

    public void setMigrateOnErrorOption_HA_ONLY(EntityModel<Boolean> value)
    {
        privateMigrateOnErrorOption_HA_ONLY = value;
    }

    private EntityModel<Boolean> enableKsm;

    public EntityModel<Boolean> getEnableKsm() {
        return enableKsm;
    }

    public void setEnableKsm(EntityModel<Boolean> enableKsm) {
        this.enableKsm = enableKsm;
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

    private boolean isGeneralTabValid;

    public boolean getIsGeneralTabValid()
    {
        return isGeneralTabValid;
    }

    public void setIsGeneralTabValid(boolean value)
    {
        if (isGeneralTabValid != value)
        {
            isGeneralTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid")); //$NON-NLS-1$
        }
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

    public MigrateOnErrorOptions getMigrateOnErrorOption()
    {
        if (getMigrateOnErrorOption_NO().getEntity() == true)
        {
            return MigrateOnErrorOptions.NO;
        }
        else if (getMigrateOnErrorOption_YES().getEntity() == true)
        {
            return MigrateOnErrorOptions.YES;
        }
        else if (getMigrateOnErrorOption_HA_ONLY().getEntity() == true)
        {
            return MigrateOnErrorOptions.HA_ONLY;
        }
        return MigrateOnErrorOptions.YES;
    }

    public void setMigrateOnErrorOption(MigrateOnErrorOptions value)
    {
        if (migrateOnErrorOption != value)
        {
            migrateOnErrorOption = value;

            // webadmin use.
            switch (migrateOnErrorOption)
            {
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

    private boolean privateisResiliencePolicyTabAvailable;

    public boolean getisResiliencePolicyTabAvailable()
    {
        return privateisResiliencePolicyTabAvailable;
    }

    public void setisResiliencePolicyTabAvailable(boolean value)
    {
        privateisResiliencePolicyTabAvailable = value;
    }

    public boolean getIsResiliencePolicyTabAvailable()
    {
        return getisResiliencePolicyTabAvailable();
    }

    public void setIsResiliencePolicyTabAvailable(boolean value)
    {
        if (getisResiliencePolicyTabAvailable() != value)
        {
            setisResiliencePolicyTabAvailable(value);
            onPropertyChanged(new PropertyChangedEventArgs("IsResiliencePolicyTabAvailable")); //$NON-NLS-1$
        }
    }

    private EntityModel<Boolean> enableOptionalReason;

    public EntityModel<Boolean> getEnableOptionalReason() {
        return enableOptionalReason;
    }

    public void setEnableOptionalReason(EntityModel<Boolean> value) {
        this.enableOptionalReason = value;
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

    public int getMemoryOverCommit()
    {
        if (getOptimizationNone_IsSelected().getEntity())
        {
            return getOptimizationNone().getEntity();
        }

        if (getOptimizationForServer_IsSelected().getEntity())
        {
            return getOptimizationForServer().getEntity();
        }

        if (getOptimizationForDesktop_IsSelected().getEntity())
        {
            return getOptimizationForDesktop().getEntity();
        }

        if (getOptimizationCustom_IsSelected().getEntity())
        {
            return getOptimizationCustom().getEntity();
        }

        return AsyncDataProvider.getClusterDefaultMemoryOverCommit();
    }

    public String getSchedulerOptimizationInfoMessage() {
        return ConstantsManager.getInstance()
                .getMessages()
                .schedulerOptimizationInfo(AsyncDataProvider.getOptimizeSchedulerForSpeedPendingRequests());
    }

    public String getAllowOverbookingInfoMessage() {
        return ConstantsManager.getInstance()
                .getMessages()
                .schedulerAllowOverbookingInfo(AsyncDataProvider.getSchedulerAllowOverbookingPendingRequestsThreshold());
    }

    public void setMemoryOverCommit(int value)
    {
        getOptimizationNone_IsSelected().setEntity(value == getOptimizationNone().getEntity());
        getOptimizationForServer_IsSelected().setEntity(value == getOptimizationForServer().getEntity());
        getOptimizationForDesktop_IsSelected().setEntity(value == getOptimizationForDesktop().getEntity());

        if (!getOptimizationNone_IsSelected().getEntity()
                && !getOptimizationForServer_IsSelected().getEntity()
                && !getOptimizationForDesktop_IsSelected().getEntity())
        {
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

    public ClusterModel()
    {
        super();
    }

    public void init(final boolean isEdit) {
        setIsEdit(isEdit);
        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setComment(new EntityModel<String>());
        setEnableTrustedService(new EntityModel<Boolean>(false));
        setEnableHaReservation(new EntityModel<Boolean>(false));
        setEnableOptionalReason(new EntityModel<Boolean>(false));
        getEnableOptionalReason().setIsAvailable(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        setAllowClusterWithVirtGlusterEnabled(true);
        AsyncDataProvider.getAllowClusterWithVirtGlusterEnabled(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                setAllowClusterWithVirtGlusterEnabled((Boolean) returnValue);
            }
        }));

        setEnableOvirtService(new EntityModel<Boolean>());
        setEnableGlusterService(new EntityModel<Boolean>());

        setSpiceProxyEnabled(new EntityModel<Boolean>());
        getSpiceProxyEnabled().setEntity(false);
        getSpiceProxyEnabled().getEntityChangedEvent().addListener(this);

        setSpiceProxy(new EntityModel<String>());
        getSpiceProxy().setIsChangable(false);

        setFencingEnabledModel(new EntityModel<Boolean>());
        getFencingEnabledModel().setEntity(true);
        getFencingEnabledModel().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                 updateFencingPolicyContent(getVersion() == null ? null : getVersion().getSelectedItem());
            }
        });

        setSkipFencingIfSDActiveEnabled(new EntityModel<Boolean>());
        getSkipFencingIfSDActiveEnabled().setEntity(true);

        setSkipFencingIfConnectivityBrokenEnabled(new EntityModel<Boolean>());
        getSkipFencingIfConnectivityBrokenEnabled().setEntity(true);

        setEnableOvirtService(new EntityModel());
        setEnableGlusterService(new EntityModel());

        setSerialNumberPolicy(new SerialNumberPolicyModel());

        getEnableOvirtService().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!getAllowClusterWithVirtGlusterEnabled() && getEnableOvirtService().getEntity()) {
                    getEnableGlusterService().setEntity(Boolean.FALSE);
                }
                getEnableGlusterService().setIsChangable(true);
                getEnableTrustedService().setEntity(false);
                if (getEnableOvirtService().getEntity() != null
                        && getEnableOvirtService().getEntity())
                {
                    if (getEnableGlusterService().getEntity() != null
                            && !getEnableGlusterService().getEntity())
                    {
                        getEnableTrustedService().setIsChangable(true);
                    }
                    else
                    {
                        getEnableTrustedService().setIsChangable(false);
                    }

                }
                else
                {
                    getEnableTrustedService().setIsChangable(false);
                }
            }
        });
        getEnableOvirtService().setEntity(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        getEnableOvirtService().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.VirtOnly
                && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));

        setRngRandomSourceRequired(new EntityModel<Boolean>());
        getRngRandomSourceRequired().setIsAvailable(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        setRngHwrngSourceRequired(new EntityModel<Boolean>());
        getRngHwrngSourceRequired().setIsAvailable(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));

        initImportCluster(isEdit);

        getEnableGlusterService().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!getAllowClusterWithVirtGlusterEnabled() && getEnableGlusterService().getEntity()) {
                    getEnableOvirtService().setEntity(Boolean.FALSE);
                }

                if (!isEdit
                        && getEnableGlusterService().getEntity() != null
                        && getEnableGlusterService().getEntity())
                {
                    getIsImportGlusterConfiguration().setIsAvailable(true);

                    getGlusterHostAddress().setIsAvailable(true);
                    getGlusterHostFingerprint().setIsAvailable(true);
                    getGlusterHostPassword().setIsAvailable(true);
                }
                else
                {
                    getIsImportGlusterConfiguration().setIsAvailable(false);
                    getIsImportGlusterConfiguration().setEntity(false);

                    getGlusterHostAddress().setIsAvailable(false);
                    getGlusterHostFingerprint().setIsAvailable(false);
                    getGlusterHostPassword().setIsAvailable(false);
                }
                if (getEnableGlusterService().getEntity() != null
                        && getEnableGlusterService().getEntity())
                {
                    getEnableTrustedService().setEntity(false);
                    getEnableTrustedService().setIsChangable(false);
                }
                else
                {
                    if (getEnableOvirtService().getEntity() != null
                            && getEnableOvirtService().getEntity())
                    {
                        getEnableTrustedService().setIsChangable(true);
                    }
                    else
                    {
                        getEnableTrustedService().setIsChangable(false);
                    }
                }

            }
       });

        getEnableTrustedService().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (getEnableTrustedService().getEntity() != null
                        && getEnableTrustedService().getEntity())
                {
                    getEnableGlusterService().setEntity(false);
                    getEnableGlusterService().setIsChangable(false);
                }
                else
                {
                    getEnableGlusterService().setIsChangable(true);
                }
            }
        });

        getEnableGlusterService().setEntity(ApplicationModeHelper.getUiMode() == ApplicationMode.GlusterOnly);
        getEnableGlusterService().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly
                && ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly));

        setOptimizationNone(new EntityModel<Integer>());
        setOptimizationForServer(new EntityModel<Integer>());
        setOptimizationForDesktop(new EntityModel<Integer>());
        setOptimizationCustom(new EntityModel<Integer>());

        EntityModel tempVar = new EntityModel<Boolean>();
        tempVar.setEntity(false);
        setOptimizationNone_IsSelected(tempVar);
        getOptimizationNone_IsSelected().getEntityChangedEvent().addListener(this);
        EntityModel tempVar2 = new EntityModel<Boolean>();
        tempVar2.setEntity(false);
        setOptimizationForServer_IsSelected(tempVar2);
        getOptimizationForServer_IsSelected().getEntityChangedEvent().addListener(this);
        EntityModel tempVar3 = new EntityModel<Boolean>();
        tempVar3.setEntity(false);
        setOptimizationForDesktop_IsSelected(tempVar3);
        getOptimizationForDesktop_IsSelected().getEntityChangedEvent().addListener(this);
        EntityModel tempVar4 = new EntityModel<Boolean>();
        tempVar4.setEntity(false);
        tempVar4.setIsAvailable(false);
        setOptimizationCustom_IsSelected(tempVar4);
        getOptimizationCustom_IsSelected().getEntityChangedEvent().addListener(this);

        EntityModel tempVar5 = new EntityModel<Boolean>();
        tempVar5.setEntity(false);
        setMigrateOnErrorOption_YES(tempVar5);
        getMigrateOnErrorOption_YES().getEntityChangedEvent().addListener(this);
        EntityModel tempVar6 = new EntityModel<Boolean>();
        tempVar6.setEntity(false);
        setMigrateOnErrorOption_NO(tempVar6);
        getMigrateOnErrorOption_NO().getEntityChangedEvent().addListener(this);
        EntityModel tempVar7 = new EntityModel<Boolean>();
        tempVar7.setEntity(false);
        setMigrateOnErrorOption_HA_ONLY(tempVar7);
        getMigrateOnErrorOption_HA_ONLY().getEntityChangedEvent().addListener(this);
        setEnableKsm(new EntityModel<Boolean>());
        getEnableKsm().setEntity(false);
        setEnableBallooning(new EntityModel<Boolean>());
        getEnableBallooning().setEntity(false);
        // Optimization methods:
        // default value =100;
        setDefaultMemoryOvercommit(AsyncDataProvider.getClusterDefaultMemoryOverCommit());

        setCountThreadsAsCores(new EntityModel(AsyncDataProvider.getClusterDefaultCountThreadsAsCores()));

        setVersionSupportsCpuThreads(new EntityModel<Boolean>(true));

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

        boolean overbookingSupported = AsyncDataProvider.getScheudulingAllowOverbookingSupported();
        getAllowOverbooking().setIsAvailable(overbookingSupported);
        if (overbookingSupported) {
            getOptimizeForSpeed().getEntityChangedEvent().addListener(new IEventListener() {
                @Override
                public void eventRaised(Event ev, Object sender, EventArgs args) {
                    Boolean entity = getOptimizeForSpeed().getEntity();
                    if (entity) {
                        getGuarantyResources().setEntity(true);
                    }
                    getAllowOverbooking().setIsChangable(!entity);
                }
            });
            getAllowOverbooking().getEntityChangedEvent().addListener(new IEventListener() {
                @Override
                public void eventRaised(Event ev, Object sender, EventArgs args) {
                    Boolean entity = getAllowOverbooking().getEntity();
                    if (entity) {
                        getOptimizeForUtilization().setEntity(true);
                    }
                    getOptimizeForSpeed().setIsChangable(!entity);
                }
            });
        }

        setHostsWithBrokenConnectivityThreshold(new ListModel<Integer>());
        getHostsWithBrokenConnectivityThreshold().setIsAvailable(true);
        getHostsWithBrokenConnectivityThreshold().getSelectedItemChangedEvent().addListener(this);
        initHostsWithBrokenConnectivityThreshold();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                ClusterModel clusterModel = (ClusterModel) model;
                clusterModel.setDesktopOverCommit((Integer) result);
                AsyncQuery _asyncQuery1 = new AsyncQuery();
                _asyncQuery1.setModel(clusterModel);
                _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model1, Object result1)
                    {
                        ClusterModel clusterModel1 = (ClusterModel) model1;
                        clusterModel1.setServerOverCommit((Integer) result1);

                        // temp is used for conversion purposes
                        EntityModel temp;

                        temp = clusterModel1.getOptimizationNone();
                        temp.setEntity(clusterModel1.getDefaultMemoryOvercommit());
                        // res1, res2 is used for conversion purposes.
                        boolean res1 = clusterModel1.getDesktopOverCommit() != clusterModel1.getDefaultMemoryOvercommit();
                        boolean res2 = clusterModel1.getServerOverCommit() != clusterModel1.getDefaultMemoryOvercommit();
                        temp = clusterModel1.getOptimizationNone_IsSelected();
                        setIsSelected(res1 && res2);
                        temp.setEntity(getIsSelected());

                        temp = clusterModel1.getOptimizationForServer();
                        temp.setEntity(clusterModel1.getServerOverCommit());
                        temp = clusterModel1.getOptimizationForServer_IsSelected();
                        temp.setEntity(clusterModel1.getServerOverCommit() == clusterModel1.getDefaultMemoryOvercommit());

                        temp = clusterModel1.getOptimizationForDesktop();
                        temp.setEntity(clusterModel1.getDesktopOverCommit());
                        temp = clusterModel1.getOptimizationForDesktop_IsSelected();
                        temp.setEntity(clusterModel1.getDesktopOverCommit() == clusterModel1.getDefaultMemoryOvercommit());

                        temp = clusterModel1.getOptimizationCustom();
                        temp.setIsAvailable(false);
                        temp.setIsChangable(false);

                        if (clusterModel1.getIsEdit())
                        {
                            clusterModel1.postInit();
                        }

                    }
                };
                AsyncDataProvider.getClusterServerMemoryOverCommit(_asyncQuery1);
            }
        };
        AsyncDataProvider.getClusterDesktopMemoryOverCommit(_asyncQuery);

        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);

        setCPU(new ListModel<ServerCpu>());
        getCPU().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getCPU().getSelectedItemChangedEvent().addListener(this);
        setVersion(new ListModel<Version>());
        getVersion().getSelectedItemChangedEvent().addListener(this);
        setMigrateOnErrorOption(MigrateOnErrorOptions.YES);

        getRngRandomSourceRequired().setEntity(false);
        getRngHwrngSourceRequired().setEntity(false);

        setArchitecture(new ListModel<ArchitectureType>());
        getArchitecture().setIsAvailable(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));

        setIsGeneralTabValid(true);
        setIsResiliencePolicyTabAvailable(true);

        setClusterPolicy(new ListModel<ClusterPolicy>());
        setCustomPropertySheet(new KeyValueModel());
        getClusterPolicy().getSelectedItemChangedEvent().addListener(this);
        Frontend.getInstance().runQuery(VdcQueryType.GetAllPolicyUnits, new VdcQueryParametersBase(), new AsyncQuery(this,
                new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ArrayList<PolicyUnit> policyUnits =
                                ((VdcQueryReturnValue) returnValue).getReturnValue();
                        policyUnitMap = new LinkedHashMap<Guid, PolicyUnit>();
                        for (PolicyUnit policyUnit : policyUnits) {
                            policyUnitMap.put(policyUnit.getId(), policyUnit);
                        }
                        Frontend.getInstance().runQuery(VdcQueryType.GetClusterPolicies,
                                new VdcQueryParametersBase(),
                                new AsyncQuery(model,
                                        new INewAsyncCallback() {

                                            @Override
                                            public void onSuccess(Object model, Object returnValue) {
                                                ClusterModel clusterModel = (ClusterModel) model;
                                                ArrayList<ClusterPolicy> list =
                                                       ((VdcQueryReturnValue) returnValue).getReturnValue();
                                                clusterModel.getClusterPolicy().setItems(list);
                                                ClusterPolicy defaultClusterPolicy = null;
                                                ClusterPolicy selectedClusterPolicy = null;
                                                for (ClusterPolicy clusterPolicy : list) {
                                                    if (clusterModel.getIsEdit() && getEntity() != null
                                                            && clusterPolicy.getId()
                                                            .equals(getEntity().getClusterPolicyId())) {
                                                        selectedClusterPolicy = clusterPolicy;
                                                    }
                                                    if (clusterPolicy.isDefaultPolicy()) {
                                                        defaultClusterPolicy = clusterPolicy;
                                                    }
                                                }
                                                if (selectedClusterPolicy != null) {
                                                    clusterModel.getClusterPolicy()
                                                            .setSelectedItem(selectedClusterPolicy);
                                                } else {
                                                    clusterModel.getClusterPolicy()
                                                            .setSelectedItem(defaultClusterPolicy);
                                                }
                                                clusterPolicyChanged();
                                            }
                                        }));
                    }
                }));
    }

    private void initSpiceProxy() {
        String proxy = getEntity().getSpiceProxy();
        boolean isProxyAvailable = !StringHelper.isNullOrEmpty(proxy);
        getSpiceProxyEnabled().setEntity(isProxyAvailable);
        getSpiceProxy().setIsChangable(isProxyAvailable);
        getSpiceProxy().setEntity(proxy);
    }

    private void initImportCluster(boolean isEdit)
    {
        setGlusterHostAddress(new EntityModel<String>());
        getGlusterHostAddress().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                setIsFingerprintVerified(false);
                if (getGlusterHostAddress().getEntity() == null
                        || (getGlusterHostAddress().getEntity()).trim().length() == 0) {
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
        getIsImportGlusterConfiguration().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (getIsImportGlusterConfiguration().getEntity() != null
                        && getIsImportGlusterConfiguration().getEntity())
                {
                    getGlusterHostAddress().setIsChangable(true);
                    getGlusterHostPassword().setIsChangable(true);
                }
                else
                {
                    getGlusterHostAddress().setIsChangable(false);
                    getGlusterHostPassword().setIsChangable(false);
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
        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                String fingerprint = (String) result;
                if (fingerprint != null && fingerprint.length() > 0)
                {
                    getGlusterHostFingerprint().setEntity((String) result);
                    setIsFingerprintVerified(true);
                }
                else
                {
                    getGlusterHostFingerprint().setEntity(ConstantsManager.getInstance()
                            .getConstants()
                            .errorLoadingFingerprint());
                    setIsFingerprintVerified(false);
                }
            }
        };
        AsyncDataProvider.getHostFingerprint(aQuery, hostAddress);
        getGlusterHostFingerprint().setEntity(ConstantsManager.getInstance().getConstants().loadingFingerprint());
    }

    private void postInit()
    {
        getDescription().setEntity(getEntity().getdescription());
        getComment().setEntity(getEntity().getComment());

        initSpiceProxy();
        getFencingEnabledModel().setEntity(getEntity().getFencingPolicy().isFencingEnabled());
        getSkipFencingIfSDActiveEnabled().setEntity(getEntity().getFencingPolicy().isSkipFencingIfSDActive());
        getSkipFencingIfConnectivityBrokenEnabled().setEntity(getEntity().getFencingPolicy().isSkipFencingIfConnectivityBroken());
        getHostsWithBrokenConnectivityThreshold().setSelectedItem(getEntity().getFencingPolicy().getHostsWithBrokenConnectivityThreshold());

        setMemoryOverCommit(getEntity().getmax_vds_memory_over_commit());

        getCountThreadsAsCores().setEntity(getEntity().getCountThreadsAsCores());
        getEnableBallooning().setEntity(getEntity().isEnableBallooning());
        getEnableKsm().setEntity(getEntity().isEnableKsm());

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                ClusterModel clusterModel = (ClusterModel) model;
                ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) result;

                clusterModel.getDataCenter().setItems(dataCenters);

                clusterModel.getDataCenter().setSelectedItem(null);
                for (StoragePool a : dataCenters)
                {
                    if (clusterModel.getEntity().getStoragePoolId() != null
                            && a.getId().equals(clusterModel.getEntity().getStoragePoolId()))
                    {
                        clusterModel.getDataCenter().setSelectedItem(a);
                        break;
                    }
                }
                clusterModel.getDataCenter().setIsChangable(clusterModel.getDataCenter().getSelectedItem() == null);

                clusterModel.setMigrateOnErrorOption(clusterModel.getEntity().getMigrateOnError());
            }
        };
        AsyncDataProvider.getDataCenterList(_asyncQuery);

    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition))
        {
            if (sender == getDataCenter())
            {
                storagePool_SelectedItemChanged(args);
            }
            else if (sender == getVersion())
            {
                version_SelectedItemChanged(args);
            }
            else if (sender == getClusterPolicy()) {
                clusterPolicyChanged();
            }
            else if (sender == getCPU()) {
                CPU_SelectedItemChanged(args);
            }
        }
        else if (ev.matchesDefinition(EntityModel.entityChangedEventDefinition))
        {
            EntityModel senderEntityModel = (EntityModel) sender;

            if (senderEntityModel == getSpiceProxyEnabled()) {
                getSpiceProxy().setIsChangable(getSpiceProxyEnabled().getEntity());
            } else if ((Boolean) senderEntityModel.getEntity()) {
                if (senderEntityModel == getOptimizationNone_IsSelected())
                {
                    getOptimizationForServer_IsSelected().setEntity(false);
                    getOptimizationForDesktop_IsSelected().setEntity(false);
                    getOptimizationCustom_IsSelected().setEntity(false);
                }
                else if (senderEntityModel == getOptimizationForServer_IsSelected())
                {
                    getOptimizationNone_IsSelected().setEntity(false);
                    getOptimizationForDesktop_IsSelected().setEntity(false);
                    getOptimizationCustom_IsSelected().setEntity(false);
                }
                else if (senderEntityModel == getOptimizationForDesktop_IsSelected())
                {
                    getOptimizationNone_IsSelected().setEntity(false);
                    getOptimizationForServer_IsSelected().setEntity(false);
                    getOptimizationCustom_IsSelected().setEntity(false);
                }
                else if (senderEntityModel == getOptimizationCustom_IsSelected())
                {
                    getOptimizationNone_IsSelected().setEntity(false);
                    getOptimizationForServer_IsSelected().setEntity(false);
                    getOptimizationForDesktop_IsSelected().setEntity(false);
                }
                else if (senderEntityModel == getMigrateOnErrorOption_YES())
                {
                    getMigrateOnErrorOption_NO().setEntity(false);
                    getMigrateOnErrorOption_HA_ONLY().setEntity(false);
                }
                else if (senderEntityModel == getMigrateOnErrorOption_NO())
                {
                    getMigrateOnErrorOption_YES().setEntity(false);
                    getMigrateOnErrorOption_HA_ONLY().setEntity(false);
                }
                else if (senderEntityModel == getMigrateOnErrorOption_HA_ONLY())
                {
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
    }

    private void CPU_SelectedItemChanged(EventArgs args) {
        updateMigrateOnError();
    }

    private void version_SelectedItemChanged(EventArgs e)
    {
        Version version;
        if (getVersion().getSelectedItem() != null)
        {
            version = getVersion().getSelectedItem();
        }
        else
        {
            version = getDataCenter().getSelectedItem().getcompatibility_version();
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                ClusterModel clusterModel = (ClusterModel) model;
                ArrayList<ServerCpu> cpus = (ArrayList<ServerCpu>) result;

                if (clusterModel.getIsEdit()) {
                    AsyncQuery emptyQuery = new AsyncQuery();

                    emptyQuery.setModel(new Object[] { clusterModel, cpus });
                    emptyQuery.asyncCallback = new INewAsyncCallback() {

                        @Override
                        public void onSuccess(Object model, Object returnValue) {
                            Boolean isEmpty = (Boolean) returnValue;

                            Object[] objArray = (Object[]) model;

                            ClusterModel clusterModel = (ClusterModel) objArray[0];
                            ArrayList<ServerCpu> cpus = (ArrayList<ServerCpu>) objArray[1];

                            if (isEmpty) {
                                populateCPUList(clusterModel, cpus, true);
                            } else {
                                ArrayList<ServerCpu> filteredCpus = new ArrayList<ServerCpu>();

                                for (ServerCpu cpu : cpus) {
                                    if (cpu.getArchitecture() == clusterModel.getEntity().getArchitecture()) {
                                        filteredCpus.add(cpu);
                                    }
                                }

                                populateCPUList(clusterModel, filteredCpus, false);
                            }
                        }
                    };

                    AsyncDataProvider.isClusterEmpty(emptyQuery, clusterModel.getEntity().getId());
                } else {
                    populateCPUList(clusterModel, cpus, true);
                }
            }
        };
        AsyncDataProvider.getCPUList(_asyncQuery, version);

        // CPU Thread support is only available for clusters of version 3.2 or greater
        getVersionSupportsCpuThreads().setEntity(version.compareTo(Version.v3_2) >= 0);
        getEnableBallooning().setChangeProhibitionReason(ConstantsManager.getInstance().getConstants().ballooningNotAvailable());
        getEnableBallooning().setIsChangable(version.compareTo(Version.v3_3) >= 0);

        setRngSourcesCheckboxes(version);

        updateFencingPolicyContent(version);

        boolean isSmallerThanVersion3_4 = version.compareTo(Version.v3_4) < 0;
        getEnableKsm().setIsChangable(!isSmallerThanVersion3_4);
        getEnableKsm().setChangeProhibitionReason(ConstantsManager.getInstance().getConstants().ksmNotAvailable());
        if (isSmallerThanVersion3_4) {
            getEnableKsm().setEntity(true);
        }

        updateMigrateOnError();
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

        if (AsyncDataProvider.isMigrationSupported(cpu.getArchitecture(), version)) {
            getMigrateOnErrorOption_YES().setIsAvailable(true);
            getMigrateOnErrorOption_HA_ONLY().setIsAvailable(true);
        } else {
            getMigrateOnErrorOption_YES().setIsAvailable(false);
            getMigrateOnErrorOption_HA_ONLY().setIsAvailable(false);

            setMigrateOnErrorOption(MigrateOnErrorOptions.NO);
        }
    }

    private void setRngSourcesCheckboxes(Version ver) {
        boolean rngSupported = isRngSupportedForClusterVersion(ver);
        getRngRandomSourceRequired().setIsChangable(rngSupported);
        getRngHwrngSourceRequired().setIsChangable(rngSupported);

        String defaultRequiredRngSourcesCsv = defaultClusterRngSourcesCsv(ver);

        if (rngSupported) {
            getRngRandomSourceRequired().setEntity(getIsNew()
                    ? defaultRequiredRngSourcesCsv.contains(VmRngDevice.Source.RANDOM.name().toLowerCase())
                    : getEntity().getRequiredRngSources().contains(VmRngDevice.Source.RANDOM));
            getRngHwrngSourceRequired().setEntity(getIsNew()
                    ? defaultRequiredRngSourcesCsv.contains(VmRngDevice.Source.HWRNG.name().toLowerCase())
                    : getEntity().getRequiredRngSources().contains(VmRngDevice.Source.HWRNG));
        } else { // reset
            getRngRandomSourceRequired().setEntity(false);
            getRngHwrngSourceRequired().setEntity(false);
            getRngRandomSourceRequired().setChangeProhibitionReason(ConstantsManager.getInstance().getConstants().rngNotSupportedByClusterCV());
            getRngHwrngSourceRequired().setChangeProhibitionReason(ConstantsManager.getInstance().getConstants().rngNotSupportedByClusterCV());
        }
    }

    private void updateFencingPolicyContent(Version ver) {
        // skipFencingIfConnectivityBroken option is enabled when fencing is enabled for all cluster versions
        getSkipFencingIfConnectivityBrokenEnabled().setIsChangable(getFencingEnabledModel().getEntity());
        getHostsWithBrokenConnectivityThreshold().setIsChangable(getFencingEnabledModel().getEntity());

        if (ver == null) {
            if (!getFencingEnabledModel().getEntity()) {
                // fencing is disabled and cluster version not selected yet, so disable skipFencingIfSDActive
                getSkipFencingIfSDActiveEnabled().setIsChangable(false);
            }
        } else {
            // skipFencingIfSDActive is enabled for supported cluster level if fencing is not disabled
            boolean supported = AsyncDataProvider.isSkipFencingIfSDActiveSupported(ver.getValue());
            getSkipFencingIfSDActiveEnabled().setIsChangable(
                    supported && getFencingEnabledModel().getEntity());
            if (supported) {
                if (getEntity() == null) {
                    // this can happen when creating new cluster and cluster dialog is shown
                    getSkipFencingIfSDActiveEnabled().setEntity(true);
                } else {
                    getSkipFencingIfSDActiveEnabled().setEntity(
                            getEntity().getFencingPolicy().isSkipFencingIfSDActive());
                }
            } else {
                getSkipFencingIfSDActiveEnabled().setEntity(false);
            }
        }
    }

    private void populateCPUList(ClusterModel clusterModel, List<ServerCpu> cpus, boolean canChangeArchitecture)
    {
        ServerCpu oldSelectedCpu = clusterModel.getCPU().getSelectedItem();

        clusterModel.getCPU().setItems(cpus);
        initSupportedArchitectures(clusterModel);

        clusterModel.getCPU().setSelectedItem(oldSelectedCpu != null ?
                Linq.firstOrDefault(cpus, new Linq.ServerCpuPredicate(oldSelectedCpu.getCpuName())) : null);

        if (clusterModel.getCPU().getSelectedItem() == null || !isCPUinitialized)
        {
            initCPU();
        }

        if (clusterModel.getIsEdit()) {
            if (!canChangeArchitecture) {
                getArchitecture().setItems(new ArrayList<ArchitectureType>(Arrays.asList(clusterModel.getEntity()
                        .getArchitecture())));
            }

            ArchitectureType oldSelectedArch =
                    clusterModel.getArchitecture().getSelectedItem();

            if (oldSelectedArch != null) {
                getArchitecture().setSelectedItem(oldSelectedArch);
            } else {
                getArchitecture().setSelectedItem(getEntity().getArchitecture());
            }
        } else {
            getArchitecture().setSelectedItem(ArchitectureType.undefined);
        }
    }

    private void initSupportedArchitectures(ClusterModel clusterModel) {
        Collection<ArchitectureType> archsWithSupportingCpus = new HashSet<ArchitectureType>();
        archsWithSupportingCpus.add(ArchitectureType.undefined);
        for (ServerCpu cpu: clusterModel.getCPU().getItems()) {
            archsWithSupportingCpus.add(cpu.getArchitecture());
        }
        clusterModel.getArchitecture().setItems(archsWithSupportingCpus);
    }

    private void initCPU()
    {
        if (!isCPUinitialized && getIsEdit())
        {
            isCPUinitialized = true;
            getCPU().setSelectedItem(null);
            for (ServerCpu a : getCPU().getItems())
            {
                if (ObjectUtils.objectsEqual(a.getCpuName(), getEntity().getcpu_name()))
                {
                    getCPU().setSelectedItem(a);
                    break;
                }
            }
        }
    }

    private void initHostsWithBrokenConnectivityThreshold() {
        ArrayList values = new ArrayList<Integer>();
        // populating threshold values with {25, 50, 75, 100}
        for (int i = 25; i <= 100; i += 25) {
            values.add(i);
        }
        getHostsWithBrokenConnectivityThreshold().setItems(values);
        getHostsWithBrokenConnectivityThreshold().setSelectedItem(50);
    }

    private void storagePool_SelectedItemChanged(EventArgs e)
    {
        // possible versions for new cluster (when editing cluster, this event won't occur)
        // are actually the possible versions for the data-center that the cluster is going
        // to be attached to.
        final StoragePool selectedDataCenter = getDataCenter().getSelectedItem();
        if (selectedDataCenter == null)
        {
            return;
        }
        if (selectedDataCenter.isLocal())
        {
            setIsResiliencePolicyTabAvailable(false);
        }
        else
        {
            setIsResiliencePolicyTabAvailable(true);
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                ClusterModel clusterModel = (ClusterModel) model;
                ArrayList<Version> versions = (ArrayList<Version>) result;
                Version selectedVersion = clusterModel.getVersion().getSelectedItem();
                clusterModel.getVersion().setItems(versions);
                if (selectedVersion == null ||
                        !versions.contains(selectedVersion) ||
                        selectedVersion.compareTo(selectedDataCenter.getcompatibility_version()) > 0)
                {
                    if(ApplicationModeHelper.getUiMode().equals(ApplicationMode.GlusterOnly)){
                        clusterModel.getVersion().setSelectedItem(Linq.selectHighestVersion(versions));
                    }
                    else {
                        clusterModel.getVersion().setSelectedItem(selectedDataCenter.getcompatibility_version());
                    }
                }
                else if (clusterModel.getIsEdit()) {
                    clusterModel.getVersion().setSelectedItem(Linq.firstOrDefault(versions,
                            new Linq.VersionPredicate(clusterModel.getEntity().getcompatibility_version())));
                }
                else {
                    clusterModel.getVersion().setSelectedItem(selectedVersion);
                }
            }
        };
        AsyncDataProvider.getDataCenterVersions(_asyncQuery, selectedDataCenter.getId());
    }

    private void clusterPolicyChanged() {
        ClusterPolicy clusterPolicy = getClusterPolicy().getSelectedItem();
        Map<String, String> policyProperties = new HashMap<String, String>();
        Map<Guid, PolicyUnit> allPolicyUnits = new HashMap<Guid, PolicyUnit>();
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

    public boolean validate(boolean validateCpu)
    {
        return validate(true, validateCpu, true);
    }

    public boolean validate(boolean validateStoragePool, boolean validateCpu, boolean validateCustomProperties)
    {
        getName().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(40),
                new I18NNameValidation() });

        if (validateStoragePool)
        {
            getDataCenter().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        if (validateCpu)
        {
            getCPU().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }
        else
        {
            getCPU().validateSelectedItem(new IValidation[] {});
        }

        if (validateCustomProperties) {
            getCustomPropertySheet().setIsValid(getCustomPropertySheet().validate());
        }

        getVersion().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        validateRngRequiredSource();

        boolean validService = true;
        if (getEnableOvirtService().getIsAvailable() && getEnableGlusterService().getIsAvailable())
        {
            validService = getEnableOvirtService().getEntity()
                            || getEnableGlusterService().getEntity();
        }

        getGlusterHostAddress().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getGlusterHostPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });

        if (!validService)
        {
            setMessage(ConstantsManager.getInstance().getConstants().clusterServiceValidationMsg());
        }
        else if (getIsImportGlusterConfiguration().getEntity() && getGlusterHostAddress().getIsValid()
                && getGlusterHostPassword().getIsValid()
                && !isFingerprintVerified())
        {
            setMessage(ConstantsManager.getInstance().getConstants().fingerprintNotVerified());
        }
        else
        {
            setMessage(null);
        }

        if (getSpiceProxyEnabled().getEntity()) {
            getSpiceProxy().validateEntity(new IValidation[] { new HostWithProtocolAndPortAddressValidation() });
        } else {
            getSpiceProxy().setIsValid(true);
        }

        if (getSerialNumberPolicy().getSelectedSerialNumberPolicy() == SerialNumberPolicy.CUSTOM) {
            getSerialNumberPolicy().getCustomSerialNumber().validateEntity(new IValidation[] { new NotEmptyValidation() });
        } else {
            getSerialNumberPolicy().getCustomSerialNumber().setIsValid(true);
        }

        boolean generalTabValid = getName().getIsValid() && getDataCenter().getIsValid() && getCPU().getIsValid()
                && getVersion().getIsValid() && validService && getGlusterHostAddress().getIsValid()
                && getRngRandomSourceRequired().getIsValid()
                && getRngHwrngSourceRequired().getIsValid()
                && getGlusterHostPassword().getIsValid()
                && (getIsImportGlusterConfiguration().getEntity() ? (getGlusterHostAddress().getIsValid()
                && getGlusterHostPassword().getIsValid()
                && getSerialNumberPolicy().getCustomSerialNumber().getIsValid()
                && isFingerprintVerified()) : true);

        setIsGeneralTabValid(generalTabValid);

        return generalTabValid && getCustomPropertySheet().getIsValid();
    }

    private void validateRngRequiredSource() {
        Version clusterVersion = getVersion().getSelectedItem();
        boolean rngSupportedForCluster = isRngSupportedForClusterVersion(clusterVersion);

        getRngRandomSourceRequired().setIsValid(rngSupportedForCluster || !getRngRandomSourceRequired().getEntity());
        getRngHwrngSourceRequired().setIsValid(rngSupportedForCluster || !getRngHwrngSourceRequired().getEntity());
    }

    private boolean isRngSupportedForClusterVersion(Version version) {
        if (version == null) {
            return false;
        }

        Boolean supported = (Boolean) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.VirtIoRngDeviceSupported, version.toString());
        return (supported == null)
                ? false
                : supported;
    }

    private String defaultClusterRngSourcesCsv(Version ver) {
        String srcs = (String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.ClusterRequiredRngSourcesDefault, ver.toString());
        return (srcs == null)
                ? ""
                : srcs;
    }
}
