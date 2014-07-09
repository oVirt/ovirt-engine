package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class EditNetworkModel extends NetworkModel {

    private final boolean originallyVmNetwork;

    public EditNetworkModel(Network network, ListModel sourceListModel) {
        super(network, sourceListModel);
        originallyVmNetwork = network.isVmNetwork();
        getDataCenters().setIsChangable(false);
        init();
    }

    private void init() {
        setTitle(ConstantsManager.getInstance().getConstants().editLogicalNetworkTitle());
        setHelpTag(HelpTag.edit_logical_network);
        setHashName("edit_logical_network"); //$NON-NLS-1$
        getName().setEntity(getNetwork().getName());
        if (isManagemet()) {
            getName().setIsChangable(false);
        }
        getDescription().setEntity(getNetwork().getDescription());
        getComment().setEntity(getNetwork().getComment());
        getIsStpEnabled().setEntity(getNetwork().getStp());
        getHasVLanTag().setEntity(getNetwork().getVlanId() != null);
        getVLanTag().setEntity((getNetwork().getVlanId() == null ? Integer.valueOf(0) : getNetwork().getVlanId()));
        initMtu();
        initIsVm();
        getExport().setEntity(getNetwork().isExternal());
        getExport().setIsChangable(false);
        getExternalProviders().setIsChangable(false);
        getNetworkLabel().setSelectedItem(getNetwork().getLabel());
        toggleProfilesAvailability();
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
        getExternalProviders().setSelectedItem(Linq.firstOrDefault(getExternalProviders().getItems(),
                new Linq.NetworkSameProviderPredicate(network)));
    }

    @Override
    protected void onExportChanged() {
        if (getExport().getEntity()) {
            getHasVLanTag().setIsChangable(false);
            getVLanTag().setIsChangable(false);
            getIsVmNetwork().setIsChangable(false);
            getNetworkLabel().setIsChangable(false);
        }
        super.onExportChanged();
    }

    @Override
    public void executeSave() {
        Frontend.getInstance().runAction(VdcActionType.UpdateNetwork,
                new AddNetworkStoragePoolParameters(getSelectedDc().getId(), getNetwork()),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result1) {
                        VdcReturnValueBase retVal = result1.getReturnValue();
                        postSaveAction(null,
                                retVal != null && retVal.getSucceeded());

                    }
                },
                null);
    }

    @Override
    protected void toggleProfilesAvailability() {
        getProfiles().setIsAvailable(getIsVmNetwork().getEntity() && !originallyVmNetwork);
    }
}
