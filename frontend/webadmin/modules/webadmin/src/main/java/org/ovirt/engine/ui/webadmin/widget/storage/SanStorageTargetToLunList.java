package org.ovirt.engine.ui.webadmin.widget.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
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
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

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
        }, "Address", "95px");

        table.addColumn(new ScrollableTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getPort();
            }
        }, "Port", "65px");

        table.setWidth("100%", true);

        // Add last blank column
        table.addColumn(new TextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return "";
            }
        }, "", "80px");

        // Add blank item list
        table.setRowData(new ArrayList<EntityModel>());

        // Add table as header widget
        treeHeader.add(table);
    }

    private void addLoginButton(HorizontalPanel panel, SanTargetModel rootModel) {
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
        loginButton.setWidth("55px");

        panel.add(loginButton);
        panel.setCellWidth(loginButton, "60px");
        panel.setCellHorizontalAlignment(loginButton, HasHorizontalAlignment.ALIGN_RIGHT);
    }

    private void additemToRootNodePanel(HorizontalPanel panel,
            TextBoxLabel item,
            String text,
            String width,
            TextAlignment align) {
        item.getElement().getStyle().setBackgroundColor("transparent");
        item.getElement().getStyle().setColor("black");
        item.setAlignment(align);
        item.setText(text);

        panel.add(item);
        panel.setCellWidth(item, width);
    }

    @Override
    protected TreeItem createRootNode(SanTargetModel rootModel) {
        HorizontalPanel panel = new HorizontalPanel();

        additemToRootNodePanel(panel, new TextBoxLabel(), rootModel.getName(), "310px", TextAlignment.LEFT);
        additemToRootNodePanel(panel, new TextBoxLabel(), rootModel.getAddress(), "80px", TextAlignment.CENTER);
        additemToRootNodePanel(panel, new TextBoxLabel(), rootModel.getPort(), "45px", TextAlignment.CENTER);
        addLoginButton(panel, rootModel);

        panel.setSpacing(1);
        panel.setWidth("100%");

        return new TreeItem(panel);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TreeItem createLeafNode(ListModel leafModel) {
        TreeItem item = new TreeItem();
        List<LunModel> items = (List<LunModel>) leafModel.getItems();

        if (hideLeaf || items.isEmpty()) {
            item.setUserObject(Boolean.TRUE);
            return item;
        }

        EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(true,
                        (Resources) GWT.create(SanStorageListLunTableResources.class));

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

        table.setRowData(items == null ? new ArrayList<LunModel>() : items);
        table.edit(leafModel);

        ScrollPanel panel = new ScrollPanel();
        panel.add(table);
        item.setWidget(panel);

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
