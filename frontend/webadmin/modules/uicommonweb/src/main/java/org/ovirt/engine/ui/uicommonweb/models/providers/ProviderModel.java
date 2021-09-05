package org.ovirt.engine.ui.uicommonweb.models.providers;

import static org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions.GENERAL_MAX_SIZE;
import static org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT;
import static org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT;
import static org.ovirt.engine.core.common.businessentities.CertificateInfo.SHA256_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportProviderCertificateParameters;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.CertificateInfo;
import org.ovirt.engine.core.common.businessentities.OpenStackApiVersionType;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenStackProtocolType;
import org.ovirt.engine.core.common.businessentities.OpenStackProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.Uri;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.HostnameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotNullIntegerValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class ProviderModel extends Model {

    private static final String CMD_SAVE = "OnSave"; //$NON-NLS-1$
    private static final String CMD_TEST = "OnTest"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$
    private static final String CMD_IMPORT_CERTIFICATE = "ImportCertificate"; //$NON-NLS-1$
    private static final String CMD_CANCEL_IMPORT = "CancelImport"; //$NON-NLS-1$
    private static final String EMPTY_ERROR_MESSAGE = ""; //$NON-NLS-1$
    private static final String AUTH_PORT_DEFAULT = "35357"; //$NON-NLS-1$

    protected final SearchableListModel sourceListModel;
    private final ActionType action;
    protected final Provider provider;

    private EntityModel<String> name = new EntityModel<>();
    private EntityModel<String> description = new EntityModel<>();
    private EntityModel<String> url = new EntityModel<>();
    private EntityModel<Boolean> autoSync = new EntityModel<>();
    private EntityModel<Boolean> isUnmanaged = new EntityModel<>();
    private EntityModel<Boolean> requiresAuthentication = new EntityModel<>();
    private EntityModel<String> username = new EntityModel<>();
    private EntityModel<String> password = new EntityModel<>();
    private EntityModel<String> tenantName = new EntityModel<>();
    private ListModel<ProviderType> type;
    private UICommand testCommand;
    private EntityModel<String> testResult = new EntityModel<>();
    private ListModel<OpenStackApiVersionType> authApiVersion;
    private ListModel<OpenStackProtocolType> authProtocol;
    private EntityModel<String> authHostname = new EntityModel<>();
    private EntityModel<String> authPort = new EntityModel<>();
    private EntityModel<String> userDomainName = new EntityModel<>();
    private EntityModel<String> projectName = new EntityModel<>();
    private EntityModel<String> projectDomainName = new EntityModel<>();

    private ListModel<StoragePool> dataCenter;

    private NeutronAgentModel neutronAgentModel = new NeutronAgentModel();
    private VmwarePropertiesModel vmwarePropertiesModel = new VmwarePropertiesModel();
    private KVMPropertiesModel kvmPropertiesModel = new KVMPropertiesModel();
    private XENPropertiesModel xenPropertiesModel = new XENPropertiesModel();
    private KubevirtPropertiesModel kubevirtPropertiesModel = new KubevirtPropertiesModel();
    private String certificate;
    private EntityModel<Boolean> readOnly = new EntityModel<>();


    public EntityModel<String> getName() {
        return name;
    }

    public ListModel<ProviderType> getType() {
        return type;
    }

    private void setType(ListModel<ProviderType> value) {
        type = value;
    }

    public ListModel<String> getPluginType() {
        return getNeutronAgentModel().getPluginType();
    }

    public EntityModel<String> getDescription() {
        return description;
    }

    public EntityModel<String> getUrl() {
        return url;
    }

    public EntityModel<Boolean> getAutoSync() {
        return autoSync;
    }

    public EntityModel<Boolean> getIsUnmanaged() {
        return isUnmanaged;
    }

    public EntityModel<Boolean> getRequiresAuthentication() {
        return requiresAuthentication;
    }

    public EntityModel<Boolean> getReadOnly() {
        return readOnly;
    }

    public EntityModel<String> getUsername() {
        return username;
    }

    public EntityModel<String> getPassword() {
        return password;
    }

    public EntityModel<String> getTenantName() {
        return tenantName;
    }

    public ListModel<OpenStackApiVersionType> getAuthApiVersion() {
        return authApiVersion;
    }

    public void setAuthApiVersion(ListModel<OpenStackApiVersionType> authApiVersion) {
        this.authApiVersion = authApiVersion;
    }

    public ListModel<OpenStackProtocolType> getAuthProtocol() {
        return authProtocol;
    }

    public void setAuthProtocol(ListModel<OpenStackProtocolType> authProtocol) {
        this.authProtocol = authProtocol;
    }

    public EntityModel<String> getAuthHostname() {
        return authHostname;
    }

    public void setAuthHostname(EntityModel<String> authHostname) {
        this.authHostname = authHostname;
    }

    public EntityModel<String> getAuthPort() {
        return authPort;
    }

    public void setAuthPort(EntityModel<String> authPort) {
        this.authPort = authPort;
    }

    public EntityModel<String> getUserDomainName() {
        return userDomainName;
    }

    public void setUserDomainName(EntityModel<String> userDomainName) {
        this.userDomainName = userDomainName;
    }

    public EntityModel<String> getProjectName() {
        return projectName;
    }

    public void setProjectName(EntityModel<String> projectName) {
        this.projectName = projectName;
    }

    public EntityModel<String> getProjectDomainName() {
        return projectDomainName;
    }

    public void setProjectDomainName(EntityModel<String> projectDomainName) {
        this.projectDomainName = projectDomainName;
    }

    public UICommand getTestCommand() {
        return testCommand;
    }

    private void setTestCommand(UICommand value) {
        testCommand = value;
    }

    public EntityModel<String> getTestResult() {
        return testResult;
    }

    public NeutronAgentModel getNeutronAgentModel() {
        return neutronAgentModel;
    }

    public VmwarePropertiesModel getVmwarePropertiesModel() {
        return vmwarePropertiesModel;
    }

    public KVMPropertiesModel getKvmPropertiesModel() {
        return kvmPropertiesModel;
    }

    public XENPropertiesModel getXenPropertiesModel() {
        return xenPropertiesModel;
    }

    public KubevirtPropertiesModel getKubevirtPropertiesModel() {
        return kubevirtPropertiesModel;
    }

    protected boolean isTypeNetwork() {
        ProviderType type = getType().getSelectedItem();
        return type == ProviderType.EXTERNAL_NETWORK || type == ProviderType.OPENSTACK_NETWORK;
    }

    private boolean hasOpenStackProviderProperties() {
        return isTypeOpenStack() || isTypeExternalNetwork();
    }

    private boolean supportsAuthApiV3() {
        return isTypeOpenStack();
    }

    private boolean isTypeOpenStack() {
        return isTypeOpenStackImage() || isTypeOpenStackNetwork();
    }

    private boolean isTypeOpenStackNetwork() {
        return getType().getSelectedItem() == ProviderType.OPENSTACK_NETWORK;
    }

    private boolean isTypeOpenStackImage() {
        return getType().getSelectedItem() == ProviderType.OPENSTACK_IMAGE;
    }

    private boolean isTypeExternalNetwork() {
        return getType().getSelectedItem() == ProviderType.EXTERNAL_NETWORK;
    }

    protected boolean isTypeVmware() {
        return getType().getSelectedItem() == ProviderType.VMWARE;
    }

    protected boolean isTypeKVM() {
        return getType().getSelectedItem() == ProviderType.KVM;
    }

    protected boolean isTypeXEN() {
        return getType().getSelectedItem() == ProviderType.XEN;
    }

    protected boolean isTypeKubevirt() {
        return getType().getSelectedItem() == ProviderType.KUBEVIRT;
    }

    public ListModel<StoragePool> getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(ListModel<StoragePool> dataCenter) {
        this.dataCenter = dataCenter;
    }

    private String getAuthUrl() {
        String scheme = getAuthProtocol().getSelectedItem() == OpenStackProtocolType.https ?
                Uri.SCHEME_HTTPS : Uri.SCHEME_HTTP;
        String authority = getAuthHostname().getEntity() + ":" +  getAuthPort().getEntity(); //$NON-NLS-1$
        Uri uri = new Uri(scheme, authority, getAuthApiVersion().getSelectedItem().getPath());
        return uri.getStringRepresentation();
    }

    private boolean isTypeRequiresAuthentication() {
        return false;
    }

    private String getDefaultUrl(ProviderType type) {
        if (type == null) {
            return ""; //$NON-NLS-1$
        }
        switch (type) {
        case EXTERNAL_NETWORK:
        case OPENSTACK_NETWORK:
            return "http://localhost:9696"; //$NON-NLS-1$
        case OPENSTACK_IMAGE:
            return "http://localhost:9292"; //$NON-NLS-1$
        case VMWARE:
            return ""; //$NON-NLS-1$
        case KVM:
            return ""; //$NON-NLS-1$
        case XEN:
            return ""; //$NON-NLS-1$
        case KUBEVIRT:
            return ""; //$NON-NLS-1$
        case FOREMAN:
        default:
            return "http://localhost"; //$NON-NLS-1$
        }
    }

    public ProviderModel(SearchableListModel sourceListModel, ActionType action, final Provider provider) {
        this.sourceListModel = sourceListModel;
        this.action = action;
        this.provider = provider;

        setAuthApiVersion(new ListModel<OpenStackApiVersionType>(){
            @Override
            protected void onSelectedItemChanging(OpenStackApiVersionType newValue, OpenStackApiVersionType oldValue) {
                super.onSelectedItemChanging(newValue, oldValue);
                getTenantName().setIsAvailable(newValue == OpenStackApiVersionType.v2_0);
                getUserDomainName().setIsAvailable(newValue == OpenStackApiVersionType.v3);
                getProjectName().setIsAvailable(newValue == OpenStackApiVersionType.v3);
                getProjectDomainName().setIsAvailable(newValue == OpenStackApiVersionType.v3);
            }
        });
        getAuthApiVersion().setItems(Arrays.asList(OpenStackApiVersionType.values()));
        setAuthProtocol(new ListModel<>());
        getAuthProtocol().setItems(Arrays.asList(OpenStackProtocolType.values()));

        getRequiresAuthentication().setEntity(false);
        getRequiresAuthentication().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateAuthFieldsChangeableStatus();
        });
        setType(new ListModel<ProviderType>() {
            @Override
            protected void onSelectedItemChanging(ProviderType newValue, ProviderType oldValue) {
                super.onSelectedItemChanging(newValue, oldValue);
                String url = getUrl().getEntity();
                if (url == null) {
                    url = ""; //$NON-NLS-1$
                }
                url = url.trim();
                if (url.equals("") || url.equalsIgnoreCase(getDefaultUrl(oldValue))) { //$NON-NLS-1$
                    getUrl().setEntity(getDefaultUrl(newValue));
                }
            }
        });
        getAutoSync().setEntity(false);
        getIsUnmanaged().setEntity(false);
        getIsUnmanaged().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            boolean isUnmanaged = getIsUnmanaged().getEntity();
            getRequiresAuthentication().setIsChangeable(!isUnmanaged);
            boolean requiresAuthentication = getRequiresAuthentication().getEntity();
            getRequiresAuthentication().setEntity(requiresAuthentication && !isUnmanaged);
            updateAuthFieldsChangeableStatus();
            setReadOnlyEntity();
            getReadOnly().setIsChangeable(!isUnmanaged);
            getUrl().setIsChangeable(!isUnmanaged);
            getAutoSync().setEntity(false);
            getAutoSync().setIsChangeable(!isUnmanaged);
        });
        updateAuthFieldsChangeableStatus();

        getType().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            boolean isUnmanagedAware = getType().getSelectedItem().isUnmanagedAware();
            boolean isAuthUrlAware = getType().getSelectedItem().isAuthUrlAware();
            boolean isReadOnlyAware = getType().getSelectedItem().isReadOnlyAware();

            getIsUnmanaged().setIsAvailable(isUnmanagedAware);
            getAuthApiVersion().setIsAvailable(isAuthUrlAware);
            getAuthApiVersion().setSelectedItem(supportsAuthApiV3() ?
                    OpenStackApiVersionType.v3 : OpenStackApiVersionType.v2_0);
            getAuthProtocol().setIsAvailable(isAuthUrlAware);
            getAuthHostname().setIsAvailable(isAuthUrlAware);
            getAuthPort().setIsAvailable(isAuthUrlAware);
            if (isAuthUrlAware) {
                getAuthPort().setEntity(AUTH_PORT_DEFAULT);
            }
            if (hasOpenStackProviderProperties()) {
                OpenStackProviderProperties properties = (OpenStackProviderProperties) provider.getAdditionalProperties();
                getTenantName().setEntity(properties == null ? null : properties.getTenantName());
                getUserDomainName().setEntity(properties == null ? null : properties.getUserDomainName());
                getProjectName().setEntity(properties == null ? null : properties.getProjectName());
                getProjectDomainName().setEntity(properties == null ? null : properties.getProjectDomainName());
            } else {
                getTenantName().setIsAvailable(false);
                getUserDomainName().setIsAvailable(false);
                getProjectName().setIsAvailable(false);
                getProjectDomainName().setIsAvailable(false);
            }

            boolean isNetworkProvider = isTypeNetwork();
            if (isNetworkProvider) {
                getNeutronAgentModel().init(provider, getType().getSelectedItem());
                getPluginType().setIsChangeable(true);
                getAutoSync().setIsChangeable(true);
            } else {
                getPluginType().setIsChangeable(false);
                getPluginType().setSelectedItem(null);
                getAutoSync().setIsChangeable(false);
            }
            getNeutronAgentModel().setIsAvailable(isNetworkProvider);

            getReadOnly().setIsAvailable(isReadOnlyAware);
            setReadOnlyEntity();

            boolean isVmware = isTypeVmware();
            boolean isKvm = isTypeKVM();
            boolean isXen = isTypeXEN();
            boolean isKubevirt = isTypeKubevirt();
            boolean requiresAuth = isTypeRequiresAuthentication();
            getRequiresAuthentication().setEntity(isVmware || isKubevirt || Boolean.valueOf(requiresAuth));
            getRequiresAuthentication().setIsChangeable(!requiresAuth);

            getDataCenter().setIsAvailable(isVmware || isKvm || isXen);

            getVmwarePropertiesModel().setIsAvailable(isVmware);
            getKvmPropertiesModel().setIsAvailable(isKvm);
            getXenPropertiesModel().setIsAvailable(isXen);
            getKubevirtPropertiesModel().setIsAvailable(isKubevirt);

            getRequiresAuthentication().setIsAvailable(!isVmware && !isXen && !isKubevirt);
            getUsername().setIsAvailable(!isXen && !isKubevirt);
            getPassword().setIsAvailable(!isXen);
            getUrl().setIsAvailable(!isVmware && !isKvm && !isXen);
            if (isVmware || isKvm || isXen) {
                updateDatacentersForExternalProvider();
            }
        });

        getNeutronAgentModel().setIsAvailable(false);
        getVmwarePropertiesModel().setIsAvailable(false);
        getTenantName().setIsAvailable(false);

        List<ProviderType> providerTypes = new ArrayList<>(Arrays.asList(ProviderType.values()));
        // Filtering out the old cinder integration from the providers type list on UI.
        // The OPENSTACK_VOLUME provider type should be removed from the ProviderType enum (bug 1950731).
        providerTypes.remove(ProviderType.OPENSTACK_VOLUME);

        Collections.sort(providerTypes,
                Comparator.comparing(t -> EnumTranslator.getInstance().translate(t), new LexoNumericComparator()));
        getType().setItems(providerTypes);

        getCommands().add(UICommand.createDefaultOkUiCommand(CMD_SAVE, this));
        getCommands().add(UICommand.createCancelUiCommand(CMD_CANCEL, this));
        setTestCommand(new UICommand(CMD_TEST, this));

        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().setIsAvailable(false);
        getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (!isTypeVmware() && !isTypeKVM() && !isTypeXEN()) {
                    return;
                }

                final ProxyHostPropertiesModel proxyHostPropertiesModel = getProxyHostPropertiesModel();

                if (getDataCenter().getSelectedItem() == null) {
                    proxyHostPropertiesModel.disableProxyHost();
                } else {
                    proxyHostPropertiesModel.getProxyHost().setIsChangeable(true);
                    AsyncDataProvider.getInstance().getHostListByDataCenter(new AsyncQuery<>(hosts -> {
                        VDS prevHost = getPreviousHost(hosts);
                        hosts.add(0, null); // Any host in the cluster
                        proxyHostPropertiesModel.getProxyHost().setItems(hosts);
                        proxyHostPropertiesModel.getProxyHost().setSelectedItem(prevHost);
                    }),
                            getDataCenter().getSelectedItem().getId());
                }
            }

            private VDS getPreviousHost(List<VDS> hosts) {
                Guid previousProxyHostId = getProxyHostPropertiesModel().getLastProxyHostId();

                for (VDS host : hosts) {
                    if (host.getId().equals(previousProxyHostId)) {
                        return host;
                    }
                }
                return null;
            }
        });
    }

    private void setReadOnlyEntity() {
        boolean isReadOnlyAware = getType().getSelectedItem().isReadOnlyAware();
        if (isReadOnlyAware && getIsUnmanaged().getEntity()) {
            getReadOnly().setEntity(false);
        } else if (isReadOnlyAware) {
            OpenstackNetworkProviderProperties properties =
                    (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
            getReadOnly().setEntity(properties != null ? properties.getReadOnly() : true);
        }
    }

    private void updateAuthFieldsChangeableStatus() {
        boolean requiresAuthentication = getRequiresAuthentication().getEntity();
        boolean isUnmanaged = getIsUnmanaged().getEntity();
        boolean status = requiresAuthentication && !isUnmanaged;
        getUsername().setIsChangeable(status);
        getPassword().setIsChangeable(status);
        getTenantName().setIsChangeable(status);
        getAuthApiVersion().setIsChangeable(status && supportsAuthApiV3());
        getAuthProtocol().setIsChangeable(status);
        getAuthHostname().setIsChangeable(status);
        getAuthPort().setIsChangeable(status);
        getUserDomainName().setIsChangeable(status);
        getProjectName().setIsChangeable(status);
        getProjectDomainName().setIsChangeable(status);
    }

    public ProxyHostPropertiesModel getProxyHostPropertiesModel() {
        if (isTypeXEN()) {
            return getXenPropertiesModel();
        } else if (isTypeKVM()) {
            return getKvmPropertiesModel();
        } else if (isTypeVmware()) {
            return getVmwarePropertiesModel();
        } else {
            // null object, to avoid null checks everywhere
            return new ProxyHostPropertiesModel() {};
        }
    }

    protected void updateDatacentersForExternalProvider() {
        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(new AsyncCallback<List<StoragePool>>() {
            @Override
            public void onSuccess(List<StoragePool> dataCenters) {
                StoragePool prevDataCenter = getPreviousDataCenter(dataCenters);
                Collections.sort(dataCenters, new NameableComparator());
                dataCenters.add(0, null); //any data center
                getDataCenter().setItems(dataCenters);
                getDataCenter().setSelectedItem(prevDataCenter);
                if (getDataCenter().getSelectedItem() == null) {
                    getProxyHostPropertiesModel().disableProxyHost();
                }
            }

            private StoragePool getPreviousDataCenter(List<StoragePool> dataCenters) {
                Guid previousDataCenterId = getProxyHostPropertiesModel().getLastStoragePoolId();

                for (StoragePool dataCenter : dataCenters) {
                    if (dataCenter.getId().equals(previousDataCenterId)) {
                        return dataCenter;
                    }
                }
                return null;
            }
        }));
    }

    private boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });
        getType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getNeutronAgentModel().validate();
        getVmwarePropertiesModel().validate();
        getKvmPropertiesModel().validate();
        getXenPropertiesModel().validate();
        boolean connectionSettingsValid = validateConnectionSettings();

        return connectionSettingsValid &&
                getName().getIsValid() &&
                getType().getIsValid() &&
                getNeutronAgentModel().getIsValid() &&
                getKvmPropertiesModel().getIsValid() &&
                getXenPropertiesModel().getIsValid() &&
                getVmwarePropertiesModel().getIsValid();
    }

    private boolean validateConnectionSettings() {
        getUsername().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getPassword().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(GENERAL_MAX_SIZE)
        });
        if (isTypeOpenStack()) {
            getTenantName().validateEntity(new IValidation[] { new NotEmptyValidation()} );
            getUserDomainName().validateEntity(new IValidation[] { new NotEmptyValidation()} );
        }

        getAuthHostname().validateEntity(new IValidation[] { new HostnameValidation()} );
        getAuthPort().validateEntity(new IValidation[] {
                new NotNullIntegerValidation(NETWORK_MIN_LEGAL_PORT, NETWORK_MAX_LEGAL_PORT)
        } );

        return (getUrl().getEntity() == null || getUrl().getIsValid() || getUrl().getEntity().isEmpty()) &&
                getUsername().getIsValid() &&
                getPassword().getIsValid() &&
                getTenantName().getIsValid() &&
                getUserDomainName().getIsValid() &&
                getAuthHostname().getIsValid() &&
                getAuthPort().getIsValid();
    }

    private void cancel() {
        sourceListModel.setWindow(null);
    }

    private void flush() {
        provider.setName(name.getEntity());
        provider.setType(type.getSelectedItem());
        provider.setDescription(description.getEntity());
        provider.setUrl(url.getEntity());
        provider.setIsUnmanaged(isUnmanaged.getEntity());

        if (isTypeNetwork()) {
            getNeutronAgentModel().flush(provider);
            OpenstackNetworkProviderProperties properties = (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
            properties.setReadOnly(readOnly.getEntity());
            properties.setAutoSync(autoSync.getEntity());
        } else if (isTypeOpenStackImage()) {
            provider.setAdditionalProperties(new OpenStackImageProviderProperties());
        } else if (isTypeVmware()) {
            provider.setAdditionalProperties(getVmwarePropertiesModel().getVmwareVmProviderProperties(
                    dataCenter.getSelectedItem() != null ? dataCenter.getSelectedItem().getId() : null));
            provider.setUrl(getVmwarePropertiesModel().getUrl());
        } else if (isTypeKVM()) {
            provider.setUrl(getKvmPropertiesModel().getUrl().getEntity());
            provider.setAdditionalProperties(getKvmPropertiesModel().getKVMVmProviderProperties(
                    dataCenter.getSelectedItem() != null ? dataCenter.getSelectedItem().getId() : null));
        } else if (isTypeXEN()) {
            provider.setUrl(getXenPropertiesModel().getUrl().getEntity());
            provider.setAdditionalProperties(getXenPropertiesModel().getXENVmProviderProperties(
                    dataCenter.getSelectedItem() != null ? dataCenter.getSelectedItem().getId() : null));
        } else if (isTypeKubevirt()) {
            provider.setAdditionalProperties(getKubevirtPropertiesModel().getKubevirtProviderProperties());
        }

        boolean authenticationRequired = requiresAuthentication.getEntity();
        provider.setRequiringAuthentication(authenticationRequired);
        if (authenticationRequired) {
            provider.setUsername(getUsername().getEntity());
            provider.setPassword(getPassword().getEntity());
            if (getTenantName().getIsAvailable()) {
                OpenStackProviderProperties properties = getOpenStackProviderProperties();
                properties.setTenantName(getTenantName().getEntity());
                properties.setUserDomainName(null);
                properties.setProjectName(null);
                properties.setProjectDomainName(null);
            } else if (getUserDomainName().getIsAvailable()) {
                OpenStackProviderProperties properties = getOpenStackProviderProperties();
                properties.setTenantName(null);
                properties.setUserDomainName(getUserDomainName().getEntity());
                properties.setProjectName(getProjectName().getEntity());
                properties.setProjectDomainName(getProjectDomainName().getEntity());
            }
            provider.setAuthUrl(getAuthUrl());
        } else {
            provider.setUsername(null);
            provider.setPassword(null);
            if (hasOpenStackProviderProperties()) {
                OpenStackProviderProperties properties = getOpenStackProviderProperties();
                properties.setTenantName(null);
                properties.setUserDomainName(null);
                properties.setProjectName(null);
                properties.setProjectDomainName(null);
            }
            provider.setAuthUrl(null);
        }

        if (isTypeKubevirt()) {
            provider.setAuthUrl(null);
        }
    }

    private OpenStackProviderProperties getOpenStackProviderProperties() {
        OpenStackProviderProperties properties = (OpenStackProviderProperties) provider.getAdditionalProperties();
        if (properties == null) {
            properties = new OpenStackProviderProperties();
            provider.setAdditionalProperties(properties);
        }
        return properties;
    }

    protected void preSave() {
        actualSave();
    }

    protected void actualSave() {
        flush();
        Frontend.getInstance().runAction(action, new ProviderParameters(provider), result -> {
            if (result.getReturnValue() == null || !result.getReturnValue().getSucceeded()) {
                return;
            }
            sourceListModel.getSearchCommand().execute();
            cancel();
        }, this);
    }

    private void onSave() {
        if (!validate()) {
            return;
        }
        preSave();
    }

    private void onTest() {
        if (isUnmanaged.getEntity()) {
            return;
        }

        if (!validateConnectionSettings()) {
            getTestResult().setEntity(ConstantsManager.getInstance().getConstants().testFailedInsufficientParams());
            return;
        }

        flush();
        startProgress();
        if (provider.getUrl().startsWith(Uri.SCHEME_HTTPS) && !isTypeKubevirt()) {
            AsyncDataProvider.getInstance().getProviderCertificateChain(new AsyncQuery<>(certs -> {
                boolean ok = false;
                certificate = null;
                if (certs != null) {
                    if (!certs.isEmpty()) {
                        certificate = certs.get(certs.size() - 1).getPayload();
                        ConfirmationModel confirmationModel =
                                getImportCertificateConfirmationModel(certs.get(certs.size() - 1));
                        sourceListModel.setConfirmWindow(confirmationModel);
                        ok = true;
                    }
                }
                if (!ok) {
                    stopProgress();
                    getTestResult().setEntity(ConstantsManager.getInstance()
                            .getConstants()
                            .testFailedUnknownErrorMsg());
                }
            }),
                    provider);
        } else {
            testProviderConnectivity();
        }
    }

    private void testProviderConnectivity() {
        Frontend.getInstance().runAction(ActionType.TestProviderConnectivity,
                new ProviderParameters(provider),
                result -> {
                    ActionReturnValue res = result.getReturnValue();
                    // If the connection failed on SSL issues, we try to fetch the provider
                    // certificate chain, and import it to the engine
                    stopProgress();
                    setTestResultValue(res);
                }, null, false);
    }

    private ImportProviderCertificateParameters importCertificateParams() {
        return new ImportProviderCertificateParameters(provider, certificate);
    }

    private ConfirmationModel getImportCertificateConfirmationModel(CertificateInfo certInfo) {
        ConfirmationModel confirmationModel = new ConfirmationModel();
        if (certInfo.getSelfSigned()) {
            confirmationModel.setMessage(
                    ConstantsManager.getInstance().getMessages().approveRootCertificateTrust(
                            certInfo.getSubject(), certInfo.getSHA256Fingerprint(), SHA256_NAME));
        } else {
            confirmationModel.setMessage(
                    ConstantsManager.getInstance().getMessages().approveCertificateTrust(
                        certInfo.getSubject(), certInfo.getIssuer(), certInfo.getSHA256Fingerprint(), SHA256_NAME));
        }
        confirmationModel.setTitle(ConstantsManager.getInstance().getConstants().importProviderCertificateTitle());
        confirmationModel.setHelpTag(HelpTag.import_provider_certificate);
        confirmationModel.setHashName("import_provider_certificate"); //$NON-NLS-1$
        UICommand importCertificateCommand = new UICommand(CMD_IMPORT_CERTIFICATE, this);
        importCertificateCommand.setTitle(ConstantsManager.getInstance().getConstants().yes());
        importCertificateCommand.setIsDefault(false);
        confirmationModel.getCommands().add(importCertificateCommand);
        UICommand cancelImport = new UICommand(CMD_CANCEL_IMPORT, this);
        cancelImport.setTitle(ConstantsManager.getInstance().getConstants().no());
        cancelImport.setIsCancel(true);
        cancelImport.setIsDefault(true);
        confirmationModel.getCommands().add(cancelImport);
        return confirmationModel;
    }

    private void importCertificate() {
        Frontend.getInstance().runAction(ActionType.ImportProviderCertificate,
                importCertificateParams(),
                result -> testProviderConnectivity(), null, false);
        sourceListModel.setConfirmWindow(null);
    }

    private void cancelImport() {
        stopProgress();
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (CMD_SAVE.equals(command.getName())) {
            onSave();
        } else if (CMD_TEST.equals(command.getName())) {
            onTest();
        } else if (CMD_CANCEL.equals(command.getName())) {
            cancel();
        } else if (CMD_IMPORT_CERTIFICATE.equals(command.getName())) {
            importCertificate();
        } else if (CMD_CANCEL_IMPORT.equals(command.getName())) {
            cancelImport();
        }
    }

    private void setTestResultValue(ActionReturnValue result) {
        String errorMessage = EMPTY_ERROR_MESSAGE;
        if (result == null) {
            errorMessage = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
        } else if (!result.getSucceeded()) {
            errorMessage = result.isValid() ?
                    result.getFault().getMessage() :
                        result.getValidationMessages().get(0);
        }
        getTestResult().setEntity(errorMessage);
    }

    public boolean isEditProviderMode () {
        return action == ActionType.UpdateProvider;
    }
}
