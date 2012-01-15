package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.webadmin.view.AbstractView;
import org.ovirt.engine.ui.webadmin.widget.table.OrderedMultiSelectionModel;

import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;

/**
 * Base class for form-based sub tab views.
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
        setLoadingState(state);
    }

}
