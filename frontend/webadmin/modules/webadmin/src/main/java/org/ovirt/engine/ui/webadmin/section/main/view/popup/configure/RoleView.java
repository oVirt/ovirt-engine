package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.webadmin.widget.table.column.IsLockedImageTypeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ObjectNameColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.RoleTypeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;

public class RoleView extends Composite {

    @UiField
    SimplePanel rolesTabContent;

    @UiField
    RadioButton allRolesRadioButton;

    @UiField
    RadioButton adminRolesRadioButton;

    @UiField
    RadioButton userRolesRadioButton;

    @UiField
    Label showLabel;

    private SimpleActionTable<roles> table;

    private SimpleActionTable<permissions> permissionTable;

    private SplitLayoutPanel splitLayoutPanel;

    private final RoleModelProvider roleModelProvider;

    private final RolePermissionModelProvider permissionModelProvider;

    @Inject
    public RoleView(ClientGinjector ginjector,
            EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            RoleModelProvider roleModelProvider,
            RolePermissionModelProvider permissionModelProvider) {
        super();
        this.roleModelProvider = roleModelProvider;
        this.permissionModelProvider = permissionModelProvider;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);

        initRolesFilterRadioButtons();
        initSplitLayoutPanel();

        initRoleTable();
        initPermissionTable();
    }

    private void initSplitLayoutPanel() {
        splitLayoutPanel = new SplitLayoutPanel();
        splitLayoutPanel.setHeight("100%");
        splitLayoutPanel.setWidth("100%");
        rolesTabContent.add(splitLayoutPanel);
    }

    public void setSubTabVisibility(boolean visible) {
        splitLayoutPanel.clear();
        if (visible) {
            splitLayoutPanel.addSouth(permissionTable, 150);
        }
        splitLayoutPanel.add(table);
    }

    private void initRolesFilterRadioButtons() {
        allRolesRadioButton.setValue(true);

        allRolesRadioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    roleModelProvider.getModel().setItemsFilter(null);
                    roleModelProvider.getModel().ForceRefresh();
                }
            }
        });

        adminRolesRadioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    roleModelProvider.getModel().setItemsFilter(RoleType.ADMIN);
                    roleModelProvider.getModel().ForceRefresh();
                }
            }
        });

        userRolesRadioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    roleModelProvider.getModel().setItemsFilter(RoleType.USER);
                    roleModelProvider.getModel().ForceRefresh();
                }
            }
        });

    }

    private void localize(ApplicationConstants constants) {
        allRolesRadioButton.setText(constants.allRolesLabel());
        adminRolesRadioButton.setText(constants.adminRolesLabel());
        userRolesRadioButton.setText(constants.userRolesLabel());
        showLabel.setText(constants.showRolesLabel());
    }

    interface ViewUiBinder extends UiBinder<VerticalPanel, RoleView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private void initRoleTable() {
        this.table =
                new SimpleActionTable<roles>(roleModelProvider, getTableHeaderlessResources(), getTableResources());
        TextColumnWithTooltip<roles> nameColumn = new TextColumnWithTooltip<roles>() {
            @Override
            public String getValue(roles object) {
                return object.getname();
            }
        };

        table.addColumn(new IsLockedImageTypeColumn(), "", "20px");

        table.addColumn(new RoleTypeColumn(), "", "20px");

        table.addColumn(nameColumn, "Name", "100px");

        TextColumnWithTooltip<roles> descColumn = new TextColumnWithTooltip<roles>() {
            @Override
            public String getValue(roles object) {
                return object.getdescription();
            }
        };
        table.addColumn(descColumn, "Description", "300px");

        table.addActionButton(new UiCommandButtonDefinition<roles>("New") {
            @Override
            protected UICommand resolveCommand() {
                return roleModelProvider.getModel().getNewCommand();
            }
        });

        table.addActionButton(new UiCommandButtonDefinition<roles>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return roleModelProvider.getModel().getEditCommand();
            }
        });

        table.addActionButton(new UiCommandButtonDefinition<roles>("Copy") {
            @Override
            protected UICommand resolveCommand() {
                return roleModelProvider.getModel().getCloneCommand();
            }
        });

        table.addActionButton(new UiCommandButtonDefinition<roles>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return roleModelProvider.getModel().getRemoveCommand();
            }
        });

        splitLayoutPanel.add(table);

        table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                roleModelProvider.setSelectedItems(table.getSelectionModel().getSelectedList());
                if (table.getSelectionModel().getSelectedList().size() > 0) {
                    setSubTabVisibility(true);
                } else {
                    setSubTabVisibility(false);
                }
            }
        });

    }

    private void initPermissionTable() {
        permissionTable = new SimpleActionTable<permissions>(permissionModelProvider,
                getTableHeaderlessResources(), getTableResources());

        TextColumnWithTooltip<permissions> userColumn = new TextColumnWithTooltip<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getOwnerName();
            }
        };
        permissionTable.addColumn(userColumn, "User");

        TextColumnWithTooltip<permissions> permissionColumn = new ObjectNameColumn<permissions>() {
            @Override
            protected Object[] getRawValue(permissions object) {
                return new Object[] { object.getObjectType(), object.getObjectName() };
            }
        };
        permissionTable.addColumn(permissionColumn, "Object");

        permissionTable.addActionButton(new UiCommandButtonDefinition<permissions>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return permissionModelProvider.getModel().getRemoveCommand();
            }
        });

        permissionTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                permissionModelProvider.setSelectedItems(permissionTable.getSelectionModel().getSelectedList());
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
