package org.ovirt.engine.ui.common.widget.uicommon;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractModelBoundTreeWidget<M extends SearchableListModel, R, N, T extends ListWithDetailsModel> extends Composite {

    private final EventBus eventBus;
    protected final SearchableDetailModelProvider<R, T, M> modelProvider;
    protected final CommonApplicationResources resources;
    protected final CommonApplicationConstants constants;

    private final AbstractSubTabTree<M, R, N> tree;

    public AbstractModelBoundTreeWidget(SearchableDetailModelProvider<R, T, M> modelProvider,
            EventBus eventBus, CommonApplicationResources resources,
            CommonApplicationConstants constants) {
        this.modelProvider = modelProvider;
        this.eventBus = eventBus;
        this.resources = resources;
        this.constants = constants;
        this.tree = createTree();
        initWidget(getWrappedWidget());
    }

    protected abstract AbstractSubTabTree<M, R, N> createTree();

    /**
     * @return Widget passed to the {@linkplain Composite#initWidget initWidget} method.
     */
    protected Widget getWrappedWidget() {
        return new SimplePanel(tree);
    }

    public AbstractSubTabTree<M, R, N> getTree() {
        return tree;
    }

    public M getModel() {
        return modelProvider.getModel();
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    public SearchableDetailModelProvider<R, T, M> getModelProvider() {
        return modelProvider;
    }

    public abstract void initTree(SubTabTreeActionPanel actionPanel, EntityModelCellTable<ListModel> table);

}
