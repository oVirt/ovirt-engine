package org.ovirt.engine.ui.userportal.uicommon.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.gin.BaseClientGinjector;
import org.ovirt.engine.ui.uicommonweb.models.userportal.IUserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

/**
 * Base class for {@link IUserPortalListModel} providers with {@linkplain #itemsChanged data update optimization}.
 *
 * @param <M>
 *            List model type.
 */
public abstract class AbstractUserPortalListProvider<M extends IUserPortalListModel> extends UserPortalDataBoundModelProvider<UserPortalItemModel, M> {

    private List<UserPortalItemModel> currentItems;

    public AbstractUserPortalListProvider(BaseClientGinjector ginjector, CurrentUser user) {
        super(ginjector, user);
    }

    @Override
    protected void updateDataProvider(List<UserPortalItemModel> items) {
        // First data update
        if (currentItems == null) {
            currentItems = items;
            super.updateDataProvider(items);
        }

        // Subsequent data update
        else if (itemsChanged(items, currentItems)) {
            super.updateDataProvider(items);
        }
    }

    /**
     * Returns {@code true} if there is a change between {@code newItems} and {@code oldItems}, {@code false} otherwise.
     */
    boolean itemsChanged(List<UserPortalItemModel> newItems, List<UserPortalItemModel> oldItems) {
        Map<Guid, UserPortalItemModel> oldItemMap = new HashMap<Guid, UserPortalItemModel>(oldItems.size());
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
