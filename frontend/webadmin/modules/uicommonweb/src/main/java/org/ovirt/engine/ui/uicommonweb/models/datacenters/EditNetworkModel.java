package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.EditVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NewVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class EditNetworkModel extends NetworkModel {

    private List<VnicProfileModel> originalProfileModels = new ArrayList<VnicProfileModel>();

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
        getComment().setEntity(getNetwork().getComment());
        getIsStpEnabled().setEntity(getNetwork().getStp());
        getHasVLanTag().setEntity(getNetwork().getVlanId() != null);
        getVLanTag().setEntity((getNetwork().getVlanId() == null ? Integer.valueOf(0) : getNetwork().getVlanId()));
        initMtu();
        initIsVm();
        getExport().setEntity(getNetwork().isExternal());
        getExport().setIsChangable(false);
        getExternalProviders().setIsChangable(false);
        getNetworkLabel().setIsChangable(false);
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
    protected void initProfiles() {
        AsyncQuery profilesQuery = new AsyncQuery();
        profilesQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<VnicProfileModel> profilesModels = new LinkedList<VnicProfileModel>();
                for (VnicProfileView profileView : (List<VnicProfileView>) returnValue) {
                    VnicProfileModel editModel = new EditVnicProfileModel(getSourceListModel(),
                            getSelectedDc().getcompatibility_version(),
                            profileView, null, false);

                    if (profileView.getNetworkQosName() != null && !profileView.getNetworkQosName().equals("")) { //$NON-NLS-1$
                        NetworkQoS networkQoS = new NetworkQoS();
                        networkQoS.setName(profileView.getNetworkQosName());
                        editModel.getNetworkQoS().setSelectedItem(networkQoS);
                    }
                    editModel.getNetworkQoS().setIsChangable(false);
                    profilesModels.add(editModel);
                    editModel.getName().setIsChangable(false);
                }
                if (profilesModels.isEmpty()){
                    VnicProfileModel newProfileModel = new NewVnicProfileModel(getSourceListModel(),
                            getSelectedDc().getcompatibility_version(), false, getSelectedDc().getId());
                    profilesModels.add(newProfileModel);
                }
                getProfiles().setItems(profilesModels);
                originalProfileModels = profilesModels;
            }
        };
        AsyncDataProvider.getVnicProfilesByNetworkId(profilesQuery, getNetwork().getId());
    }

    @Override
    protected void onExportChanged() {
        if ((Boolean) getExport().getEntity()) {
            getHasVLanTag().setIsChangable(false);
            getVLanTag().setIsChangable(false);
            getIsVmNetwork().setIsChangable(false);
            getHasMtu().setIsChangable(false);
            getMtu().setIsChangable(false);
        }
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

    @Override
    protected void performProfilesActions(final Guid networkGuid) {

        List<VnicProfileModel> profileModels = (List<VnicProfileModel>) getProfiles().getItems();
        List<VnicProfileModel> profileModelsToRemove = new ArrayList<VnicProfileModel>(originalProfileModels);
        profileModelsToRemove.removeAll(profileModels);

        final List<VnicProfileModel> profileModelsToAdd = new ArrayList<VnicProfileModel>();

        for (VnicProfileModel profileModel : profileModels) {
            if (profileModel instanceof NewVnicProfileModel) {
                profileModelsToAdd.add(profileModel);
            }
        }
        startProgress(null);

        performVnicProfileAction(VdcActionType.RemoveVnicProfile,
                profileModelsToRemove,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        performVnicProfileAction(VdcActionType.AddVnicProfile,
                                profileModelsToAdd,
                                new IFrontendMultipleActionAsyncCallback() {

                                    @Override
                                    public void executed(FrontendMultipleActionAsyncResult result) {
                                        stopProgress();
                                        cancel();
                                    }
                                }, networkGuid);
                    }
                }, networkGuid);
    }

}
