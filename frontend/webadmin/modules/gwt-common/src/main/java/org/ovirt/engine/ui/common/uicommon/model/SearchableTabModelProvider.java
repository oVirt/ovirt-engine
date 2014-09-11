package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;

/**
 * Default {@link SearchableTableModelProvider} implementation for use with tab controls.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public abstract class SearchableTabModelProvider<T, M extends SearchableListModel> extends DataBoundTabModelProvider<T, M> implements SearchableTableModelProvider<T, M> {

    public SearchableTabModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
    }

    @Override
    protected void initializeModelHandlers() {
        super.initializeModelHandlers();

        // Add necessary property change handlers
        getModel().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                // For EventListModel classes: update data whenever the last event changes
                if (getModel() instanceof EventListModel && "LastEvent".equals(args.propertyName)) { //$NON-NLS-1$
                    EventListModel model = (EventListModel) getModel();

                    if (model.getLastEvent() == null && model.isRequestingData()) {
                        // Tell data provider we await further data
                        clearData();
                    } else {
                        // Data has arrived, update data provider
                        updateData();
                    }
                }
                if (PropertyChangedEventArgs.PROGRESS.equals(args.propertyName)) {
                    clearData();
                }
            }
        });
    }

    @Override
    void clearData() {
        // Remove locally cached row data and enforce "loading" state
        getDataProvider().updateRowCount(0, false);
    }

    @Override
    protected boolean handleItemsChangedEvent() {
        return getModel() instanceof EventListModel ? false : true;
    }

}
