package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.ui.common.MainTableHeaderlessResources;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractObjectNameColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.IsLockedImageTypeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.RoleTypeColumn;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

    private SimpleActionTable<Role> table;
    private SimpleActionTable<Permission> permissionTable;
    private SplitLayoutPanel splitLayoutPanel;

    private final RoleModelProvider roleModelProvider;
    private final RolePermissionModelProvider permissionModelProvider;

    private final EventBus eventBus;
    private final ClientStorage clientStorage;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public RoleView(RoleModelProvider roleModelProvider,
            RolePermissionModelProvider permissionModelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        this.roleModelProvider = roleModelProvider;
        this.permissionModelProvider = permissionModelProvider;
        this.eventBus = eventBus;
        this.clientStorage = clientStorage;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();

        initRolesFilterRadioButtons();
        initSplitLayoutPanel();

        initRoleTable();
        initPermissionTable();
    }

    private void initSplitLayoutPanel() {
        splitLayoutPanel = new SplitLayoutPanel();
        splitLayoutPanel.setHeight("100%"); //$NON-NLS-1$
        splitLayoutPanel.setWidth("100%"); //$NON-NLS-1$
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
                    roleModelProvider.getModel().forceRefresh();
                }
            }
        });

        adminRolesRadioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    roleModelProvider.getModel().setItemsFilter(RoleType.ADMIN);
                    roleModelProvider.getModel().forceRefresh();
                }
            }
        });

        userRolesRadioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    roleModelProvider.getModel().setItemsFilter(RoleType.USER);
                    roleModelProvider.getModel().forceRefresh();
                }
            }
        });

    }

    private void localize() {
        allRolesRadioButton.setText(constants.allRolesLabel());
        adminRolesRadioButton.setText(constants.adminRolesLabel());
        userRolesRadioButton.setText(constants.userRolesLabel());
        showLabel.setText(constants.showRolesLabel());
    }

    interface ViewUiBinder extends UiBinder<VerticalPanel, RoleView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private void initRoleTable() {
        this.table = new SimpleActionTable<>(roleModelProvider,
                getTableHeaderlessResources(), getTableResources(), eventBus, clientStorage);

        this.table.enableColumnResizing();
        AbstractTextColumn<Role> nameColumn = new AbstractTextColumn<Role>() {
            @Override
            public String getValue(Role object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();

        table.addColumn(new IsLockedImageTypeColumn(), constants.empty(), "25px"); //$NON-NLS-1$

        table.addColumn(new RoleTypeColumn(), constants.empty(), "25px"); //$NON-NLS-1$

        table.addColumn(nameColumn, constants.nameRole(), "175px"); //$NON-NLS-1$

        AbstractTextColumn<Role> descColumn = new AbstractTextColumn<Role>() {
            @Override
            public String getValue(Role object) {
                return object.getDescription();
            }
        };
        descColumn.makeSortable();
        table.addColumn(descColumn, constants.descriptionRole(), "575px"); //$NON-NLS-1$

        table.addActionButton(new WebAdminButtonDefinition<Role>(constants.newRole()) {
            @Override
            protected UICommand resolveCommand() {
                return roleModelProvider.getModel().getNewCommand();
            }
        });

        table.addActionButton(new WebAdminButtonDefinition<Role>(constants.editRole()) {
            @Override
            protected UICommand resolveCommand() {
                return roleModelProvider.getModel().getEditCommand();
            }
        });

        table.addActionButton(new WebAdminButtonDefinition<Role>(constants.copyRole()) {
            @Override
            protected UICommand resolveCommand() {
                return roleModelProvider.getModel().getCloneCommand();
            }
        });

        table.addActionButton(new WebAdminButtonDefinition<Role>(constants.removeRole()) {
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
        permissionTable = new SimpleActionTable<>(permissionModelProvider,
                getTableHeaderlessResources(), getTableResources(), eventBus, clientStorage);

        permissionTable.enableColumnResizing();

        AbstractTextColumn<Permission> userColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getOwnerName();
            }
        };
        userColumn.makeSortable();
        permissionTable.addColumn(userColumn, constants.userPermission());

        AbstractTextColumn<Permission> permissionColumn = new AbstractObjectNameColumn<Permission>() {
            @Override
            protected Object[] getRawValue(Permission object) {
                return new Object[] { object.getObjectType(), object.getObjectName() };
            }
        };
        permissionColumn.makeSortable();
        permissionTable.addColumn(permissionColumn, constants.objectPermission());

        permissionTable.addActionButton(new WebAdminButtonDefinition<Permission>(constants.removePermission()) {
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

}
