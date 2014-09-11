package org.ovirt.engine.ui.common.view;

import java.util.ArrayList;

import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTreeWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for sub tab views that use {@linkplain AbstractModelBoundTreeWidget model-bound table widgets}.
 *
 * @param <I>
 *            Main tab table row data type.
 * @param <T>
 *            Sub tab table row data type.
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public class AbstractSubTabTreeWidgetView<I, T, M extends ListWithDetailsModel, D extends SearchableListModel> extends AbstractView implements AbstractSubTabPresenter.ViewDef<I> {

    interface ViewUiBinder extends UiBinder<Widget, AbstractSubTabTreeWidgetView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    protected SimplePanel headerTableContainer;

    @UiField
    protected SimplePanel treeContainer;

    @UiField
    protected SimplePanel actionPanelContainer;

    private final EventBus eventBus;

    @WithElementId
    public final SubTabTreeActionPanel actionPanel;

    protected final EntityModelCellTable<ListModel> table;
    protected final AbstractSubTabTree tree;
    protected final AbstractModelBoundTreeWidget modelBoundTreeWidget;

    public AbstractSubTabTreeWidgetView(AbstractModelBoundTreeWidget modelBoundTreeWidget, EventBus eventBus) {
        this.eventBus = eventBus;
        this.modelBoundTreeWidget = modelBoundTreeWidget;
        this.table = new EntityModelCellTable<ListModel>(false, true);
        this.tree = modelBoundTreeWidget.getTree();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        headerTableContainer.add(table);
        treeContainer.add(tree);

        modelBoundTreeWidget.getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                table.setRowData(new ArrayList<EntityModel>());
            }
        });

        actionPanel = createActionPanel(modelBoundTreeWidget.getModelProvider());
        if (actionPanel != null) {
            actionPanelContainer.add(actionPanel);
            actionPanel.addContextMenuHandler(tree);
        }

        updateStyles();
    }

    protected AbstractModelBoundTreeWidget<D, I, T, M> getModelBoundTreeWidget() {
        return modelBoundTreeWidget;
    }

    AbstractSubTabTree getTree() {
        return modelBoundTreeWidget.getTree();
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public ActionTable<?> getTable() {
        // Tree-based sub tab views have no table widget associated
        return null;
    }

    @Override
    public void setMainTabSelectedItem(I selectedItem) {
        if (getModelBoundTreeWidget().getModel().getItems() == null) {
            table.setLoadingState(LoadingState.LOADING);
        }
        tree.clearTree();
        tree.updateTree(getModelBoundTreeWidget().getModel());
    }

    private void updateStyles() {
        treeContainer.addStyleName(style.actionTreeContainer());
    }

    protected SubTabTreeActionPanel createActionPanel(SearchableDetailModelProvider<I, M, D> modelProvider) {
        return null;
    }

    interface WidgetStyle extends CssResource {
        String treeContainer();

        String actionTreeContainer();
    }

}
