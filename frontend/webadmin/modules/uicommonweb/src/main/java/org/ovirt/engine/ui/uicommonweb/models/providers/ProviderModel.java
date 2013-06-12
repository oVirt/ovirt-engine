package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
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

    private final ListModel sourceListModel;
    private final VdcActionType action;
    private final Provider provider;

    private EntityModel privateName;
    private ListModel privateType;
    private EntityModel privateDescription;
    private EntityModel privateUrl;
    private EntityModel privateRequiresAuthentication;
    private EntityModel privateUsername;
    private EntityModel privatePassword;
    private UICommand privateTestCommand;
    private EntityModel privateTestResult;

    public EntityModel getName() {
        return privateName;
    }

    private void setName(EntityModel value) {
        privateName = value;
    }

    public ListModel getType() {
        return privateType;
    }

    private void setType(ListModel value) {
        privateType = value;
    }

    public EntityModel getDescription() {
        return privateDescription;
    }

    private void setDescription(EntityModel value) {
        privateDescription = value;
    }

    public EntityModel getUrl() {
        return privateUrl;
    }

    private void setUrl(EntityModel value) {
        privateUrl = value;
    }

    public EntityModel getRequiresAuthentication() {
        return privateRequiresAuthentication;
    }

    private void setRequiresAuthentication(EntityModel value) {
        privateRequiresAuthentication = value;
    }

    public EntityModel getUsername() {
        return privateUsername;
    }

    private void setUsername(EntityModel value) {
        privateUsername = value;
    }

    public EntityModel getPassword() {
        return privatePassword;
    }

    private void setPassword(EntityModel value) {
        privatePassword = value;
    }

    public UICommand getTestCommand() {
        return privateTestCommand;
    }

    private void setTestCommand(UICommand value) {
        privateTestCommand = value;
    }

    public EntityModel getTestResult() {
        return privateTestResult;
    }

    private void setTestResult(EntityModel value) {
        privateTestResult = value;
    }

    public ProviderModel(ListModel sourceListModel, VdcActionType action, Provider provider) {
        this.sourceListModel = sourceListModel;
        this.action = action;
        this.provider = provider;

        setRequiresAuthentication(new EntityModel());
        getRequiresAuthentication().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean requiresAuthentication = (Boolean) privateRequiresAuthentication.getEntity();
                getUsername().setIsChangable(requiresAuthentication);
                getPassword().setIsChangable(requiresAuthentication);
            }
        });

        setName(new EntityModel(provider.getName()));
        setType(new ListModel());
        setDescription(new EntityModel(provider.getDescription()));
        setUrl(new EntityModel(provider.getUrl()));
        setUsername(new EntityModel(provider.getUsername()));
        setPassword(new EntityModel(provider.getPassword()));
        getRequiresAuthentication().setEntity(provider.isRequiringAuthentication());
        setTestResult(new EntityModel());

        List<ProviderType> allTypes = Arrays.asList(ProviderType.values());
        getType().setItems(allTypes);
        getType().setSelectedItem(provider.getType());

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
        getUsername().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });

        Uri url = new Uri((String) getUrl().getEntity());
        if (url.getScheme().isEmpty()) {
            url.setScheme(Uri.SCHEME_HTTP);
            getUrl().setEntity(url.getStringRepresentation());
        }
        getUrl().validateEntity(new IValidation[] { new NotEmptyValidation(),
                new UrlValidation(Uri.SCHEME_HTTP, Uri.SCHEME_HTTPS) });

        return getName().getIsValid() && getType().getIsValid() && getUrl().getIsValid() && getUsername().getIsValid()
                && getPassword().getIsValid();
    }

    private void cancel() {
        sourceListModel.setWindow(null);
    }

    private void flush() {
        provider.setName((String) privateName.getEntity());
        provider.setType((ProviderType) privateType.getSelectedItem());
        provider.setDescription((String) privateDescription.getEntity());
        provider.setUrl((String) privateUrl.getEntity());

        boolean requiresAuthentication = (Boolean) privateRequiresAuthentication.getEntity();
        provider.setRequiringAuthentication(requiresAuthentication);
        if (requiresAuthentication) {
            provider.setUsername((String) privateUsername.getEntity());
            provider.setPassword((String) privatePassword.getEntity());
        }
    }

    private void onSave() {
        if (!validate()) {
            return;
        }

        flush();
        Frontend.RunAction(action, new ProviderParameters(provider));
        cancel();
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
