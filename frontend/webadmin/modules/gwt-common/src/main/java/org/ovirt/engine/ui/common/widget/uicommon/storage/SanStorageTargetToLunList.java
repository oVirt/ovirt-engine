package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLunSelectionColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLunTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractScrollableTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.TableLayout;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

public class SanStorageTargetToLunList extends AbstractSanStorageList<SanTargetModel, ListModel> {

    protected int treeScrollPosition;

    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public SanStorageTargetToLunList(SanStorageModelBase model) {
        super(model);
    }

    public SanStorageTargetToLunList(SanStorageModelBase model, boolean hideLeaf) {
        super(model, hideLeaf, false);
    }

    public SanStorageTargetToLunList(SanStorageModelBase model, boolean hideLeaf, boolean multiSelection) {
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
                (Resources) GWT.create(SanStorageListHeaderResources.class),
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
        }, constants.addressSanStorage(), "95px"); //$NON-NLS-1$

        table.addColumn(new AbstractScrollableTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getPort();
            }
        }, constants.portSanStorage(), "65px"); //$NON-NLS-1$

        table.setWidth("100%", true); //$NON-NLS-1$

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
        table.setWidth("100%", true); //$NON-NLS-1$

        // Add table as header widget
        treeHeader.add(table);
    }

    private void addLoginButton(HorizontalPanel panel, SanTargetModel rootModel) {
        final UiCommandButton loginButton = new UiCommandButton();
        loginButton.setCommand(rootModel.getLoginCommand());
        loginButton.setTitle(constants.storageIscsiPopupLoginButtonLabel());
        loginButton.setImage(rootModel.getLoginCommand().getIsExecutionAllowed() ?
                resources.loginIcon() : resources.loginDisabledIcon());
        loginButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                treeScrollPosition = treeContainer.getVerticalScrollPosition();
                loginButton.getCommand().execute();
            }
        });
        loginButton.getElement().getStyle().setFloat(Float.RIGHT);
        loginButton.getElement().getStyle().setMarginRight(6, Unit.PX);
        Style spanStyle = loginButton.getElement().getElementsByTagName("span").getItem(0).getStyle(); //$NON-NLS-1$
        spanStyle.setPosition(Style.Position.RELATIVE);
        spanStyle.setTop(-3, Unit.PX);

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
        additemToRootNodePanel(panel, new StringValueLabel(), rootModel.getAddress(), "95px", TextAlignment.LEFT); //$NON-NLS-1$
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
        TreeItem item = new TreeItem();
        List<LunModel> items = (List<LunModel>) leafModel.getItems();

        if (hideLeaf || items.isEmpty()) {
            item.setUserObject(Boolean.TRUE);
            return item;
        }

        final EntityModelCellTable<ListModel<LunModel>> table =
                new EntityModelCellTable<>(multiSelection, (Resources) GWT.create(SanStorageListLunTableResources.class));

        AbstractLunSelectionColumn lunSelectionColumn = new AbstractLunSelectionColumn(multiSelection) {
            @Override
            public LunModel getValue(LunModel object) {
                return object;
            }
        };

        table.setCustomSelectionColumn(lunSelectionColumn, "30px"); //$NON-NLS-1$

        table.addColumn(new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return model.getLunId();
            }
        }, constants.lunIdSanStorage());

        table.addColumn(new AbstractLunTextColumn() {
            @Override
            public String getRawValue(LunModel model) {
                return String.valueOf(model.getSize()) + "GB"; //$NON-NLS-1$
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
        }, constants.serialSanStorage(), "120px"); //$NON-NLS-1$

        table.setRowData(items);
        Object selectedItem = leafModel.getSelectedItem();
        leafModel.setSelectedItem(null);
        table.asEditor().edit(leafModel);
        leafModel.setSelectedItem(selectedItem);

        table.setWidth("100%", true); //$NON-NLS-1$

        if (!multiSelection) {
            for (LunModel lunModel : items) {
                if (lunModel.getIsSelected()) {
                    table.getSelectionModel().setSelected(lunModel, true);
                }
            }

            table.getSelectionModel().addSelectionChangeHandler(new Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    SingleSelectionModel SingleSelectionModel = (SingleSelectionModel) event.getSource();
                    LunModel selectedLunModel = (LunModel) SingleSelectionModel.getSelectedObject();

                    if (selectedLunModel != null) {
                        updateSelectedLunWarning(selectedLunModel);
                    }
                }
            });
        }
        else {
            for (LunModel lunModel : items) {
                table.getSelectionModel().setSelected(lunModel, lunModel.getIsSelected());
            }
        }

        ScrollPanel panel = new ScrollPanel();
        panel.add(table);
        item.setWidget(panel);

        // Display LUNs as grayed-out if needed
        for (LunModel lunModel : items) {
            if (lunModel.getIsGrayedOut()) {
                grayOutItem(lunModel.getGrayedOutReasons(), lunModel, table);
            }
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
