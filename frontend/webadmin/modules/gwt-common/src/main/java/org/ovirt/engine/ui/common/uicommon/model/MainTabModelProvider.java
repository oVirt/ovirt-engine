package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;

/**
 * Default {@link MainModelProvider} implementation for use with tab controls.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public class MainTabModelProvider<T, M extends SearchableListModel> extends SearchableTabModelProvider<T, M> implements MainModelProvider<T, M> {

    private final Class<M> mainModelClass;

    public MainTabModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            Class<M> mainModelClass) {
        super(eventBus, defaultConfirmPopupProvider);
        this.mainModelClass = mainModelClass;
    }

    @Override
    public M getModel() {
        return UiCommonModelResolver.getMainListModel(getCommonModel(), mainModelClass);
    }

    @Override
    public void onMainTabSelected() {
        getCommonModel().setSelectedItem(getModel());
    }

}
