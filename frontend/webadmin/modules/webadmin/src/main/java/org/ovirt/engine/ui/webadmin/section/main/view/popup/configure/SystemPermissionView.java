package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.MainTableHeaderlessResources;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemPermissionModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class SystemPermissionView extends Composite {

    interface ViewUiBinder extends UiBinder<Container, SystemPermissionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    FlowPanel tablePanel;

    private SimpleActionTable<Permission> table;

    private final SystemPermissionModelProvider modelProvider;

    private final EventBus eventBus;
    private final ClientStorage clientStorage;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SystemPermissionView(EventBus eventBus,  ClientStorage clientStorage,
            SystemPermissionModelProvider modelProvider,
            SystemPermissionActionPanelPresenterWidget actionPanel) {
        super();
        this.eventBus = eventBus;
        this.clientStorage = clientStorage;
        this.modelProvider = modelProvider;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initTable(actionPanel);
    }

    private void initTable(SystemPermissionActionPanelPresenterWidget actionPanel) {
        table = new SimpleActionTable<>(modelProvider,
                getTableHeaderlessResources(), getTableResources(), eventBus, clientStorage);
        table.enableColumnResizing();

        table.addColumn(new PermissionTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> userColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getOwnerName();
            }
        };
        userColumn.makeSortable((u1, u2) -> u1.getOwnerName().compareTo(u2.getObjectName()));
        table.addColumn(userColumn, constants.userPermission(), "270px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> authzColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getAuthz();
            }
        };
        authzColumn.makeSortable((a1, a2) -> a1.getAuthz().compareTo(a2.getAuthz()));
        table.addColumn(authzColumn, constants.authz(), "180px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> namespaceColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getNamespace();
            }
        };
        namespaceColumn.makeSortable((ns1, ns2) -> ns1.getNamespace().compareTo(ns1.getNamespace()));
        table.addColumn(namespaceColumn, constants.namespace(), "170px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> roleColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getRoleName();
            }
        };
        roleColumn.makeSortable((r1, r2) -> r1.getRoleName().compareTo(r2.getRoleName()));
        table.addColumn(roleColumn, constants.rolePermission(), "110px"); //$NON-NLS-1$

        table.getSelectionModel().addSelectionChangeHandler(event -> modelProvider.setSelectedItems(
                table.getSelectionModel().getSelectedObjects()));

        tablePanel.add(actionPanel);
        tablePanel.add(table);
    }

    protected Resources getTableHeaderlessResources() {
        return (Resources) GWT.create(MainTableHeaderlessResources.class);
    }

    protected Resources getTableResources() {
        return (Resources) GWT.create(MainTableResources.class);
    }

}
