package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

/**
 * Subset of {@link SanStorageModelBase} currently used in the UI.
 */
public interface SanStoragePartialModel {
    void setMultiSelection(boolean multiSelection);

    Event<EventArgs> getItemsChangedEvent();

    Collection getItems();

    void setSelectedLunWarning(String warning);

    void updateLunWarningForDiscardAfterDelete();

    StorageModel getContainer();

    boolean isReduceDeviceSupported();

    HasEntity<Boolean> getRequireTableRefresh();

    Set<String> getMetadataDevices();

    int getNumOfLUNsToRemove();

    List<LunModel> getIncludedLuns();
}
