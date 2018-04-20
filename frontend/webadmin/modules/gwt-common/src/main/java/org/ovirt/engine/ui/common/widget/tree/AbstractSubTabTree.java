package org.ovirt.engine.ui.common.widget.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractSubTabTree<M extends SearchableListModel, R, N> extends Composite {

    protected final Tree tree;
    protected Map<Object, Boolean> oldItemStatesMap;
    protected Map<Object, TreeItem> oldRootItemsMap;
    protected ArrayList<Object> selectedItems;
    protected ArrayList<Object> newSelectedItems;

    protected M listModel;

    protected boolean isRootSelectionEnabled;
    protected boolean isNodeSelectionEnabled;
    protected boolean isMultiSelection;
    protected boolean isControlKeyDown;

    protected static final String NODE_HEADER = "nodeHeader"; //$NON-NLS-1$

    public AbstractSubTabTree() {

        tree = new Tree();
        initWidget(tree);

        selectedItems = new ArrayList<>();
        newSelectedItems = new ArrayList<>();
        oldRootItemsMap = new HashMap<>();
        oldItemStatesMap = new HashMap<>();
        isMultiSelection = true;

        tree.addOpenHandler(treeOpenHandler);
        addSelectionHandler();
    }

    public boolean isMultiSelection() {
        return isMultiSelection;
    }

    public void setMultiSelection(boolean isMultiSelection) {
        this.isMultiSelection = isMultiSelection;
    }

    public void setRootSelectionEnabled(boolean isRootSelectionEnabled) {
        this.isRootSelectionEnabled = isRootSelectionEnabled;
        selectedItems.clear();
    }

    public void setNodeSelectionEnabled(boolean isNodeSelectionEnabled) {
        this.isNodeSelectionEnabled = isNodeSelectionEnabled;
        selectedItems.clear();
    }

    private IEventListener<EventArgs> itemsChangedEventListener = (ev, sender, args) -> refreshTree();

    private OpenHandler<TreeItem> treeOpenHandler = event -> {
        TreeItem item = event.getTarget();
        onTreeItemOpen(item);
    };

    public void clearTree() {
        tree.clear();
    }

    private void saveTreeState() {
        oldItemStatesMap.clear();

        for (int i = 0; i < tree.getItemCount(); i++) {
            TreeItem rootItem = tree.getItem(i);
            oldRootItemsMap.put(rootItem.getUserObject(), rootItem);
            saveTreeItemState(rootItem);
        }
    }

    private void saveTreeItemState(TreeItem item) {
        oldItemStatesMap.put(item.getUserObject(), item.getState());

        for (int n = 0; n < item.getChildCount(); n++) {
            saveTreeItemState(item.getChild(n));
        }
    }

    private void updateTreeState() {
        newSelectedItems.clear();

        for (int i = 0; i < tree.getItemCount(); i++) {
            updateItemSelection(tree.getItem(i));
        }

        selectedItems.clear();
        selectedItems.addAll(newSelectedItems);
        onItemsSelection();
    }

    private boolean getItemOldState(Object userObject) {
        for (Map.Entry<Object, Boolean> oldItemStatesEntry : oldItemStatesMap.entrySet()) {
            if (oldItemStatesEntry.getKey() != null && userObject != null && oldItemStatesEntry.getKey().equals(userObject)) {
                return oldItemStatesEntry.getValue();
            }
        }
        return false;
    }

    public void updateTree(M listModel) {
        this.listModel = listModel;

        selectedItems.clear();
        onItemsSelection();

        listModel.getItemsChangedEvent().removeListener(itemsChangedEventListener);
        listModel.getItemsChangedEvent().addListener(itemsChangedEventListener);

        if (listModel.getItems() != null) {
            refreshTree();
        }
    }

    public void refreshTree() {
        List<R> rootItems = (List<R>) listModel.getItems();

        saveTreeState();
        tree.clear();

        if (rootItems == null) {
                return;
        }

        for (R root : rootItems) {
            TreeItem rootItem = getRootItem(root);

            TreeItem nodeHeader = getNodeHeader();
            if (nodeHeader != null) {
                rootItem.addItem(nodeHeader);
            }

            if (getNodeObjects(root).isEmpty()) {
                boolean isOpen = getItemOldState(rootItem.getUserObject());
                if (isOpen) {
                    rootItem = oldRootItemsMap.get(rootItem.getUserObject());
                } else {
                    if (getEmptyRoot() != null) {
                        rootItem.addItem(getEmptyRoot());
                    }
                }
            } else {
                for (N node : getNodeObjects(root)) {
                    TreeItem nodeItem = getNodeItem(node);

                    addLeaves(nodeItem, node);

                    rootItem.addItem(nodeItem);
                    styleItem(nodeItem, getIsNodeEnabled(node));
                }
            }

            tree.addItem(rootItem);
            styleItem(rootItem, true);
        }

        updateTreeState();
    }

    protected void addLeaves(TreeItem nodeItem, N node) {
        TreeItem leafItem = getLeafItem(node);
        if (leafItem != null) {
            nodeItem.addItem(leafItem);
            styleItem(leafItem, getIsNodeEnabled(node));
        }
    }

    protected abstract TreeItem getRootItem(R rootObject);

    protected abstract TreeItem getNodeItem(N nodeObject);

    protected TreeItem getLeafItem(N nodeObject) {
        return null;
    }

    protected TreeItem getNodeHeader() {
        return null;
    }

    protected TreeItem getEmptyRoot() {
        return null;
    }

    protected abstract List<N> getNodeObjects(R root);

    protected boolean getIsNodeEnabled(N nodeObject) {
        return true;
    }

    protected String getNodeDisabledTooltip() {
        return null;
    }

    protected void onTreeItemOpen(TreeItem item) {

    }

    public interface TreeHeaderlessTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/TreeHeaderlessTable.css" })
        TableStyle cellTableStyle();
    }

    protected TreeItem createTreeItem(EntityModelCellTable<ListModel> table, ArrayList<EntityModel> list) {
        table.setRowData(list);
        table.setWidth("100%"); //$NON-NLS-1$
        TreeItem item = new TreeItem(table);
        return item;
    }

    protected void styleItem(TreeItem item, boolean enabled) {
        Element tableElm = DOM.getFirstChild(item.getElement());
        tableElm.setAttribute("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        Element col = tableElm.getElementsByTagName("td").getItem(0); //$NON-NLS-1$
        col.setAttribute("width", "20px"); //$NON-NLS-1$ //$NON-NLS-2$

        NodeList<Element> inputs = item.getElement().getElementsByTagName("input"); //$NON-NLS-1$
        for (int i = 0; i < inputs.getLength(); i++) {
            if (!enabled) {
                disableElement(inputs.getItem(i));
            } else {
                ElementTooltipUtils.destroyTooltip(inputs.getItem(i));
            }
        }

        NodeList<Element> spans = item.getElement().getElementsByTagName("span"); //$NON-NLS-1$
        for (int i = 0; i < spans.getLength(); i++) {
            if (!enabled) {
                disableElement(spans.getItem(i));
            } else {
                ElementTooltipUtils.destroyTooltip(spans.getItem(i));
            }
        }

        boolean isLeafEmpty = item.getUserObject() != null && item.getUserObject().equals(true);
        if (isLeafEmpty) {
            item.getElement().getElementsByTagName("td").getItem(0).getStyle().setVisibility(Visibility.HIDDEN); //$NON-NLS-1$
        }
    }

    protected void addTextBoxToPanel(HorizontalPanel panel, StringValueLabel item, String text, String width) {
        item.setValue(text);
        addItemToPanel(panel, item, width);
    }

    protected void addTextBoxToPanel(HorizontalPanel panel, WidgetTooltip item, String text, String width) {
        Widget w = item.getWidget();
        if (w instanceof Label) {
            Label label = (Label) item.getWidget();
            label.setText(text);
            addItemToPanel(panel, item, width);
        } else if (w instanceof StringValueLabel) {
            StringValueLabel label = (StringValueLabel) item.getWidget();
            label.setValue(text);
            addItemToPanel(panel, item, width);
        } else {
            throw new ClassCastException("tooltipped label contains unknown Widget: " + w.getClass()); //$NON-NLS-1$
        }
    }

    protected <T> void addValueLabelToPanel(HorizontalPanel panel, ValueLabel<T> item, T value, String width) {
        item.setValue(value);
        addItemToPanel(panel, item, width);
    }

    protected void addItemToPanel(HorizontalPanel panel, IsWidget item, String width) {
        addItemToPanel(panel, item.asWidget(), width);
    }

    protected void addItemToPanel(HorizontalPanel panel, Widget item, String width) {
        item.getElement().getStyle().setBackgroundColor("transparent"); //$NON-NLS-1$
        item.getElement().getStyle().setColor("black"); //$NON-NLS-1$

        panel.add(item);
        panel.setCellWidth(item, width);
    }

    protected void disableElement(Element element) {
        element.getStyle().setProperty("disabled", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        element.getStyle().setColor("#999999"); //$NON-NLS-1$

        if (getNodeDisabledTooltip() != null) {
            ElementTooltipUtils.setTooltipOnElement(element, SafeHtmlUtils.fromString(getNodeDisabledTooltip()));
        }
    }

    public void addSelectionHandler() {
        tree.addSelectionHandler(event -> onItemSelection(event.getSelectedItem(), false));

        tree.addMouseDownHandler(event -> {
            if (event.getNativeEvent().getButton() == NativeEvent.BUTTON_RIGHT) {
                onItemSelection(findSelectedItem(event.getClientX(), event.getClientY()), true);
            }
        });

        tree.addKeyDownHandler(event -> isControlKeyDown = event.isControlKeyDown());

        tree.addKeyUpHandler(event -> isControlKeyDown = event.isControlKeyDown());
    }

    private void onItemSelection(TreeItem item, boolean enforceSelection) {
        Object entity = item.getUserObject();
        boolean isRootItem = item.getParentItem() == null;
        boolean isNodeItem = item.getParentItem() != null;

        if ((isRootItem && !isRootSelectionEnabled) || (isNodeItem && !isNodeSelectionEnabled)) {
            return;
        }

        if (!isControlKeyDown || !isMultiSelection) {
            selectedItems.clear();
        }

        saveTreeState();
        updateTreeState();

        if (!selectedItems.contains(entity)) {
            selectedItems.add(entity);
            onItemsSelection();
        } else if (!enforceSelection) {
            selectedItems.remove(entity);
            onItemsSelection();
        }

        updateItemSelection(item);
    }

    private void updateItemSelection(TreeItem item) {
        Object entity = item.getUserObject();
        boolean isRootItem = item.getParentItem() == null;

        if (entity == null) {
            return;
        }

        // Update selected Items
        boolean selected = false;
        for (Object selectedEntity : selectedItems) {
            if (entity.equals(selectedEntity) && !newSelectedItems.contains(selectedEntity)) {
                selected = true;
                newSelectedItems.add(selectedEntity);
            }
        }

        // Update element's style
        Element element = isRootItem ?
                item.getElement().getElementsByTagName("table").getItem(0) : item.getElement(); //$NON-NLS-1$
        if (!NODE_HEADER.equals(item.getUserObject())) {
            element.getStyle().setBackgroundColor(selected ? "#C3D0E0" : "transparent"); //$NON-NLS-1$ //$NON-NLS-2$
            element.getStyle().setProperty("borderBottom", "1px solid white"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Set item's state
        item.setState(getItemOldState(item.getUserObject()));

        // Recursively update children
        for (int n = 0; n < item.getChildCount(); n++) {
            TreeItem node = item.getChild(n);
            updateItemSelection(node);
        }
    }

    protected Object getEntityId(Object entity) {
        return ((BusinessEntity) entity).getId();
    }

    protected ArrayList<Object> getSelectedEntities() {
        ArrayList<Object> selectedEntities = new ArrayList<>();
        for (Object entity : (ArrayList<Object>) listModel.getItems()) {
            if (selectedItems.contains(getEntityId(entity))) {
                selectedEntities.add(entity);
            }
        }
        return selectedEntities;
    }

    protected void onItemsSelection() {
        if (listModel.getItems() == null || (!isRootSelectionEnabled && !isNodeSelectionEnabled)) {
            return;
        }

        ArrayList<Object> selectedEntities = getSelectedEntities();

        listModel.setSelectedItem(selectedEntities.isEmpty() ? null : selectedEntities.get(0));

        if (isMultiSelection) {
            listModel.setSelectedItems(selectedEntities);
        }
    };

    TreeItem findSelectedItem(int clientX, int clientY) {
        return findSelectedTreeItemRecursive(null, clientX, clientY);
    }

    TreeItem findSelectedTreeItemRecursive(TreeItem item, int x, int y) {
        if (null == item) {
            int count = tree.getItemCount();
            for (int i = 0; i < count; i++) {
                TreeItem selected = findSelectedTreeItemRecursive(tree.getItem(i), x, y);
                if (selected != null) {
                    return selected;
                }
            }
            return null;
        }

        int count = item.getChildCount();
        for (int i = 0; i < count; i++) {
            TreeItem selected = findSelectedTreeItemRecursive(item.getChild(i), x, y);
            if (selected != null) {
                return selected;
            }
        }

        if (x >= item.getAbsoluteLeft()
                && x <= item.getAbsoluteLeft() + item.getOffsetWidth()
                && y >= item.getAbsoluteTop()
                && y <= item.getAbsoluteTop() + item.getOffsetHeight()) {
            return item;
        }
        return null;
    }
}
