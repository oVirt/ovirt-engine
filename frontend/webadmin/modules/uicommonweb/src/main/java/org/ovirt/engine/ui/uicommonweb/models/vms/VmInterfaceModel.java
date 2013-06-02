package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetDeviceCustomPropertiesParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.MacAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public abstract class VmInterfaceModel extends Model
{
    protected static String ENGINE_NETWORK_NAME;

    private EntityModel privateName;
    private ListModel privateNetwork;
    private EntityModel linked;
    private EntityModel linked_IsSelected;
    private EntityModel unlinked_IsSelected;
    private ListModel privateNicType;
    private EntityModel privatePortMirroring;
    private EntityModel privateMAC;
    private EntityModel enableMac;
    private EntityModel plugged;
    private EntityModel plugged_IsSelected;
    private EntityModel unplugged_IsSelected;

    protected final boolean hotPlugSupported;
    protected final boolean hotUpdateSupported;
    private final VmBase vm;
    private final ArrayList<VmNetworkInterface> vmNicList;

    private UICommand okCommand;

    private final EntityModel sourceModel;
    private final Version clusterCompatibilityVersion;

    private KeyValueModel customPropertySheet;

    protected VmInterfaceModel(VmBase vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            EntityModel sourceModel)
    {
        // get management network name
        ENGINE_NETWORK_NAME =
                (String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

        this.vm = vm;
        this.vmNicList = vmNicList;
        this.sourceModel = sourceModel;
        this.clusterCompatibilityVersion = clusterCompatibilityVersion;

        hotPlugSupported =
                (Boolean) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.HotPlugEnabled,
                        clusterCompatibilityVersion.toString());

        hotUpdateSupported =
                (Boolean) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.NetworkLinkingSupported,
                        clusterCompatibilityVersion.toString());

        setName(new EntityModel());
        setNetwork(new ListModel() {
            @Override
            public void setSelectedItem(Object value) {
                super.setSelectedItem(value);
                updateLinkChangability();
            }
        });
        setNicType(new ListModel());
        setMAC(new EntityModel());
        setEnableMac(new EntityModel() {
            @Override
            public void setEntity(Object value) {
                super.setEntity(value);
                Boolean enableManualMac = (Boolean) value;
                getMAC().setIsChangable(enableManualMac);
            }
        });
        getEnableMac().setEntity(false);
        setPortMirroring(new EntityModel());
        getMAC().getPropertyChangedEvent().addListener(this);

        setLinked(new EntityModel());
        getLinked().getPropertyChangedEvent().addListener(this);

        setLinked_IsSelected(new EntityModel());
        getLinked_IsSelected().getEntityChangedEvent().addListener(this);

        setUnlinked_IsSelected(new EntityModel());
        getUnlinked_IsSelected().getEntityChangedEvent().addListener(this);

        setPlugged(new EntityModel());
        getPlugged().getPropertyChangedEvent().addListener(this);

        setPlugged_IsSelected(new EntityModel());
        getPlugged_IsSelected().getEntityChangedEvent().addListener(this);

        setUnplugged_IsSelected(new EntityModel());
        getUnplugged_IsSelected().getEntityChangedEvent().addListener(this);

        setCustomPropertySheet(new KeyValueModel());
    }

    protected abstract void init();

    public EntityModel getSourceModel() {
        return sourceModel;
    }

    public VmBase getVm() {
        return vm;
    }

    public ArrayList<VmNetworkInterface> getVmNicList() {
        return vmNicList;
    }

    public Version getClusterCompatibilityVersion() {
        return clusterCompatibilityVersion;
    }

    public EntityModel getName()
    {
        return privateName;
    }

    private void setName(EntityModel value)
    {
        privateName = value;
    }

    public ListModel getNetwork()
    {
        return privateNetwork;
    }

    private void setNetwork(ListModel value)
    {
        privateNetwork = value;
    }

    public EntityModel getLinked()
    {
        return linked;
    }

    private void setLinked(EntityModel value)
    {
        linked = value;
    }

    public EntityModel getLinked_IsSelected()
    {
        return linked_IsSelected;
    }

    public void setLinked_IsSelected(EntityModel value)
    {
        linked_IsSelected = value;
    }

    public EntityModel getUnlinked_IsSelected()
    {
        return unlinked_IsSelected;
    }

    public void setUnlinked_IsSelected(EntityModel value)
    {
        unlinked_IsSelected = value;
    }

    public ListModel getNicType()
    {
        return privateNicType;
    }

    private void setNicType(ListModel value)
    {
        privateNicType = value;
    }

    public EntityModel getPortMirroring()
    {
        return privatePortMirroring;
    }

    public void setPortMirroring(EntityModel value)
    {
        privatePortMirroring = value;
    }

    public EntityModel getMAC()
    {
        return privateMAC;
    }

    private void setMAC(EntityModel value)
    {
        privateMAC = value;
    }

    public EntityModel getEnableMac()
    {
        return enableMac;
    }

    private void setEnableMac(EntityModel value)
    {
        enableMac = value;
    }

    public EntityModel getPlugged()
    {
        return plugged;
    }

    private void setPlugged(EntityModel value)
    {
        plugged = value;
    }

    public EntityModel getPlugged_IsSelected()
    {
        return plugged_IsSelected;
    }

    public void setPlugged_IsSelected(EntityModel value)
    {
        plugged_IsSelected = value;
    }

    public EntityModel getUnplugged_IsSelected()
    {
        return unplugged_IsSelected;
    }

    public void setUnplugged_IsSelected(EntityModel value)
    {
        unplugged_IsSelected = value;
    }

    public KeyValueModel getCustomPropertySheet() {
        return customPropertySheet;
    }

    public void setCustomPropertySheet(KeyValueModel customPropertySheet) {
        this.customPropertySheet = customPropertySheet;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (sender == getMAC())
        {
            mAC_PropertyChanged((PropertyChangedEventArgs) args);
        }

        else if (sender == getPlugged())
        {
            PropertyChangedEventArgs propArgs = (PropertyChangedEventArgs) args;
            if (propArgs.PropertyName.equals("Entity")) { //$NON-NLS-1$
                boolean plugged = (Boolean) getPlugged().getEntity();
                getPlugged_IsSelected().setEntity(plugged);
                getUnplugged_IsSelected().setEntity(!plugged);
            } else if (propArgs.PropertyName.equals("IsChangable")) { //$NON-NLS-1$

                boolean isPlugChangeable = getPlugged().getIsChangable();

                getPlugged_IsSelected().setChangeProhibitionReason(getLinked().getChangeProhibitionReason());
                getPlugged_IsSelected().setIsChangable(isPlugChangeable);

                getUnplugged_IsSelected().setChangeProhibitionReason(getLinked().getChangeProhibitionReason());
                getUnplugged_IsSelected().setIsChangable(isPlugChangeable);
            } else if (propArgs.PropertyName.equals("IsAvailable")) { //$NON-NLS-1$
                boolean isPlugAvailable = getPlugged().getIsAvailable();
                getPlugged_IsSelected().setIsAvailable(isPlugAvailable);
                getUnplugged_IsSelected().setIsAvailable(isPlugAvailable);
            }
        }
        else if (sender == getPlugged_IsSelected())
        {
            if ((Boolean) getPlugged_IsSelected().getEntity()) {
                getPlugged().setEntity(true);
            }
        }
        else if (sender == getUnplugged_IsSelected())
        {
            if ((Boolean) getUnplugged_IsSelected().getEntity()) {
                getPlugged().setEntity(false);
            }
        }

        else if (sender == getLinked())
        {
            PropertyChangedEventArgs propArgs = (PropertyChangedEventArgs) args;
            if (propArgs.PropertyName.equals("Entity")) { //$NON-NLS-1$
                boolean linked = (Boolean) getLinked().getEntity();
                getLinked_IsSelected().setEntity(linked);
                getUnlinked_IsSelected().setEntity(!linked);
            } else if (propArgs.PropertyName.equals("IsChangable")) { //$NON-NLS-1$
                boolean isLinkedChangeable = getLinked().getIsChangable();

                getLinked_IsSelected().setChangeProhibitionReason(getChangeProhibitionReason());
                getLinked_IsSelected().setIsChangable(isLinkedChangeable);

                getUnlinked_IsSelected().setChangeProhibitionReason(getLinked().getChangeProhibitionReason());
                getUnlinked_IsSelected().setIsChangable(isLinkedChangeable);
            } else if (propArgs.PropertyName.equals("IsAvailable")) { //$NON-NLS-1$
                boolean isLinkedAvailable = getLinked().getIsAvailable();
                getLinked_IsSelected().setIsAvailable(isLinkedAvailable);
                getUnlinked_IsSelected().setIsAvailable(isLinkedAvailable);
            }
        }
        else if (sender == getLinked_IsSelected())
        {
            if ((Boolean) getLinked_IsSelected().getEntity()) {
                getLinked().setEntity(true);
            }
        }
        else if (sender == getUnlinked_IsSelected())
        {
            if ((Boolean) getUnlinked_IsSelected().getEntity()) {
                getLinked().setEntity(false);
            }
        }
    }

    private void mAC_PropertyChanged(PropertyChangedEventArgs e)
    {
        if (e.PropertyName.equals("IsChangeAllowed") && !getMAC().getIsChangable()) //$NON-NLS-1$
        {
            getMAC().setIsValid(true);
        }
    }

    public boolean validate()
    {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new I18NNameValidation() });

        getNicType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getMAC().setIsValid(true);
        if (getMAC().getIsChangable())
        {
            getMAC().validateEntity(new IValidation[] { new NotEmptyValidation(), new MacAddressValidation() });
        }

        return getName().getIsValid() && getNetwork().getIsValid() && getNicType().getIsValid()
                && getMAC().getIsValid() && getCustomPropertySheet().validate();
    }

    protected abstract VmNetworkInterface createBaseNic();

    private void onSave()
    {
        VmNetworkInterface nic = createBaseNic();

        if (getProgress() != null)
        {
            return;
        }

        if (!validate())
        {
            return;
        }

        // Save changes.
        nic.setName((String) getName().getEntity());
        Network network = (Network) getNetwork().getSelectedItem();
        nic.setNetworkName(network != null ? network.getName() : null);
        nic.setLinked((Boolean) getLinked().getEntity());
        onSavePortMirroring(nic);
        if (getNicType().getSelectedItem() == null)
        {
            nic.setType(null);
        }
        else
        {
            nic.setType(((VmInterfaceType) getNicType().getSelectedItem()).getValue());
        }
        onSaveMAC(nic);

        nic.setPlugged((Boolean) getPlugged().getEntity());

        nic.setCustomProperties(KeyValueModel
                .convertProperties(getCustomPropertySheet().getEntity()));

        startProgress(null);

        Frontend.RunAction(getVdcActionType(),
                createVdcActionParameters(nic),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        VdcReturnValueBase returnValue = result.getReturnValue();
                        stopProgress();

                        if (returnValue != null && returnValue.getSucceeded())
                        {
                            cancel();
                            postOnSave();
                        }
                    }
                },
                this);
    }

    protected void postOnSave()
    {
        // Do nothing
    }

    protected void cancel()
    {
        sourceModel.setWindow(null);
    }

    protected abstract String getDefaultMacAddress();

    protected abstract VdcActionType getVdcActionType();

    protected void initNetworks() {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model1, Object result1)
            {
                ArrayList<Network> networks = new ArrayList<Network>();
                for (Network a : (ArrayList<Network>) result1)
                {
                    if (a.isVmNetwork())
                    {
                        networks.add(a);
                    }
                }
                if (hotUpdateSupported) {
                    networks.add(null);
                }

                Collections.sort(networks, new Comparator<Network>() {

                    private final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

                    @Override
                    public int compare(Network net1, Network net2) {
                        if (net1 == null) {
                            return net2 == null ? 0 : 1;
                        } else if (net2 == null) {
                            return -1;
                        }
                        return lexoNumeric.compare(net1.getName(), net2.getName());
                    }

                });

                getNetwork().setItems(networks);
                initSelectedNetwork();

                // fetch completed
                okCommand.setIsExecutionAllowed(true);
            }
        };
        AsyncDataProvider.getClusterNetworkList(_asyncQuery, getVm().getVdsGroupId());
    }

    protected void initCommands() {
        okCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        // wait for data to fetch
        okCommand.setIsExecutionAllowed(false);
        getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        getCommands().add(cancelCommand);
    }

    protected abstract void initSelectedNetwork();

    protected abstract void initSelectedType();

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            onSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
    }

    protected abstract void initMAC();

    protected abstract void initPortMirroring();

    protected abstract void initLinked();

    protected void onSaveMAC(VmNetworkInterface nicToSave) {
        nicToSave.setMacAddress(getMAC().getIsChangable() ? (getMAC().getEntity() == null ? null
                : ((String) (getMAC().getEntity())).toLowerCase()) : getDefaultMacAddress());
    }

    protected void onSavePortMirroring(VmNetworkInterface nicToSave) {
        nicToSave.setPortMirroring((Boolean) getPortMirroring().getEntity());
    }

    protected abstract VdcActionParametersBase createVdcActionParameters(VmNetworkInterface nicToSave);

    protected boolean isPortMirroringSupported() {
        Version v31 = new Version(3, 1);
        boolean isLessThan31 = getClusterCompatibilityVersion().compareTo(v31) < 0;

        return !isLessThan31;
    }

    protected void updateLinkChangability() {
        boolean isNullNetworkSelected = getNetwork().getSelectedItem() == null;

        if (isNullNetworkSelected) {
            getLinked().setIsChangable(false);
            return;
        }
        if (!hotUpdateSupported) {
            getLinked().setIsChangable(false);
            return;
        }
        getLinked().setIsChangable(true);
    }

    protected void updateNetworkChangability() {
        getNetwork().setIsChangable(true);
    }

    protected void initCustomPropertySheet() {
        GetDeviceCustomPropertiesParameters params = new GetDeviceCustomPropertiesParameters();
        params.setVersion(getClusterCompatibilityVersion());
        params.setDeviceType(VmDeviceGeneralType.INTERFACE);
        Frontend.RunQuery(VdcQueryType.GetDeviceCustomProperties,
                params,
                new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {
                                if (returnValue != null) {
                                    Map<String, String> customPropertiesList =
                                            ((Map<String, String>) ((VdcQueryReturnValue) returnValue).getReturnValue());

                                    List<String> lines = new ArrayList<String>();

                                    for (Map.Entry<String, String> keyValue : customPropertiesList.entrySet()) {
                                        lines.add(keyValue.getKey() + '=' + keyValue.getValue());
                                    }
                                    getCustomPropertySheet().setKeyValueString(lines);
                                    getCustomPropertySheet().setIsChangable(!lines.isEmpty());

                                    setCustomPropertyFromVm();
                                }
                            }
                        }));
    }

    protected abstract void setCustomPropertyFromVm();
}
