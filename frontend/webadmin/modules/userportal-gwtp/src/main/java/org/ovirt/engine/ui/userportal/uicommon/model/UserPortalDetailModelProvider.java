package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.TabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A {@link DetailModelProvider} implementation that uses {@link UserPortalModelResolver} to retrieve UiCommon
 * {@link EntityModel}.
 *
 * @param <M>
 *            Parent model type.
 * @param <D>
 *            Detail model type.
 */
public class UserPortalDetailModelProvider<M extends ListWithDetailsModel, D extends EntityModel> extends TabModelProvider<D> implements DetailModelProvider<M, D> {

    private Provider<M> parentModelProvider;

    @Inject
    public UserPortalDetailModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
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
