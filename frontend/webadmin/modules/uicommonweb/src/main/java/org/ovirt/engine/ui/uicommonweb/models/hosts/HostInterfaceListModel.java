package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.ProvidePropertyChangedEvent;

@SuppressWarnings("unused")
public class HostInterfaceListModel extends SearchableListModel
{
    private UICommand privateEditCommand;

    @Override
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
            onPropertyChanged(new PropertyChangedEventArgs("isSelectionAvailable")); //$NON-NLS-1$
        }
    }

    @Override
    public Collection getItems()
    {
        return super.items;
    }

    @Override
    public void setItems(Collection value)
    {
        if (items != value)
        {
            itemsChanging(value, items);
            items = value;
            itemsChanged();
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
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

            Guid currentItemId = currentItem.getId();
            Guid newItemId = newItem.getId();

            if (currentItemId.equals(newItemId))
            {
                setEntity(value, false);
                updateActionAvailability();
                return;
            }
        }

        super.setEntity(value);
    }

    private ArrayList<VdsNetworkInterface> getSelectedItems(boolean withVlans)
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
        return getSelectedItems(false);
    }

    public ArrayList<VdsNetworkInterface> getSelectedItemsWithVlans()
    {
        return getSelectedItems(true);
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
            onPropertyChanged(new PropertyChangedEventArgs("DetachConfirmationNeeded")); //$NON-NLS-1$
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
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHelpTag(HelpTag.network_interfaces);
        setHashName("network_interfaces"); //$NON-NLS-1$

        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setEditManagementNetworkCommand(new UICommand("EditManagementNetwork", this)); //$NON-NLS-1$
        setBondCommand(new UICommand("Bond", this)); //$NON-NLS-1$
        setDetachCommand(new UICommand("Detach", this)); //$NON-NLS-1$
        setSaveNetworkConfigCommand(new UICommand("SaveNetworkConfig", this)); //$NON-NLS-1$
        setSetupNetworksCommand(new UICommand("SetupNetworks", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            super.search();
        }
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        getSearchCommand().execute();
        updateActionAvailability();
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
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("status") || e.propertyName.equals("net_config_dirty")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            updateActionAvailability();
        }
    }

    @Override
    protected void syncSearch()
    {
        super.syncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel interfaceModel = (HostInterfaceListModel) model;
                Iterable iVdcQueryableItems = ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                ArrayList<VdsNetworkInterface> items = new ArrayList<VdsNetworkInterface>();

                Iterator networkInterfacesIterator = iVdcQueryableItems.iterator();
                while (networkInterfacesIterator.hasNext())
                {
                    items.add((VdsNetworkInterface) networkInterfacesIterator.next());
                }
                interfaceModel.updateItems(items);
            }
        };

        IdQueryParameters tempVar = new IdQueryParameters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetVdsInterfacesByVdsId, tempVar, _asyncQuery);
    }

    private void updateItems(Iterable<VdsNetworkInterface> source)
    {
        ArrayList<HostInterfaceLineModel> items = new ArrayList<HostInterfaceLineModel>();
        setOriginalItems((ArrayList<VdsNetworkInterface>) source);

        List<Bond> nonEmptyBonds = new ArrayList<Bond>();
        List<Nic> independentNics = new ArrayList<Nic>();
        Map<String, List<Nic>> bondToNics = new HashMap<String, List<Nic>>();
        Map<String, List<Vlan>> nicToVlans = new HashMap<String, List<Vlan>>();

        sortNics();
        classifyNics(nonEmptyBonds, independentNics, bondToNics, nicToVlans);

        // create all bond models
        for (Bond bond : nonEmptyBonds) {
            HostInterfaceLineModel model = lineModelFromBond(bond);
            items.add(model);

            // add contained interface models - should exist, but check just in case
            if (bondToNics.containsKey(bond.getName())) {
                for (Nic nic : bondToNics.get(bond.getName())) {
                    model.getInterfaces().add(hostInterfaceFromNic(nic));
                }
            }

            // add any corresponding VLAN bridge models
            model.getVLans().addAll(gatherVlans(bond, nicToVlans));
        }

        // create all independent NIC models
        for (Nic nic : independentNics) {
            HostInterfaceLineModel model = lineModelFromInterface(nic);
            model.getInterfaces().add(hostInterfaceFromNic(nic));
            items.add(model);

            // add any corresponding VLAN bridge models
            model.getVLans().addAll(gatherVlans(nic, nicToVlans));
        }

        setItems(items);
        updateActionAvailability();
    }

    private List<HostVLan> gatherVlans(VdsNetworkInterface nic, Map<String, List<Vlan>> nicToVlans) {
        List<HostVLan> hostVlanList = new ArrayList<HostVLan>();
        if (nicToVlans.containsKey(nic.getName())) {
            for (Vlan vlan : nicToVlans.get(nic.getName())) {
                hostVlanList.add(hostVlanFromNic(vlan));
            }
        }
        return hostVlanList;
    }

    private void sortNics() {
        Collections.sort(getOriginalItems(), new Linq.InterfaceComparator());
    }

    private void classifyNics(List<Bond> nonEmptyBonds,
            List<Nic> independentNics,
            Map<String, List<Nic>> bondToNics,
            Map<String, List<Vlan>> nicToVlans) {
        for (VdsNetworkInterface nic : getOriginalItems()) {
            if (nic instanceof Bond) {
                nonEmptyBonds.add((Bond) nic);
            } else if (nic instanceof Nic) {
                if (nic.getBondName() == null) {
                    independentNics.add((Nic) nic);
                } else {
                    if (bondToNics.containsKey(nic.getBondName())) {
                        bondToNics.get(nic.getBondName()).add((Nic) nic);
                    } else {
                        List<Nic> nicList = new ArrayList<Nic>();
                        nicList.add((Nic) nic);
                        bondToNics.put(nic.getBondName(), nicList);
                    }
                }
            } else if (nic instanceof Vlan) {
                String nameWithoutVlan = nic.getBaseInterface();
                if (nicToVlans.containsKey(nameWithoutVlan)) {
                    nicToVlans.get(nameWithoutVlan).add((Vlan) nic);
                } else {
                    List<Vlan> vlanList = new ArrayList<Vlan>();
                    vlanList.add((Vlan) nic);
                    nicToVlans.put(nameWithoutVlan, vlanList);
                }
            }
        }
    }

    private HostInterfaceLineModel lineModelFromInterface(VdsNetworkInterface nic) {
        HostInterfaceLineModel model = new HostInterfaceLineModel();
        model.setInterfaces(new ArrayList<HostInterface>());
        model.setVLans(new ArrayList<HostVLan>());
        model.setNetworkName(nic.getNetworkName());
        model.setIsManagement(nic.getIsManagement());
        model.setAddress(nic.getAddress());

        return model;
    }

    private HostInterfaceLineModel lineModelFromBond(VdsNetworkInterface nic) {
        HostInterfaceLineModel model = lineModelFromInterface(nic);
        model.setInterface(nic);
        model.setIsBonded(true);
        model.setBondName(nic.getName());
        model.setAddress(nic.getAddress());

        return model;
    }

    private HostInterface hostInterfaceFromNic(VdsNetworkInterface nic) {
        HostInterface hi = new HostInterface();
        hi.setInterface(nic);
        hi.setName(nic.getName());
        hi.setAddress(nic.getAddress());
        hi.setMAC(nic.getMacAddress());
        hi.setSpeed(nic.getSpeed());
        hi.setRxRate(nic.getStatistics().getReceiveRate());
        hi.setRxTotal(nic.getStatistics().getReceivedBytes());
        hi.setRxDrop(nic.getStatistics().getReceiveDropRate());
        hi.setTxRate(nic.getStatistics().getTransmitRate());
        hi.setTxTotal(nic.getStatistics().getTransmittedBytes());
        hi.setTxDrop(nic.getStatistics().getTransmitDropRate());
        hi.setStatus(nic.getStatistics().getStatus());
        hi.getPropertyChangedEvent().addListener(this);

        return hi;
    }

    private HostVLan hostVlanFromNic(VdsNetworkInterface nic) {
        HostVLan hv = new HostVLan();
        hv.setInterface(nic);
        hv.setName(nic.getName());
        hv.setNetworkName(nic.getNetworkName());
        hv.setAddress(nic.getAddress());
        hv.getPropertyChangedEvent().addListener(this);

        return hv;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ProvidePropertyChangedEvent.definition))
        {
            model_PropertyChanged(sender, (PropertyChangedEventArgs) args);
        }
        else if (sender instanceof Model && "InterfaceList".equals(((Model) sender).getTitle())) //$NON-NLS-1$
        {
            HostManagementNetworkModel managementNetworkModel = ((HostManagementNetworkModel) getWindow());
            VdsNetworkInterface vdsNetworkInterface = managementNetworkModel.getInterface().getSelectedItem();
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

    private void model_PropertyChanged(Object sender, PropertyChangedEventArgs args)
    {
        if (!args.propertyName.equals("IsSelected")) //$NON-NLS-1$
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
                setSelectedItem(Linq.firstOrDefault(getSelectedItems()));
            }
        }

        updateActionAvailability();
    }

    private ArrayList<String> getSelectedNicsNetworks(RefObject<Boolean> isVlanSelected,
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

    public void edit()
    {

        if (getWindow() != null)
        {
            return;
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
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
                        if (ObjectUtils.objectsEqual(network.getName(), item.getNetworkName()))
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

                    ArrayList<Network> unAttachedNetworks = Linq.except(networksByCluster, attachedNetworks);

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
                Collections.sort(networksToAdd, new NameableComparator());
                // Add a 'none' option to networks.
                if (!StringHelper.isNullOrEmpty(item.getNetworkName()))
                {
                    Network tempVar = new Network();
                    tempVar.setId(Guid.Empty);
                    tempVar.setName("None"); //$NON-NLS-1$
                    networksToAdd.add(0, tempVar);
                }

                HostInterfaceModel hostInterfaceModel = new HostInterfaceModel();
                hostInterfaceListModel.setWindow(hostInterfaceModel);
                hostInterfaceModel.setEntity(item.getName());
                hostInterfaceModel.setTitle(ConstantsManager.getInstance().getConstants().editNetworkInterfaceTitle());
                hostInterfaceModel.setHelpTag(HelpTag.edit_network_interface_hosts);
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
                    Map.Entry<String, EntityModel<String>> defaultItem = null;
                    RefObject<Map.Entry<String, EntityModel<String>>> tempRef_defaultItem =
                            new RefObject<Map.Entry<String, EntityModel<String>>>(defaultItem);
                    ArrayList<Map.Entry<String, EntityModel<String>>> list =
                            AsyncDataProvider.getInstance().getBondingOptionList(tempRef_defaultItem);
                    defaultItem = tempRef_defaultItem.argvalue;
                    Map.Entry<String, EntityModel<String>> selectBondingOpt =
                            new KeyValuePairCompat<String, EntityModel<String>>();
                    boolean containsSelectBondingOpt = false;
                    hostInterfaceModel.getBondingOptions().setItems(list);
                    for (int i = 0; i < list.size(); i++)
                    {
                        if (ObjectUtils.objectsEqual(list.get(i).getKey(), item.getBondOptions()))
                        {
                            selectBondingOpt = list.get(i);
                            containsSelectBondingOpt = true;
                            break;
                        }
                    }
                    if (containsSelectBondingOpt == false)
                    {
                        if (ObjectUtils.objectsEqual(item.getBondOptions(), AsyncDataProvider.getInstance().getDefaultBondingOption()))
                        {
                            selectBondingOpt = defaultItem;
                        }
                        else
                        {
                            selectBondingOpt = list.get(list.size() - 1);
                            EntityModel<String> entityModel = selectBondingOpt.getValue();
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
                    UICommand tempVar3 = UICommand.createDefaultOkUiCommand("OnSave", hostInterfaceListModel); //$NON-NLS-1$
                    hostInterfaceModel.getCommands().add(tempVar3);
                    UICommand tempVar4 = UICommand.createCancelUiCommand("Cancel", hostInterfaceListModel); //$NON-NLS-1$
                    hostInterfaceModel.getCommands().add(tempVar4);
                }

            }
        };
        AsyncDataProvider.getInstance().getClusterNetworkList(_asyncQuery, getEntity().getVdsGroupId());
    }

    public void editManagementNetwork()
    {
        if (getWindow() != null)
        {
            return;
        }

        HostManagementNetworkModel managementNicModel = new HostManagementNetworkModel();
        setWindow(managementNicModel);
        managementNicModel.setTitle(ConstantsManager.getInstance().getConstants().editManagementNetworkTitle());
        managementNicModel.setHelpTag(HelpTag.edit_management_network);
        managementNicModel.setHashName("edit_management_network"); //$NON-NLS-1$

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                final HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                ArrayList<Network> clusterNetworks = (ArrayList<Network>) ReturnValue;
                final VdsNetworkInterface item = (VdsNetworkInterface) hostInterfaceListModel.getSelectedItem();
                final HostManagementNetworkModel managementModel =
                        (HostManagementNetworkModel) hostInterfaceListModel.getWindow();
                Network networkToEdit = Linq.findNetworkByName(clusterNetworks, item.getNetworkName());

                managementModel.setEntity(networkToEdit);

                managementModel.setNoneBootProtocolAvailable(!item.getIsManagement());
                managementModel.setBootProtocol(!managementModel.getNoneBootProtocolAvailable()
                        && item.getBootProtocol() == NetworkBootProtocol.NONE ? NetworkBootProtocol.DHCP
                        : item.getBootProtocol());

                managementModel.getAddress().setEntity(item.getAddress());
                managementModel.getSubnet().setEntity(item.getSubnet());
                managementModel.getGateway().setEntity(item.getGateway());

                final StringBuilder tmpDefaultInterfaceName = new StringBuilder();

                AsyncDataProvider.getInstance().getInterfaceOptionsForEditNetwork(new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ArrayList<VdsNetworkInterface> interfaces = (ArrayList<VdsNetworkInterface>) returnValue;

                        String defaultInterfaceName = tmpDefaultInterfaceName.toString();
                        managementModel.getInterface().setItems(interfaces);
                        managementModel.getInterface()
                                .setSelectedItem(Linq.findInterfaceByNetworkName(interfaces, defaultInterfaceName));
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
                            Map.Entry<String, EntityModel<String>> defaultItem = null;
                            RefObject<Map.Entry<String, EntityModel<String>>> tempRef_defaultItem =
                                    new RefObject<Map.Entry<String, EntityModel<String>>>(defaultItem);
                            ArrayList<Map.Entry<String, EntityModel<String>>> list =
                                    AsyncDataProvider.getInstance().getBondingOptionList(tempRef_defaultItem);
                            defaultItem = tempRef_defaultItem.argvalue;
                            Map.Entry<String, EntityModel<String>> selectBondingOpt =
                                    new KeyValuePairCompat<String, EntityModel<String>>();
                            boolean containsSelectBondingOpt = false;
                            managementModel.getBondingOptions().setItems(list);
                            for (int i = 0; i < list.size(); i++)
                            {
                                if (ObjectUtils.objectsEqual(list.get(i).getKey(), item.getBondOptions()))
                                {
                                    selectBondingOpt = list.get(i);
                                    containsSelectBondingOpt = true;
                                    break;
                                }
                            }
                            if (containsSelectBondingOpt == false)
                            {
                                if (ObjectUtils.objectsEqual(item.getBondOptions(),
                                                             AsyncDataProvider.getInstance().getDefaultBondingOption()))
                                {
                                    selectBondingOpt = defaultItem;
                                }
                                else
                                {
                                    selectBondingOpt = list.get(list.size() - 1);
                                    EntityModel<String> entityModel = selectBondingOpt.getValue();
                                    entityModel.setEntity(item.getBondOptions());
                                }
                            }
                            managementModel.getBondingOptions().setSelectedItem(selectBondingOpt);
                        }

                        UICommand tempVar =
                                UICommand.createDefaultOkUiCommand("OnEditManagementNetworkConfirmation", hostInterfaceListModel); //$NON-NLS-1$
                        managementModel.getCommands().add(tempVar);
                        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", hostInterfaceListModel); //$NON-NLS-1$
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
        AsyncDataProvider.getInstance().getClusterNetworkList(_asyncQuery, getEntity().getVdsGroupId());
    }

    public void onEditManagementNetworkConfirmation(boolean isBond)
    {
        if (!isBond)
        {
            HostManagementNetworkModel model = (HostManagementNetworkModel) getWindow();
            if (!model.validate())
            {
                return;
            }
            if (model.getCheckConnectivity().getEntity() == true)
            {
                onEditManagementNetwork();
                return;
            }
        }
        else
        {
            HostBondInterfaceModel model = (HostBondInterfaceModel) getWindow();
            if (!model.validate())
            {
                return;
            }
            if (model.getCheckConnectivity().getEntity() == true)
            {
                onBond();
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
            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnEditManagementNetwork", this); //$NON-NLS-1$
            confirmModel.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = UICommand.createDefaultOkUiCommand("OnBond", this); //$NON-NLS-1$
            confirmModel.getCommands().add(tempVar2);
        }
        UICommand tempVar3 = UICommand.createCancelUiCommand("CancelConfirm", this); //$NON-NLS-1$
        confirmModel.getCommands().add(tempVar3);

    }

    public void onEditManagementNetwork()
    {
        HostManagementNetworkModel model = (HostManagementNetworkModel) getWindow();
        if (getConfirmWindow() != null)
        {
            ConfirmationModel confirmModel = (ConfirmationModel) getConfirmWindow();
            if (confirmModel.getLatch().getEntity() == true)
            {
                model.getCheckConnectivity().setEntity(true);
            }
        }

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.validate())
        {
            return;
        }

        VdsNetworkInterface nic = model.getInterface().getSelectedItem();
        Network network = model.getEntity();

        VdcActionType actionType = VdcActionType.UpdateNetworkToVdsInterface;
        UpdateNetworkToVdsParameters parameters =
                new UpdateNetworkToVdsParameters(getEntity().getId(),
                        network,
                        new ArrayList<VdsNetworkInterface>(Arrays.asList(new VdsNetworkInterface[] { nic })));

        Map.Entry<String, EntityModel<String>> bondingOption;
        if (model.getBondingOptions().getSelectedItem() != null)
        {
            bondingOption = model.getBondingOptions().getSelectedItem();

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
        if (nic.getBonded() == null || nic.getBonded() == false)
        {
            parameters.setBondingOptions(null);
        }

        if (network != null)
        {
            parameters.setOldNetworkName(network.getName());
        }
        parameters.setCheckConnectivity(model.getCheckConnectivity().getEntity());
        parameters.setBootProtocol(model.getBootProtocol());

        if (model.getIsStaticAddress())
        {
            parameters.setAddress(model.getAddress().getEntity());
            parameters.setSubnet(model.getSubnet().getEntity());
            parameters.setGateway(model.getGateway().getEntity());
        }

        model.startProgress(null);
        setcurrentModel(model);

        Frontend.getInstance().runAction(actionType, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) result.getState();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded())
                        {
                            EntityModel<Boolean> commitChanges =
                                    ((HostManagementNetworkModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                            if (commitChanges.getEntity())
                            {
                                new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                        hostInterfaceListModel.getcurrentModel(), getEntity()).execute();
                            }
                            else
                            {
                                hostInterfaceListModel.getcurrentModel().stopProgress();
                                hostInterfaceListModel.cancel();
                                hostInterfaceListModel.search();
                            }
                        }
                        else
                        {
                            hostInterfaceListModel.getcurrentModel().stopProgress();
                        }

                    }
                },
                this);
        cancelConfirm();
    }

    public void bond()
    {
        if (getWindow() != null)
        {
            return;
        }

        HostBondInterfaceModel bondModel = new HostBondInterfaceModel();
        setWindow(bondModel);
        bondModel.setTitle(ConstantsManager.getInstance().getConstants().bondNetworkInterfacesTitle());
        bondModel.setHelpTag(HelpTag.bond_network_interfaces);
        bondModel.setHashName("bond_network_interfaces"); //$NON-NLS-1$

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                ArrayList<Network> networksByCluster = (ArrayList<Network>) ReturnValue;
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

                ArrayList<Network> unAttachedNetworks = Linq.except(networksByCluster, attachedNetworks);

                // adding selected network names to list.
                boolean isVlanSelected = false;
                boolean isManagement = false;
                RefObject<Boolean> tempRef_isVlanSelected = new RefObject<Boolean>(isVlanSelected);
                RefObject<Boolean> tempRef_isManagement = new RefObject<Boolean>(isManagement);
                ArrayList<String> selectedNicsNetworks =
                        hostInterfaceListModel.getSelectedNicsNetworks(tempRef_isVlanSelected, tempRef_isManagement);
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
                VdsNetworkInterface bond = Linq.findInterfaceByIsBond(getSelectedItems());
                if (bond != null)
                // one of the bond items is a bond itself -> don't
                // allocate a new bond name, edit the existing one:
                {
                    innerBondModel.getBond()
                            .setItems(new ArrayList<String>(Arrays.asList(new String[] { bond.getName() })));
                    innerBondModel.getBond().setSelectedItem(bond.getName());
                    innerBondModel.getBond().setIsChangable(false);
                    hostInterfaceListModel.postBond(hostInterfaceListModel,
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
                        public void onSuccess(Object model1, Object ReturnValue1)
                        {
                            HostInterfaceListModel innerHostInterfaceListModel = (HostInterfaceListModel) model1;
                            HostBondInterfaceModel bModel =
                                    (HostBondInterfaceModel) innerHostInterfaceListModel.getWindow();
                            List<VdsNetworkInterface> bonds =
                                    ((VdcQueryReturnValue) ReturnValue1).getReturnValue();

                            List<String> bondNames = new ArrayList<String>();
                            for (VdsNetworkInterface bond : bonds) {
                                bondNames.add(bond.getName());
                            }
                            bModel.getBond().setItems(bondNames);
                            // ((List<Interface>)model.Bond.Options).Sort(a => a.name);
                            bModel.getBond().setSelectedItem(Linq.firstOrDefault(bondNames));
                            boolean hasManagement = false;
                            for (VdsNetworkInterface innerItem : innerHostInterfaceListModel.getSelectedItemsWithVlans())
                            {
                                if (innerItem.getIsManagement())
                                {
                                    hasManagement = true;
                                    break;
                                }
                            }
                            innerHostInterfaceListModel.postBond(innerHostInterfaceListModel,
                                    bModel,
                                    bModel.getNetwork().getItems() != null ? (ArrayList<Network>) bModel.getNetwork()
                                            .getItems()
                                            : new ArrayList<Network>(),
                                    hasManagement);

                        }
                    };
                    Frontend.getInstance().runQuery(VdcQueryType.GetVdsFreeBondsByVdsId,
                            new IdQueryParameters(host.getId()),
                            _asyncQuery1);
                }
            }
        };
        AsyncDataProvider.getInstance().getClusterNetworkList(_asyncQuery, getEntity().getVdsGroupId());
    }

    public void postBond(HostInterfaceListModel hostInterfaceListModel,
            HostBondInterfaceModel innerBondModel,
            ArrayList<Network> networksToAdd,
            boolean isAnyManagement)
    {

        VdsNetworkInterface interfaceWithNetwork =
                Linq.findInterfaceNetworkNameNotEmpty(getSelectedItemsWithVlans());

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
                UICommand tempVar2 = UICommand.createDefaultOkUiCommand("OnEditManagementNetworkConfirmation_Bond", hostInterfaceListModel); //$NON-NLS-1$
                innerBondModel.getCommands().add(tempVar2);
                UICommand tempVar3 = UICommand.createCancelUiCommand("Cancel", hostInterfaceListModel); //$NON-NLS-1$
                innerBondModel.getCommands().add(tempVar3);
            }
            else
            {
                UICommand tempVar4 = UICommand.createDefaultOkUiCommand("OnBond", hostInterfaceListModel); //$NON-NLS-1$
                innerBondModel.getCommands().add(tempVar4);
                UICommand tempVar5 = UICommand.createCancelUiCommand("Cancel", hostInterfaceListModel); //$NON-NLS-1$
                innerBondModel.getCommands().add(tempVar5);
            }
        }
    }

    public void onBond()
    {
        HostBondInterfaceModel model = (HostBondInterfaceModel) getWindow();

        if (getConfirmWindow() != null)
        {
            ConfirmationModel confirmModel = (ConfirmationModel) getConfirmWindow();
            if (confirmModel.getLatch().getEntity() == true)
            {
                model.getCheckConnectivity().setEntity(true);
            }
            cancelConfirm();
        }

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.validate())
        {
            return;
        }

        VDS host = getEntity();
        ArrayList<VdsNetworkInterface> selectedItems = getSelectedItems();
        Network net = model.getNetwork().getSelectedItem();

        // Interface interfaceWithNetwork = items.FirstOrDefault(a => !string.IsNullOrEmpty(a.network_name));
        VdsNetworkInterface interfaceWithNetwork = Linq.findInterfaceNetworkNameNotEmpty(selectedItems);

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
            parameters.setCheckConnectivity(model.getCheckConnectivity().getEntity());
            parameters.setOldNetworkName(interfaceWithNetwork.getNetworkName());

            Map.Entry<String, EntityModel<String>> bondingOption;
            if (model.getBondingOptions().getSelectedItem() != null)
            {
                bondingOption = model.getBondingOptions().getSelectedItem();

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
            parameters.setBondName(model.getBond().getSelectedItem());

            if (model.getIsStaticAddress())
            {
                parameters.setAddress(model.getAddress().getEntity());
                parameters.setSubnet(model.getSubnet().getEntity());
                if (interfaceWithNetwork.getIsManagement())
                {
                    parameters.setGateway(model.getGateway().getEntity());
                }
            }

            model.startProgress(null);
            setcurrentModel(model);

            Frontend.getInstance().runAction(VdcActionType.UpdateNetworkToVdsInterface, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {

                            HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) result.getState();
                            VdcReturnValueBase returnValueBase = result.getReturnValue();
                            if (returnValueBase != null && returnValueBase.getSucceeded())
                            {
                                EntityModel<Boolean> commitChanges =
                                        ((HostBondInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                                if (commitChanges.getEntity())
                                {
                                    new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                            hostInterfaceListModel.getcurrentModel(), getEntity()).execute();
                                }
                                else
                                {
                                    hostInterfaceListModel.getcurrentModel().stopProgress();
                                    hostInterfaceListModel.cancel();
                                    hostInterfaceListModel.search();
                                }
                            }
                            else
                            {
                                hostInterfaceListModel.getcurrentModel().stopProgress();
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

            AddBondParameters parameters =
                    new AddBondParameters(host.getId(),
                                          model.getBond().getSelectedItem(),
                            net,
                            nics);
            Map.Entry<String, EntityModel<String>> bondingOption;
            if (model.getBondingOptions().getSelectedItem() != null)
            {
                bondingOption = model.getBondingOptions().getSelectedItem();

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
                parameters.setAddress(model.getAddress().getEntity());
                parameters.setSubnet(model.getSubnet().getEntity());
                parameters.setGateway(model.getGateway().getEntity());
            }

            model.startProgress(null);
            setcurrentModel(model);

            Frontend.getInstance().runAction(VdcActionType.AddBond, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {

                            HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) result.getState();
                            VdcReturnValueBase returnValueBase = result.getReturnValue();
                            if (returnValueBase != null && returnValueBase.getSucceeded())
                            {
                                EntityModel<Boolean> commitChanges =
                                        ((HostBondInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                                if (commitChanges.getEntity())
                                {
                                    new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                            hostInterfaceListModel.getcurrentModel(), getEntity()).execute();
                                }
                                else
                                {
                                    hostInterfaceListModel.getcurrentModel().stopProgress();
                                    hostInterfaceListModel.cancel();
                                    hostInterfaceListModel.search();
                                }
                            }
                            else
                            {
                                hostInterfaceListModel.getcurrentModel().stopProgress();
                            }

                        }
                    },
                    this);
        }
    }

    public void detach()
    {
        if (getWindow() != null)
        {
            return;
        }

        HostInterfaceModel model = new HostInterfaceModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().detachNetworkInterfacesTitle());
        model.setHelpTag(HelpTag.detach_network_interfaces);
        model.setHashName("detach_network_interfaces"); //$NON-NLS-1$

        VdsNetworkInterface nic = (VdsNetworkInterface) getSelectedItem();
        model.getName().setEntity(nic.getName());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnDetach", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onDetach()
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
            public void onSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                ArrayList<Network> networks = (ArrayList<Network>) ReturnValue;

                Network defaultNetwork = new Network();
                VdsNetworkInterface nic = (VdsNetworkInterface) getSelectedItem();
                defaultNetwork.setName(nic.getNetworkName());
                Network tempVar = Linq.findNetworkByName(networks, nic.getNetworkName());
                Network net = (tempVar != null) ? tempVar : defaultNetwork;

                hostInterfaceListModel.startProgress(null);

                Frontend.getInstance().runAction(VdcActionType.DetachNetworkFromVdsInterface,
                        new AttachNetworkToVdsParameters(getEntity().getId(), net, nic),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void executed(FrontendActionAsyncResult result) {

                                HostInterfaceListModel hostInterfaceListModel =
                                        (HostInterfaceListModel) result.getState();
                                VdcReturnValueBase returnValueBase = result.getReturnValue();
                                if (returnValueBase != null && returnValueBase.getSucceeded())
                                {
                                    EntityModel<Boolean> commitChanges =
                                            ((HostInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                                    if (commitChanges.getEntity())
                                    {
                                        new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                                getcurrentModel(),
                                                getEntity()).execute();
                                    }
                                    else
                                    {
                                        hostInterfaceListModel.getcurrentModel().stopProgress();
                                        hostInterfaceListModel.cancel();
                                        hostInterfaceListModel.search();
                                    }
                                }
                                else
                                {
                                    hostInterfaceListModel.getcurrentModel().stopProgress();
                                    hostInterfaceListModel.cancel();
                                }

                            }
                        },
                        hostInterfaceListModel);

            }
        };
        AsyncDataProvider.getInstance().getClusterNetworkList(_asyncQuery, getEntity().getVdsGroupId());
    }

    public void onSave()
    {
        HostInterfaceModel model = (HostInterfaceModel) getWindow();

        if (!model.validate())
        {
            return;
        }

        String nicName = (String) model.getEntity();
        final VdsNetworkInterface nic =
                (VdsNetworkInterface) Linq.findInterfaceByName(getAllItems(), nicName);

        if (nic == null)
        {
            cancel();
            return;
        }

        Network network = model.getNetwork().getSelectedItem();

        // Save changes.
        if (network.getId().equals(Guid.Empty))
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
                confirmModel.setHelpTag(HelpTag.edit_management_network_interface);
                confirmModel.setHashName("edit_management_network_interface"); //$NON-NLS-1$
                confirmModel.setMessage(ConstantsManager.getInstance()
                        .getMessages()
                        .youAreAboutToDisconnectHostInterfaceMsg(nic.getName()));

                UICommand tempVar = UICommand.createDefaultOkUiCommand("OnConfirmManagementDetach", this); //$NON-NLS-1$
                confirmModel.getCommands().add(tempVar);
                UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
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
                    public void onSuccess(Object model, Object ReturnValue)
                    {
                        final HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                        HostInterfaceModel hostInterfaceModel = (HostInterfaceModel) hostInterfaceListModel.getWindow();
                        ArrayList<Network> networks = (ArrayList<Network>) ReturnValue;
                        Network defaultNetwork = new Network();
                        defaultNetwork.setName(nic.getNetworkName());
                        Network tempVar3 = Linq.findNetworkByName(networks, nic.getNetworkName());
                        Network net = (tempVar3 != null) ? tempVar3 : defaultNetwork;

                        hostInterfaceModel.startProgress(null);
                        setcurrentModel(hostInterfaceModel);

                        Frontend.getInstance().runAction(VdcActionType.DetachNetworkFromVdsInterface,
                                new AttachNetworkToVdsParameters(getEntity().getId(), net, nic),
                                new IFrontendActionAsyncCallback() {
                                    @Override
                                    public void executed(FrontendActionAsyncResult result) {

                                        HostInterfaceListModel hostInterfaceListModel =
                                                (HostInterfaceListModel) result.getState();
                                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                                        if (returnValueBase != null && returnValueBase.getSucceeded())
                                        {
                                            EntityModel<Boolean> commitChanges =
                                                    ((HostInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                                            if (commitChanges.getEntity())
                                            {
                                                new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                                        getcurrentModel(), getEntity()).execute();
                                            }
                                            else
                                            {
                                                hostInterfaceListModel.getcurrentModel().stopProgress();
                                                hostInterfaceListModel.cancel();
                                                hostInterfaceListModel.search();
                                            }
                                        }
                                        else
                                        {
                                            hostInterfaceListModel.getcurrentModel().stopProgress();
                                        }

                                    }
                                },
                                hostInterfaceListModel);
                    }
                };
                AsyncDataProvider.getInstance().getClusterNetworkList(_asyncQuery, getEntity().getVdsGroupId());
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
                    if (item.getInterface() != null && item.getInterface().getId().equals(nic.getId()))
                    {
                        if (item.getVLans() != null && item.getVLans().size() > 0)
                        {
                            bondWithVlans = true;
                            for (HostVLan vLan : item.getVLans())
                            {
                                if (ObjectUtils.objectsEqual(network.getName(), vLan.getNetworkName()))
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
                    if (item.getVlanId() != null && ObjectUtils.objectsEqual(item.getNetworkName(), network.getName()))
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
                parameters.setCheckConnectivity(model.getCheckConnectivity().getEntity());

                actionType = VdcActionType.UpdateNetworkToVdsInterface;
            }
            Map.Entry<String, EntityModel<String>> bondingOption;
            if (model.getBondingOptions().getSelectedItem() != null)
            {
                bondingOption = model.getBondingOptions().getSelectedItem();

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
                parameters.setAddress(model.getAddress().getEntity());
                parameters.setSubnet(model.getSubnet().getEntity());
            }

            model.startProgress(null);
            setcurrentModel(model);

            Frontend.getInstance().runAction(actionType, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {

                            HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) result.getState();
                            VdcReturnValueBase returnValueBase = result.getReturnValue();
                            if (returnValueBase != null && returnValueBase.getSucceeded())
                            {
                                EntityModel<Boolean> commitChanges =
                                        ((HostInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                                if (commitChanges.getEntity())
                                {
                                    new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                            hostInterfaceListModel.getcurrentModel(), getEntity()).execute();
                                }
                                else
                                {
                                    hostInterfaceListModel.getcurrentModel().stopProgress();
                                    hostInterfaceListModel.cancel();
                                    hostInterfaceListModel.search();
                                }
                            }
                            else
                            {
                                hostInterfaceListModel.getcurrentModel().stopProgress();
                            }

                        }
                    },
                    this);
        }
    }

    public void saveNetworkConfig() {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().saveNetworkConfigurationTitle());
        model.setHelpTag(HelpTag.save_network_configuration);
        model.setHashName("save_network_configuration"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantToMakeTheChangesPersistentMsg());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSaveNetworkConfig", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onSaveNetworkConfig() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        model.startProgress(null);
        setcurrentModel(model);
        new SaveNetworkConfigAction(this, model, getEntity()).execute();
    }

    public void onConfirmManagementDetach()
    {
        HostInterfaceModel model = (HostInterfaceModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        String nicName = (String) model.getEntity();
        final VdsNetworkInterface nic =
                (VdsNetworkInterface) Linq.findInterfaceByName(getInterfaceItems(), nicName);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                HostInterfaceListModel hostInterfaceListModel = (HostInterfaceListModel) model;
                ArrayList<Network> networks = (ArrayList<Network>) ReturnValue;
                Network defaultNetwork = new Network();
                defaultNetwork.setName(nic.getNetworkName());
                Network tempVar = Linq.findNetworkByName(networks, nic.getNetworkName());
                Network net = (tempVar != null) ? tempVar : defaultNetwork;

                hostInterfaceListModel.startProgress(null);
                setcurrentModel(hostInterfaceListModel);

                Frontend.getInstance().runAction(VdcActionType.DetachNetworkFromVdsInterface,
                        new AttachNetworkToVdsParameters(getEntity().getId(), net, nic),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void executed(FrontendActionAsyncResult result) {

                                HostInterfaceListModel hostInterfaceListModel =
                                        (HostInterfaceListModel) result.getState();
                                VdcReturnValueBase returnValueBase = result.getReturnValue();
                                if (returnValueBase != null && returnValueBase.getSucceeded())
                                {
                                    EntityModel<Boolean> commitChanges =
                                            ((HostInterfaceModel) hostInterfaceListModel.getcurrentModel()).getCommitChanges();
                                    if (commitChanges.getEntity())
                                    {
                                        new SaveNetworkConfigAction(HostInterfaceListModel.this,
                                                hostInterfaceListModel.getcurrentModel(), getEntity()).execute();
                                    }
                                    else
                                    {
                                        hostInterfaceListModel.getcurrentModel().stopProgress();
                                        hostInterfaceListModel.cancel();
                                        hostInterfaceListModel.search();
                                    }
                                }
                                else
                                {
                                    hostInterfaceListModel.getcurrentModel().stopProgress();
                                }

                            }
                        },
                        hostInterfaceListModel);
            }
        };
        AsyncDataProvider.getInstance().getClusterNetworkList(_asyncQuery, getEntity().getVdsGroupId());
    }

    public void cancel()
    {
        setConfirmWindow(null);
        setWindow(null);
    }

    public void cancelConfirm()
    {
        setConfirmWindow(null);
    }

    public void setupNetworks() {

        if (getWindow() != null) {
            return;
        }

        HostSetupNetworksModel setupNetworksWindowModel = new HostSetupNetworksModel(this, getEntity());
        setWindow(setupNetworksWindowModel);
    }

    private void updateActionAvailability()
    {
        VDS host = getEntity();
        VdsNetworkInterface selectedItem = (VdsNetworkInterface) getSelectedItem();
        ArrayList<VdsNetworkInterface> selectedItems = getSelectedItems();

        getEditCommand().setIsExecutionAllowed(host != null
                && host.getStatus() != VDSStatus.NonResponsive && selectedItem != null
                && selectedItems.size() == 1 && StringHelper.isNullOrEmpty(selectedItem.getBondName())
                && !selectedItem.getIsManagement());

        getBondCommand().setIsExecutionAllowed(host != null
                && host.getStatus() != VDSStatus.NonResponsive
                && selectedItems.size() >= 2
                && !isAnyBond(selectedItems)
                && Linq.findAllInterfaceNetworkNameNotEmpty(selectedItems).size() <= 1
                && Linq.findAllInterfaceBondNameIsEmpty(selectedItems).size() == selectedItems.size()
                && Linq.findAllInterfaceVlanIdIsEmpty(selectedItems).size() == selectedItems.size());

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
                && host.getStatus() != VDSStatus.NonResponsive && selectedItems.size() == 1
                && selectedItem != null && !StringHelper.isNullOrEmpty(selectedItem.getNetworkName())
                && !selectedItem.getIsManagement());

        getSaveNetworkConfigCommand().setIsExecutionAllowed(host != null
                && (host.getNetConfigDirty() == null ? false : host.getNetConfigDirty()));

        getEditManagementNetworkCommand().setIsExecutionAllowed(host != null
                && host.getStatus() != VDSStatus.NonResponsive && selectedItems.size() == 1
                && selectedItem != null && selectedItem.getIsManagement());

        // Setup Networks is only available on 3.1 Clusters, all the other commands (except save network configuration)
        // available only on less than 3.1 Clusters
        if (host != null) {
            boolean isLessThan31 = host.getVdsGroupCompatibilityVersion().compareTo(Version.v3_1) < 0;

            getSetupNetworksCommand().setIsAvailable(!isLessThan31);

            getSaveNetworkConfigCommand().setIsAvailable(true);

            getEditCommand().setIsAvailable(isLessThan31);
            getBondCommand().setIsAvailable(isLessThan31);
            getDetachCommand().setIsAvailable(isLessThan31);
            getEditManagementNetworkCommand().setIsAvailable(isLessThan31);

            setSelectionAvailable(isLessThan31);

            // disable subtab refresh for pre-3.1 clusters, to avoid interfering with row selection
            setIsTimerDisabled(isLessThan31);
            if (isLessThan31) {
                getTimer().stopWithNotification();
            } else {
                getTimer().startWithNotification();
            }
        }
    }

    private boolean isAnyBond(Iterable<VdsNetworkInterface> items)
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
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getEditCommand())
        {
            edit();
        }
        else if (command == getEditManagementNetworkCommand())
        {
            editManagementNetwork();
        }
        else if (command == getSetupNetworksCommand())
        {
            setupNetworks();
        }
        else if (command == getBondCommand())
        {
            bond();
        }
        else if (command == getDetachCommand())
        {
            detach();
        }
        else if (command == getSaveNetworkConfigCommand())
        {
            saveNetworkConfig();
        }

        else if ("OnSave".equals(command.getName())) //$NON-NLS-1$
        {
            onSave();
        }

        else if ("OnEditManagementNetwork".equals(command.getName())) //$NON-NLS-1$
        {
            onEditManagementNetwork();
        }

        else if ("OnEditManagementNetworkConfirmation".equals(command.getName())) //$NON-NLS-1$
        {
            onEditManagementNetworkConfirmation(false);
        }

        else if ("OnEditManagementNetworkConfirmation_Bond".equals(command.getName())) //$NON-NLS-1$
        {
            onEditManagementNetworkConfirmation(true);
        }

        else if ("OnBond".equals(command.getName())) //$NON-NLS-1$
        {
            onBond();
        }

        else if ("OnDetach".equals(command.getName())) //$NON-NLS-1$
        {
            onDetach();
        }

        else if ("OnConfirmManagementDetach".equals(command.getName())) //$NON-NLS-1$
        {
            onConfirmManagementDetach();
        }

        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }

        else if ("CancelConfirm".equals(command.getName())) //$NON-NLS-1$
        {
            cancelConfirm();
        }

        else if ("OnSaveNetworkConfig".equals(command.getName())) //$NON-NLS-1$
        {
            onSaveNetworkConfig();
        }

    }

    @Override
    protected String getListName() {
        return "HostInterfaceListModel"; //$NON-NLS-1$
    }
}
