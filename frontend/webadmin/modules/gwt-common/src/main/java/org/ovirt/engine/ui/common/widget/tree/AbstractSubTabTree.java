package org.ovirt.engine.ui.common.widget.tree;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractSubTabTree<M extends SearchableListModel, R, N> extends Composite {

    protected final Tree tree;
    protected ArrayList<TreeItem> oldTreeItems;
    protected ArrayList<Object> selectedItems;
    protected ArrayList<Object> newSelectedItems;

    protected M listModel;

    protected boolean isSelectionEnabled;
    protected boolean isMultiSelection;
    protected boolean isControlKeyDown;

    protected final CommonApplicationResources resources;
    protected final CommonApplicationConstants constants;

    public AbstractSubTabTree(CommonApplicationResources resources, CommonApplicationConstants constants) {
        this.resources = resources;
        this.constants = constants;

        tree = new Tree();
        initWidget(tree);

        selectedItems = new ArrayList<Object>();
        newSelectedItems = new ArrayList<Object>();
        isMultiSelection = true;

        tree.addOpenHandler(treeOpenHandler);
        addRootSelectionHandler();
    }

    public boolean isMultiSelection() {
        return isMultiSelection;
    }

    public void setMultiSelection(boolean isMultiSelection) {
        this.isMultiSelection = isMultiSelection;
    }

    public boolean isSelectionEnabled() {
        return isSelectionEnabled;
    }

    public void setSelectionEnabled(boolean isSelectionEnabled) {
        this.isSelectionEnabled = isSelectionEnabled;
        selectedItems.clear();
    }

    private IEventListener itemsChangedEventListener = new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            refreshTree();
        }
    };

    private OpenHandler<TreeItem> treeOpenHandler = new OpenHandler<TreeItem>() {
        @Override
        public void onOpen(OpenEvent<TreeItem> event) {
            TreeItem item = event.getTarget();
            onTreeItemOpen(item);
        }
    };

    public void clearTree() {
        tree.clear();
    }

    private void saveTreeState() {
        oldTreeItems = new ArrayList<TreeItem>();

        for (int i = 0; i < tree.getItemCount(); i++) {
            TreeItem root = tree.getItem(i);
            oldTreeItems.add(root);

            for (int n = 0; n < root.getChildCount(); n++) {
                TreeItem node = root.getChild(n);
                oldTreeItems.add(node);
            }
        }
    }

    private void updateTreeState() {
        newSelectedItems.clear();

        for (int i = 0; i < tree.getItemCount(); i++) {
            TreeItem root = tree.getItem(i);
            root.setState(getItemOldState(root));
            updateItemSelection(root);

            for (int n = 0; n < root.getChildCount(); n++) {
                TreeItem node = root.getChild(n);
                node.setState(getItemOldState(node));
                updateItemSelection(node);
            }
        }

        selectedItems.clear();
        selectedItems.addAll(newSelectedItems);
        onItemsSelection();
    }

    private boolean getItemOldState(TreeItem treeItem) {
        for (TreeItem oldTreeItem : oldTreeItems) {
            Object oldEntity = (Object) oldTreeItem.getUserObject();
            Object entity = (Object) treeItem.getUserObject();

            if (oldEntity != null && entity != null && oldEntity.equals(entity)) {
                return oldTreeItem.getState();
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
    }

    public void refreshTree() {
        List<R> rootItems = (List<R>) listModel.getItems();

        saveTreeState();
        tree.clear();

        if (rootItems == null)
            return;

        for (R root : rootItems) {
            TreeItem rootItem = getRootItem(root);

            TreeItem nodeHeader = getNodeHeader();
            if (nodeHeader != null) {
                rootItem.addItem(nodeHeader);
            }

            for (N node : getNodeObjects(root)) {
                TreeItem nodeItem = getNodeItem(node);

                TreeItem leafItem = getLeafItem(node);
                if (leafItem != null) {
                    nodeItem.addItem(leafItem);
                    styleItem(leafItem, getIsNodeEnabled(node));
                }

                rootItem.addItem(nodeItem);
                styleItem(nodeItem, getIsNodeEnabled(node));
            }

            tree.addItem(rootItem);
            styleItem(rootItem, true);
        }

        updateTreeState();
    }

    protected abstract TreeItem getRootItem(R rootObject);

    protected abstract TreeItem getNodeItem(N nodeObject);

    protected TreeItem getLeafItem(N nodeObject) {
        return null;
    }

    protected TreeItem getNodeHeader() {
        return null;
    }

    protected abstract ArrayList<N> getNodeObjects(R root);

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
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TreeHeaderlessTable.css" })
        TableStyle cellTableStyle();
    }

    protected TreeItem createTreeItem(EntityModelCellTable<ListModel> table, ArrayList<EntityModel> list) {
        table.setRowData(list);
        table.setWidth("100%");
        TreeItem item = new TreeItem(table);
        return item;
    }

    protected void styleItem(TreeItem item, boolean enabled) {
        Element tableElm = DOM.getFirstChild(item.getElement());
        tableElm.setAttribute("width", "100%");

        Element col = tableElm.getElementsByTagName("td").getItem(0);
        col.setAttribute("width", "20px");

        if (!enabled) {
            NodeList<Element> inputs = item.getElement().getElementsByTagName("input");
            for (int i = 0; i < inputs.getLength(); i++) {
                disableElement(inputs.getItem(i));
            }
            NodeList<Element> spans = item.getElement().getElementsByTagName("span");
            for (int i = 0; i < spans.getLength(); i++) {
                disableElement(spans.getItem(i));
            }
        }

        boolean isLeafEmpty = item.getUserObject() != null && item.getUserObject().equals(true);
        if (isLeafEmpty) {
            item.getElement().getElementsByTagName("td").getItem(0).getStyle().setVisibility(Visibility.HIDDEN);
        }
    }

    protected void addTextBoxToPanel(HorizontalPanel panel, TextBoxLabel item, String text, String width) {
        item.setText(text);
        addItemToPanel(panel, item, width);
    }

    protected <T> void addValueLabelToPanel(HorizontalPanel panel, ValueLabel<T> item, T value, String width) {
        item.setValue(value);
        addItemToPanel(panel, item, width);
    }

    protected void addItemToPanel(HorizontalPanel panel, Widget item, String width) {
        item.getElement().getStyle().setBackgroundColor("transparent");
        item.getElement().getStyle().setColor("black");

        panel.add(item);
        panel.setCellWidth(item, width);
    }

    protected void disableElement(Element element) {
        element.getStyle().setProperty("disabled", "true");
        element.getStyle().setColor("#999999");

        if (getNodeDisabledTooltip() != null) {
            element.setTitle(getNodeDisabledTooltip());
        }
    }

    public void addRootSelectionHandler() {
        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                onItemSelection(event.getSelectedItem(), false);
            }
        });

        tree.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                if (event.getNativeEvent().getButton() == NativeEvent.BUTTON_RIGHT) {
                    onItemSelection(findSelectedItem(event.getClientX(), event.getClientY()), true);
                }
            }
        });

        tree.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                isControlKeyDown = event.isControlKeyDown();
            }
        });

        tree.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                isControlKeyDown = event.isControlKeyDown();
            }
        });
    }

    private void onItemSelection(TreeItem item, boolean enforceSelection) {
        Object entity = (Object) item.getUserObject();

        if (!isSelectionEnabled) {
            return;
        }

        // Root node
        if (item.getParentItem() == null) {

            if (!isControlKeyDown || !isMultiSelection) {
                selectedItems.clear();
            }

            saveTreeState();
            updateTreeState();

            if (!selectedItems.contains(entity)) {
                selectedItems.add(entity);
                onItemsSelection();
            }
            else if (!enforceSelection) {
                selectedItems.remove(entity);
                onItemsSelection();
            }

            updateItemSelection(item);
        }
    }

    private void updateItemSelection(TreeItem item) {
        Object entity = (Object) item.getUserObject();
        if (entity == null) {
            return;
        }

        boolean selected = false;
        for (Object selectedEntity : selectedItems) {
            if (entity.equals(selectedEntity)) {
                selected = true;
                newSelectedItems.add(selectedEntity);
            }
        }

        Element tableElement = item.getElement().getElementsByTagName("table").getItem(0);
        tableElement.getStyle().setBackgroundColor(selected ? "#C3D0E0" : "transparent");
        tableElement.getStyle().setProperty("borderBottom", "1px solid white");
    }

    protected Object getEntityId(Object entity) {
        return ((BusinessEntity) entity).getId();
    }

    protected void onItemsSelection() {
        if (listModel.getItems() == null) {
            return;
        }

        ArrayList<Object> selectedEntities = new ArrayList<Object>();
        for (Object entity : (ArrayList<Object>) listModel.getItems()) {
            if (selectedItems.contains(getEntityId(entity))) {
                selectedEntities.add(entity);
            }
        }

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
