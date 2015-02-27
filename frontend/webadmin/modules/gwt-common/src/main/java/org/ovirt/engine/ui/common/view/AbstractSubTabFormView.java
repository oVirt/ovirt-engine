package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
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
public abstract class AbstractSubTabFormView<T, M extends ListWithDetailsModel, D extends HasEntity> extends AbstractView implements AbstractSubTabPresenter.ViewDef<T> {

    private final DetailModelProvider<M, D> modelProvider;

    public AbstractSubTabFormView(DetailModelProvider<M, D> modelProvider) {
        this.modelProvider = modelProvider;
    }

    protected D getDetailModel() {
        return modelProvider.getModel();
    }

    @Override
    public ActionTable<?> getTable() {
        // Form-based sub tab views have no table widget associated
        return null;
    }

    /**
     * Call this to invoke the Element ID Framework's annotation processor.
     * This will set IDs on all elements annotated with {@link WithElementId}.
     * Only call after UIBinder.createAndBindUi() is called -- otherwise elements
     * may still be null and thus cannot have an ID set.
     * <p>
     * A typical implementation is:
     * <pre>ViewIdHandler.idHandler.generateAndSetIds(this);</pre>
     */
    protected abstract void generateIds();

}
