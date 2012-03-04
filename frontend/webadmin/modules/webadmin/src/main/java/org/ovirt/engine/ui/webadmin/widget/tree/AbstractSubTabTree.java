package org.ovirt.engine.ui.webadmin.widget.tree;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
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
    protected ArrayList<BusinessEntity> selectedItems;
    protected ArrayList<BusinessEntity> newSelectedItems;

    protected final ApplicationResources resources;
    protected final ApplicationConstants constants;

    protected M listModel;

    protected boolean isControlKeyDown;

    public AbstractSubTabTree() {
        tree = new Tree();
        initWidget(tree);

        resources = ClientGinjectorProvider.instance().getApplicationResources();
        constants = ClientGinjectorProvider.instance().getApplicationConstants();

        selectedItems = new ArrayList<BusinessEntity>();
        newSelectedItems = new ArrayList<BusinessEntity>();
    }

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
            BusinessEntity oldEntity = (BusinessEntity) oldTreeItem.getUserObject();
            BusinessEntity entity = (BusinessEntity) treeItem.getUserObject();

            if (oldEntity != null && entity != null && oldEntity.getId().equals(entity.getId())) {
                return oldTreeItem.getState();
            }
        }
        return false;
    }

    public void updateTree(final M listModel) {
        this.listModel = listModel;

        selectedItems.clear();
        onItemsSelection();

        listModel.getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                M model = (M) sender;
                List<R> rootItems = (List<R>) model.getItems();

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
        });
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

    public class EmptyColumn extends TextColumn<EntityModel> {
        @Override
        public String getValue(EntityModel object) {
            return null;
        }
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

    protected <T> ArrayList<EntityModel> toEntityModelList(ArrayList<T> entities) {
        ArrayList<EntityModel> entityModelList = new ArrayList<EntityModel>();

        for (T entity : entities) {
            EntityModel entityModel = new EntityModel();
            entityModel.setEntity(entity);
            entityModelList.add(entityModel);
        }

        return entityModelList;
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

    public void enableRootSelection() {
        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                TreeItem item = event.getSelectedItem();
                BusinessEntity entity = (BusinessEntity) item.getUserObject();

                if (item.getParentItem() == null) {

                    if (!isControlKeyDown) {
                        selectedItems.clear();
                    }

                    saveTreeState();
                    updateTreeState();

                    if (!selectedItems.contains(entity)) {
                        selectedItems.add(entity);
                        onItemsSelection();
                    }
                    else {
                        selectedItems.remove(entity);
                        onItemsSelection();
                    }

                    updateItemSelection(item);
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

    private void updateItemSelection(TreeItem item) {
        BusinessEntity entity = (BusinessEntity) item.getUserObject();
        if (entity == null) {
            return;
        }

        boolean selected = false;
        for (BusinessEntity selectedEntity : selectedItems) {
            if (entity.getId().equals(selectedEntity.getId())) {
                selected = true;
                newSelectedItems.add(selectedEntity);
            }
        }

        Element tableElement = item.getElement().getElementsByTagName("table").getItem(0);
        tableElement.getStyle().setBackgroundColor(selected ? "#C3D0E0" : "transparent");
        tableElement.getStyle().setProperty("borderBottom", "1px solid white");
    }

    protected void onItemsSelection() {
        listModel.setSelectedItems((ArrayList) selectedItems.clone());
    };
}
