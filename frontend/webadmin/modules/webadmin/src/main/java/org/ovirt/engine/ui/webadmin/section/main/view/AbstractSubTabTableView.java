package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.presenter.PlaceTransitionHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Base class for sub tab views that use {@link SimpleActionTable} directly.
 *
 * @param <I> Main tab table row data type.
 * @param <T> Sub tab table row data type.
 * @param <M> Main model type (extends ListWithDetailsModel)
 * @param <D> Detail model type (extends SearchableListModel)
 */
public abstract class AbstractSubTabTableView<I, T, M extends ListWithDetailsModel, D extends SearchableListModel>
    extends AbstractView implements AbstractSubTabPresenter.ViewDef<I> {

    private static final String OBRAND_DETAIL_TAB = "obrand_detail_tab"; // $NON-NLS-1$

    private final SearchableDetailModelProvider<T, M, D> modelProvider;

    @WithElementId
    public final SimpleActionTable<I, T> table;

    private final FlowPanel container = new FlowPanel();
    private final SimplePanel actionPanelContainer = new SimplePanel();
    private final SimplePanel contentContainer = new SimplePanel();

    private PlaceTransitionHandler placeTransitionHandler;

    public AbstractSubTabTableView(SearchableDetailModelProvider<T, M, D> modelProvider) {
        this.modelProvider = modelProvider;
        this.table = createActionTable();
        container.add(actionPanelContainer);
        container.add(contentContainer);
        container.add(table);
        container.addStyleName(OBRAND_DETAIL_TAB);
        bindSlot(MainContentPresenter.TYPE_SetContent, contentContainer);
        bindSlot(AbstractSubTabPresenter.TYPE_SetActionPanel, actionPanelContainer);
        generateIds();
    }

    @Override
    public HandlerRegistration addWindowResizeHandler(ResizeHandler handler) {
        return Window.addResizeHandler(handler);
    }

    @Override
    public void resizeToFullHeight() {
        int tableTop = table.getTableAbsoluteTop();
        if (tableTop > 0) {
            table.setMaxGridHeight(Window.getClientHeight() - tableTop);
            table.updateGridSize();
        }
    }

    protected SimpleActionTable<I, T> createActionTable() {
        return new SimpleActionTable<I, T>(modelProvider, getTableResources(),
                ClientGinjectorProvider.getEventBus(), ClientGinjectorProvider.getClientStorage()) {
            {
                if (useTableWidgetForContent()) {
                    enableHeaderContextMenu();
                }
            }
        };
    }

    /**
     * Returns {@code true} if table content is provided by the {@link #table} widget itself.
     * Returns {@code false} if table content is provided by a custom widget, e.g. a tree.
     */
    protected boolean useTableWidgetForContent() {
        return true;
    }

    protected Resources getTableResources() {
        return GWT.create(SubTableResources.class);
    }

    protected D getDetailModel() {
        return modelProvider.getModel();
    }

    @Override
    public SimpleActionTable<I, T> getTable() {
        return table;
    }

    @Override
    public IsWidget getTableContainer() {
        return container;
    }

    protected SearchableDetailModelProvider<T, M, D> getModelProvider() {
        return this.modelProvider;
    }

    @Override
    public void setMainSelectedItem(I selectedItem) {
        // No-op since table-based sub tab views don't handle main tab selection on their own
    }

    @Override
    public void setPlaceTransitionHandler(PlaceTransitionHandler handler) {
        placeTransitionHandler = handler;
    }

    protected PlaceTransitionHandler getPlaceTransitionHandler() {
        return placeTransitionHandler;
    }

    protected abstract void generateIds();
}
