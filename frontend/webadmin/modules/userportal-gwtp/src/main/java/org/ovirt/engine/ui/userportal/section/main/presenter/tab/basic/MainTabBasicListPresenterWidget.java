package org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic;

import java.util.ArrayList;
import java.util.List;

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

    public interface ViewDef extends View {

        String getElementId();

    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_VmListContent = new Type<RevealContentHandler<?>>();

    private final Provider<MainTabBasicListItemPresenterWidget> basicVmPresenterWidgetProvider;

    private final UserPortalBasicListProvider modelProvider;

    private final List<MainTabBasicListItemPresenterWidget> currentItemPresenterWidgets = new ArrayList<MainTabBasicListItemPresenterWidget>();

    @Inject
    public MainTabBasicListPresenterWidget(EventBus eventBus, ViewDef view,
            Provider<MainTabBasicListItemPresenterWidget> basicVmPresenterWidgetProvider,
            UserPortalBasicListProvider modelProvider) {
        super(eventBus, view);
        this.basicVmPresenterWidgetProvider = basicVmPresenterWidgetProvider;
        this.modelProvider = modelProvider;
        modelProvider.setDataChangeListener(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
    }

    @Override
    public void onDataChange(List<UserPortalItemModel> items) {
        // TODO optimize
        //Cleanup existing item presenter widgets.
        for (MainTabBasicListItemPresenterWidget itemPresenterwidget : currentItemPresenterWidgets) {
            itemPresenterwidget.unbind();
            removeFromSlot(TYPE_VmListContent, itemPresenterwidget);
        }
        clearSlot(TYPE_VmListContent);
        currentItemPresenterWidgets.clear();

        int vmIndex = 0;
        for (UserPortalItemModel item : items) {
            MainTabBasicListItemPresenterWidget basicVmPresenterWidget = basicVmPresenterWidgetProvider.get();
            basicVmPresenterWidget.getView().setElementId(
                    ElementIdUtils.createElementId(getView().getElementId(), "vm" + vmIndex++)); //$NON-NLS-1$
            basicVmPresenterWidget.setModel(item);
            currentItemPresenterWidgets.add(basicVmPresenterWidget);
            addToSlot(TYPE_VmListContent, basicVmPresenterWidget);
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
