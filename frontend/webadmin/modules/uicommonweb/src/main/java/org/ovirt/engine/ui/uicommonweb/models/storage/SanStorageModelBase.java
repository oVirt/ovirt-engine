package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

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
            OnPropertyChanged(new PropertyChangedEventArgs("ProposeDiscoverTargets"));
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
            IsAllLunsSelectedChanged();
            OnPropertyChanged(new PropertyChangedEventArgs("IsAllLunsSelected"));
        }
    }

    public boolean loginAllInProgress;
    public SanTargetModel sanTargetModel;
    private ArrayList<SanTargetModel> targetsToConnect;

    protected SanStorageModelBase()
    {
        setUpdateCommand(new UICommand("Update", this));
        UICommand tempVar = new UICommand("LoginAll", this);
        tempVar.setIsExecutionAllowed(false);
        setLoginAllCommand(tempVar);
        setDiscoverTargetsCommand(new UICommand("DiscoverTargets", this));

        setAddress(new EntityModel());
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setEntity("3260");
        setPort(tempVar2);
        setUserName(new EntityModel());
        setPassword(new EntityModel());
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setEntity(false);
        setUseUserAuth(tempVar3);
        getUseUserAuth().getEntityChangedEvent().addListener(this);

        UpdateUserAuthFields();
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(SanTargetModel.LoggedInEventDefinition))
        {
            SanTargetModel_LoggedIn(sender, args);
        }
        else if (ev.equals(EntityChangedEventDefinition))
        {
            UseUserAuth_EntityChanged(sender, args);
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
            sanStorageModel.getContainer().StopProgress();
            sanStorageModel.UpdateInternal();
        }
    }

    private void connectTargets() {

        VDS host = (VDS) getContainer().getHost().getSelectedItem();
        if (host == null)
        {
            return;
        }

        java.util.ArrayList<VdcActionType> actionTypes = new java.util.ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> paramerters = new ArrayList<VdcActionParametersBase>();
        ArrayList<IFrontendActionAsyncCallback> callbacks = new ArrayList<IFrontendActionAsyncCallback>();

        IFrontendActionAsyncCallback loginCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {
                SanStorageModelBase sanStorageModel = (SanStorageModelBase) result.getState();
                sanStorageModel.postLogin(result);
            }
        };

        for (int i = 0; i < targetsToConnect.size(); i++) {
            SanTargetModel model = targetsToConnect.get(i);
            storage_server_connections connection = new storage_server_connections();
            connection.setportal("0");
            connection.setstorage_type(StorageType.ISCSI);
            connection.setuser_name((Boolean) getUseUserAuth().getEntity() ? (String) getUserName().getEntity() : "");
            connection.setpassword((Boolean) getUseUserAuth().getEntity() ? (String) getPassword().getEntity() : "");
            connection.setiqn(model.getName());
            connection.setconnection(model.getAddress());
            connection.setport(String.valueOf(model.getPort()));

            actionTypes.add(VdcActionType.ConnectStorageToVds);
            paramerters.add(new StorageServerConnectionParametersBase(connection, host.getvds_id()));
            callbacks.add(loginCallback);
        }

        getContainer().StartProgress(null);

        Frontend.RunMultipleActions(actionTypes, paramerters, callbacks, null, this);
    }

    private void SanTargetModel_LoggedIn(Object sender, EventArgs args)
    {
        SanTargetModel model = (SanTargetModel) sender;
        targetsToConnect = new ArrayList<SanTargetModel>();
        targetsToConnect.add(model);
        connectTargets();
    }

    private void LoginAll()
    {
        // Cast to list of SanTargetModel because we get call
        // to this method only from target/LUNs mode.
        java.util.List<SanTargetModel> items = (java.util.List<SanTargetModel>) getItems();
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

    private void DiscoverTargets()
    {
        if (getContainer().getProgress() != null)
        {
            return;
        }

        if (!ValidateDiscoverTargetFields())
        {
            return;
        }

        VDS host = (VDS) getContainer().getHost().getSelectedItem();

        storage_server_connections tempVar = new storage_server_connections();
        tempVar.setconnection(((String) getAddress().getEntity()).trim());
        tempVar.setport(((String) getPort().getEntity()).trim());
        tempVar.setportal("0");
        tempVar.setstorage_type(StorageType.ISCSI);
        tempVar.setuser_name((Boolean) getUseUserAuth().getEntity() ? (String) getUserName().getEntity() : "");
        tempVar.setpassword((Boolean) getUseUserAuth().getEntity() ? (String) getPassword().getEntity() : "");
        DiscoverSendTargetsQueryParameters parameters =
                new DiscoverSendTargetsQueryParameters(host.getvds_id(), tempVar);

        setMessage(null);
        getContainer().StartProgress(null);

        Frontend.RunQuery(VdcQueryType.DiscoverSendTargets, parameters, new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        SanStorageModelBase model = (SanStorageModelBase) target;
                        Object result = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        model.PostDiscoverTargetsInternal(result != null ? (java.util.ArrayList<storage_server_connections>) result
                                : new java.util.ArrayList<storage_server_connections>());

                    }
                },
                true));
    }

    private void PostDiscoverTargetsInternal(java.util.ArrayList<storage_server_connections> items)
    {
        java.util.ArrayList<SanTargetModel> newItems = new java.util.ArrayList<SanTargetModel>();

        for (storage_server_connections a : items)
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
            setMessage("No new devices were found. This may be due to either: incorrect multipath configuration on the Host or wrong address of the iscsi target or a failure to authenticate on the target device. Please consult your Storage Administrator.");
        }

        PostDiscoverTargets(newItems);
    }

    protected void PostDiscoverTargets(java.util.ArrayList<SanTargetModel> newItems)
    {
    }

    private boolean ValidateDiscoverTargetFields()
    {
        getContainer().getHost().ValidateSelectedItem(new NotEmptyValidation[] { new NotEmptyValidation() });

        getAddress().ValidateEntity(new IValidation[] { new NotEmptyValidation() });

        IntegerValidation tempVar = new IntegerValidation();
        tempVar.setMinimum(0);
        tempVar.setMaximum(65535);
        getPort().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

        if ((Boolean) getUseUserAuth().getEntity())
        {
            getUserName().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
            getPassword().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
        }

        return getContainer().getHost().getIsValid() && getAddress().getIsValid() && getPort().getIsValid()
                && getUserName().getIsValid() && getPassword().getIsValid();
    }

    @Override
    public boolean Validate()
    {
        return true;
    }

    private void UseUserAuth_EntityChanged(Object sender, EventArgs args)
    {
        UpdateUserAuthFields();
    }

    private void UpdateUserAuthFields()
    {
        getUserName().setIsValid(true);
        getUserName().setIsChangable((Boolean) getUseUserAuth().getEntity());

        getPassword().setIsValid(true);
        getPassword().setIsChangable((Boolean) getUseUserAuth().getEntity());
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getUpdateCommand())
        {
            Update();
        }
        else if (command == getLoginAllCommand())
        {
            LoginAll();
        }
        else if (command == getDiscoverTargetsCommand())
        {
            DiscoverTargets();
        }
    }

    protected void Update()
    {
        UpdateInternal();
        setIsValid(true);
    }

    protected void UpdateInternal()
    {
    }

    protected void UpdateLoginAllAvailability()
    {
        java.util.List<SanTargetModel> items = (java.util.List<SanTargetModel>) getItems();

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

    protected void IsAllLunsSelectedChanged()
    {
    }
}
