package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.CustomInstanceType;
import org.ovirt.engine.ui.uicommonweb.models.vms.PriorityUtil;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VirtioScsiUtil;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import java.util.Map;
/**
 * Base class which takes care about copying the proper fields from instance type/template/vm static to the VM.
 *
 * Please note that this class manages only the fields which are defined on the instance type, so the
 * fields which are taken from the template and are not from instance type have to be handled on some
 * different place (most often in the behavior classes).
 *
 * While this class takes care of the fields which are defined on instance type it does not mean they
 * has to be taken from instance type. For example if the custom instance type is selected, the fields
 * are taken from the selected template. If the edit VM is being displayed, the fields are taken from the VM static.
 *
 */
public abstract class InstanceTypeManager {

    // turns the maybeSet* to always let the entity be set - useful for e.g. pooled VMs were everything is disabled but has to be set according to the underlying VM
    private boolean alwaysEnabledFieldUpdate = false;

    private final UnitVmModel model;

    private PriorityUtil priorityUtil;

    private VirtioScsiUtil virtioScsiUtil;

    private InstanceTypeAttachDetachManager instanceTypeAttachDetachManager;

    /**
     * Since the manager can be activated/deactivated more times (because of queries running in parallel) only
     * after all the queries which has deactivated the manager again activates it can be the manager considered to be
     * active again.
     */
    private int deactivatedNumber = 0;

    private List<ActivatedListener> activatedListeners;

    public InstanceTypeManager(UnitVmModel model) {
        this.model = model;

        priorityUtil = new PriorityUtil(model);
        virtioScsiUtil = new VirtioScsiUtil(model);
        instanceTypeAttachDetachManager = new InstanceTypeAttachDetachManager(this, model);
        activatedListeners = new ArrayList<ActivatedListener>();

        registerListeners(model);
    }

