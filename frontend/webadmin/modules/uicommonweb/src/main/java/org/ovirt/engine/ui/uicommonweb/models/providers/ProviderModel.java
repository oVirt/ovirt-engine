package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.TenantProviderProperties;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.StringHelper;
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
    private static final String CMD_IMPORT_CHAIN = "ImportChain"; //$NON-NLS-1$
    private static final String CMD_CANCEL_IMPORT = "CancelImport"; //$NON-NLS-1$
    private static final String EMPTY_ERROR_MESSAGE = ""; //$NON-NLS-1$

    private final String keystoneUrl =
            (String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.KeystoneAuthUrl);

    protected final SearchableListModel sourceListModel;
    private final VdcActionType action;
    protected final Provider provider;

    private EntityModel<String> name = new EntityModel<String>();
    private EntityModel<String> description = new EntityModel<String>();
    private EntityModel<String> url = new EntityModel<String>();
    private EntityModel<Boolean> requiresAuthentication = new EntityModel<Boolean>();
    private EntityModel<String> username = new EntityModel<String>();
    private EntityModel<String> password = new EntityModel<String>();
    private EntityModel<String> tenantName = new EntityModel<String>();
    private ListModel<ProviderType> type;
    private UICommand testCommand;
    private EntityModel<String> testResult = new EntityModel<String>();

    private NeutronAgentModel neutronAgentModel = new NeutronAgentModel();

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

    protected boolean isTypeOpenStackNetwork() {
        return getType().getSelectedItem() == ProviderType.OPENSTACK_NETWORK;
    }

    private boolean isTypeOpenStackImage() {
        return getType().getSelectedItem() == ProviderType.OPENSTACK_IMAGE;
    }

    private boolean isTypeTenantAware() {
        ProviderType type = getType().getSelectedItem();
        return type == ProviderType.OPENSTACK_NETWORK || type == ProviderType.OPENSTACK_IMAGE;
    }

    private boolean isTypeRequiresAuthentication() {
        return false;
    }

    private String getDefaultUrl(ProviderType type) {
        if (type == null) {
            return ""; //$NON-NLS-1$
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
                boolean authenticationRequired = requiresAuthentication.getEntity();
                getUsername().setIsChangable(authenticationRequired);
                getPassword().setIsChangable(authenticationRequired);
                getTenantName().setIsChangable(authenticationRequired);
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
        getType().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isTenant = isTypeTenantAware();
                getTenantName().setIsAvailable(isTenant);
                if (isTenant) {
                    TenantProviderProperties properties = (TenantProviderProperties) provider.getAdditionalProperties();
                    getTenantName().setEntity(properties == null ? null : properties.getTenantName());
                }

                boolean isNeutron = isTypeOpenStackNetwork();
                getNeutronAgentModel().setIsAvailable(isNeutron);

                boolean requiresAuth = isTypeRequiresAuthentication();
                getRequiresAuthentication().setEntity(Boolean.valueOf(requiresAuth));
                getRequiresAuthentication().setIsChangable(!requiresAuth);
            }
        });

        getNeutronAgentModel().setIsAvailable(false);
        getTenantName().setIsAvailable(false);

        List<ProviderType> providerTypes = new ArrayList<ProviderType>(Arrays.asList(ProviderType.values()));
        Collections.sort(providerTypes, new Linq.ProviderTypeComparator());
        getType().setItems(providerTypes);

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
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });
        getType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getNeutronAgentModel().validate();
        boolean connectionSettingsValid = validateConnectionSettings();

        return connectionSettingsValid &&
                getName().getIsValid() &&
                getType().getIsValid() &&
                getNeutronAgentModel().getIsValid();
    }

    private boolean validateConnectionSettings() {
        getUsername().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getTenantName().validateEntity(new IValidation[] { new NotEmptyValidation()} );
        getUrl().validateEntity(new IValidation[] { new NotEmptyValidation(),
                new UrlValidation(Uri.SCHEME_HTTP, Uri.SCHEME_HTTPS) });

        return getUrl().getIsValid() &&
                getUsername().getIsValid() &&
                getPassword().getIsValid() &&
                getTenantName().getIsValid();
    }

    private void cancel() {
        sourceListModel.setWindow(null);
    }

    private void flush() {
        provider.setName(name.getEntity());
        provider.setType(type.getSelectedItem());
        provider.setDescription(description.getEntity());
        provider.setUrl(url.getEntity());

        if (isTypeOpenStackNetwork()) {
            getNeutronAgentModel().flush(provider);
        } else if (isTypeOpenStackImage()) {
            provider.setAdditionalProperties(new OpenStackImageProviderProperties());
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
        Frontend.getInstance().runAction(action, new ProviderParameters(provider), new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                if (result.getReturnValue() == null || !result.getReturnValue().getSucceeded()) {
                    return;
                }
                sourceListModel.getSearchCommand().execute();
                cancel();
            }
        });
    }

    private void onSave() {
        if (!validate()) {
            return;
        }
        preSave();
    }

    private void onTest() {
        if (!validateConnectionSettings()) {
            return;
        }

        flush();
        startProgress(null);
        Frontend.getInstance().runAction(VdcActionType.TestProviderConnectivity,
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
                                getTestResult().setEntity(ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg());
                            }
                        }
                    };
                    AsyncDataProvider.getProviderCertificateChain(getCertChainQuery, provider);
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
        confirmationModel.setHelpTag(HelpTag.import_provider_certificates);
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
        Frontend.getInstance().runAction(VdcActionType.ImportProviderCertificateChain,
                new ProviderParameters(provider),
                new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                VdcReturnValueBase res = result.getReturnValue();

                if (res != null && res.getSucceeded()) {
                    Frontend.getInstance().runAction(VdcActionType.TestProviderConnectivity,
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

        if (CMD_SAVE.equals(command.getName())) {
            onSave();
        } else if (CMD_TEST.equals(command.getName())) {
            onTest();
        } else if (CMD_CANCEL.equals(command.getName())) {
            cancel();
        } else if (CMD_IMPORT_CHAIN.equals(command.getName())) {
            importChain();
        } else if (CMD_CANCEL_IMPORT.equals(command.getName())) {
            cancelImport();
        }
    }

    private void setTestResultValue(VdcReturnValueBase result) {
        String errorMessage = EMPTY_ERROR_MESSAGE;
        if (result == null || !result.getSucceeded()) {
            if ((Boolean) requiresAuthentication.getEntity() && StringHelper.isNullOrEmpty(keystoneUrl)) {
                errorMessage = ConstantsManager.getInstance().getConstants().noAuthUrl();
            } else if (result != null) {
                errorMessage = Frontend.getInstance().translateVdcFault(result.getFault());
            } else {
                errorMessage = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
            }
        }
        getTestResult().setEntity(errorMessage);
    }


}
