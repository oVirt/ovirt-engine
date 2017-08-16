package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainViewModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class EventModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<AuditLog, EventListModel<Void>> getEventListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EventPopupPresenterWidget> popupProvider,
            @Named("main") final Provider<EventListModel<Void>> modelProvider) {
        MainViewModelProvider<AuditLog, EventListModel<Void>> result =
                new MainViewModelProvider<AuditLog, EventListModel<Void>>(eventBus, defaultConfirmPopupProvider) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(EventListModel<Void> source,
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

    @Provides
    @Singleton
    @Named("notification")
    public EventModelProvider getEventModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            @Named("notification") final Provider<EventListModel<Void>> modelProvider) {
        EventModelProvider result = new EventModelProvider(eventBus, defaultConfirmPopupProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<EventListModel<Void>>(){})
            .annotatedWith(Names.named("main")).to(new TypeLiteral<EventListModel<Void>>(){}) //$NON-NLS-1$
            .in(Singleton.class);
        bind(new TypeLiteral<EventListModel<Void>>(){})
            .annotatedWith(Names.named("notification")).to(new TypeLiteral<EventListModel<Void>>(){}) //$NON-NLS-1$
            .in(Singleton.class);
    }

}
