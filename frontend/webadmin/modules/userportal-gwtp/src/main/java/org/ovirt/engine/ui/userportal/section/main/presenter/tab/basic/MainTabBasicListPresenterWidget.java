package org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.idhandler.ProvidesElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider.DataChangeListener;
import org.ovirt.engine.ui.userportal.uicommon.model.basic.UserPortalBasicListProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class MainTabBasicListPresenterWidget extends PresenterWidget<MainTabBasicListPresenterWidget.ViewDef> implements DataChangeListener<UserPortalItemModel> {

    public interface ViewDef extends View, ProvidesElementId {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_VmListContent = new Type<>();

    private final Provider<MainTabBasicListItemPresenterWidget> itemPresenterWidgetProvider;
    private final UserPortalBasicListProvider modelProvider;

    private final Map<Guid, MainTabBasicListItemPresenterWidget> currentItemPresenterWidgets = new HashMap<>();

    @Inject
    public MainTabBasicListPresenterWidget(EventBus eventBus, ViewDef view,
            Provider<MainTabBasicListItemPresenterWidget> itemPresenterWidgetProvider,
            UserPortalBasicListProvider modelProvider) {
        super(eventBus, view);
        this.itemPresenterWidgetProvider = itemPresenterWidgetProvider;
        this.modelProvider = modelProvider;
        modelProvider.setDataChangeListener(this);
    }

    @Override
    public void onDataChange(List<UserPortalItemModel> items) {
        int itemIndex = 0;
        Set<Guid> itemsToRemove = new HashSet<>(currentItemPresenterWidgets.keySet());

        // Clear the list view, detaching any existing item views
        clearSlot(TYPE_VmListContent);

        // Process newly received data
        for (UserPortalItemModel newItem : items) {
            Guid newItemId = newItem.getId();
            MainTabBasicListItemPresenterWidget itemPresenterWidget = currentItemPresenterWidgets.get(newItemId);

            // Create new item presenter widget, if necessary
            if (itemPresenterWidget == null) {
                itemPresenterWidget = itemPresenterWidgetProvider.get();
                currentItemPresenterWidgets.put(newItemId, itemPresenterWidget);
            }

            // Initialize item presenter widget with new data
            itemPresenterWidget.getView().setElementId(ElementIdUtils.createElementId(
                            getView().getElementId(), "vm" + itemIndex)); //$NON-NLS-1$
            itemPresenterWidget.setModel(newItem);

            // Update the list view, attaching current item view
            addToSlot(TYPE_VmListContent, itemPresenterWidget);

            itemsToRemove.remove(newItemId);
            itemIndex++;
        }

        // Cleanup old data
        for (Guid oldItemId : itemsToRemove) {
            MainTabBasicListItemPresenterWidget itemPresenterWidget = currentItemPresenterWidgets.get(oldItemId);
            itemPresenterWidget.unbind();
            currentItemPresenterWidgets.remove(oldItemId);
        }

        selectDefault(modelProvider.getModel(), items);
    }

    /**
     * When there is nothing selected, selects the first. When there is something selected, does nothing.
     */
    private void selectDefault(UserPortalBasicListModel model, List<UserPortalItemModel> items) {
        if (model.getSelectedItem() != null) {
            return;
        }

        if (items == null || items.size() == 0) {
            return;
        }

        UserPortalItemModel item = items.iterator().next();
        if (item == null) {
            return;
        }

        model.setSelectedItem(item);
    }

}
