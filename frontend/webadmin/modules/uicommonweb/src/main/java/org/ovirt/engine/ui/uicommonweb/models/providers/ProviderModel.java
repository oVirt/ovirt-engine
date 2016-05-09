package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ImportProviderCertificateParameters;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CertificateInfo;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.TenantProviderProperties;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
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
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.UrlValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class ProviderModel extends Model {

    private static final String CMD_SAVE = "OnSave"; //$NON-NLS-1$
    private static final String CMD_TEST = "OnTest"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$
    private static final String CMD_IMPORT_CERTIFICATE = "ImportCertificate"; //$NON-NLS-1$
    private static final String CMD_CANCEL_IMPORT = "CancelImport"; //$NON-NLS-1$
    private static final String EMPTY_ERROR_MESSAGE = ""; //$NON-NLS-1$

    protected final SearchableListModel sourceListModel;
    private final VdcActionType action;
    protected final Provider provider;

    private EntityModel<String> name = new EntityModel<>();
    private EntityModel<String> description = new EntityModel<>();
    private EntityModel<String> url = new EntityModel<>();
    private EntityModel<Boolean> requiresAuthentication = new EntityModel<>();
    private EntityModel<String> username = new EntityModel<>();
    private EntityModel<String> password = new EntityModel<>();
    private EntityModel<String> tenantName = new EntityModel<>();
    private ListModel<ProviderType> type;
    private UICommand testCommand;
    private EntityModel<String> testResult = new EntityModel<>();
    private EntityModel<String> authUrl = new EntityModel<>();
    private ListModel<StoragePool> dataCenter;

    private NeutronAgentModel neutronAgentModel = new NeutronAgentModel();
    private VmwarePropertiesModel vmwarePropertiesModel = new VmwarePropertiesModel();
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

    protected boolean isExternalNetwork() {
        return getType().getSelectedItem() == ProviderType.EXTERNAL_NETWORK;
    }

    protected boolean isTypeOpenStackNetwork() {
        return getType().getSelectedItem() == ProviderType.OPENSTACK_NETWORK;
    }

    private boolean isTypeOpenStackImage() {
        return getType().getSelectedItem() == ProviderType.OPENSTACK_IMAGE;
    }

    private boolean isTypeOpenStackVolume() {
        return getType().getSelectedItem() == ProviderType.OPENSTACK_VOLUME;
    }

    protected boolean isTypeVmware() {
        return getType().getSelectedItem() == ProviderType.VMWARE;
    }

    public ListModel<StoragePool> getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(ListModel<StoragePool> dataCenter) {
        this.dataCenter = dataCenter;
    }

    public EntityModel<String> getAuthUrl() {
        return authUrl;
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
            case OPENSTACK_VOLUME:
                return "http://localhost:8776"; //$NON-NLS-1$
            case VMWARE:
                return ""; //$NON-NLS-1$
            case FOREMAN:
            default:
                return "http://localhost"; //$NON-NLS-1$
        }
    }

    public ProviderModel(SearchableListModel sourceListModel, VdcActionType action, final Provider provider) {
        this.sourceListModel = sourceListModel;
        this.action = action;
        this.provider = provider;

        getRequiresAuthentication().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                boolean authenticationRequired = requiresAuthentication.getEntity();
                getUsername().setIsChangeable(authenticationRequired);
                getPassword().setIsChangeable(authenticationRequired);
                getTenantName().setIsChangeable(authenticationRequired);
                getAuthUrl().setIsChangeable(authenticationRequired);
            }
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
        getType().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                boolean isTenantAware = getType().getSelectedItem().isTenantAware();
                boolean isAuthUrlAware = getType().getSelectedItem().isAuthUrlAware();
                boolean isReadOnlyAware = getType().getSelectedItem().isReadOnlyAware();

                getTenantName().setIsAvailable(isTenantAware);
                getAuthUrl().setIsAvailable(isAuthUrlAware);
                if (isTenantAware) {
                    TenantProviderProperties properties = (TenantProviderProperties) provider.getAdditionalProperties();
                    getTenantName().setEntity(properties == null ? null : properties.getTenantName());
                }

                boolean isNeutron = isTypeOpenStackNetwork();
                getNeutronAgentModel().setIsAvailable(isNeutron);

                getReadOnly().setIsAvailable(isReadOnlyAware);
                if (isReadOnlyAware){
                    OpenstackNetworkProviderProperties properties = (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
                        getReadOnly().setEntity(properties != null ? properties.getReadOnly() : true);
                }

                boolean isVmware = isTypeVmware();
                boolean requiresAuth = isTypeRequiresAuthentication();
                getRequiresAuthentication().setEntity(isVmware || Boolean.valueOf(requiresAuth));
                getRequiresAuthentication().setIsChangeable(!requiresAuth);

                boolean isCinder = isTypeOpenStackVolume();
                getDataCenter().setIsAvailable(isCinder || isVmware);
                if (isCinder) {
                    updateDatacentersForVolumeProvider();
                }

                getVmwarePropertiesModel().setIsAvailable(isVmware);
                getRequiresAuthentication().setIsAvailable(!isVmware);
                getUrl().setIsAvailable(!isVmware);
                if (isVmware) {
                    updateDatacentersForVmwareProvider();
                }
            }
        });

        getNeutronAgentModel().setIsAvailable(false);
        getVmwarePropertiesModel().setIsAvailable(false);
        getTenantName().setIsAvailable(false);

        List<ProviderType> providerTypes = new ArrayList<>(Arrays.asList(ProviderType.values()));
        Collections.sort(providerTypes, new Linq.ProviderTypeComparator());
        getType().setItems(providerTypes);

        getCommands().add(UICommand.createDefaultOkUiCommand(CMD_SAVE, this));
        getCommands().add(UICommand.createCancelUiCommand(CMD_CANCEL, this));
        setTestCommand(new UICommand(CMD_TEST, this));

        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().setIsAvailable(false);
        getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (getType().getSelectedItem() != ProviderType.VMWARE) {
                    return;
                }

                if (getDataCenter().getSelectedItem() == null) {
                    getVmwarePropertiesModel().disableProxyHost();
                } else {
                    getVmwarePropertiesModel().getProxyHost().setIsChangeable(true);
                    AsyncDataProvider.getInstance().getHostListByDataCenter(new AsyncQuery(this, new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object model, Object returnValue) {
                            List<VDS> hosts = (List<VDS>) returnValue;
                            VDS prevHost = getPreviousHost(hosts);
                            hosts.add(0, null); // Any host in the cluster
                            getVmwarePropertiesModel().getProxyHost().setItems(hosts);
                            getVmwarePropertiesModel().getProxyHost().setSelectedItem(prevHost);
                        }
                    }),
                    getDataCenter().getSelectedItem().getId());
                }
            }

            private VDS getPreviousHost(List<VDS> hosts) {
                Guid previousProxyHostId = getVmwarePropertiesModel().getLastProxyHostId();
                for (VDS host : hosts) {
                    if (host.getId().equals(previousProxyHostId)) {
                        return host;
                    }
                }
                return null;
            }
        });
    }

    protected void updateDatacentersForVmwareProvider() {
        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                final List<StoragePool> dataCenters = (List<StoragePool>) returnValue;
                StoragePool prevDataCenter = getPreviousDataCenter(dataCenters);
                Collections.sort(dataCenters, new NameableComparator());
                dataCenters.add(0, null); //any data center
                getDataCenter().setItems(dataCenters);
                getDataCenter().setSelectedItem(prevDataCenter);
                if (getDataCenter().getSelectedItem() == null) {
                    getVmwarePropertiesModel().disableProxyHost();
                }
            }

            private StoragePool getPreviousDataCenter(List<StoragePool> dataCenters) {
                Guid previousDataCenterId = getVmwarePropertiesModel().getLastStoragePoolId();
                for (StoragePool dataCenter : dataCenters) {
                    if (dataCenter.getId().equals(previousDataCenterId)) {
                        return dataCenter;
                    }
                }
                return null;
            }
        }));
    }

    protected void updateDatacentersForVolumeProvider() {
        // implemented on sub-classes
    }

    private boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });
        getType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getNeutronAgentModel().validate();
        getVmwarePropertiesModel().validate();
        boolean connectionSettingsValid = validateConnectionSettings();

        return connectionSettingsValid &&
                getName().getIsValid() &&
                getType().getIsValid() &&
                getNeutronAgentModel().getIsValid() &&
                getVmwarePropertiesModel().getIsValid();
    }

    private boolean validateConnectionSettings() {
        getUsername().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });
        if (getType().getSelectedItem().isTenantRequired()) {
            getTenantName().validateEntity(new IValidation[] { new NotEmptyValidation()} );
        }
        getAuthUrl().validateEntity(new IValidation[] { new NotEmptyValidation(),
                new UrlValidation(Uri.SCHEME_HTTP, Uri.SCHEME_HTTPS) });
        getUrl().validateEntity(new IValidation[] { new NotEmptyValidation(),
                new UrlValidation(Uri.SCHEME_HTTP, Uri.SCHEME_HTTPS) });

        return getUrl().getIsValid() &&
                getUsername().getIsValid() &&
                getPassword().getIsValid() &&
                getTenantName().getIsValid() &&
                getAuthUrl().getIsValid();
    }

    private void cancel() {
        sourceListModel.setWindow(null);
    }

    private void flush() {
        provider.setName(name.getEntity());
        provider.setType(type.getSelectedItem());
        provider.setDescription(description.getEntity());
        provider.setUrl(url.getEntity());

        if (isTypeOpenStackNetwork() || isExternalNetwork()) {
            getNeutronAgentModel().flush(provider);
            OpenstackNetworkProviderProperties properties = (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
            properties.setReadOnly(readOnly.getEntity());
        } else if (isTypeOpenStackImage()) {
            provider.setAdditionalProperties(new OpenStackImageProviderProperties());
        } else if (isTypeOpenStackVolume()) {
            provider.setAdditionalProperties(new OpenStackVolumeProviderProperties(getDataCenter().getSelectedItem().getId()));
        } else if (isTypeVmware()) {
            provider.setAdditionalProperties(getVmwarePropertiesModel().getVmwareVmProviderProperties(
                    dataCenter.getSelectedItem() != null ? dataCenter.getSelectedItem().getId() : null));
            provider.setUrl(getVmwarePropertiesModel().getUrl());
        }

        boolean authenticationRequired = requiresAuthentication.getEntity();
        provider.setRequiringAuthentication(authenticationRequired);
        if (authenticationRequired) {
            provider.setUsername(getUsername().getEntity());
            provider.setPassword(getPassword().getEntity());
            if (getTenantName().getIsAvailable()) {
                TenantProviderProperties properties = (TenantProviderProperties) provider.getAdditionalProperties();
                if (properties == null) {
                    properties = new TenantProviderProperties();
                    provider.setAdditionalProperties(properties);
                }
                properties.setTenantName(getTenantName().getEntity());
            }
            provider.setAuthUrl(getAuthUrl().getEntity());
        } else {
            provider.setUsername(null);
            provider.setPassword(null);
            if (getTenantName().getIsAvailable()) {
                TenantProviderProperties properties = (TenantProviderProperties) provider.getAdditionalProperties();
                if (properties != null) {
                    properties.setTenantName(null);
                }
            }
            provider.setAuthUrl(null);
        }
    }

    protected void preSave() {
        actualSave();
    }

    protected void actualSave() {
        flush();
        Frontend.getInstance().runAction(action, new ProviderParameters(provider), new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                if (result.getReturnValue() == null || !result.getReturnValue().getSucceeded()) {
                    return;
                }
                sourceListModel.getSearchCommand().execute();
                cancel();
            }
        }, this);
    }

    private void onSave() {
        if (!validate()) {
            return;
        }
        preSave();
    }

    private void onTest() {
        if (!validateConnectionSettings()) {
            getTestResult().setEntity(ConstantsManager.getInstance().getConstants().testFailedInsufficientParams());
            return;
        }

        flush();
        startProgress();
        if (provider.getUrl().startsWith(Uri.SCHEME_HTTPS)) {
            AsyncDataProvider.getInstance().getProviderCertificateChain(new AsyncQuery(this, new INewAsyncCallback() {

                @Override
                public void onSuccess(Object model, Object returnValue) {
                    boolean ok = false;
                    certificate = null;
                    if (returnValue != null) {
                        List<CertificateInfo> certs = (List<CertificateInfo>) returnValue;
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
                }

            }),
                    provider);
        } else {
            testProviderConnectivity();
        }
    }

    private void testProviderConnectivity() {
        Frontend.getInstance().runAction(VdcActionType.TestProviderConnectivity,
                new ProviderParameters(provider),
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        VdcReturnValueBase res = result.getReturnValue();
                        // If the connection failed on SSL issues, we try to fetch the provider
                        // certificate chain, and import it to the engine
                        stopProgress();
                        setTestResultValue(res);
                    }
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
                        certInfo.getSubject(), certInfo.getSHA1Fingerprint()));
        } else {
            confirmationModel.setMessage(
                    ConstantsManager.getInstance().getMessages().approveCertificateTrust(
                        certInfo.getSubject(), certInfo.getIssuer(), certInfo.getSHA1Fingerprint()));
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
        Frontend.getInstance().runAction(VdcActionType.ImportProviderCertificate,
                importCertificateParams(),
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        testProviderConnectivity();
                    }
                }, null, false);
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

    private void setTestResultValue(VdcReturnValueBase result) {
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

}
