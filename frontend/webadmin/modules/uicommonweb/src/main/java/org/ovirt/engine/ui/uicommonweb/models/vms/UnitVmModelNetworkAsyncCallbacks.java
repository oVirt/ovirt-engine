package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class UnitVmModelNetworkAsyncCallbacks {

    public static abstract class BaseNetworkFrontendActionAsyncCallback implements IFrontendActionAsyncCallback {

        private final UnitVmModel unitVmModel;
        protected final VmInterfaceCreatingManager networkCreatingManager;

        public BaseNetworkFrontendActionAsyncCallback(final UnitVmModel unitVmModel,
                                                      final VmInterfaceCreatingManager networkCreatingManager) {
            this.unitVmModel = unitVmModel;
            this.networkCreatingManager = networkCreatingManager;
        }

        @Override
        public void executed(FrontendActionAsyncResult result) {
            VdcReturnValueBase returnValue = result.getReturnValue();
            if (returnValue != null && returnValue.getSucceeded()) {
                doNetworkOperation(returnValue,
                        (List<VnicInstanceType>) unitVmModel.getNicsWithLogicalNetworks().getItems()
                );
            } else {
                networkCreatingManager.getCallback().queryFailed();
            }
        }

        protected  abstract void doNetworkOperation(VdcReturnValueBase returnValue, List<VnicInstanceType> nicWithLogicalNetworks);
    }

    public static class NetworkCreateOrUpdateFrontendActionAsyncCallback extends BaseNetworkFrontendActionAsyncCallback {

        public NetworkCreateOrUpdateFrontendActionAsyncCallback(final UnitVmModel unitVmModel,
                                                                final VmInterfaceCreatingManager networkCreatingManager) {
            super(unitVmModel, networkCreatingManager);
        }

        @Override
        protected void doNetworkOperation(VdcReturnValueBase returnValue, List<VnicInstanceType> nicWithLogicalNetworks) {
            networkCreatingManager.updateOrCreateIfNothingToUpdate((Guid) returnValue.getActionReturnValue(), nicWithLogicalNetworks);
        }

    }

    public static class NetworkCreateFrontendAsyncCallback extends BaseNetworkFrontendActionAsyncCallback {
        public NetworkCreateFrontendAsyncCallback(final UnitVmModel unitVmModel,
                                                  final VmInterfaceCreatingManager networkCreatingManager) {
            super(unitVmModel, networkCreatingManager);
        }

        @Override
        protected void doNetworkOperation(VdcReturnValueBase returnValue, List<VnicInstanceType> nicWithLogicalNetworks) {
            networkCreatingManager.createVnics((Guid) returnValue.getActionReturnValue(), nicWithLogicalNetworks);
        }
    }

    public static class NetworkUpdateFrontendAsyncCallback extends BaseNetworkFrontendActionAsyncCallback {

        private final Guid idToUpdate;

        public NetworkUpdateFrontendAsyncCallback(final UnitVmModel unitVmModel,
                                                  final VmInterfaceCreatingManager networkCreatingManager,
                                                  final Guid idToUpdate) {
            super(unitVmModel, networkCreatingManager);
            this.idToUpdate = idToUpdate;
        }

        @Override
        protected void doNetworkOperation(VdcReturnValueBase returnValue, List<VnicInstanceType> nicWithLogicalNetworks) {
            networkCreatingManager.updateVnics(idToUpdate, nicWithLogicalNetworks);
        }
    }

}
