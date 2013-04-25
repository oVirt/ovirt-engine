package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class EditNetworkModel extends NetworkModel {

    public EditNetworkModel(Network network, ListModel sourceListModel) {
        super(network, sourceListModel);
        getDataCenters().setIsChangable(false);
        init();
    }

    private void init() {
        setTitle(ConstantsManager.getInstance().getConstants().editLogicalNetworkTitle());
        setHashName("edit_logical_network"); //$NON-NLS-1$
        getName().setEntity(getNetwork().getName());
        if (isManagemet()) {
            getName().setIsChangable(false);
        }
        getDescription().setEntity(getNetwork().getDescription());
        getIsStpEnabled().setEntity(getNetwork().getStp());
        getHasVLanTag().setEntity(getNetwork().getVlanId() != null);
        getVLanTag().setEntity((getNetwork().getVlanId() == null ? Integer.valueOf(0) : getNetwork().getVlanId()));
        initMtu();
        initIsVm();
    }

    @Override
    public void syncWithBackend() {
        super.syncWithBackend();
        if (firstInit) {
            firstInit = false;
            addCommands();
        }
    }

    @Override
    protected void initIsVm() {
        getIsVmNetwork().setEntity(getNetwork().isVmNetwork());
    }

    @Override
    protected void initMtu() {
        getHasMtu().setEntity(getNetwork().getMtu() != 0);
        getMtu().setEntity(getNetwork().getMtu() != 0 ? String.valueOf(getNetwork().getMtu()) : null);
    }

    @Override
    public void executeSave() {
        Frontend.RunAction(VdcActionType.UpdateNetwork,
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
}
