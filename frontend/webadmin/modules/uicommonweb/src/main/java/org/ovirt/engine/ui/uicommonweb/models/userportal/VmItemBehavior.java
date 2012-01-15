package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.HibernateVmParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.ChangeCDModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class VmItemBehavior extends ItemBehavior
{
    public VmItemBehavior(UserPortalItemModel item)
    {
        super(item);
    }

    @Override
    public void OnEntityChanged()
    {
        UpdateProperties();
        UpdateActionAvailability();
    }

    @Override
    public void EntityPropertyChanged(PropertyChangedEventArgs e)
    {
        UpdateProperties();
        if (e.PropertyName.equals("status"))
        {
            UpdateActionAvailability();
        }
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        if (command == getItem().getRunCommand())
        {
            Run();
        }
        else if (command == getItem().getPauseCommand())
        {
            Pause();
        }
        else if (command == getItem().getStopCommand())
        {
            stop();
        }
        else if (command == getItem().getShutdownCommand())
        {
            Shutdown();
        }
        else if (command == getItem().getRetrieveCdImagesCommand())
        {
            RetrieveCdImages();
        }
        else if (command == getItem().getReturnVmCommand())
        {
            ReturnVm();
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        if (ev.equals(ChangeCDModel.ExecutedEventDefinition))
        {
            ChangeCD(sender, args);
        }
    }

    private void ChangeCD(Object sender, EventArgs args)
    {
        VM entity = (VM) getItem().getEntity();
        ChangeCDModel model = (ChangeCDModel) sender;

        // TODO: Patch!
        String imageName = model.getTitle();
        if (StringHelper.stringsEqual(imageName, "No CDs"))
        {
            return;
        }

        Frontend.RunAction(VdcActionType.ChangeDisk,
                new ChangeDiskCommandParameters(entity.getvm_guid(), StringHelper.stringsEqual(imageName,
                        ConsoleModel.EjectLabel) ? "" : imageName));
    }

    private void ReturnVm()
    {
        VM entity = (VM) getItem().getEntity();

        Frontend.RunAction(VdcActionType.ShutdownVm, new ShutdownVmParameters(entity.getvm_guid(), false),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                    }
                }, null);
    }

    private void RetrieveCdImages()
    {
        VM entity = (VM) getItem().getEntity();

        getItem().getCdImages().clear();

        AsyncQuery _asyncQuery0 = new AsyncQuery();
        _asyncQuery0.setModel(this);

        _asyncQuery0.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model0, Object result0)
            {
                if (result0 != null)
                {
                    storage_domains isoDomain = (storage_domains) result0;
                    VmItemBehavior thisVmItemBehavior = (VmItemBehavior) model0;

                    AsyncQuery _asyncQuery = new AsyncQuery();
                    _asyncQuery.setModel(thisVmItemBehavior);

                    _asyncQuery.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model, Object result)
                        {
                            VmItemBehavior vmItemBehavior = (VmItemBehavior) model;
                            java.util.ArrayList<String> list = (java.util.ArrayList<String>) result;
                            if (list.size() > 0)
                            {
                                list.add(ConsoleModel.EjectLabel);
                                for (String iso : list)
                                {
                                    ChangeCDModel tempVar = new ChangeCDModel();
                                    tempVar.setTitle(iso);
                                    ChangeCDModel changeCDModel = tempVar;
                                    changeCDModel.getExecutedEvent().addListener(getItem());
                                    UserPortalItemModel userPortalItemModel = vmItemBehavior.getItem();
                                    userPortalItemModel.getCdImages().add(changeCDModel);
                                }
                            }
                            else
                            {
                                UserPortalItemModel userPortalItemModel = vmItemBehavior.getItem();
                                ChangeCDModel tempVar2 = new ChangeCDModel();
                                tempVar2.setTitle("No CDs");
                                userPortalItemModel.getCdImages().add(tempVar2);
                            }
                        }
                    };
                    AsyncDataProvider.GetIrsImageList(_asyncQuery, isoDomain.getid(), false);

                }
            }
        };

        AsyncDataProvider.GetIsoDomainByDataCenterId(_asyncQuery0, entity.getstorage_pool_id());
    }

    private void Shutdown()
    {
        VM entity = (VM) getItem().getEntity();
        Frontend.RunAction(VdcActionType.ShutdownVm, new ShutdownVmParameters(entity.getvm_guid(), true));
    }

    private void stop()
    {
        VM entity = (VM) getItem().getEntity();
        Frontend.RunAction(VdcActionType.StopVm, new StopVmParameters(entity.getvm_guid(), StopVmTypeEnum.NORMAL));
    }

    private void Pause()
    {
        VM entity = (VM) getItem().getEntity();
        Frontend.RunAction(VdcActionType.HibernateVm, new HibernateVmParameters(entity.getvm_guid()));
    }

    private void Run()
    {
        VM entity = (VM) getItem().getEntity();
        // use sysprep iff the vm is not initialized and vm has Win OS
        boolean reinitialize = !entity.getis_initialized() && DataProvider.IsWindowsOsType(entity.getvm_os());
        RunVmParams tempVar = new RunVmParams(entity.getvm_guid());
        tempVar.setReinitialize(reinitialize);
        Frontend.RunAction(VdcActionType.RunVm, tempVar);
    }

    private void UpdateProperties()
    {
        VM entity = (VM) getItem().getEntity();

        getItem().setName(entity.getvm_name());
        getItem().setDescription(entity.getvm_description());
        getItem().setStatus(entity.getstatus());
        getItem().setIsPool(false);
        getItem().setIsServer(entity.getvm_type() == VmType.Server);
        getItem().setOsType(entity.getvm_os());
        getItem().setIsFromPool(entity.getVmPoolId() != null);

        // Assign PoolType.
        if (entity.getVmPoolId() != null)
        {
            vm_pools pool = getItem().getResolutionService().ResolveVmPoolById(entity.getVmPoolId().getValue());

            // Throw exception. Will help finding bugs in development phase.
            if (pool == null)
            {
                throw new NotImplementedException();
            }

            getItem().setPoolType(pool.getvm_pool_type());
        }

        if (getItem().getDefaultConsole() == null)
        {
            getItem().setDefaultConsole(new SpiceConsoleModel());
        }
        getItem().getDefaultConsole().setEntity(entity);

        // Support RDP console for windows VMs.
        if (DataProvider.IsWindowsOsType(entity.getvm_os()))
        {
            if (getItem().getAdditionalConsole() == null)
            {
                getItem().setAdditionalConsole(new RdpConsoleModel());
            }
            getItem().getAdditionalConsole().setEntity(entity);
            getItem().setHasAdditionalConsole(true);
        }
        else
        {
            getItem().setAdditionalConsole(null);
            getItem().setHasAdditionalConsole(false);
        }
    }

    private void UpdateActionAvailability()
    {
        VM entity = (VM) getItem().getEntity();

        getItem().getTakeVmCommand().setIsAvailable(false);

        java.util.ArrayList<VM> entities = new java.util.ArrayList<VM>();
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
            AsyncDataProvider.GetPoolById(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            VmItemBehavior behavior = (VmItemBehavior) target;
                            vm_pools pool = (vm_pools) returnValue;
                            boolean isManualPool = pool.getvm_pool_type() == VmPoolType.Manual;
                            behavior.UpdateCommandsAccordingToPoolType(isManualPool);

                        }
                    }), entity.getVmPoolId().getValue());
        }
        else
        {
            UpdateCommandsAccordingToPoolType(true);
        }
    }

    public void UpdateCommandsAccordingToPoolType(boolean isManualPool)
    {
        getItem().getReturnVmCommand().setIsAvailable(!isManualPool);
        getItem().getRunCommand().setIsAvailable(isManualPool);
    }
}
