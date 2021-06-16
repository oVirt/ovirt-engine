package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VgpuPlacement;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.CpuVendor;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.pm.PowerManagementUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.HasValidatedTabs;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.ValidationCompleteEvent;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.HostnameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NoTrimmingWhitespacesValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public abstract class HostModel extends Model implements HasValidatedTabs {
    public static final String BeginTestStage = "BeginTest"; //$NON-NLS-1$
    public static final String EndTestStage = "EndTest"; //$NON-NLS-1$
    public static final String RootUserName = "root"; //$NON-NLS-1$

    UIConstants constants = ConstantsManager.getInstance().getConstants();

    private EntityModel<String> currentKernelCmdLine;

    public EntityModel<String> getCurrentKernelCmdLine() {
        return currentKernelCmdLine;
    }

    public void setCurrentKernelCmdLine(EntityModel<String> currentKernelCmdLine) {
        this.currentKernelCmdLine = currentKernelCmdLine;
    }

    private CpuVendor lastNonNullCpuVendor;

    private EnableableEventListener<EventArgs> kernelCmdlineListener;

    public boolean getIsNew() {
        return getHostId() == null;
    }

    private Guid privateHostId;

    public Guid getHostId() {
        return privateHostId;
    }

    public void setHostId(Guid value) {
        privateHostId = value;
    }

    private String privateOriginalName;

    public String getOriginalName() {
        return privateOriginalName;
    }

    public void setOriginalName(String value) {
        privateOriginalName = value;
    }

    private EntityModel<String> privateName;

    public EntityModel<String> getName() {
        return privateName;
    }

    private void setName(EntityModel<String> value) {
        privateName = value;
    }

    private EntityModel<String> bootUuid;

    public EntityModel<String> getKernelCmdlineBootUuid() {
        return getCurrentKernelCmdLine().getEntity().toLowerCase().contains("boot=uuid=") ? new EntityModel<String>("") : bootUuid; //$NON-NLS-1$
    }

    private void setKernelCmdlineBootUuid(EntityModel<String> value) {
        bootUuid = value;
    }

    private EntityModel<String> privateUserName;

    public EntityModel<String> getUserName() {
        return privateUserName;
    }

    private void setUserName(EntityModel<String> value) {
        privateUserName = value;
    }

    private EntityModel<Void> privatePkSection;

    public EntityModel<Void> getPkSection() {
        return privatePkSection;
    }

    private void setPkSection(EntityModel<Void> value) {
        privatePkSection = value;
    }

    private EntityModel<String> privateFetchSshPublicKey;

    public EntityModel<String> getFetchSshPublicKey(){
        return privateFetchSshPublicKey;
    }

    public void setFetchSshPublicKey(EntityModel<String> value){
        this.privateFetchSshPublicKey = value;
    }

    private EntityModel<Integer> privateAuthSshPort;

    public EntityModel<Integer> getAuthSshPort() {
        return privateAuthSshPort;
    }

    private void setAuthSshPort(EntityModel<Integer> value) {
        privateAuthSshPort = value;
    }

    private EntityModel<Boolean> activateHostAfterInstall;

    public EntityModel<Boolean> getActivateHostAfterInstall() {
        return activateHostAfterInstall;
    }

    private void setActivateHostAfterInstall(EntityModel<Boolean> value) {
        activateHostAfterInstall = value;
    }

    private EntityModel<Boolean> rebootHostAfterInstall;

    public EntityModel<Boolean> getRebootHostAfterInstall() {
        return rebootHostAfterInstall;
    }

    private void setRebootHostAfterInstall(EntityModel<Boolean> value) {
        rebootHostAfterInstall = value;
    }

    private EntityModel<String> privateUserPassword;

    public EntityModel<String> getUserPassword() {
        return privateUserPassword;
    }

    private void setUserPassword(EntityModel<String> value) {
        privateUserPassword = value;
    }

    private boolean isPasswordSectionViewable;

    public boolean isPasswordSectionViewable() {
        return isPasswordSectionViewable;
    }

    public void setPasswordSectionViewable(boolean passwordSectionViewable) {
        isPasswordSectionViewable = passwordSectionViewable;
    }

    private EntityModel<String> privatePublicKey;

    public EntityModel<String> getPublicKey() {
        return privatePublicKey;
    }

    private void setPublicKey(EntityModel<String> value) {
        privatePublicKey = value;
    }

    private EntityModel<String> privateHost;

    public EntityModel<String> getHost() {
        return privateHost;
    }

    private void setHost(EntityModel<String> value) {
        privateHost = value;
    }

    private ListModel<StoragePool> privateDataCenter;

    public ListModel<StoragePool> getDataCenter() {
        return privateDataCenter;
    }

    private void setDataCenter(ListModel<StoragePool> value) {
        privateDataCenter = value;
    }

    private ListModel<Cluster> privateCluster;

    public ListModel<Cluster> getCluster() {
        return privateCluster;
    }

    private void setCluster(ListModel<Cluster> value) {
        privateCluster = value;
    }

    private EntityModel<Integer> privatePort;

    public EntityModel<Integer> getPort() {
        return privatePort;
    }

    private void setPort(EntityModel<Integer> value) {
        privatePort = value;
    }

    private AuthenticationMethod hostAuthenticationMethod;

    public void setAuthenticationMethod(AuthenticationMethod value) {
        hostAuthenticationMethod = value;
    }

    public AuthenticationMethod getAuthenticationMethod() {
        return hostAuthenticationMethod;
    }

    private EntityModel<String> privateFetchResult;

    public EntityModel<String> getFetchResult() {
        return privateFetchResult;
    }

    private void setFetchResult(EntityModel<String> value) {
        privateFetchResult = value;
    }

    private EntityModel<Boolean> privateOverrideIpTables;

    public EntityModel<Boolean> getOverrideIpTables() {
        return privateOverrideIpTables;
    }

    private void setOverrideIpTables(EntityModel<Boolean> value) {
        privateOverrideIpTables = value;
    }

    private EntityModel<Boolean> privateIsPm;

    public EntityModel<Boolean> getIsPm() {
        return privateIsPm;
    }

    private void setIsPm(EntityModel<Boolean> value) {
        privateIsPm = value;
    }

    private EntityModel<String> consoleAddress;

    public void setConsoleAddress(EntityModel<String> consoleAddress) {
        this.consoleAddress = consoleAddress;
    }

    public EntityModel<String> getConsoleAddress() {
        return consoleAddress;
    }

    private EntityModel<Boolean> consoleAddressEnabled;

    public EntityModel<Boolean> getConsoleAddressEnabled() {
        return consoleAddressEnabled;
    }

    public void setConsoleAddressEnabled(EntityModel<Boolean> consoleAddressEnabled) {
        this.consoleAddressEnabled = consoleAddressEnabled;
    }

    private EntityModel<Boolean> vgpuConsolidatedPlacement;

    public EntityModel<Boolean> getVgpuConsolidatedPlacement() {
        return vgpuConsolidatedPlacement;
    }

    public void setVgpuConsolidatedPlacement(EntityModel<Boolean> vgpuConsolidatedPlacement) {
        this.vgpuConsolidatedPlacement = vgpuConsolidatedPlacement;
    }

    private EntityModel<Boolean> vgpuSeparatedPlacement;

    public EntityModel<Boolean> getVgpuSeparatedPlacement() {
        return vgpuSeparatedPlacement;
    }

    public void setVgpuSeparatedPlacement(EntityModel<Boolean> vgpuSeparatedPlacement) {
        this.vgpuSeparatedPlacement = vgpuSeparatedPlacement;
    }

    public VgpuPlacement getVgpuPlacement() {
        if (vgpuSeparatedPlacement.getEntity()) {
            return VgpuPlacement.SEPARATED;
        } else {
            return VgpuPlacement.CONSOLIDATED;
        }
    }

    void setVgpuPlacement(VgpuPlacement vgpuPlacement) {
        getVgpuSeparatedPlacement().setEntity(vgpuPlacement == VgpuPlacement.SEPARATED);
        getVgpuConsolidatedPlacement().setEntity(vgpuPlacement == VgpuPlacement.CONSOLIDATED);
    }

    void setVgpuPlacementChangeability(Version version) {
        boolean vgpuPlacementEnabled;
        if (version == null) {
            vgpuPlacementEnabled = false;
        } else {
            vgpuPlacementEnabled = AsyncDataProvider.getInstance().isVgpuPlacementSupported(version);
        }
        getVgpuConsolidatedPlacement().setIsChangeable(vgpuPlacementEnabled);
        getVgpuSeparatedPlacement().setIsChangeable(vgpuPlacementEnabled);
        if (! vgpuPlacementEnabled) {
            getVgpuConsolidatedPlacement().setChangeProhibitionReason(constants.vgpuPlacementCompatibilityInfo());
            getVgpuSeparatedPlacement().setChangeProhibitionReason(constants.vgpuPlacementCompatibilityInfo());
        }
    }

    private EntityModel<Boolean> pmKdumpDetection;

    public EntityModel<Boolean> getPmKdumpDetection() {
        return pmKdumpDetection;
    }

    private void setPmKdumpDetection(EntityModel<Boolean> value) {
        pmKdumpDetection = value;
    }

    private EntityModel<Boolean> fencingEnabled;

    public EntityModel<Boolean> getFencingEnabled() {
        return fencingEnabled;
    }

    private void setFencingEnabled(EntityModel<Boolean> value) {
        fencingEnabled = value;
    }

    private EntityModel<Boolean> disableAutomaticPowerManagement;

    public EntityModel<Boolean> getDisableAutomaticPowerManagement() {
        return disableAutomaticPowerManagement;
    }

    private void setDisableAutomaticPowerManagement(EntityModel<Boolean> value) {
        disableAutomaticPowerManagement = value;
    }

    private boolean isPowerManagementTabSelected;

    public boolean getIsPowerManagementTabSelected() {
        return isPowerManagementTabSelected;
    }

    public void setIsPowerManagementTabSelected(boolean value) {
        if (isPowerManagementTabSelected != value) {
            isPowerManagementTabSelected = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsPowerManagementTabSelected")); //$NON-NLS-1$
        }
    }

    private EntityModel<String> kernelCmdline;

    public EntityModel<String> getKernelCmdline() {
        return kernelCmdline;
    }

    public void setKernelCmdline(EntityModel<String> kernelCmdline) {
        this.kernelCmdline = kernelCmdline;
    }

    private boolean kernelCmdlineParsable;

    public boolean isKernelCmdlineParsable() {
        return kernelCmdlineParsable;
    }

    public void setKernelCmdlineParsable(boolean kernelCmdlineParsable) {
        this.kernelCmdlineParsable = kernelCmdlineParsable;
    }

    private EntityModel<Boolean> kernelCmdlineBlacklistNouveau;

    public EntityModel<Boolean> getKernelCmdlineBlacklistNouveau() {
        return kernelCmdlineBlacklistNouveau;
    }

    public void setKernelCmdlineBlacklistNouveau(EntityModel<Boolean> kernelCmdlineBlacklistNouveau) {
        this.kernelCmdlineBlacklistNouveau = kernelCmdlineBlacklistNouveau;
    }

    private EntityModel<Boolean> kernelCmdlineIommu;

    public EntityModel<Boolean> getKernelCmdlineIommu() {
        return kernelCmdlineIommu;
    }

    public void setKernelCmdlineIommu(EntityModel<Boolean> kernelCmdlineIommu) {
        this.kernelCmdlineIommu = kernelCmdlineIommu;
    }

    private EntityModel<Boolean> kernelCmdlineKvmNested;

    public EntityModel<Boolean> getKernelCmdlineKvmNested() {
        return kernelCmdlineKvmNested;
    }

    public void setKernelCmdlineKvmNested(EntityModel<Boolean> kernelCmdlineKvmNested) {
        this.kernelCmdlineKvmNested = kernelCmdlineKvmNested;
    }

    private EntityModel<Boolean> kernelCmdlineUnsafeInterrupts;

    public EntityModel<Boolean> getKernelCmdlineUnsafeInterrupts() {
        return kernelCmdlineUnsafeInterrupts;
    }

    public void setKernelCmdlineUnsafeInterrupts(EntityModel<Boolean> kernelCmdlineUnsafeInterrupts) {
        this.kernelCmdlineUnsafeInterrupts = kernelCmdlineUnsafeInterrupts;
    }

    private EntityModel<Boolean> kernelCmdlinePciRealloc;

    public EntityModel<Boolean> getKernelCmdlinePciRealloc() {
        return kernelCmdlinePciRealloc;
    }

    public void setKernelCmdlinePciRealloc(EntityModel<Boolean> kernelCmdlinePciRealloc) {
        this.kernelCmdlinePciRealloc = kernelCmdlinePciRealloc;
    }

    private EntityModel<Boolean> kernelCmdlineFips;

    public EntityModel<Boolean> getKernelCmdlineFips() {
        return kernelCmdlineFips;
    }

    public void setKernelCmdlineFips(EntityModel<Boolean> kernelCmdlineFips) {
        this.kernelCmdlineFips = kernelCmdlineFips;
    }

    private EntityModel<Boolean> kernelCmdlineSmtDisabled;

    public EntityModel<Boolean> getKernelCmdlineSmtDisabled() {
        return kernelCmdlineSmtDisabled;
    }

    public void setKernelCmdlineSmtDisabled(EntityModel<Boolean> kernelCmdlineSmtDisabled) {
        this.kernelCmdlineSmtDisabled = kernelCmdlineSmtDisabled;
    }

    public String getPmProxyPreferences() {
        // Return null if power management is not enabled.
        if (!getIsPm().getEntity()) {
            return null;
        }
        //Translate the preferences back into a comma separated string.
        StringBuilder builder = new StringBuilder();

        if (getPmProxyPreferencesList().getItems() != null) {
            Collection<FenceProxyModel> items = getPmProxyPreferencesList().getItems();
            for (FenceProxyModel item : items) {
                if (builder.length() > 0) {
                    builder.append(",");    //$NON-NLS-1$
                }
                builder.append(item.getEntity().getValue());

            }
        }

        return builder.toString();
    }

    public void setPmProxyPreferences(String value) {
        // Create list from the provided comma delimited string.
        String[] array = value.split(",");    //$NON-NLS-1$
        List<FenceProxyModel> list = new ArrayList<>();

        for (String item : array) {
            FenceProxyModel model = new FenceProxyModel();
            model.setEntity(FenceProxySourceType.forValue(item));
            list.add(model);
        }

        getPmProxyPreferencesList().setItems(list);
    }

    private ListModel<FenceProxyModel> pmProxyPreferencesList;

    public ListModel<FenceProxyModel> getPmProxyPreferencesList() {
        return pmProxyPreferencesList;
    }

    private void setPmProxyPreferencesList(ListModel<FenceProxyModel> value) {
        pmProxyPreferencesList = value;
    }

    private FenceAgentListModel fenceAgents;

    public void setFenceAgents(FenceAgentListModel fenceAgentListModel) {
        fenceAgents = fenceAgentListModel;
    }

    public FenceAgentListModel getFenceAgentListModel() {
        return fenceAgents;
    }

    private UICommand proxySSHPublicKeyCommand;

    public UICommand getSSHPublicKey(){
        return proxySSHPublicKeyCommand;
    }

    public void setSSHPublicKey(UICommand value){
        this.proxySSHPublicKeyCommand =value;
    }

    private Integer postponedSpmPriority;

    public void setSpmPriorityValue(Integer value) {
        if (spmInitialized) {
            updateSpmPriority(value);
        } else {
            postponedSpmPriority = value;
        }
    }

    public int getSpmPriorityValue() {

        EntityModel<Integer> selectedItem = getSpmPriority().getSelectedItem();
        if (selectedItem != null) {
            return selectedItem.getEntity();
        }

        return 0;
    }

    private ListModel<EntityModel<Integer>> spmPriority;

    public ListModel<EntityModel<Integer>> getSpmPriority() {
        return spmPriority;
    }

    private void setSpmPriority(ListModel<EntityModel<Integer>> value) {
        spmPriority = value;
    }

    private EntityModel<String> privateComment;

    public EntityModel<String> getComment() {
        return privateComment;
    }

    protected void setComment(EntityModel<String> value) {
        privateComment = value;
    }

    private EntityModel<Boolean> externalHostProviderEnabled;

    public EntityModel<Boolean> getExternalHostProviderEnabled() {
        return externalHostProviderEnabled;
    }

    public void setExternalHostProviderEnabled(EntityModel<Boolean> externalHostProviderEnabled) {
        this.externalHostProviderEnabled = externalHostProviderEnabled;
    }

    private ListModel<Provider<Provider.AdditionalProperties>> privateProviders;

    public ListModel<Provider<Provider.AdditionalProperties>> getProviders() {
        return privateProviders;
    }

    protected void setProviders(ListModel<Provider<Provider.AdditionalProperties>> value) {
        privateProviders = value;
    }

    private HostedEngineHostModel hostedEngineHostModel;

    private ListModel<AffinityGroup> affinityGroupList;

    public ListModel<AffinityGroup> getAffinityGroupList() {
        return affinityGroupList;
    }

    public void setAffinityGroupList(ListModel<AffinityGroup> affinityGroupList) {
        this.affinityGroupList = affinityGroupList;
    }

    private ListModel<Label> labelList;

    public void setLabelList(ListModel<Label> labelList) {
        this.labelList = labelList;
    }

    public ListModel<Label> getLabelList() {
        return labelList;
    }

    private boolean isHeSystem;

    public void setIsHeSystem(boolean isHeSystem) {
        this.isHeSystem = isHeSystem;
    }

    public boolean getIsHeSystem() {
        return isHeSystem;
    }

    private boolean isHostedEngineDeployed;

    public void setIsHostedEngineDeployed(boolean isHeDeployed) {
        isHostedEngineDeployed = isHeDeployed;
    }

    public boolean getIsHostedEngineDeployed() {
        return isHostedEngineDeployed;
    }

    private List<Guid> hostsWithHeDeployed;

    public void setHostsWithHeDeployed(List<Guid> hostsWithHeDeployed) {
        this.hostsWithHeDeployed = hostsWithHeDeployed;
    }

    public List<Guid> getHostsWithHeDeployed() {
        return hostsWithHeDeployed;
    }

    public boolean isLastHostWithHeDeployed() {
        return hostsWithHeDeployed.size() == 1
                && hostsWithHeDeployed.get(0).equals(getHostId());
    }

    public HostModel() {
        setSSHPublicKey(new UICommand("fetch", new ICommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand command) {
                fetchSSHPublicKey();
            }

            @Override
            public void executeCommand(UICommand uiCommand, Object... parameters) {
                fetchSSHPublicKey();
            }
        }));

        setName(new EntityModel<String>());
        setComment(new EntityModel<String>());
        setHost(new EntityModel<String>());
        setPkSection(new EntityModel<Void>());
        setAuthSshPort(new EntityModel<Integer>());
        getAuthSshPort().setEntity(VdsStatic.DEFAULT_SSH_PORT);
        setActivateHostAfterInstall(new EntityModel<Boolean>());
        getActivateHostAfterInstall().setEntity(true);
        setRebootHostAfterInstall(new EntityModel<Boolean>());
        getRebootHostAfterInstall().setEntity(true);
        setUserName(new EntityModel<String>());
        getUserName().setEntity(RootUserName);
        // TODO: remove setIsChangeable when configured ssh username is enabled
        getUserName().setIsChangeable(false);
        setFetchSshPublicKey(new EntityModel<String>());
        getFetchSshPublicKey().setEntity(""); //$NON-NLS-1$
        setUserPassword(new EntityModel<String>());
        getUserPassword().setEntity(""); //$NON-NLS-1$
        setPublicKey(new EntityModel<String>());
        getPublicKey().setEntity(""); //$NON-NLS-1$
        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        setCluster(new ListModel<Cluster>());
        getCluster().getSelectedItemChangedEvent().addListener(this);
        setPort(new EntityModel<Integer>());
        setFetchResult(new EntityModel<String>());
        setOverrideIpTables(new EntityModel<Boolean>());
        getOverrideIpTables().setEntity(false);
        setFenceAgents(new FenceAgentListModel());

        IEventListener<EventArgs> pmListener = (ev, sender, args) -> updatePmModels();

        setExternalHostProviderEnabled(new EntityModel<>(false));
        getExternalHostProviderEnabled().setIsAvailable(false);
        setProviders(new ListModel<>());
        getProviders().setIsAvailable(false);

        setDisableAutomaticPowerManagement(new EntityModel<Boolean>());
        getDisableAutomaticPowerManagement().setEntity(false);
        setPmKdumpDetection(new EntityModel<Boolean>());
        setFencingEnabled(new EntityModel<Boolean>());
        getPmKdumpDetection().setEntity(true);

        setPmProxyPreferencesList(new ListModel<FenceProxyModel>());
        getPmProxyPreferencesList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updatePmModels());

        setConsoleAddress(new EntityModel<String>());
        getConsoleAddress().setEntity(null);
        setConsoleAddressEnabled(new EntityModel<Boolean>());
        getConsoleAddressEnabled().setEntity(false);
        getConsoleAddressEnabled().getEntityChangedEvent().addListener(this);

        setIsPm(new EntityModel<Boolean>());
        getIsPm().getEntityChangedEvent().addListener(pmListener);
        getIsPm().setEntity(false);

        setValidTab(TabName.POWER_MANAGEMENT_TAB, true);
        setValidTab(TabName.GENERAL_TAB, true);

        setSpmPriority(new ListModel<EntityModel<Integer>>());

        initSpmPriorities();
        fetchEngineSshPublicKey();

        setKernelCmdline(new EntityModel<String>());
        setKernelCmdlineBlacklistNouveau(new EntityModel<>(false));
        setKernelCmdlineIommu(new EntityModel<>(false));
        setKernelCmdlineKvmNested(new EntityModel<>(false));
        setKernelCmdlineUnsafeInterrupts(new EntityModel<>(false));
        setKernelCmdlinePciRealloc(new EntityModel<>(false));
        setKernelCmdlineFips(new EntityModel<>(false));
        setKernelCmdlineBootUuid(new EntityModel<>(""));
        setKernelCmdlineSmtDisabled(new EntityModel<>(false));
        kernelCmdlineListener = new EnableableEventListener<>(null);
        setCurrentKernelCmdLine(new EntityModel<>(""));
        setHostedEngineHostModel(new HostedEngineHostModel());
        setAffinityGroupList(new ListModel<>());
        setLabelList(new ListModel<Label>());
        updateAffinityLists();

        setPasswordSectionViewable(true);

        setVgpuConsolidatedPlacement(new EntityModel<>(false));
        setVgpuSeparatedPlacement(new EntityModel<>(false));
    }

    private void updatePmModels() {
        boolean isPm = getIsPm().getEntity();
        getPmProxyPreferencesList().setIsChangeable(getIsPm().getEntity());
        getDisableAutomaticPowerManagement().setIsValid(true);
        getDisableAutomaticPowerManagement().setIsChangeable(isPm);
        getFenceAgentListModel().setIsChangeable(isPm);

        // Update other PM fields.
        getPmKdumpDetection().setIsChangeable(isPm);
        getFenceAgentListModel().setIsChangeable(isPm);
        getFenceAgentListModel().setHost(this);
    }

    public void fetchEngineSshPublicKey() {
        AsyncDataProvider.getInstance().getEngineSshPublicKey(new AsyncQuery<>(pk -> {
            if (pk != null && pk.length() > 0) {
                getPublicKey().setEntity(pk);
            }
        }));
    }

    private void fetchSSHPublicKey() {
        // Cleaning up fields for initialization
        getFetchSshPublicKey().setEntity(""); //$NON-NLS-1$
        getFetchResult().setEntity(""); //$NON-NLS-1$

        AsyncQuery<String> aQuery = new AsyncQuery<>(publicKeyPem -> {
            if (publicKeyPem != null && publicKeyPem.length() > 0) {
                getFetchSshPublicKey().setEntity(publicKeyPem);
                getFetchResult().setEntity(ConstantsManager.getInstance().getConstants().successLoadingPublicKey());
            } else {
                getFetchResult().setEntity(ConstantsManager.getInstance().getConstants().errorLoadingPublicKey());
            }
        });

        getHost().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(255),
                new HostAddressValidation() });
        if (!getHost().getIsValid()) {
            getFetchResult().setEntity(ConstantsManager.getInstance().getConstants().publicKeyAddressError()
                    + getHost().getInvalidityReasons().get(0));
        } else {
            getFetchResult().setEntity(ConstantsManager.getInstance().getConstants().loadingPublicKey());
            AsyncDataProvider.getInstance().getHostSshPublicKey(
                    aQuery,
                    getHost().getEntity(),
                    getAuthSshPort().getEntity());
        }
    }

    boolean spmInitialized;
    int maxSpmPriority;
    int defaultSpmPriority;

    private void initSpmPriorities() {

        AsyncDataProvider.getInstance().getMaxSpmPriority(new AsyncQuery<>(returnValue -> {

            maxSpmPriority = returnValue;
            initSpmPriorities1();
        }));
    }

    private void initSpmPriorities1() {

        AsyncDataProvider.getInstance().getDefaultSpmPriority(new AsyncQuery<>(returnValue -> {

            defaultSpmPriority = returnValue;

            if (postponedSpmPriority != null) {
                updateSpmPriority(postponedSpmPriority);
            }

            spmInitialized = true;
        }));
    }

    private void updateSpmPriority(Integer value) {

        List<EntityModel<Integer>> items = new ArrayList<>();

        if (value == null) {
            value = defaultSpmPriority;
        }

        int neverValue = -1;
        EntityModel<Integer> neverItem = new EntityModel<>(constants.neverTitle(), neverValue);
        items.add(neverItem);
        int lowValue = defaultSpmPriority / 2;
        items.add(new EntityModel<>(constants.lowTitle(), lowValue));
        items.add(new EntityModel<>(constants.normalTitle(), defaultSpmPriority));
        int highValue = defaultSpmPriority + (maxSpmPriority - defaultSpmPriority) / 2;
        items.add(new EntityModel<>(constants.highTitle(), highValue));

        // Determine whether to set custom SPM priority, and where.
        EntityModel<Integer> selectedItem = null;

        int[] values = new int[] { neverValue, lowValue, defaultSpmPriority, highValue, maxSpmPriority + 1 };
        Integer prevValue = null;

        for (int i = 0; i < values.length; i++) {

            int currentValue = values[i];

            if (value == currentValue) {
                selectedItem = items.get(i);
                break;
            } else if (prevValue != null && value > prevValue && value < currentValue) {
                EntityModel<Integer> customItem = new EntityModel<>("Custom (" + value + ")", value);//$NON-NLS-1$ //$NON-NLS-2$

                items.add(i, customItem);
                selectedItem = customItem;
                break;
            }

            prevValue = currentValue;
        }

        getSpmPriority().setItems(items);
        getSpmPriority().setSelectedItem(selectedItem);
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getDataCenter()) {
            dataCenter_SelectedItemChanged();
        } else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getCluster()) {
            cluster_SelectedItemChanged();
        } else if (sender == getConsoleAddressEnabled()) {
            consoleAddressChanged();
        } else if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)
                && (sender == getKernelCmdlineBlacklistNouveau()
                || sender == getKernelCmdlineIommu()
                || sender == getKernelCmdlineKvmNested()
                || sender == getKernelCmdlineUnsafeInterrupts()
                || sender == getKernelCmdlinePciRealloc()
                || sender == getKernelCmdlineFips()
                || sender == getKernelCmdlineSmtDisabled())) {
            if (isKernelCmdlineParsable()) {
                updateKernelCmdlineAccordingToCheckboxes();
            }
        }
    }

    private void consoleAddressChanged() {
        boolean enabled = getConsoleAddressEnabled().getEntity();
        getConsoleAddress().setIsChangeable(enabled);
    }

    private void dataCenter_SelectedItemChanged() {
        StoragePool dataCenter = getDataCenter().getSelectedItem();
        if (dataCenter != null) {
            AsyncDataProvider.getInstance().getClusterList(new AsyncQuery<>(clusters -> {
                if (getIsNew()) {
                    updateClusterList(HostModel.this, clusters);
                } else {
                    AsyncDataProvider.getInstance().getHostArchitecture(new AsyncQuery<>(architecture -> {
                        ArrayList<Cluster> filteredClusters = new ArrayList<>();

                        for (Cluster cluster : clusters) {
                            if (architecture == ArchitectureType.undefined
                                    || cluster.getArchitecture() == ArchitectureType.undefined
                                    || cluster.getArchitecture() == architecture) {
                                filteredClusters.add(cluster);
                            }
                        }

                        updateClusterList(HostModel.this, filteredClusters);
                    }), getHostId());

                }
                updatePmModels();
            }));
        }
    }

    private void updateClusterList(HostModel hostModel, List<Cluster> clusters) {
        Cluster oldCluster = hostModel.getCluster().getSelectedItem();
        List<Cluster> filteredClusters = filterClusters(clusters, hostModel.getDataCenter().getItems());
        hostModel.getCluster().setItems(filteredClusters);

        if (oldCluster != null) {
            Cluster newSelectedItem =
                    Linq.firstOrNull(filteredClusters, new Linq.IdPredicate<>(oldCluster.getId()));
            if (newSelectedItem != null) {
                hostModel.getCluster().setSelectedItem(newSelectedItem);
            }
        }

        if (hostModel.getCluster().getSelectedItem() == null) {
            hostModel.getCluster().setSelectedItem(Linq.firstOrNull(filteredClusters));
        }

    }

    private List<Cluster> filterClusters(List<Cluster> clusters, Collection<StoragePool> dataCenters) {
        List<Cluster> result = new ArrayList<>();
        Set<Guid> dataCenterIds = new HashSet<>();
        for (StoragePool dataCenter: dataCenters) {
            dataCenterIds.add(dataCenter.getId());
        }
        for (Cluster cluster: clusters) {
            if (dataCenterIds.contains(cluster.getStoragePoolId())) {
                result.add(cluster);
            }
        }
        return result;
    }

    protected void cluster_SelectedItemChanged() {
        updateAffinityLists();

        Cluster cluster = getCluster().getSelectedItem();
        if (cluster == null) {
            return;
        }

        getFencingEnabled().setEntity(cluster.getFencingPolicy().isFencingEnabled());

        AsyncDataProvider.getInstance().getPmTypeList(new AsyncQuery<>(pmTypes -> updatePmTypeList(pmTypes)), cluster.getCompatibilityVersion());

        //Match the appropriate selected data center to the selected cluster, don't fire update events.
        if (getDataCenter() != null && getDataCenter().getItems() != null) {
            for (StoragePool datacenter : getDataCenter().getItems()) {
                if (datacenter.getId().equals(cluster.getStoragePoolId())) {
                    getDataCenter().setSelectedItem(datacenter, false);
                    break;
                }
            }
        }

        final CpuVendor newCpuVendor = getCurrentCpuVendor();
        if (newCpuVendor != null && !newCpuVendor.equals(lastNonNullCpuVendor)) {
            lastNonNullCpuVendor = newCpuVendor;
            cpuVendorChanged();
        }

        setVgpuPlacementChangeability(cluster.getCompatibilityVersion());
    }

    protected abstract void cpuVendorChanged();

    private void updatePmTypeList(List<String> pmTypes) {
        getFenceAgentListModel().setPmTypes(pmTypes);
    }

    public boolean validate() {
        getName().validateEntity(new IValidation[] { new HostnameValidation() });

        getComment().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        getHost().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(255),
                new HostAddressValidation() });

        getAuthSshPort().validateEntity(new IValidation[] {new NotEmptyValidation(), new IntegerValidation(1, 65535)});

        if (getConsoleAddressEnabled().getEntity()) {
            getConsoleAddress().validateEntity(new IValidation[] {new NotEmptyValidation(), new HostAddressValidation()});
        } else {
            // the console address is ignored so can not be invalid
            getConsoleAddress().setIsValid(true);
        }
        setValidTab(TabName.CONSOLE_TAB, getConsoleAddress().getIsValid());

        getKernelCmdline().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
        setValidTab(TabName.KERNEL_TAB, getKernelCmdline().getIsValid());

        getDataCenter().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getCluster().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        boolean fenceAgentsValid = true;
        if (getIsPm().getEntity()) {
            fenceAgentsValid = getFenceAgentListModel().validate();
        }

        setValidTab(TabName.GENERAL_TAB, getName().getIsValid()
                && getComment().getIsValid()
                && getHost().getIsValid()
                && getAuthSshPort().getIsValid()
                && getCluster().getIsValid()
                && getUserPassword().getIsValid()
                && getProviders().getIsValid()
        );

        setValidTab(TabName.POWER_MANAGEMENT_TAB, fenceAgentsValid);

        ValidationCompleteEvent.fire(getEventBus(), this);
        return isValidTab(TabName.GENERAL_TAB) && isValidTab(TabName.POWER_MANAGEMENT_TAB)
                && getConsoleAddress().getIsValid()
                && isValidTab(TabName.KERNEL_TAB);
    }

    public void updateModelFromVds(VDS vds,
            List<StoragePool> dataCenters,
            boolean isEditWithPMemphasis) {
        setHostId(vds.getId());
        setIsHostedEngineDeployed(vds.isHostedEngineDeployed());
        updateExternalHostModels(vds.getHostProviderId());
        getOverrideIpTables().setIsAvailable(showInstallationProperties());
        setSpmPriorityValue(vds.getVdsSpmPriority());
        setOriginalName(vds.getName());
        getName().setEntity(vds.getName());
        getComment().setEntity(vds.getComment());
        getKernelCmdlineBootUuid().setEntity(vds.getBootUuid());
        getHost().setEntity(vds.getHostName());
        getFetchSshPublicKey().setEntity(vds.getSshPublicKey());
        getUserName().setEntity(vds.getSshUsername());
        getAuthSshPort().setEntity(vds.getSshPort());
        if (StringHelper.isNotNullOrEmpty(vds.getKernelArgs())) {
            getCurrentKernelCmdLine().setEntity(constants.currentKernelCmdLine() + " " + vds.getKernelArgs()); //$NON-NLS-1$
        }
        setPort(vds);
        boolean consoleAddressEnabled = vds.getConsoleAddress() != null;
        getConsoleAddressEnabled().setEntity(consoleAddressEnabled);
        getConsoleAddress().setEntity(vds.getConsoleAddress());
        getConsoleAddress().setIsChangeable(consoleAddressEnabled);
        setVgpuPlacement(VgpuPlacement.forValue(vds.getVgpuPlacement()));
        setVgpuPlacementChangeability(getCluster().getSelectedItem().getCompatibilityVersion());

        if (!showInstallationProperties()) {
            getPkSection().setIsChangeable(false);
            getPkSection().setIsAvailable(false);

            // Use public key when edit or approve host
            setAuthenticationMethod(AuthenticationMethod.PublicKey);
        }

        setAllowChangeHost(vds);
        if (vds.isFenceAgentsExist()) {
            orderAgents(vds.getFenceAgents());
            List<FenceAgentModel> agents = getFenceAgentModelList(vds);
            getFenceAgentListModel().setItems(agents);
        }
        getDisableAutomaticPowerManagement().setEntity(vds.isDisablePowerManagementPolicy());
        getPmKdumpDetection().setEntity(vds.isPmKdumpDetection());
        getFencingEnabled().setEntity(vds.isFencingEnabled());
        // Set other PM parameters.
        if (isEditWithPMemphasis) {
            setIsPowerManagementTabSelected(true);
            getIsPm().setEntity(true);
            getIsPm().setIsChangeable(false);
        } else {
            getIsPm().setEntity(vds.isPmEnabled());
        }
        updateModelDataCenterFromVds(dataCenters, vds);

        if (vds.getStatus() != VDSStatus.Maintenance &&
            vds.getStatus() != VDSStatus.PendingApproval &&
            vds.getStatus() != VDSStatus.InstallingOS) {
            setAllowChangeHostPlacementPropertiesWhenNotInMaintenance();
        }

        getKernelCmdline().setEntity(vds.getCurrentKernelCmdline());
        setKernelCmdlineParsable(vds.isKernelCmdlineParsable());
        getKernelCmdlineBlacklistNouveau().setEntity(vds.isKernelCmdlineBlacklistNouveau());
        getKernelCmdlineIommu().setEntity(vds.isKernelCmdlineIommu());
        getKernelCmdlineKvmNested().setEntity(vds.isKernelCmdlineKvmNested());
        getKernelCmdlineUnsafeInterrupts().setEntity(vds.isKernelCmdlineUnsafeInterrupts());
        getKernelCmdlinePciRealloc().setEntity(vds.isKernelCmdlinePciRealloc());
        getKernelCmdlineFips().setEntity(vds.isKernelCmdlineFips());
        getKernelCmdlineSmtDisabled().setEntity(vds.isKernelCmdlineSmtDisabled());
    }

    public List<FenceAgentModel> getFenceAgentModelList(VDS vds) {
        List<FenceAgentModel> agents = new ArrayList<>();
        //Keep a list of examined agents to prevent duplicate management IPs from showing up in the UI.
        Set<Pair<String, String>> examinedAgents = new HashSet<>();
        for (FenceAgent agent: vds.getFenceAgents()) {
            FenceAgentModel model = new FenceAgentModel();
            model.setHost(this);
            // Set primary PM parameters.
            model.getManagementIp().setEntity(agent.getIp());
            model.getPmUserName().setEntity(agent.getUser());
            model.getPmPassword().setEntity(agent.getPassword());
            model.getPmType().setSelectedItem(agent.getType());
            if (agent.getPort() != null) {
                model.getPmPort().setEntity(agent.getPort());
            }
            model.getPmEncryptOptions().setEntity(agent.getEncryptOptions());
            model.setPmOptionsMap(PowerManagementUtils.pmOptionsStringToMap(agent.getOptions()));
            model.setOrder(agent.getOrder());
            if (!examinedAgents.contains(new Pair<>(model.getManagementIp().getEntity(),
                    model.getPmType().getSelectedItem()))) {
                boolean added = false;
                for (FenceAgentModel concurrentModel: agents) {
                    if (model.getOrder().getEntity() != null && model.getOrder().getEntity().equals(concurrentModel.getOrder().getEntity())) {
                        concurrentModel.getConcurrentList().add(model);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    agents.add(model);
                }
            }
            examinedAgents.add(new Pair<>(model.getManagementIp().getEntity(),
                    model.getPmType().getSelectedItem()));
        }
        return agents;
    }

    public static void orderAgents(List<FenceAgent> fenceAgents) {
        synchronized (fenceAgents) {
            Collections.sort(fenceAgents, new FenceAgent.FenceAgentOrderComparator());
        }
    }

    private void updateExternalHostModels(final Guid selected) {
        AsyncDataProvider.getInstance().getAllProvidersByType(new AsyncQuery<>(result -> {
            List<Provider<Provider.AdditionalProperties>> providers = (List) result;
            ListModel<Provider<Provider.AdditionalProperties>> providersListModel = getProviders();
            if (selected != null) {
                for (Provider<Provider.AdditionalProperties> provider: providers) {
                    if (provider.getId().equals(selected)) {
                        providersListModel.setItems(providers, provider);
                        getExternalHostProviderEnabled().setEntity(true);
                        break;
                    }
                }
            }
            if (providersListModel.getItems() == null || providersListModel.getItems().isEmpty()
                    || providersListModel.getSelectedItem() == null) {
                providersListModel.setItems(providers, Linq.firstOrNull(providers));
            }
            providersListModel.setIsChangeable(true);
        }), ProviderType.FOREMAN);
    }

    /**
     * This should be called when model is synchronously filled by data from business entity
     * (async model updates may follow).
     */
    public void onDataInitialized() {
        addKernelCmdlineCheckboxesListeners();
        addKernelCmdlineListener();
        updateKernelCmdlineCheckboxesChangeability();
        updateAffinityLists();
    }

    private void addKernelCmdlineListener() {
        kernelCmdlineListener = new EnableableEventListener<>((ev, sender, args) -> {
            setKernelCmdlineParsable(false);
            resetKernelCmdlineCheckboxes();
        });
        getKernelCmdline().getEntityChangedEvent().addListener(kernelCmdlineListener);
    }

    public void resetKernelCmdlineCheckboxes() {
        final CpuVendor cpuVendor = getCurrentCpuVendor();
        if (cpuVendor == null) {
            return;
        }
        switch (cpuVendor) {
            case INTEL:
            case AMD:
            case IBMS390:
                resetKernelCmdlineCheckboxesValue();
                break;
            case IBM:
                resetKernelCmdlineCheckboxesValuePpc();
                break;
            default:
                throw new RuntimeException("Unknown CpuVendor type: " + cpuVendor); //$NON-NLS-1$
        }
        updateKernelCmdlineCheckboxesChangeability();
    }

    protected void updateKernelCmdlineCheckboxesChangeability() {
        final CpuVendor cpuVendor = getCurrentCpuVendor();
        if (cpuVendor == null) {
            return;
        }
        switch (cpuVendor) {
            case INTEL:
            case AMD:
            case IBMS390:
                setKernelCmdlineCheckboxesChangeability(
                        isKernelCmdlineParsable(),
                        constants.kernelCmdlineCheckboxesAndDirectCustomizationNotAllowed());
                break;
            case IBM:
                setKernelCmdlineCheckboxesChangeability(
                        false,
                        constants.kernelCmdlineNotAvailableInClusterWithIbmCpu());
                // FIPS and SMT kernel params should be available on POWER
                getKernelCmdlineFips().setIsChangeable(isKernelCmdlineParsable());
                getKernelCmdlineSmtDisabled().setIsChangeable(isKernelCmdlineParsable());
                break;
            default:
                throw new RuntimeException("Unknown CpuVendor type: " + cpuVendor); //$NON-NLS-1$
        }
    }

    private void setKernelCmdlineCheckboxesChangeability(boolean changeable, String reason) {
        getKernelCmdlineBlacklistNouveau().setIsChangeable(changeable, reason);
        getKernelCmdlineIommu().setIsChangeable(changeable, reason);
        getKernelCmdlineKvmNested().setIsChangeable(changeable, reason);
        getKernelCmdlineUnsafeInterrupts().setIsChangeable(changeable, reason);
        getKernelCmdlinePciRealloc().setIsChangeable(changeable, reason);
        getKernelCmdlineFips().setIsChangeable(changeable, reason);
        getKernelCmdlineSmtDisabled().setIsChangeable(changeable, reason);
    }

    private void resetKernelCmdlineCheckboxesValue() {
        getKernelCmdlineBlacklistNouveau().setEntity(false);
        getKernelCmdlineIommu().setEntity(false);
        getKernelCmdlineKvmNested().setEntity(false);
        getKernelCmdlineUnsafeInterrupts().setEntity(false);
        getKernelCmdlinePciRealloc().setEntity(false);
        getKernelCmdlineFips().setEntity(false);
        getKernelCmdlineSmtDisabled().setEntity(false);
    }

    private void resetKernelCmdlineCheckboxesValuePpc() {
        // PPC processors are specific:
        // * they don't require flag to turn iommu on - flag needs to be true
        // * FIPS and SMT are available to be set by users
        // * nesting and other options are not available - flag needs to be false
        getKernelCmdlineBlacklistNouveau().setEntity(false);
        getKernelCmdlineIommu().setEntity(true);
        getKernelCmdlineKvmNested().setEntity(false);
        getKernelCmdlineUnsafeInterrupts().setEntity(false);
        getKernelCmdlinePciRealloc().setEntity(false);
        getKernelCmdlineFips().setEntity(false);
        getKernelCmdlineSmtDisabled().setEntity(false);
    }

    /**
     * @return it may return `null`
     */
    private CpuVendor getCurrentCpuVendor() {
        if (getCluster().getSelectedItem() == null) {
            return null;
        }
        final Cluster selectedCluster = getCluster().getSelectedItem();
        final ServerCpu clustersCpu = AsyncDataProvider.getInstance()
                .getCpuByName(selectedCluster.getCpuName(), selectedCluster.getCompatibilityVersion());
        if (clustersCpu == null) {
            // in case CPU of cluster was not yet set
            return null;
        }
        return  clustersCpu.getVendor();
    }

    private void addKernelCmdlineCheckboxesListeners() {
        getKernelCmdlineBlacklistNouveau().getEntityChangedEvent().addListener(this);
        getKernelCmdlineIommu().getEntityChangedEvent().addListener(this);
        getKernelCmdlineKvmNested().getEntityChangedEvent().addListener(this);
        getKernelCmdlineUnsafeInterrupts().getEntityChangedEvent().addListener(this);
        getKernelCmdlinePciRealloc().getEntityChangedEvent().addListener(this);
        getKernelCmdlineFips().getEntityChangedEvent().addListener(this);
        getKernelCmdlineSmtDisabled().getEntityChangedEvent().addListener(this);
    }

    private void updateKernelCmdlineAccordingToCheckboxes() {
        final CpuVendor cpuVendor = getCurrentCpuVendor();
        if (cpuVendor == null
                || getKernelCmdlineBlacklistNouveau().getEntity() == null
                || getKernelCmdlineIommu().getEntity() == null
                || getKernelCmdlineKvmNested().getEntity() == null
                || getKernelCmdlineUnsafeInterrupts().getEntity() == null
                || getKernelCmdlinePciRealloc().getEntity() == null
                || getKernelCmdlineFips().getEntity() == null
                || getKernelCmdlineSmtDisabled().getEntity() == null) {
            return;
        }
        final String kernelCmdline = KernelCmdlineUtil.create(
                cpuVendor,
                getKernelCmdlineBlacklistNouveau().getEntity(),
                getKernelCmdlineIommu().getEntity(),
                getKernelCmdlineKvmNested().getEntity(),
                getKernelCmdlineUnsafeInterrupts().getEntity(),
                getKernelCmdlinePciRealloc().getEntity(),
                getKernelCmdlineFips().getEntity(),
                getKernelCmdlineBootUuid().getEntity(),
                getKernelCmdlineSmtDisabled().getEntity());
        kernelCmdlineListener.whilePaused(() -> getKernelCmdline().setEntity(kernelCmdline));
    }

    public void resetKernelCmdline() {
        setKernelCmdlineParsable(true);
        kernelCmdlineListener.whilePaused(() -> getKernelCmdline().setEntity(""));
        resetKernelCmdlineCheckboxes();
    }

    private void updateAffinityLists() {
        AsyncCallback<List<AffinityGroup>> affinityGroupsCallback = groups -> {
            affinityGroupList.setItems(groups);
            if (getIsNew()) {
                affinityGroupList.setSelectedItems(new ArrayList<>());
            } else {
                Guid hostId = getHostId();
                affinityGroupList.setSelectedItems(groups.stream()
                        .filter(ag -> ag.getVdsIds().contains(hostId))
                        .collect(Collectors.toList()));
            }
        };

        if (getCluster().getSelectedItem() == null) {
            // This must be a modifiable list, otherwise an exception is thrown.
            // Once the selected cluster is not null anymore and this list is changed for another one,
            // somewhere the code adds an element to this list, even if it is not used.
            affinityGroupsCallback.onSuccess(new ArrayList<>());
        } else {
            AsyncDataProvider.getInstance().getAffinityGroupsByClusterId(new AsyncQuery<>(affinityGroupsCallback),
                    getCluster().getSelectedItem().getId());
        }

        AsyncDataProvider.getInstance().getLabelList(new AsyncQuery<>(labels -> {
            labelList.setItems(labels);
            if (getIsNew()) {
                labelList.setSelectedItems(new ArrayList<>());
            } else {
                Guid hostId = getHostId();
                labelList.setSelectedItems(labels.stream()
                        .filter(label -> label.getHosts().contains(hostId))
                        .collect(Collectors.toList()));
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

    protected abstract boolean showInstallationProperties();

    protected abstract boolean editTransportProperties(VDS vds);

    public abstract boolean showExternalProviderPanel();

    protected abstract void updateModelDataCenterFromVds(List<StoragePool> dataCenters, VDS vds);

    protected abstract void updateModelClusterFromVds(ArrayList<Cluster> clusters, VDS vds);

    protected abstract void setAllowChangeHost(VDS vds);

    protected abstract void setAllowChangeHostPlacementPropertiesWhenNotInMaintenance();

    public void updateHosts() {
        updateExternalHostModels(null);
    }

    public boolean externalProvisionEnabled() {
        return true;
    }

    protected abstract void setPort(VDS vds);

    /**
     * {@code EntityModel.setEntity(..., false);} can't be used because this prevents
     * view form update
     */
    private static class EnableableEventListener<T extends EventArgs> implements IEventListener<T> {

        private boolean enabled = true;

        private final IEventListener<T> delegate;

        public EnableableEventListener(IEventListener<T> delegate) {
            this.delegate = delegate;
        }

        public void whilePaused(Runnable action) {
            final boolean originalEnabled = this.enabled;
            enabled = false;
            action.run();
            enabled = originalEnabled;
        }

        @Override
        public void eventRaised(Event<? extends T> ev, Object sender, T args) {
            if (enabled && delegate != null) {
                delegate.eventRaised(ev, sender, args);
            }
        }
    }

    public HostedEngineHostModel getHostedEngineHostModel() {
        return hostedEngineHostModel;
    }

    public void setHostedEngineHostModel(final HostedEngineHostModel hostedEngineHostModel) {
        this.hostedEngineHostModel = hostedEngineHostModel;

        if (!isHostedEngineDeployed || getIsNew()) {
            hostedEngineHostModel.removeActionFromList(HostedEngineDeployConfiguration.Action.UNDEPLOY);
        } else if (isLastHostWithHeDeployed()) {
            hostedEngineHostModel.setIsChangeable(false);
            hostedEngineHostModel.setChangeProhibitionReason(constants.cannotUndeployHeFromLastHostWithHeDeployed());
        } else {
            hostedEngineHostModel.removeActionFromList(HostedEngineDeployConfiguration.Action.DEPLOY);
        }
    }
}
