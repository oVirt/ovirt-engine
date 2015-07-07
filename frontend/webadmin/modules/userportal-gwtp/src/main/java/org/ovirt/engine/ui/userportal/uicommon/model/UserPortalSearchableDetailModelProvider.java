package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A {@link SearchableDetailModelProvider} implementation that uses {@link UserPortalModelResolver} to retrieve UiCommon
 * {@link SearchableListModel}.
 *
 * @param <T>
 *            Detail model item type.
 * @param <M>
 *            Parent model type.
 * @param <D>
 *            Detail model type.
 */
public class UserPortalSearchableDetailModelProvider<T, M extends ListWithDetailsModel, D extends SearchableListModel>
        extends UserPortalDataBoundModelProvider<T, D> implements SearchableDetailModelProvider<T, M, D> {

    private Provider<M> parentModelProvider;

    @Inject
    public UserPortalSearchableDetailModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user) {
        super(eventBus, defaultConfirmPopupProvider, user);
    }

    protected M getParentModel() {
        return parentModelProvider.get();
    }

    @Override
    public void onSubTabSelected() {
        getParentModel().setActiveDetailModel(getModel());
    }

    @Override
    public void onSubTabDeselected() {
        getParentModel().setActiveDetailModel(null);
    }

    @Inject
    public void setParentModelProvider(Provider<M> parentModelProvider) {
        this.parentModelProvider = parentModelProvider;
    }

}
