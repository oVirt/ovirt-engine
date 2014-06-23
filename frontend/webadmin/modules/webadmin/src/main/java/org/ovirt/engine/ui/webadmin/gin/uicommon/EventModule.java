package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class EventModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<AuditLog, EventListModel> getEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> popupProvider,
            final Provider<EventListModel> modelProvider,
            final Provider<CommonModel> commonModelProvider) {
        MainTabModelProvider<AuditLog, EventListModel> result =
                new MainTabModelProvider<AuditLog, EventListModel>(eventBus, defaultConfirmPopupProvider,
                        commonModelProvider) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(EventListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getDetailsCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }
        };
        result.setModelProvider(modelProvider);
        return result;
    }

    @Override
    protected void configure() {
        bind(EventListModel.class).in(Singleton.class);
    }

}
