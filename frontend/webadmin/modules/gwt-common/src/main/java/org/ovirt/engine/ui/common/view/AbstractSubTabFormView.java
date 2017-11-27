package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.presenter.PlaceTransitionHandler;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;

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
public abstract class AbstractSubTabFormView<T, M extends ListWithDetailsModel, D extends HasEntity>
        extends AbstractView implements AbstractSubTabPresenter.ViewDef<T> {

    private static final String OBRAND_DETAIL_TAB = "obrand_detail_tab"; // $NON-NLS-1$

    private final DetailModelProvider<M, D> modelProvider;

    private PlaceTransitionHandler placeTransitionHandler;

    public AbstractSubTabFormView(DetailModelProvider<M, D> modelProvider) {
        this.modelProvider = modelProvider;
    }

    @Override
    protected void initWidget(IsWidget widget) {
        super.initWidget(widget);
        asWidget().addStyleName(OBRAND_DETAIL_TAB);
    }

    @Override
    public HandlerRegistration addWindowResizeHandler(ResizeHandler handler) {
        return Window.addResizeHandler(handler);
    }

    @Override
    public void resizeToFullHeight() {
        // No op, doesn't need to manually resize.
    }

    protected D getDetailModel() {
        return modelProvider.getModel();
    }

    @Override
    public ActionTable<?> getTable() {
        // Form-based sub tab views have no table widget associated
        return null;
    }

    @Override
    public IsWidget getTableContainer() {
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

    @Override
    public void setPlaceTransitionHandler(PlaceTransitionHandler handler) {
        placeTransitionHandler = handler;
    }

    protected PlaceTransitionHandler getPlaceTransitionHandler() {
        return placeTransitionHandler;
    }
}
