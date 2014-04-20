package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class VmInterfaceCreatingManager {

    private VmInterfaceType defaultType;
    private PostVnicCreatedCallback callback;

    public VmInterfaceCreatingManager(PostVnicCreatedCallback callback) {
        this.callback = callback;
    }

    public PostVnicCreatedCallback getCallback() {
        return callback;
    }

    public static interface PostVnicCreatedCallback {
        void vnicCreated(Guid vmId);

        void queryFailed();
    }

    public void updateVnics(final Guid vmId,
            final Iterable<VnicInstanceType> vnicsWithProfiles,
            final UnitVmModel unitVmModel) {

        final AsyncQuery getVmNicsQuery = new AsyncQuery();
        getVmNicsQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                Iterable<VmNetworkInterface> existingVnics = (Iterable<VmNetworkInterface>) result;
                if (existingVnics == null) {
                    existingVnics = new ArrayList<VmNetworkInterface>();
                }

                Map<String, VmNetworkInterface> existingVnicForName = new HashMap<String, VmNetworkInterface>();
                for (VmNetworkInterface vnic : existingVnics) {
                    existingVnicForName.put(vnic.getName(), vnic);
                }

                final ArrayList<VdcActionParametersBase> createVnicParameters =
                        new ArrayList<VdcActionParametersBase>();
                final ArrayList<VdcActionParametersBase> updateVnicParameters =
                        new ArrayList<VdcActionParametersBase>();
                final ArrayList<VdcActionParametersBase> removeVnicParameters =
                        new ArrayList<VdcActionParametersBase>();
                final Set<String> vnicsEncountered = new HashSet<String>();

                // iterate over edited VNICs, see if any need to be added or have been assigned a different profile
                for (VnicInstanceType vnicWithProfile : vnicsWithProfiles) {
                    VmNetworkInterface editedVnic = vnicWithProfile.getNetworkInterface();
                    String vnicName = editedVnic.getName();
                    VmNetworkInterface existingVnic = existingVnicForName.get(vnicName);
                    if (existingVnic == null) {
                        editedVnic.setType(defaultType == null ? null : defaultType.getValue());
                        createVnicParameters.add(new AddVmInterfaceParameters(vmId, editedVnic));
                    } else {
                        vnicsEncountered.add(vnicName);
                        Guid existingProfileId = existingVnic.getVnicProfileId();
                        Guid editedProfileId = editedVnic.getVnicProfileId();
                        if ((editedProfileId == null && existingProfileId != null)
                                || (editedProfileId != null && !editedProfileId.equals(existingProfileId))) {
                            existingVnic.setVnicProfileId(editedProfileId);
                            existingVnic.setNetworkName(editedVnic.getNetworkName());
                            updateVnicParameters.add(new AddVmInterfaceParameters(vmId, existingVnic));
                        }
                    }
                }

                // iterate over existing VNICs, see if any have not been encountered and thus removed in editing
                for (VmNetworkInterface existingVnic : existingVnics) {
                    if (!vnicsEncountered.contains(existingVnic.getName())) {
                        removeVnicParameters.add(new RemoveVmInterfaceParameters(vmId, existingVnic.getId()));
                    }
                }

                Frontend.getInstance().runMultipleActions(VdcActionType.AddVmInterface,
                        createVnicParameters,
                        new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                                Frontend.getInstance().runMultipleActions(VdcActionType.UpdateVmInterface,
                                        updateVnicParameters,
                                        new IFrontendActionAsyncCallback() {

                            @Override
                            public void executed(FrontendActionAsyncResult result) {
                                                Frontend.getInstance().runMultipleActions(VdcActionType.RemoveVmInterface,
                                                        removeVnicParameters,
                                                        new IFrontendActionAsyncCallback() {

                                    @Override
                                    public void executed(FrontendActionAsyncResult result) {
                                        if (unitVmModel.getIsNew()) {
                                            VmOperationParameterBase reorderParams = new VmOperationParameterBase(vmId);
                                            Frontend.getInstance().runAction(VdcActionType.ReorderVmNics, reorderParams, new IFrontendActionAsyncCallback() {
                                                public void executed(FrontendActionAsyncResult result) {
                                                    callback.vnicCreated(vmId);
                                                }
                                            });
                                        } else {
                                            callback.vnicCreated(vmId);
                                        }
                                    }
                                }, this);
                            }
                        }, this);
                    }
                }, this);
            }
        };

        AsyncQuery osInfoQuery = new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                defaultType = AsyncDataProvider.getDefaultNicType((Collection<VmInterfaceType>) returnValue);
                AsyncDataProvider.getVmNicList(getVmNicsQuery, vmId);
            }
        });
        AsyncDataProvider.getNicTypeList(unitVmModel.getOSType().getSelectedItem(),
                unitVmModel.getDataCenterWithClustersList().getSelectedItem().getCluster().getcompatibility_version(),
                osInfoQuery);
    }

}
