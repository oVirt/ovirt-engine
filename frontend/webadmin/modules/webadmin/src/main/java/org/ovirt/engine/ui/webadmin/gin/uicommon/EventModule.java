package org.ovirt.engine.ui.webadmin.gin.uicommon;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;

public class EventModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<AuditLog, EventListModel> getEventListProvider(ClientGinjector ginjector,
            final Provider<EventPopupPresenterWidget> popupProvider) {
        return new MainTabModelProvider<AuditLog, EventListModel>(ginjector, EventListModel.class) {
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
    }

    @Override
    protected void configure() {
    }

}
