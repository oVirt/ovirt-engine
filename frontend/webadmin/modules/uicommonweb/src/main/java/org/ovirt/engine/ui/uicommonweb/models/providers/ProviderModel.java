package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.Arrays;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.TenantProviderProperties;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.Uri;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.UrlValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("deprecation")
public class ProviderModel extends Model {

    private static final String CMD_SAVE = "OnSave"; //$NON-NLS-1$
    private static final String CMD_TEST = "OnTest"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$
    private static final String CMD_IMPORT_CHAIN = "ImportChain"; //$NON-NLS-1$
    private static final String CMD_CANCEL_IMPORT = "CancelImport"; //$NON-NLS-1$
    private static final String EMPTY_ERROR_MESSAGE = ""; //$NON-NLS-1$

    protected final SearchableListModel sourceListModel;
    private final VdcActionType action;
    protected final Provider provider;

    private EntityModel name = new EntityModel();
    private EntityModel description = new EntityModel();
    private EntityModel url = new EntityModel();
    private ListModel apiVersion = new ListModel();
    private EntityModel requiresAuthentication = new EntityModel();
    private EntityModel username = new EntityModel();
    private EntityModel password = new EntityModel();
    private EntityModel tenantName = new EntityModel();
    private ListModel type;
    private ListModel pluginType = new ListModel();
    private UICommand testCommand;
    private EntityModel testResult = new EntityModel();

    private NeutronAgentModel neutronAgentModel;

    public EntityModel getName() {
        return name;
    }

    public ListModel getType() {
        return type;
    }

    private void setType(ListModel value) {
        type = value;
    }

    public EntityModel getDescription() {
        return description;
    }

    public EntityModel getUrl() {
        return url;
    }

    public ListModel getApiVersion() {
        return apiVersion;
    }

    public EntityModel getRequiresAuthentication() {
        return requiresAuthentication;
    }

    public EntityModel getUsername() {
        return username;
    }

    public EntityModel getPassword() {
        return password;
    }

    public EntityModel getTenantName() {
        return tenantName;
    }

    public ListModel getPluginType() {
        return pluginType;
    }

    public UICommand getTestCommand() {
        return testCommand;
    }

    private void setTestCommand(UICommand value) {
        testCommand = value;
    }

    public EntityModel getTestResult() {
        return testResult;
    }

    public NeutronAgentModel getNeutronAgentModel() {
        return neutronAgentModel;
    }

    protected boolean isTypeOpenStackNetwork() {
        return (ProviderType) getType().getSelectedItem() == ProviderType.OPENSTACK_NETWORK;
    }

    private boolean isTypeOpenStackImage() {
        return (ProviderType) getType().getSelectedItem() == ProviderType.OPENSTACK_IMAGE;
    }

    private boolean isTypeTenantAware() {
        ProviderType type = (ProviderType) getType().getSelectedItem();
        return type == ProviderType.OPENSTACK_NETWORK || type == ProviderType.OPENSTACK_IMAGE;
    }

    private String getDefaultUrl(ProviderType type) {
        if (type == null) {
            return new String();
        }
        switch (type) {
            case OPENSTACK_NETWORK:
                return "http://localhost:9696"; //$NON-NLS-1$
            case OPENSTACK_IMAGE:
                return "http://localhost:9292"; //$NON-NLS-1$
            case FOREMAN:
            default:
                return "http://localhost"; //$NON-NLS-1$
        }
    }

