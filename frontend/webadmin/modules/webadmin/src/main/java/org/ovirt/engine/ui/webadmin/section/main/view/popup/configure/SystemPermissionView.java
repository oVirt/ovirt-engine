package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.MainTableHeaderlessResources;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.action.PatternflyActionPanel;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemPermissionModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class SystemPermissionView extends Composite {

    interface ViewUiBinder extends UiBinder<FlowPanel, SystemPermissionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    FlowPanel tabContent;

    private PatternflyActionPanel actionPanel;

    private SimpleActionTable<Permission> table;

    private final SystemPermissionModelProvider modelProvider;

    private final EventBus eventBus;
    private final ClientStorage clientStorage;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SystemPermissionView(SystemPermissionModelProvider modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super();
        this.modelProvider = modelProvider;
        this.eventBus = eventBus;
        this.clientStorage = clientStorage;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();

        initTable();
    }

    private void localize() {
    }

    private void initTable() {
        actionPanel = new PatternflyActionPanel();
        tabContent.add(actionPanel);
        table = new SimpleActionTable<>(modelProvider,
                getTableHeaderlessResources(), getTableResources(), eventBus, clientStorage);
        tabContent.add(table);
        table.enableColumnResizing();

        table.addColumn(new PermissionTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> userColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getOwnerName();
            }
        };
        table.addColumn(userColumn, constants.userPermission(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> authzColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getAuthz();
            }
        };
        table.addColumn(authzColumn, constants.authz(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> namespaceColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getNamespace();
            }
        };
        table.addColumn(namespaceColumn, constants.namespace(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> roleColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getRoleName();
            }
        };
        table.addColumn(roleColumn, constants.rolePermission());

        actionPanel.addButtonToActionGroup(
        table.addActionButton(new WebAdminButtonDefinition<Permission>(constants.addPermission()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getAddCommand();
            }
        }));

        actionPanel.addButtonToActionGroup(
        table.addActionButton(new WebAdminButtonDefinition<Permission>(constants.removePermission()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getRemoveCommand();
            }
        }));

        table.getSelectionModel().addSelectionChangeHandler(event -> modelProvider.setSelectedItems(table.getSelectionModel().getSelectedList()));
    }

    protected Resources getTableHeaderlessResources() {
        return (Resources) GWT.create(MainTableHeaderlessResources.class);
    }

    protected Resources getTableResources() {
        return (Resources) GWT.create(MainTableResources.class);
    }

}
