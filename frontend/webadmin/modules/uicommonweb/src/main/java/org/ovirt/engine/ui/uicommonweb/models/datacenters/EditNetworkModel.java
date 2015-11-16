package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class EditNetworkModel extends NetworkModel {

    private final boolean originallyVmNetwork;
    private boolean management;

    public EditNetworkModel(Network network, ListModel sourceListModel) {
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
        getExport().setEntity(getNetwork().isExternal());
        getExport().setIsChangeable(false);
        getExternalProviders().setIsChangeable(false);

        if (getNetwork().isExternal()) {
            getNeutronPhysicalNetwork().setEntity(getNetwork().getLabel());
        } else {
            getNetworkLabel().setSelectedItem(getNetwork().getLabel());
        }

        toggleProfilesAvailability();
    }

    private void initManagement() {
        final AsyncQuery callback = new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                management = (Boolean) returnValue;
            }
        });
        AsyncDataProvider.getInstance().isManagementNetwork(callback, getNetwork().getId());
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
    protected void onExportChanged() {
        super.onExportChanged();
        if (getExport().getEntity()) {
            getHasVLanTag().setIsChangeable(false);
            getVLanTag().setIsChangeable(false);
            getIsVmNetwork().setIsChangeable(false);
            getNetworkLabel().setIsChangeable(false);
            getNeutronPhysicalNetwork().setIsChangeable(false);
        }
    }

    @Override
    public void executeSave() {
        Frontend.getInstance().runAction(VdcActionType.UpdateNetwork,
                new AddNetworkStoragePoolParameters(getSelectedDc().getId(), getNetwork()),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        VdcReturnValueBase retVal = result.getReturnValue();
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

    @Override
    protected boolean isManagement() {
        return management;
    }

}
