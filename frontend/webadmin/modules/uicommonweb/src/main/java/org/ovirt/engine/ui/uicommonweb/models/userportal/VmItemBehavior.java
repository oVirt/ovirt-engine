package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.HibernateVmParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.ChangeCDModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VmItemBehavior extends ItemBehavior
{
    public VmItemBehavior(UserPortalItemModel item)
    {
        super(item);
    }

    @Override
    public void onEntityChanged()
    {
        updateProperties();
        updateActionAvailability();
    }

    @Override
    public void entityPropertyChanged(PropertyChangedEventArgs e)
    {
        updateProperties();
        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            updateActionAvailability();
        }
    }

    @Override
    public void executeCommand(UICommand command)
    {
        if (command == getItem().getRunCommand())
        {
            run();
        }
        else if (command == getItem().getPauseCommand())
        {
            pause();
        }
        else if (command == getItem().getStopCommand())
        {
            stop();
        }
        else if (command == getItem().getShutdownCommand())
        {
            shutdown();
        }
        else if (command == getItem().getReturnVmCommand())
        {
            returnVm();
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        if (ev.matchesDefinition(ChangeCDModel.ExecutedEventDefinition))
        {
            changeCD(sender, args);
        }
    }

    private void changeCD(Object sender, EventArgs args)
    {
        VM entity = (VM) getItem().getEntity();
        ChangeCDModel model = (ChangeCDModel) sender;

        // TODO: Patch!
        String imageName = model.getTitle();
        if (StringHelper.stringsEqual(imageName, ConstantsManager.getInstance()
                .getConstants()
                .noCds()))
        {
            return;
        }

        Frontend.RunAction(VdcActionType.ChangeDisk,
                new ChangeDiskCommandParameters(entity.getId(), StringHelper.stringsEqual(imageName,
                        ConsoleModel.EjectLabel) ? "" : imageName)); //$NON-NLS-1$
    }

    private void returnVm()
    {
        VM entity = (VM) getItem().getEntity();

        Frontend.RunAction(VdcActionType.ShutdownVm, new ShutdownVmParameters(entity.getId(), false),
                null, null);
    }

    private void shutdown()
    {
        VM entity = (VM) getItem().getEntity();
        Frontend.RunAction(VdcActionType.ShutdownVm, new ShutdownVmParameters(entity.getId(), true));
    }

    private void stop()
    {
        VM entity = (VM) getItem().getEntity();
        Frontend.RunAction(VdcActionType.StopVm, new StopVmParameters(entity.getId(), StopVmTypeEnum.NORMAL));
    }

    private void pause()
    {
        VM entity = (VM) getItem().getEntity();
        Frontend.RunAction(VdcActionType.HibernateVm, new HibernateVmParameters(entity.getId()));
    }

    private void run()
    {
        VM entity = (VM) getItem().getEntity();
        // use sysprep iff the vm is not initialized and vm has Win OS
        boolean reinitialize = !entity.isInitialized() && AsyncDataProvider.isWindowsOsType(entity.getVmOsId());
        RunVmParams tempVar = new RunVmParams(entity.getId());
        tempVar.setReinitialize(reinitialize);
        Frontend.RunAction(VdcActionType.RunVm, tempVar);
    }

    private void updateProperties()
    {
        VM entity = (VM) getItem().getEntity();

        getItem().setName(entity.getName());
        getItem().setDescription(entity.getVmDescription());
        getItem().setStatus(entity.getStatus());
        getItem().setIsPool(false);
        getItem().setIsServer(entity.getVmType() == VmType.Server);
        getItem().setOsId(entity.getVmOsId());
        getItem().setIsFromPool(entity.getVmPoolId() != null);
        getItem().setSpiceDriverVersion(entity.getSpiceDriverVersion());

        if (getItem().getDefaultConsoleModel() == null)
        {
            getItem().setDefaultConsole(new SpiceConsoleModel());
        }
        getItem().getDefaultConsoleModel().setEntity(entity);

        // Support RDP console for windows VMs.
        if (AsyncDataProvider.isWindowsOsType(entity.getVmOsId()))
        {
            if (getItem().getAdditionalConsoleModel() == null)
            {
                getItem().setAdditionalConsole(new RdpConsoleModel());
            }
            getItem().getAdditionalConsoleModel().setEntity(entity);
        }
        else
        {
            getItem().setAdditionalConsole(null);
        }
    }

    private void updateActionAvailability()
    {
        VM entity = (VM) getItem().getEntity();

        getItem().getTakeVmCommand().setIsAvailable(false);

        ArrayList<VM> entities = new ArrayList<VM>();
        entities.add(entity);

        getItem().getRunCommand().setIsExecutionAllowed(VdcActionUtils.CanExecute(entities,
                VM.class,
                VdcActionType.RunVm));
        getItem().getPauseCommand().setIsExecutionAllowed(VdcActionUtils.CanExecute(entities,
                VM.class,
                VdcActionType.HibernateVm));
        getItem().getShutdownCommand().setIsExecutionAllowed(VdcActionUtils.CanExecute(entities,
                VM.class,
                VdcActionType.ShutdownVm));
        getItem().getStopCommand().setIsExecutionAllowed(VdcActionUtils.CanExecute(entities,
                VM.class,
                VdcActionType.StopVm));

        // Check whether a VM is from the manual pool.
        if (entity.getVmPoolId() != null)
        {
            AsyncDataProvider.getPoolById(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            VmItemBehavior behavior = (VmItemBehavior) target;
                            VmPool pool = (VmPool) returnValue;
                            boolean isManualPool = pool.getVmPoolType() == VmPoolType.Manual;
                            behavior.updateCommandsAccordingToPoolType(isManualPool);

                        }
                    }), entity.getVmPoolId().getValue());
        }
        else
        {
            updateCommandsAccordingToPoolType(true);
        }
    }

    public void updateCommandsAccordingToPoolType(boolean isManualPool)
    {
        getItem().getReturnVmCommand().setIsAvailable(!isManualPool);
        getItem().getRunCommand().setIsAvailable(isManualPool);
    }
}
