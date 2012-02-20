package org.ovirt.engine.ui.webadmin.widget.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;
import org.ovirt.engine.ui.webadmin.widget.table.column.LunSelectionColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.LunTextColumn;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class SanStorageLunToTargetList extends AbstractSanStorageList<LunModel, ListModel> {

    public SanStorageLunToTargetList(SanStorageModelBase model) {
        super(model);
    }

    public SanStorageLunToTargetList(SanStorageModelBase model, boolean hideLeaf) {
        super(model, hideLeaf);
    }

    @Override
    protected void createSanStorageListWidget() {
        super.createSanStorageListWidget();
    }

    @Override
    protected ListModel getLeafModel(LunModel rootModel) {
        return rootModel.getTargetsList();
    }

    @Override
    protected void createHeaderWidget() {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<ListModel>(false,
                (Resources) GWT.create(SanStorageListHeaderResources.class),
                true);

        // Create select all button
        addSelectAllButton(table);

        // Create header table
        initRootNodeTable(table);

        // Add first blank column
        table.insertColumn(0, new TextColumn<LunModel>() {
            @Override
            public String getValue(LunModel model) {
                return "";
            }
        }, "", "20px");

        // Add blank item list
        table.setRowData(new ArrayList<EntityModel>());

        // Style table
        table.setWidth("100%", true);

        // Add table as header widget
        treeHeader.add(table);
    }

    private void addSelectAllButton(EntityModelCellTable<ListModel> table) {
        // Create 'Select All' check-box
        Header<Boolean> selectAllHeader = new Header<Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue() {
                return model.getIsAllLunsSelected();
            }
        };

        selectAllHeader.setUpdater(new ValueUpdater<Boolean>() {
            @Override
            public void update(Boolean value)
            {
                model.setIsAllLunsSelected(value);
            }
        });

        table.addColumn(new TextColumn<LunModel>() {
            @Override
            public String getValue(LunModel model) {
                return "";
            }
        }, selectAllHeader, "27px");
    }

    @Override
    protected TreeItem createRootNode(LunModel rootModel) {
        final EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(true,
                        (Resources) GWT.create(SanStorageListLunRootResources.class));

        // Create table
        initRootNodeTable(table);

        // Set custom selection column
        LunSelectionColumn lunSelectionColumn = new LunSelectionColumn() {
            @Override
            public LunModel getValue(LunModel object) {
                return object;
            }
        };
        table.setCustomSelectionColumn(lunSelectionColumn, "25px");

        // Add items
        List<LunModel> items = new ArrayList<LunModel>();
        items.add(rootModel);
        ListModel listModel = new ListModel();
        listModel.setItems(items);

        // Add selection handlers
        rootModel.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if (propName.equals("IsSelected")) {
                    LunModel lunModel = (LunModel) sender;
                    if (lunModel.getIsSelected() != table.getSelectionModel().isSelected(lunModel)) {
                        table.getSelectionModel().setSelected(lunModel, lunModel.getIsSelected());
                    }
                }
            }
        });

        table.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                LunModel lunModel = (LunModel) table.getVisibleItem(0);
                lunModel.setIsSelected(table.getSelectionModel().isSelected(lunModel));
            }
        });

        // Update table
        table.setRowData(items);
        table.setWidth("100%", true);

        // Create tree item
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(table);
        TreeItem item = new TreeItem(panel);
        return item;
    }

    private void initRootNodeTable(EntityModelCellTable<ListModel> table) {

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getLunId();
            }
        }, "LUN ID", "135px");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return String.valueOf(model.getSize()) + "GB";
            }
        }, "Dev. Size", "70px");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return String.valueOf(model.getMultipathing());
            }
        }, "#path", "40px");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getVendorId();
            }
        }, "Vendor ID", "80px");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getProductId();
            }
        }, "Product ID", "80px");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getSerial();
            }
        }, "Serial");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TreeItem createLeafNode(ListModel leafModel) {
        if (hideLeaf) {
            return null;
        }

        EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(false,
                        (Resources) GWT.create(SanStorageListTargetTableResources.class),
                        true);

        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getName();
            }
        }, "Target Name");

        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getAddress();
            }
        }, "Address", "100px");

        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getPort();
            }
        }, "Port", "80px");

        List<SanTargetModel> items = (List<SanTargetModel>) leafModel.getItems();
        if (items.isEmpty()) {
            return null;
        }

        table.setRowData(items == null ? new ArrayList<SanTargetModel>() : items);
        table.edit(leafModel);

        ScrollPanel panel = new ScrollPanel();
        panel.add(table);
        TreeItem item = new TreeItem(panel);
        return item;
    }

}
