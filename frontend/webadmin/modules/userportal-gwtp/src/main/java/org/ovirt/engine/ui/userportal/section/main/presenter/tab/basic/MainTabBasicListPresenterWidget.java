package org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalBasicListProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider.DataChangeListener;

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
        void clear();
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_VmListContent = new Type<RevealContentHandler<?>>();

    private final Provider<MainTabBasicListItemPresenterWidget> basicVmPresenterWidgetProvider;

    @Inject
    public MainTabBasicListPresenterWidget(EventBus eventBus, ViewDef view,
            Provider<MainTabBasicListItemPresenterWidget> basicVmPresenterWidgetProvider,
            UserPortalBasicListProvider modelProvider) {
        super(eventBus, view);
        this.basicVmPresenterWidgetProvider = basicVmPresenterWidgetProvider;
        modelProvider.setDataChangeListener(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
    }

    @Override
    public void onDataChange(List<UserPortalItemModel> items) {
        getView().clear();
        for (UserPortalItemModel item : items) {
            MainTabBasicListItemPresenterWidget basicVmPresenterWidget = basicVmPresenterWidgetProvider.get();
            basicVmPresenterWidget.setModel(item);
            addToSlot(TYPE_VmListContent, basicVmPresenterWidget);
        }
    }

}
