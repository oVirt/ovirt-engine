package org.ovirt.engine.ui.common.widget.tree;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.AbstractActionPanel;
import org.ovirt.engine.ui.common.widget.action.ActionButton;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.SimpleActionButton;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class AbstractSubTabActionTree<M extends SearchableListModel, R, N> extends AbstractActionPanel {

    interface ViewUiBinder extends UiBinder<Widget, AbstractSubTabActionTree> {
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
    protected AbstractSubTabTree<M, R, N> tree;

    protected final EventBus eventBus;
    protected SearchableDetailModelProvider<?, ?, M> modelProvider;

    protected final CommonApplicationResources resources;
    protected final CommonApplicationConstants constants;

    public AbstractSubTabActionTree(SearchableDetailModelProvider<?, ?, M> modelProvider,
            EventBus eventBus, CommonApplicationResources resources,
            CommonApplicationConstants constants) {
        super(modelProvider, eventBus);

        this.eventBus = eventBus;
        this.modelProvider = modelProvider;
        this.resources = resources;
        this.constants = constants;

        // ViewUiBinder.uiBinder.createAndBindUi(this);

        table = new EntityModelCellTable<ListModel>(false, true);
        initHeader();

        headerTableContainer.add(table);
        treeContainer.setWidget(tree);

        modelProvider.getModel().getItemsChangedEvent().addListener(new IEventListener() {
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

    @Override
    protected void initWidget(Widget widget) {
        ViewUiBinder.uiBinder.createAndBindUi(this);
    }

    private void updateStyles() {
        treeContainer.addStyleName(style.actionTreeContainer());
    }

    protected void initHeader() {

    }

    protected SubTabTreeActionPanel createActionPanel(SearchableDetailModelProvider<?, ?, M> modelProvider) {
        return null;
    }

    interface WidgetStyle extends CssResource {
        String treeContainer();

        String actionTreeContainer();
    }

    @Override
    protected List getSelectedItems() {
        return null;
    }

    @Override
    protected ActionButton createNewActionButton(ActionButtonDefinition buttonDef) {
        return new SimpleActionButton();
    }
}
