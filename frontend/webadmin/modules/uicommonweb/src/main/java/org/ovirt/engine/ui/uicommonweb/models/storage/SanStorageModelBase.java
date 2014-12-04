package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
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

@SuppressWarnings("unused")
public abstract class SanStorageModelBase extends SearchableListModel implements IStorageModel
{

    private UICommand privateUpdateCommand;

    @Override
    public UICommand getUpdateCommand()
    {
        return privateUpdateCommand;
    }

    private void setUpdateCommand(UICommand value)
    {
        privateUpdateCommand = value;
    }

    private UICommand loginCommand;

    public UICommand getLoginCommand()
    {
        return loginCommand;
    }

    private void setLoginCommand(UICommand value)
    {
        loginCommand = value;
    }

    private UICommand privateDiscoverTargetsCommand;

    public UICommand getDiscoverTargetsCommand()
    {
        return privateDiscoverTargetsCommand;
    }

    private void setDiscoverTargetsCommand(UICommand value)
    {
        privateDiscoverTargetsCommand = value;
    }

    private StorageModel privateContainer;

    @Override
    public StorageModel getContainer()
    {
        return privateContainer;
    }

    @Override
    public void setContainer(StorageModel value)
    {
        privateContainer = value;
    }

    private StorageDomainType privateRole = StorageDomainType.values()[0];

    @Override
    public StorageDomainType getRole()
    {
        return privateRole;
    }

    @Override
    public void setRole(StorageDomainType value)
    {
        privateRole = value;
    }

    @Override
    public abstract StorageType getType();

    private EntityModel<String> privateAddress;

    public EntityModel<String> getAddress()
    {
        return privateAddress;
    }

    private void setAddress(EntityModel<String> value)
    {
        privateAddress = value;
    }

    private EntityModel<String> privatePort;

    public EntityModel<String> getPort()
    {
        return privatePort;
    }

    private void setPort(EntityModel<String> value)
    {
        privatePort = value;
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

    private EntityModel<String> privatePassword;

    public EntityModel<String> getPassword()
    {
        return privatePassword;
    }

    private void setPassword(EntityModel<String> value)
    {
        privatePassword = value;
    }

    private EntityModel<Boolean> privateUseUserAuth;

    public EntityModel<Boolean> getUseUserAuth()
    {
        return privateUseUserAuth;
    }

    private void setUseUserAuth(EntityModel<Boolean> value)
    {
        privateUseUserAuth = value;
    }

    private boolean proposeDiscoverTargets;

    public boolean getProposeDiscoverTargets()
    {
        return proposeDiscoverTargets;
    }

    public void setProposeDiscoverTargets(boolean value)
    {
        if (proposeDiscoverTargets != value)
        {
            proposeDiscoverTargets = value;
            onPropertyChanged(new PropertyChangedEventArgs("ProposeDiscoverTargets")); //$NON-NLS-1$
        }
    }

    private boolean isAllLunsSelected;

    public boolean getIsAllLunsSelected()
    {
        return isAllLunsSelected;
    }

    public void setIsAllLunsSelected(boolean value)
    {
        if (isAllLunsSelected != value)
        {
            isAllLunsSelected = value;
            isAllLunsSelectedChanged();
            onPropertyChanged(new PropertyChangedEventArgs("IsAllLunsSelected")); //$NON-NLS-1$
        }
    }

    private boolean ignoreGrayedOut;

    public boolean isIgnoreGrayedOut()
    {
        return ignoreGrayedOut;
    }

    public void setIgnoreGrayedOut(boolean value)
    {
        if (ignoreGrayedOut != value)
        {
            ignoreGrayedOut = value;
            onPropertyChanged(new PropertyChangedEventArgs("IgnoreGrayedOut")); //$NON-NLS-1$
        }
    }

    private boolean multiSelection;

    public boolean isMultiSelection()
    {
        return multiSelection;
    }

    public void setMultiSelection(boolean value)
    {
        if (multiSelection != value)
        {
            multiSelection = value;
            onPropertyChanged(new PropertyChangedEventArgs("MultiSelection")); //$NON-NLS-1$
        }
    }

    private String selectedLunWarning;

    public String getSelectedLunWarning()
    {
        return selectedLunWarning;
    }

    public void setSelectedLunWarning(String value)
    {
        if (!ObjectUtils.objectsEqual(selectedLunWarning, value))
        {
            selectedLunWarning = value;
            onPropertyChanged(new PropertyChangedEventArgs("SelectedLunWarning")); //$NON-NLS-1$
        }
    }

    private String privateHash;

    public String getHash()
    {
        return privateHash;
    }

    public void setHash(String value)
    {
        privateHash = value;
    }

    private ArrayList<SanTargetModel> targetsToConnect;

    protected SanStorageModelBase()
    {
        Frontend.getInstance().getQueryStartedEvent().addListener(this);
        Frontend.getInstance().getQueryCompleteEvent().addListener(this);

        setHelpTag(HelpTag.SanStorageModelBase);
        setHashName("SanStorageModelBase"); //$NON-NLS-1$
        setHash(getHashName() + new Date());

        setUpdateCommand(new UICommand("Update", this)); //$NON-NLS-1$
        UICommand tempVar = new UICommand("Login", this); //$NON-NLS-1$
        tempVar.setIsExecutionAllowed(false);
        setLoginCommand(tempVar);
        setDiscoverTargetsCommand(new UICommand("DiscoverTargets", this)); //$NON-NLS-1$

        setAddress(new EntityModel<String>());
        EntityModel<String> tempVar2 = new EntityModel<String>();
        tempVar2.setEntity("3260"); //$NON-NLS-1$
        setPort(tempVar2);
        setUserName(new EntityModel<String>());
        setPassword(new EntityModel<String>());
        EntityModel<Boolean> tempVar3 = new EntityModel<Boolean>();
        tempVar3.setEntity(false);
        setUseUserAuth(tempVar3);
        getUseUserAuth().getEntityChangedEvent().addListener(this);

        updateUserAuthFields();
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(SanTargetModel.loggedInEventDefinition))
        {
            sanTargetModel_LoggedIn(sender, args);
        }
        else if (ev.matchesDefinition(entityChangedEventDefinition))
        {
            useUserAuth_EntityChanged(sender, args);
        }
        else if (ev.matchesDefinition(Frontend.getInstance().getQueryStartedEventDefinition())
                && ObjectUtils.objectsEqual(Frontend.getInstance().getCurrentContext(), getHash()))
        {
            frontend_QueryStarted();
        }
        else if (ev.matchesDefinition(Frontend.getInstance().getQueryCompleteEventDefinition())
                && ObjectUtils.objectsEqual(Frontend.getInstance().getCurrentContext(), getHash()))
        {
            frontend_QueryComplete();
        }
    }

