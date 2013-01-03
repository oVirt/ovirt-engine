package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.AddBondParameters;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.action.UpdateNetworkToVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
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
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class HostInterfaceListModel extends SearchableListModel
{

    public static String ENGINE_NETWORK_NAME;

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

    private UICommand privateSetupNetworksCommand;

    public UICommand getSetupNetworksCommand()
    {
        return privateSetupNetworksCommand;
    }

    private void setSetupNetworksCommand(UICommand value)
    {
        privateSetupNetworksCommand = value;
    }

    private ArrayList<VdsNetworkInterface> privateOriginalItems;

    public ArrayList<VdsNetworkInterface> getOriginalItems()
    {
        return privateOriginalItems;
    }

    public void setOriginalItems(ArrayList<VdsNetworkInterface> value)
    {
        privateOriginalItems = value;
    }

    private boolean isSelectionAvailable;

    public boolean getIsSelectionAvailable() {
        return isSelectionAvailable;
    }

    public void setSelectionAvailable(boolean value) {
        if (isSelectionAvailable != value)
        {
            isSelectionAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("isSelectionAvailable")); //$NON-NLS-1$
        }
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
            OnPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
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

            Guid currentItemId = currentItem.getId().getValue();
            Guid newItemId = newItem.getId().getValue();

            if (currentItemId.equals(newItemId))
            {
                setEntity(value, false);
                UpdateActionAvailability();
                return;
            }
        }

        super.setEntity(value);
    }

    private ArrayList<VdsNetworkInterface> GetSelectedItems(boolean withVlans)
    {
        ArrayList<VdsNetworkInterface> list = new ArrayList<VdsNetworkInterface>();
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
    public ArrayList<VdsNetworkInterface> getSelectedItems()
    {
        return GetSelectedItems(false);
    }

    public ArrayList<VdsNetworkInterface> getSelectedItemsWithVlans()
    {
        return GetSelectedItems(true);
    }

    private ArrayList<VdsNetworkInterface> getInterfaceItems()
    {
        ArrayList<VdsNetworkInterface> list = new ArrayList<VdsNetworkInterface>();
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

    private ArrayList<VdsNetworkInterface> getAllItems()
    {
        ArrayList<VdsNetworkInterface> list = new ArrayList<VdsNetworkInterface>();
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
            OnPropertyChanged(new PropertyChangedEventArgs("DetachConfirmationNeeded")); //$NON-NLS-1$
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
        // get management network name
        ENGINE_NETWORK_NAME =
                (String) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

        setIsTimerDisabled(true);
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHashName("network_interfaces"); //$NON-NLS-1$

        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setEditManagementNetworkCommand(new UICommand("EditManagementNetwork", this)); //$NON-NLS-1$
        setBondCommand(new UICommand("Bond", this)); //$NON-NLS-1$
        setDetachCommand(new UICommand("Detach", this)); //$NON-NLS-1$
        setSaveNetworkConfigCommand(new UICommand("SaveNetworkConfig", this)); //$NON-NLS-1$
        setSetupNetworksCommand(new UICommand("SetupNetworks", this)); //$NON-NLS-1$

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

        if (e.PropertyName.equals("status") || e.PropertyName.equals("net_config_dirty")) //$NON-NLS-1$ //$NON-NLS-2$
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
                ArrayList<VdsNetworkInterface> items = new ArrayList<VdsNetworkInterface>();

                Iterator networkInterfacesIterator = iVdcQueryableItems.iterator();
                while (networkInterfacesIterator.hasNext())
                {
                    items.add((VdsNetworkInterface) networkInterfacesIterator.next());
                }
                interfaceModel.UpdateItems(items);
            }
        };

        GetVdsByVdsIdParameters tempVar = new GetVdsByVdsIdParameters(getEntity().getId());
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
        ArrayList<HostInterfaceLineModel> items = new ArrayList<HostInterfaceLineModel>();
        setOriginalItems((ArrayList<VdsNetworkInterface>) source);
        // Add bonded interfaces.
        for (VdsNetworkInterface nic : source)
        {
            if ((nic.getBonded() == null ? false : nic.getBonded()))
            {
                HostInterfaceLineModel model = new HostInterfaceLineModel();
                model.setInterfaces(new ArrayList<HostInterface>());
                model.setInterface(nic);
                model.setVLans(new ArrayList<HostVLan>());
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
                model.setInterfaces(new ArrayList<HostInterface>());
                model.setVLans(new ArrayList<HostVLan>());
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
                        && StringHelper.stringsEqual(nicName + "." + nic.getVlanId(), nic.getName())) //$NON-NLS-1$
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

            ArrayList<HostVLan> list = model.getVLans();
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
        else if (sender instanceof Model && StringHelper.stringsEqual(((Model) sender).getTitle(), "InterfaceList")) //$NON-NLS-1$
        {
            HostManagementNetworkModel managementNetworkModel = ((HostManagementNetworkModel) getWindow());
            VdsNetworkInterface vdsNetworkInterface =
                    (VdsNetworkInterface) managementNetworkModel.getInterface().getSelectedItem();
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
        if (!args.PropertyName.equals("IsSelected")) //$NON-NLS-1$
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

    private ArrayList<String> GetSelectedNicsNetworks(RefObject<Boolean> isVlanSelected,
            RefObject<Boolean> isManagementSelected)
    {
        ArrayList<VdsNetworkInterface> selectedItems = getSelectedItemsWithVlans();
        ArrayList<String> list = new ArrayList<String>();
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
                ArrayList<Network> networksByCluster = (ArrayList<Network>) ReturnValue;
                VdsNetworkInterface item = (VdsNetworkInterface) hostInterfaceListModel.getSelectedItem();
                ArrayList<Network> networksToAdd = new ArrayList<Network>();
                Network selectedNetwork = null;
                if (item.getVlanId() != null)
                {
                    for (Network network : networksByCluster)
                    {
                        if (StringHelper.stringsEqual(network.getName(), item.getNetworkName()))
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
                    HashMap<String, Network> networkDictionary = new HashMap<String, Network>();
                    for (Network network : networksByCluster)
                    {
                        networkDictionary.put(network.getName(), network);
                    }
                    // creating list of attached networks.
                    ArrayList<Network> attachedNetworks = new ArrayList<Network>();
                    for (VdsNetworkInterface nic : hostInterfaceListModel.getAllItems())
                    {
                        if (nic.getNetworkName() != null && networkDictionary.containsKey(nic.getNetworkName()))
                        {
                            attachedNetworks.add(networkDictionary.get(nic.getNetworkName()));
                        }
                    }

                    ArrayList<Network> unAttachedNetworks = Linq.Except(networksByCluster, attachedNetworks);

                    // adding selected network names to list.
                    boolean isVlanSelected = false;
                    boolean isManagementSelected = false;

                    ArrayList<VdsNetworkInterface> selectedItems =
                            hostInterfaceListModel.getSelectedItemsWithVlans();
                    ArrayList<String> selectedNicsNetworks = new ArrayList<String>();
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
                            Network network = networkDictionary.get(selectedNetworkName);
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
                        for (Network unAttachedNetwork : unAttachedNetworks)
                        {
                            if (isVlanSelected)
                            {
                                if (unAttachedNetwork.getVlanId() != null)
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
                    Network tempVar = new Network();
                    tempVar.setId(NGuid.Empty);
                    tempVar.setName("None"); //$NON-NLS-1$
                    networksToAdd.add(0, tempVar);
                }

                HostInterfaceModel hostInterfaceModel = new HostInterfaceModel();
                hostInterfaceListModel.setWindow(hostInterfaceModel);
                hostInterfaceModel.setEntity(item.getName());
                hostInterfaceModel.setTitle(ConstantsManager.getInstance().getConstants().editNetworkInterfaceTitle());
                hostInterfaceModel.setHashName("edit_network_interface_hosts"); //$NON-NLS-1$

                hostInterfaceModel.setNetworks(hostInterfaceListModel.getSelectedItemsWithVlans());

                hostInterfaceModel.setNoneBootProtocolAvailable(!item.getIsManagement());
                hostInterfaceModel.setBootProtocol(!hostInterfaceModel.getNoneBootProtocolAvailable()
                        && item.getBootProtocol() == NetworkBootProtocol.NONE ? NetworkBootProtocol.DHCP
                        : item.getBootProtocol());

                hostInterfaceModel.getAddress().setEntity(item.getAddress());
                hostInterfaceModel.getSubnet().setEntity(item.getSubnet());

                hostInterfaceModel.getNetwork().setItems(networksToAdd);
                hostInterfaceModel.getName().setEntity(item.getName());

                hostInterfaceModel.getBondingOptions().setIsAvailable(false);

                if (item.getBonded() != null && item.getBonded().equals(true))
                {
                    hostInterfaceModel.getBondingOptions().setIsAvailable(true);
                    Map.Entry<String, EntityModel> defaultItem = null;
                    RefObject<Map.Entry<String, EntityModel>> tempRef_defaultItem =
                            new RefObject<Map.Entry<String, EntityModel>>(defaultItem);
                    ArrayList<Map.Entry<String, EntityModel>> list =
                            AsyncDataProvider.GetBondingOptionList(tempRef_defaultItem);
                    defaultItem = tempRef_defaultItem.argvalue;
                    Map.Entry<String, EntityModel> selectBondingOpt =
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
                        if (StringHelper.stringsEqual(item.getBondOptions(), AsyncDataProvider.GetDefaultBondingOption()))
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
                    hostInterfaceModel.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .thereAreNoNetworksAvailablePleaseAddAdditionalNetworksMsg());

                    UICommand tempVar2 = new UICommand("Cancel", hostInterfaceListModel); //$NON-NLS-1$
                    tempVar2.setTitle(ConstantsManager.getInstance().getConstants().close());
                    tempVar2.setIsDefault(true);
                    tempVar2.setIsCancel(true);
                    hostInterfaceModel.getCommands().add(tempVar2);
                }
                else
                {
                    UICommand tempVar3 = new UICommand("OnSave", hostInterfaceListModel); //$NON-NLS-1$
                    tempVar3.setTitle(ConstantsManager.getInstance().getConstants().ok());
                    tempVar3.setIsDefault(true);
                    hostInterfaceModel.getCommands().add(tempVar3);
                    UICommand tempVar4 = new UICommand("Cancel", hostInterfaceListModel); //$NON-NLS-1$
                    tempVar4.setTitle(ConstantsManager.getInstance().getConstants().cancel());
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
        managementNicModel.setTitle(ConstantsManager.getInstance().getConstants().editManagementNetworkTitle());
        managementNicModel.setHashName("edit_management_network"); //$NON-NLS-1$

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                final HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                ArrayList<Network> clusterNetworks = (ArrayList<Network>) ReturnValue;
                final VdsNetworkInterface item = (VdsNetworkInterface) hostInterfaceListModel.getSelectedItem();
                final HostManagementNetworkModel managementModel =
                        (HostManagementNetworkModel) hostInterfaceListModel.getWindow();
                Network networkToEdit = Linq.FindNetworkByName(clusterNetworks, item.getNetworkName());

                managementModel.setEntity(networkToEdit);

                managementModel.setNoneBootProtocolAvailable(!item.getIsManagement());
                managementModel.setBootProtocol(!managementModel.getNoneBootProtocolAvailable()
                        && item.getBootProtocol() == NetworkBootProtocol.NONE ? NetworkBootProtocol.DHCP
                        : item.getBootProtocol());

                managementModel.getAddress().setEntity(item.getAddress());
                managementModel.getSubnet().setEntity(item.getSubnet());
                managementModel.getGateway().setEntity(item.getGateway());

                final StringBuilder tmpDefaultInterfaceName = new StringBuilder();

                AsyncDataProvider.GetInterfaceOptionsForEditNetwork(new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        ArrayList<VdsNetworkInterface> interfaces = (ArrayList<VdsNetworkInterface>) returnValue;

                        String defaultInterfaceName = tmpDefaultInterfaceName.toString();
                        managementModel.getInterface().setItems(interfaces);
                        managementModel.getInterface()
                                .setSelectedItem(Linq.FindInterfaceByName(Linq.VdsNetworkInterfaceListToBase(interfaces),
                                        defaultInterfaceName));
                        if (item.getBonded() != null && item.getBonded().equals(true))
                        {
                            managementModel.getInterface().setTitle(ConstantsManager.getInstance()
                                    .getConstants()
                                    .interfaceListTitle());
                            managementModel.getInterface()
                                    .getSelectedItemChangedEvent()
                                    .addListener(hostInterfaceListModel);
                        }
                        managementModel.getCheckConnectivity().setIsAvailable(true);
                        managementModel.getCheckConnectivity().setIsChangable(true);
                        managementModel.getCheckConnectivity().setEntity(item.getIsManagement()); // currently, always
                                                                                                  // should be
                                                                                                  // true

                        managementModel.getBondingOptions().setIsAvailable(false);
                        if (item.getBonded() != null && item.getBonded().equals(true))
                        {
                            managementModel.getBondingOptions().setIsAvailable(true);
                            Map.Entry<String, EntityModel> defaultItem = null;
                            RefObject<Map.Entry<String, EntityModel>> tempRef_defaultItem =
                                    new RefObject<Map.Entry<String, EntityModel>>(defaultItem);
                            ArrayList<Map.Entry<String, EntityModel>> list =
                                    AsyncDataProvider.GetBondingOptionList(tempRef_defaultItem);
                            defaultItem = tempRef_defaultItem.argvalue;
                            Map.Entry<String, EntityModel> selectBondingOpt =
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
                                if (StringHelper.stringsEqual(item.getBondOptions(),
                                        AsyncDataProvider.GetDefaultBondingOption()))
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

                        UICommand tempVar =
                                new UICommand("OnEditManagementNetworkConfirmation", hostInterfaceListModel); //$NON-NLS-1$
                        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                        tempVar.setIsDefault(true);
                        managementModel.getCommands().add(tempVar);
                        UICommand tempVar2 = new UICommand("Cancel", hostInterfaceListModel); //$NON-NLS-1$
                        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                        tempVar2.setIsCancel(true);
                        managementModel.getCommands().add(tempVar2);

                    }
                }),
                        getOriginalItems(),
                        item,
                        networkToEdit,
                        getEntity().getId(),
                        tmpDefaultInterfaceName);

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
        confirmModel.setTitle(ConstantsManager.getInstance().getConstants().confirmTitle());
        confirmModel.getLatch().setEntity(true);
        confirmModel.getLatch().setIsAvailable(true);
        confirmModel.getLatch().setIsChangable(true);

        if (!isBond)
        {
            UICommand tempVar = new UICommand("OnEditManagementNetwork", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar.setIsDefault(true);
            confirmModel.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnBond", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar2.setIsDefault(true);
            confirmModel.getCommands().add(tempVar2);
        }
        UICommand tempVar3 = new UICommand("CancelConfirm", this); //$NON-NLS-1$
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
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
        Network network = model.getEntity();

        VdcActionType actionType = VdcActionType.UpdateNetworkToVdsInterface;
        UpdateNetworkToVdsParameters parameters =
                new UpdateNetworkToVdsParameters(getEntity().getId(),
                        network,
                        new ArrayList<VdsNetworkInterface>(Arrays.asList(new VdsNetworkInterface[] { nic })));

        Map.Entry<String, EntityModel> bondingOption;
        if (model.getBondingOptions().getSelectedItem() != null)
        {
            bondingOption = (Map.Entry<String, EntityModel>) model.getBondingOptions().getSelectedItem();

            if (!bondingOption.getKey().equals("custom")) //$NON-NLS-1$
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
            parameters.setOldNetworkName(network.getName());
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
                                new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                        hostInterfaceListModel.getcurrentModel(), getEntity()).execute();
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

    public void Bond()
    {
        if (getWindow() != null)
        {
            return;
        }

        HostBondInterfaceModel bondModel = new HostBondInterfaceModel();
        setWindow(bondModel);
        bondModel.setTitle(ConstantsManager.getInstance().getConstants().bondNetworkInterfacesTitle());
        bondModel.setHashName("bond_network_interfaces"); //$NON-NLS-1$

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                ArrayList<Network> networksByCluster = (ArrayList<Network>) ReturnValue;
                VdsNetworkInterface item = (VdsNetworkInterface) hostInterfaceListModel.getSelectedItem();
                HostBondInterfaceModel innerBondModel = (HostBondInterfaceModel) hostInterfaceListModel.getWindow();
                Network selectedNetwork = null;

                VDS host = hostInterfaceListModel.getEntity();
                // Allow change gateway if there one of the selected interfaces connected to engine network.
                boolean isAnyManagement = false;
                for (VdsNetworkInterface innerItem : hostInterfaceListModel.getSelectedItemsWithVlans())
                {
                    if (innerItem.getIsManagement())
                    {
                        isAnyManagement = true;
                        break;
                    }
                }

                ArrayList<Network> networksToAdd = new ArrayList<Network>();
                // creating dictionary of networks by name
                HashMap<String, Network> networkDictionary = new HashMap<String, Network>();
                for (Network network : networksByCluster)
                {
                    networkDictionary.put(network.getName(), network);
                }
                // creating list of attached networks.
                ArrayList<Network> attachedNetworks = new ArrayList<Network>();
                for (VdsNetworkInterface nic : hostInterfaceListModel.getAllItems())
                {
                    if (nic.getNetworkName() != null && networkDictionary.containsKey(nic.getNetworkName()))
                    {
                        attachedNetworks.add(networkDictionary.get(nic.getNetworkName()));
                    }
                }

                ArrayList<Network> unAttachedNetworks = Linq.Except(networksByCluster, attachedNetworks);

                // adding selected network names to list.
                boolean isVlanSelected = false;
                boolean isManagement = false;
                RefObject<Boolean> tempRef_isVlanSelected = new RefObject<Boolean>(isVlanSelected);
                RefObject<Boolean> tempRef_isManagement = new RefObject<Boolean>(isManagement);
                ArrayList<String> selectedNicsNetworks =
                        hostInterfaceListModel.GetSelectedNicsNetworks(tempRef_isVlanSelected, tempRef_isManagement);
                isVlanSelected = tempRef_isVlanSelected.argvalue;
                isManagement = tempRef_isManagement.argvalue;

                for (String selectedNetworkName : selectedNicsNetworks)
                {
                    if (networkDictionary.containsKey(selectedNetworkName))
                    {
                        Network network = networkDictionary.get(selectedNetworkName);
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
                    for (Network unAttachedNetwork : unAttachedNetworks)
                    {
                        if (isVlanSelected)
                        {
                            if (unAttachedNetwork.getVlanId() != null)
                            {
                                networksToAdd.add(unAttachedNetwork);
                            }
                        }
                        else
                        {
                            networksToAdd.add(unAttachedNetwork);
                        }
                    }
                    innerBondModel.getNetwork().setItems(networksToAdd);
                }
                else
                {
                    innerBondModel.getNetwork().setItems(new ArrayList<Network>(Arrays.asList(selectedNetwork)));
                }

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
                            .setItems(new ArrayList<VdsNetworkInterface>(Arrays.asList(new VdsNetworkInterface[] { bond })));
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
                            for (VdsNetworkInterface innerItem : innerHostInterfaceListModel.getSelectedItemsWithVlans())
                            {
                                if (innerItem.getIsManagement())
                                {
                                    hasManagement = true;
                                    break;
                                }
                            }
                            innerHostInterfaceListModel.PostBond(innerHostInterfaceListModel,
                                    bModel,
                                    bModel.getNetwork().getItems() != null ? (ArrayList<Network>) bModel.getNetwork()
                                            .getItems()
                                            : new ArrayList<Network>(),
                                    hasManagement);

                        }
                    };
                    Frontend.RunQuery(VdcQueryType.GetVdsFreeBondsByVdsId,
                            new GetVdsByVdsIdParameters(host.getId()),
                            _asyncQuery1);
                }
            }
        };
        AsyncDataProvider.GetClusterNetworkList(_asyncQuery, getEntity().getvds_group_id());
    }

    public void PostBond(HostInterfaceListModel hostInterfaceListModel,
            HostBondInterfaceModel innerBondModel,
            ArrayList<Network> networksToAdd,
            boolean isAnyManagement)
    {
        ArrayList<NetworkInterface> baseSelectedItems =
                Linq.VdsNetworkInterfaceListToBase(getSelectedItemsWithVlans());
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
                    && interfaceWithNetwork.getBootProtocol() == NetworkBootProtocol.NONE ? NetworkBootProtocol.DHCP
                    : interfaceWithNetwork.getBootProtocol());
            innerBondModel.getAddress().setEntity(interfaceWithNetwork.getAddress());
            innerBondModel.getSubnet().setEntity(interfaceWithNetwork.getSubnet());
            innerBondModel.getGateway().setEntity(interfaceWithNetwork.getGateway());
        }
        else
        {
            innerBondModel.setBootProtocol(NetworkBootProtocol.NONE);
        }

        innerBondModel.getGateway().setIsAvailable(isAnyManagement);

        if (networksToAdd.isEmpty())
        {
            innerBondModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .thereAreNoNetworksAvailablePleaseAddAdditionalNetworksMsg());

            UICommand tempVar = new UICommand("Cancel", hostInterfaceListModel); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            innerBondModel.getCommands().add(tempVar);
        }
        else
        {
            if (interfaceWithNetwork != null && interfaceWithNetwork.getIsManagement())
            {
                UICommand tempVar2 = new UICommand("OnEditManagementNetworkConfirmation_Bond", hostInterfaceListModel); //$NON-NLS-1$
                tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
                tempVar2.setIsDefault(true);
                innerBondModel.getCommands().add(tempVar2);
                UICommand tempVar3 = new UICommand("Cancel", hostInterfaceListModel); //$NON-NLS-1$
                tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                tempVar3.setIsCancel(true);
                innerBondModel.getCommands().add(tempVar3);
            }
            else
            {
                UICommand tempVar4 = new UICommand("OnBond", hostInterfaceListModel); //$NON-NLS-1$
                tempVar4.setTitle(ConstantsManager.getInstance().getConstants().ok());
                tempVar4.setIsDefault(true);
                innerBondModel.getCommands().add(tempVar4);
                UICommand tempVar5 = new UICommand("Cancel", hostInterfaceListModel); //$NON-NLS-1$
                tempVar5.setTitle(ConstantsManager.getInstance().getConstants().cancel());
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
        ArrayList<VdsNetworkInterface> selectedItems = getSelectedItems();
        Network net = (Network) model.getNetwork().getSelectedItem();

        // Interface interfaceWithNetwork = items.FirstOrDefault(a => !string.IsNullOrEmpty(a.network_name));
        VdsNetworkInterface interfaceWithNetwork =
                (VdsNetworkInterface) Linq.FindInterfaceNetworkNameNotEmpty(Linq.VdsNetworkInterfaceListToBase(selectedItems));

        // look for lines with vlans
        ArrayList<HostInterfaceLineModel> itemList =
                (ArrayList<HostInterfaceLineModel>) getItems();
        for (HostInterfaceLineModel lineModel : itemList)
        {
            if (lineModel.getIsSelected() && lineModel.getVlanSize() == 1)
            {
                interfaceWithNetwork = lineModel.getVLans().get(0).getInterface();
                // bond action is only enabled if there is one vlaned interface
                break;
            }
        }

        if (interfaceWithNetwork != null)
        {
            UpdateNetworkToVdsParameters parameters =
                    new UpdateNetworkToVdsParameters(host.getId(), net, selectedItems);
            parameters.setCheckConnectivity((Boolean) model.getCheckConnectivity().getEntity());
            parameters.setOldNetworkName(interfaceWithNetwork.getNetworkName());

            Map.Entry<String, EntityModel> bondingOption;
            if (model.getBondingOptions().getSelectedItem() != null)
            {
                bondingOption = (Map.Entry<String, EntityModel>) model.getBondingOptions().getSelectedItem();

                if (!bondingOption.getKey().equals("custom")) //$NON-NLS-1$
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
                                    new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                            hostInterfaceListModel.getcurrentModel(), getEntity()).execute();
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
                    new AddBondParameters(host.getId(),
                            ((VdsNetworkInterface) model.getBond().getSelectedItem()).getName(),
                            net,
                            nics);
            Map.Entry<String, EntityModel> bondingOption;
            if (model.getBondingOptions().getSelectedItem() != null)
            {
                bondingOption = (Map.Entry<String, EntityModel>) model.getBondingOptions().getSelectedItem();

                if (!bondingOption.getKey().equals("custom")) //$NON-NLS-1$
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
                                    new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                            hostInterfaceListModel.getcurrentModel(), getEntity()).execute();
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
        model.setTitle(ConstantsManager.getInstance().getConstants().detachNetworkInterfacesTitle());
        model.setHashName("detach_network_interfaces"); //$NON-NLS-1$

        VdsNetworkInterface nic = (VdsNetworkInterface) getSelectedItem();
        model.getName().setEntity(nic.getName());

        UICommand tempVar = new UICommand("OnDetach", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
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

        setcurrentModel(model);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                ArrayList<Network> networks = (ArrayList<Network>) ReturnValue;

                Network defaultNetwork = new Network();
                VdsNetworkInterface nic = (VdsNetworkInterface) getSelectedItem();
                defaultNetwork.setName(nic.getNetworkName());
                Network tempVar = Linq.FindNetworkByName(networks, nic.getNetworkName());
                Network net = (tempVar != null) ? tempVar : defaultNetwork;

                hostInterfaceListModel.StartProgress(null);

                Frontend.RunAction(VdcActionType.DetachNetworkFromVdsInterface,
                        new AttachNetworkToVdsParameters(getEntity().getId(), net, nic),
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
                                        new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                                getcurrentModel(),
                                                getEntity()).execute();
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
                                    hostInterfaceListModel.Cancel();
                                }

                            }
                        },
                        hostInterfaceListModel);

            }
        };
        AsyncDataProvider.GetClusterNetworkList(_asyncQuery, getEntity().getvds_group_id());
    }

    public void OnSave()
    {
        HostInterfaceModel model = (HostInterfaceModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        String nicName = (String) model.getEntity();
        final VdsNetworkInterface nic =
                (VdsNetworkInterface) Linq.FindInterfaceByName(Linq.VdsNetworkInterfaceListToBase(getAllItems()),
                        nicName);

        if (nic == null)
        {
            Cancel();
            return;
        }

        Network network = (Network) model.getNetwork().getSelectedItem();

        // Save changes.
        if (network.getId().equals(NGuid.Empty))
        {
            if (nic.getIsManagement())
            {
                // We are trying to disconnect the management interface from its
                // network -> ask for the user's confirmation before doing that.
                ConfirmationModel confirmModel = new ConfirmationModel();
                setConfirmWindow(confirmModel);
                confirmModel.setTitle(ConstantsManager.getInstance()
                        .getConstants()
                        .editManagementNetworkInterfaceTitle());
                confirmModel.setHashName("edit_management_network_interface"); //$NON-NLS-1$
                confirmModel.setMessage(ConstantsManager.getInstance()
                        .getMessages()
                        .youAreAboutToDisconnectHostInterfaceMsg(nic.getName()));

                UICommand tempVar = new UICommand("OnConfirmManagementDetach", this); //$NON-NLS-1$
                tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                tempVar.setIsDefault(true);
                confirmModel.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
                tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                tempVar2.setIsCancel(true);
                confirmModel.getCommands().add(tempVar2);
            }
            else
            {
                if (model.getProgress() != null)
                {
                    return;
                }

                AsyncQuery _asyncQuery = new AsyncQuery();
                _asyncQuery.setModel(this);
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object ReturnValue)
                    {
                        final HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                        HostInterfaceModel hostInterfaceModel = (HostInterfaceModel) hostInterfaceListModel.getWindow();
                        ArrayList<Network> networks = (ArrayList<Network>) ReturnValue;
                        Network defaultNetwork = new Network();
                        defaultNetwork.setName(nic.getNetworkName());
                        Network tempVar3 = Linq.FindNetworkByName(networks, nic.getNetworkName());
                        Network net = (tempVar3 != null) ? tempVar3 : defaultNetwork;

                        hostInterfaceModel.StartProgress(null);
                        setcurrentModel(hostInterfaceModel);

                        Frontend.RunAction(VdcActionType.DetachNetworkFromVdsInterface,
                                new AttachNetworkToVdsParameters(getEntity().getId(), net, nic),
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
                                                new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                                        getcurrentModel(), getEntity()).execute();
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
                                hostInterfaceListModel);
                    }
                };
                AsyncDataProvider.GetClusterNetworkList(_asyncQuery, getEntity().getvds_group_id());
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
                for (HostInterfaceLineModel item : (ArrayList<HostInterfaceLineModel>) getItems())
                {
                    if (item.getInterface() != null && item.getInterface().getId().getValue().equals(nic.getId()))
                    {
                        if (item.getVLans() != null && item.getVLans().size() > 0)
                        {
                            bondWithVlans = true;
                            for (HostVLan vLan : item.getVLans())
                            {
                                if (StringHelper.stringsEqual(network.getName(), vLan.getNetworkName()))
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
                    if (item.getVlanId() != null && StringHelper.stringsEqual(item.getNetworkName(), network.getName()))
                    {
                        isUpdateVlan = true;
                        break;
                    }
                }
            }
            // If the selected item is a non-attached or attached to vlan eth (over bond or not),
            // and the selected network in the dialog is a new vlan, attach selected network.
            if ((StringHelper.isNullOrEmpty(nic.getNetworkName()) && (nic.getBonded() == null || !nic.getBonded()) && !isUpdateVlan)
                    || (bondWithVlans && (!vLanAttached && network.getVlanId() != null)))
            {
                parameters = new AttachNetworkToVdsParameters(getEntity().getId(), network, nic);
                actionType = VdcActionType.AttachNetworkToVdsInterface;
            }
            else
            {
                parameters =
                        new UpdateNetworkToVdsParameters(getEntity().getId(),
                                network,
                                new ArrayList<VdsNetworkInterface>(Arrays.asList(new VdsNetworkInterface[] { nic })));
                parameters.setOldNetworkName((nic.getNetworkName() != null ? nic.getNetworkName() : network.getName()));
                parameters.setCheckConnectivity((Boolean) model.getCheckConnectivity().getEntity());

                actionType = VdcActionType.UpdateNetworkToVdsInterface;
            }
            Map.Entry<String, EntityModel> bondingOption;
            if (model.getBondingOptions().getSelectedItem() != null)
            {
                bondingOption = (Map.Entry<String, EntityModel>) model.getBondingOptions().getSelectedItem();

                if (!bondingOption.getKey().equals("custom")) //$NON-NLS-1$
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
                                    new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                            hostInterfaceListModel.getcurrentModel(), getEntity()).execute();
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

    public void SaveNetworkConfig() {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().saveNetworkConfigurationTitle());
        model.setHashName("save_network_configuration"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantToMakeTheChangesPersistentMsg());

        UICommand tempVar = new UICommand("OnSaveNetworkConfig", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnSaveNetworkConfig() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        model.StartProgress(null);
        setcurrentModel(model);
        new SaveNetworkConfigAction(this, model, getEntity()).execute();
    }

    public void OnConfirmManagementDetach()
    {
        HostInterfaceModel model = (HostInterfaceModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        String nicName = (String) model.getEntity();
        final VdsNetworkInterface nic =
                (VdsNetworkInterface) Linq.FindInterfaceByName(Linq.<NetworkInterface> Cast(getInterfaceItems()),
                        nicName);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                ArrayList<Network> networks = (ArrayList<Network>) ReturnValue;
                Network defaultNetwork = new Network();
                defaultNetwork.setName(nic.getNetworkName());
                Network tempVar = Linq.FindNetworkByName(networks, nic.getNetworkName());
                Network net = (tempVar != null) ? tempVar : defaultNetwork;

                hostInterfaceListModel.StartProgress(null);
                setcurrentModel(hostInterfaceListModel);

                Frontend.RunAction(VdcActionType.DetachNetworkFromVdsInterface,
                        new AttachNetworkToVdsParameters(getEntity().getId(), net, nic),
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
                                        new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                                hostInterfaceListModel.getcurrentModel(), getEntity()).execute();
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
                        hostInterfaceListModel);
            }
        };
        AsyncDataProvider.GetClusterNetworkList(_asyncQuery, getEntity().getvds_group_id());
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

    public void SetupNetworks() {

        if (getWindow() != null) {
            return;
        }

        HostSetupNetworksModel setupNetworksWindowModel = new HostSetupNetworksModel(this);
        setWindow(setupNetworksWindowModel);

        // set entity
        setupNetworksWindowModel.setEntity(getEntity());
    }

    private void UpdateActionAvailability()
    {
        VDS host = getEntity();
        VdsNetworkInterface selectedItem = (VdsNetworkInterface) getSelectedItem();
        ArrayList<VdsNetworkInterface> selectedItems = getSelectedItems();

        getEditCommand().setIsExecutionAllowed(host != null
                && host.getstatus() != VDSStatus.NonResponsive && selectedItem != null
                && selectedItems.size() == 1 && StringHelper.isNullOrEmpty(selectedItem.getBondName())
                && !selectedItem.getIsManagement());

        getBondCommand().setIsExecutionAllowed(host != null
                && host.getstatus() != VDSStatus.NonResponsive
                && selectedItems.size() >= 2
                && !IsAnyBond(selectedItems)
                && Linq.FindAllInterfaceNetworkNameNotEmpty(Linq.VdsNetworkInterfaceListToBase(selectedItems)).size() <= 1
                && Linq.FindAllInterfaceBondNameIsEmpty(selectedItems).size() == selectedItems.size()
                && Linq.FindAllInterfaceVlanIdIsEmpty(selectedItems).size() == selectedItems.size());

        // to bond, selected lines must not have more that 1 networks (vlan or not)
        if (getItems() != null)
        {
            ArrayList<HostInterfaceLineModel> itemList =
                    (ArrayList<HostInterfaceLineModel>) getItems();
            // total network count cannot be more than 1
            int totalNetworkCount = 0;
            for (HostInterfaceLineModel lineModel : itemList)
            {
                if (lineModel.getIsSelected())
                {
                    int lineNetworkCount = lineModel.getVlanSize() + (lineModel.getNetworkName() != null ? 1 : 0);
                    if (lineNetworkCount > 1) {
                        // bailout
                        getBondCommand().setIsExecutionAllowed(false);
                        break;
                    }
                    totalNetworkCount += lineNetworkCount;
                    if (totalNetworkCount > 1) {
                        // bailout
                        getBondCommand().setIsExecutionAllowed(false);
                        break;
                    }
                }
            }
        }

        getDetachCommand().setIsExecutionAllowed(host != null
                && host.getstatus() != VDSStatus.NonResponsive && selectedItems.size() == 1
                && selectedItem != null && !StringHelper.isNullOrEmpty(selectedItem.getNetworkName())
                && !selectedItem.getIsManagement());

        getSaveNetworkConfigCommand().setIsExecutionAllowed(host != null
                && (host.getnet_config_dirty() == null ? false : host.getnet_config_dirty()));

        getEditManagementNetworkCommand().setIsExecutionAllowed(host != null
                && host.getstatus() != VDSStatus.NonResponsive && selectedItems.size() == 1
                && selectedItem != null && selectedItem.getIsManagement());

        // Setup Networks is only available on 3.1 Clusters, all the other commands (except save network configuration)
        // available only on less than 3.1 Clusters
        if (host != null) {
            Version v31 = new Version(3, 1);
            boolean isLessThan31 = host.getvds_group_compatibility_version().compareTo(v31) < 0;

            getSetupNetworksCommand().setIsAvailable(!isLessThan31);

            getSaveNetworkConfigCommand().setIsAvailable(true);

            getEditCommand().setIsAvailable(isLessThan31);
            getBondCommand().setIsAvailable(isLessThan31);
            getDetachCommand().setIsAvailable(isLessThan31);
            getEditManagementNetworkCommand().setIsAvailable(isLessThan31);

            setSelectionAvailable(isLessThan31);
        }
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
    public void ExecuteCommand(UICommand command) {
        ExecuteCommand(command, new Object[0]);
    }

    @Override
    public void ExecuteCommand(UICommand command, Object... parameters)
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
        else if (command == getSetupNetworksCommand())
        {
            SetupNetworks();
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
            SaveNetworkConfig();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnEditManagementNetwork")) //$NON-NLS-1$
        {
            OnEditManagementNetwork();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnEditManagementNetworkConfirmation")) //$NON-NLS-1$
        {
            OnEditManagementNetworkConfirmation(false);
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnEditManagementNetworkConfirmation_Bond")) //$NON-NLS-1$
        {
            OnEditManagementNetworkConfirmation(true);
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnBond")) //$NON-NLS-1$
        {
            OnBond();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnDetach")) //$NON-NLS-1$
        {
            OnDetach();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnConfirmManagementDetach")) //$NON-NLS-1$
        {
            OnConfirmManagementDetach();
        }

        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }

        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirm")) //$NON-NLS-1$
        {
            CancelConfirm();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnSaveNetworkConfig")) //$NON-NLS-1$
        {
            OnSaveNetworkConfig();
        }

    }

    @Override
    protected String getListName() {
        return "HostInterfaceListModel"; //$NON-NLS-1$
    }
}
