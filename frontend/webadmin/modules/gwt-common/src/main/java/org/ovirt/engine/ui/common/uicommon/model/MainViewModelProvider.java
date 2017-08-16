package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Default {@link MainModelProvider} implementation for use with tab controls.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public class MainViewModelProvider<T, M extends SearchableListModel> extends SearchableTabModelProvider<T, M> implements MainModelProvider<T, M> {

    @Inject
    public MainViewModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
    }

    @Override
    public void onMainViewSelected() {
        getModel().executeCommand(getModel().getSearchCommand());
    }

}
