package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class EventModelProvider extends SearchableTabModelProvider<AuditLog, EventListModel<Void>> {

    @Inject
    public EventModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
    }

    @Override
    protected void initializeModelHandlers(final EventListModel<Void> model) {
        super.initializeModelHandlers(model);
        model.setDisplayEventsOnly(true);
        // Add necessary property change handlers
        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                // For EventListModel classes: update data whenever the last event changes
                if ("LastEvent".equals(args.propertyName)) { //$NON-NLS-1$
                    updateData();
                }
            }
        });
    }

    @Override
    protected boolean handleItemsChangedEvent() {
        return false;
    }
}