    public ProviderModel(SearchableListModel sourceListModel, VdcActionType action, final Provider provider) {
        this.sourceListModel = sourceListModel;
        this.action = action;
        this.provider = provider;

        getRequiresAuthentication().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean authenticationRequired = (Boolean) requiresAuthentication.getEntity();
                getUsername().setIsChangable(authenticationRequired);
                getPassword().setIsChangable(authenticationRequired);
                getTenantName().setIsChangable(authenticationRequired);
            }
        });
        setType(new ListModel() {
            @Override
            protected void onSelectedItemChanging(Object newValue, Object oldValue) {
                super.onSelectedItemChanging(newValue, oldValue);
                String url = (String) getUrl().getEntity();
                if (url == null) {
                    url = new String();
                }
                url = url.trim();
                if (url.equals("") || url.equalsIgnoreCase(getDefaultUrl((ProviderType) oldValue))) { //$NON-NLS-1$
                    getUrl().setEntity(getDefaultUrl((ProviderType) newValue));
                }
            }
        });
        getType().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getTenantName().setIsAvailable(isTypeTenantAware());

                boolean providerNeutron = isTypeOpenStackNetwork();
                getApiVersion().setIsAvailable(providerNeutron);
                if (providerNeutron) {
                    OpenstackNetworkProviderProperties properties =
                            (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
                    String pluginName = (properties == null) ? new String() : properties.getPluginType();
                    getPluginType().setSelectedItem(NeutronPluginTranslator.getDisplayStringForPluginName(pluginName));
                }
            }
        });

        neutronAgentModel = new NeutronAgentModel(getType(), getPluginType());

        getPluginType().setIsAvailable(false);
        getApiVersion().setIsAvailable(false);
        getTenantName().setIsAvailable(false);

        getType().setItems(Arrays.asList(ProviderType.values()));
        getPluginType().setItems(NeutronPluginTranslator.getPresetDisplayStrings());
        getApiVersion().setItems(Arrays.asList("v2.0")); //$NON-NLS-1$

        UICommand tempVar = new UICommand(CMD_SAVE, this);
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand(CMD_CANCEL, this);
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getCommands().add(tempVar2);
        setTestCommand(new UICommand(CMD_TEST, this));
    }

    private boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getPluginType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getUsername().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getTenantName().validateEntity(new IValidation[] { new NotEmptyValidation()} );
        getUrl().validateEntity(new IValidation[] { new NotEmptyValidation(),
                new UrlValidation(Uri.SCHEME_HTTP, Uri.SCHEME_HTTPS) });
        getNeutronAgentModel().validate();

        return getName().getIsValid() && getType().getIsValid() && getPluginType().getIsValid()
                && getUrl().getIsValid() && getUsername().getIsValid() && getPassword().getIsValid()
                && getTenantName().getIsValid() && getNeutronAgentModel().getIsValid();
    }

    private void cancel() {
        sourceListModel.setWindow(null);
    }

    private void flush() {
        provider.setName((String) name.getEntity());
        provider.setType((ProviderType) type.getSelectedItem());
        provider.setDescription((String) description.getEntity());
        provider.setUrl((String) url.getEntity());

        if (isTypeOpenStackNetwork()) {
            OpenstackNetworkProviderProperties properties = new OpenstackNetworkProviderProperties();
            properties.setPluginType(NeutronPluginTranslator.
                    getPluginNameForDisplayString((String) getPluginType().getSelectedItem()));
            properties.setAgentConfiguration(getNeutronAgentModel().flush());
            provider.setAdditionalProperties(properties);
        } else if (isTypeOpenStackImage()) {
            provider.setAdditionalProperties(new OpenStackImageProviderProperties());
        }

        boolean authenticationRequired = (Boolean) requiresAuthentication.getEntity();
        provider.setRequiringAuthentication(authenticationRequired);
        if (authenticationRequired) {
            provider.setUsername((String) getUsername().getEntity());
            provider.setPassword((String) getPassword().getEntity());
            if (getTenantName().getIsAvailable()) {
                TenantProviderProperties properties = (TenantProviderProperties) provider.getAdditionalProperties();
                if (properties == null) {
                    properties = new TenantProviderProperties();
                    provider.setAdditionalProperties(properties);
                }
                properties.setTenantName((String) getTenantName().getEntity());
            }
        } else {
            provider.setUsername(null);
            provider.setPassword(null);
            if (getTenantName().getIsAvailable()) {
                TenantProviderProperties properties = (TenantProviderProperties) provider.getAdditionalProperties();
                if (properties != null) {
                    properties.setTenantName(null);
                }
            }
        }
    }

    protected void preSave() {
        actualSave();
    }

    protected void actualSave() {
        flush();
        Frontend.RunAction(action, new ProviderParameters(provider), new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                sourceListModel.getSearchCommand().execute();
            }
        });
        cancel();
    }

    private void onSave() {
        if (!validate()) {
            return;
        }
        preSave();
    }

    private void onTest() {
        flush();
        startProgress(null);
        Frontend.RunAction(VdcActionType.TestProviderConnectivity,
                new ProviderParameters(provider),
                new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                VdcReturnValueBase res = result.getReturnValue();
                // If the connection failed on SSL issues, we try to fetch the provider certificate chain, and import it to the engine
                if (isFailedOnSSL(res)) {
                    AsyncQuery getCertChainQuery = new AsyncQuery();
                    getCertChainQuery.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object model, Object result)
                        {
                            if (result != null) {
                                ConfirmationModel confirmationModel = getImportChainConfirmationModel((String) result);
                                sourceListModel.setConfirmWindow(confirmationModel);
                            } else {
                                stopProgress();
                                setTestResultValue((VdcQueryReturnValue) result);
                            }
                        }
                    };
                    AsyncDataProvider.GetProviderCertificateChain(getCertChainQuery, provider);
                } else {
                    stopProgress();
                    setTestResultValue(res);
                }
            }
        }, null, false);
    }

    private boolean isFailedOnSSL(VdcReturnValueBase res) {
        return res != null && !res.getSucceeded() && res.getFault() != null && VdcBllErrors.PROVIDER_SSL_FAILURE.equals(res.getFault().getError());
    }

    private ConfirmationModel getImportChainConfirmationModel(String certChainString) {
        ConfirmationModel confirmationModel = new ConfirmationModel();
        confirmationModel.setMessage(ConstantsManager.getInstance().getConstants().theProviderHasTheFollowingCertificates()
                + certChainString
                + ConstantsManager.getInstance().getConstants().doYouApproveImportingTheseCertificates());
        confirmationModel.setTitle(ConstantsManager.getInstance().getConstants().importProviderCertificatesTitle());
        confirmationModel.setHashName("import_provider_certificates"); //$NON-NLS-1$
        UICommand importChainCommand = new UICommand(CMD_IMPORT_CHAIN, this);
        importChainCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        importChainCommand.setIsDefault(false);
        confirmationModel.getCommands().add(importChainCommand);
        UICommand cancelImport = new UICommand(CMD_CANCEL_IMPORT, this);
        cancelImport.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelImport.setIsCancel(true);
        cancelImport.setIsDefault(true);
        confirmationModel.getCommands().add(cancelImport);
        return confirmationModel;
    }

    private void importChain() {
        Frontend.RunAction(VdcActionType.ImportProviderCertificateChain,
                new ProviderParameters(provider),
                new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                VdcReturnValueBase res = result.getReturnValue();

                if (res != null && res.getSucceeded()) {
                    Frontend.RunAction(VdcActionType.TestProviderConnectivity,
                            new ProviderParameters(provider),
                            new IFrontendActionAsyncCallback() {

                        @Override
                        public void executed(FrontendActionAsyncResult result) {
                            VdcReturnValueBase res = result.getReturnValue();
                            setTestResultValue(res);
                            stopProgress();
                        }
                    }, null, false);
                } else {
                    setTestResultValue(res);
                    stopProgress();
                }
            }
        });
        sourceListModel.setConfirmWindow(null);
    }

    private void cancelImport() {
        stopProgress();
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), CMD_SAVE)) {
            onSave();
        } else if (StringHelper.stringsEqual(command.getName(), CMD_TEST)) {
            onTest();
        } else if (StringHelper.stringsEqual(command.getName(), CMD_CANCEL)) {
            cancel();
        } else if (StringHelper.stringsEqual(command.getName(), CMD_IMPORT_CHAIN)) {
            importChain();
        } else if (StringHelper.stringsEqual(command.getName(), CMD_CANCEL_IMPORT)) {
            cancelImport();
        }
    }

    private void setTestResultValue(VdcReturnValueBase result) {
        String errorMessage = EMPTY_ERROR_MESSAGE;
        if (result == null) {
            errorMessage = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
        } else if (!result.getSucceeded()) {
            if (result.getFault() != null) {
                errorMessage = Frontend.translateVdcFault(result.getFault());
            } else {
                errorMessage = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
            }
        }
        getTestResult().setEntity(errorMessage);
    }

    private void setTestResultValue(VdcQueryReturnValue result) {
        String errorMessage = EMPTY_ERROR_MESSAGE;
        if (result == null) {
            errorMessage = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
        } else if (!result.getSucceeded()) {
            if (result.getExceptionString() != null && !result.getExceptionString().isEmpty()) {
                errorMessage = result.getExceptionString();
            } else {
                errorMessage = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
            }
        }
        getTestResult().setEntity(errorMessage);
    }

}
