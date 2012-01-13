package org.ovirt.engine.ui.webadmin.widget.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.widget.UiCommandButton;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.webadmin.widget.table.column.LunSelectionColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.LunTextColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ScrollableTextColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;

public class SanStorageTargetToLunList extends AbstractSanStorageList<SanTargetModel, ListModel> {

    protected int treeScrollPosition;

    public SanStorageTargetToLunList(SanStorageModelBase model) {
        super(model);
    }

    public SanStorageTargetToLunList(SanStorageModelBase model, boolean hideLeaf) {
        super(model, hideLeaf);
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
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<ListModel>(false,
                (Resources) GWT.create(SanStorageListHeaderResources.class),
                true);

        // Add first blank column
        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return "";
            }
        }, "", "20px");

        // Create header table
        initRootNodeTable(table);

        // Add last blank column
        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return "";
            }
        }, "", "75px");

        // Add blank item list
        table.setRowData(new ArrayList<EntityModel>());

        // Add table as header widget
        treeHeader.add(table);
    }

    @Override
    protected TreeItem createRootNode(SanTargetModel rootModel) {
        EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(false,
                        (Resources) GWT.create(SanStorageListTargetRootResources.class),
                        true);

        // Create table
        initRootNodeTable(table);

        // Update and edit table
        List<SanTargetModel> items = new ArrayList<SanTargetModel>();
        items.add(rootModel);
        table.setRowData(items);
        table.edit(model);

        // Create tree node's panel
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(table);

        // Add login button column
        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return "";
            }
        }, "", "0px");

        // Create login button
        ApplicationConstants constants = ClientGinjectorProvider.instance().getApplicationConstants();
        final UiCommandButton loginButton = new UiCommandButton();
        loginButton.setCommand(rootModel.getLoginCommand());
        loginButton.setLabel(constants.storageIscsiPopupLoginButtonLabel());
        loginButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                treeScrollPosition = treeContainer.getVerticalScrollPosition();
                loginButton.getCommand().Execute();
            }
        });
        loginButton.setWidth("50px");

        TreeItem item = new TreeItem(panel);
        panel.add(loginButton);
        panel.setCellVerticalAlignment(loginButton, HasVerticalAlignment.ALIGN_MIDDLE);

        return item;
    }

    private void initRootNodeTable(EntityModelCellTable<ListModel> table) {
        table.addColumn(new ScrollableTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getName();
            }
        }, "Target Name");

        table.addColumn(new ScrollableTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getAddress();
            }
        }, "Address", "100px");

        table.addColumn(new ScrollableTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getPort();
            }
        }, "Port", "80px");

        table.setWidth("100%", true);
    }

    @Override
    protected TreeItem createLeafNode(ListModel leafModel) {
        if (hideLeaf) {
            return null;
        }

        EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(true, (Resources) GWT.create(SanStorageListLunTableResources.class));

        LunSelectionColumn lunSelectionColumn = new LunSelectionColumn() {
            @Override
            public LunModel getValue(LunModel object) {
                return object;
            }
        };

        table.setCustomSelectionColumn(lunSelectionColumn, "30px");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getLunId();
            }
        }, "LUN ID");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return String.valueOf(model.getSize()) + "GB";
            }
        }, "Dev. Size", "60px");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return String.valueOf(model.getMultipathing());
            }
        }, "#path", "60px");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getVendorId();
            }
        }, "Vendor ID");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getProductId();
            }
        }, "Product ID");

        table.addColumn(new LunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getSerial();
            }
        }, "Serial", "90px");

        ScrollPanel panel = new ScrollPanel();
        panel.add(table);
        TreeItem item = new TreeItem(panel);

        List<LunModel> items = (List<LunModel>) leafModel.getItems();
        if (!items.isEmpty()) {
            table.setRowData(items == null ? new ArrayList<LunModel>() : items);
            table.edit(leafModel);
        }
        else {
            item.setUserObject(Boolean.TRUE);
        }

        return item;
    }

    @Override
    protected void updateItems() {
        super.updateItems();

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                treeContainer.setVerticalScrollPosition(treeScrollPosition);
            }
        });
    }
}