    /**
     * First updates the list of instance types and selects the one which is supposed to be selected and then
     * updates all the fields which are taken from the instance type (by calling the updateFields()).
     */
    public void updateAll() {
        final Guid selectedInstanceTypeId = getSelectedInstanceTypeId();

        Frontend.getInstance().runQuery(VdcQueryType.GetAllInstanceTypes, new VdcQueryParametersBase(), new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                VdcQueryReturnValue res = (VdcQueryReturnValue) returnValue;
                if (res == null || !res.getSucceeded()) {
                    return;
                }

                List<InstanceType> instanceTypes = new ArrayList<InstanceType>();

                // add this only if the user is allowed to
                if (!getModel().isCreateInstanceOnly()) {
                    instanceTypes.add(CustomInstanceType.INSTANCE);
                }

                for (InstanceType instanceType : (Iterable<InstanceType>) res.getReturnValue()) {
                    instanceTypes.add(instanceType);
                }

                getModel().getInstanceTypes().setItems(instanceTypes);
                for (InstanceType instanceType : instanceTypes) {
                    if ((instanceType instanceof CustomInstanceType) && selectedInstanceTypeId == null) {
                        getModel().getInstanceTypes().setSelectedItem(CustomInstanceType.INSTANCE);
                        break;
                    }

                    if (instanceType.getId() == null || selectedInstanceTypeId == null) {
                        continue;
                    }

                    if (instanceType.getId().equals(selectedInstanceTypeId)) {
                        getModel().getInstanceTypes().setSelectedItem(instanceType);
                        break;
                    }
                }

                if (getModel().getInstanceTypes().getSelectedItem() instanceof CustomInstanceType) {
                    // detach if the instance type is "custom"
                    getModel().getAttachedToInstanceType().setEntity(false);
                }

                updateFields();
            }
        }));
    }

    /**
     * All the fields which are copied from the instance type.
     *
     * This method ignores all the fields which are not changeable, because the fields which are not changeable
     * are for some reason not enabled in the current context (e.g. the device is not supported on the specific cluster version).
     *
     * This method has to be re-called every time this situation changes (e.g. cluster level changes or os type changes) so the specific fields can
     * be properly populated.
     */
    public void updateFields() {
        getModel().startProgress(null);
        doUpdateManagedFieldsFrom(getSource());
    }

    private void registerListeners(UnitVmModel model) {
        ManagedFieldsManager managedFieldsManager = new ManagedFieldsManager();
        model.getInstanceTypes().getSelectedItemChangedEvent().addListener(managedFieldsManager);
        model.getTemplate().getSelectedItemChangedEvent().addListener(managedFieldsManager);

        model.getOSType().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

            }
        });
    }

    public void activate() {
        if (deactivatedNumber != 0) {
            deactivatedNumber--;
        }

        if (isActive() && activatedListeners.size() != 0) {
//            copy done to avoid infinite recursion
            List<ActivatedListener> copy = new ArrayList<ActivatedListener>(activatedListeners);

            activatedListeners.clear();

            for (ActivatedListener listener : copy) {
                listener.activated();
            }
        }
    }

    public void deactivate(ActivatedListener activatedListener) {
        activatedListeners.add(activatedListener);
        deactivate();
    }

    public void deactivate() {
        deactivatedNumber++;
    }

    public boolean isActive() {
        return deactivatedNumber == 0;
    }

    protected UnitVmModel getModel() {
        return model;
    }

    class ManagedFieldsManager implements IEventListener {

        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            if (!isActive()) {
                return;
            }

            boolean customInstanceTypeSelected = model.getInstanceTypes().getSelectedItem() instanceof CustomInstanceType;

            if (sender == model.getTemplate() && customInstanceTypeSelected) {
                // returns the VM/Pool's static data or the template (depending on the specific new/existing manager)
                updateInstanceTypeFieldsFrom(getSource());
            } else if (sender == model.getInstanceTypes() && !customInstanceTypeSelected) {
                // the instance type is in fact a template
                updateInstanceTypeFieldsFrom((VmTemplate) model.getInstanceTypes().getSelectedItem());
            } else if (sender == model.getInstanceTypes() && customInstanceTypeSelected) {
                instanceTypeAttachDetachManager.manageInstanceType(CustomInstanceType.INSTANCE);
            }

        }

        private void updateInstanceTypeFieldsFrom(VmBase template) {
            if (template == null) {
                return;
            }

            model.startProgress(null);
            doUpdateManagedFieldsFrom(template);
        }
    }

    private VmDevice findVmDeviceByName(Map<Guid, VmDevice> vmManagedDeviceMap, String name) {
        for (VmDevice vmDevice : vmManagedDeviceMap.values()) {
            if (vmDevice.getDevice().equals(name)) {
                return vmDevice;
            }
        }

        return null;
    }

    private boolean isVmDeviceExists(Map<Guid, VmDevice> vmManagedDeviceMap, String name) {
        return findVmDeviceByName(vmManagedDeviceMap, name) != null;
    }

    protected void doUpdateManagedFieldsFrom(final VmBase vmBase) {
        if (vmBase == null) {
            model.stopProgress();
            return;
        }

        deactivate();
        maybeSetEntity(model.getMemSize(), vmBase.getMemSizeMb());
        maybeSetEntity(model.getTotalCPUCores(), Integer.toString(vmBase.getNumOfCpus()));
        model.setBootSequence(vmBase.getDefaultBootSequence());

        List<MigrationSupport> supportedModes = (List<MigrationSupport>) getModel().getMigrationMode().getItems();
        if (supportedModes.contains(vmBase.getMigrationSupport())) {
            maybeSetSelectedItem(getModel().getMigrationMode(), vmBase.getMigrationSupport());
        }

        maybeSetEntity(model.getIsHighlyAvailable(), vmBase.isAutoStartup());
        maybeSetSelectedItem(model.getNumOfSockets(), vmBase.getNumOfSockets());
        maybeSetSelectedItem(model.getCoresPerSocket(), vmBase.getCpuPerSocket());

        model.setSelectedMigrationDowntime(vmBase.getMigrationDowntime());
        priorityUtil.initPriority(vmBase.getPriority());

        updateDefaultDisplayRelatedFields(vmBase);

        if (vmBase.getMinAllocatedMem() != 0) {
            model.getMinAllocatedMemory().setEntity(vmBase.getMinAllocatedMem());
        }

        activate();

        AsyncDataProvider.isSoundcardEnabled(new AsyncQuery(model, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                deactivate();
                getModel().getIsSoundcardEnabled().setEntity((Boolean) returnValue);
                activate();
                Frontend.getInstance().runQuery(VdcQueryType.GetConsoleDevices, new IdQueryParameters(vmBase.getId()), new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        deactivate();
                        List<String> consoleDevices = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        getModel().getIsConsoleDeviceEnabled().setEntity(!consoleDevices.isEmpty());
                        activate();
                        updateWatchdog(vmBase);
                    }
                }));

            }
        }), vmBase.getId());
    }

    private void updateWatchdog(final VmBase vmBase) {
        AsyncDataProvider.getWatchdogByVmId(new AsyncQuery(this.getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                deactivate();
                UnitVmModel model = (UnitVmModel) target;
                VdcQueryReturnValue val = (VdcQueryReturnValue) returnValue;
                @SuppressWarnings("unchecked")
                Collection<VmWatchdog> watchdogs = val.getReturnValue();

                if (watchdogs.size() == 0) {
                    model.getWatchdogAction().setSelectedItem(model.getWatchdogAction().getItems().iterator().next());
                    model.getWatchdogModel().setSelectedItem(model.getWatchdogModel().getItems().iterator().next());
                }

                for (VmWatchdog watchdog : watchdogs) {
                    if (watchdogAvailable(watchdog.getModel().name())) {
                        model.getWatchdogAction().setSelectedItem(watchdog.getAction() == null ? null
                                : watchdog.getAction().name().toLowerCase());
                        model.getWatchdogModel().setSelectedItem(watchdog.getModel() == null ? "" //$NON-NLS-1$
                                : watchdog.getModel().name());
                    }
                }
                activate();

                updateBalloon(vmBase);
            }
        }), vmBase.getId());
    }

    private void updateBalloon(final VmBase vmBase) {
        if (model.getMemoryBalloonDeviceEnabled().getIsChangable() && model.getMemoryBalloonDeviceEnabled().getIsAvailable()) {
            Frontend.getInstance().runQuery(VdcQueryType.IsBalloonEnabled, new IdQueryParameters(vmBase.getId()), new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object parenModel, Object returnValue) {
                            deactivate();
                            getModel().getMemoryBalloonDeviceEnabled().setEntity((Boolean) ((VdcQueryReturnValue)returnValue).getReturnValue());
                            activate();
                            updateRngDevice(vmBase);
                        }
                    }
            ));
        } else {
            updateRngDevice(vmBase);
        }

    }

    private void updateRngDevice(final VmBase vmBase) {
        if (model.getIsRngEnabled().getIsChangable() && model.getIsRngEnabled().getIsAvailable()) {
            Frontend.getInstance().runQuery(VdcQueryType.GetRngDevice, new IdQueryParameters(vmBase.getId()), new AsyncQuery(
                    this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object model, Object returnValue) {
                            deactivate();
                            List<VmDevice> rngDevices = ((VdcQueryReturnValue) returnValue).getReturnValue();
                            getModel().getIsRngEnabled().setEntity(!rngDevices.isEmpty());
                            if (!rngDevices.isEmpty()) {
                                VmRngDevice rngDevice = new VmRngDevice(rngDevices.get(0));
                                getModel().setRngDevice(rngDevice);
                            }
                            activate();
                            updateVirtioScsi(vmBase);
                        }
                    }
            ));
        } else {
            updateVirtioScsi(vmBase);
        }
    }

    private void updateVirtioScsi(VmBase vmBase) {
        if (isNextRunConfigurationExists()) {
            getModel().getIsVirtioScsiEnabled().setEntity(isVmDeviceExists(vmBase.getManagedDeviceMap(), VmDeviceType.VIRTIOSCSI.getName()));
            return;
        }

        virtioScsiUtil.updateVirtioScsiEnabled(vmBase.getId(), getModel().getOSType().getSelectedItem(), new VirtioScsiUtil.VirtioScasiEnablingFinished() {
            @Override
            public void beforeUpdates() {
                deactivate();
            }

            @Override
            public void afterUpdates() {
                activate();
                model.stopProgress();

                instanceTypeAttachDetachManager.manageInstanceType(model.getInstanceTypes().getSelectedItem());
            }
        });
    }

    private boolean watchdogAvailable(String watchdogModel) {
        for (String availableWatchdogModel : model.getWatchdogModel().getItems()) {
            if (watchdogModel == null && availableWatchdogModel == null) {
                return true;
            }

            if (watchdogModel != null && availableWatchdogModel != null && watchdogModel.equals(availableWatchdogModel)) {
                return true;
            }
        }

        return false;
    }

    protected void updateDefaultDisplayRelatedFields(VmBase vmBase) {
        // Update display protocol selected item
        if (model.getDisplayProtocol().getItems() == null) {
            return;
        }

        EntityModel<DisplayType> displayProtocol = null;
        boolean isFirst = true;
        for (EntityModel<DisplayType> item : model.getDisplayProtocol().getItems()) {
            if (isFirst) {
                displayProtocol = item;
                isFirst = false;
            }
            DisplayType dt = item.getEntity();
            if (dt == vmBase.getDefaultDisplayType()) {
                displayProtocol = item;
                break;
            }
        }

        maybeSetSelectedItem(model.getDisplayProtocol(), displayProtocol);
        maybeSetSelectedItem(model.getNumOfMonitors(), vmBase.getNumOfMonitors());
        maybeSetSelectedItem(model.getUsbPolicy(), vmBase.getUsbPolicy());
        maybeSetEntity(model.getIsSmartcardEnabled(), vmBase.isSmartcardEnabled());
        maybeSetSingleQxlPci(vmBase);
    }

    protected void maybeSetSingleQxlPci(VmBase vmBase) {
        maybeSetEntity(model.getIsSingleQxlEnabled(), vmBase.getSingleQxlPci());
    }

    protected <T> void maybeSetSelectedItem(ListModel<T> entityModel, T value) {
        if (alwaysEnabledFieldUpdate || (entityModel != null && entityModel.getIsChangable() && entityModel.getIsAvailable())) {
            entityModel.setSelectedItem(value);
        }
    }

    protected <T> void maybeSetEntity(EntityModel<T> listModel, T value) {
        if (alwaysEnabledFieldUpdate || (listModel != null && listModel.getIsChangable() && listModel.getIsAvailable())) {
            listModel.setEntity(value);
        }
    }

    protected Guid getSelectedInstanceTypeId() {
        return model.getInstanceTypes().getSelectedItem() != null ? model.getInstanceTypes().getSelectedItem().getId() : null;
    }

    public static interface ActivatedListener {
        void activated();
    }

    public void setAlwaysEnabledFieldUpdate(boolean alwaysEnabledFieldUpdate) {
        this.alwaysEnabledFieldUpdate = alwaysEnabledFieldUpdate;
    }

    /**
     * The source from which the data has to be copyed to managed fields.
     *
     * It can be an instance type, a template or a VM's static data
     */
    protected abstract VmBase getSource();

    protected boolean isNextRunConfigurationExists() { return false; }

}
