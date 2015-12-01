package org.ovirt.engine.ui.userportal.uicommon.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.userportal.AbstractUserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;

/**
 * Base class for {@link org.ovirt.engine.ui.uicommonweb.models.userportal.AbstractUserPortalListModel} providers with {@linkplain #itemsChanged data update optimization}.
 *
 * @param <M>
 *            List model type.
 */
public abstract class AbstractUserPortalListProvider<M extends AbstractUserPortalListModel>
    extends UserPortalDataBoundModelProvider<UserPortalItemModel, M>
        implements UserPortalSearchableTableModelProvider<UserPortalItemModel, M> {

    private List<UserPortalItemModel> currentItems;

    /**
     * Force an update of the view regardless of if anything has changed.
     */
    private boolean forceUpdate = false;

    public AbstractUserPortalListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user) {
        super(eventBus, defaultConfirmPopupProvider, user);
    }

    @Override
    protected void updateDataProvider(List<UserPortalItemModel> items) {
        // First data update
        if (currentItems == null) {
            currentItems = items;
            super.updateDataProvider(items);
        }

        // Subsequent data update, with item change
        else if (forceUpdate || itemsChanged(items, currentItems)) {
            clearReferences();
            super.updateDataProvider(items);
        }

        // Subsequent data update, without item change
        else {
            retainSelectedItems();
        }

        forceUpdate = false;
        currentItems = items;
    }

    private void clearReferences() {
        if (currentItems != null) {
            for(UserPortalItemModel itemModel: currentItems) {
                itemModel.clearReferences();
            }
        }
    }

    @Override
    public void clearCurrentItems() {
        clearReferences();
        currentItems = null;
    }

    /**
     * Instead of clearing the items in the model, just mark a flag that forces the
     * view to update the grid when updateProvider is called.
     */
    @Override
    public void onManualRefresh() {
        forceUpdate = true;
    }

    /**
     * Returns {@code true} if there is a change between {@code newItems} and {@code oldItems}, {@code false} otherwise.
     */
    boolean itemsChanged(List<UserPortalItemModel> newItems, List<UserPortalItemModel> oldItems) {
        Map<Guid, UserPortalItemModel> oldItemMap = new HashMap<>(oldItems.size());
        for (UserPortalItemModel oldItem : oldItems) {
            oldItemMap.put(oldItem.getId(), oldItem);
        }

        for (UserPortalItemModel newItem : newItems) {
            Guid newItemId = newItem.getId();
            UserPortalItemModel oldItem = oldItemMap.get(newItemId);

            // Return true in case of new item or item change
            if (oldItem == null || !newItem.entityStateEqualTo(oldItem)) {
                return true;
            }

            oldItemMap.remove(newItemId);
        }

        // Return true in case there are no more old items left (to remove)
        return !oldItemMap.isEmpty();
    }

}
