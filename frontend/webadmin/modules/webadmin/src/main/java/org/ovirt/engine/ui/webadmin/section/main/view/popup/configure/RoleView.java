package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.ui.common.MainTableHeaderlessResources;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractObjectNameColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.column.IsLockedImageTypeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.RoleTypeColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;

public class RoleView extends Composite {

    interface ViewUiBinder extends UiBinder<Container, RoleView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    RadioButton allRolesRadioButton;

    @UiField
    RadioButton adminRolesRadioButton;

    @UiField
    RadioButton userRolesRadioButton;

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    FlowPanel roleTablePanel;

    @UiField
    FlowPanel permissionTablePanel;

    private SimpleActionTable<Role> roleTable;
    private SimpleActionTable<Permission> permissionTable;

    private final RoleModelProvider roleModelProvider;
    private final RolePermissionModelProvider permissionModelProvider;

    private final EventBus eventBus;
    private final ClientStorage clientStorage;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public RoleView(EventBus eventBus, ClientStorage clientStorage,
            RoleModelProvider roleModelProvider,
            RoleActionPanelPresenterWidget roleActionPanel,
            RolePermissionModelProvider permissionModelProvider,
            RolePermissionActionPanelPresenterWidget permissionActionPanel) {
        this.eventBus = eventBus;
        this.clientStorage = clientStorage;
        this.roleModelProvider = roleModelProvider;
        this.permissionModelProvider = permissionModelProvider;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initRolesFilterRadioButtons();
        initRoleTable(roleActionPanel);
        initPermissionTable(permissionActionPanel);

        setSubTabVisibility(false);
    }

    public void setSubTabVisibility(boolean visible) {
        splitLayoutPanel.clear();
        if (visible) {
            splitLayoutPanel.addSouth(permissionTablePanel, 150);
        }
        splitLayoutPanel.add(roleTablePanel);
    }

    private void initRolesFilterRadioButtons() {
        allRolesRadioButton.addValueChangeHandler(event -> {
            if (event.getValue()) {
                roleModelProvider.getModel().setItemsFilter(null);
                roleModelProvider.getModel().forceRefresh();
            }
        });

        adminRolesRadioButton.addValueChangeHandler(event -> {
            if (event.getValue()) {
                roleModelProvider.getModel().setItemsFilter(RoleType.ADMIN);
                roleModelProvider.getModel().forceRefresh();
            }
        });

        userRolesRadioButton.addValueChangeHandler(event -> {
            if (event.getValue()) {
                roleModelProvider.getModel().setItemsFilter(RoleType.USER);
                roleModelProvider.getModel().forceRefresh();
            }
        });
    }

    private void initRoleTable(RoleActionPanelPresenterWidget roleActionPanel) {
        roleTable = new SimpleActionTable<>(roleModelProvider,
                getTableHeaderlessResources(), getTableResources(), eventBus, clientStorage);

        roleTable.enableColumnResizing();

        roleTable.addColumn(new IsLockedImageTypeColumn(), constants.empty(), "25px"); //$NON-NLS-1$

        roleTable.addColumn(new RoleTypeColumn(), constants.empty(), "25px"); //$NON-NLS-1$

        AbstractTextColumn<Role> nameColumn = new AbstractTextColumn<Role>() {
            @Override
            public String getValue(Role object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        roleTable.addColumn(nameColumn, constants.nameRole(), "175px"); //$NON-NLS-1$

        AbstractTextColumn<Role> descColumn = new AbstractTextColumn<Role>() {
            @Override
            public String getValue(Role object) {
                return object.getDescription();
            }
        };
        descColumn.makeSortable();
        roleTable.addColumn(descColumn, constants.descriptionRole(), "500px"); //$NON-NLS-1$

        roleTable.getSelectionModel().addSelectionChangeHandler(event -> {
            roleModelProvider.setSelectedItems(roleTable.getSelectionModel().getSelectedList());
            if (roleTable.getSelectionModel().getSelectedList().size() > 0) {
                setSubTabVisibility(true);
            } else {
                setSubTabVisibility(false);
            }
        });

        roleTablePanel.add(roleActionPanel);
        roleTablePanel.add(roleTable);
    }

    private void initPermissionTable(RolePermissionActionPanelPresenterWidget permissionActionPanel) {
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

        permissionTable.getSelectionModel().addSelectionChangeHandler(event ->
                permissionModelProvider.setSelectedItems(permissionTable.getSelectionModel().getSelectedList()));

        permissionTablePanel.add(permissionActionPanel);
        permissionTablePanel.add(permissionTable);
    }

    protected Resources getTableHeaderlessResources() {
        return (Resources) GWT.create(MainTableHeaderlessResources.class);
    }

    protected Resources getTableResources() {
        return (Resources) GWT.create(MainTableResources.class);
    }

}
