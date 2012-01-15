package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.AddBondParameters;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.action.UpdateNetworkToVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.ProvidePropertyChangedEvent;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class HostInterfaceListModel extends SearchableListModel
{

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateEditManagementNetworkCommand;

    public UICommand getEditManagementNetworkCommand()
    {
        return privateEditManagementNetworkCommand;
    }

    private void setEditManagementNetworkCommand(UICommand value)
    {
        privateEditManagementNetworkCommand = value;
    }

    private UICommand privateBondCommand;

    public UICommand getBondCommand()
    {
        return privateBondCommand;
    }

    private void setBondCommand(UICommand value)
    {
        privateBondCommand = value;
    }

    private UICommand privateDetachCommand;

    public UICommand getDetachCommand()
    {
        return privateDetachCommand;
    }

    private void setDetachCommand(UICommand value)
    {
        privateDetachCommand = value;
    }

    private UICommand privateSaveNetworkConfigCommand;

    public UICommand getSaveNetworkConfigCommand()
    {
        return privateSaveNetworkConfigCommand;
    }

    private void setSaveNetworkConfigCommand(UICommand value)
    {
        privateSaveNetworkConfigCommand = value;
    }

    private java.util.ArrayList<VdsNetworkInterface> privateOriginalItems;

    public java.util.ArrayList<VdsNetworkInterface> getOriginalItems()
    {
        return privateOriginalItems;
    }

    public void setOriginalItems(java.util.ArrayList<VdsNetworkInterface> value)
    {
        privateOriginalItems = value;
    }

    @Override
    public Iterable getItems()
    {
        return super.items;
    }

    @Override
    public void setItems(Iterable value)
    {
        if (items != value)
        {
            ItemsChanging(value, items);
            items = value;
            ItemsChanged();
            getItemsChangedEvent().raise(this, EventArgs.Empty);
            OnPropertyChanged(new PropertyChangedEventArgs("Items"));
        }
    }

    @Override
    public VDS getEntity()
    {
        return (VDS) super.getEntity();
    }

    public void setEntity(VDS value)
    {
        if (super.getEntity() != null)
        {
            VDS currentItem = ((VDS) super.getEntity());
            VDS newItem = value;

            Guid currentItemId = currentItem.getvds_id().getValue();
            Guid newItemId = newItem.getvds_id().getValue();

            if (currentItemId.equals(newItemId))
            {
                return;
            }
        }

        super.setEntity(value);
    }

    private java.util.ArrayList<VdsNetworkInterface> GetSelectedItems(boolean withVlans)
    {
        java.util.ArrayList<VdsNetworkInterface> list = new java.util.ArrayList<VdsNetworkInterface>();
        if (getItems() != null)
        {
            boolean isModelSelected;
            for (Object item : getItems())
            {
                isModelSelected = false;
                HostInterfaceLineModel model = (HostInterfaceLineModel) item;
                if (model.getIsBonded())
                {
                    if (model.getIsSelected())
                    {
                        isModelSelected = true;
                        list.add(model.getInterface());
                    }
                }
                else
                {
                    for (HostInterface hostInterface : model.getInterfaces())
                    {
                        if (hostInterface.getIsSelected())
                        {
                            isModelSelected = true;
                            list.add(hostInterface.getInterface());
                        }
                    }
                }

                for (HostVLan vLan : model.getVLans())
                {
                    if (vLan.getIsSelected() || (withVlans && isModelSelected))
                    {
                        list.add(vLan.getInterface());
                    }
                }
            }
        }

        return list;
    }

    @Override
    public java.util.ArrayList<VdsNetworkInterface> getSelectedItems()
    {
        return GetSelectedItems(false);
    }

    public java.util.ArrayList<VdsNetworkInterface> getSelectedItemsWithVlans()
    {
        return GetSelectedItems(true);
    }

    private java.util.ArrayList<VdsNetworkInterface> getInterfaceItems()
    {
        java.util.ArrayList<VdsNetworkInterface> list = new java.util.ArrayList<VdsNetworkInterface>();
        if (getItems() != null)
        {
            for (Object item : getItems())
            {
                HostInterfaceLineModel model = (HostInterfaceLineModel) item;
                for (HostInterface hostInterface : model.getInterfaces())
                {
                    list.add(hostInterface.getInterface());
                }
            }
        }

        return list;
    }

    private java.util.ArrayList<VdsNetworkInterface> getAllItems()
    {
        java.util.ArrayList<VdsNetworkInterface> list = new java.util.ArrayList<VdsNetworkInterface>();
        for (Object a : getItems())
        {
            HostInterfaceLineModel item = (HostInterfaceLineModel) a;
            if (item.getIsBonded())
            {
                list.add(item.getInterface());
            }

            for (HostInterface hostInterface : item.getInterfaces())
            {
                list.add(hostInterface.getInterface());
            }

            for (HostVLan vLan : item.getVLans())
            {
                list.add(vLan.getInterface());
            }
        }

        return list;
    }

    /**
     * Gets a boolean value indicating whether a detach confirmation is needed (cuurently happens when attempting to
     * change the Management Interface's network to 'None').
     */
    // TODO: Suspect this property is obsolete.
    private boolean detachConfirmationNeeded;

    public boolean getDetachConfirmationNeeded()
    {
        return detachConfirmationNeeded;
    }

    private void setDetachConfirmationNeeded(boolean value)
    {
        if (detachConfirmationNeeded != value)
        {
            detachConfirmationNeeded = value;
            OnPropertyChanged(new PropertyChangedEventArgs("DetachConfirmationNeeded"));
        }
    }

    private Model privatecurrentModel;

    public Model getcurrentModel()
    {
        return privatecurrentModel;
    }

    public void setcurrentModel(Model value)
    {
        privatecurrentModel = value;
    }

    public HostInterfaceListModel()
    {
        setIsTimerDisabled(true);
        setTitle("Network Interfaces");

        setEditCommand(new UICommand("Edit", this));
        setEditManagementNetworkCommand(new UICommand("EditManagementNetwork", this));
        setBondCommand(new UICommand("Bond", this));
        setDetachCommand(new UICommand("Detach", this));
        setSaveNetworkConfigCommand(new UICommand("SaveNetworkConfig", this));

        UpdateActionAvailability();
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            super.Search();
        }
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        getSearchCommand().Execute();
        UpdateActionAvailability();
    }

    // protected override void OnSelectedItemChanged()
    // {
    // base.OnSelectedItemChanged();
    // UpdateActionAvailability();
    // }

    // protected override void SelectedItemsChanged()
    // {
    // base.SelectedItemsChanged();
    // UpdateActionAvailability();
    // }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("status") || e.PropertyName.equals("net_config_dirty"))
        {
            UpdateActionAvailability();
        }
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel interfaceModel = (HostInterfaceListModel) model;
                Iterable iVdcQueryableItems = (Iterable) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                java.util.ArrayList<VdsNetworkInterface> items = new java.util.ArrayList<VdsNetworkInterface>();

                java.util.Iterator networkInterfacesIterator = iVdcQueryableItems.iterator();
                while (networkInterfacesIterator.hasNext())
                {
                    items.add((VdsNetworkInterface) networkInterfacesIterator.next());
                }
                interfaceModel.UpdateItems(items);
            }
        };

        GetVdsByVdsIdParameters tempVar = new GetVdsByVdsIdParameters(getEntity().getvds_id());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetVdsInterfacesByVdsId, tempVar, _asyncQuery);

        // VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetVdsInterfacesByVdsId,
        // new GetVdsByVdsIdParameters(Entity.vds_id) { Refresh = IsQueryFirstTime });

        // if (returnValue != null && returnValue.Succeeded)
        // {
        // List<VdsNetworkInterface> items = new List<VdsNetworkInterface>();
        // foreach (IVdcQueryable item in ((List<IVdcQueryable>)returnValue.ReturnValue))
        // {
        // VdsNetworkInterface i = (VdsNetworkInterface)item;
        // items.Add(i);
        // }

        // UpdateItems(items);
        // }
        // else
        // {
        // UpdateItems(new List<VdsNetworkInterface>());
        // }
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        SyncSearch();
    }

    private void UpdateItems(Iterable<VdsNetworkInterface> source)
    {
        java.util.ArrayList<HostInterfaceLineModel> items = new java.util.ArrayList<HostInterfaceLineModel>();
        setOriginalItems((java.util.ArrayList<VdsNetworkInterface>) source);
        // Add bonded interfaces.
        for (VdsNetworkInterface nic : source)
        {
            if ((nic.getBonded() == null ? false : nic.getBonded()))
            {
                HostInterfaceLineModel model = new HostInterfaceLineModel();
                model.setInterfaces(new java.util.ArrayList<HostInterface>());
                model.setInterface(nic);
                model.setVLans(new java.util.ArrayList<HostVLan>());
                model.setIsBonded(true);
                model.setBondName(nic.getName());
                model.setAddress(nic.getAddress());
                model.setNetworkName(nic.getNetworkName());
                model.setIsManagement(nic.getIsManagement());

                items.add(model);
            }
        }

        // Find for each bond containing interfaces.
        for (HostInterfaceLineModel model : items)
        {
            if (model.getIsBonded())
            {
                for (VdsNetworkInterface nic : source)
                {
                    if (StringHelper.stringsEqual(nic.getBondName(), model.getBondName()))
                    {
                        HostInterface hi = new HostInterface();
                        hi.setInterface(nic);
                        hi.setName(nic.getName());
                        hi.setAddress(nic.getAddress());
                        hi.setMAC(nic.getMacAddress());
                        hi.setSpeed(nic.getSpeed());
                        hi.setRxRate(nic.getStatistics().getReceiveRate());
                        hi.setRxDrop(nic.getStatistics().getReceiveDropRate());
                        hi.setTxRate(nic.getStatistics().getTransmitRate());
                        hi.setTxDrop(nic.getStatistics().getTransmitDropRate());
                        hi.setStatus(nic.getStatistics().getStatus());
                        hi.getPropertyChangedEvent().addListener(this);

                        model.getInterfaces().add(hi);
                    }
                }
            }
        }

        // Add not bonded interfaces with no vlan.
        for (VdsNetworkInterface nic : source)
        {
            if (!(nic.getBonded() == null ? false : nic.getBonded()) && StringHelper.isNullOrEmpty(nic.getBondName())
                    && nic.getVlanId() == null)
            {
                HostInterfaceLineModel model = new HostInterfaceLineModel();
                model.setInterfaces(new java.util.ArrayList<HostInterface>());
                model.setVLans(new java.util.ArrayList<HostVLan>());
                model.setNetworkName(nic.getNetworkName());
                model.setIsManagement(nic.getIsManagement());

                // There is only one interface.
                HostInterface hi = new HostInterface();
                hi.setInterface(nic);
                hi.setName(nic.getName());
                hi.setAddress(nic.getAddress());
                hi.setMAC(nic.getMacAddress());
                hi.setSpeed(nic.getSpeed());
                hi.setRxRate(nic.getStatistics().getReceiveRate());
                hi.setRxDrop(nic.getStatistics().getReceiveDropRate());
                hi.setTxRate(nic.getStatistics().getTransmitRate());
                hi.setTxDrop(nic.getStatistics().getTransmitDropRate());
                hi.setStatus(nic.getStatistics().getStatus());
                hi.getPropertyChangedEvent().addListener(this);

                model.getInterfaces().add(hi);

                items.add(model);
            }
        }

        // Find vlans.
        for (HostInterfaceLineModel model : items)
        {
            String nicName = model.getIsBonded() ? model.getBondName() : model.getInterfaces().get(0).getName();

            for (VdsNetworkInterface nic : source)
            {
                if (nic.getVlanId() != null
                        && StringHelper.stringsEqual(StringFormat.format("%1$s.%2$s", nicName, nic.getVlanId()),
                                nic.getName()))
                {
                    HostVLan hv = new HostVLan();
                    hv.setInterface(nic);
                    hv.setName(nic.getName());
                    hv.setNetworkName(nic.getNetworkName());
                    hv.setAddress(nic.getAddress());
                    hv.getPropertyChangedEvent().addListener(this);

                    model.getVLans().add(hv);
                }
            }

            java.util.ArrayList<HostVLan> list = model.getVLans();
            Collections.sort(list, new HostVLanByNameComparer());
        }

        setItems(items);
        UpdateActionAvailability();
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(ProvidePropertyChangedEvent.Definition))
        {
            Model_PropertyChanged(sender, (PropertyChangedEventArgs) args);
        }
        else if (sender instanceof Model && StringHelper.stringsEqual(((Model) sender).getTitle(), "InterfaceList"))
        {
            HostManagementNetworkModel managementNetworkModel = ((HostManagementNetworkModel) getWindow());
            VdsNetworkInterface vdsNetworkInterface =
                    (VdsNetworkInterface) managementNetworkModel.getInterface().getSelectedItem();
            // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to
            // null-value logic:
            if (vdsNetworkInterface.getBonded() != null && vdsNetworkInterface.getBonded().equals(true))
            {
                managementNetworkModel.getBondingOptions().setIsChangable(true);
            }
            else
            {
                managementNetworkModel.getBondingOptions().setIsChangable(false);
            }
        }
    }

    private void Model_PropertyChanged(Object sender, PropertyChangedEventArgs args)
    {
        if (!args.PropertyName.equals("IsSelected"))
        {
            return;
        }

        if (sender instanceof HostInterfaceLineModel)
        {
            HostInterfaceLineModel model = (HostInterfaceLineModel) sender;
            for (HostInterface hostInterface : model.getInterfaces())
            {
                hostInterface.getPropertyChangedEvent().removeListener(this);
                hostInterface.setIsSelected(model.getIsSelected());
                hostInterface.getPropertyChangedEvent().addListener(this);
            }

            for (HostVLan vLan : model.getVLans())
            {
                vLan.getPropertyChangedEvent().removeListener(this);
                vLan.setIsSelected(false);
                vLan.getPropertyChangedEvent().addListener(this);
            }

            if (model.getIsSelected())
            {
                if (model.getIsBonded())
                {
                    setSelectedItem(model.getInterface());
                }
                else
                {
                    setSelectedItem(model.getInterfaces().get(0).getInterface());
                }
            }
        }
        else if (sender instanceof HostInterface)
        {
            HostInterface model = (HostInterface) sender;
            Object selectItem = null;

            if (model.getIsSelected())
            {
                selectItem = model.getInterface();
            }

            for (Object a : getItems())
            {
                HostInterfaceLineModel item = (HostInterfaceLineModel) a;
                boolean found = false;

                for (HostInterface hostInterface : item.getInterfaces())
                {
                    if (hostInterface == model)
                    {
                        item.getPropertyChangedEvent().removeListener(this);
                        item.setIsSelected(model.getIsSelected());
                        item.getPropertyChangedEvent().addListener(this);

                        if (item.getIsBonded() && item.getIsSelected())
                        {
                            selectItem = item.getInterface();
                        }

                        for (HostVLan vLan : item.getVLans())
                        {
                            vLan.getPropertyChangedEvent().removeListener(this);
                            vLan.setIsSelected(false);
                            vLan.getPropertyChangedEvent().addListener(this);
                        }

                        found = true;

                        break;
                    }
                }

                if (found)
                {
                    for (HostInterface hostInterface : item.getInterfaces())
                    {
                        hostInterface.getPropertyChangedEvent().removeListener(this);
                        hostInterface.setIsSelected(model.getIsSelected());
                        hostInterface.getPropertyChangedEvent().addListener(this);
                    }
                }
            }

            if (selectItem != null)
            {
                setSelectedItem(selectItem);
            }
        }
        else if (sender instanceof HostVLan)
        {
            HostVLan model = (HostVLan) sender;

            for (Object a : getItems())
            {
                HostInterfaceLineModel item = (HostInterfaceLineModel) a;
                for (HostVLan vLan : item.getVLans())
                {
                    if (vLan == model)
                    {
                        for (HostInterface hostInterface : item.getInterfaces())
                        {
                            hostInterface.getPropertyChangedEvent().removeListener(this);
                            hostInterface.setIsSelected(false);
                            hostInterface.getPropertyChangedEvent().addListener(this);
                        }

                        item.getPropertyChangedEvent().removeListener(this);
                        item.setIsSelected(false);
                        item.getPropertyChangedEvent().addListener(this);

                        break;
                    }
                }
            }

            if (model.getIsSelected())
            {
                setSelectedItem(model.getInterface());
            }
        }

        if (getSelectedItems().isEmpty())
        {
            setSelectedItem(null);
        }
        else
        {
            // Check whether the SelectedItem is still a one from SelectedItems. If not, choose the first one.
            boolean found = false;
            for (VdsNetworkInterface item : getSelectedItems())
            {
                if (item == getSelectedItem())
                {
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                setSelectedItem(Linq.FirstOrDefault(getSelectedItems()));
            }
        }

        UpdateActionAvailability();
    }

    private java.util.ArrayList<String> GetSelectedNicsNetworks(RefObject<Boolean> isVlanSelected,
            RefObject<Boolean> isManagementSelected)
    {
        java.util.ArrayList<VdsNetworkInterface> selectedItems = getSelectedItemsWithVlans();
        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        isVlanSelected.argvalue = false;
        isManagementSelected.argvalue = false;
        for (VdsNetworkInterface nic : selectedItems)
        {
            if (!StringHelper.isNullOrEmpty(nic.getNetworkName()))
            {
                if (nic.getIsManagement())
                {
                    isManagementSelected.argvalue = true;
                }
                list.add(nic.getNetworkName());
                if (!isVlanSelected.argvalue && nic.getVlanId() != null)
                {
                    isVlanSelected.argvalue = true;
                }
            }
        }
        return list;
    }

    public void Edit()
    {

        if (getWindow() != null)
        {
            return;
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                java.util.ArrayList<network> networksByCluster = (java.util.ArrayList<network>) ReturnValue;
                VdsNetworkInterface item = (VdsNetworkInterface) hostInterfaceListModel.getSelectedItem();
                java.util.ArrayList<network> networksToAdd = new java.util.ArrayList<network>();
                network selectedNetwork = null;
                if (item.getVlanId() != null)
                {
                    for (network network : networksByCluster)
                    {
                        if (StringHelper.stringsEqual(network.getname(), item.getNetworkName()))
                        {
                            networksToAdd.add(network);
                            if (selectedNetwork == null)
                            {
                                selectedNetwork = network;
                            }
                        }
                    }
                }
                else
                {
                    // creating dictionary of networks by name
                    java.util.HashMap<String, network> networkDictionary = new java.util.HashMap<String, network>();
                    for (network network : networksByCluster)
                    {
                        networkDictionary.put(network.getname(), network);
                    }
                    // creating list of attached networks.
                    java.util.ArrayList<network> attachedNetworks = new java.util.ArrayList<network>();
                    for (VdsNetworkInterface nic : hostInterfaceListModel.getAllItems())
                    {
                        if (nic.getNetworkName() != null && networkDictionary.containsKey(nic.getNetworkName()))
                        {
                            attachedNetworks.add(networkDictionary.get(nic.getNetworkName()));
                        }
                    }

                    java.util.ArrayList<network> unAttachedNetworks = Linq.Except(networksByCluster, attachedNetworks);

                    // adding selected network names to list.
                    boolean isVlanSelected = false;
                    boolean isManagementSelected = false;

                    java.util.ArrayList<VdsNetworkInterface> selectedItems =
                            hostInterfaceListModel.getSelectedItemsWithVlans();
                    java.util.ArrayList<String> selectedNicsNetworks = new java.util.ArrayList<String>();
                    for (VdsNetworkInterface nic : selectedItems)
                    {
                        if (!StringHelper.isNullOrEmpty(nic.getNetworkName()))
                        {
                            if (nic.getIsManagement())
                            {
                                isManagementSelected = true;
                            }
                            selectedNicsNetworks.add(nic.getNetworkName());
                            if (!isVlanSelected && nic.getVlanId() != null)
                            {
                                isVlanSelected = true;
                            }
                        }
                    }

                    for (String selectedNetworkName : selectedNicsNetworks)
                    {
                        if (networkDictionary.containsKey(selectedNetworkName))
                        {
                            network network = networkDictionary.get(selectedNetworkName);
                            networksToAdd.add(network);
                            attachedNetworks.remove(network);

                            if (selectedNetwork == null)
                            {
                                selectedNetwork = network;
                            }
                        }
                    }

                    if (!isManagementSelected || isVlanSelected)
                    {
                        for (network unAttachedNetwork : unAttachedNetworks)
                        {
                            if (isVlanSelected)
                            {
                                if (unAttachedNetwork.getvlan_id() != null)
                                {
                                    networksToAdd.add(unAttachedNetwork);
                                }
                            }
                            else
                            {
                                networksToAdd.add(unAttachedNetwork);
                            }
                        }
                    }
                }
                Collections.sort(networksToAdd, new Linq.NetworkByNameComparer());
                // Add a 'none' option to networks.
                if (!StringHelper.isNullOrEmpty(item.getNetworkName()))
                {
                    network tempVar = new network(null);
                    tempVar.setId(NGuid.Empty);
                    tempVar.setname("None");
                    networksToAdd.add(0, tempVar);
                }

                HostInterfaceModel hostInterfaceModel = new HostInterfaceModel();
                hostInterfaceListModel.setWindow(hostInterfaceModel);
                hostInterfaceModel.setEntity(item.getName());
                hostInterfaceModel.setTitle("Edit Network Interface");
                hostInterfaceModel.setHashName("edit_network_interface_hosts");

                hostInterfaceModel.setNetworks(hostInterfaceListModel.getSelectedItemsWithVlans());

                hostInterfaceModel.setNoneBootProtocolAvailable(!item.getIsManagement());
                hostInterfaceModel.setBootProtocol(!hostInterfaceModel.getNoneBootProtocolAvailable()
                        && item.getBootProtocol() == NetworkBootProtocol.None ? NetworkBootProtocol.Dhcp
                        : item.getBootProtocol());

                hostInterfaceModel.getAddress().setEntity(item.getAddress());
                hostInterfaceModel.getSubnet().setEntity(item.getSubnet());

                hostInterfaceModel.getNetwork().setItems(networksToAdd);
                hostInterfaceModel.getName().setEntity(item.getName());

                hostInterfaceModel.getBondingOptions().setIsAvailable(false);

                // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to
                // null-value logic:
                if (item.getBonded() != null && item.getBonded().equals(true))
                {
                    hostInterfaceModel.getBondingOptions().setIsAvailable(true);
                    java.util.Map.Entry<String, EntityModel> defaultItem = null;
                    RefObject<java.util.Map.Entry<String, EntityModel>> tempRef_defaultItem =
                            new RefObject<java.util.Map.Entry<String, EntityModel>>(defaultItem);
                    java.util.ArrayList<java.util.Map.Entry<String, EntityModel>> list =
                            DataProvider.GetBondingOptionList(tempRef_defaultItem);
                    defaultItem = tempRef_defaultItem.argvalue;
                    java.util.Map.Entry<String, EntityModel> selectBondingOpt =
                            new KeyValuePairCompat<String, EntityModel>();
                    boolean containsSelectBondingOpt = false;
                    hostInterfaceModel.getBondingOptions().setItems(list);
                    for (int i = 0; i < list.size(); i++)
                    {
                        if (StringHelper.stringsEqual(list.get(i).getKey(), item.getBondOptions()))
                        {
                            selectBondingOpt = list.get(i);
                            containsSelectBondingOpt = true;
                            break;
                        }
                    }
                    if (containsSelectBondingOpt == false)
                    {
                        if (StringHelper.stringsEqual(item.getBondOptions(), DataProvider.GetDefaultBondingOption()))
                        {
                            selectBondingOpt = defaultItem;
                        }
                        else
                        {
                            selectBondingOpt = list.get(list.size() - 1);
                            EntityModel entityModel = selectBondingOpt.getValue();
                            entityModel.setEntity(item.getBondOptions());
                        }
                    }
                    hostInterfaceModel.getBondingOptions().setSelectedItem(selectBondingOpt);
                }
                if (selectedNetwork == null && networksToAdd.size() > 0)
                {
                    selectedNetwork = networksToAdd.get(0);
                }
                hostInterfaceModel.getNetwork().setSelectedItem(selectedNetwork);

                hostInterfaceModel.getCheckConnectivity()
                        .setIsAvailable(!StringHelper.isNullOrEmpty(item.getNetworkName()) && item.getIsManagement());
                hostInterfaceModel.getCheckConnectivity()
                        .setIsChangable(!StringHelper.isNullOrEmpty(item.getNetworkName()) && item.getIsManagement());
                hostInterfaceModel.getCheckConnectivity().setEntity(item.getIsManagement());

                if (networksToAdd.isEmpty())
                {
                    hostInterfaceModel.setMessage("There are no networks available. Please add additional networks.");

                    UICommand tempVar2 = new UICommand("Cancel", hostInterfaceListModel);
                    tempVar2.setTitle("Close");
                    tempVar2.setIsDefault(true);
                    tempVar2.setIsCancel(true);
                    hostInterfaceModel.getCommands().add(tempVar2);
                }
                else
                {
                    UICommand tempVar3 = new UICommand("OnSave", hostInterfaceListModel);
                    tempVar3.setTitle("OK");
                    tempVar3.setIsDefault(true);
                    hostInterfaceModel.getCommands().add(tempVar3);
                    UICommand tempVar4 = new UICommand("Cancel", hostInterfaceListModel);
                    tempVar4.setTitle("Cancel");
                    tempVar4.setIsCancel(true);
                    hostInterfaceModel.getCommands().add(tempVar4);
                }

            }
        };
        AsyncDataProvider.GetClusterNetworkList(_asyncQuery, getEntity().getvds_group_id());
    }

    public void EditManagementNetwork()
    {
        if (getWindow() != null)
        {
            return;
        }

        HostManagementNetworkModel managementNicModel = new HostManagementNetworkModel();
        setWindow(managementNicModel);
        managementNicModel.setTitle("Edit Management Network");
        managementNicModel.setHashName("edit_management_network");

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                java.util.ArrayList<network> clusterNetworks = (java.util.ArrayList<network>) ReturnValue;
                VdsNetworkInterface item = (VdsNetworkInterface) hostInterfaceListModel.getSelectedItem();
                HostManagementNetworkModel managementModel =
                        (HostManagementNetworkModel) hostInterfaceListModel.getWindow();
                network networkToEdit = Linq.FindNetworkByName(clusterNetworks, item.getNetworkName());

                managementModel.setEntity(networkToEdit);

                managementModel.setNoneBootProtocolAvailable(!item.getIsManagement());
                managementModel.setBootProtocol(!managementModel.getNoneBootProtocolAvailable()
                        && item.getBootProtocol() == NetworkBootProtocol.None ? NetworkBootProtocol.Dhcp
                        : item.getBootProtocol());

                managementModel.getAddress().setEntity(item.getAddress());
                managementModel.getSubnet().setEntity(item.getSubnet());
                managementModel.getGateway().setEntity(item.getGateway());

                String defaultInterfaceName = null;
                RefObject<String> tempRef_defaultInterfaceName = new RefObject<String>(defaultInterfaceName);
                java.util.ArrayList<VdsNetworkInterface> interfaces =
                        DataProvider.GetInterfaceOptionsForEditNetwork(getOriginalItems(),
                                item,
                                networkToEdit,
                                getEntity().getvds_id(),
                                tempRef_defaultInterfaceName);
                defaultInterfaceName = tempRef_defaultInterfaceName.argvalue;
                managementModel.getInterface().setItems(interfaces);
                managementModel.getInterface()
                        .setSelectedItem(Linq.FindInterfaceByName(Linq.VdsNetworkInterfaceListToBase(interfaces),
                                defaultInterfaceName));
                // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to
                // null-value logic:
                if (item.getBonded() != null && item.getBonded().equals(true))
                {
                    managementModel.getInterface().setTitle("InterfaceList");
                    managementModel.getInterface().getSelectedItemChangedEvent().addListener(hostInterfaceListModel);
                }
                managementModel.getCheckConnectivity().setIsAvailable(true);
                managementModel.getCheckConnectivity().setIsChangable(true);
                managementModel.getCheckConnectivity().setEntity(item.getIsManagement()); // currently, always should be
                                                                                          // true

                managementModel.getBondingOptions().setIsAvailable(false);
                // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to
                // null-value logic:
                if (item.getBonded() != null && item.getBonded().equals(true))
                {
                    managementModel.getBondingOptions().setIsAvailable(true);
                    java.util.Map.Entry<String, EntityModel> defaultItem = null;
                    RefObject<java.util.Map.Entry<String, EntityModel>> tempRef_defaultItem =
                            new RefObject<java.util.Map.Entry<String, EntityModel>>(defaultItem);
                    java.util.ArrayList<java.util.Map.Entry<String, EntityModel>> list =
                            DataProvider.GetBondingOptionList(tempRef_defaultItem);
                    defaultItem = tempRef_defaultItem.argvalue;
                    java.util.Map.Entry<String, EntityModel> selectBondingOpt =
                            new KeyValuePairCompat<String, EntityModel>();
                    boolean containsSelectBondingOpt = false;
                    managementModel.getBondingOptions().setItems(list);
                    for (int i = 0; i < list.size(); i++)
                    {
                        if (StringHelper.stringsEqual(list.get(i).getKey(), item.getBondOptions()))
                        {
                            selectBondingOpt = list.get(i);
                            containsSelectBondingOpt = true;
                            break;
                        }
                    }
                    if (containsSelectBondingOpt == false)
                    {
                        if (StringHelper.stringsEqual(item.getBondOptions(), DataProvider.GetDefaultBondingOption()))
                        {
                            selectBondingOpt = defaultItem;
                        }
                        else
                        {
                            selectBondingOpt = list.get(list.size() - 1);
                            EntityModel entityModel = selectBondingOpt.getValue();
                            entityModel.setEntity(item.getBondOptions());
                        }
                    }
                    managementModel.getBondingOptions().setSelectedItem(selectBondingOpt);
                }

                UICommand tempVar = new UICommand("OnEditManagementNetworkConfirmation", hostInterfaceListModel);
                tempVar.setTitle("OK");
                tempVar.setIsDefault(true);
                managementModel.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("Cancel", hostInterfaceListModel);
                tempVar2.setTitle("Cancel");
                tempVar2.setIsCancel(true);
                managementModel.getCommands().add(tempVar2);
            }
        };
        AsyncDataProvider.GetClusterNetworkList(_asyncQuery, getEntity().getvds_group_id());
    }

    public void OnEditManagementNetworkConfirmation(boolean isBond)
    {
        if (!isBond)
        {
            HostManagementNetworkModel model = (HostManagementNetworkModel) getWindow();
            if (!model.Validate())
            {
                return;
            }
            if ((Boolean) model.getCheckConnectivity().getEntity() == true)
            {
                OnEditManagementNetwork();
                return;
            }
        }
        else
        {
            HostBondInterfaceModel model = (HostBondInterfaceModel) getWindow();
            if (!model.Validate())
            {
                return;
            }
            if ((Boolean) model.getCheckConnectivity().getEntity() == true)
            {
                OnBond();
                return;
            }
        }
        ConfirmationModel confirmModel = new ConfirmationModel();
        setConfirmWindow(confirmModel);
        confirmModel.setTitle("Confirm");
        confirmModel.getLatch().setEntity(true);

        if (!isBond)
        {
            UICommand tempVar = new UICommand("OnEditManagementNetwork", this);
            tempVar.setTitle("OK");
            tempVar.setIsDefault(true);
            confirmModel.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnBond", this);
            tempVar2.setTitle("OK");
            tempVar2.setIsDefault(true);
            confirmModel.getCommands().add(tempVar2);
        }
        UICommand tempVar3 = new UICommand("CancelConfirm", this);
        tempVar3.setTitle("Cancel");
        tempVar3.setIsCancel(true);
        confirmModel.getCommands().add(tempVar3);

    }

    public void OnEditManagementNetwork()
    {
        HostManagementNetworkModel model = (HostManagementNetworkModel) getWindow();
        if (getConfirmWindow() != null)
        {
            ConfirmationModel confirmModel = (ConfirmationModel) getConfirmWindow();
            if ((Boolean) confirmModel.getLatch().getEntity() == true)
            {
                model.getCheckConnectivity().setEntity(true);
            }
        }

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        VdsNetworkInterface nic = (VdsNetworkInterface) model.getInterface().getSelectedItem();
        network network = model.getEntity();

        VdcActionType actionType = VdcActionType.UpdateNetworkToVdsInterface;
        UpdateNetworkToVdsParameters parameters =
                new UpdateNetworkToVdsParameters(getEntity().getvds_id(),
                        network,
                        new java.util.ArrayList<VdsNetworkInterface>(java.util.Arrays.asList(new VdsNetworkInterface[] { nic })));

        java.util.Map.Entry<String, EntityModel> bondingOption;
        if (model.getBondingOptions().getSelectedItem() != null)
        {
            bondingOption = (java.util.Map.Entry<String, EntityModel>) model.getBondingOptions().getSelectedItem();

            if (!bondingOption.getKey().equals("custom"))
            {
                parameters.setBondingOptions((StringHelper.isNullOrEmpty(bondingOption.getKey()) ? null
                        : bondingOption.getKey()));
            }
            else
            {
                EntityModel entityModel = bondingOption.getValue();
                if (entityModel.getEntity() != null)
                {
                    parameters.setBondingOptions(entityModel.getEntity().toString());
                }
            }
        }
        VdsNetworkInterface selectedItem = (VdsNetworkInterface) getSelectedItem();
        if (nic.getBonded() == null || nic.getBonded() == false)
        {
            parameters.setBondingOptions(null);
        }

        if (network != null)
        {
            parameters.setOldNetworkName(network.getname());
        }
        parameters.setCheckConnectivity((Boolean) model.getCheckConnectivity().getEntity());
        parameters.setBootProtocol(model.getBootProtocol());

        if (model.getIsStaticAddress())
        {
            parameters.setAddress((String) model.getAddress().getEntity());
            parameters.setSubnet((String) model.getSubnet().getEntity());
            parameters.setGateway((String) model.getGateway().getEntity());
        }

        model.StartProgress(null);
        setcurrentModel(model);

        Frontend.RunAction(actionType, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) result.getState();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded())
                        {
                            EntityModel commitChanges =
                                    ((HostManagementNetworkModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                            if ((Boolean) commitChanges.getEntity())
                            {
                                SaveNetworkConfig(hostInterfaceListModel.getEntity().getvds_id(),
                                        hostInterfaceListModel);
                            }
                            else
                            {
                                hostInterfaceListModel.getcurrentModel().StopProgress();
                                hostInterfaceListModel.Cancel();
                                hostInterfaceListModel.Search();
                            }
                        }
                        else
                        {
                            hostInterfaceListModel.getcurrentModel().StopProgress();
                        }

                    }
                },
                this);
        CancelConfirm();
    }

    private java.util.ArrayList<network> GetNetworksList(RefObject<network> selectedNetwork)
    {
        selectedNetwork.argvalue = null;
        java.util.ArrayList<network> networksByCluster =
                DataProvider.GetClusterNetworkList(getEntity().getvds_group_id());
        java.util.ArrayList<network> networksToAdd = new java.util.ArrayList<network>();
        // creating dictionary of networks by name
        java.util.HashMap<String, network> networkDictionary = new java.util.HashMap<String, network>();
        for (network network : networksByCluster)
        {
            networkDictionary.put(network.getname(), network);
        }
        // creating list of attached networks.
        java.util.ArrayList<network> attachedNetworks = new java.util.ArrayList<network>();
        for (VdsNetworkInterface nic : getAllItems())
        {
            if (nic.getNetworkName() != null && networkDictionary.containsKey(nic.getNetworkName()))
            {
                attachedNetworks.add(networkDictionary.get(nic.getNetworkName()));
            }
        }

        java.util.ArrayList<network> unAttachedNetworks = Linq.Except(networksByCluster, attachedNetworks);

        // adding selected network names to list.
        boolean isVlanSelected = false;
        boolean isManagement = false;
        RefObject<Boolean> tempRef_isVlanSelected = new RefObject<Boolean>(isVlanSelected);
        RefObject<Boolean> tempRef_isManagement = new RefObject<Boolean>(isManagement);
        java.util.ArrayList<String> selectedNicsNetworks =
                GetSelectedNicsNetworks(tempRef_isVlanSelected, tempRef_isManagement);
        isVlanSelected = tempRef_isVlanSelected.argvalue;
        isManagement = tempRef_isManagement.argvalue;

        for (String selectedNetworkName : selectedNicsNetworks)
        {
            if (networkDictionary.containsKey(selectedNetworkName))
            {
                network network = networkDictionary.get(selectedNetworkName);
                networksToAdd.add(network);
                attachedNetworks.remove(network);

                if (selectedNetwork.argvalue == null)
                {
                    selectedNetwork.argvalue = network;
                }
            }
        }

        if (!isManagement)
        {
            for (network unAttachedNetwork : unAttachedNetworks)
            {
                if (isVlanSelected)
                {
                    if (unAttachedNetwork.getvlan_id() != null)
                    {
                        networksToAdd.add(unAttachedNetwork);
                    }
                }
                else
                {
                    networksToAdd.add(unAttachedNetwork);
                }
            }
        }
        return networksToAdd;
    }

    public void Bond()
    {
        if (getWindow() != null)
        {
            return;
        }

        HostBondInterfaceModel bondModel = new HostBondInterfaceModel();
        setWindow(bondModel);
        bondModel.setTitle("Bond Network Interfaces");
        bondModel.setHashName("bond_network_interfaces");

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                java.util.ArrayList<network> networksByCluster = (java.util.ArrayList<network>) ReturnValue;
                VdsNetworkInterface item = (VdsNetworkInterface) hostInterfaceListModel.getSelectedItem();
                HostBondInterfaceModel innerBondModel = (HostBondInterfaceModel) hostInterfaceListModel.getWindow();
                network selectedNetwork = null;

                VDS host = hostInterfaceListModel.getEntity();
                // Allow change gateway if there one of the selected interfaces connected to engine network.
                boolean isAnyManagement = false;
                for (VdsNetworkInterface innerItem : hostInterfaceListModel.getSelectedItems())
                {
                    if (innerItem.getIsManagement())
                    {
                        isAnyManagement = true;
                        break;
                    }
                }

                java.util.ArrayList<network> networksToAdd = new java.util.ArrayList<network>();
                // creating dictionary of networks by name
                java.util.HashMap<String, network> networkDictionary = new java.util.HashMap<String, network>();
                for (network network : networksByCluster)
                {
                    networkDictionary.put(network.getname(), network);
                }
                // creating list of attached networks.
                java.util.ArrayList<network> attachedNetworks = new java.util.ArrayList<network>();
                for (VdsNetworkInterface nic : hostInterfaceListModel.getAllItems())
                {
                    if (nic.getNetworkName() != null && networkDictionary.containsKey(nic.getNetworkName()))
                    {
                        attachedNetworks.add(networkDictionary.get(nic.getNetworkName()));
                    }
                }

                java.util.ArrayList<network> unAttachedNetworks = Linq.Except(networksByCluster, attachedNetworks);

                // adding selected network names to list.
                boolean isVlanSelected = false;
                boolean isManagement = false;
                RefObject<Boolean> tempRef_isVlanSelected = new RefObject<Boolean>(isVlanSelected);
                RefObject<Boolean> tempRef_isManagement = new RefObject<Boolean>(isManagement);
                java.util.ArrayList<String> selectedNicsNetworks =
                        hostInterfaceListModel.GetSelectedNicsNetworks(tempRef_isVlanSelected, tempRef_isManagement);
                isVlanSelected = tempRef_isVlanSelected.argvalue;
                isManagement = tempRef_isManagement.argvalue;

                for (String selectedNetworkName : selectedNicsNetworks)
                {
                    if (networkDictionary.containsKey(selectedNetworkName))
                    {
                        network network = networkDictionary.get(selectedNetworkName);
                        networksToAdd.add(network);
                        attachedNetworks.remove(network);

                        if (selectedNetwork == null)
                        {
                            selectedNetwork = network;
                        }
                    }
                }

                if (!isManagement)
                {
                    for (network unAttachedNetwork : unAttachedNetworks)
                    {
                        if (isVlanSelected)
                        {
                            if (unAttachedNetwork.getvlan_id() != null)
                            {
                                networksToAdd.add(unAttachedNetwork);
                            }
                        }
                        else
                        {
                            networksToAdd.add(unAttachedNetwork);
                        }
                    }
                }

                innerBondModel.getNetwork().setItems(networksToAdd);

                if (selectedNetwork == null && networksToAdd.size() > 0)
                {
                    selectedNetwork = networksToAdd.get(0);
                }
                innerBondModel.getNetwork().setSelectedItem(selectedNetwork);

                // Interface bond = selectedItems.FirstOrDefault(a => a.is_bond.HasValue && a.is_bond.Value);
                VdsNetworkInterface bond = Linq.FindInterfaceByIsBond(getSelectedItems());
                if (bond != null)
                // one of the bond items is a bond itself -> don't
                // allocate a new bond name, edit the existing one:
                {
                    innerBondModel.getBond()
                            .setItems(new java.util.ArrayList<VdsNetworkInterface>(java.util.Arrays.asList(new VdsNetworkInterface[] { bond })));
                    innerBondModel.getBond().setSelectedItem(bond);
                    innerBondModel.getBond().setIsChangable(false);
                    hostInterfaceListModel.PostBond(hostInterfaceListModel,
                            innerBondModel,
                            networksToAdd,
                            isAnyManagement);
                }
                else
                {
                    AsyncQuery _asyncQuery1 = new AsyncQuery();
                    _asyncQuery1.setModel(hostInterfaceListModel);
                    _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model1, Object ReturnValue1)
                        {
                            HostInterfaceListModel innerHostInterfaceListModel = (HostInterfaceListModel) model1;
                            HostBondInterfaceModel bModel =
                                    (HostBondInterfaceModel) innerHostInterfaceListModel.getWindow();
                            List<VdsNetworkInterface> bonds =
                                    (List<VdsNetworkInterface>) ((VdcQueryReturnValue) ReturnValue1).getReturnValue();

                            bModel.getBond().setItems(bonds);
                            // ((List<Interface>)model.Bond.Options).Sort(a => a.name);
                            bModel.getBond().setSelectedItem(Linq.FirstOrDefault(bonds));
                            boolean hasManagement = false;
                            for (VdsNetworkInterface innerItem : innerHostInterfaceListModel.getSelectedItems())
                            {
                                if (innerItem.getIsManagement())
                                {
                                    hasManagement = true;
                                    break;
                                }
                            }
                            innerHostInterfaceListModel.PostBond(innerHostInterfaceListModel,
                                    bModel,
                                    bModel.getNetwork().getItems() != null ? (java.util.ArrayList<network>) bModel.getNetwork()
                                            .getItems()
                                            : new java.util.ArrayList<network>(),
                                    hasManagement);

                        }
                    };
                    Frontend.RunQuery(VdcQueryType.GetVdsFreeBondsByVdsId,
                            new GetVdsByVdsIdParameters(host.getvds_id()),
                            _asyncQuery1);
                }
            }
        };
        AsyncDataProvider.GetClusterNetworkList(_asyncQuery, getEntity().getvds_group_id());
    }

    public void PostBond(HostInterfaceListModel hostInterfaceListModel,
            HostBondInterfaceModel innerBondModel,
            java.util.ArrayList<network> networksToAdd,
            boolean isAnyManagement)
    {
        java.util.ArrayList<NetworkInterface> baseSelectedItems =
                Linq.VdsNetworkInterfaceListToBase(getSelectedItems());
        VdsNetworkInterface interfaceWithNetwork =
                (VdsNetworkInterface) Linq.FindInterfaceNetworkNameNotEmpty(baseSelectedItems);

        innerBondModel.getCheckConnectivity().setIsChangable(interfaceWithNetwork != null);
        innerBondModel.getCheckConnectivity().setIsAvailable(interfaceWithNetwork != null
                && interfaceWithNetwork.getIsManagement());

        innerBondModel.getCheckConnectivity().setEntity(interfaceWithNetwork != null
                && interfaceWithNetwork.getIsManagement());
        innerBondModel.setNoneBootProtocolAvailable(!(interfaceWithNetwork != null && interfaceWithNetwork.getIsManagement()));

        if (interfaceWithNetwork != null)
        {
            innerBondModel.setBootProtocol(!innerBondModel.getNoneBootProtocolAvailable()
                    && interfaceWithNetwork.getBootProtocol() == NetworkBootProtocol.None ? NetworkBootProtocol.Dhcp
                    : interfaceWithNetwork.getBootProtocol());
            innerBondModel.getAddress().setEntity(interfaceWithNetwork.getAddress());
            innerBondModel.getSubnet().setEntity(interfaceWithNetwork.getSubnet());
            innerBondModel.getGateway().setEntity(interfaceWithNetwork.getGateway());
        }
        else
        {
            innerBondModel.setBootProtocol(NetworkBootProtocol.Dhcp);
        }

        innerBondModel.getGateway().setIsAvailable(isAnyManagement);

        if (networksToAdd.isEmpty())
        {
            innerBondModel.setMessage("There are no networks available. Please add additional networks.");

            UICommand tempVar = new UICommand("Cancel", hostInterfaceListModel);
            tempVar.setTitle("Close");
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            innerBondModel.getCommands().add(tempVar);
        }
        else
        {
            if (interfaceWithNetwork != null && interfaceWithNetwork.getIsManagement())
            {
                UICommand tempVar2 = new UICommand("OnEditManagementNetworkConfirmation_Bond", hostInterfaceListModel);
                tempVar2.setTitle("OK");
                tempVar2.setIsDefault(true);
                innerBondModel.getCommands().add(tempVar2);
                UICommand tempVar3 = new UICommand("Cancel", hostInterfaceListModel);
                tempVar3.setTitle("Cancel");
                tempVar3.setIsCancel(true);
                innerBondModel.getCommands().add(tempVar3);
            }
            else
            {
                UICommand tempVar4 = new UICommand("OnBond", hostInterfaceListModel);
                tempVar4.setTitle("OK");
                tempVar4.setIsDefault(true);
                innerBondModel.getCommands().add(tempVar4);
                UICommand tempVar5 = new UICommand("Cancel", hostInterfaceListModel);
                tempVar5.setTitle("Cancel");
                tempVar5.setIsCancel(true);
                innerBondModel.getCommands().add(tempVar5);
            }
        }
    }

    public void OnBond()
    {
        HostBondInterfaceModel model = (HostBondInterfaceModel) getWindow();

        if (getConfirmWindow() != null)
        {
            ConfirmationModel confirmModel = (ConfirmationModel) getConfirmWindow();
            if ((Boolean) confirmModel.getLatch().getEntity() == true)
            {
                model.getCheckConnectivity().setEntity(true);
            }
            CancelConfirm();
        }

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        VDS host = getEntity();
        java.util.ArrayList<VdsNetworkInterface> selectedItems = getSelectedItems();
        network net = (network) model.getNetwork().getSelectedItem();

        // Interface interfaceWithNetwork = items.FirstOrDefault(a => !string.IsNullOrEmpty(a.network_name));
        VdsNetworkInterface interfaceWithNetwork =
                (VdsNetworkInterface) Linq.FindInterfaceNetworkNameNotEmpty(Linq.VdsNetworkInterfaceListToBase(selectedItems));

        if (interfaceWithNetwork != null)
        {
            UpdateNetworkToVdsParameters parameters =
                    new UpdateNetworkToVdsParameters(host.getvds_id(), net, selectedItems);
            parameters.setCheckConnectivity((Boolean) model.getCheckConnectivity().getEntity());
            parameters.setOldNetworkName(interfaceWithNetwork.getNetworkName());

            java.util.Map.Entry<String, EntityModel> bondingOption;
            if (model.getBondingOptions().getSelectedItem() != null)
            {
                bondingOption = (java.util.Map.Entry<String, EntityModel>) model.getBondingOptions().getSelectedItem();

                if (!bondingOption.getKey().equals("custom"))
                {
                    parameters.setBondingOptions((StringHelper.isNullOrEmpty(bondingOption.getKey()) ? null
                            : bondingOption.getKey()));
                }
                else
                {
                    EntityModel entityModel = bondingOption.getValue();
                    if (entityModel.getEntity() != null)
                    {
                        parameters.setBondingOptions(entityModel.getEntity().toString());
                    }
                }
            }

            parameters.setBootProtocol(model.getBootProtocol());
            parameters.setBondName(((VdsNetworkInterface) model.getBond().getSelectedItem()).getName());

            if (model.getIsStaticAddress())
            {
                parameters.setAddress((String) model.getAddress().getEntity());
                parameters.setSubnet((String) model.getSubnet().getEntity());
                if (interfaceWithNetwork.getIsManagement())
                {
                    parameters.setGateway((String) model.getGateway().getEntity());
                }
            }

            model.StartProgress(null);
            setcurrentModel(model);

            Frontend.RunAction(VdcActionType.UpdateNetworkToVdsInterface, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) result.getState();
                            VdcReturnValueBase returnValueBase = result.getReturnValue();
                            if (returnValueBase != null && returnValueBase.getSucceeded())
                            {
                                EntityModel commitChanges =
                                        ((HostBondInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                                if ((Boolean) commitChanges.getEntity())
                                {
                                    SaveNetworkConfig(hostInterfaceListModel.getEntity().getvds_id(),
                                            hostInterfaceListModel);
                                }
                                else
                                {
                                    hostInterfaceListModel.getcurrentModel().StopProgress();
                                    hostInterfaceListModel.Cancel();
                                    hostInterfaceListModel.Search();
                                }
                            }
                            else
                            {
                                hostInterfaceListModel.getcurrentModel().StopProgress();
                            }

                        }
                    },
                    this);
        }
        else
        {
            String[] nics = new String[selectedItems.size()];
            for (int i = 0; i < selectedItems.size(); i++)
            {
                nics[i] = selectedItems.get(i).getName();
            }
            // var parameters = new AddBondParameters(
            // host.vds_id,
            // model.Bond.ValueAs<Interface>().name,
            // net,
            // items.Select(a => a.name).ToArray())
            // {
            // BondingOptions = model.BondingOptions.ValueAs<string>(),
            // BootProtocol = model.BootProtocol
            // };
            AddBondParameters parameters =
                    new AddBondParameters(host.getvds_id(),
                            ((VdsNetworkInterface) model.getBond().getSelectedItem()).getName(),
                            net,
                            nics);
            java.util.Map.Entry<String, EntityModel> bondingOption;
            if (model.getBondingOptions().getSelectedItem() != null)
            {
                bondingOption = (java.util.Map.Entry<String, EntityModel>) model.getBondingOptions().getSelectedItem();

                if (!bondingOption.getKey().equals("custom"))
                {
                    parameters.setBondingOptions((StringHelper.isNullOrEmpty(bondingOption.getKey()) ? null
                            : bondingOption.getKey()));
                }
                else
                {
                    EntityModel entityModel = bondingOption.getValue();
                    if (entityModel.getEntity() != null)
                    {
                        parameters.setBondingOptions(entityModel.getEntity().toString());
                    }
                }
            }
            parameters.setBootProtocol(model.getBootProtocol());

            if (model.getIsStaticAddress())
            {
                parameters.setAddress((String) model.getAddress().getEntity());
                parameters.setSubnet((String) model.getSubnet().getEntity());
                parameters.setGateway((String) model.getGateway().getEntity());
            }

            model.StartProgress(null);
            setcurrentModel(model);

            Frontend.RunAction(VdcActionType.AddBond, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) result.getState();
                            VdcReturnValueBase returnValueBase = result.getReturnValue();
                            if (returnValueBase != null && returnValueBase.getSucceeded())
                            {
                                EntityModel commitChanges =
                                        ((HostBondInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                                if ((Boolean) commitChanges.getEntity())
                                {
                                    SaveNetworkConfig(hostInterfaceListModel.getEntity().getvds_id(),
                                            hostInterfaceListModel);
                                }
                                else
                                {
                                    hostInterfaceListModel.getcurrentModel().StopProgress();
                                    hostInterfaceListModel.Cancel();
                                    hostInterfaceListModel.Search();
                                }
                            }
                            else
                            {
                                hostInterfaceListModel.getcurrentModel().StopProgress();
                            }

                        }
                    },
                    this);
        }
    }

    public void Detach()
    {
        if (getWindow() != null)
        {
            return;
        }

        HostInterfaceModel model = new HostInterfaceModel();
        setWindow(model);
        model.setTitle("Detach Network Interfaces");
        model.setHashName("detach_network_interfaces");

        VdsNetworkInterface nic = (VdsNetworkInterface) getSelectedItem();
        model.getName().setEntity(nic.getName());

        UICommand tempVar = new UICommand("OnDetach", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnDetach()
    {
        HostInterfaceModel model = (HostInterfaceModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        VdsNetworkInterface nic = (VdsNetworkInterface) getSelectedItem();
        java.util.ArrayList<network> networks = DataProvider.GetClusterNetworkList(getEntity().getvds_group_id());

        network defaultNetwork = new network(null);
        defaultNetwork.setname(nic.getNetworkName());
        network tempVar = Linq.FindNetworkByName(networks, nic.getNetworkName());
        network net = (tempVar != null) ? tempVar : defaultNetwork;

        model.StartProgress(null);
        setcurrentModel(model);

        Frontend.RunAction(VdcActionType.DetachNetworkFromVdsInterface,
                new AttachNetworkToVdsParameters(getEntity().getvds_id(), net, nic),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) result.getState();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded())
                        {
                            EntityModel commitChanges =
                                    ((HostInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                            if ((Boolean) commitChanges.getEntity())
                            {
                                SaveNetworkConfig(hostInterfaceListModel.getEntity().getvds_id(),
                                        hostInterfaceListModel);
                            }
                            else
                            {
                                hostInterfaceListModel.getcurrentModel().StopProgress();
                                hostInterfaceListModel.Cancel();
                                hostInterfaceListModel.Search();
                            }
                        }
                        else
                        {
                            hostInterfaceListModel.getcurrentModel().StopProgress();
                        }

                    }
                },
                this);
    }

    public void OnSave()
    {
        HostInterfaceModel model = (HostInterfaceModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        String nicName = (String) model.getEntity();
        VdsNetworkInterface nic =
                (VdsNetworkInterface) Linq.FindInterfaceByName(Linq.VdsNetworkInterfaceListToBase(getAllItems()),
                        nicName);

        if (nic == null)
        {
            Cancel();
            return;
        }

        network network = (network) model.getNetwork().getSelectedItem();

        // Save changes.
        if (network.getId().equals(NGuid.Empty))
        {
            if (nic.getIsManagement())
            {
                // We are trying to disconnect the management interface from its
                // network -> ask for the user's confirmation before doing that.
                ConfirmationModel confirmModel = new ConfirmationModel();
                setConfirmWindow(confirmModel);
                confirmModel.setTitle("Edit Management Network Interface");
                confirmModel.setHashName("edit_management_network_interface");
                confirmModel.setMessage(StringFormat.format("You are about to disconnect the Management Interface (%1$s).\\nAs a result, the Host might become unreachable.\\n\\nAre you sure you want to disconnect the Management Interface?",
                        nic.getName()));

                UICommand tempVar = new UICommand("OnConfirmManagementDetach", this);
                tempVar.setTitle("OK");
                tempVar.setIsDefault(true);
                confirmModel.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("Cancel", this);
                tempVar2.setTitle("Cancel");
                tempVar2.setIsCancel(true);
                confirmModel.getCommands().add(tempVar2);
            }
            else
            {
                if (model.getProgress() != null)
                {
                    return;
                }

                java.util.ArrayList<network> networks =
                        DataProvider.GetClusterNetworkList(getEntity().getvds_group_id());
                network defaultNetwork = new network(null);
                defaultNetwork.setname(nic.getNetworkName());
                network tempVar3 = Linq.FindNetworkByName(networks, nic.getNetworkName());
                network net = (tempVar3 != null) ? tempVar3 : defaultNetwork;

                model.StartProgress(null);
                setcurrentModel(model);

                Frontend.RunAction(VdcActionType.DetachNetworkFromVdsInterface,
                        new AttachNetworkToVdsParameters(getEntity().getvds_id(), net, nic),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult result) {

                                HostInterfaceListModel hostInterfaceListModel =
                                        (HostInterfaceListModel) result.getState();
                                VdcReturnValueBase returnValueBase = result.getReturnValue();
                                if (returnValueBase != null && returnValueBase.getSucceeded())
                                {
                                    EntityModel commitChanges =
                                            ((HostInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                                    if ((Boolean) commitChanges.getEntity())
                                    {
                                        SaveNetworkConfig(hostInterfaceListModel.getEntity().getvds_id(),
                                                hostInterfaceListModel);
                                    }
                                    else
                                    {
                                        hostInterfaceListModel.getcurrentModel().StopProgress();
                                        hostInterfaceListModel.Cancel();
                                        hostInterfaceListModel.Search();
                                    }
                                }
                                else
                                {
                                    hostInterfaceListModel.getcurrentModel().StopProgress();
                                }

                            }
                        },
                        this);
            }
        }
        else
        {
            if (model.getProgress() != null)
            {
                return;
            }

            AttachNetworkToVdsParameters parameters;
            VdcActionType actionType;
            boolean vLanAttached = false;
            boolean bondWithVlans = false;
            boolean isUpdateVlan = false;
            if (nic.getBonded() != null && nic.getBonded())
            {
                for (HostInterfaceLineModel item : (java.util.ArrayList<HostInterfaceLineModel>) getItems())
                {
                    if (item.getInterface() != null && item.getInterface().getId().getValue().equals(nic.getId()))
                    {
                        if (item.getVLans() != null && item.getVLans().size() > 0)
                        {
                            bondWithVlans = true;
                            for (HostVLan vLan : item.getVLans())
                            {
                                if (StringHelper.stringsEqual(network.getname(), vLan.getNetworkName()))
                                {
                                    vLanAttached = true;
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
            else
            {
                for (VdsNetworkInterface item : getSelectedItemsWithVlans())
                {
                    if (item.getVlanId() != null && StringHelper.stringsEqual(item.getNetworkName(), network.getname()))
                    {
                        isUpdateVlan = true;
                        break;
                    }
                }
            }
            // if the selected item is a non-attached or attached to vlan eth, or if the selected item is a bond that
            // has vlans attached to it,
            // and the selected network in the dialog is a new vlan, attach selected network.
            if (((StringHelper.isNullOrEmpty(nic.getNetworkName()) && (nic.getBonded() == null || !nic.getBonded())) && !isUpdateVlan)
                    || (bondWithVlans && (!vLanAttached && network.getvlan_id() != null)))
            {
                parameters = new AttachNetworkToVdsParameters(getEntity().getvds_id(), network, nic);
                actionType = VdcActionType.AttachNetworkToVdsInterface;
            }
            else
            {
                parameters =
                        new UpdateNetworkToVdsParameters(getEntity().getvds_id(),
                                network,
                                new java.util.ArrayList<VdsNetworkInterface>(java.util.Arrays.asList(new VdsNetworkInterface[] { nic })));
                parameters.setOldNetworkName((nic.getNetworkName() != null ? nic.getNetworkName() : network.getname()));
                parameters.setCheckConnectivity((Boolean) model.getCheckConnectivity().getEntity());

                actionType = VdcActionType.UpdateNetworkToVdsInterface;
            }
            java.util.Map.Entry<String, EntityModel> bondingOption;
            if (model.getBondingOptions().getSelectedItem() != null)
            {
                bondingOption = (java.util.Map.Entry<String, EntityModel>) model.getBondingOptions().getSelectedItem();

                if (!bondingOption.getKey().equals("custom"))
                {
                    parameters.setBondingOptions((StringHelper.isNullOrEmpty(bondingOption.getKey()) ? null
                            : bondingOption.getKey()));
                }
                else
                {
                    EntityModel entityModel = bondingOption.getValue();
                    if (entityModel.getEntity() != null)
                    {
                        parameters.setBondingOptions(entityModel.getEntity().toString());
                    }
                }
            }

            parameters.setBootProtocol(model.getBootProtocol());

            if (model.getIsStaticAddress())
            {
                parameters.setAddress((String) model.getAddress().getEntity());
                parameters.setSubnet((String) model.getSubnet().getEntity());
            }

            model.StartProgress(null);
            setcurrentModel(model);

            Frontend.RunAction(actionType, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) result.getState();
                            VdcReturnValueBase returnValueBase = result.getReturnValue();
                            if (returnValueBase != null && returnValueBase.getSucceeded())
                            {
                                EntityModel commitChanges =
                                        ((HostInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                                if ((Boolean) commitChanges.getEntity())
                                {
                                    SaveNetworkConfig(hostInterfaceListModel.getEntity().getvds_id(),
                                            hostInterfaceListModel);
                                }
                                else
                                {
                                    hostInterfaceListModel.getcurrentModel().StopProgress();
                                    hostInterfaceListModel.Cancel();
                                    hostInterfaceListModel.Search();
                                }
                            }
                            else
                            {
                                hostInterfaceListModel.getcurrentModel().StopProgress();
                            }

                        }
                    },
                    this);
        }
    }

    public void SaveNetworkConfig(Guid vdsId, HostInterfaceListModel hostInterfaceListModel)
    {
        Frontend.RunAction(VdcActionType.CommitNetworkChanges, new VdsActionParameters(vdsId),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded())
                        {
                            HostInterfaceListModel interfaceListModel = (HostInterfaceListModel) result.getState();
                            if (interfaceListModel.getcurrentModel() != null)
                            {
                                interfaceListModel.getcurrentModel().StopProgress();
                                interfaceListModel.Cancel();
                                interfaceListModel.Search();
                            }
                        }

                    }
                }, hostInterfaceListModel);
    }

    public void OnConfirmManagementDetach()
    {
        HostInterfaceModel model = (HostInterfaceModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        String nicName = (String) model.getEntity();
        VdsNetworkInterface nic =
                (VdsNetworkInterface) Linq.FindInterfaceByName(Linq.<NetworkInterface> Cast(getInterfaceItems()),
                        nicName);
        java.util.ArrayList<network> networks = DataProvider.GetClusterNetworkList(getEntity().getvds_group_id());

        network defaultNetwork = new network(null);
        defaultNetwork.setname(nic.getNetworkName());
        network tempVar = Linq.FindNetworkByName(networks, nic.getNetworkName());
        network net = (tempVar != null) ? tempVar : defaultNetwork;

        model.StartProgress(null);
        setcurrentModel(model);

        Frontend.RunAction(VdcActionType.DetachNetworkFromVdsInterface,
                new AttachNetworkToVdsParameters(getEntity().getvds_id(), net, nic),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) result.getState();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded())
                        {
                            EntityModel commitChanges =
                                    ((HostInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                            if ((Boolean) commitChanges.getEntity())
                            {
                                SaveNetworkConfig(hostInterfaceListModel.getEntity().getvds_id(),
                                        hostInterfaceListModel);
                            }
                            else
                            {
                                hostInterfaceListModel.getcurrentModel().StopProgress();
                                hostInterfaceListModel.Cancel();
                                hostInterfaceListModel.Search();
                            }
                        }
                        else
                        {
                            hostInterfaceListModel.getcurrentModel().StopProgress();
                        }

                    }
                },
                this);

    }

    public void Cancel()
    {
        setConfirmWindow(null);
        setWindow(null);
    }

    public void CancelConfirm()
    {
        setConfirmWindow(null);
    }

    private void UpdateActionAvailability()
    {
        VdsNetworkInterface selectedItem = (VdsNetworkInterface) getSelectedItem();
        java.util.ArrayList<VdsNetworkInterface> selectedItems = getSelectedItems();

        getEditCommand().setIsExecutionAllowed(getEntity() != null
                && getEntity().getstatus() != VDSStatus.NonResponsive && selectedItem != null
                && selectedItems.size() == 1 && StringHelper.isNullOrEmpty(selectedItem.getBondName())
                && !selectedItem.getIsManagement());

        getBondCommand().setIsExecutionAllowed(getEntity() != null
                && getEntity().getstatus() != VDSStatus.NonResponsive
                && selectedItems.size() >= 2
                && !IsAnyBond(selectedItems)
                && Linq.FindAllInterfaceNetworkNameNotEmpty(Linq.VdsNetworkInterfaceListToBase(selectedItems)).size() <= 1
                && Linq.FindAllInterfaceBondNameIsEmpty(selectedItems).size() == selectedItems.size()
                && Linq.FindAllInterfaceVlanIdIsEmpty(selectedItems).size() == selectedItems.size());

        if (getItems() != null)
        {
            java.util.ArrayList<HostInterfaceLineModel> itemList =
                    (java.util.ArrayList<HostInterfaceLineModel>) getItems();
            for (HostInterfaceLineModel lineMdoel : itemList)
            {
                if (lineMdoel.getIsSelected() && lineMdoel.getVLans() != null && lineMdoel.getVLans().size() > 0)
                {
                    getBondCommand().setIsExecutionAllowed(false);
                    break;
                }
            }
        }

        getDetachCommand().setIsExecutionAllowed(getEntity() != null
                && getEntity().getstatus() != VDSStatus.NonResponsive && selectedItems.size() == 1
                && selectedItem != null && !StringHelper.isNullOrEmpty(selectedItem.getNetworkName())
                && !selectedItem.getIsManagement());

        getSaveNetworkConfigCommand().setIsExecutionAllowed(getEntity() != null
                && (getEntity().getnet_config_dirty() == null ? false : getEntity().getnet_config_dirty()));

        getEditManagementNetworkCommand().setIsExecutionAllowed(getEntity() != null
                && getEntity().getstatus() != VDSStatus.NonResponsive && selectedItems.size() == 1
                && selectedItem != null && selectedItem.getIsManagement());
    }

    private boolean IsAnyBond(Iterable<VdsNetworkInterface> items)
    {
        for (VdsNetworkInterface item : items)
        {
            if ((item.getBonded() == null ? false : item.getBonded()))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getEditManagementNetworkCommand())
        {
            EditManagementNetwork();
        }
        else if (command == getBondCommand())
        {
            Bond();
        }
        else if (command == getDetachCommand())
        {
            Detach();
        }
        else if (command == getSaveNetworkConfigCommand())
        {
            SaveNetworkConfig(getEntity().getvds_id(), this);
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
        {
            OnSave();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnEditManagementNetwork"))
        {
            OnEditManagementNetwork();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnEditManagementNetworkConfirmation"))
        {
            OnEditManagementNetworkConfirmation(false);
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnEditManagementNetworkConfirmation_Bond"))
        {
            OnEditManagementNetworkConfirmation(true);
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnBond"))
        {
            OnBond();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnDetach"))
        {
            OnDetach();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnConfirmManagementDetach"))
        {
            OnConfirmManagementDetach();
        }

        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }

        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirm"))
        {
            CancelConfirm();
        }
    }

    @Override
    protected String getListName() {
        return "HostInterfaceListModel";
    }
}
