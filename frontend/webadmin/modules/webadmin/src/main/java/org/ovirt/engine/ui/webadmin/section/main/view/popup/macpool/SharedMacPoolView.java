package org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.ui.common.MainTableHeaderlessResources;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.permissions.PermissionListModelTable;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.SharedMacPoolListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SharedMacPoolModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;

public class SharedMacPoolView extends Composite {

    private final PermissionModelProvider<MacPool, SharedMacPoolListModel> permissionModelProvider;
    private final SharedMacPoolModelProvider sharedMacPoolModelProvider;

    private final SimpleActionTable<MacPool> macPoolTable;
    private final PermissionListModelTable<PermissionListModel<MacPool>> authorizationTable;

    private final EventBus eventBus;
    private final ClientStorage clientStorage;
    private final MainTableHeaderlessResources headerlessResources;
    private final MainTableResources tableResources;
    private final SplitLayoutPanel rootPanel;

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SharedMacPoolView(final SharedMacPoolModelProvider sharedMacPoolModelProvider,
            final PermissionModelProvider<MacPool, SharedMacPoolListModel> permissionModelProvider,
            final EventBus eventBus,
            final ClientStorage clientStorage,
            final MainTableHeaderlessResources headerlessResources,
            final MainTableResources tableResources) {

        this.sharedMacPoolModelProvider = sharedMacPoolModelProvider;
        this.permissionModelProvider = permissionModelProvider;

        this.eventBus = eventBus;
        this.clientStorage = clientStorage;
        this.headerlessResources = headerlessResources;
        this.tableResources = tableResources;

        macPoolTable = createMacPoolTable();
        authorizationTable = new PermissionListModelTable<>(permissionModelProvider, eventBus, clientStorage);
        authorizationTable.initTable();

        authorizationTable.getTable().getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                permissionModelProvider.setSelectedItems(authorizationTable.getTable().getSelectedItems());
            }
        });

        rootPanel = createRootPanel();

        setupAuthorizationTableVisibility(false);

        initWidget(rootPanel);
    }

    private void setupAuthorizationTableVisibility(boolean visible) {
        rootPanel.clear();
        if (visible) {
            rootPanel.addSouth(authorizationTable, 150);
        }
        rootPanel.add(macPoolTable);
    }

    private SplitLayoutPanel createRootPanel() {
        SplitLayoutPanel rootPanel = new SplitLayoutPanel();
        rootPanel.setHeight("495px"); //$NON-NLS-1$
        rootPanel.setWidth("100%"); //$NON-NLS-1$
        return rootPanel;
    }

    private SimpleActionTable<MacPool> createMacPoolTable() {

        final SimpleActionTable<MacPool> macPoolTable =
                new SimpleActionTable<>(sharedMacPoolModelProvider,
                        headerlessResources,
                        tableResources,
                        eventBus,
                        clientStorage);
        macPoolTable.addColumn(new AbstractImageResourceColumn<MacPool>() {

            @Override
            public ImageResource getValue(MacPool macPool) {
                return macPool.isDefaultPool() ? resources.lockImage() : null;
            }
        }, constants.empty(), "20px"); //$NON-NLS-1$
        macPoolTable.addColumn(new AbstractTextColumn<MacPool>() {

            @Override
            public String getValue(MacPool macPool) {
                return macPool.getName();
            }
        }, constants.configureMacPoolNameColumn(), "100px"); //$NON-NLS-1$
        macPoolTable.addColumn(new AbstractTextColumn<MacPool>() {

            @Override
            public String getValue(MacPool macPool) {
                return macPool.getDescription();
            }
        }, constants.configureMacPoolDescriptionColumn(), "300px"); //$NON-NLS-1$


        macPoolTable.addActionButton(new WebAdminButtonDefinition<MacPool>(constants.configureMacPoolAddButton()) {

            @Override
            protected UICommand resolveCommand() {
                return sharedMacPoolModelProvider.getModel().getNewCommand();
            }
        });

        macPoolTable.addActionButton(new WebAdminButtonDefinition<MacPool>(constants.configureMacPoolEditButton()) {

            @Override
            protected UICommand resolveCommand() {
                return sharedMacPoolModelProvider.getModel().getEditCommand();
            }
        });
        macPoolTable.addActionButton(new WebAdminButtonDefinition<MacPool>(constants.configureMacPoolRemoveButton()) {

            @Override
            protected UICommand resolveCommand() {
                return sharedMacPoolModelProvider.getModel().getRemoveCommand();
            }
        });


        macPoolTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final List<MacPool> selectedItems = macPoolTable.getSelectedItems();
                sharedMacPoolModelProvider.setSelectedItems(selectedItems);

                final PermissionListModel<MacPool> model = permissionModelProvider.getModel();

                if (selectedItems.size() == 1) {
                    model.setEntity(selectedItems.get(0));
                    setupAuthorizationTableVisibility(true);
                } else {
                    model.setEntity(null);
                    setupAuthorizationTableVisibility(false);
                }
            }
        });

        return macPoolTable;
    }
}
