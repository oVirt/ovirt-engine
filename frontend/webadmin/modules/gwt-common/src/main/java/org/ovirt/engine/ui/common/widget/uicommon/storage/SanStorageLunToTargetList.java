package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.LunSelectionColumn;
import org.ovirt.engine.ui.common.widget.table.column.LunTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

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
        super(model, hideLeaf, false);
    }

    public SanStorageLunToTargetList(SanStorageModelBase model, boolean hideLeaf, boolean multiSelection) {
        super(model, hideLeaf, multiSelection);
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
                return ""; //$NON-NLS-1$
            }
        }, "", "20px"); //$NON-NLS-1$ //$NON-NLS-2$

        // Add blank item list
        table.setRowData(new ArrayList<EntityModel>());

        // Style table
        table.setWidth("100%", true); //$NON-NLS-1$

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
                return ""; //$NON-NLS-1$
            }
        }, multiSelection ? selectAllHeader : null, "27px"); //$NON-NLS-1$
    }

    @Override
    protected TreeItem createRootNode(LunModel rootModel) {
        final EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(multiSelection,
                        (Resources) GWT.create(SanStorageListLunRootResources.class));

        // Create table
        initRootNodeTable(table);

        // Set custom selection column
        LunSelectionColumn lunSelectionColumn = new LunSelectionColumn(multiSelection) {
            @Override
            public LunModel getValue(LunModel object) {
                return object;
            }
        };
        table.setCustomSelectionColumn(lunSelectionColumn, "25px"); //$NON-NLS-1$

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
                if (propName.equals("IsSelected")) { //$NON-NLS-1$
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
                boolean isSelected = table.getSelectionModel().isSelected(lunModel);

                if (!multiSelection) {
                    if (!isSelected) {
                        return;
                    }

                    // Clear current selection
                    for (LunModel item : (List<LunModel>) model.getItems()) {
                        if (!item.equals(lunModel) && item.getIsSelected()) {
                            item.setIsSelected(false);
                        }
                    }
                    lunModel.setIsSelected(true);
                }
                else {
                    lunModel.setIsSelected(isSelected);
                }

                if (isSelected && model.isIgnoreGrayedOut()) {
                    updateSelectedLunWarning(lunModel);
                }
            }
        });

        // Update table
        table.setRowData(items);
        table.setWidth("100%", true); //$NON-NLS-1$

        // Create tree item
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(table);
        TreeItem item = new TreeItem(panel);

        // Display LUNs as grayed-out if needed
        if (rootModel.getIsGrayedOut()) {
            grayOutItem(rootModel.getGrayedOutReasons(), rootModel, table);
        }

        return item;
    }

    private void initRootNodeTable(EntityModelCellTable<ListModel> table) {

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getLunId();
            }
        }, constants.lunIdSanStorage(), "135px"); //$NON-NLS-1$

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return String.valueOf(model.getSize()) + "GB"; //$NON-NLS-1$
            }
        }, constants.devSizeSanStorage(), "70px"); //$NON-NLS-1$

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return String.valueOf(model.getMultipathing());
            }
        }, constants.pathSanStorage(), "40px"); //$NON-NLS-1$

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return EnumTranslator.createAndTranslate(model.getStatus());
            }
        }, constants.statusSanStorage(), "60px"); //$NON-NLS-1$

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getVendorId();
            }
        }, constants.vendorIdSanStorage(), "80px"); //$NON-NLS-1$

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getProductId();
            }
        }, constants.productIdSanStorage(), "80px"); //$NON-NLS-1$

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getSerial();
            }
        }, constants.serialSanStorage());
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
        }, constants.targetNameSanStorage());

        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getAddress();
            }
        }, constants.addressSanStorage(), "100px"); //$NON-NLS-1$

        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getPort();
            }
        }, constants.portSanStorage(), "80px"); //$NON-NLS-1$

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
