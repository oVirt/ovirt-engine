package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLunAddOrExtendColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLunRemoveColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLunSelectionColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLunTextColumn;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.TableLayout;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.view.client.SingleSelectionModel;

public class SanStorageLunToTargetList extends AbstractSanStorageList<LunModel, ListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

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
        EntityModelCellTable<ListModel<LunModel>> table = new EntityModelCellTable<>(false,
                (Resources) GWT.create(SanStorageListTargetTableResources.class),
                true);

        // Add first blank header
        table.addColumn(new TextColumn<LunModel>() {
            @Override
            public String getValue(LunModel model) {
                return ""; //$NON-NLS-1$
            }
        }, "", "20px"); //$NON-NLS-1$

        // Create header table
        initRootNodeTable(table);

        // Add first blank column
        table.insertColumn(0, new TextColumn<LunModel>() {
            @Override
            public String getValue(LunModel model) {
                return ""; //$NON-NLS-1$
            }
        }, "", "15px"); //$NON-NLS-1$ //$NON-NLS-2$

        // Add last blank column
        table.addColumn(new TextColumn<LunModel>() {
            @Override
            public String getValue(LunModel model) {
                return ""; //$NON-NLS-1$
            }
        }, "", "22px"); //$NON-NLS-1$ //$NON-NLS-2$

        // Add blank item list
        table.setRowData(new ArrayList<EntityModel>());

        // Style table
        table.setWidth("100%"); //$NON-NLS-1$

        // Add table as header widget
        treeHeader.add(table);
    }

    final IEventListener<PropertyChangedEventArgs> lunModelSelectedItemListener = (ev, sender, args) -> {
        String propName = args.propertyName;
        final EntityModelCellTable<ListModel> table = (EntityModelCellTable<ListModel>) ev.getContext();

        if (propName.equals("IsSelected")) { //$NON-NLS-1$
            final LunModel lunModel = (LunModel) sender;
            Scheduler.get().scheduleDeferred(() -> {
                table.getSelectionModel().setSelected(lunModel, lunModel.getIsSelected());
                table.redraw();
            });
        }
    };

    @Override
    protected TreeItem createRootNode(LunModel rootModel) {
        final EntityModelCellTable<ListModel<LunModel>> table =
                new EntityModelCellTable<>(multiSelection, (Resources) GWT.create(SanStorageListLunRootResources.class));

        // Create table
        initRootNodeTable(table);

        // Set custom selection column
        AbstractLunSelectionColumn lunSelectionColumn = new AbstractLunSelectionColumn() {
            @Override
            public LunModel getValue(LunModel object) {
                return object;
            }
        };
        table.setCustomSelectionColumn(lunSelectionColumn, "20px"); //$NON-NLS-1$

        // Add items
        List<LunModel> items = new ArrayList<>();
        items.add(rootModel);
        ListModel<LunModel> listModel = new ListModel<>();
        listModel.setItems(items);

        // Update table
        table.setRowData(items);
        table.asEditor().edit(listModel);

        table.setWidth("100%"); // $NON-NLS-1$
        // This was the height of the header
        table.setHeight("46px"); // $NON-NLS-1$

        rootModel.getPropertyChangedEvent().removeListener(lunModelSelectedItemListener);
        rootModel.getPropertyChangedEvent().addListener(lunModelSelectedItemListener, table);

        if (!multiSelection) {
            table.getSelectionModel().addSelectionChangeHandler(event -> {
                SingleSelectionModel SingleSelectionModel = (SingleSelectionModel) event.getSource();
                LunModel selectedLunModel = (LunModel) SingleSelectionModel.getSelectedObject();

                if (selectedLunModel != null) {
                    updateSelectedLunWarning(selectedLunModel);
                }
            });
        }
        else {
            table.getSelectionModel().setSelected(rootModel, rootModel.getIsSelected());
            table.getSelectionModel().addSelectionChangeHandler(event -> model.updateLunWarningForDiscardAfterDelete());
        }

        // Create tree item
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(table);
        panel.setWidth("100%"); // $NON-NLS-1$
        panel.getElement().getStyle().setTableLayout(TableLayout.FIXED);

        TreeItem item = new TreeItem(table);

        // Display LUNs as grayed-out if needed
        if (rootModel.getIsGrayedOut()) {
            grayOutItem(rootModel.getGrayedOutReasons(), rootModel, table);
        }

        return item;
    }

    private void initRootNodeTable(EntityModelCellTable<ListModel<LunModel>> table) {

        table.addColumn(new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getLunId();
            }
        }, constants.lunIdSanStorage(), "35px"); //$NON-NLS-1$

        table.addColumn(new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return messages.gigabytes(String.valueOf(model.getSize()));
            }
        }, constants.devSizeSanStorage(), "70px"); //$NON-NLS-1$

        table.addColumn(new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return String.valueOf(model.getMultipathing());
            }
        }, constants.pathSanStorage(), "55px"); //$NON-NLS-1$

        table.addColumn(new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getVendorId();
            }
        }, constants.vendorIdSanStorage(), "100px"); //$NON-NLS-1$

        table.addColumn(new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getProductId();
            }
        }, constants.productIdSanStorage(), "100px"); //$NON-NLS-1$

        table.addColumn(new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getSerial();
            }
        }, constants.serialSanStorage(), "350px"); //$NON-NLS-1$

        if (!model.getContainer().isNewStorage()) {
            StorageDomainStatus status = model.getContainer().getStorage().getStatus();
            if (status == StorageDomainStatus.Maintenance) {
                boolean reduceDeviceFromStorageDomainSupported =
                        (Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(
                        ConfigValues.ReduceDeviceFromStorageDomain,
                        model.getContainer().getDataCenter().getSelectedItem().getCompatibilityVersion().toString());
                if (reduceDeviceFromStorageDomainSupported) {
                    table.addColumn(new AbstractLunRemoveColumn(model) {
                        @Override
                        public LunModel getValue(LunModel object) {
                            return object;
                        }
                    }, constants.removeSanStorage(), "85px"); //$NON-NLS-1$
                    model.getRequireTableRefresh().getEntityChangedEvent().addListener((ev, sender, args) -> {
                        table.redraw();
                    });
                }
            } else {
                addAbstractLunAddOrExtendColumn(table, constants.actionsSanStorage());
            }
        } else {
            addAbstractLunAddOrExtendColumn(table, constants.addSanStorage());
        }
    }

    private void addAbstractLunAddOrExtendColumn(EntityModelCellTable<ListModel<LunModel>> table, String headerString) {
        table.addColumn(new AbstractLunAddOrExtendColumn() {
            @Override
            public LunModel getValue(LunModel object) {
                return object;
            }
        }, headerString, "85px"); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TreeItem createLeafNode(ListModel leafModel) {
        if (hideLeaf) {
            return null;
        }

        EntityModelCellTable<ListModel<LunModel>> table =
                new EntityModelCellTable<>(false, (Resources) GWT.create(SanStorageListTargetTableResources.class), true);

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

        table.setRowData(items);

        Object selectedItem = leafModel.getSelectedItem();
        leafModel.setSelectedItem(null);
        table.asEditor().edit(leafModel);
        leafModel.setSelectedItem(selectedItem);

        table.setWidth("100%"); // $NON-NLS-1$

        TreeItem item = new TreeItem(table);
        addOpenHandlerToTree(tree, item, table);
        return item;
    }
}
