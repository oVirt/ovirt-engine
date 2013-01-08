package org.ovirt.engine.ui.userportal.uicommon.model.basic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceToGuestWithNonRespAgentModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncInfoModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.console.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VncInfoPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserPortalBasicListProvider extends UserPortalDataBoundModelProvider<UserPortalItemModel, UserPortalBasicListModel> {

    private Provider<VncInfoPopupPresenterWidget> vncInfoPopupProvider;
    private final Provider<ConsolePopupPresenterWidget> consolePopupProvider;
    private final Provider<DefaultConfirmationPopupPresenterWidget> spiceToGuestWithNonRespAgentPopupProvider;

    private List<UserPortalItemModel> currentItems;

    @Inject
    public UserPortalBasicListProvider(ClientGinjector ginjector,
            Provider<VncInfoPopupPresenterWidget> vncInfoPopupProvider,
            Provider<ConsolePopupPresenterWidget> consolePopupProvider,
            Provider<DefaultConfirmationPopupPresenterWidget> spiceToGuestWithNonRespAgentPopupProvider,
            CurrentUser user) {
        super(ginjector, user);
        this.vncInfoPopupProvider = vncInfoPopupProvider;
        this.consolePopupProvider = consolePopupProvider;
        this.spiceToGuestWithNonRespAgentPopupProvider = spiceToGuestWithNonRespAgentPopupProvider;
    }

    @Override
    protected UserPortalBasicListModel createModel() {
        return new UserPortalBasicListModel();
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

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UserPortalBasicListModel source,
            UICommand lastExecutedCommand,
            Model windowModel) {
        if (windowModel instanceof VncInfoModel) {
            return vncInfoPopupProvider.get();
        } else if (windowModel instanceof SpiceToGuestWithNonRespAgentModel) {
            return spiceToGuestWithNonRespAgentPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getEditConsoleCommand()) {
            return consolePopupProvider.get();
        }

        return super.getModelPopup(source, lastExecutedCommand, windowModel);
    }

}
