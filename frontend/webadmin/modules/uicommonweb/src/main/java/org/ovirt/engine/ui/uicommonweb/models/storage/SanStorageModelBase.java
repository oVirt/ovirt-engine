package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class SanStorageModelBase extends SearchableListModel implements IStorageModel {

    private UICommand updateCommand;

    @Override
    public UICommand getUpdateCommand() {
        return updateCommand;
    }

    private void setUpdateCommand(UICommand value) {
        updateCommand = value;
    }

    private UICommand loginCommand;

    public UICommand getLoginCommand() {
        return loginCommand;
    }

    private void setLoginCommand(UICommand value) {
        loginCommand = value;
    }

    private UICommand discoverTargetsCommand;

    public UICommand getDiscoverTargetsCommand() {
        return discoverTargetsCommand;
    }

    private void setDiscoverTargetsCommand(UICommand value) {
        discoverTargetsCommand = value;
    }

    private StorageModel container;

    @Override
    public StorageModel getContainer() {
        return container;
    }

    @Override
    public void setContainer(StorageModel value) {
        container = value;
    }

    private StorageDomainType role = StorageDomainType.values()[0];

    @Override
    public StorageDomainType getRole() {
        return role;
    }

    @Override
    public void setRole(StorageDomainType value) {
        role = value;
    }

    @Override
    public abstract StorageType getType();

    private EntityModel<String> address;

    public EntityModel<String> getAddress() {
        return address;
    }

    private void setAddress(EntityModel<String> value) {
        address = value;
    }

    private EntityModel<String> port;

    public EntityModel<String> getPort() {
        return port;
    }

    private void setPort(EntityModel<String> value) {
        port = value;
    }

    private EntityModel<String> userName;

    public EntityModel<String> getUserName() {
        return userName;
    }

    private void setUserName(EntityModel<String> value) {
        userName = value;
    }

    private EntityModel<String> password;

    public EntityModel<String> getPassword() {
        return password;
    }

    private void setPassword(EntityModel<String> value) {
        password = value;
    }

    private EntityModel<Boolean> useUserAuth;

    public EntityModel<Boolean> getUseUserAuth() {
        return useUserAuth;
    }

    private void setUseUserAuth(EntityModel<Boolean> value) {
        useUserAuth = value;
    }

    private boolean proposeDiscoverTargets;

    public boolean getProposeDiscoverTargets() {
        return proposeDiscoverTargets;
    }

    public void setProposeDiscoverTargets(boolean value) {
        if (proposeDiscoverTargets != value) {
            proposeDiscoverTargets = value;
            onPropertyChanged(new PropertyChangedEventArgs("ProposeDiscoverTargets")); //$NON-NLS-1$
        }
    }

    private boolean isAllLunsSelected;

    public boolean getIsAllLunsSelected() {
        return isAllLunsSelected;
    }

    public void setIsAllLunsSelected(boolean value) {
        if (isAllLunsSelected != value) {
            isAllLunsSelected = value;
            isAllLunsSelectedChanged();
            onPropertyChanged(new PropertyChangedEventArgs("IsAllLunsSelected")); //$NON-NLS-1$
        }
    }

    private boolean ignoreGrayedOut;

    public boolean isIgnoreGrayedOut() {
        return ignoreGrayedOut;
    }

    public void setIgnoreGrayedOut(boolean value) {
        if (ignoreGrayedOut != value) {
            ignoreGrayedOut = value;
            onPropertyChanged(new PropertyChangedEventArgs("IgnoreGrayedOut")); //$NON-NLS-1$
        }
    }

    private boolean multiSelection;

    public boolean isMultiSelection() {
        return multiSelection;
    }

    public void setMultiSelection(boolean value) {
        if (multiSelection != value) {
            multiSelection = value;
            onPropertyChanged(new PropertyChangedEventArgs("MultiSelection")); //$NON-NLS-1$
        }
    }

    private String selectedLunWarning;

    public String getSelectedLunWarning() {
        return selectedLunWarning;
    }

    public void setSelectedLunWarning(String value) {
        if (!Objects.equals(selectedLunWarning, value)) {
            selectedLunWarning = value;
            onPropertyChanged(new PropertyChangedEventArgs("SelectedLunWarning")); //$NON-NLS-1$
        }
    }

    private ArrayList<SanTargetModel> targetsToConnect;

    protected SanStorageModelBase() {
        setHelpTag(HelpTag.SanStorageModelBase);
        setHashName("SanStorageModelBase"); //$NON-NLS-1$

        setUpdateCommand(new UICommand("Update", this)); //$NON-NLS-1$
        UICommand tempVar = new UICommand("Login", this); //$NON-NLS-1$
        tempVar.setIsExecutionAllowed(false);
        setLoginCommand(tempVar);
        setDiscoverTargetsCommand(new UICommand("DiscoverTargets", this)); //$NON-NLS-1$

        setAddress(new EntityModel<String>());
        EntityModel<String> tempVar2 = new EntityModel<>();
        tempVar2.setEntity("3260"); //$NON-NLS-1$
        setPort(tempVar2);
        setUserName(new EntityModel<String>());
        setPassword(new EntityModel<String>());
        EntityModel<Boolean> tempVar3 = new EntityModel<>();
        tempVar3.setEntity(false);
        setUseUserAuth(tempVar3);
        getUseUserAuth().getEntityChangedEvent().addListener(this);

        updateUserAuthFields();
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(SanTargetModel.loggedInEventDefinition)) {
            sanTargetModel_LoggedIn(sender, args);
        }
        else if (ev.matchesDefinition(entityChangedEventDefinition)) {
            useUserAuth_EntityChanged(sender, args);
        }
    }

    private void postLogin( VdcReturnValueBase returnValue, SanStorageModelBase sanStorageModel) {
        SanTargetModel sanTargetModel = sanStorageModel.targetsToConnect.remove(0);
        boolean success = returnValue != null && returnValue.getSucceeded();

        if (success) {
            sanTargetModel.setIsLoggedIn(true);
            sanTargetModel.getLoginCommand().setIsExecutionAllowed(false);
        }

        if (sanStorageModel.targetsToConnect.isEmpty()) {
            sanStorageModel.updateInternal();
        }
    }

    private void connectTargets() {

        VDS host = getContainer().getHost().getSelectedItem();
        if (host == null) {
            return;
        }

        ArrayList<VdcActionType> actionTypes = new ArrayList<>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        ArrayList<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();

        final SanStorageModelBase sanStorageModel = this;
        IFrontendActionAsyncCallback loginCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                sanStorageModel.postLogin(result.getReturnValue(), sanStorageModel);
            }
        };

        for (int i = 0; i < targetsToConnect.size(); i++) {
            SanTargetModel model = targetsToConnect.get(i);
            StorageServerConnections connection = new StorageServerConnections();
            connection.setStorageType(StorageType.ISCSI);
            connection.setUserName(getUseUserAuth().getEntity() ? getUserName().getEntity() : ""); //$NON-NLS-1$
            connection.setPassword(getUseUserAuth().getEntity() ? getPassword().getEntity() : ""); //$NON-NLS-1$
            connection.setIqn(model.getName());
            connection.setConnection(model.getAddress());
            connection.setPort(String.valueOf(model.getPort()));

            actionTypes.add(VdcActionType.ConnectStorageToVds);
            parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
            callbacks.add(loginCallback);
        }

        Object target = getWidgetModel() != null ? getWidgetModel() : getContainer();
        Frontend.getInstance().runMultipleActions(actionTypes, parameters, callbacks, null, target);
    }

    private void sanTargetModel_LoggedIn(Object sender, EventArgs args) {
        SanTargetModel model = (SanTargetModel) sender;
        targetsToConnect = new ArrayList<>();
        targetsToConnect.add(model);
        connectTargets();
    }

    protected void login() {
        loginAll();
    }

    private void loginAll() {
        // Cast to list of SanTargetModel because we get call
        // to this method only from target/LUNs mode.
        List<SanTargetModel> items = (List<SanTargetModel>) getItems();
        targetsToConnect = new ArrayList<>();

        for (SanTargetModel item : items) {
            if (!item.getIsLoggedIn()) {
                targetsToConnect.add(item);
            }
        }

        connectTargets();
    }

    private void discoverTargets() {
        if (!validateDiscoverTargetFields()) {
            return;
        }

        VDS host = getContainer().getHost().getSelectedItem();

        StorageServerConnections tempVar = new StorageServerConnections();
        tempVar.setConnection(getAddress().getEntity().trim());
        tempVar.setPort(getPort().getEntity().trim());
        tempVar.setStorageType(StorageType.ISCSI);
        tempVar.setUserName(getUseUserAuth().getEntity() ? getUserName().getEntity() : ""); //$NON-NLS-1$
        tempVar.setPassword(getUseUserAuth().getEntity() ? getPassword().getEntity() : ""); //$NON-NLS-1$
        DiscoverSendTargetsQueryParameters parameters =
                new DiscoverSendTargetsQueryParameters(host.getId(), tempVar);

        setMessage(null);

        final SanStorageModelBase model = this;
        Object target = getWidgetModel() != null ? getWidgetModel() : getContainer();
        AsyncQuery asyncQuery = new AsyncQuery(target, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                Object result = ((VdcQueryReturnValue) returnValue).getReturnValue();
                model.postDiscoverTargetsInternal(result != null ? (ArrayList<StorageServerConnections>) result
                        : new ArrayList<StorageServerConnections>());
            }
        }, true);
        Frontend.getInstance().runQuery(VdcQueryType.DiscoverSendTargets, parameters, asyncQuery);
    }

    protected void postDiscoverTargetsInternal(ArrayList<StorageServerConnections> items) {
        ArrayList<SanTargetModel> newItems = new ArrayList<>();

        for (StorageServerConnections a : items) {
            SanTargetModel model = new SanTargetModel();
            model.setAddress(a.getConnection());
            model.setPort(a.getPort());
            model.setName(a.getIqn());
            model.setLuns(new ObservableCollection<LunModel>());
            model.getLoggedInEvent().addListener(this);

            newItems.add(model);
        }

        if (items.isEmpty()) {
            setMessage(ConstantsManager.getInstance().getConstants().noNewDevicesWereFoundMsg());
        }

        postDiscoverTargets(newItems);
    }

    protected void postDiscoverTargets(ArrayList<SanTargetModel> newItems) {
    }

    private boolean validateDiscoverTargetFields() {
        getContainer().getHost().validateSelectedItem(new NotEmptyValidation[] { new NotEmptyValidation() });

        getAddress().validateEntity(new IValidation[] { new NotEmptyValidation() });

        IntegerValidation tempVar = new IntegerValidation();
        tempVar.setMinimum(0);
        tempVar.setMaximum(65535);
        getPort().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

        if (getUseUserAuth().getEntity()) {
            getUserName().validateEntity(new IValidation[] { new NotEmptyValidation() });
            getPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });
        }

        return getContainer().getHost().getIsValid() && getAddress().getIsValid() && getPort().getIsValid()
                && getUserName().getIsValid() && getPassword().getIsValid();
    }

    @Override
    public boolean validate() {
        return true;
    }

    private void useUserAuth_EntityChanged(Object sender, EventArgs args) {
        updateUserAuthFields();
    }

    private void updateUserAuthFields() {
        getUserName().setIsValid(true);
        getUserName().setIsChangeable(getUseUserAuth().getEntity());

        getPassword().setIsValid(true);
        getPassword().setIsChangeable(getUseUserAuth().getEntity());
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getUpdateCommand()) {
            update();
        }
        else if (command == getLoginCommand()) {
            login();
        }
        else if (command == getDiscoverTargetsCommand()) {
            discoverTargets();
        }
    }

    protected void update() {
        updateInternal();
        setIsValid(true);
    }

    protected void updateInternal() {
    }

    protected void updateLoginAvailability() {
        List<SanTargetModel> items = (List<SanTargetModel>) getItems();

        // Allow login all command when there at least one target that may be logged in.
        boolean allow = false;

        for (SanTargetModel item : items) {
            if (!item.getIsLoggedIn()) {
                allow = true;
                break;
            }
        }

        getLoginCommand().setIsExecutionAllowed(allow);
    }

    protected void isAllLunsSelectedChanged() {
    }

    public String getLoginButtonLabel() {
        return ""; //$NON-NLS-1$
    }
}
