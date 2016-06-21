package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ExternalEntityBase;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.utils.CpuVendor;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.pm.PowerManagementUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
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
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.ValidationCompleteEvent;
import org.ovirt.engine.ui.uicommonweb.models.providers.HostNetworkProviderModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.HostnameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public abstract class HostModel extends Model implements HasValidatedTabs {
    public static final int HostNameMaxLength = 255;
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

    private UICommand privateUpdateHostsCommand;

    public UICommand getUpdateHostsCommand() {
        return privateUpdateHostsCommand;
    }

    private void setUpdateHostsCommand(UICommand value) {
        privateUpdateHostsCommand = value;
    }

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

    private EntityModel<String> privateFetchSshFingerprint;

    public EntityModel<String> getFetchSshFingerprint() {
        return privateFetchSshFingerprint;
    }

    private void setFetchSshFingerprint(EntityModel<String> value) {
        privateFetchSshFingerprint = value;
    }

    private EntityModel<Integer> privateAuthSshPort;

    public EntityModel<Integer> getAuthSshPort() {
        return privateAuthSshPort;
    }

    private void setAuthSshPort(EntityModel<Integer> value) {
        privateAuthSshPort = value;
    }

    private EntityModel<String> privateUserPassword;

    public EntityModel<String> getUserPassword() {
        return privateUserPassword;
    }

    private void setUserPassword(EntityModel<String> value) {
        privateUserPassword = value;
    }

    private EntityModel<String> privatePublicKey;

    public EntityModel<String> getPublicKey() {
        return privatePublicKey;
    }

    private void setPublicKey(EntityModel<String> value) {
        privatePublicKey = value;
    }

    private EntityModel<String> privateProviderSearchFilterLabel;

    public EntityModel<String> getProviderSearchFilterLabel() {
        return privateProviderSearchFilterLabel;
    }

    private void setProviderSearchFilterLabel(EntityModel<String> value) {
        privateProviderSearchFilterLabel = value;
    }

    private EntityModel<String> privateProviderSearchFilter;

    public EntityModel<String> getProviderSearchFilter() {
        return privateProviderSearchFilter;
    }

    private void setProviderSearchFilter(EntityModel<String> value) {
        privateProviderSearchFilter = value;
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

    private EntityModel<Boolean> privateProtocol;

    public EntityModel<Boolean> getProtocol() {
        return privateProtocol;
    }

    private void setProtocol(EntityModel<Boolean> value) {
        privateProtocol = value;
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

    private EntityModel<Boolean> pmKdumpDetection;

    public EntityModel<Boolean> getPmKdumpDetection() {
        return pmKdumpDetection;
    }

    private void setPmKdumpDetection(EntityModel<Boolean> value) {
        pmKdumpDetection = value;
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

    private VdsProtocol vdsProtocol;

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

    private UICommand proxySSHFingerPrintCommand;

    public UICommand getSSHFingerPrint() {
        return proxySSHFingerPrintCommand;
    }

    public void setSSHFingerPrint(UICommand value) {
        proxySSHFingerPrintCommand = value;
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

    private ListModel<VDS> privateExternalHostName;

    public ListModel<VDS> getExternalHostName() {
        return privateExternalHostName;
    }

    protected void setExternalHostName(ListModel<VDS> value) {
        privateExternalHostName = value;
    }

    private ListModel<ExternalEntityBase> privateExternalDiscoveredHosts;

    public ListModel<ExternalEntityBase> getExternalDiscoveredHosts() {
        return privateExternalDiscoveredHosts;
    }

    protected void setExternalDiscoveredHosts(ListModel<ExternalEntityBase> value) {
        privateExternalDiscoveredHosts = value;
    }

    private ListModel<ExternalEntityBase> privateExternalHostGroups;

    public ListModel<ExternalEntityBase> getExternalHostGroups() {
        return privateExternalHostGroups;
    }

    protected void setExternalHostGroups(ListModel<ExternalEntityBase> value) {
        privateExternalHostGroups = value;
    }

    private ListModel<ExternalEntityBase> privateExternalComputeResource;

    public ListModel<ExternalEntityBase> getExternalComputeResource() {
        return privateExternalComputeResource;
    }

    protected void setExternalComputeResource(ListModel<ExternalEntityBase> value) {
        privateExternalComputeResource = value;
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

    private ListModel<Provider<OpenstackNetworkProviderProperties>> privateProviders;

    public ListModel<Provider<OpenstackNetworkProviderProperties>> getProviders() {
        return privateProviders;
    }

    protected void setProviders(ListModel<Provider<OpenstackNetworkProviderProperties>> value) {
        privateProviders = value;
    }

    private HostNetworkProviderModel networkProviderModel;

    public HostNetworkProviderModel getNetworkProviderModel() {
        return networkProviderModel;
    }

    private void setNetworkProviderModel(HostNetworkProviderModel value) {
        networkProviderModel = value;
    }

    private EntityModel<Boolean> isDiscoveredHosts;

    public EntityModel<Boolean> getIsDiscoveredHosts() {
        return isDiscoveredHosts;
    }

    public void setIsDiscoveredHosts(EntityModel<Boolean> value) {
        isDiscoveredHosts = value;
    }

    public ListModel<Provider<org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties>> getNetworkProviders() {
        return getNetworkProviderModel().getNetworkProviders();
    }

    public EntityModel<String> getInterfaceMappings() {
        return getNetworkProviderModel().getInterfaceMappings();
    }

    private HostedEngineHostModel hostedEngineHostModel;

    public HostModel() {
        setUpdateHostsCommand(new UICommand("", new ICommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand command) {
                updateProvisionedHosts();
            }

            @Override
            public void executeCommand(UICommand uiCommand, Object... parameters) {
                updateProvisionedHosts();
            }
        }));
        setSSHFingerPrint(new UICommand("fetch", new ICommandTarget() {    //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand command) {
                fetchSSHFingerprint();
            }

            @Override
            public void executeCommand(UICommand uiCommand, Object... parameters) {
                fetchSSHFingerprint();
            }
        }));

        setName(new EntityModel<String>());
        setComment(new EntityModel<String>());
        setHost(new EntityModel<String>());
        setPkSection(new EntityModel<Void>());
        setAuthSshPort(new EntityModel<Integer>());
        getAuthSshPort().setEntity(Integer.parseInt(constants.defaultHostSSHPort()));
        setUserName(new EntityModel<String>());
        getUserName().setEntity(RootUserName);
        // TODO: remove setIsChangeable when configured ssh username is enabled
        getUserName().setIsChangeable(false);
        setFetchSshFingerprint(new EntityModel<String>());
        getFetchSshFingerprint().setEntity(""); //$NON-NLS-1$
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
        setProtocol(new EntityModel<Boolean>());
        getProtocol().setEntity(true);
        setFenceAgents(new FenceAgentListModel());

        IEventListener<EventArgs> pmListener = new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updatePmModels();
            }
        };

        setExternalHostName(new ListModel<VDS>());
        getExternalHostName().setIsAvailable(false);
        setExternalHostProviderEnabled(new EntityModel<>(false));
        getExternalHostProviderEnabled().setIsAvailable(false);
        setProviders(new ListModel<Provider<OpenstackNetworkProviderProperties>>());
        getProviders().setIsAvailable(false);
        setProviderSearchFilter(new EntityModel<String>());
        getProviderSearchFilter().setIsAvailable(false);
        setProviderSearchFilterLabel(new EntityModel<String>());
        getProviderSearchFilterLabel().setIsAvailable(false);
        setExternalDiscoveredHosts(new ListModel<ExternalEntityBase>());
        setExternalHostGroups(new ListModel<ExternalEntityBase>());
        getExternalHostGroups().setIsChangeable(true);
        setExternalComputeResource(new ListModel<ExternalEntityBase>());
        getExternalComputeResource().setIsChangeable(true);
        getUpdateHostsCommand().setIsExecutionAllowed(false);

        setDisableAutomaticPowerManagement(new EntityModel<Boolean>());
        getDisableAutomaticPowerManagement().setEntity(false);
        setPmKdumpDetection(new EntityModel<Boolean>());
        getPmKdumpDetection().setEntity(true);

        setPmProxyPreferencesList(new ListModel<FenceProxyModel>());
        getPmProxyPreferencesList().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
               updatePmModels();
            }
        });

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

        setNetworkProviderModel(new HostNetworkProviderModel());
        setIsDiscoveredHosts(new EntityModel<Boolean>());

        setKernelCmdline(new EntityModel<String>());
        setKernelCmdlineIommu(new EntityModel<>(false));
        setKernelCmdlineKvmNested(new EntityModel<>(false));
        setKernelCmdlineUnsafeInterrupts(new EntityModel<>(false));
        setKernelCmdlinePciRealloc(new EntityModel<>(false));
        kernelCmdlineListener = new EnableableEventListener<>(null);
        setCurrentKernelCmdLine(new EntityModel<>(""));
        setHostedEngineHostModel(new HostedEngineHostModel());
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
        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                String pk = (String) result;
                if (pk != null && pk.length() > 0) {
                    getPublicKey().setEntity(pk);
                }
            }
        };
        AsyncDataProvider.getInstance().getEngineSshPublicKey(aQuery);
    }

    private void fetchSSHFingerprint() {
        // Cleaning up fields for initialization
        getFetchSshFingerprint().setEntity(""); //$NON-NLS-1$
        getFetchResult().setEntity(""); //$NON-NLS-1$

        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                String fingerprint = (String) result;
                if (fingerprint != null && fingerprint.length() > 0) {
                    getFetchSshFingerprint().setEntity(fingerprint);
                    getFetchResult().setEntity(ConstantsManager.getInstance().getConstants().successLoadingFingerprint());
                }
                else {
                    getFetchResult().setEntity(ConstantsManager.getInstance().getConstants().errorLoadingFingerprint());
                }
            }
        };

        getHost().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(255),
                new HostAddressValidation() });
        if (!getHost().getIsValid()) {
            getFetchResult().setEntity(ConstantsManager.getInstance().getConstants().fingerprintAddressError()
                    + getHost().getInvalidityReasons().get(0));
        }
        else {
            getFetchResult().setEntity(ConstantsManager.getInstance().getConstants().loadingFingerprint());
            AsyncDataProvider.getInstance().getHostFingerprint(aQuery, getHost().getEntity());
        }
    }

    boolean spmInitialized;
    int maxSpmPriority;
    int defaultSpmPriority;

    private void initSpmPriorities() {

        AsyncDataProvider.getInstance().getMaxSpmPriority(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                HostModel model = (HostModel) target;

                model.maxSpmPriority = (Integer) returnValue;
                initSpmPriorities1();
            }
        }));
    }

    private void initSpmPriorities1() {

        AsyncDataProvider.getInstance().getDefaultSpmPriority(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                HostModel model = (HostModel) target;

                model.defaultSpmPriority = (Integer) returnValue;

                if (postponedSpmPriority != null) {
                    updateSpmPriority(postponedSpmPriority);
                }

                spmInitialized = true;
            }
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

        // Delete 'never' item if it's not selected.
        if (selectedItem != neverItem) {
            items.remove(neverItem);
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
                && (sender == getKernelCmdlineIommu()
                || sender == getKernelCmdlineKvmNested()
                || sender == getKernelCmdlineUnsafeInterrupts()
                || sender == getKernelCmdlinePciRealloc())) {
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
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object nothing, Object result) {
                    @SuppressWarnings("unchecked")
                    final ArrayList<Cluster> clusters = (ArrayList<Cluster>) result;

                    if (getIsNew()) {
                        updateClusterList(HostModel.this, clusters);
                    } else {
                        AsyncQuery architectureQuery = new AsyncQuery();

                        architectureQuery.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object nothing, Object returnValue) {
                                ArchitectureType architecture = (ArchitectureType) returnValue;

                                ArrayList<Cluster> filteredClusters = new ArrayList<>();

                                for (Cluster cluster : clusters) {
                                    if (architecture == ArchitectureType.undefined
                                            || cluster.getArchitecture() == ArchitectureType.undefined
                                            || cluster.getArchitecture() == architecture) {
                                        filteredClusters.add(cluster);
                                    }
                                }

                                updateClusterList(HostModel.this, filteredClusters);
                            }
                        };

                        AsyncDataProvider.getInstance().getHostArchitecture(architectureQuery, getHostId());

                    }
                    updatePmModels();
                }
            };

            AsyncDataProvider.getInstance().getClusterList(_asyncQuery);
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
        Cluster cluster = getCluster().getSelectedItem();
        if (cluster == null) {
            return;
        }

        AsyncDataProvider.getInstance().getPmTypeList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {

                List<String> pmTypes = (ArrayList<String>) returnValue;
                updatePmTypeList(pmTypes);
            }
        }), cluster.getCompatibilityVersion());

        boolean clusterSupportsJsonrpcOnly = cluster.getCompatibilityVersion().greaterOrEquals(Version.v3_6);

        if (clusterSupportsJsonrpcOnly) {
            getProtocol().setIsAvailable(false);
            getProtocol().setEntity(true);
        } else {
            getProtocol().setIsAvailable(true);
            getProtocol().setEntity(vdsProtocol == null ? true : VdsProtocol.STOMP == vdsProtocol);
        }
        getProtocol().setIsChangeable(true);
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

        if (Boolean.TRUE.equals(getIsDiscoveredHosts().getEntity())) {
            getUserPassword().validateEntity(new IValidation[] {
                    new NotEmptyValidation(),
                    new LengthValidation(255)
            });
            getExternalComputeResource().setIsValid(getExternalComputeResource().getSelectedItem() != null);
            getExternalHostGroups().setIsValid(getExternalHostGroups().getSelectedItem() != null);
        }
        else {
            getExternalComputeResource().setIsValid(true);
            getExternalHostGroups().setIsValid(true);
        }
        if (getExternalHostProviderEnabled().getEntity() && getProviders().getSelectedItem() == null) {
            getProviders().getInvalidityReasons().add(constants.validateSelectExternalProvider());
            getProviders().setIsValid(false);
        }

        getAuthSshPort().validateEntity(new IValidation[] {new NotEmptyValidation(), new IntegerValidation(1, 65535)});

        if (getConsoleAddressEnabled().getEntity()) {
            getConsoleAddress().validateEntity(new IValidation[] {new NotEmptyValidation(), new HostAddressValidation()});
        } else {
            // the console address is ignored so can not be invalid
            getConsoleAddress().setIsValid(true);
        }
        setValidTab(TabName.CONSOLE_TAB, getConsoleAddress().getIsValid());

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
                && getExternalHostGroups().getIsValid()
                && getExternalComputeResource().getIsValid()
                && getUserPassword().getIsValid()
                && getProviders().getIsValid()
        );

        setValidTab(TabName.POWER_MANAGEMENT_TAB, fenceAgentsValid);

        getNetworkProviderModel().validate();

        ValidationCompleteEvent.fire(getEventBus(), this);
        return isValidTab(TabName.GENERAL_TAB) && isValidTab(TabName.POWER_MANAGEMENT_TAB)
                && getConsoleAddress().getIsValid() && getNetworkProviderModel().getIsValid();
    }

    public void updateModelFromVds(VDS vds,
            ArrayList<StoragePool> dataCenters,
            boolean isEditWithPMemphasis,
            SystemTreeItemModel selectedSystemTreeItem) {
        setHostId(vds.getId());
        updateExternalHostModels(vds.getHostProviderId());
        getOverrideIpTables().setIsAvailable(showInstallationProperties());
        vdsProtocol = vds.getProtocol();
        getProtocol().setEntity(VdsProtocol.STOMP == vds.getProtocol());
        getProtocol().setIsChangeable(editTransportProperties(vds));
        setSpmPriorityValue(vds.getVdsSpmPriority());
        setOriginalName(vds.getName());
        getName().setEntity(vds.getName());
        getComment().setEntity(vds.getComment());
        getHost().setEntity(vds.getHostName());
        getFetchSshFingerprint().setEntity(vds.getSshKeyFingerprint());
        getUserName().setEntity(vds.getSshUsername());
        getAuthSshPort().setEntity(vds.getSshPort());
        if (StringUtils.isNotEmpty(vds.getKernelArgs())) {
            getCurrentKernelCmdLine().setEntity(constants.currentKernelCmdLine() + vds.getKernelArgs());
        }
        setPort(vds);
        boolean consoleAddressEnabled = vds.getConsoleAddress() != null;
        getConsoleAddressEnabled().setEntity(consoleAddressEnabled);
        getConsoleAddress().setEntity(vds.getConsoleAddress());
        getConsoleAddress().setIsChangeable(consoleAddressEnabled);

        if (!showInstallationProperties()) {
            getPkSection().setIsChangeable(false);
            getPkSection().setIsAvailable(false);

            // Use public key when edit or approve host
            setAuthenticationMethod(AuthenticationMethod.PublicKey);
        }

        setAllowChangeHost(vds);
        if (vds.isFenceAgentsExist()) {
            orderAgents(vds.getFenceAgents());
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
            getFenceAgentListModel().setItems(agents);
        }
        getDisableAutomaticPowerManagement().setEntity(vds.isDisablePowerManagementPolicy());
        getPmKdumpDetection().setEntity(vds.isPmKdumpDetection());
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
        else if (selectedSystemTreeItem != null) {
            final UIConstants constants = ConstantsManager.getInstance().getConstants();

            switch (selectedSystemTreeItem.getType()) {
            case Host:
                getName().setIsChangeable(false);
                getName().setChangeProhibitionReason(constants.cannotEditNameInTreeContext());
                break;
            case Hosts:
            case Cluster:
            case Cluster_Gluster:
                getCluster().setIsChangeable(false);
                getCluster().setChangeProhibitionReason(constants.cannotChangeClusterInTreeContext());
                getDataCenter().setIsChangeable(false);
                break;
            case DataCenter:
                StoragePool selectDataCenter = (StoragePool) selectedSystemTreeItem.getEntity();
                getDataCenter()
                        .setItems(new ArrayList<>(Arrays.asList(new StoragePool[]{selectDataCenter})));
                getDataCenter().setSelectedItem(selectDataCenter);
                getDataCenter().setIsChangeable(false);
                break;
            default:
                break;
            }
        }

        getKernelCmdline().setEntity(vds.getCurrentKernelCmdline());
        setKernelCmdlineParsable(vds.isKernelCmdlineParsable());
        getKernelCmdlineIommu().setEntity(vds.isKernelCmdlineIommu());
        getKernelCmdlineKvmNested().setEntity(vds.isKernelCmdlineKvmNested());
        getKernelCmdlineUnsafeInterrupts().setEntity(vds.isKernelCmdlineUnsafeInterrupts());
        getKernelCmdlinePciRealloc().setEntity(vds.isKernelCmdlinePciRealloc());
    }

    public void cleanHostParametersFields() {
        getName().setEntity(""); //$NON-NLS-1$
        getHost().setEntity(""); //$NON-NLS-1$
    }

    public static void orderAgents(List<FenceAgent> fenceAgents) {
        synchronized (fenceAgents) {
            Collections.sort(fenceAgents, new FenceAgent.FenceAgentOrderComparator());
        }
    }

    private void updateExternalHostModels(final Guid selected) {
        AsyncQuery getProvidersQuery = new AsyncQuery();
        getProvidersQuery.asyncCallback = new INewAsyncCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object result) {
                List<Provider<OpenstackNetworkProviderProperties>> providers =
                        (List<Provider<OpenstackNetworkProviderProperties>>) result;
                ListModel<Provider<OpenstackNetworkProviderProperties>> providersListModel = getProviders();
                if (selected != null) {
                    for (Provider<OpenstackNetworkProviderProperties> provider: providers) {
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
                if (!externalProvisionEnabled()) {
                    getIsDiscoveredHosts().setEntity(null);
                }
            }
        };
        AsyncDataProvider.getInstance().getAllProvidersByType(getProvidersQuery, ProviderType.FOREMAN);
    }

    /**
     * This should be called when model is synchronously filled by data from business entity
     * (async model updates may follow).
     */
    public void onDataInitialized() {
        addKernelCmdlineCheckboxesListeners();
        addKernelCmdlineListener();
        updateKernelCmdlineCheckboxesChangeability();
    }

    private void addKernelCmdlineListener() {
        kernelCmdlineListener = new EnableableEventListener<>(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                setKernelCmdlineParsable(false);
                resetKernelCmdlineCheckboxes();
            }
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
                setKernelCmdlineCheckboxesValue(false);
                break;
            case IBM:
                setKernelCmdlineCheckboxesValue(true);
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
                setKernelCmdlineCheckboxesChangeability(
                        isKernelCmdlineParsable(),
                        constants.kernelCmdlineCheckboxesAndDirectCustomizationNotAllowed());
                break;
            case IBM:
                setKernelCmdlineCheckboxesChangeability(
                        false,
                        constants.kernelCmdlineNotAvailableInClusterWithIbmCpu());
                break;
            default:
                throw new RuntimeException("Unknown CpuVendor type: " + cpuVendor); //$NON-NLS-1$
        }
    }

    private void setKernelCmdlineCheckboxesChangeability(boolean changeable, String reason) {
        getKernelCmdlineIommu().setIsChangeable(changeable, reason);
        getKernelCmdlineKvmNested().setIsChangeable(changeable, reason);
        getKernelCmdlineUnsafeInterrupts().setIsChangeable(changeable, reason);
        getKernelCmdlinePciRealloc().setIsChangeable(changeable, reason);
    }

    private void setKernelCmdlineCheckboxesValue(boolean checked) {
        getKernelCmdlineIommu().setEntity(checked);
        getKernelCmdlineKvmNested().setEntity(checked);
        getKernelCmdlineUnsafeInterrupts().setEntity(checked);
        getKernelCmdlinePciRealloc().setEntity(checked);
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
        getKernelCmdlineIommu().getEntityChangedEvent().addListener(this);
        getKernelCmdlineKvmNested().getEntityChangedEvent().addListener(this);
        getKernelCmdlineUnsafeInterrupts().getEntityChangedEvent().addListener(this);
        getKernelCmdlinePciRealloc().getEntityChangedEvent().addListener(this);
    }

    private void updateKernelCmdlineAccordingToCheckboxes() {
        final CpuVendor cpuVendor = getCurrentCpuVendor();
        if (cpuVendor == null
                || getKernelCmdlineIommu().getEntity() == null
                || getKernelCmdlineKvmNested().getEntity() == null
                || getKernelCmdlineUnsafeInterrupts().getEntity() == null
                || getKernelCmdlinePciRealloc().getEntity() == null) {
            return;
        }
        final String kernelCmdline = KernelCmdlineUtil.create(
                cpuVendor,
                getKernelCmdlineIommu().getEntity(),
                getKernelCmdlineKvmNested().getEntity(),
                getKernelCmdlineUnsafeInterrupts().getEntity(),
                getKernelCmdlinePciRealloc().getEntity());
        kernelCmdlineListener.whilePaused(new Runnable() {
            @Override
            public void run() {
                getKernelCmdline().setEntity(kernelCmdline);
            }
        });
    }

    public void resetKernelCmdline() {
        setKernelCmdlineParsable(true);
        kernelCmdlineListener.whilePaused(new Runnable() {
            @Override
            public void run() {
                getKernelCmdline().setEntity("");
            }
        });
        resetKernelCmdlineCheckboxes();
    }

    protected abstract boolean showInstallationProperties();

    protected abstract boolean editTransportProperties(VDS vds);

    public abstract boolean showExternalProviderPanel();

    protected abstract void updateModelDataCenterFromVds(ArrayList<StoragePool> dataCenters, VDS vds);

    protected abstract void updateModelClusterFromVds(ArrayList<Cluster> clusters, VDS vds);

    protected abstract void setAllowChangeHost(VDS vds);

    protected abstract void setAllowChangeHostPlacementPropertiesWhenNotInMaintenance();

    public void updateHosts() {
        updateExternalHostModels(null);
    }

    protected abstract void updateProvisionedHosts();

    public boolean externalProvisionEnabled() {
        return true;
    }

    protected abstract void setPort(VDS vds);

    public abstract boolean showNetworkProviderTab();

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

    public void setHostedEngineHostModel(HostedEngineHostModel hostedEngineHostModel) {
        this.hostedEngineHostModel = hostedEngineHostModel;
    }
}
