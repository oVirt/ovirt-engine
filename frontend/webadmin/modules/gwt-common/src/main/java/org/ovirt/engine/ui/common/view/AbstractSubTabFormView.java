package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;

/**
 * Base class for form-based {@link AbstractSubTabPresenter} views.
 *
 * @param <T>
 *            Main tab table row data type.
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public abstract class AbstractSubTabFormView<T, M extends ListWithDetailsModel, D extends EntityModel> extends AbstractView implements AbstractSubTabPresenter.ViewDef<T> {

    private final DetailModelProvider<M, D> modelProvider;

    public AbstractSubTabFormView(DetailModelProvider<M, D> modelProvider) {
        this.modelProvider = modelProvider;
    }

    protected D getDetailModel() {
        return modelProvider.getModel();
    }

    @Override
    public OrderedMultiSelectionModel<?> getTableSelectionModel() {
        // Form-based sub tab views have no table selection model
        return null;
    }

    @Override
    public void setLoadingState(LoadingState state) {
        // Form-based sub tab views don't indicate progress
    }

}
