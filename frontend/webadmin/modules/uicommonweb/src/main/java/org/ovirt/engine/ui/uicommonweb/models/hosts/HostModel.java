package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ExternalEntityBase;
import org.ovirt.engine.core.common.businessentities.FenceAgentOrder;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetNewVdsFenceStatusParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.HostNetworkProviderModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.HostnameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.KeyValuePairValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public abstract class HostModel extends Model
{

    public static final int HostNameMaxLength = 255;
    public static final String PmSecureKey = "secure"; //$NON-NLS-1$
    public static final String PmPortKey = "port"; //$NON-NLS-1$
    public static final String PmSlotKey = "slot"; //$NON-NLS-1$
    public static final String BeginTestStage = "BeginTest"; //$NON-NLS-1$
    public static final String EndTestStage = "EndTest"; //$NON-NLS-1$
    public static final String RootUserName = "root"; //$NON-NLS-1$

    private UICommand privateTestCommand;
    UIConstants constants = ConstantsManager.getInstance().getConstants();

    public UICommand getTestCommand()
    {
        return privateTestCommand;
    }

    private void setTestCommand(UICommand value)
    {
        privateTestCommand = value;
    }

    private UICommand privateUpdateHostsCommand;

    public UICommand getUpdateHostsCommand()
    {
        return privateUpdateHostsCommand;
    }

    private void setUpdateHostsCommand(UICommand value)
    {
        privateUpdateHostsCommand = value;
    }

    public boolean getIsNew()
    {
        return getHostId() == null;
    }

    private Guid privateHostId;

    public Guid getHostId()
    {
        return privateHostId;
    }

    public void setHostId(Guid value)
    {
        privateHostId = value;
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

    private EntityModel<String> privateName;

    public EntityModel<String> getName()
    {
        return privateName;
    }

    private void setName(EntityModel<String> value)
    {
        privateName = value;
    }

    private EntityModel<String> privateUserName;

    public EntityModel<String> getUserName()
    {
        return privateUserName;
    }

    private void setUserName(EntityModel<String> value)
    {
        privateUserName = value;
    }

    private EntityModel privatePkSection;

    public EntityModel getPkSection()
    {
        return privatePkSection;
    }

    private void setPkSection(EntityModel value)
    {
        privatePkSection = value;
    }

    private EntityModel privateDiscoveredHostSection;

    public EntityModel getDiscoveredHostSection() { return privateDiscoveredHostSection; }

    private void setDiscoveredHostSection(EntityModel value)
    {
        privateDiscoveredHostSection = value;
    }

    private EntityModel privateProvisionedHostSection;

    public EntityModel getProvisionedHostSection() { return privateProvisionedHostSection; }

    private void setProvisionedHostSection(EntityModel value)
    {
        privateProvisionedHostSection = value;
    }

    private EntityModel privatePasswordSection;

    public EntityModel getPasswordSection()
    {
        return privatePasswordSection;
    }

    private void setPasswordSection(EntityModel value)
    {
        privatePasswordSection = value;
    }

    private EntityModel<String> privateFetchSshFingerprint;

    public EntityModel<String> getFetchSshFingerprint()
    {
        return privateFetchSshFingerprint;
    }

    private void setFetchSshFingerprint(EntityModel<String> value)
    {
        privateFetchSshFingerprint = value;
    }

    private EntityModel<Integer> privateAuthSshPort;

    public EntityModel<Integer> getAuthSshPort()
    {
        return privateAuthSshPort;
    }

    private void setAuthSshPort(EntityModel<Integer> value)
    {
        privateAuthSshPort = value;
    }

    private EntityModel<String> privateUserPassword;

    public EntityModel<String> getUserPassword()
    {
        return privateUserPassword;
    }

    private void setUserPassword(EntityModel<String> value)
    {
        privateUserPassword = value;
    }

    private EntityModel<String> privatePublicKey;

    public EntityModel<String> getPublicKey()
    {
        return privatePublicKey;
    }

    private void setPublicKey(EntityModel<String> value)
    {
        privatePublicKey = value;
    }

    private EntityModel<String> privateProviderSearchFilterLabel;

    public EntityModel<String> getProviderSearchFilterLabel()
    {
        return privateProviderSearchFilterLabel;
    }

    private void setProviderSearchFilterLabel(EntityModel<String> value)
    {
        privateProviderSearchFilterLabel = value;
    }

    private EntityModel<String> privateProviderSearchFilter;

    public EntityModel<String> getProviderSearchFilter()
    {
        return privateProviderSearchFilter;
    }

    private void setProviderSearchFilter(EntityModel<String> value)
    {
        privateProviderSearchFilter = value;
    }

    private EntityModel<String> privateHost;

    public EntityModel<String> getHost()
    {
        return privateHost;
    }

    private void setHost(EntityModel<String> value)
    {
        privateHost = value;
    }

    private EntityModel<String> privateManagementIp;

    public EntityModel<String> getManagementIp()
    {
        return privateManagementIp;
    }

    private void setManagementIp(EntityModel<String> value)
    {
        privateManagementIp = value;
    }

    private ListModel<StoragePool> privateDataCenter;

    public ListModel<StoragePool> getDataCenter()
    {
        return privateDataCenter;
    }

    private void setDataCenter(ListModel<StoragePool> value)
    {
        privateDataCenter = value;
    }

    private ListModel<VDSGroup> privateCluster;

    public ListModel<VDSGroup> getCluster()
    {
        return privateCluster;
    }

    private void setCluster(ListModel<VDSGroup> value)
    {
        privateCluster = value;
    }

    private EntityModel<Integer> privatePort;

    public EntityModel<Integer> getPort()
    {
        return privatePort;
    }

    private void setPort(EntityModel<Integer> value)
    {
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

    public EntityModel<String> getFetchResult()
    {
        return privateFetchResult;
    }

    private void setFetchResult(EntityModel<String> value)
    {
        privateFetchResult = value;
    }

    private EntityModel<Boolean> privateOverrideIpTables;

    public EntityModel<Boolean> getOverrideIpTables()
    {
        return privateOverrideIpTables;
    }

    private void setOverrideIpTables(EntityModel<Boolean> value)
    {
        privateOverrideIpTables = value;
    }

    private EntityModel<Boolean> privateProtocol;

    public EntityModel<Boolean> getProtocol()
    {
        return privateProtocol;
    }

    private void setProtocol(EntityModel<Boolean> value)
    {
        privateProtocol = value;
    }

    private EntityModel<Boolean> privateIsPm;

    public EntityModel<Boolean> getIsPm()
    {
        return privateIsPm;
    }

    private void setIsPm(EntityModel<Boolean> value)
    {
        privateIsPm = value;
    }

    private EntityModel<String> privatePmUserName;

    public EntityModel<String> getPmUserName()
    {
        return privatePmUserName;
    }

    private void setPmUserName(EntityModel<String> value)
    {
        privatePmUserName = value;
    }

    private EntityModel<String> privatePmPassword;

    public EntityModel<String> getPmPassword()
    {
        return privatePmPassword;
    }

    private void setPmPassword(EntityModel<String> value)
    {
        privatePmPassword = value;
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

    private ListModel<String> privatePmType;

    public ListModel<String> getPmType()
    {
        return privatePmType;
    }

    private void setPmType(ListModel<String> value)
    {
        privatePmType = value;
    }

    private EntityModel<Boolean> privatePmSecure;

    public EntityModel<Boolean> getPmSecure()
    {
        return privatePmSecure;
    }

    private void setPmSecure(EntityModel<Boolean> value)
    {
        privatePmSecure = value;
    }

    private EntityModel<String> privatePmPort;

    public EntityModel<String> getPmPort()
    {
        return privatePmPort;
    }

    private void setPmPort(EntityModel<String> value)
    {
        privatePmPort = value;
    }

    private EntityModel<String> privatePmSlot;

    public EntityModel<String> getPmSlot()
    {
        return privatePmSlot;
    }

    private void setPmSlot(EntityModel<String> value)
    {
        privatePmSlot = value;
    }

    private EntityModel<String> privatePmOptions;

    public EntityModel<String> getPmOptions()
    {
        return privatePmOptions;
    }

    private void setPmOptions(EntityModel<String> value)
    {
        privatePmOptions = value;
    }

    private EntityModel<String> pmSecondaryIp;

    public EntityModel<String> getPmSecondaryIp() {
        return pmSecondaryIp;
    }

    private void setPmSecondaryIp(EntityModel<String> value) {
        pmSecondaryIp = value;
    }

    private EntityModel<String> pmSecondaryPort;

    public EntityModel<String> getPmSecondaryPort() {
        return pmSecondaryPort;
    }

    private void setPmSecondaryPort(EntityModel<String> value) {
        pmSecondaryPort = value;
    }

    private EntityModel<String> pmSecondaryUserName;

    public EntityModel<String> getPmSecondaryUserName() {
        return pmSecondaryUserName;
    }

    private void setPmSecondaryUserName(EntityModel<String> value) {
        pmSecondaryUserName = value;
    }

    private EntityModel<String> pmSecondaryPassword;

    public EntityModel<String> getPmSecondaryPassword() {
        return pmSecondaryPassword;
    }

    private void setPmSecondaryPassword(EntityModel<String> value) {
        pmSecondaryPassword = value;
    }

    private ListModel<String> pmSecondaryType;

    public ListModel<String> getPmSecondaryType() {
        return pmSecondaryType;
    }

    private void setPmSecondaryType(ListModel<String> value) {
        pmSecondaryType = value;
    }

    private EntityModel<String> pmSecondaryOptions;

    public EntityModel<String> getPmSecondaryOptions() {
        return pmSecondaryOptions;
    }

    private void setPmSecondaryOptions(EntityModel<String> value) {
        pmSecondaryOptions = value;
    }

    private EntityModel<Boolean> pmSecondarySecure;

    public EntityModel<Boolean> getPmSecondarySecure() {
        return pmSecondarySecure;
    }

    private void setPmSecondarySecure(EntityModel<Boolean> value) {
        pmSecondarySecure = value;
    }

    private EntityModel<Boolean> pmKdumpDetection;

    public EntityModel<Boolean> getPmKdumpDetection()
    {
        return pmKdumpDetection;
    }

    private void setPmKdumpDetection(EntityModel<Boolean> value)
    {
        pmKdumpDetection = value;
    }

    public HashMap<String, String> getPmSecondaryOptionsMap() {

        // For secondary map determine (workarround) if it's was specified
        // by checking secondary PM fields.
        if (!isEntityModelEmpty(getPmSecondaryIp())
            || !isEntityModelEmpty(getPmSecondaryUserName())
            || !isEntityModelEmpty(getPmSecondaryPassword())) {

            return getPmOptionsMapInternal(getPmSecondaryPort(), getPmSecondarySlot(), getPmSecondarySecure(), getPmSecondaryOptions());
        }

        return new HashMap<String, String>();
    }

    public void setPmSecondaryOptionsMap(Map<String, String> value) {
        setPmOptionsMapInternal(value, getPmSecondaryPort(), getPmSecondarySlot(), getPmSecondarySecure(), getPmSecondaryOptions());
    }

    private EntityModel<String> pmSecondarySlot;

    public EntityModel<String> getPmSecondarySlot() {
        return pmSecondarySlot;
    }

    private void setPmSecondarySlot(EntityModel<String> value) {
        pmSecondarySlot = value;
    }


    private EntityModel<Boolean> pmSecondaryConcurrent;

    public EntityModel<Boolean> getPmSecondaryConcurrent() {
        return pmSecondaryConcurrent;
    }

    private void setPmSecondaryConcurrent(EntityModel<Boolean> value) {
        pmSecondaryConcurrent = value;
    }

    private ListModel<String> pmVariants;

    public ListModel<String> getPmVariants() {
        return pmVariants;
    }

    private void setPmVariants(ListModel<String> value) {
        pmVariants = value;
    }

    private EntityModel<Boolean> disableAutomaticPowerManagement;

    public EntityModel<Boolean> getDisableAutomaticPowerManagement() {
        return disableAutomaticPowerManagement;
    }

    private void setDisableAutomaticPowerManagement(EntityModel<Boolean> value) {
        disableAutomaticPowerManagement = value;
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

    private boolean isPowerManagementTabValid;

    public boolean getIsPowerManagementTabValid()
    {
        return isPowerManagementTabValid;
    }

    public void setIsPowerManagementTabValid(boolean value)
    {
        if (isPowerManagementTabValid != value)
        {
            isPowerManagementTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsPowerManagementTabValid")); //$NON-NLS-1$
        }
    }

    private boolean isPowerManagementTabSelected;

    public boolean getIsPowerManagementTabSelected()
    {
        return isPowerManagementTabSelected;
    }

    public void setIsPowerManagementTabSelected(boolean value)
    {
        if (isPowerManagementTabSelected != value)
        {
            isPowerManagementTabSelected = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsPowerManagementTabSelected")); //$NON-NLS-1$
        }
    }

    private boolean ciscoUcsPrimaryPmTypeSelected;

    public boolean isCiscoUcsPrimaryPmTypeSelected() {
        return ciscoUcsPrimaryPmTypeSelected;
    }

    public void setCiscoUcsPrimaryPmTypeSelected(boolean value) {
        if (ciscoUcsPrimaryPmTypeSelected != value) {
            ciscoUcsPrimaryPmTypeSelected = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsCiscoUcsPrimaryPmTypeSelected")); //$NON-NLS-1$
        }
    }

    private boolean ciscoUcsSecondaryPmTypeSelected;

    public boolean isCiscoUcsSecondaryPmTypeSelected() {
        return ciscoUcsSecondaryPmTypeSelected;
    }

    public void setCiscoUcsSecondaryPmTypeSelected(boolean value) {
        if (ciscoUcsSecondaryPmTypeSelected != value) {
            ciscoUcsSecondaryPmTypeSelected = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsCiscoUcsSecondaryPmTypeSelected")); //$NON-NLS-1$
        }
    }

    public HashMap<String, String> getPmOptionsMap() {
        return getPmOptionsMapInternal(getPmPort(), getPmSlot(), getPmSecure(), getPmOptions());
    }

    public void setPmOptionsMap(Map<String, String> value) {
        setPmOptionsMapInternal(value, getPmPort(), getPmSlot(), getPmSecure(), getPmOptions());
    }

    public String getPmProxyPreferences() {
        // Return null if power management is not enabled.
        if (!getIsPm().getEntity()) {
            return null;
        }

        // Pack back proxy items to the comma delimited string.
        StringBuilder builder = new StringBuilder();

        if (getPmProxyPreferencesList().getItems() != null) {
            List items = (List) getPmProxyPreferencesList().getItems();
            for (Object item : items) {

                builder.append(item);

                if (items.indexOf(item) < items.size() - 1) {
                    builder.append(",");    //$NON-NLS-1$
                }
            }
        }

        return builder.toString();
    }

    public void setPmProxyPreferences(String value) {
        // Create list from the provided comma delimited string.
        String[] array = value.split(",");    //$NON-NLS-1$
        List<String> list = new ArrayList<String>();

        for (String item : array) {
            list.add(item);
        }

        getPmProxyPreferencesList().setItems(list);
    }

    private ListModel<String> pmProxyPreferencesList;

    public ListModel<String> getPmProxyPreferencesList() {
        return pmProxyPreferencesList;
    }

    private void setPmProxyPreferencesList(ListModel<String> value) {
        pmProxyPreferencesList = value;
    }

    private UICommand proxyUpCommand;

    public UICommand getProxyUpCommand() {
        return proxyUpCommand;
    }

    private void setProxyUpCommand(UICommand value) {
        proxyUpCommand = value;
    }

    private UICommand proxyDownCommand;

    public UICommand getProxyDownCommand() {
        return proxyDownCommand;
    }

    private void setProxyDownCommand(UICommand value) {
        proxyDownCommand = value;
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

    public ListModel<VDS> getExternalHostName()
    {
        return privateExternalHostName;
    }

    protected void setExternalHostName(ListModel<VDS> value)
    {
        privateExternalHostName = value;
    }

    private ListModel<ExternalEntityBase> privateExternalDiscoveredHosts;

    public ListModel<ExternalEntityBase> getExternalDiscoveredHosts() { return privateExternalDiscoveredHosts; }

    protected void setExternalDiscoveredHosts(ListModel<ExternalEntityBase> value) { privateExternalDiscoveredHosts = value; }

    private ListModel<ExternalEntityBase> privateExternalHostGroups;

    public ListModel<ExternalEntityBase> getExternalHostGroups() { return privateExternalHostGroups; }

    protected void setExternalHostGroups(ListModel<ExternalEntityBase> value) { privateExternalHostGroups = value; }

    private ListModel<ExternalEntityBase> privateExternalComputeResource;

    public ListModel<ExternalEntityBase> getExternalComputeResource() { return privateExternalComputeResource; }

    protected void setExternalComputeResource(ListModel<ExternalEntityBase> value) { privateExternalComputeResource = value; }

    private EntityModel<String> privateComment;

    public EntityModel<String> getComment()
    {
        return privateComment;
    }

    protected void setComment(EntityModel<String> value)
    {
        privateComment = value;
    }

    private EntityModel<Boolean> externalHostProviderEnabled;

    public EntityModel<Boolean> getExternalHostProviderEnabled() {
        return externalHostProviderEnabled;
    }

    public void setExternalHostProviderEnabled(EntityModel<Boolean> externalHostProviderEnabled) {
        this.externalHostProviderEnabled = externalHostProviderEnabled;
    }

    private ListModel<Provider> privateProviders;

    public ListModel<Provider> getProviders()
    {
        return privateProviders;
    }

    protected void setProviders(ListModel<Provider> value)
    {
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

    public EntityModel<Boolean> getIsDiscoveredHosts()
    {
        return isDiscoveredHosts;
    }

    public void setIsDiscoveredHosts(EntityModel<Boolean> value)
    {
        isDiscoveredHosts = value;
    }

    public ListModel<Provider<org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties>> getNetworkProviders() {
        return getNetworkProviderModel().getNetworkProviders();
    }

    public EntityModel<String> getInterfaceMappings() {
        return getNetworkProviderModel().getInterfaceMappings();
    }

    public HostModel()
    {
        setTestCommand(new UICommand("Test", new ICommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand command) {
                test();
            }

            @Override
            public void executeCommand(UICommand uiCommand, Object... parameters) {
                test();
            }
        }));
        setUpdateHostsCommand(new UICommand("", new ICommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand command) {
                updateHosts();
            }

            @Override
            public void executeCommand(UICommand uiCommand, Object... parameters) {
                updateHosts();
            }
        }));
        setProxyUpCommand(new UICommand("Up", new ICommandTarget() {    //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand command) {
                proxyUp();
            }

            @Override
            public void executeCommand(UICommand uiCommand, Object... parameters) {
                proxyUp();
            }
        }));
        setProxyDownCommand(new UICommand("Down", new ICommandTarget() {    //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand command) {
                proxyDown();
            }

            @Override
            public void executeCommand(UICommand uiCommand, Object... parameters) {
                proxyDown();
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
        setPkSection(new EntityModel());
        setPasswordSection(new EntityModel());
        setDiscoveredHostSection(new EntityModel());
        setProvisionedHostSection(new EntityModel());
        setAuthSshPort(new EntityModel<Integer>());
        getAuthSshPort().setEntity(Integer.parseInt(constants.defaultHostSSHPort()));
        setUserName(new EntityModel<String>());
        getUserName().setEntity(RootUserName);
        // TODO: remove setIsChangable when configured ssh username is enabled
        getUserName().setIsChangable(false);
        setFetchSshFingerprint(new EntityModel<String>());
        getFetchSshFingerprint().setEntity(constants.empty());
        setUserPassword(new EntityModel<String>());
        getUserPassword().setEntity(constants.empty());
        setPublicKey(new EntityModel<String>());
        getPublicKey().setEntity(constants.empty());
        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        setCluster(new ListModel<VDSGroup>());
        getCluster().getSelectedItemChangedEvent().addListener(this);
        setPort(new EntityModel<Integer>());
        setFetchResult(new EntityModel<String>());
        setOverrideIpTables(new EntityModel<Boolean>());
        getOverrideIpTables().setEntity(false);
        setProtocol(new EntityModel());
        getProtocol().setEntity(false);

        IEventListener pmListener = new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updatePmModels();
            }
        };

        setExternalHostName(new ListModel<VDS>());
        getExternalHostName().setIsAvailable(false);
        setExternalHostProviderEnabled(new EntityModel<Boolean>());
        getExternalHostProviderEnabled().setIsAvailable(false);
        setProviders(new ListModel());
        getProviders().setIsAvailable(false);
        setProviderSearchFilter(new EntityModel<String>());
        getProviderSearchFilter().setIsAvailable(false);
        setProviderSearchFilterLabel(new EntityModel<String>());
        getProviderSearchFilterLabel().setIsAvailable(false);
        setExternalDiscoveredHosts(new ListModel());
        setExternalHostGroups(new ListModel());
        getExternalHostGroups().setIsChangable(true);
        setExternalComputeResource(new ListModel());
        getExternalComputeResource().setIsChangable(true);
        getUpdateHostsCommand().setIsExecutionAllowed(false);

        // Initialize primary PM fields.
        setManagementIp(new EntityModel<String>());
        setPmUserName(new EntityModel<String>());
        setPmPassword(new EntityModel<String>());
        setPmType(new ListModel<String>());
        getPmType().getSelectedItemChangedEvent().addListener(pmListener);
        setPmPort(new EntityModel<String>());
        getPmPort().setIsAvailable(false);
        setPmSlot(new EntityModel<String>());
        getPmSlot().setIsAvailable(false);
        setPmOptions(new EntityModel<String>());
        setPmSecure(new EntityModel<Boolean>());
        getPmSecure().setIsAvailable(false);
        getPmSecure().setEntity(false);

        // Initialize secondary PM fields.
        setPmSecondaryIp(new EntityModel<String>());
        setPmSecondaryUserName(new EntityModel<String>());
        setPmSecondaryPassword(new EntityModel<String>());
        setPmSecondaryType(new ListModel<String>());
        getPmSecondaryType().getSelectedItemChangedEvent().addListener(pmListener);
        setPmSecondaryPort(new EntityModel<String>());
        getPmSecondaryPort().setIsAvailable(false);
        setPmSecondarySlot(new EntityModel<String>());
        getPmSecondarySlot().setIsAvailable(false);
        setPmSecondaryOptions(new EntityModel<String>());
        setPmSecondarySecure(new EntityModel<Boolean>());
        getPmSecondarySecure().setIsAvailable(false);
        getPmSecondarySecure().setEntity(false);

        // Initialize other PM fields.
        setPmSecondaryConcurrent(new EntityModel<Boolean>());
        getPmSecondaryConcurrent().setEntity(false);
        setDisableAutomaticPowerManagement(new EntityModel<Boolean>());
        getDisableAutomaticPowerManagement().setEntity(false);
        setPmKdumpDetection(new EntityModel<Boolean>());
        getPmKdumpDetection().setEntity(true);

        setPmVariants(new ListModel<String>());
        List<String> pmVariants = new ArrayList<String>();
        pmVariants.add(ConstantsManager.getInstance().getConstants().primaryPmVariant());
        pmVariants.add(ConstantsManager.getInstance().getConstants().secondaryPmVariant());
        getPmVariants().setItems(pmVariants);
        getPmVariants().setSelectedItem(pmVariants.get(0));

        setPmProxyPreferencesList(new ListModel<String>());
        getPmProxyPreferencesList().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
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


        setIsPowerManagementTabValid(true);
        setIsGeneralTabValid(getIsPowerManagementTabValid());

        setSpmPriority(new ListModel<EntityModel<Integer>>());

        initSpmPriorities();
        fetchPublicKey();

        setNetworkProviderModel(new HostNetworkProviderModel());
        setIsDiscoveredHosts(new EntityModel<Boolean>());
    }

    private void proxyUp() {
        if (getPmProxyPreferencesList().getItems() == null) {
            return;
        }

        List<String> list = new ArrayList<String>(getPmProxyPreferencesList().getItems());
        String selectedItem = getPmProxyPreferencesList().getSelectedItem();
        int selectedItemIndex = list.indexOf(selectedItem);

        // Check whether the selected item is first in the list.
        if (selectedItemIndex > 0) {
            list.remove(selectedItemIndex);
            list.add(selectedItemIndex - 1, selectedItem);

            getPmProxyPreferencesList().setItems(list);
            getPmProxyPreferencesList().setSelectedItem(selectedItem);
        }
    }

    private void proxyDown() {
        if (getPmProxyPreferencesList().getItems() == null) {
            return;
        }

        List<String> list = new ArrayList<String>(getPmProxyPreferencesList().getItems());
        String selectedItem = getPmProxyPreferencesList().getSelectedItem();
        int selectedItemIndex = list.indexOf(selectedItem);

        // Check whether the selected item is first in the list.
        if (selectedItemIndex < list.size()) {
            list.remove(selectedItemIndex);
            list.add(selectedItemIndex + 1, selectedItem);

            getPmProxyPreferencesList().setItems(list);
            getPmProxyPreferencesList().setSelectedItem(selectedItem);
        }
    }

    public void fetchPublicKey() {
        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                String pk = (String) result;
                if (pk != null && pk.length() > 0)
                {
                    getPublicKey().setEntity(pk);
                }
            }
        };
        AsyncDataProvider.getHostPublicKey(aQuery);
    }

    private void fetchSSHFingerprint() {
        // Cleaning up fields for initialization
        getFetchSshFingerprint().setEntity(ConstantsManager.getInstance().getConstants().empty());
        getFetchResult().setEntity(ConstantsManager.getInstance().getConstants().empty());

        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                String fingerprint = (String) result;
                if (fingerprint != null && fingerprint.length() > 0)
                {
                    getFetchSshFingerprint().setEntity(fingerprint);
                    getFetchResult().setEntity(ConstantsManager.getInstance().getConstants().successLoadingFingerprint());
                }
                else
                {
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
            AsyncDataProvider.getHostFingerprint(aQuery, getHost().getEntity());
        }
    }

    boolean spmInitialized;
    int maxSpmPriority;
    int defaultSpmPriority;

    private void initSpmPriorities() {

        AsyncDataProvider.getMaxSpmPriority(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                HostModel model = (HostModel) target;

                model.maxSpmPriority = (Integer) returnValue;
                initSpmPriorities1();
            }
        }));
    }

    private void initSpmPriorities1() {

        AsyncDataProvider.getDefaultSpmPriority(new AsyncQuery(this, new INewAsyncCallback() {
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

        List<EntityModel<Integer>> items = new ArrayList<EntityModel<Integer>>();

        if (value == null) {
            value = defaultSpmPriority;
        }

        int neverValue = -1;
        EntityModel<Integer> neverItem = new EntityModel<Integer>(constants.neverTitle(), neverValue);
        items.add(neverItem);
        int lowValue = defaultSpmPriority / 2;
        items.add(new EntityModel<Integer>(constants.lowTitle(), lowValue));
        items.add(new EntityModel<Integer>(constants.normalTitle(), defaultSpmPriority));
        int highValue = defaultSpmPriority + (maxSpmPriority - defaultSpmPriority) / 2;
        items.add(new EntityModel<Integer>(constants.highTitle(), highValue));

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
                EntityModel<Integer> customItem = new EntityModel<Integer>("Custom (" + value + ")", value);//$NON-NLS-1$ //$NON-NLS-2$

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
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getDataCenter())
        {
            dataCenter_SelectedItemChanged();
        }
        else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getCluster())
        {
            cluster_SelectedItemChanged();
        } else if (sender == getConsoleAddressEnabled()) {
            consoleAddressChanged();
        }
    }

    private void consoleAddressChanged() {
        boolean enabled = getConsoleAddressEnabled().getEntity();
        getConsoleAddress().setIsChangable(enabled);
    }

    private void dataCenter_SelectedItemChanged()
    {
        StoragePool dataCenter = getDataCenter().getSelectedItem();
        if (dataCenter != null)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result)
                {
                    HostModel hostModel = (HostModel) model;
                    ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) result;
                    StoragePool selectedDataCenter = getDataCenter().getSelectedItem();

                    // Update selected cluster only if the returned cluster list is indeed the selected datacenter's
                    // clusters
                    if (clusters.isEmpty()
                            || clusters.size() > 0
                            && clusters.get(0)
                                    .getStoragePoolId()
                                    .equals(selectedDataCenter.getId()))
                    {


                        if (hostModel.getIsNew()) {
                            updateClusterList(hostModel, clusters);
                        } else {
                            AsyncQuery architectureQuery = new AsyncQuery();

                            architectureQuery.setModel(new Object[] { hostModel, clusters });
                            architectureQuery.asyncCallback = new INewAsyncCallback() {
                                @Override
                                public void onSuccess(Object model, Object returnValue) {
                                    Object[] objArray = (Object[]) model;
                                    HostModel hostModel = (HostModel) objArray[0];
                                    ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) objArray[1];

                                    ArchitectureType architecture = (ArchitectureType) returnValue;

                                    ArrayList<VDSGroup> filteredClusters = new ArrayList<VDSGroup>();

                                    for (VDSGroup cluster : clusters) {
                                        if (architecture == ArchitectureType.undefined
                                                || cluster.getArchitecture() == ArchitectureType.undefined
                                                || cluster.getArchitecture() == architecture) {
                                            filteredClusters.add(cluster);
                                        }
                                    }

                                    updateClusterList(hostModel, filteredClusters);
                                }
                            };

                            AsyncDataProvider.getHostArchitecture(architectureQuery, hostModel.getHostId());

                        }
                    }
                }
            };

            AsyncDataProvider.getClusterList(_asyncQuery, dataCenter.getId());
        }
    }

    private void updateClusterList(HostModel hostModel, List<VDSGroup> clusters) {
        VDSGroup oldCluster = hostModel.getCluster().getSelectedItem();

        hostModel.getCluster().setItems(clusters);

        if (oldCluster != null)
        {
            VDSGroup newSelectedItem =
                    Linq.firstOrDefault(clusters, new Linq.ClusterPredicate(oldCluster.getId()));
            if (newSelectedItem != null)
            {
                hostModel.getCluster().setSelectedItem(newSelectedItem);
            }
        }

        if (hostModel.getCluster().getSelectedItem() == null)
        {
            hostModel.getCluster().setSelectedItem(Linq.firstOrDefault(clusters));
        }

    }

    private void cluster_SelectedItemChanged()
    {
        VDSGroup cluster = getCluster().getSelectedItem();
        if (cluster != null)
        {
            AsyncDataProvider.getPmTypeList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {

                    ArrayList<String> pmTypes = (ArrayList<String>) returnValue;
                    updatePmTypeList(pmTypes, getPmType());
                    updatePmTypeList(pmTypes, getPmSecondaryType());
                }
            }), cluster.getcompatibility_version());
        }
    }

    private void updatePmTypeList(List<String> pmTypes, ListModel<String> model) {

        String pmType = model.getSelectedItem();

        model.setItems(pmTypes);

        if (pmTypes.contains(pmType)) {
            model.setSelectedItem(pmType);
        }
    }

    private void setPmOptionsMapInternal(Map<String, String> value, EntityModel<String> port, EntityModel<String> slot, EntityModel<Boolean> secure, EntityModel<String> options) {

        StringBuilder pmOptions = new StringBuilder();

        for (Map.Entry<String, String> pair : value.entrySet()) {
            String k = pair.getKey();
            String v = pair.getValue();

            if (PmPortKey.equals(k)) {
                port.setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k)); //$NON-NLS-1$

            } else if (PmSlotKey.equals(k)) {
                slot.setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k)); //$NON-NLS-1$

            } else if (PmSecureKey.equals(k)) {
                secure.setEntity(Boolean.parseBoolean(value.get(k)));

            } else {
                // Compose custom string from unknown pm options.
                if (StringHelper.isNullOrEmpty(v)) {
                    pmOptions.append(k).append(","); //$NON-NLS-1$
                } else {
                    pmOptions.append(k).append("=").append(v).append(","); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        String pmOptionsValue = pmOptions.toString();
        if (!StringHelper.isNullOrEmpty(pmOptionsValue)) {
            options.setEntity(pmOptionsValue.substring(0, pmOptionsValue.length() - 1));
        }
    }

    private HashMap<String, String> getPmOptionsMapInternal(EntityModel<String> port, EntityModel<String> slot, EntityModel<Boolean> secure, EntityModel<String> options) {

        HashMap<String, String> dict = new HashMap<String, String>();

        // Add well known pm options.
        if (port.getIsAvailable() && port.getEntity() != null) {
            dict.put(PmPortKey, port.getEntity());
        }
        if (slot.getIsAvailable() && slot.getEntity() != null) {
            dict.put(PmSlotKey, slot.getEntity());
        }
        if (secure.getIsAvailable()) {
            dict.put(PmSecureKey, secure.getEntity().toString());
        }

        // Add unknown pm options.
        // Assume Validate method was called before this getter.
        String pmOptions = options.getEntity();
        if (!StringHelper.isNullOrEmpty(pmOptions)) {
            for (String pair : pmOptions.split("[,]", -1)) //$NON-NLS-1$
            {
                String[] array = pair.split("[=]", -1); //$NON-NLS-1$
                if (array.length == 2) {
                    dict.put(array[0], array[1]);
                } else if (array.length == 1) {
                    dict.put(array[0], ""); //$NON-NLS-1$
                }
            }
        }

        return dict;
    }

    private void updatePmModels()
    {
        boolean isPm = getIsPm().getEntity();
        final String ciscoUcsValue = "cisco_ucs"; //$NON-NLS-1$

        // Update primary PM fields.
        getManagementIp().setIsChangable(isPm);
        getManagementIp().setIsValid(true);
        getPmUserName().setIsChangable(isPm);
        getPmUserName().setIsValid(true);
        getPmPassword().setIsChangable(isPm);
        getPmPassword().setIsValid(true);
        getPmType().setIsChangable(isPm);
        getPmType().setIsValid(true);
        getPmPort().setIsChangable(isPm);
        getPmPort().setIsValid(true);
        getPmProxyPreferencesList().setIsChangable(getIsPm().getEntity());
        String proxySelectedItem = getPmProxyPreferencesList().getSelectedItem();
        getTestCommand().setIsExecutionAllowed(isPm);
        getProxyUpCommand().setIsExecutionAllowed(isPm && proxySelectedItem != null);
        getProxyDownCommand().setIsExecutionAllowed(isPm && proxySelectedItem != null);
        getPmSlot().setIsChangable(isPm);
        getPmOptions().setIsChangable(isPm);
        getPmOptions().setIsValid(true);
        getPmSecure().setIsChangable(isPm);
        VDSGroup cluster = getCluster().getSelectedItem();
        String version = AsyncDataProvider.getDefaultConfigurationVersion();
        if (cluster != null) {
            version = cluster.getcompatibility_version().toString();
        }
        String pmType = getPmType().getSelectedItem();
        if (!StringHelper.isNullOrEmpty(pmType)) {
            AsyncDataProvider.getPmOptions(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {

                    List<String> pmOptions = (ArrayList<String>) returnValue;
                    if (pmOptions != null) {
                        getPmPort().setIsAvailable(pmOptions.contains(PmPortKey));
                        getPmSlot().setIsAvailable(pmOptions.contains(PmSlotKey));
                        getPmSecure().setIsAvailable(pmOptions.contains(PmSecureKey));
                    }
                }
            }), pmType, version);
            setCiscoUcsPrimaryPmTypeSelected(pmType.equals(ciscoUcsValue));
        } else {
            getPmPort().setIsAvailable(false);
            getPmSlot().setIsAvailable(false);
            getPmSecure().setIsAvailable(false);
        }

        // Update secondary PM fields.
        getPmSecondaryIp().setIsChangable(isPm);
        getPmSecondaryIp().setIsValid(true);
        getPmSecondaryUserName().setIsChangable(isPm);
        getPmSecondaryUserName().setIsValid(true);
        getPmSecondaryPassword().setIsChangable(isPm);
        getPmSecondaryPassword().setIsValid(true);
        getPmSecondaryType().setIsChangable(isPm);
        getPmSecondaryType().setIsValid(true);
        getPmSecondaryPort().setIsChangable(isPm);
        getPmSecondaryPort().setIsValid(true);
        getPmSecondarySlot().setIsChangable(isPm);
        getPmSecondaryOptions().setIsChangable(isPm);
        getPmSecondaryOptions().setIsValid(true);
        getPmSecondarySecure().setIsChangable(isPm);
        getDisableAutomaticPowerManagement().setIsValid(true);
        getDisableAutomaticPowerManagement().setIsChangable(isPm);

        String pmSecondaryType = getPmSecondaryType().getSelectedItem();
        if (!StringHelper.isNullOrEmpty(pmSecondaryType)) {
            AsyncDataProvider.getPmOptions(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {

                    List<String> pmOptions = (ArrayList<String>) returnValue;

                    if (pmOptions != null) {
                        getPmSecondaryPort().setIsAvailable(pmOptions.contains(PmPortKey));
                        getPmSecondarySlot().setIsAvailable(pmOptions.contains(PmSlotKey));
                        getPmSecondarySecure().setIsAvailable(pmOptions.contains(PmSecureKey));
                    }
                }
            }), pmSecondaryType, version);
            setCiscoUcsSecondaryPmTypeSelected(pmSecondaryType.equals(ciscoUcsValue));
        } else {
            getPmSecondaryPort().setIsAvailable(false);
            getPmSecondarySlot().setIsAvailable(false);
            getPmSecondarySecure().setIsAvailable(false);
        }

        // Update other PM fields.
        getPmVariants().setIsChangable(isPm);
        getPmSecondaryConcurrent().setIsChangable(isPm);
        getPmKdumpDetection().setIsChangable(isPm);
        getTestCommand().setIsExecutionAllowed(isPm);
    }

    private boolean isPmPrimarySelected() {

        List items = (List) getPmVariants().getItems();
        String selectedItem = getPmVariants().getSelectedItem();

        return items.indexOf(selectedItem) == 0;
    }

        // Validate user input.
    public void test()
        {
        boolean isPrimary = isPmPrimarySelected();
            getCluster().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
            validatePmModels(isPrimary);

        if (isPrimary && (!getManagementIp().getIsValid()
            || !getPmUserName().getIsValid()
            || !getPmPassword().getIsValid()
            || !getPmType().getIsValid()
            || !getPmPort().getIsValid()
            || !getPmOptions().getIsValid())) {
            return;
        }

        if (!isPrimary && (!getPmSecondaryIp().getIsValid()
            || !getPmSecondaryUserName().getIsValid()
            || !getPmSecondaryPassword().getIsValid()
            || !getPmSecondaryType().getIsValid()
            || !getPmSecondaryPort().getIsValid()
            || !getPmSecondaryOptions().getIsValid())) {
            return;
        }

        setMessage(ConstantsManager.getInstance().getConstants().testingInProgressItWillTakeFewSecondsPleaseWaitMsg());
        getTestCommand().setIsExecutionAllowed(false);

        VDSGroup cluster = getCluster().getSelectedItem();

        GetNewVdsFenceStatusParameters param = new GetNewVdsFenceStatusParameters();
        if (getHostId() != null)
        {
            param.setVdsId(getHostId());
        }
        param.setOrder(isPrimary ? FenceAgentOrder.Primary : FenceAgentOrder.Secondary);
        param.setManagementIp(isPrimary ? getManagementIp().getEntity() : getPmSecondaryIp().getEntity());
        param.setPmType(isPrimary ? getPmType().getSelectedItem() : getPmSecondaryType().getSelectedItem());
        param.setUser(isPrimary ? getPmUserName().getEntity() : getPmSecondaryUserName().getEntity());
        param.setPassword(isPrimary ? getPmPassword().getEntity() : getPmSecondaryPassword().getEntity());
        param.setStoragePoolId(cluster.getStoragePoolId() != null ? cluster.getStoragePoolId() : Guid.Empty);
        param.setFencingOptions(getPmOptionsMap());
        param.setPmProxyPreferences(getPmProxyPreferences());

        Frontend.getInstance().runQuery(VdcQueryType.GetNewVdsFenceStatus, param, new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                VdcQueryReturnValue response = (VdcQueryReturnValue) returnValue;
                if (response == null || !response.getSucceeded()) {
                    String message;
                    if (response != null && response.getReturnValue() != null) {
                        FenceStatusReturnValue fenceStatusReturnValue =
                                response.getReturnValue();
                        message = fenceStatusReturnValue.toString();
                    } else {
                        message = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
                    }
                    setMessage(message);
                    getTestCommand().setIsExecutionAllowed(true);
                } else {

                    if (response.getReturnValue() != null) {
                        FenceStatusReturnValue fenceStatusReturnValue =
                                response.getReturnValue();
                        String message = fenceStatusReturnValue.toString();
                        setMessage(message);
                        getTestCommand().setIsExecutionAllowed(true);
                    }

                }
            }
        }
                , true));
    }

    private void validatePmModels(boolean primary)
    {
        EntityModel<String> ip = primary ? getManagementIp() : getPmSecondaryIp();
        EntityModel<String> userName = primary ? getPmUserName() : getPmSecondaryUserName();
        EntityModel<String> password = primary ? getPmPassword() : getPmSecondaryPassword();
        ListModel<String> type = primary ? getPmType() : getPmSecondaryType();
        EntityModel<String> port = primary ? getPmPort() : getPmSecondaryPort();
        EntityModel<String> options = primary ? getPmOptions() : getPmSecondaryOptions();

        ip.validateEntity(new IValidation[] {new NotEmptyValidation(), new HostAddressValidation()});
        userName.validateEntity(new IValidation[] {new NotEmptyValidation()});
        password.validateEntity(new IValidation[] {new NotEmptyValidation(), new LengthValidation(50)});
        type.validateSelectedItem(new IValidation[] {new NotEmptyValidation()});
        port.validateEntity(new IValidation[] {new IntegerValidation(1, 65535)});
        options.validateEntity(new IValidation[] {new KeyValuePairValidation(true)});
    }

    public boolean validate()
    {
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

        getAuthSshPort().validateEntity(new IValidation[] {new NotEmptyValidation(), new IntegerValidation(1, 65535)});

        if (getConsoleAddressEnabled().getEntity()) {
            getConsoleAddress().validateEntity(new IValidation[] {new NotEmptyValidation(), new HostAddressValidation()});
        } else {
            // the console address is ignored so can not be invalid
            getConsoleAddress().setIsValid(true);
        }

        getDataCenter().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getCluster().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        if (getIsPm().getEntity())
        {
            // If PM enabled primary fencing options must be specified, ensure that.
            validatePmModels(true);

            // Secondary fencing options aren't mandatory, only ensure there was set
            // if one of the related fields was filled.
            if (!isEntityModelEmpty(getPmSecondaryIp())
                || !isEntityModelEmpty(getPmSecondaryUserName())
                || !isEntityModelEmpty(getPmSecondaryPassword())
                || !isEntityModelEmpty(getPmSecondaryPort())
                || !isEntityModelEmpty(getPmSecondarySlot())
                || !isEntityModelEmpty(getPmSecondaryOptions())) {

                getPmSecondaryIp().setIsValid(true);
                getPmSecondaryUserName().setIsValid(true);
                getPmSecondaryPassword().setIsValid(true);
                getPmSecondaryPort().setIsValid(true);
                getPmSecondarySlot().setIsValid(true);
                getPmSecondaryOptions().setIsValid(true);

                validatePmModels(false);
            }
        }

        setIsGeneralTabValid(getName().getIsValid()
                && getComment().getIsValid()
                && getHost().getIsValid()
                && getAuthSshPort().getIsValid()
                && getCluster().getIsValid()
                && getExternalHostGroups().getIsValid()
                && getExternalComputeResource().getIsValid()
                && getUserPassword().getIsValid()
        );

        setIsPowerManagementTabValid(getManagementIp().getIsValid()
                && getPmUserName().getIsValid()
                && getPmPassword().getIsValid()
                && getPmType().getIsValid()
                && getPmPort().getIsValid()
                && getPmOptions().getIsValid()

                && getPmSecondaryIp().getIsValid()
                && getPmSecondaryUserName().getIsValid()
                && getPmSecondaryPassword().getIsValid()
                && getPmSecondaryType().getIsValid()
                && getPmSecondaryPort().getIsValid()
                && getPmSecondaryOptions().getIsValid());

        getNetworkProviderModel().validate();

        return getIsGeneralTabValid() && getIsPowerManagementTabValid() && getConsoleAddress().getIsValid()
                && getNetworkProviderModel().getIsValid();
    }

    private boolean isEntityModelEmpty(EntityModel<String> model) {
        return !(model.getEntity() != null && !model.getEntity().equals(""));
    }

    public void updateModelFromVds(VDS vds,
            ArrayList<StoragePool> dataCenters,
            boolean isEditWithPMemphasis,
            SystemTreeItemModel selectedSystemTreeItem)
    {
        setHostId(vds.getId());
        getOverrideIpTables().setIsAvailable(showInstallationProperties());
        getProtocol().setEntity(VdsProtocol.STOMP == vds.getProtocol());
        getProtocol().setIsAvailable(showTransportProperties(vds));
        setSpmPriorityValue(vds.getVdsSpmPriority());
        setOriginalName(vds.getName());
        getName().setEntity(vds.getName());
        getComment().setEntity(vds.getComment());
        getHost().setEntity(vds.getHostName());
        getFetchSshFingerprint().setEntity(vds.getSshKeyFingerprint());
        getUserName().setEntity(vds.getSshUsername());
        getAuthSshPort().setEntity(vds.getSshPort());
        setPort(vds);
        boolean consoleAddressEnabled = vds.getConsoleAddress() != null;
        getConsoleAddressEnabled().setEntity(consoleAddressEnabled);
        getConsoleAddress().setEntity(vds.getConsoleAddress());
        getConsoleAddress().setIsChangable(consoleAddressEnabled);

        if (!showInstallationProperties()) {
            getPkSection().setIsChangable(false);
            getPkSection().setIsAvailable(false);

            // Use public key when edit or approve host
            setAuthenticationMethod(AuthenticationMethod.PublicKey);
        }

        setAllowChangeHost(vds);

        // Set primary PM parameters.
        getManagementIp().setEntity(vds.getManagementIp());
        getPmUserName().setEntity(vds.getPmUser());
        getPmPassword().setEntity(vds.getPmPassword());
        getPmType().setSelectedItem(vds.getPmType());
        setPmOptionsMap(VdsStatic.pmOptionsStringToMap(vds.getPmOptions()));

        // Set secondary PM parameters.
        getPmSecondaryIp().setEntity(vds.getPmSecondaryIp());
        getPmSecondaryUserName().setEntity(vds.getPmSecondaryUser());
        getPmSecondaryPassword().setEntity(vds.getPmSecondaryPassword());
        getPmSecondaryType().setSelectedItem(vds.getPmSecondaryType());
        setPmSecondaryOptionsMap(vds.getPmSecondaryOptionsMap());

        // Set other PM parameters.
        if (isEditWithPMemphasis) {
            setIsPowerManagementTabSelected(true);
            getIsPm().setEntity(true);
            getIsPm().setIsChangable(false);
        } else {
            getIsPm().setEntity(vds.getpm_enabled());
        }

        getPmSecondaryConcurrent().setEntity(vds.isPmSecondaryConcurrent());
        getDisableAutomaticPowerManagement().setEntity(vds.isDisablePowerManagementPolicy());
        getPmKdumpDetection().setEntity(vds.isPmKdumpDetection());

        updateModelDataCenterFromVds(dataCenters, vds);

        ArrayList<VDSGroup> clusters;
        if (getCluster().getItems() == null)
        {
            VDSGroup tempVar = new VDSGroup();
            tempVar.setName(vds.getVdsGroupName());
            tempVar.setId(vds.getVdsGroupId());
            tempVar.setcompatibility_version(vds.getVdsGroupCompatibilityVersion());
            getCluster()
                    .setItems(new ArrayList<VDSGroup>(Arrays.asList(new VDSGroup[] { tempVar })));
        }
        clusters = (ArrayList<VDSGroup>) getCluster().getItems();
        updateModelClusterFromVds(clusters, vds);
        if (getCluster().getSelectedItem() == null)
        {
            getCluster().setSelectedItem(Linq.firstOrDefault(clusters));
        }

        if (vds.getStatus() != VDSStatus.Maintenance &&
            vds.getStatus() != VDSStatus.PendingApproval &&
            vds.getStatus() != VDSStatus.InstallingOS) {
            setAllowChangeHostPlacementPropertiesWhenNotInMaintenance();
        }
        else if (selectedSystemTreeItem != null)
        {
            final UIConstants constants = ConstantsManager.getInstance().getConstants();

            switch (selectedSystemTreeItem.getType())
            {
            case Host:
                getName().setIsChangable(false);
                getName().setChangeProhibitionReason(constants.cannotEditNameInTreeContext());
                break;
            case Hosts:
            case Cluster:
            case Cluster_Gluster:
                getCluster().setIsChangable(false);
                getCluster().setChangeProhibitionReason(constants.cannotChangeClusterInTreeContext());
                getDataCenter().setIsChangable(false);
                break;
            case DataCenter:
                StoragePool selectDataCenter = (StoragePool) selectedSystemTreeItem.getEntity();
                getDataCenter()
                        .setItems(new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { selectDataCenter })));
                getDataCenter().setSelectedItem(selectDataCenter);
                getDataCenter().setIsChangable(false);
                break;
            default:
                break;
            }
        }
    }

    public void cleanHostParametersFields() {
        getName().setEntity(constants.empty());
        getComment().setEntity(constants.empty());
        getAuthSshPort().setEntity(Integer.parseInt(constants.defaultHostSSHPort()));
        getHost().setEntity(constants.empty());
        getUserPassword().setEntity(constants.empty());
        getFetchSshFingerprint().setEntity(constants.empty());
        getExternalHostName().setItems(null);
        getExternalHostName().setIsChangable(false);
        getExternalDiscoveredHosts().setItems(null);
        getExternalDiscoveredHosts().setIsChangable(false);
        getExternalHostGroups().setItems(null);
        getExternalHostGroups().setIsChangable(false);
        getExternalComputeResource().setItems(null);
        getExternalComputeResource().setIsChangable(false);

    }

    protected abstract boolean showInstallationProperties();

    protected abstract boolean showTransportProperties(VDS vds);

    public abstract boolean showExternalProviderPanel();

    protected abstract void updateModelDataCenterFromVds(ArrayList<StoragePool> dataCenters, VDS vds);

    protected abstract void updateModelClusterFromVds(ArrayList<VDSGroup> clusters, VDS vds);

    protected abstract void setAllowChangeHost(VDS vds);

    protected abstract void setAllowChangeHostPlacementPropertiesWhenNotInMaintenance();

    public abstract void updateHosts();

    protected abstract void setPort(VDS vds);

    public abstract boolean showNetworkProviderTab();
}
