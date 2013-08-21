package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.FenceAgentOrder;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetNewVdsFenceStatusParameters;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
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
import org.ovirt.engine.ui.uicommonweb.models.providers.NeutronAgentModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.NeutronPluginTranslator;
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

    private EntityModel privateName;

    public EntityModel getName()
    {
        return privateName;
    }

    private void setName(EntityModel value)
    {
        privateName = value;
    }

    private EntityModel privateUserName;

    public EntityModel getUserName()
    {
        return privateUserName;
    }

    private void setUserName(EntityModel value)
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

    private EntityModel privatePasswordSection;

    public EntityModel getPasswordSection()
    {
        return privatePasswordSection;
    }

    private void setPasswordSection(EntityModel value)
    {
        privatePasswordSection = value;
    }

    private EntityModel privateFetchSshFingerprint;

    public EntityModel getFetchSshFingerprint()
    {
        return privateFetchSshFingerprint;
    }

    private void setFetchSshFingerprint(EntityModel value)
    {
        privateFetchSshFingerprint = value;
    }

    private EntityModel privateAuthSshPort;

    public EntityModel getAuthSshPort()
    {
        return privateAuthSshPort;
    }

    private void setAuthSshPort(EntityModel value)
    {
        privateAuthSshPort = value;
    }

    private EntityModel privateUserPassword;

    public EntityModel getUserPassword()
    {
        return privateUserPassword;
    }

    private void setUserPassword(EntityModel value)
    {
        privateUserPassword = value;
    }

    private EntityModel privatePublicKey;

    public EntityModel getPublicKey()
    {
        return privatePublicKey;
    }

    private void setPublicKey(EntityModel value)
    {
        privatePublicKey = value;
    }

    private EntityModel privateProviderSearchFilterLabel;

    public EntityModel getProviderSearchFilterLabel()
    {
        return privateProviderSearchFilterLabel;
    }

    private void setProviderSearchFilterLabel(EntityModel value)
    {
        privateProviderSearchFilterLabel = value;
    }

    private EntityModel privateProviderSearchFilter;

    public EntityModel getProviderSearchFilter()
    {
        return privateProviderSearchFilter;
    }

    private void setProviderSearchFilter(EntityModel value)
    {
        privateProviderSearchFilter = value;
    }

    private EntityModel privateHost;

    public EntityModel getHost()
    {
        return privateHost;
    }

    private void setHost(EntityModel value)
    {
        privateHost = value;
    }

    private EntityModel privateManagementIp;

    public EntityModel getManagementIp()
    {
        return privateManagementIp;
    }

    private void setManagementIp(EntityModel value)
    {
        privateManagementIp = value;
    }

    private ListModel privateDataCenter;

    public ListModel getDataCenter()
    {
        return privateDataCenter;
    }

    private void setDataCenter(ListModel value)
    {
        privateDataCenter = value;
    }

    private ListModel privateCluster;

    public ListModel getCluster()
    {
        return privateCluster;
    }

    private void setCluster(ListModel value)
    {
        privateCluster = value;
    }

    private EntityModel privatePort;

    public EntityModel getPort()
    {
        return privatePort;
    }

    private void setPort(EntityModel value)
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

    private EntityModel privateFetchResult;

    public EntityModel getFetchResult()
    {
        return privateFetchResult;
    }

    private void setFetchResult(EntityModel value)
    {
        privateFetchResult = value;
    }

    private EntityModel privateOverrideIpTables;

    public EntityModel getOverrideIpTables()
    {
        return privateOverrideIpTables;
    }

    private void setOverrideIpTables(EntityModel value)
    {
        privateOverrideIpTables = value;
    }

    private EntityModel privateIsPm;

    public EntityModel getIsPm()
    {
        return privateIsPm;
    }

    private void setIsPm(EntityModel value)
    {
        privateIsPm = value;
    }

    private EntityModel privatePmUserName;

    public EntityModel getPmUserName()
    {
        return privatePmUserName;
    }

    private void setPmUserName(EntityModel value)
    {
        privatePmUserName = value;
    }

    private EntityModel privatePmPassword;

    public EntityModel getPmPassword()
    {
        return privatePmPassword;
    }

    private void setPmPassword(EntityModel value)
    {
        privatePmPassword = value;
    }

    private EntityModel consoleAddress;

    public void setConsoleAddress(EntityModel consoleAddress) {
        this.consoleAddress = consoleAddress;
    }

    public EntityModel getConsoleAddress() {
        return consoleAddress;
    }

    private EntityModel consoleAddressEnabled;

    public EntityModel getConsoleAddressEnabled() {
        return consoleAddressEnabled;
    }

    public void setConsoleAddressEnabled(EntityModel consoleAddressEnabled) {
        this.consoleAddressEnabled = consoleAddressEnabled;
    }

    private ListModel privatePmType;

    public ListModel getPmType()
    {
        return privatePmType;
    }

    private void setPmType(ListModel value)
    {
        privatePmType = value;
    }

    private EntityModel privatePmSecure;

    public EntityModel getPmSecure()
    {
        return privatePmSecure;
    }

    private void setPmSecure(EntityModel value)
    {
        privatePmSecure = value;
    }

    private EntityModel privatePmPort;

    public EntityModel getPmPort()
    {
        return privatePmPort;
    }

    private void setPmPort(EntityModel value)
    {
        privatePmPort = value;
    }

    private EntityModel privatePmSlot;

    public EntityModel getPmSlot()
    {
        return privatePmSlot;
    }

    private void setPmSlot(EntityModel value)
    {
        privatePmSlot = value;
    }

    private EntityModel privatePmOptions;

    public EntityModel getPmOptions()
    {
        return privatePmOptions;
    }

    private void setPmOptions(EntityModel value)
    {
        privatePmOptions = value;
    }

    private EntityModel pmSecondaryIp;

    public EntityModel getPmSecondaryIp() {
        return pmSecondaryIp;
    }

    private void setPmSecondaryIp(EntityModel value) {
        pmSecondaryIp = value;
    }

    private EntityModel pmSecondaryPort;

    public EntityModel getPmSecondaryPort() {
        return pmSecondaryPort;
    }

    private void setPmSecondaryPort(EntityModel value) {
        pmSecondaryPort = value;
    }

    private EntityModel pmSecondaryUserName;

    public EntityModel getPmSecondaryUserName() {
        return pmSecondaryUserName;
    }

    private void setPmSecondaryUserName(EntityModel value) {
        pmSecondaryUserName = value;
    }

    private EntityModel pmSecondaryPassword;

    public EntityModel getPmSecondaryPassword() {
        return pmSecondaryPassword;
    }

    private void setPmSecondaryPassword(EntityModel value) {
        pmSecondaryPassword = value;
    }

    private ListModel pmSecondaryType;

    public ListModel getPmSecondaryType() {
        return pmSecondaryType;
    }

    private void setPmSecondaryType(ListModel value) {
        pmSecondaryType = value;
    }

    private EntityModel pmSecondaryOptions;

    public EntityModel getPmSecondaryOptions() {
        return pmSecondaryOptions;
    }

    private void setPmSecondaryOptions(EntityModel value) {
        pmSecondaryOptions = value;
    }

    private EntityModel pmSecondarySecure;

    public EntityModel getPmSecondarySecure() {
        return pmSecondarySecure;
    }

    private void setPmSecondarySecure(EntityModel value) {
        pmSecondarySecure = value;
    }

    public Map<String, String> getPmSecondaryOptionsMap() {

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

    private EntityModel pmSecondarySlot;

    public EntityModel getPmSecondarySlot() {
        return pmSecondarySlot;
    }

    private void setPmSecondarySlot(EntityModel value) {
        pmSecondarySlot = value;
    }


    private EntityModel pmSecondaryConcurrent;

    public EntityModel getPmSecondaryConcurrent() {
        return pmSecondaryConcurrent;
    }

    private void setPmSecondaryConcurrent(EntityModel value) {
        pmSecondaryConcurrent = value;
    }

    private ListModel pmVariants;

    public ListModel getPmVariants() {
        return pmVariants;
    }

    private void setPmVariants(ListModel value) {
        pmVariants = value;
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

    public Map<String, String> getPmOptionsMap() {
        return getPmOptionsMapInternal(getPmPort(), getPmSlot(), getPmSecure(), getPmOptions());
    }

    public void setPmOptionsMap(Map<String, String> value) {
        setPmOptionsMapInternal(value, getPmPort(), getPmSlot(), getPmSecure(), getPmOptions());
    }

    public String getPmProxyPreferences() {
        // Return null if power management is not enabled.
        if (!(Boolean) getIsPm().getEntity()) {
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

    private ListModel pmProxyPreferencesList;

    public ListModel getPmProxyPreferencesList() {
        return pmProxyPreferencesList;
    }

    private void setPmProxyPreferencesList(ListModel value) {
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

        EntityModel selectedItem = (EntityModel) getSpmPriority().getSelectedItem();
        if (selectedItem != null) {
            return (Integer) selectedItem.getEntity();
        }

        return 0;
    }

    private ListModel spmPriority;

    public ListModel getSpmPriority() {
        return spmPriority;
    }

    private void setSpmPriority(ListModel value) {
        spmPriority = value;
    }

    private ListModel privateExternalHostName;

    public ListModel getExternalHostName()
    {
        return privateExternalHostName;
    }

    protected void setExternalHostName(ListModel value)
    {
        privateExternalHostName = value;
    }

    private EntityModel privateComment;

    public EntityModel getComment()
    {
        return privateComment;
    }

    protected void setComment(EntityModel value)
    {
        privateComment = value;
    }

    private EntityModel externalHostProviderEnabled;

    public EntityModel getExternalHostProviderEnabled() {
        return externalHostProviderEnabled;
    }

    public void setExternalHostProviderEnabled(EntityModel externalHostProviderEnabled) {
        this.externalHostProviderEnabled = externalHostProviderEnabled;
    }

    private ListModel privateProviders;

    public ListModel getProviders()
    {
        return privateProviders;
    }

    protected void setProviders(ListModel value)
    {
        privateProviders = value;
    }

    private ListModel externalProviders;

    public void setExternalProviders(ListModel externalProviders) {
        this.externalProviders = externalProviders;
    }

    public ListModel getExternalProviders() {
        return externalProviders;
    }

    private ListModel providerType;

    public ListModel getProviderType() {
        return providerType;
    }

    protected void setProviderType(ListModel value) {
        providerType = value;
    }

    private ListModel providerPluginType;

    public ListModel getProviderPluginType() {
        return providerPluginType;
    }

    protected void setProviderPluginType(ListModel value) {
        providerPluginType = value;
    }

    private NeutronAgentModel neutronAgentModel;

    public NeutronAgentModel getNeutronAgentModel() {
        return neutronAgentModel;
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

        setName(new EntityModel());
        setComment(new EntityModel());
        setHost(new EntityModel());
        setPkSection(new EntityModel());
        setPasswordSection(new EntityModel());
        setAuthSshPort(new EntityModel());
        getAuthSshPort().setEntity(constants.defaultHostSSHPort());
        setUserName(new EntityModel());
        getUserName().setEntity(constants.defaultUserName());
        // TODO: remove setIsChangable when configured ssh username is enabled
        getUserName().setIsChangable(false);
        setFetchSshFingerprint(new EntityModel());
        getFetchSshFingerprint().setEntity(constants.empty());
        setUserPassword(new EntityModel());
        getUserPassword().setEntity(constants.empty());
        setPublicKey(new EntityModel());
        getPublicKey().setEntity(constants.empty());
        setDataCenter(new ListModel());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        setCluster(new ListModel());
        getCluster().getSelectedItemChangedEvent().addListener(this);
        setPort(new EntityModel());
        setFetchResult(new EntityModel());
        setOverrideIpTables(new EntityModel());
        getOverrideIpTables().setEntity(false);

        IEventListener pmListener = new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updatePmModels();
            }
        };

        setExternalHostName(new ListModel());
        getExternalHostName().setIsAvailable(false);
        setExternalHostProviderEnabled(new EntityModel());
        getExternalHostProviderEnabled().setIsAvailable(false);
        setProviders(new ListModel());
        getProviders().setIsAvailable(false);
        setProviderSearchFilter(new EntityModel());
        getProviderSearchFilter().setIsAvailable(false);
        setProviderSearchFilterLabel(new EntityModel());
        getProviderSearchFilterLabel().setIsAvailable(false);
        getUpdateHostsCommand().setIsExecutionAllowed(false);
        // Initialize primary PM fields.
        setManagementIp(new EntityModel());
        setPmUserName(new EntityModel());
        setPmPassword(new EntityModel());
        setPmType(new ListModel());
        getPmType().getSelectedItemChangedEvent().addListener(pmListener);
        setPmPort(new EntityModel());
        getPmPort().setIsAvailable(false);
        setPmSlot(new EntityModel());
        getPmSlot().setIsAvailable(false);
        setPmOptions(new EntityModel());
        setPmSecure(new EntityModel());
        getPmSecure().setIsAvailable(false);
        getPmSecure().setEntity(false);

        // Initialize secondary PM fields.
        setPmSecondaryIp(new EntityModel());
        setPmSecondaryUserName(new EntityModel());
        setPmSecondaryPassword(new EntityModel());
        setPmSecondaryType(new ListModel());
        getPmSecondaryType().getSelectedItemChangedEvent().addListener(pmListener);
        setPmSecondaryPort(new EntityModel());
        getPmSecondaryPort().setIsAvailable(false);
        setPmSecondarySlot(new EntityModel());
        getPmSecondarySlot().setIsAvailable(false);
        setPmSecondaryOptions(new EntityModel());
        setPmSecondarySecure(new EntityModel());
        getPmSecondarySecure().setIsAvailable(false);
        getPmSecondarySecure().setEntity(false);

        // Initialize other PM fields.
        setPmSecondaryConcurrent(new EntityModel());
        getPmSecondaryConcurrent().setEntity(false);

        setPmVariants(new ListModel());
        List<String> pmVariants = new ArrayList<String>();
        pmVariants.add(ConstantsManager.getInstance().getConstants().primaryPmVariant());
        pmVariants.add(ConstantsManager.getInstance().getConstants().secondaryPmVariant());
        getPmVariants().setItems(pmVariants);
        getPmVariants().setSelectedItem(pmVariants.get(0));

        setPmProxyPreferencesList(new ListModel());
        getPmProxyPreferencesList().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
               updatePmModels();
            }
        });

        setConsoleAddress(new EntityModel());
        getConsoleAddress().setEntity(null);
        setConsoleAddressEnabled(new EntityModel());
        getConsoleAddressEnabled().setEntity(false);
        getConsoleAddressEnabled().getEntityChangedEvent().addListener(this);

        setIsPm(new EntityModel());
        getIsPm().getEntityChangedEvent().addListener(pmListener);
        getIsPm().setEntity(false);


        setIsPowerManagementTabValid(true);
        setIsGeneralTabValid(getIsPowerManagementTabValid());

        setSpmPriority(new ListModel());

        initSpmPriorities();
        fetchPublicKey();

        setExternalProviders(new ListModel());
        getExternalProviders().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                onExternalProviderChanged();
            }
        });
        setProviderType(new ListModel());
        getProviderType().setIsChangable(false);
        getProviderType().setIsAvailable(false);
        setProviderPluginType(new ListModel());
        getProviderPluginType().setIsChangable(false);
        getProviderPluginType().setIsAvailable(false);
        neutronAgentModel = new NeutronAgentModel(getProviderType(), getProviderPluginType());
        neutronAgentModel.setIsChangable(false);
        neutronAgentModel.setIsAvailable(false);

        onExternalProviderChanged();
        initExternalProvidersList();
    }

    @SuppressWarnings("unchecked")
    private void onExternalProviderChanged() {
        Provider provider = (Provider) getExternalProviders().getSelectedItem();
        getProviderType().setIsAvailable(provider != null);
        getProviderType().setSelectedItem(provider == null ? null : provider.getType());
        if (getProviderType().getSelectedItem() == ProviderType.OPENSTACK_NETWORK) {
            OpenstackNetworkProviderProperties properties = ((Provider<OpenstackNetworkProviderProperties>)
                    getExternalProviders().getSelectedItem()).getAdditionalProperties();
            String pluginName = (properties == null) ? new String() : properties.getPluginType();
            getProviderPluginType().setSelectedItem(NeutronPluginTranslator.getDisplayStringForPluginName(pluginName));
            if (properties != null) {
                getNeutronAgentModel().init(properties.getAgentConfiguration());
            }
        }
    }

    private void initExternalProvidersList() {
        AsyncQuery getProvidersQuery = new AsyncQuery();
        getProvidersQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                ArrayList<Provider> providers = Linq.toList(Linq.filterNetworkProviders((ArrayList<Provider>) result));
                providers.add(0, null);
                getExternalProviders().setItems(providers);
                getExternalProviders().setSelectedItem(null);
            }
        };
        AsyncDataProvider.GetAllProviders(getProvidersQuery);
    }

    private void proxyUp() {
        if (getPmProxyPreferencesList().getItems() == null) {
            return;
        }

        List list = new ArrayList((List) getPmProxyPreferencesList().getItems());
        Object selectedItem = getPmProxyPreferencesList().getSelectedItem();
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

        List list = new ArrayList((List) getPmProxyPreferencesList().getItems());
        Object selectedItem = getPmProxyPreferencesList().getSelectedItem();
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
                    getPublicKey().setEntity(result);
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
                    getFetchSshFingerprint().setEntity(result);
                    getFetchResult().setEntity((String)
                            ConstantsManager.getInstance().getConstants().successLoadingFingerprint());
                }
                else
                {
                    getFetchResult().setEntity((String)
                             ConstantsManager.getInstance().getConstants().errorLoadingFingerprint());
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
            getFetchResult().setEntity((String) ConstantsManager.getInstance().getConstants().loadingFingerprint());
            AsyncDataProvider.getHostFingerprint(aQuery, getHost().getEntity().toString());
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

        List<EntityModel> items = new ArrayList<EntityModel>();

        if (value == null) {
            value = defaultSpmPriority;
        }

        int neverValue = -1;
        EntityModel neverItem = new EntityModel(constants.neverTitle(), neverValue);
        items.add(neverItem);
        int lowValue = defaultSpmPriority / 2;
        items.add(new EntityModel(constants.lowTitle(), lowValue));
        items.add(new EntityModel(constants.normalTitle(), defaultSpmPriority));
        int highValue = defaultSpmPriority + (maxSpmPriority - defaultSpmPriority) / 2;
        items.add(new EntityModel(constants.highTitle(), highValue));

        // Determine whether to set custom SPM priority, and where.
        EntityModel selectedItem = null;

        int[] values = new int[] { neverValue, lowValue, defaultSpmPriority, highValue, maxSpmPriority + 1 };
        Integer prevValue = null;

        for (int i = 0; i < values.length; i++) {

            int currentValue = values[i];

            if (value == currentValue) {
                selectedItem = items.get(i);
                break;
            } else if (prevValue != null && value > prevValue && value < currentValue) {
                EntityModel customItem = new EntityModel("Custom (" + value + ")", value);//$NON-NLS-1$ //$NON-NLS-2$

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
        boolean enabled = (Boolean) getConsoleAddressEnabled().getEntity();
        getConsoleAddress().setIsChangable(enabled);
    }

    private void dataCenter_SelectedItemChanged()
    {
        StoragePool dataCenter = (StoragePool) getDataCenter().getSelectedItem();
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
                    VDSGroup oldCluster = (VDSGroup) hostModel.getCluster().getSelectedItem();
                    StoragePool selectedDataCenter = (StoragePool) getDataCenter().getSelectedItem();

                    // Update selected cluster only if the returned cluster list is indeed the selected datacenter's
                    // clusters
                    if (clusters.isEmpty()
                            || clusters.size() > 0
                            && clusters.get(0)
                                    .getStoragePoolId()
                                    .equals(selectedDataCenter.getId()))
                    {
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
                }
            };

            AsyncDataProvider.getClusterList(_asyncQuery, dataCenter.getId());
        }
    }

    private void cluster_SelectedItemChanged()
    {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();
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

    private void updatePmTypeList(List<String> pmTypes, ListModel model) {

        String pmType = (String) model.getSelectedItem();

        model.setItems(pmTypes);

        if (pmTypes.contains(pmType)) {
            model.setSelectedItem(pmType);
        }
    }

    private void setPmOptionsMapInternal(Map<String,String> value, EntityModel port, EntityModel slot, EntityModel secure, EntityModel options) {

        StringBuilder pmOptions = new StringBuilder();

        for (Map.Entry<String, String> pair : value.entrySet()) {
            String k = pair.getKey();
            String v = pair.getValue();

            if (StringHelper.stringsEqual(k, PmPortKey)) {
                port.setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k)); //$NON-NLS-1$

            } else if (StringHelper.stringsEqual(k, PmSlotKey)) {
                slot.setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k)); //$NON-NLS-1$

            } else if (StringHelper.stringsEqual(k, PmSecureKey)) {
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

    private Map<String,String> getPmOptionsMapInternal(EntityModel port, EntityModel slot, EntityModel secure, EntityModel options) {

        Map<String, String> dict = new HashMap<String, String>();

        if ((Boolean) getIsPm().getEntity()) {
            // Add well known pm options.
            if (port.getIsAvailable() && port.getEntity() != null) {
                dict.put(PmPortKey, (String) port.getEntity());
            }
            if (slot.getIsAvailable() && slot.getEntity() != null) {
                dict.put(PmSlotKey, (String) slot.getEntity());
            }
            if (secure.getIsAvailable()) {
                dict.put(PmSecureKey, secure.getEntity().toString());
            }

            // Add unknown pm options.
            // Assume Validate method was called before this getter.
            String pmOptions = (String) options.getEntity();
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
        }

        return dict;
    }

    private void updatePmModels()
    {
        boolean isPm = (Boolean) getIsPm().getEntity();

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
        getPmProxyPreferencesList().setIsChangable((Boolean) getIsPm().getEntity());
        Object proxySelectedItem = getPmProxyPreferencesList().getSelectedItem();
        getTestCommand().setIsExecutionAllowed(isPm);
        getProxyUpCommand().setIsExecutionAllowed(isPm && proxySelectedItem != null);
        getProxyDownCommand().setIsExecutionAllowed(isPm && proxySelectedItem != null);
        getPmSlot().setIsChangable(isPm);
        getPmOptions().setIsChangable(isPm);
        getPmOptions().setIsValid(true);
        getPmSecure().setIsChangable(isPm);

        String pmType = (String) getPmType().getSelectedItem();
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
            }), pmType);
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

        String pmSecondaryType = (String) getPmSecondaryType().getSelectedItem();
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
            }), pmSecondaryType);
        } else {
            getPmSecondaryPort().setIsAvailable(false);
            getPmSecondarySlot().setIsAvailable(false);
            getPmSecondarySecure().setIsAvailable(false);
        }


        // Update other PM fields.
        getPmVariants().setIsChangable(isPm);
        getPmSecondaryConcurrent().setIsChangable(isPm);
        getTestCommand().setIsExecutionAllowed(isPm);
    }

    private boolean isPmPrimarySelected() {

        List items = (List) getPmVariants().getItems();
        Object selectedItem = getPmVariants().getSelectedItem();

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

        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();

        GetNewVdsFenceStatusParameters param = new GetNewVdsFenceStatusParameters();
        if (getHostId() != null)
        {
            param.setVdsId(getHostId());
        }
        param.setOrder(isPrimary ? FenceAgentOrder.Primary : FenceAgentOrder.Secondary);
        param.setManagementIp(isPrimary ? (String) getManagementIp().getEntity() : (String) getPmSecondaryIp().getEntity());
        param.setPmType(isPrimary ? (String) getPmType().getSelectedItem() : (String) getPmSecondaryType().getSelectedItem());
        param.setUser(isPrimary ? (String) getPmUserName().getEntity() : (String) getPmSecondaryUserName().getEntity());
        param.setPassword(isPrimary ? (String) getPmPassword().getEntity() : (String) getPmSecondaryPassword().getEntity());
        param.setStoragePoolId(cluster.getStoragePoolId() != null ? cluster.getStoragePoolId() : Guid.Empty);
        param.setFencingOptions(new ValueObjectMap(getPmOptionsMap(), false));
        param.setPmProxyPreferences(getPmProxyPreferences());

        Frontend.RunQuery(VdcQueryType.GetNewVdsFenceStatus, param, new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                VdcQueryReturnValue response = (VdcQueryReturnValue) returnValue;
                if (response == null || !response.getSucceeded()) {
                    String message;
                    if (response != null && response.getReturnValue() != null) {
                        FenceStatusReturnValue fenceStatusReturnValue =
                                (FenceStatusReturnValue) response.getReturnValue();
                        message = fenceStatusReturnValue.toString();
                    } else {
                        message = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
                    }
                    setMessage(message);
                    getTestCommand().setIsExecutionAllowed(true);
                } else {

                    if (response.getReturnValue() != null) {
                        FenceStatusReturnValue fenceStatusReturnValue =
                                (FenceStatusReturnValue) response.getReturnValue();
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
        EntityModel ip = primary ? getManagementIp() : getPmSecondaryIp();
        EntityModel userName = primary ? getPmUserName() : getPmSecondaryUserName();
        EntityModel password = primary ? getPmPassword() : getPmSecondaryPassword();
        ListModel type = primary ? getPmType() : getPmSecondaryType();
        EntityModel port = primary ? getPmPort() : getPmSecondaryPort();
        EntityModel options = primary ? getPmOptions() : getPmSecondaryOptions();

        ip.validateEntity(new IValidation[] {new NotEmptyValidation(), new HostAddressValidation()});
        userName.validateEntity(new IValidation[] {new NotEmptyValidation()});
        password.validateEntity(new IValidation[] {new NotEmptyValidation()});
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

        getAuthSshPort().validateEntity(new IValidation[] {new NotEmptyValidation(), new IntegerValidation(1, 65535)});

        if ((Boolean) getConsoleAddressEnabled().getEntity()) {
            getConsoleAddress().validateEntity(new IValidation[] {new NotEmptyValidation(), new HostAddressValidation()});
        } else {
            // the console address is ignored so can not be invalid
            getConsoleAddress().setIsValid(true);
        }

        getDataCenter().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getCluster().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        if ((Boolean) getIsPm().getEntity())
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

        neutronAgentModel.validate();

        return getIsGeneralTabValid() && getIsPowerManagementTabValid() && getConsoleAddress().getIsValid()
                && neutronAgentModel.getIsValid();
    }

    private boolean isEntityModelEmpty(EntityModel model) {
        return !(model.getEntity() != null && !model.getEntity().equals(""));
    }

    public void updateModelFromVds(VDS vds,
            ArrayList<StoragePool> dataCenters,
            boolean isEditWithPMemphasis,
            SystemTreeItemModel selectedSystemTreeItem)
    {
        setHostId(vds.getId());
        getOverrideIpTables().setIsAvailable(showInstallationProperties());
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

        getPkSection().setIsChangable(false);
        getPkSection().setIsAvailable(false);
        // Use public key when edit or approve host
        setAuthenticationMethod(AuthenticationMethod.PublicKey);

        setAllowChangeHost(vds);

        // Set primary PM parameters.
        getManagementIp().setEntity(vds.getManagementIp());
        getPmUserName().setEntity(vds.getPmUser());
        getPmPassword().setEntity(vds.getPmPassword());
        getPmType().setSelectedItem(vds.getPmType());
        setPmOptionsMap(VdsStatic.PmOptionsStringToMap(vds.getPmOptions()).asMap());

        // Set secondary PM parameters.
        getPmSecondaryIp().setEntity(vds.getPmSecondaryIp());
        getPmSecondaryUserName().setEntity(vds.getPmSecondaryUser());
        getPmSecondaryPassword().setEntity(vds.getPmSecondaryPassword());
        getPmSecondaryType().setSelectedItem(vds.getPmSecondaryType());
        setPmSecondaryOptionsMap(vds.getPmSecondaryOptionsMap().asMap());

        // Set other PM parameters.
        if (isEditWithPMemphasis) {
            setIsPowerManagementTabSelected(true);
            getIsPm().setEntity(true);
            getIsPm().setIsChangable(false);
        } else {
            getIsPm().setEntity(vds.getpm_enabled());
        }

        getPmSecondaryConcurrent().setEntity(vds.isPmSecondaryConcurrent());

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

        if (vds.getStatus() != VDSStatus.Maintenance && vds.getStatus() != VDSStatus.PendingApproval) {
            setAllowChangeHostPlacementPropertiesWhenNotInMaintenance();
        }
        else if (selectedSystemTreeItem != null)
        {
            switch (selectedSystemTreeItem.getType())
            {
            case Host:
                getName().setIsChangable(false);
                getName().setInfo("Cannot edit Host's Name in this tree context"); //$NON-NLS-1$
                break;
            case Hosts:
            case Cluster:
            case Cluster_Gluster:
                getCluster().setIsChangable(false);
                getCluster().setInfo("Cannot change Host's Cluster in tree context"); //$NON-NLS-1$
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

    protected abstract boolean showInstallationProperties();

    public abstract boolean showExternalProviderPanel();

    protected abstract void updateModelDataCenterFromVds(ArrayList<StoragePool> dataCenters, VDS vds);

    protected abstract void updateModelClusterFromVds(ArrayList<VDSGroup> clusters, VDS vds);

    protected abstract void setAllowChangeHost(VDS vds);

    protected abstract void setAllowChangeHostPlacementPropertiesWhenNotInMaintenance();

    protected abstract void updateHosts();

    protected abstract void setPort(VDS vds);

    public abstract boolean showNetworkProviderTab();
}
