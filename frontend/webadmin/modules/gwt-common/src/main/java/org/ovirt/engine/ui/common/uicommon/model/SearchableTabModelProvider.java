package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

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
    protected void clearData() {
        // Remove locally cached row data and enforce "loading" state
        getDataProvider().updateRowCount(0, false);
    }
}
