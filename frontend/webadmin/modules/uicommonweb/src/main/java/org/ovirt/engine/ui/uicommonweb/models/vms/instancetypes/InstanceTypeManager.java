package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
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
        activatedListeners = new ArrayList<>();

        registerListeners(model);
    }

    /**
     * First updates the list of instance types and selects the one which is supposed to be selected and then
     * updates all the fields which are taken from the instance type (by calling the updateFields()).
     */
    public void updateAll() {
        final Guid selectedInstanceTypeId = getSelectedInstanceTypeId();

        Frontend.getInstance().runQuery(QueryType.GetAllInstanceTypes, new QueryParametersBase(),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    if (returnValue == null || !returnValue.getSucceeded()) {
                        return;
                    }

                    List<InstanceType> instanceTypes = new ArrayList<>();

                    // add this only if the user is allowed to
                    if (!getModel().isCreateInstanceOnly()) {
                        instanceTypes.add(CustomInstanceType.INSTANCE);
                    }

                    for (InstanceType instanceType : (Iterable<InstanceType>) returnValue.getReturnValue()) {
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
        getModel().startProgress();
        doUpdateManagedFieldsFrom(getSource());
    }

    public void updateFildsAfterOsChanged() {
        deactivateAndStartProgress();
        VmBase vmBase = getSource();

        updateWatchdog(vmBase, false);
        maybeSetEntity(model.getIsUsbEnabled(), vmBase.getUsbPolicy() != UsbPolicy.DISABLED);

        activate();
    }

    /**
     * Starts progress, deactivates the manager and waits until it gets activated again.
     * On activation it stops the progress
     */
    public void deactivateAndStartProgress() {
        model.startProgress();
        deactivate(() -> model.stopProgress());
    }

    private void registerListeners(UnitVmModel model) {
        ManagedFieldsManager managedFieldsManager = new ManagedFieldsManager();
        model.getInstanceTypes().getSelectedItemChangedEvent().addListener(managedFieldsManager);
    }

    public void activate() {
        if (deactivatedNumber != 0) {
            deactivatedNumber--;
        }

        if (isActive() && activatedListeners.size() != 0) {
//            copy done to avoid infinite recursion
            List<ActivatedListener> copy = new ArrayList<>(activatedListeners);

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

    class ManagedFieldsManager implements IEventListener<EventArgs> {

        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            if (!isActive()) {
                return;
            }

            boolean customInstanceTypeSelected = model.getInstanceTypes().getSelectedItem() instanceof CustomInstanceType;

            if (sender == model.getTemplateWithVersion() && customInstanceTypeSelected) {
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

            model.startProgress();
            doUpdateManagedFieldsFrom(template);
        }
    }

    public void updateInstanceTypeFieldsFromSource() {
        model.startProgress();
        doUpdateManagedFieldsFrom(getSource());
    }

    protected void doUpdateManagedFieldsFrom(final VmBase vmBase) {
        if (vmBase == null) {
            model.stopProgress();
            return;
        }

        deactivate();
        maybeSetEntity(model.getMemSize(), vmBase.getMemSizeMb());
        maybeSetEntity(model.getMaxMemorySize(), vmBase.getMaxMemorySizeMb());
        maybeSetEntity(model.getIoThreadsEnabled(), vmBase.getNumOfIoThreads() != 0);
        maybeSetEntity(model.getNumOfIoThreads(), Integer.toString(vmBase.getNumOfIoThreads()));
        maybeSetEntity(model.getTotalCPUCores(), Integer.toString(vmBase.getNumOfCpus()));
        model.setBootSequence(vmBase.getDefaultBootSequence());

        List<MigrationSupport> supportedModes = (List<MigrationSupport>) getModel().getMigrationMode().getItems();
        if (supportedModes.contains(vmBase.getMigrationSupport())) {
            maybeSetSelectedItem(getModel().getMigrationMode(), vmBase.getMigrationSupport());
        }

        maybeSetEntity(model.getIsHighlyAvailable(), vmBase.isAutoStartup());
        maybeSetSelectedItem(model.getNumOfSockets(), vmBase.getNumOfSockets());
        maybeSetSelectedItem(model.getCoresPerSocket(), vmBase.getCpuPerSocket());
        maybeSetSelectedItem(model.getThreadsPerCore(), vmBase.getThreadsPerCpu());

        maybeSetSelectedItem(model.getEmulatedMachine(), vmBase.getCustomEmulatedMachine());
        maybeSetSelectedItem(model.getCustomCpu(), vmBase.getCustomCpuName());

        model.setSelectedMigrationDowntime(vmBase.getMigrationDowntime());
        model.selectMigrationPolicy(vmBase.getMigrationPolicyId());
        maybeSetEntity(model.getMemoryBalloonEnabled(), vmBase.isBalloonEnabled());
        priorityUtil.initPriority(vmBase.getPriority(), new PriorityUtil.PriorityUpdatingCallbacks() {
            @Override
            public void beforeUpdates() {
                deactivate();
            }

            @Override
            public void afterUpdates() {
                activate();
            }
        });

        updateDefaultDisplayRelatedFields(vmBase);

        if (vmBase.getMinAllocatedMem() != 0) {
            model.getMinAllocatedMemory().setEntity(vmBase.getMinAllocatedMem());
        }

        activate();

        AsyncDataProvider.getInstance().isSoundcardEnabled(new AsyncQuery<>(returnValue -> {
            deactivate();
            getModel().getIsSoundcardEnabled().setEntity(returnValue);
            activate();

            Frontend.getInstance().runQuery(QueryType.GetConsoleDevices, new IdQueryParameters(vmBase.getId()),
                    new AsyncQuery<QueryReturnValue>(r -> {
                        deactivate();
                        List<String> consoleDevices = r.getReturnValue();
                        getModel().getIsConsoleDeviceEnabled().setEntity(!consoleDevices.isEmpty());
                        activate();

                        Frontend.getInstance().runQuery(QueryType.GetTpmDevices, new IdQueryParameters(vmBase.getId()),
                                new AsyncQuery<QueryReturnValue>(rr -> {
                                    deactivate();
                                    List<String> tpmDevices = rr.getReturnValue();
                                    boolean tpmEnabled = !tpmDevices.isEmpty();
                                    getModel().getTpmEnabled().setEntity(tpmEnabled);
                                    getModel().setTpmOriginallyEnabled(tpmEnabled);
                                    activate();

                                    postDoUpdateManagedFieldsFrom(vmBase);
                                }));
                    }));
        }), vmBase.getId());
    }

    private void postDoUpdateManagedFieldsFrom(VmBase vmBase) {
        if (isNextRunConfigurationExists()) {
            deactivate();
            getModel().getIsSoundcardEnabled().setEntity(
                    VmDeviceCommonUtils.isVmDeviceExists(vmBase.getManagedDeviceMap(), VmDeviceGeneralType.SOUND));
            getModel().getTpmEnabled().setEntity(
                    VmDeviceCommonUtils.isVmDeviceExists(vmBase.getManagedDeviceMap(), VmDeviceType.TPM));
            getModel().getIsConsoleDeviceEnabled().setEntity(
                    VmDeviceCommonUtils.isVmDeviceExists(vmBase.getManagedDeviceMap(), VmDeviceType.CONSOLE));

            Set<GraphicsType> graphicsTypeSet = new HashSet<>();
            for (GraphicsType graphicsType : GraphicsType.values()) {
                if (VmDeviceCommonUtils.isVmDeviceExists(vmBase.getManagedDeviceMap(),
                        graphicsType.getCorrespondingDeviceType())) {
                    graphicsTypeSet.add(graphicsType);
                }
            }
            getModel().getGraphicsType().setSelectedItem(UnitVmModel.GraphicsTypes.fromGraphicsTypes(graphicsTypeSet));

            activate();

            AsyncDataProvider.getInstance().getVmNextRunConfiguration(new AsyncQuery<>(result -> {
                VM vm = result;
                VmDevice device = VmDeviceCommonUtils.findVmDeviceByGeneralType(vm.getManagedVmDeviceMap(), VmDeviceGeneralType.WATCHDOG);

                if (device == null) {
                    updateWatchdog(vmBase, Collections.emptyList(), true);
                } else {
                    List<VmWatchdog> devices = new ArrayList<>();
                    devices.add(new VmWatchdog(device));
                    updateWatchdog(vmBase, devices, true);
                }
            }), vmBase.getId());
        } else {
            updateWatchdog(vmBase, true);
        }
    }

    private void updateWatchdog(final VmBase vmBase, final boolean continueWithNext) {
        AsyncDataProvider.getInstance().getWatchdogByVmId(new AsyncQuery<QueryReturnValue>(returnValue -> {
            updateWatchdog(vmBase, returnValue.getReturnValue(), continueWithNext);
        }), vmBase.getId());
    }

    private void updateWatchdog(final VmBase vmBase, List<VmWatchdog> watchdogs, final boolean continueWithNext) {
        deactivate();

        if (watchdogs.size() == 0) {
            model.getWatchdogAction().setSelectedItem(model.getWatchdogAction().getItems().iterator().next());
            model.getWatchdogModel().setSelectedItem(model.getWatchdogModel().getItems().iterator().next());
        }

        for (VmWatchdog watchdog : watchdogs) {
            if (watchdogAvailable(watchdog.getModel())) {
                model.getWatchdogAction().setSelectedItem(watchdog.getAction() == null ? null
                        : watchdog.getAction());
                model.getWatchdogModel().setSelectedItem(watchdog.getModel() == null ? null //$NON-NLS-1$
                        : watchdog.getModel());
            }
        }
        activate();

        if (continueWithNext) {
            updateRngDevice(vmBase);
        }
    }

    protected void updateRngDevice(final VmBase vmBase) {
        if (model.getIsRngEnabled().getIsChangable() && model.getIsRngEnabled().getIsAvailable()) {
            if (!isNextRunConfigurationExists()) {
                Frontend.getInstance().runQuery(QueryType.GetRngDevice, new IdQueryParameters(vmBase.getId()),
                        new AsyncQuery<QueryReturnValue>(returnValue -> {
                            deactivate();
                            List<VmDevice> rngDevices = returnValue.getReturnValue();
                            getModel().getIsRngEnabled().setEntity(!rngDevices.isEmpty());
                            if (!rngDevices.isEmpty()) {
                                VmRngDevice rngDevice = new VmRngDevice(rngDevices.get(0));
                                rngDevice.updateSourceByVersion(getModel().getCompatibilityVersion());
                                getModel().setRngDevice(rngDevice);
                            }
                            activate();
                            updateVirtioScsi(vmBase);
                        }
                ));
            } else {
                deactivate();
                VmDevice rngDevice = VmDeviceCommonUtils.findVmDeviceByGeneralType(
                        vmBase.getManagedDeviceMap(), VmDeviceGeneralType.RNG);
                getModel().getIsRngEnabled().setEntity(rngDevice != null);
                if (rngDevice != null) {
                    getModel().setRngDevice(new VmRngDevice(rngDevice));
                }
                activate();
                updateVirtioScsi(vmBase);
            }
        } else {
            updateVirtioScsi(vmBase);
        }
    }

    private void updateVirtioScsi(VmBase vmBase) {
        if (isNextRunConfigurationExists()) {
            getModel().getIsVirtioScsiEnabled().setEntity(
                    VmDeviceCommonUtils.isVmDeviceExists(vmBase.getManagedDeviceMap(), VmDeviceType.VIRTIOSCSI));
            model.stopProgress();
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

    private boolean watchdogAvailable(VmWatchdogType watchdogModel) {
        for (VmWatchdogType availableWatchdogModel : model.getWatchdogModel().getItems()) {
            if (watchdogModel == null && availableWatchdogModel == null) {
                return true;
            }

            if (watchdogModel != null && availableWatchdogModel != null && watchdogModel.equals(availableWatchdogModel)) {
                return true;
            }
        }

        return false;
    }

    protected void updateDefaultDisplayRelatedFields(final VmBase vmBase) {
        final Collection<DisplayType> displayTypes = model.getDisplayType().getItems();
        if (displayTypes == null || displayTypes.isEmpty()) {
            return;
        }

        deactivate();

        model.getIsHeadlessModeEnabled().setEntity(vmBase.getDefaultDisplayType() == DisplayType.none);

        // select display protocol
        DisplayType displayProtocol = displayTypes.iterator().next(); // first by default
        if (displayTypes.contains(vmBase.getDefaultDisplayType())) {
            displayProtocol = vmBase.getDefaultDisplayType(); // if display types contain DT of a vm, pick this one
        }
        maybeSetSelectedItem(model.getDisplayType(), displayProtocol);

        Frontend.getInstance().runQuery(QueryType.GetGraphicsDevices, new IdQueryParameters(vmBase.getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<GraphicsDevice> graphicsDevices = returnValue.getReturnValue();
                    Set<GraphicsType> graphicsTypes = new HashSet<>();

                    for (GraphicsDevice graphicsDevice : graphicsDevices) {
                        graphicsTypes.add(graphicsDevice.getGraphicsType());
                    }

                    UnitVmModel.GraphicsTypes selected = UnitVmModel.GraphicsTypes.fromGraphicsTypes(graphicsTypes);
                    if (selected != null && getModel().getGraphicsType().getItems().contains(selected)) {
                        maybeSetSelectedItem(getModel().getGraphicsType(), selected);
                    }

                    maybeSetSelectedItem(model.getNumOfMonitors(), vmBase.getNumOfMonitors());
                    maybeSetEntity(model.getIsUsbEnabled(), vmBase.getUsbPolicy() != UsbPolicy.DISABLED);
                    maybeSetEntity(model.getIsSmartcardEnabled(), vmBase.isSmartcardEnabled());

                    activate();
                }));
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

    protected boolean isNextRunConfigurationExists() {
        return false;
    }

    protected boolean isSourceCustomInstanceType() {
        if (getSource() instanceof VmTemplate) {
            VmTemplate source = (VmTemplate) getSource();
            if (source.getTemplateType() == VmEntityType.INSTANCE_TYPE) {
                // only the custom instance type has null id.
                // not using instanceof check because findbugs can not handle it properly here
                return source.getId() == null;
            }

            return false;
        }

        return false;

    }
}
