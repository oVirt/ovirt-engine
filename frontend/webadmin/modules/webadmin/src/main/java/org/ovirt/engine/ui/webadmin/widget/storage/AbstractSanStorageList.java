package org.ovirt.engine.ui.webadmin.widget.storage;

import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractSanStorageList<M extends EntityModel, L extends ListModel> extends Composite {

    @SuppressWarnings("rawtypes")
    interface WidgetUiBinder extends UiBinder<Widget, AbstractSanStorageList> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    SimplePanel treeHeader;

    @UiField
    ScrollPanel treeContainer;

    SanStorageModelBase model;

    Tree tree;

    boolean hideLeaf;

    public AbstractSanStorageList(SanStorageModelBase model) {
        this(model, false);
    }

    public AbstractSanStorageList(SanStorageModelBase model, boolean hideLeaf) {
        this.model = model;
        this.hideLeaf = hideLeaf;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        createHeaderWidget();
        createSanStorageListWidget();
    }

    public void activateItemsUpdate() {
        disableItemsUpdate();

        model.getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateItems();
            }
        });
        updateItems();
    }

    public void disableItemsUpdate() {
        model.getItemsChangedEvent().getListeners().clear();
    }

    protected void updateItems() {
        List<M> items = (List<M>) model.getItems();
        tree.clear();

        if (items != null) {
            for (M rootModel : items) {
                addRootNode(createRootNode(rootModel), createLeafNode(getLeafModel(rootModel)));
            }
        }
    }

    protected void addRootNode(TreeItem rootItem, TreeItem leafItem) {
        rootItem.getElement().getStyle().setBackgroundColor("#eff3ff");
        rootItem.getElement().getStyle().setMarginBottom(1, Unit.PX);
        rootItem.getElement().getStyle().setPadding(0, Unit.PX);

        if (leafItem != null) {
            rootItem.addItem(leafItem);

            leafItem.getElement().getStyle().setBackgroundColor("#ffffff");
            leafItem.getElement().getStyle().setMarginLeft(0, Unit.PX);
            leafItem.getElement().getStyle().setPadding(0, Unit.PX);

            Boolean isLeafEmpty = (Boolean) leafItem.getUserObject();
            if (isLeafEmpty != null && isLeafEmpty.equals(Boolean.TRUE)) {
                rootItem.getElement().getElementsByTagName("td").getItem(0).getStyle().setVisibility(Visibility.HIDDEN);
            }
        }

        tree.addItem(rootItem);
    }

    protected void createSanStorageListWidget() {
        tree = new Tree();
        treeContainer.add(tree);
    }

    public void setTreeContainerStyleName(String styleName) {
        treeContainer.setStyleName(styleName);
    }

    public void setTreeContainerHeight(double height) {
        treeContainer.getElement().getStyle().setHeight(height, Unit.PX);
    }

    protected abstract void createHeaderWidget();

    protected abstract L getLeafModel(M rootModel);

    protected abstract TreeItem createRootNode(M rootModel);

    protected abstract TreeItem createLeafNode(L leafModel);

    public interface SanStorageListHeaderResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/SanStorageListHeader.css" })
        TableStyle cellTableStyle();
    }

    public interface SanStorageListTargetRootResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/SanStorageListTargetRoot.css" })
        TableStyle cellTableStyle();
    }

    public interface SanStorageListLunTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/SanStorageListLunTable.css" })
        TableStyle cellTableStyle();
    }

    public interface SanStorageListLunRootResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/SanStorageListLunRoot.css" })
        TableStyle cellTableStyle();
    }

    public interface SanStorageListTargetTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/SanStorageListTargetTable.css" })
        TableStyle cellTableStyle();
    }

}
