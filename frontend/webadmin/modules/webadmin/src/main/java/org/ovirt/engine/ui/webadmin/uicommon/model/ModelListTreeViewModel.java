package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.TreeViewModel;

/**
 * A TreeView Model for {@link TreeNodeModel<T>} Nodes
 */
public class ModelListTreeViewModel<T, M extends TreeNodeModel<T, M>> implements TreeViewModel {

    private final class NodeSelectionHandler implements SelectionHandler<M> {
        private final HasData<M> display;

        private NodeSelectionHandler(HasData<M> display) {
            this.display = display;
        }

        @Override
        public void onSelection(SelectionEvent<M> event) {
            M selectedItem = event.getSelectedItem();
            display.getSelectionModel().setSelected(selectedItem,
                    selectedItem.getSelected());
        }
    }

    private final class CellLabel extends TextColumn<M> {
        @Override
        public String getValue(M object) {
            return object.getName();
        }
    }

    private final class CheckboxColumn implements HasCell<M, Boolean> {

        private final CheckboxCell cell = new ExCheckboxCell(true, false);

        @Override
        public Cell<Boolean> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<M, Boolean> getFieldUpdater() {
            return null;
        }

        @Override
        public Boolean getValue(M object) {
            return selectionModel.isSelected(object);
        }
    }

    /**
     * A {@link CheckboxCell} that can be disabled
     */
    private final class ExCheckboxCell extends CheckboxCell {
        /**
         * An html string representation of a disabled checked input box.
         */
        private final SafeHtml INPUT_CHECKED_DISABLED =
                SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled/>"); //$NON-NLS-1$
        /**
         * An html string representation of a disabled unchecked input box.
         */
        private final SafeHtml INPUT_UNCHECKED_DISABLED =
                SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled/>"); //$NON-NLS-1$

        private final SafeHtml INPUT_CHECKED_NULL_DISABLED =
                SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked style=\"-moz-Field ! important; background-color: yellow;\" disabled/>"); //$NON-NLS-1$

        private final SafeHtml INPUT_CHECKED_NULL =
                SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked style=\"-moz-Field ! important; background-color: yellow;\"/>"); //$NON-NLS-1$

        private ExCheckboxCell(boolean dependsOnSelection, boolean handlesSelection) {
            super(dependsOnSelection, handlesSelection);
        }

        @Override
        public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
            Object key = context.getKey();
            // TODO semi-checked checkbox (null value)
            // value = ((SimpleSelectionTreeNodeModel) context.getKey()).getIsSelectedNullable();
            @SuppressWarnings("unchecked")
            M model = (M) key;
            if (!model.isEditable()) {
                // disabled
                Boolean viewData = getViewData(key);
                if (viewData != null && viewData.equals(value)) {
                    clearViewData(key);
                    viewData = null;
                }
                // if (value == null) {
                // sb.append(INPUT_CHECKED_NULL_DISABLED);
                // } else
                if (value != null && ((viewData != null) ? viewData : value)) {
                    sb.append(INPUT_CHECKED_DISABLED);
                } else {
                    sb.append(INPUT_UNCHECKED_DISABLED);
                }
            } else {
                // if (value == null) {
                // Boolean viewData = getViewData(key);
                // if (viewData != null && viewData.equals(value)) {
                // clearViewData(key);
                // viewData = null;
                // }
                // sb.append(INPUT_CHECKED_NULL);
                // } else
                // enabled
                super.render(context, value, sb);
            }
        }
    }

    private final AsyncDataProvider<M> asyncTreeDataProvider;

    private final List<HasCell<M, ?>> cells = new ArrayList<HasCell<M, ?>>();

    private List<M> root;

    private final MultiSelectionModel<M> selectionModel = new MultiSelectionModel<M>();

    private NodeSelectionHandler nodeSelectionHandler;

    public ModelListTreeViewModel() {
        cells.add(new CheckboxColumn());
        cells.add(new CellLabel());

        asyncTreeDataProvider = new AsyncDataProvider<M>() {

            @Override
            protected void onRangeChanged(HasData<M> display) {
                // no-op
            }

            @Override
            protected void updateRowData(final HasData<M> display, int start, List<M> values) {
                super.updateRowData(display, start, values);

                for (M model : values) {
                    UpdateSelection(model, display);
                }
            }
        };

        // Drive selection
        selectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<M> selectedSet = selectionModel.getSelectedSet();
                HashSet<TreeNodeModel> removedSet = new HashSet<TreeNodeModel>();
                for (TreeNodeModel<T, M> child : root) {
                    if (!selectedSet.contains(child) && child.getSelected()) {
                        selectedSet.remove(child);
                        removedSet.add(child);
                    }
                    for (TreeNodeModel<T, M> child1 : child.getChildren()) {
                        if (!selectedSet.contains(child1) && child1.getSelected()) {
                            selectedSet.remove(child1);
                            removedSet.add(child1);
                        }
                        for (TreeNodeModel<T, M> child2 : child1.getChildren()) {
                            if (!selectedSet.contains(child2) && child2.getSelected()) {
                                selectedSet.remove(child2);
                                removedSet.add(child2);
                            }
                        }
                    }
                }
                for (M m : selectedSet) {
                    m.setSelected(true);
                }
                for (TreeNodeModel treeNodeModel : removedSet) {
                    treeNodeModel.setSelected(false);
                }
            }
        });
    }

    public void UpdateSelection(M model, final HasData<M> display) {
        // Add Selection Listener
        if (nodeSelectionHandler == null) {
            nodeSelectionHandler = new NodeSelectionHandler(display);
        }
        model.addSelectionHandler(nodeSelectionHandler);

        // show value
        display.getSelectionModel().setSelected(model, model.getSelected());

        for (int i = 0; i < model.getChildren().size(); i++) {
            UpdateSelection(model.getChildren().get(i), display);
        }
    }

    public AsyncDataProvider<M> getAsyncTreeDataProvider() {
        return asyncTreeDataProvider;
    }

    @Override
    public <N> NodeInfo<?> getNodeInfo(N value) {
        @SuppressWarnings("unchecked")
        M model = (M) value;
        CompositeCell<M> composite = new CompositeCell<M>(cells);
        if (value == null) {
            // root node
            return new DefaultNodeInfo<M>(asyncTreeDataProvider,
                    composite,
                    selectionModel,
                    DefaultSelectionEventManager.<M> createCheckboxManager(),
                    null);
        } else {
            // child nodes
            return new DefaultNodeInfo<M>(new ListDataProvider<M>(model.getChildren()),
                    composite,
                    selectionModel,
                    DefaultSelectionEventManager.<M> createCheckboxManager(),
                    null);
        }
    }

    @Override
    public boolean isLeaf(Object value) {
        @SuppressWarnings("unchecked")
        M model = (M) value;
        return model != null && model.getChildren() != null && model.getChildren().size() == 0;
    }

    /**
     * Set the Root list- required to sync the Model Tree correctly
     *
     * @param arrayList
     */
    public void setRoot(List<M> arrayList) {
        this.root = arrayList;
    }
}
