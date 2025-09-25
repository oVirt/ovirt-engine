package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStoragePartialModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

/**
 * Base class of a proxy model that will intercept {@link SanStorageModelBase#getItems()} and provide the ability
 * to filter items.
 */
public abstract class ProxyModelBase implements SanStoragePartialModel {

    private final SanStorageModelBase model;

    public ProxyModelBase(SanStorageModelBase model) {
        this.model = model;
    }

    @Override
    public void setMultiSelection(boolean multiSelection) {
        model.setMultiSelection(multiSelection);
    }

    @Override
    public Event<EventArgs> getItemsChangedEvent() {
        return model.getItemsChangedEvent();
    }

    @Override
    public void setSelectedLunWarning(String warning) {
        model.setSelectedLunWarning(warning);
    }

    @Override
    public void updateLunWarningForDiscardAfterDelete() {
        model.updateLunWarningForDiscardAfterDelete();
    }

    @Override
    public StorageModel getContainer() {
        return model.getContainer();
    }

    @Override
    public boolean isReduceDeviceSupported() {
        return model.isReduceDeviceSupported();
    }

    @Override
    public HasEntity<Boolean> getRequireTableRefresh() {
        return model.getRequireTableRefresh();
    }

    @Override
    public Set<String> getMetadataDevices() {
        return model.getMetadataDevices();
    }

    @Override
    public int getNumOfLUNsToRemove() {
        return model.getNumOfLUNsToRemove();
    }

    @Override
    public List<LunModel> getIncludedLuns() {
        return model.getIncludedLuns();
    }

    protected SanStorageModelBase getModel() {
        return model;
    }
}