    private int queryCounter;

    private void frontend_QueryStarted()
    {
        queryCounter++;
        if (getProgress() == null)
        {
            startProgress(null);
        }
    }

    private void frontend_QueryComplete()
    {
        queryCounter--;
        if (queryCounter == 0)
        {
            stopProgress();
        }
    }

    private void postLogin(FrontendActionAsyncResult result) {
        VdcReturnValueBase returnValue = result.getReturnValue();
        SanStorageModelBase sanStorageModel = (SanStorageModelBase) result.getState();
        SanTargetModel sanTargetModel = sanStorageModel.targetsToConnect.remove(0);
        boolean success = returnValue != null && returnValue.getSucceeded();

        if (success)
        {
            sanTargetModel.setIsLoggedIn(true);
            sanTargetModel.getLoginCommand().setIsExecutionAllowed(false);
        }

        if (sanStorageModel.targetsToConnect.isEmpty()) {
            sanStorageModel.updateInternal();
        }
    }

    private void connectTargets() {

        VDS host = getContainer().getHost().getSelectedItem();
        if (host == null)
        {
            return;
        }

        ArrayList<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        ArrayList<IFrontendActionAsyncCallback> callbacks = new ArrayList<IFrontendActionAsyncCallback>();

        IFrontendActionAsyncCallback loginCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                SanStorageModelBase sanStorageModel = (SanStorageModelBase) result.getState();
                sanStorageModel.postLogin(result);
            }
        };

        for (int i = 0; i < targetsToConnect.size(); i++) {
            SanTargetModel model = targetsToConnect.get(i);
            StorageServerConnections connection = new StorageServerConnections();
            connection.setstorage_type(StorageType.ISCSI);
            connection.setuser_name(getUseUserAuth().getEntity() ? getUserName().getEntity() : ""); //$NON-NLS-1$
            connection.setpassword(getUseUserAuth().getEntity() ? getPassword().getEntity() : ""); //$NON-NLS-1$
            connection.setiqn(model.getName());
            connection.setconnection(model.getAddress());
            connection.setport(String.valueOf(model.getPort()));

            actionTypes.add(VdcActionType.ConnectStorageToVds);
            parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
            callbacks.add(loginCallback);
        }

        getContainer().startProgress(null);

        Frontend.getInstance().runMultipleActions(actionTypes, parameters, callbacks, null, this);
    }

    private void sanTargetModel_LoggedIn(Object sender, EventArgs args)
    {
        SanTargetModel model = (SanTargetModel) sender;
        targetsToConnect = new ArrayList<SanTargetModel>();
        targetsToConnect.add(model);
        connectTargets();
    }

    protected void login() {
        loginAll();
    }

    private void loginAll()
    {
        // Cast to list of SanTargetModel because we get call
        // to this method only from target/LUNs mode.
        List<SanTargetModel> items = (List<SanTargetModel>) getItems();
        targetsToConnect = new ArrayList<SanTargetModel>();

        for (SanTargetModel item : items)
        {
            if (!item.getIsLoggedIn())
            {
                targetsToConnect.add(item);
            }
        }

        connectTargets();
    }

    private void discoverTargets()
    {
        if (!validateDiscoverTargetFields())
        {
            return;
        }

        VDS host = getContainer().getHost().getSelectedItem();

        StorageServerConnections tempVar = new StorageServerConnections();
        tempVar.setconnection(getAddress().getEntity().trim());
        tempVar.setport(getPort().getEntity().trim());
        tempVar.setstorage_type(StorageType.ISCSI);
        tempVar.setuser_name(getUseUserAuth().getEntity() ? getUserName().getEntity() : ""); //$NON-NLS-1$
        tempVar.setpassword(getUseUserAuth().getEntity() ? getPassword().getEntity() : ""); //$NON-NLS-1$
        DiscoverSendTargetsQueryParameters parameters =
                new DiscoverSendTargetsQueryParameters(host.getId(), tempVar);

        setMessage(null);

        AsyncQuery asyncQuery = new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                SanStorageModelBase model = (SanStorageModelBase) target;
                Object result = ((VdcQueryReturnValue) returnValue).getReturnValue();
                model.postDiscoverTargetsInternal(result != null ? (ArrayList<StorageServerConnections>) result
                        : new ArrayList<StorageServerConnections>());
            }
        }, true);
        asyncQuery.setContext(getHash());
        Frontend.getInstance().runQuery(VdcQueryType.DiscoverSendTargets, parameters, asyncQuery);
    }

    protected void postDiscoverTargetsInternal(ArrayList<StorageServerConnections> items)
    {
        ArrayList<SanTargetModel> newItems = new ArrayList<SanTargetModel>();

        for (StorageServerConnections a : items)
        {
            SanTargetModel model = new SanTargetModel();
            model.setAddress(a.getconnection());
            model.setPort(a.getport());
            model.setName(a.getiqn());
            model.setLuns(new ObservableCollection<LunModel>());
            model.getLoggedInEvent().addListener(this);

            newItems.add(model);
        }

        if (items.isEmpty())
        {
            setMessage(ConstantsManager.getInstance().getConstants().noNewDevicesWereFoundMsg());
        }

        postDiscoverTargets(newItems);
    }

    protected void postDiscoverTargets(ArrayList<SanTargetModel> newItems)
    {
    }

    private boolean validateDiscoverTargetFields()
    {
        getContainer().getHost().validateSelectedItem(new NotEmptyValidation[] { new NotEmptyValidation() });

        getAddress().validateEntity(new IValidation[] { new NotEmptyValidation() });

        IntegerValidation tempVar = new IntegerValidation();
        tempVar.setMinimum(0);
        tempVar.setMaximum(65535);
        getPort().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

        if (getUseUserAuth().getEntity())
        {
            getUserName().validateEntity(new IValidation[] { new NotEmptyValidation() });
            getPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });
        }

        return getContainer().getHost().getIsValid() && getAddress().getIsValid() && getPort().getIsValid()
                && getUserName().getIsValid() && getPassword().getIsValid();
    }

    @Override
    public boolean validate()
    {
        return true;
    }

    private void useUserAuth_EntityChanged(Object sender, EventArgs args)
    {
        updateUserAuthFields();
    }

    private void updateUserAuthFields()
    {
        getUserName().setIsValid(true);
        getUserName().setIsChangable(getUseUserAuth().getEntity());

        getPassword().setIsValid(true);
        getPassword().setIsChangable(getUseUserAuth().getEntity());
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getUpdateCommand())
        {
            update();
        }
        else if (command == getLoginCommand())
        {
            login();
        }
        else if (command == getDiscoverTargetsCommand())
        {
            discoverTargets();
        }
    }

    protected void update()
    {
        updateInternal();
        setIsValid(true);
    }

    protected void updateInternal()
    {
    }

    protected void updateLoginAvailability()
    {
        List<SanTargetModel> items = (List<SanTargetModel>) getItems();

        // Allow login all command when there at least one target that may be logged in.
        boolean allow = false;

        for (SanTargetModel item : items)
        {
            if (!item.getIsLoggedIn())
            {
                allow = true;
                break;
            }
        }

        getLoginCommand().setIsExecutionAllowed(allow);
    }

    protected void isAllLunsSelectedChanged()
    {
    }

    public String getLoginButtonLabel() {
        return ""; //$NON-NLS-1$
    }
}
