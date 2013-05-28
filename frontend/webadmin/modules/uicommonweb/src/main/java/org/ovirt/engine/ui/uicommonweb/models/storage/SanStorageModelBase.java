package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
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

    private UICommand privateLoginAllCommand;

    public UICommand getLoginAllCommand()
    {
        return privateLoginAllCommand;
    }

    private void setLoginAllCommand(UICommand value)
    {
        privateLoginAllCommand = value;
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

    private EntityModel privateAddress;

    public EntityModel getAddress()
    {
        return privateAddress;
    }

    private void setAddress(EntityModel value)
    {
        privateAddress = value;
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

    private EntityModel privateUserName;

    public EntityModel getUserName()
    {
        return privateUserName;
    }

    private void setUserName(EntityModel value)
    {
        privateUserName = value;
    }

    private EntityModel privatePassword;

    public EntityModel getPassword()
    {
        return privatePassword;
    }

    private void setPassword(EntityModel value)
    {
        privatePassword = value;
    }

    private EntityModel privateUseUserAuth;

    public EntityModel getUseUserAuth()
    {
        return privateUseUserAuth;
    }

    private void setUseUserAuth(EntityModel value)
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
        if (!StringHelper.stringsEqual(selectedLunWarning, value))
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

    public boolean loginAllInProgress;
    public SanTargetModel sanTargetModel;
    private ArrayList<SanTargetModel> targetsToConnect;

    protected SanStorageModelBase()
    {
        Frontend.getQueryStartedEvent().addListener(this);
        Frontend.getQueryCompleteEvent().addListener(this);

        setHashName("SanStorageModelBase"); //$NON-NLS-1$
        setHash(getHashName() + new Date());

        setUpdateCommand(new UICommand("Update", this)); //$NON-NLS-1$
        UICommand tempVar = new UICommand("LoginAll", this); //$NON-NLS-1$
        tempVar.setIsExecutionAllowed(false);
        setLoginAllCommand(tempVar);
        setDiscoverTargetsCommand(new UICommand("DiscoverTargets", this)); //$NON-NLS-1$

        setAddress(new EntityModel());
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setEntity("3260"); //$NON-NLS-1$
        setPort(tempVar2);
        setUserName(new EntityModel());
        setPassword(new EntityModel());
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setEntity(false);
        setUseUserAuth(tempVar3);
        getUseUserAuth().getEntityChangedEvent().addListener(this);

        updateUserAuthFields();
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(SanTargetModel.LoggedInEventDefinition))
        {
            sanTargetModel_LoggedIn(sender, args);
        }
        else if (ev.matchesDefinition(EntityChangedEventDefinition))
        {
            useUserAuth_EntityChanged(sender, args);
        }
        else if (ev.matchesDefinition(Frontend.QueryStartedEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
        {
            frontend_QueryStarted();
        }
        else if (ev.matchesDefinition(Frontend.QueryCompleteEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
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

        VDS host = (VDS) getContainer().getHost().getSelectedItem();
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
            connection.setportal("0"); //$NON-NLS-1$
            connection.setstorage_type(StorageType.ISCSI);
            connection.setuser_name((Boolean) getUseUserAuth().getEntity() ? (String) getUserName().getEntity() : ""); //$NON-NLS-1$
            connection.setpassword((Boolean) getUseUserAuth().getEntity() ? (String) getPassword().getEntity() : ""); //$NON-NLS-1$
            connection.setiqn(model.getName());
            connection.setconnection(model.getAddress());
            connection.setport(String.valueOf(model.getPort()));

            actionTypes.add(VdcActionType.ConnectStorageToVds);
            parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
            callbacks.add(loginCallback);
        }

        getContainer().startProgress(null);

        Frontend.RunMultipleActions(actionTypes, parameters, callbacks, null, this);
    }

    private void sanTargetModel_LoggedIn(Object sender, EventArgs args)
    {
        SanTargetModel model = (SanTargetModel) sender;
        targetsToConnect = new ArrayList<SanTargetModel>();
        targetsToConnect.add(model);
        connectTargets();
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

        VDS host = (VDS) getContainer().getHost().getSelectedItem();

        StorageServerConnections tempVar = new StorageServerConnections();
        tempVar.setconnection(((String) getAddress().getEntity()).trim());
        tempVar.setport(((String) getPort().getEntity()).trim());
        tempVar.setportal("0"); //$NON-NLS-1$
        tempVar.setstorage_type(StorageType.ISCSI);
        tempVar.setuser_name((Boolean) getUseUserAuth().getEntity() ? (String) getUserName().getEntity() : ""); //$NON-NLS-1$
        tempVar.setpassword((Boolean) getUseUserAuth().getEntity() ? (String) getPassword().getEntity() : ""); //$NON-NLS-1$
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
        Frontend.RunQuery(VdcQueryType.DiscoverSendTargets, parameters, asyncQuery);
    }

    private void postDiscoverTargetsInternal(ArrayList<StorageServerConnections> items)
    {
        ArrayList<SanTargetModel> newItems = new ArrayList<SanTargetModel>();

        for (StorageServerConnections a : items)
        {
            SanTargetModel tempVar = new SanTargetModel();
            tempVar.setAddress(a.getconnection());
            tempVar.setPort(a.getport());
            tempVar.setName(a.getiqn());
            tempVar.setLuns(new ObservableCollection<LunModel>());
            SanTargetModel model = tempVar;
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

        if ((Boolean) getUseUserAuth().getEntity())
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
        getUserName().setIsChangable((Boolean) getUseUserAuth().getEntity());

        getPassword().setIsValid(true);
        getPassword().setIsChangable((Boolean) getUseUserAuth().getEntity());
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getUpdateCommand())
        {
            update();
        }
        else if (command == getLoginAllCommand())
        {
            loginAll();
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

    protected void updateLoginAllAvailability()
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

        getLoginAllCommand().setIsExecutionAllowed(allow);
    }

    protected void isAllLunsSelectedChanged()
    {
    }
}
