package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLunActionsColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLunRemoveColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLunSelectionColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLunTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractScrollableTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStoragePartialModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.TableLayout;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import com.google.gwt.view.client.SingleSelectionModel;

public class SanStorageTargetToLunList extends AbstractSanStorageList<SanTargetModel, ListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    private LunModel selectedLunModel;
    protected int treeScrollPosition;

    public SanStorageTargetToLunList(SanStoragePartialModel model) {
        super(model);
    }

    public SanStorageTargetToLunList(SanStoragePartialModel model, boolean hideLeaf) {
        super(model, hideLeaf, false);
    }

    public SanStorageTargetToLunList(SanStoragePartialModel model, boolean hideLeaf, boolean multiSelection) {
        super(model, hideLeaf, multiSelection);
    }

    @Override
    protected void createSanStorageListWidget() {
        super.createSanStorageListWidget();
    }

    @Override
    protected ListModel getLeafModel(SanTargetModel rootModel) {
        return rootModel.getLunsList();
    }

    @Override
    protected void createHeaderWidget() {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<>(false,
                (Resources) GWT.create(SanStorageListTargetTableResources.class),
                true);

        // Add first blank column
        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return ""; //$NON-NLS-1$
            }
        }, constants.empty(), "15px"); //$NON-NLS-1$

        table.addColumn(new AbstractScrollableTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getName();
            }
        }, constants.targetNameSanStorage(), ""); //$NON-NLS-1$

        table.addColumn(new AbstractScrollableTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getAddress();
            }
        }, constants.addressSanStorage(), "210px"); //$NON-NLS-1$

        table.addColumn(new AbstractScrollableTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getPort();
            }
        }, constants.portSanStorage(), "65px"); //$NON-NLS-1$

        table.setWidth("100%"); //$NON-NLS-1$

        // Add last blank columns
        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return ""; //$NON-NLS-1$
            }
        }, constants.empty(), "30px"); //$NON-NLS-1$
        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return ""; //$NON-NLS-1$
            }
        }, constants.empty(), "17px"); //$NON-NLS-1$

        // Add blank item list
        table.setRowData(new ArrayList<EntityModel>());
        table.setWidth("100%"); //$NON-NLS-1$
        // This was the height of the header
        table.setHeight("23px"); // $NON-NLS-1$

        // Add table as header widget
        treeHeader.add(table);
    }

    private void addLoginButton(HorizontalPanel panel, SanTargetModel rootModel) {
        final UiCommandButton loginButton = new UiCommandButton();
        loginButton.setCommand(rootModel.getLoginCommand());
        loginButton.setTitle(constants.storageIscsiPopupLoginButtonLabel());
        loginButton.setIcon(IconType.ARROW_RIGHT);
        loginButton.addClickHandler(event -> {
            treeScrollPosition = treeContainer.getVerticalScrollPosition();
            loginButton.getCommand().execute();
        });
        loginButton.getElement().getStyle().setFloat(Float.RIGHT);
        loginButton.getElement().getStyle().setMarginRight(6, Unit.PX);

        panel.add(loginButton);
        panel.setCellVerticalAlignment(loginButton, HasVerticalAlignment.ALIGN_MIDDLE);
        panel.setCellWidth(loginButton, "35px"); //$NON-NLS-1$
    }

    private void additemToRootNodePanel(HorizontalPanel panel,
            StringValueLabel item,
            String text,
            String width,
            TextAlignment align) {
        item.getElement().getStyle().setBackgroundColor("transparent"); //$NON-NLS-1$
        item.getElement().getStyle().setColor("black"); //$NON-NLS-1$
        item.setValue(text);

        panel.add(item);
        panel.setCellWidth(item, width);
    }

    @Override
    protected TreeItem createRootNode(SanTargetModel rootModel) {
        HorizontalPanel panel = new HorizontalPanel();

        additemToRootNodePanel(panel, new StringValueLabel(), rootModel.getName(), "", TextAlignment.LEFT); //$NON-NLS-1$
        additemToRootNodePanel(panel, new StringValueLabel(), rootModel.getAddress(), "210px", TextAlignment.LEFT); //$NON-NLS-1$
        additemToRootNodePanel(panel, new StringValueLabel(), rootModel.getPort(), "60px", TextAlignment.LEFT); //$NON-NLS-1$
        addLoginButton(panel, rootModel);

        panel.setSpacing(1);
        panel.setWidth("100%"); //$NON-NLS-1$
        panel.getElement().getStyle().setTableLayout(TableLayout.FIXED);

        return new TreeItem(panel);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TreeItem createLeafNode(ListModel leafModel) {
        final TreeItem item = new TreeItem();

        List<LunModel> items = (List<LunModel>) leafModel.getItems();

        if (hideLeaf || items.isEmpty()) {
            item.setUserObject(Boolean.TRUE);
            return item;
        }

        final SortedListModel sortedLeafModel = new SortedListModel();
        sortedLeafModel.setItems(items);
        final EntityModelCellTable<ListModel<LunModel>> table =
                new EntityModelCellTable<>(multiSelection, (Resources) GWT.create(SanStorageListLunTableResources.class));
        table.enableColumnResizing();
        table.initModelSortHandler(sortedLeafModel);
        AbstractLunSelectionColumn lunSelectionColumn = new AbstractLunSelectionColumn(multiSelection) {
            @Override
            public LunModel getValue(LunModel object) {
                return object;
            }
        };
        table.setCustomSelectionColumn(lunSelectionColumn, "25px"); //$NON-NLS-1$

        AbstractLunTextColumn lunIdColumn = new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getLunId();
            }
        };
        lunIdColumn.makeSortable(new Comparator<LunModel>() {
            @Override public int compare(LunModel lunModel1, LunModel lunModel2) {
                return lunModel2.getLunId().compareTo(lunModel1.getLunId());
            }
        });
        table.addColumn(lunIdColumn, constants.lunIdSanStorage(), "250px"); //$NON-NLS-1$

        AbstractLunTextColumn devSizeColumn = new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return messages.gibibytes(String.valueOf(model.getSize()));
            }
        };
        devSizeColumn.makeSortable();
        table.addColumn(devSizeColumn, constants.devSizeSanStorage(), "60px"); //$NON-NLS-1$

        AbstractLunTextColumn path = new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return String.valueOf(model.getMultipathing());
            }
        };
        path.makeSortable();
        table.addColumn(path, constants.pathSanStorage(), "45px"); //$NON-NLS-1$

        AbstractLunTextColumn vendorIdColumn = new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getVendorId();
            }
        };
        vendorIdColumn.makeSortable();
        table.addColumn(vendorIdColumn, constants.vendorIdSanStorage(), "70px"); //$NON-NLS-1$

        AbstractLunTextColumn productIdColumn = new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getProductId();
            }
        };
        productIdColumn.makeSortable();
        table.addColumn(productIdColumn, constants.productIdSanStorage(), "70px"); //$NON-NLS-1$

        AbstractLunTextColumn serialNumColumn = new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getSerial();
            }
        };
        serialNumColumn.makeSortable();
        table.addColumn(serialNumColumn, constants.serialSanStorage(), "210px"); //$NON-NLS-1$

        if (model.getContainer().isNewStorage() ||
                model.getContainer().getStorage().getStatus() != StorageDomainStatus.Maintenance) {
            if (multiSelection) {
                addAbstractLunActionsColumn(table,
                        model.getContainer().isNewStorage() ? constants.addSanStorage() : constants.actionsSanStorage());
            }
        } else if (model.isReduceDeviceSupported()) {
            AbstractLunRemoveColumn removeColumn = new AbstractLunRemoveColumn(model) {
                @Override
                public LunModel getValue(LunModel object) {
                    return object;
                }
            };
            table.addColumn(removeColumn, constants.removeSanStorage(), "95px"); //$NON-NLS-1$
        }

        model.getRequireTableRefresh().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (Boolean.TRUE.equals(model.getRequireTableRefresh().getEntity())) {
                if (!multiSelection) {
                    updateLunSelectionModel(table, items);
                }
            }
            table.redraw();
        });

        table.setRowData(items);
        final Object selectedItem = sortedLeafModel.getSelectedItem();
        sortedLeafModel.setSelectedItem(null);
        table.asEditor().edit(sortedLeafModel);
        sortedLeafModel.setSelectedItem(selectedItem);

        table.setWidth("100%"); // $NON-NLS-1$

        if (!multiSelection) {
            table.getSelectionModel().addSelectionChangeHandler(event -> {
                SingleSelectionModel SingleSelectionModel = (SingleSelectionModel) event.getSource();
                selectedLunModel = SingleSelectionModel.getSelectedObject() == null ? selectedLunModel :
                        (LunModel) SingleSelectionModel.getSelectedObject();

                if (selectedLunModel != null && !selectedLunModel.getIsGrayedOut()) {
                    updateSelectedLunWarning(selectedLunModel);
                    sortedLeafModel.setSelectedItem(selectedLunModel);
                }
            });
        } else {
            for (LunModel lunModel : items) {
                table.getSelectionModel().setSelected(lunModel, lunModel.getIsSelected());
            }
            table.getSelectionModel().addSelectionChangeHandler(event -> model.updateLunWarningForDiscardAfterDelete());
        }

        item.setWidget(table);

        // Display LUNs as grayed-out if needed
        for (LunModel lunModel : items) {
            if (lunModel.getIsGrayedOut()) {
                grayOutItem(lunModel.getGrayedOutReasons(), lunModel, table);
            }
        }

        addOpenHandlerToTree(tree, item, table);

        return item;
    }

    void updateLunSelectionModel(EntityModelCellTable<ListModel<LunModel>> table, List<LunModel> items) {
        for (LunModel lunModel : items) {
            table.getSelectionModel().setSelected(lunModel, lunModel.getIsSelected());
        }
    }

    @Override
    protected void updateItems() {
        super.updateItems();

        Scheduler.get().scheduleDeferred(() -> treeContainer.setVerticalScrollPosition(treeScrollPosition));
    }

    private void addAbstractLunActionsColumn(EntityModelCellTable<ListModel<LunModel>> table, String headerString) {
        AbstractLunActionsColumn actionsColumn = new AbstractLunActionsColumn() {
            @Override
            public LunModel getValue(LunModel object) {
                return object;
            }
        };
        actionsColumn.makeSortable();
        table.addColumn(actionsColumn, headerString, "95px"); //$NON-NLS-1$
    }
}
