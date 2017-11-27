package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.presenter.PlaceTransitionHandler;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Detail Tab that contains list views. This will automatically resize based on the size of the window to properly
 * provide scroll-bars so that the action buttons do not scroll while the list view is scrolling.
 *
 * @param <T> Main Table row type, eg. VMs
 * @param <M> Main model list model type. eg. VmListModel
 * @param <D> Detail model type.
 */
public abstract class AbstractDetailTabListView<T, M extends ListWithDetailsModel, D extends HasEntity>
    extends AbstractView implements AbstractSubTabPresenter.ViewDef<T> {

    private final DetailModelProvider<M, D> modelProvider;
    private final ScrollPanel scrollPanel = new ScrollPanel();
    private final FlowPanel container = new FlowPanel();
    private PlaceTransitionHandler placeTransitionHandler;

    public AbstractDetailTabListView(DetailModelProvider<M, D> modelProvider) {
        this.modelProvider = modelProvider;
    }

    @Override
    public HandlerRegistration addWindowResizeHandler(ResizeHandler handler) {
        return Window.addResizeHandler(handler);
    }

    protected D getDetailModel() {
        return modelProvider.getModel();
    }

    @Override
    public void onAttach() {
        super.onAttach();
        resizeToFullHeight();
    }

    @Override
    public ActionTable<?> getTable() {
        // ListView detail tab views have no table widget associated
        return null;
    }

    @Override
    public IsWidget getTableContainer() {
        // ListView detail tab views have no table widget associated
        return null;
    }

    @Override
    protected void initWidget(IsWidget widget) {
        container.add(scrollPanel);
        super.initWidget(container);
    }

    protected SimplePanel getContentPanel() {
        return scrollPanel;
    }

    protected FlowPanel getContainer() {
        return container;
    }

    private int calculateTotalAvailableSpace() {
        return Window.getClientHeight() - getContentPanel().getAbsoluteTop();
    }

    @Override
    public void resizeToFullHeight() {
        scrollPanel.setHeight(calculateTotalAvailableSpace() + Unit.PX.getType());
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
