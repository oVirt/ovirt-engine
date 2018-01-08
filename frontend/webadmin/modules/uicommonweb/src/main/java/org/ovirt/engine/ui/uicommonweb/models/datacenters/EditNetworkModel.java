package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.Objects;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditNetworkModel extends NetworkModel {

    private final boolean originallyVmNetwork;
    private boolean management;

    public EditNetworkModel(Network network, SearchableListModel<?, ? extends Network> sourceListModel) {
        super(network, sourceListModel);
        originallyVmNetwork = network.isVmNetwork();
        getDataCenters().setIsChangeable(false);
        init();
        initManagement();
    }

    private void init() {
        setTitle(ConstantsManager.getInstance().getConstants().editLogicalNetworkTitle());
        setHelpTag(HelpTag.edit_logical_network);
        setHashName("edit_logical_network"); //$NON-NLS-1$
        getName().setEntity(getNetwork().getName());
        getDescription().setEntity(getNetwork().getDescription());
        getComment().setEntity(getNetwork().getComment());
        getIsStpEnabled().setEntity(getNetwork().getStp());
        getHasVLanTag().setEntity(getNetwork().getVlanId() != null);
        getVLanTag().setEntity(getNetwork().getVlanId());
        initMtu();
        initIsVm();
        getExternal().setEntity(getNetwork().isExternal());
        getExternal().setIsChangeable(false);
        getExternalProviders().setIsChangeable(false);

        getConnectedToPhysicalNetwork().setEntity(isConnectedToPhysicalNetwork());
        getConnectedToPhysicalNetwork().setIsChangeable(false);
        getUsePhysicalNetworkFromDatacenter().setIsChangeable(false);
        getUsePhysicalNetworkFromCustom().setIsChangeable(false);
        getDatacenterPhysicalNetwork().setIsChangeable(false);

        if (isConnectedToPhysicalNetwork()) {
            if (getNetwork().getProvidedBy().isSetPhysicalNetworkId()) {
                getUsePhysicalNetworkFromDatacenter().setEntity(true);
            } else {
                getUsePhysicalNetworkFromCustom().setEntity(true);
                getCustomPhysicalNetwork().setEntity(getNetwork().getLabel());
            }
        } else {
            getNetworkLabel().setSelectedItem(getNetwork().getLabel());
        }

        toggleProfilesAvailability();
    }

    private void initManagement() {
        AsyncDataProvider.getInstance().isManagementNetwork(new AsyncQuery<>(returnValue -> management = returnValue), getNetwork().getId());
    }

    @Override
    protected void initIsVm() {
        getIsVmNetwork().setEntity(getNetwork().isVmNetwork());
    }

    @Override
    protected void initMtu() {
        boolean isCustomMtu = getNetwork().getMtu() != 0;
        getMtuSelector().setSelectedItem(isCustomMtu ? MtuSelector.customMtu : MtuSelector.defaultMtu);
        getMtu().setEntity(isCustomMtu() ? getNetwork().getMtu() : null);
    }

    @Override
    protected void selectExternalProvider() {
        final Network network = getNetwork();
        getExternalProviders().setSelectedItem(Linq.firstOrNull(getExternalProviders().getItems(),
                new Linq.NetworkSameProviderPredicate(network)));
    }

    @Override
    protected void selectPhysicalDatacenterNetwork() {
        final Network network = getNetwork();
        if (network.isExternal() && network.getProvidedBy().isSetPhysicalNetworkId()) {
            getDatacenterPhysicalNetwork().getItems()
                    .stream()
                    .filter(net -> Objects.equals(net.getId(), network.getProvidedBy().getPhysicalNetworkId()))
                    .findAny()
                    .ifPresent(this.getDatacenterPhysicalNetwork()::setSelectedItem);
        }
    }

    private boolean isConnectedToPhysicalNetwork() {
        final Network network = getNetwork();
        return network.isExternal() && (network.getProvidedBy().isSetPhysicalNetworkId() || StringHelper.isNotNullOrEmpty(network.getLabel()));
    }

    @Override
    protected void onExportChanged() {
        super.onExportChanged();
        if (getExternal().getEntity()) {
            getHasVLanTag().setIsChangeable(false);
            getVLanTag().setIsChangeable(false);
            getIsVmNetwork().setIsChangeable(false);
            getNetworkLabel().setIsChangeable(false);
            getCustomPhysicalNetwork().setIsChangeable(false);
            getConnectedToPhysicalNetwork().setIsChangeable(false);
            getUsePhysicalNetworkFromDatacenter().setIsChangeable(false);
            getUsePhysicalNetworkFromCustom().setIsChangeable(false);
            getDatacenterPhysicalNetwork().setIsChangeable(false);
        }
    }

    @Override
    public void executeSave() {
        Frontend.getInstance().runAction(ActionType.UpdateNetwork,
                new AddNetworkStoragePoolParameters(getSelectedDc().getId(), getNetwork()),
                result -> {
                    ActionReturnValue retVal = result.getReturnValue();
                    postSaveAction(null,
                            retVal != null && retVal.getSucceeded());

                },
                null);
    }

    @Override
    protected void toggleProfilesAvailability() {
        getProfiles().setIsAvailable(getIsVmNetwork().getEntity() && !originallyVmNetwork);
    }

    @Override
    protected boolean isManagement() {
        return management;
    }

}
