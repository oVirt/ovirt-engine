package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.shared.EventBus;
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
public abstract class UserPortalSearchableDetailModelProvider<T, M extends ListWithDetailsModel, D extends SearchableListModel>
        extends UserPortalDataBoundModelProvider<T, D> implements SearchableDetailModelProvider<T, M, D> {

    private final ModelProvider<M> parentModelProvider;
    private final Class<D> detailModelClass;
    private final UserPortalModelResolver modelResolver;

    public UserPortalSearchableDetailModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            ModelProvider<M> parentModelProvider, Class<D> detailModelClass,
            UserPortalModelResolver modelResolver) {
        super(eventBus, defaultConfirmPopupProvider, user);
        this.parentModelProvider = parentModelProvider;
        this.detailModelClass = detailModelClass;
        this.modelResolver = modelResolver;
    }

    @Override
    public D getModel() {
        return modelResolver.<D, M> getDetailModel(detailModelClass, parentModelProvider);
    }

    protected M getParentModel() {
        return parentModelProvider.getModel();
    }

    @Override
    public void onSubTabSelected() {
        getParentModel().setActiveDetailModel(getModel());
    }

    @Override
    public void onSubTabDeselected() {
        getParentModel().setActiveDetailModel(null);
    }

}
