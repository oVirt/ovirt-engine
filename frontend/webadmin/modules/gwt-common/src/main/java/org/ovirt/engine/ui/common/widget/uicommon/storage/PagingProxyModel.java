package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.Collection;
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
 * Create a proxy model that will intercept {@link SanStorageModelBase#getItems()} and filter the items that belong to
 * the current page. From a logical point of view all pages are still one model i.e. user changes are not lost when
 * changing page and submitting.
 */
public class PagingProxyModel implements SanStoragePartialModel {

    private final SanStorageModelBase model;
    private final PageFilter pageFilter;

    public PagingProxyModel(SanStorageModelBase model, PageFilter pageFilter) {
        this.model = model;
        this.pageFilter = pageFilter;
    }

    public static SanStoragePartialModel create(PageFilter pageFilter, SanStorageModelBase model) {
        return new PagingProxyModel(model, pageFilter);
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
    public Collection getItems() {
        return pageFilter.filter(model.getItems());
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
}
