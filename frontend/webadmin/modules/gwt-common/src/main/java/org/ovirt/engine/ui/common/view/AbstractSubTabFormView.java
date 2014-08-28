package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

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
        generateIds();
    }

    protected D getDetailModel() {
        return modelProvider.getModel();
    }

    @Override
    public ActionTable<?> getTable() {
        // Form-based sub tab views have no table widget associated
        return null;
    }

    protected abstract void generateIds();

}
