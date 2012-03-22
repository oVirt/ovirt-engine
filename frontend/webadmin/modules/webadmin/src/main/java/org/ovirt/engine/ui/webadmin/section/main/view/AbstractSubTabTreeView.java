package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractSubTabTreeView<E extends AbstractSubTabTree, I, T, M extends ListWithDetailsModel, D extends SearchableListModel> extends AbstractSubTabTableView<I, T, M, D> {

    interface ViewUiBinder extends UiBinder<Widget, AbstractSubTabTreeView> {
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

    protected SubTabTreeActionPanel actionPanel;

    protected EntityModelCellTable<ListModel> table;

    protected E tree;

    boolean isActionTree;

    protected final ApplicationResources resources;
    protected final ApplicationConstants constants;

    public AbstractSubTabTreeView(SearchableDetailModelProvider modelProvider) {
        super(modelProvider);

        resources = ClientGinjectorProvider.instance().getApplicationResources();
        constants = ClientGinjectorProvider.instance().getApplicationConstants();

        table = new EntityModelCellTable<ListModel>(false, true);
        tree = getTree();

        initHeader();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        headerTableContainer.add(table);
        treeContainer.add(tree);

        getDetailModel().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                table.setRowData(new ArrayList<EntityModel>());
            }
        });

        actionPanel = createActionPanel(modelProvider);
        if (actionPanel != null) {
            actionPanelContainer.add(actionPanel);
            actionPanel.addContextMenuHandler(tree);
        }

        updateStyles();
    }

    private void updateStyles() {
        treeContainer.addStyleName(isActionTree ? style.actionTreeContainer() : style.treeContainer());
    }

    public void setIsActionTree(boolean isActionTree) {
        this.isActionTree = isActionTree;

        updateStyles();
    }

    @Override
    public void setMainTabSelectedItem(I selectedItem) {
        table.setLoadingState(LoadingState.LOADING);
        tree.clearTree();
        tree.updateTree(getDetailModel());
    }

    protected abstract void initHeader();

    protected abstract E getTree();

    protected SubTabTreeActionPanel createActionPanel(SearchableDetailModelProvider modelProvider) {
        return null;
    }

    interface WidgetStyle extends CssResource {
        String treeContainer();

        String actionTreeContainer();
    }
}
