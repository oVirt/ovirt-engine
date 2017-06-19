package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Objects;

import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.ChangeCDModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VmItemBehavior extends ItemBehavior {
    public VmItemBehavior(UserPortalItemModel item) {
        super(item);
    }

    @Override
    public void onEntityChanged() {
        updateProperties();
        updateActionAvailability();
    }

    @Override
    public void entityPropertyChanged(PropertyChangedEventArgs e) {
        updateProperties();
        if (e.propertyName.equals("status")) { //$NON-NLS-1$
            updateActionAvailability();
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        if (command == getItem().getRunCommand()) {
            run();
        }
        else if (command == getItem().getPauseCommand()) {
            pause();
        }
        else if (command == getItem().getStopCommand()) {
            stop();
        }
        else if (command == getItem().getShutdownCommand()) {
            shutdown();
        }
        else if (command == getItem().getReturnVmCommand()) {
            returnVm();
        }
        else if (command == getItem().getRebootCommand()) {
            reboot();
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        if (ev.matchesDefinition(ChangeCDModel.executedEventDefinition)) {
            changeCD(sender, args);
        }
    }

    private void changeCD(Object sender, EventArgs args) {
        VM entity = (VM) getItem().getEntity();
        ChangeCDModel model = (ChangeCDModel) sender;

        // TODO: Patch!
        String imageName = model.getTitle();
        if (Objects.equals(imageName, ConstantsManager.getInstance()
                .getConstants()
                .noCds())) {
            return;
        }

        Frontend.getInstance().runAction(ActionType.ChangeDisk,
                new ChangeDiskCommandParameters(entity.getId(), Objects.equals(imageName, ConsoleModel.getEjectLabel()) ? "" : imageName)); //$NON-NLS-1$
    }

    private void returnVm() {
        VM entity = (VM) getItem().getEntity();

        Frontend.getInstance().runAction(ActionType.ShutdownVm, new ShutdownVmParameters(entity.getId(), false),
                null, null);
    }

    private void shutdown() {
        VM entity = (VM) getItem().getEntity();
        Frontend.getInstance().runAction(ActionType.ShutdownVm, new ShutdownVmParameters(entity.getId(), true));
    }

    private void reboot() {
        VM entity = (VM) getItem().getEntity();
        Frontend.getInstance().runAction(ActionType.RebootVm, new VmOperationParameterBase(entity.getId()));
    }

    private void stop() {
        VM entity = (VM) getItem().getEntity();
        Frontend.getInstance().runAction(ActionType.StopVm, new StopVmParameters(entity.getId(), StopVmTypeEnum.NORMAL));
    }

    private void pause() {
        VM entity = (VM) getItem().getEntity();
        Frontend.getInstance().runAction(ActionType.HibernateVm, new VmOperationParameterBase(entity.getId()));
    }

    private void run() {
        VM entity = (VM) getItem().getEntity();

        Frontend.getInstance().runAction(ActionType.RunVm, new RunVmParams(entity.getId()));
    }

    private void updateProperties() {
        VM entity = (VM) getItem().getEntity();

        getItem().setName(entity.getName());
        getItem().setDescription(entity.getVmDescription());
        getItem().setStatus(entity.getStatus());
        getItem().setIsPool(false);
        getItem().setIsServer(entity.getVmType() == VmType.Server);
        getItem().setOsId(entity.getVmOsId());
        getItem().setIsFromPool(entity.getVmPoolId() != null);
        getItem().setSmallIconId(entity.getStaticData().getSmallIconId());
        getItem().setLargeIconId(entity.getStaticData().getLargeIconId());
    }

    private void updateActionAvailability() {
        VM entity = (VM) getItem().getEntity();

        getItem().getTakeVmCommand().setIsAvailable(false);

        ArrayList<VM> entities = new ArrayList<>();
        entities.add(entity);

        getItem().getRunCommand().setIsExecutionAllowed(ActionUtils.canExecute(entities,
                VM.class,
                ActionType.RunVm));
        getItem().getPauseCommand().setIsExecutionAllowed(ActionUtils.canExecute(entities,
                                                                                    VM.class,
                                                                                    ActionType.HibernateVm)
                                                                  && AsyncDataProvider.getInstance().canVmsBePaused(entities));
        getItem().getShutdownCommand().setIsExecutionAllowed(ActionUtils.canExecute(entities,
                VM.class,
                ActionType.ShutdownVm));
        getItem().getStopCommand().setIsExecutionAllowed(ActionUtils.canExecute(entities,
                VM.class,
                ActionType.StopVm));
        getItem().getRebootCommand().setIsExecutionAllowed(AsyncDataProvider.getInstance().isRebootCommandExecutionAllowed(entities));

        // Check whether a VM is from the manual pool.
        if (entity.getVmPoolId() != null) {
            AsyncDataProvider.getInstance().getPoolById(new AsyncQuery<>(
                    pool -> {
                        boolean isManualPool = pool.getVmPoolType() == VmPoolType.MANUAL;
                        updateCommandsAccordingToPoolType(isManualPool);

                    }), entity.getVmPoolId());
        }
        else {
            updateCommandsAccordingToPoolType(true);
        }
    }

    public void updateCommandsAccordingToPoolType(boolean isManualPool) {
        getItem().getReturnVmCommand().setIsAvailable(!isManualPool);
        getItem().getRunCommand().setIsAvailable(isManualPool);
    }
}
