package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.ui.common.uicommon.model.TreeNodeModel;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.tree.TreeModelWithElementId;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.DOM;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

/**
 * A TreeView Model for {@link TreeNodeModel} Nodes
 */
public class ModelListTreeViewModel<T, M extends TreeNodeModel<T, M>> implements TreeModelWithElementId {

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

    interface ExCheckboxCellTemplate extends SafeHtmlTemplates {

        /**
         * An html string representation of a checked input box.
         */
        @Template("<input type=\"checkbox\" id=\"{0}\" tabindex=\"-1\" checked/>")
        SafeHtml inputChecked(String elementId);

        /**
         * An html string representation of an unchecked input box.
         */
        @Template("<input type=\"checkbox\" id=\"{0}\" tabindex=\"-1\"/>")
        SafeHtml inputUnchecked(String elementId);

        /**
         * An html string representation of a disabled checked input box.
         */
        @Template("<input type=\"checkbox\" id=\"{0}\" tabindex=\"-1\" checked disabled/>")
        SafeHtml inputCheckedDisabled(String elementId);

        /**
         * An html string representation of a disabled unchecked input box.
         */
        @Template("<input type=\"checkbox\" id=\"{0}\" tabindex=\"-1\" disabled/>")
        SafeHtml inputUncheckedDisabled(String elementId);

    }

    /**
     * A {@link CheckboxCell} that can be disabled
     */
    private final class ExCheckboxCell extends CheckboxCell {

        private final ExCheckboxCellTemplate template = GWT.create(ExCheckboxCellTemplate.class);

        private ExCheckboxCell(boolean dependsOnSelection, boolean handlesSelection) {
            super(dependsOnSelection, handlesSelection);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
            // TODO semi-checked checkbox (null value)
            // value = ((SimpleSelectionTreeNodeModel) context.getKey()).getIsSelectedNullable();
            Object key = context.getKey();
            Boolean viewData = getViewData(key);

            M nodeModel = (M) key;
            String elementId = ElementIdUtils.createTreeCellElementId(
                    elementIdPrefix, nodeModel, roots);

            if (viewData != null && viewData.equals(value)) {
                clearViewData(key);
                viewData = null;
            }

            if (value != null && ((viewData != null) ? viewData : value)) {
                // Checked state
                sb.append(nodeModel.isEditable() ? template.inputChecked(elementId)
                        : template.inputCheckedDisabled(elementId));
            } else {
                // Unchecked state
                sb.append(nodeModel.isEditable() ? template.inputUnchecked(elementId)
                        : template.inputUncheckedDisabled(elementId));
            }
        }

    }

    private final AsyncDataProvider<M> asyncTreeDataProvider;

    private List<M> roots;

    private final CompositeCell<M> compositeCell;

    private final MultiSelectionModel<M> selectionModel = new MultiSelectionModel<>();

    private NodeSelectionHandler nodeSelectionHandler;

    private final HandlerRegistration selectionModelChangeHandlerReg;
    private final List<HandlerRegistration> nodeModelSelectionHandlerRegList = new ArrayList<>();

    private String elementIdPrefix = DOM.createUniqueId();

    public ModelListTreeViewModel() {
        List<HasCell<M, ?>> cells = new ArrayList<>();
        cells.add(new CheckboxColumn());
        cells.add(new CellLabel());
        this.compositeCell = new CompositeCell<>(cells);

        asyncTreeDataProvider = new AsyncDataProvider<M>() {
            @Override
            protected void onRangeChanged(HasData<M> display) {
                // no-op
            }

            @Override
            protected void updateRowData(final HasData<M> display, int start, List<M> values) {
                super.updateRowData(display, start, values);

                for (M model : values) {
                    updateSelection(model, display);
                }
            }
        };

        // Drive selection
        selectionModelChangeHandlerReg = selectionModel.addSelectionChangeHandler(event -> {
            Set<M> selectedSet = selectionModel.getSelectedSet();
            Set<TreeNodeModel<?, ?>> removedSet = new HashSet<>();
            updateSelectionSets(selectedSet, removedSet, roots);
            for (M toSelect : selectedSet) {
                toSelect.setSelected(true);
            }
            for (TreeNodeModel<?, ?> toDeselect : removedSet) {
                toDeselect.setSelected(false);
            }
        });
    }

    /**
     * Recursively determine the set of changed nodes.
     * @param selectedSet The set of selected nodes.
     * @param removedSet The set of removed nodes.
     * @param nodes The list of nodes to check against.
     */
    private void updateSelectionSets(Set<M> selectedSet, Set<TreeNodeModel<?, ?>> removedSet, List<M> nodes) {
        for (TreeNodeModel<T, M> node : nodes) {
            if (!selectedSet.contains(node) && node.getSelected()) {
                removedSet.add(node);
            }
            updateSelectionSets(selectedSet, removedSet, node.getChildren());
        }
    }

    public void updateSelection(M model, final HasData<M> display) {
        // Add Selection Listener
        if (nodeSelectionHandler == null) {
            nodeSelectionHandler = new NodeSelectionHandler(display);
        }
        nodeModelSelectionHandlerRegList.add(model.addSelectionHandler(nodeSelectionHandler));

        // show value
        display.getSelectionModel().setSelected(model, model.getSelected());

        for (int i = 0; i < model.getChildren().size(); i++) {
            updateSelection(model.getChildren().get(i), display);
        }
    }

    public void removeHandlers() {
        selectionModelChangeHandlerReg.removeHandler();
        for (HandlerRegistration reg : nodeModelSelectionHandlerRegList) {
            reg.removeHandler();
        }
    }

    public AsyncDataProvider<M> getAsyncTreeDataProvider() {
        return asyncTreeDataProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <N> NodeInfo<?> getNodeInfo(N value) {
        M model = (M) value;

        if (value == null) {
            // root node
            return new DefaultNodeInfo<>(asyncTreeDataProvider,
                    compositeCell, selectionModel,
                    DefaultSelectionEventManager.createCheckboxManager(),
                    null);
        } else {
            // child nodes
            return new DefaultNodeInfo<>(new ListDataProvider<>(model.getChildren()),
                    compositeCell, selectionModel,
                    DefaultSelectionEventManager.createCheckboxManager(),
                    null);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isLeaf(Object value) {
        M model = (M) value;
        return model != null && model.getChildren() != null && model.getChildren().size() == 0;
    }

    /**
     * Set the Root list- required to sync the Model Tree correctly
     */
    public void setRoots(List<M> arrayList) {
        this.roots = arrayList;
    }

    @Override
    public void setElementIdPrefix(String elementIdPrefix) {
        this.elementIdPrefix = elementIdPrefix;
    }

}
