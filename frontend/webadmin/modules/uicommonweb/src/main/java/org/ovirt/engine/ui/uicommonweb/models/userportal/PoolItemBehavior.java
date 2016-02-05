package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class PoolItemBehavior extends ItemBehavior {

    // this has to be static because in every request a new instance of this class is created
    private static Map<Guid, Integer> poolToOsType = new HashMap<>();

    private VM poolRepresentant;

    /**
     * @see UserPortalItemModel#UserPortalItemModel(Object, VmConsoles, VM)
     */
    public PoolItemBehavior(UserPortalItemModel item, VM poolRepresentant) {
        super(item);
        this.poolRepresentant = poolRepresentant;
    }

    @Override
    public void onEntityChanged() {
        updateProperties();
        updateActionAvailability();
    }

    @Override
    public void entityPropertyChanged(PropertyChangedEventArgs e) {
    }

    @Override
    public void executeCommand(UICommand command) {
        if (command == getItem().getTakeVmCommand()) {
            takeVm();
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        // Do nothing. There are no events to handle for pools.
    }

    private void takeVm() {
        VmPool entity = (VmPool) getItem().getEntity();

        VmPoolUserParameters params = new VmPoolUserParameters(entity.getVmPoolId(),
                Frontend.getInstance().getLoggedInUser().getId(),
                false);

        Frontend.getInstance().runAction(VdcActionType.AttachUserToVmFromPoolAndRun, params,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        if (!result.getReturnValue().getSucceeded()) {
                            return;
                        }
                    }
                }, this);
    }

    private void updateProperties() {
        VmPool entity = (VmPool) getItem().getEntity();

        getItem().setName(entity.getName());
        getItem().setDescription(entity.getVmPoolDescription());
        getItem().setIsPool(true);
        getItem().setIsServer(false);
        getItem().setStatus(VMStatus.Down);
        getItem().setIsFromPool(false);
        getItem().setPoolType(entity.getVmPoolType());
        if (poolToOsType.containsKey(entity.getVmPoolId())) {
            getItem().setOsId(poolToOsType.get(entity.getVmPoolId()));
        }

        if (poolRepresentant != null) {
            updatePropertiesFromPoolRepresentant(poolRepresentant);
            poolRepresentant = null;
        } else {
            Frontend.getInstance().runQuery(VdcQueryType.GetVmDataByPoolId,
                    new IdQueryParameters(entity.getVmPoolId()),
                    new AsyncQuery(this, new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            if (returnValue != null) {
                                VM vm = ((VdcQueryReturnValue) returnValue).getReturnValue();
                                if (vm == null) {
                                    return;
                                }
                                updatePropertiesFromPoolRepresentant(vm);
                            }
                        }
                    }));
        }
    }

    private void updatePropertiesFromPoolRepresentant(VM poolRepresentant) {
        UserPortalItemModel model = getItem();
        model.setOsId(poolRepresentant.getVmOsId());
        model.setSmallIconId(poolRepresentant.getStaticData().getSmallIconId());
        model.setLargeIconId(poolRepresentant.getStaticData().getLargeIconId());
        poolToOsType.put(((VmPool) model.getEntity()).getVmPoolId(), poolRepresentant.getVmOsId());
    }

    private void updateActionAvailability() {
        getItem().getTakeVmCommand().setIsAvailable(true);

        getItem().getRunCommand().setIsAvailable(false);
        getItem().getPauseCommand().setIsAvailable(true);
        getItem().getShutdownCommand().setIsAvailable(true);
        getItem().getStopCommand().setIsAvailable(true);
        getItem().getReturnVmCommand().setIsAvailable(false);

        getItem().getPauseCommand().setIsExecutionAllowed(false);
        getItem().getShutdownCommand().setIsExecutionAllowed(false);
        getItem().getStopCommand().setIsExecutionAllowed(false);
        getItem().getRebootCommand().setIsExecutionAllowed(false);
    }
}
