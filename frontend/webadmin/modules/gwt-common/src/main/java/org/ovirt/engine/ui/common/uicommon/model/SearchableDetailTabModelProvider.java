package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Default {@link SearchableDetailModelProvider} implementation for use with tab controls.
 *
 * @param <T>
 *            Detail model item type.
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public class SearchableDetailTabModelProvider<T, M extends ListWithDetailsModel, D extends SearchableListModel> extends SearchableTabModelProvider<T, D> implements SearchableDetailModelProvider<T, M, D> {

    private Provider<M> mainModelProvider;

    @Inject
    public SearchableDetailTabModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
    }

    @Override
    public M getMainModel() {
        return mainModelProvider.get();
    }

    @Override
    public void onSubTabSelected() {
        getMainModel().setActiveDetailModel(getModel());
    }

    @Override
    public void onSubTabDeselected() {
        getMainModel().setActiveDetailModel(null);
    }

    public void activateDetailModel() {
        getMainModel().addActiveDetailModel(getModel());
    }

    @Inject
    public void setMainModelProvider(Provider<M> mainModelProvider) {
        this.mainModelProvider = mainModelProvider;
    }
}
