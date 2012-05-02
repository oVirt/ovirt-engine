package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("unused")
public class TemplateInterfaceListModel extends SearchableListModel
{

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    // TODO: Check if we really need the following property.
    private VmTemplate getEntityStronglyTyped()
    {
        Object tempVar = getEntity();
        return (VmTemplate) ((tempVar instanceof VmTemplate) ? tempVar : null);
    }

    public TemplateInterfaceListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHashName("network_interfaces"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        UpdateActionAvailability();
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        getSearchCommand().Execute();
        UpdateActionAvailability();
    }

    @Override
    public void Search()
    {
        if (getEntityStronglyTyped() != null)
        {
            super.Search();
        }
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.SyncSearch(VdcQueryType.GetTemplateInterfacesByTemplateId,
                new GetVmTemplateParameters(getEntityStronglyTyped().getId()));
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetTemplateInterfacesByTemplateId,
                new GetVmTemplateParameters(getEntityStronglyTyped().getId())));
        setItems(getAsyncResult().getData());
    }

    private void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        VmInterfaceModel model = new VmInterfaceModel();
        model.getMAC().setIsAvailable(false);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newNetworkInterfaceTitle());

        model.setIsNew(true);

        AsyncDataProvider.GetClusterNetworkList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        TemplateInterfaceListModel vmInterfaceListModel = (TemplateInterfaceListModel) target;
                        ArrayList<network> network_list =
                                returnValue != null ? (ArrayList<network>) returnValue
                                        : new ArrayList<network>();
                        vmInterfaceListModel.PostGetClusterNetworkList_New(network_list);

                    }
                }), getEntityStronglyTyped().getvds_group_id());
    }

    public void PostGetClusterNetworkList_New(ArrayList<network> network_list)
    {
        ArrayList<network> networks = new ArrayList<network>();
        for (network a : network_list)
        {
            if (a.getStatus() == NetworkStatus.Operational)
            {
                networks.add(a);
            }
        }

        ArrayList<VmNetworkInterface> nics = Linq.<VmNetworkInterface> Cast(getItems());
        int nicCount = nics.size();
        String newNicName = DataProvider.GetNewNicName(nics);

        VmInterfaceModel model = (VmInterfaceModel) getWindow();
        model.getNetwork().setItems(networks);
        model.getNetwork().setSelectedItem(networks.size() > 0 ? networks.get(0) : null);
        model.getNicType().setItems(DataProvider.GetNicTypeList(getEntityStronglyTyped().getos(), false));
        model.getNicType().setSelectedItem(DataProvider.GetDefaultNicType(getEntityStronglyTyped().getos()));
        model.getName().setEntity(newNicName);
        model.getMAC().setIsAvailable(false);

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void Edit()
    {
        if (getWindow() != null)
        {
            return;
        }

        VmInterfaceModel model = new VmInterfaceModel();
        model.getMAC().setIsAvailable(false);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editNetworkInterfaceTitle());

        AsyncDataProvider.GetClusterNetworkList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        TemplateInterfaceListModel vmInterfaceListModel = (TemplateInterfaceListModel) target;
                        ArrayList<network> network_list =
                                returnValue != null ? (ArrayList<network>) returnValue
                                        : new ArrayList<network>();
                        vmInterfaceListModel.PostGetClusterNetworkList_Edit(network_list);

                    }
                }), getEntityStronglyTyped().getvds_group_id());
    }

    public void PostGetClusterNetworkList_Edit(ArrayList<network> network_list)
    {
        VmNetworkInterface nic = (VmNetworkInterface) getSelectedItem();
        int nicCount = Linq.<VmNetworkInterface> Cast(getItems()).size();
        ArrayList<network> networks = new ArrayList<network>();
        for (network a : network_list)
        {
            if (a.getStatus() == NetworkStatus.Operational)
            {
                networks.add(a);
            }
        }

        VmInterfaceModel model = (VmInterfaceModel) getWindow();
        model.getNetwork().setItems(networks);
        network network = null;
        for (network a : networks)
        {
            if (StringHelper.stringsEqual(a.getname(), nic.getNetworkName()))
            {
                network = a;
                break;
            }
        }
        model.getNetwork().setSelectedItem(network);

        Integer selectedNicType = nic.getType();
        ArrayList<VmInterfaceType> nicTypes =
                DataProvider.GetNicTypeList(getEntityStronglyTyped().getos(),
                        VmInterfaceType.forValue(selectedNicType) == VmInterfaceType.rtl8139_pv);
        model.getNicType().setItems(nicTypes);

        if (selectedNicType == null || !nicTypes.contains(VmInterfaceType.forValue(selectedNicType)))
        {
            selectedNicType = DataProvider.GetDefaultNicType(getEntityStronglyTyped().getos()).getValue();
        }

        model.getNicType().setSelectedItem(VmInterfaceType.forValue(selectedNicType));

        model.getName().setEntity(nic.getName());
        model.getMAC().setIsAvailable(false);

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnSave()
    {
        VmInterfaceModel model = (VmInterfaceModel) getWindow();
        VmNetworkInterface nic =
                model.getIsNew() ? new VmNetworkInterface() : (VmNetworkInterface) Cloner.clone(getSelectedItem());

        if (!model.Validate())
        {
            return;
        }

        // Save changes.
        nic.setName((String) model.getName().getEntity());
        nic.setNetworkName(((network) model.getNetwork().getSelectedItem()).getname());
        if (model.getNicType().getSelectedItem() == null)
        {
            nic.setType(null);
        }
        else
        {
            nic.setType(((VmInterfaceType) model.getNicType().getSelectedItem()).getValue());
        }
        nic.setMacAddress(model.getMAC().getIsChangable() ? (model.getMAC().getEntity() == null ? null
                : ((String) (model.getMAC().getEntity())).toLowerCase()) : model.getIsNew() ? "" : nic.getMacAddress()); //$NON-NLS-1$

        if (model.getIsNew())
        {
            Frontend.RunMultipleAction(VdcActionType.AddVmTemplateInterface,
                    new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] {new AddVmTemplateInterfaceParameters(getEntityStronglyTyped().getId(),
                        nic)})),
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {

                            Cancel();

                        }
                    },
                    null);
        }
        else
        {
            Frontend.RunMultipleAction(VdcActionType.UpdateVmTemplateInterface,
                    new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { new AddVmTemplateInterfaceParameters(getEntityStronglyTyped().getId(),
                            nic) })),
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {

                            Cancel();

                        }
                    },
                    null);
        }
    }

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeNetworkInterfacesTitle());
        model.setHashName("remove_network_interface_tmps"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().networkInterfacesMsg());

        ArrayList<String> items = new ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VmNetworkInterface a = (VmNetworkInterface) item;
            items.add(a.getName());
        }
        model.setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VmNetworkInterface a = (VmNetworkInterface) item;
            list.add(new RemoveVmTemplateInterfaceParameters(getEntityStronglyTyped().getId(), a.getId()));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVmTemplateInterface, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    private void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
    {
        getEditCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1
                && getSelectedItem() != null);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            New();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
    }

    @Override
    protected String getListName() {
        return "TemplateInterfaceListModel"; //$NON-NLS-1$
    }
}
