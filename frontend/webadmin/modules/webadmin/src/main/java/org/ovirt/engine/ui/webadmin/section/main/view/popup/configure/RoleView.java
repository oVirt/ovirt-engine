package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
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
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;

public class RoleView extends Composite {

    interface ViewUiBinder extends UiBinder<Container, RoleView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<RoleView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    RadioButton allRolesRadioButton;

    @UiField
    RadioButton adminRolesRadioButton;

    @UiField
    RadioButton userRolesRadioButton;

    @UiField
    Column actionPanelContainer;

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    ResizeLayoutPanel roleTablePanel;

    @UiField
    FlowPanel permissionTablePanel;

    @WithElementId
    SimpleActionTable<Void, Role> roleTable;

    @WithElementId
    SimpleActionTable<Role, Permission> permissionTable;

    private final RoleModelProvider roleModelProvider;
    private final RolePermissionModelProvider permissionModelProvider;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public RoleView(EventBus eventBus, ClientStorage clientStorage,
            RoleModelProvider roleModelProvider,
            RoleActionPanelPresenterWidget roleActionPanel,
            RolePermissionModelProvider permissionModelProvider,
            RolePermissionActionPanelPresenterWidget permissionActionPanel) {
         this.roleModelProvider = roleModelProvider;
        this.permissionModelProvider = permissionModelProvider;

        // We need to instantiate the tables first, then set the element id, and then set the columns so the
        // persistence framework has all the right information to work.
        roleTable = new SimpleActionTable<>(roleModelProvider,
                getTableResources(), eventBus, clientStorage);
        permissionTable = new SimpleActionTable<>(permissionModelProvider,
                getTableResources(), eventBus, clientStorage);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initRolesFilterRadioButtons();
        actionPanelContainer.add(roleActionPanel);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initRoleTable();
        initPermissionTable(permissionActionPanel);

        roleTablePanel.addResizeHandler(e -> {
            // Set the height of the role table to its container height - the table control height.
            roleTable.table.setHeight(
                    Math.max(
                            0,
                            e.getHeight() - roleTable.getTableControlsHeight()) + Unit.PX.getType());
            // Set the height of the permissions table to its container height - the action panel height and - the
            // table controls height.
            permissionTable.table.setHeight(
                    Math.max(
                            0,
                            permissionTablePanel.getOffsetHeight()
                                    - permissionTable.getTableControlsHeight()
                                    - permissionActionPanel.asWidget().getOffsetHeight())
                            + Unit.PX.getType());
        });
        setSubTabVisibility(false);
    }

    public void setSubTabVisibility(boolean visible) {
        splitLayoutPanel.clear();
        if (visible) {
            splitLayoutPanel.addSouth(permissionTablePanel, 160);
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

    private void initRoleTable() {
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
            roleModelProvider.setSelectedItems(roleTable.getSelectionModel().getSelectedObjects());
            if (roleTable.getSelectionModel().getSelectedObjects().size() > 0) {
                setSubTabVisibility(true);
            } else {
                setSubTabVisibility(false);
            }
        });

        roleTablePanel.add(roleTable);
    }

    private void initPermissionTable(RolePermissionActionPanelPresenterWidget permissionActionPanel) {
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
                permissionModelProvider.setSelectedItems(permissionTable.getSelectionModel().getSelectedObjects()));

        permissionTablePanel.add(permissionActionPanel);
        permissionTablePanel.add(permissionTable);
    }

    protected Resources getTableResources() {
        return (Resources) GWT.create(MainTableResources.class);
    }

}
