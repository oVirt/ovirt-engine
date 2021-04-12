package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.CustomInstanceType;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

/**
 * Class which listens to fields which if changed makes the VM detached from the instance type.
 * If detects a change in such a field it deciedes, if the VM should be detached from the current instance type
 * or attached back to the original one if the fields has been changed back.
 */
public class InstanceTypeAttachDetachManager implements IEventListener<EventArgs> {

    private UnitVmModel model;

    private InstanceTypeManager instanceTypeManager;

    private InstanceType managedInstanceType;

    private Map<Model, Object> modelToProperValue;

    private List<Model> detachableModels;

    private boolean alreadyRegistered = false;

    public InstanceTypeAttachDetachManager(InstanceTypeManager instanceTypeManager, UnitVmModel model) {
        this.instanceTypeManager = instanceTypeManager;
        this.model = model;

        initDetachableFields();
    }

    public void manageInstanceType(InstanceType instanceType) {
        this.managedInstanceType = instanceType;
        setAttachedTo(instanceType);

        modelToProperValue = new HashMap<>();

        for (Model model : detachableModels) {
            if (model instanceof ListModel) {
                modelToProperValue.put(model, ((ListModel) model).getSelectedItem());
            } else if (model instanceof EntityModel) {
                modelToProperValue.put(model, ((EntityModel) model).getEntity());
            }
        }

        if (!alreadyRegistered) {
            model.getInstanceTypes().getSelectedItemChangedEvent().addListener(this);
            listenToDetachableFields(detachableModels);

            alreadyRegistered = true;
        }
    }

    private void listenToDetachableFields(List<Model> models) {
        for (Model model : models) {
            if (model instanceof ListModel) {
                ((ListModel<?>) model).getSelectedItemChangedEvent().addListener(this);
            } else if (model instanceof EntityModel) {
                ((EntityModel<?>) model).getEntityChangedEvent().addListener(this);
            }
        }
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        if (!instanceTypeManager.isActive()) {
            return;
        }

        boolean attached = model.getAttachedToInstanceType().getEntity();
        boolean instanceTypeChanged = sender == model.getInstanceTypes();
        boolean customInstanceType = model.getInstanceTypes().getSelectedItem() instanceof CustomInstanceType;
        boolean allFieldsAttached = checkAllFieldsAttached();

        if (!attached && !customInstanceType && instanceTypeChanged) {
            // if the instance type changed attach again to the new instance type
            model.getAttachedToInstanceType().setEntity(true);
        }

        if (instanceTypeChanged && customInstanceType) {
            // if changed to custom instance type than detach
            setAttachedTo(CustomInstanceType.INSTANCE);
        }

        if (!instanceTypeChanged) {
            // if some managed field changed, consider detach / attach (ignoring custom instance type)

            if (attached && !allFieldsAttached && !customInstanceType) {
                // detach if it was attached but some managed field gets changed
                setAttachedTo(CustomInstanceType.INSTANCE);
            }

            if (!attached && allFieldsAttached && customInstanceType) {
                // it was previously detached but the fields changed back so attaching again
                setAttachedTo(managedInstanceType);
            }
        }
    }

    private void setAttachedTo(InstanceType instanceType) {
        if (instanceType == null) {
            return;
        }

        instanceTypeManager.deactivate();
        model.getAttachedToInstanceType().setEntity(!(instanceType instanceof CustomInstanceType));
        model.getInstanceTypes().setSelectedItem(instanceType);
        instanceTypeManager.activate();
    }

    private boolean checkAllFieldsAttached() {
        for (Map.Entry<Model, Object> entry : modelToProperValue.entrySet()) {
            if (entry.getKey() instanceof ListModel) {
                if (!Objects.equals(((ListModel) entry.getKey()).getSelectedItem(), entry.getValue())) {
                    return false;
                }
            } else if (entry.getKey() instanceof EntityModel) {
                if (!Objects.equals(((EntityModel) entry.getKey()).getEntity(), entry.getValue())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * All the fields which are hard bound to the instance type (e.g. if changed, the
     * VM gets detached from the instance type)
     */
    private void initDetachableFields() {
        detachableModels = Arrays.asList(
                model.getMemSize(),
                model.getTotalCPUCores(),
                model.getNumOfSockets(),
                model.getCoresPerSocket(),
                model.getThreadsPerCore(),
                model.getIsHighlyAvailable(),
                model.getMigrationMode(),
                model.getMigrationDowntime(),
                model.getMigrationPolicies(),
                model.getPriority(),
                model.getMinAllocatedMemory(),
                model.getMemoryBalloonEnabled(),
                model.getNumOfIoThreads(),
                model.getIoThreadsEnabled());

    }
}
