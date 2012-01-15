package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemPermissionModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.webadmin.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;

public class SystemPermissionView extends Composite {

    interface ViewUiBinder extends UiBinder<SimplePanel, SystemPermissionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel tabContent;

    SplitLayoutPanel content;

    private SimpleActionTable<permissions> table;

    private final SystemPermissionModelProvider modelProvider;

    @Inject
    public SystemPermissionView(ClientGinjector ginjector,
            EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            SystemPermissionModelProvider modelProvider) {
        super();
        this.modelProvider = modelProvider;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);

        content = new SplitLayoutPanel();
        content.setWidth("100%");
        content.setHeight("100%");
        tabContent.add(content);

        initTable();
    }

    private void localize(ApplicationConstants constants) {

    }

    private void initTable() {
        table =
                new SimpleActionTable<permissions>(modelProvider,
                        getTableHeaderlessResources(),
                        getTableResources());

        content.add(table);

        table.addColumn(new PermissionTypeColumn(), "", "30px");

        TextColumnWithTooltip<permissions> userColumn = new TextColumnWithTooltip<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getOwnerName();
            }
        };
        table.addColumn(userColumn, "User");

        TextColumnWithTooltip<permissions> roleColumn = new TextColumnWithTooltip<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getRoleName();
            }
        };
        table.addColumn(roleColumn, "Role");

        table.addActionButton(new UiCommandButtonDefinition<permissions>("Add") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getAddCommand();
            }
        });

        table.addActionButton(new UiCommandButtonDefinition<permissions>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getRemoveCommand();
            }
        });

        table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                modelProvider.setSelectedItems(table.getSelectionModel().getSelectedList());
            }
        });
    }

    protected Resources getTableHeaderlessResources() {
        return (Resources) GWT.create(MainTableHeaderlessResources.class);
    }

    protected Resources getTableResources() {
        return (Resources) GWT.create(MainTableResources.class);
    }

    public interface MainTableHeaderlessResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TabCellTableHeaderless.css" })
        TableStyle cellTableStyle();
    }

    public interface MainTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TabCellTable.css" })
        TableStyle cellTableStyle();
    }
}
