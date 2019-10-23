package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.common.widget.uicommon.permissions.PermissionWithInheritedPermissionListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.CpuProfilesActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.CpuProfilePermissionModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class CpuProfilesListModelTable extends AbstractModelBoundTableWidget<Cluster, CpuProfile, CpuProfileListModel> {

    interface WidgetUiBinder extends UiBinder<Widget, CpuProfilesListModelTable> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<CpuProfilesListModelTable> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final PermissionWithInheritedPermissionListModelTable<CpuProfile, PermissionListModel<CpuProfile>> permissionListModelTable;

    @UiField
    FlowPanel tableContainer;

    SimplePanel permissionContainer;

    private boolean permissionPanelVisible = false;

    private final CpuProfilePermissionModelProvider cpuProfilePermissionModelProvider;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public CpuProfilesListModelTable(SearchableTableModelProvider<CpuProfile, CpuProfileListModel> modelProvider,
            CpuProfilePermissionModelProvider cpuProfilePermissionModelProvider,
            EventBus eventBus,
            CpuProfilesActionPanelPresenterWidget actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, actionPanel, clientStorage, false);
        this.cpuProfilePermissionModelProvider = cpuProfilePermissionModelProvider;
        ViewIdHandler.idHandler.generateAndSetIds(this);
        // Create cpu profile table
        tableContainer.add(getContainer());

        // Create permission panel
        permissionListModelTable =
                new PermissionWithInheritedPermissionListModelTable<>(cpuProfilePermissionModelProvider,
                        eventBus, null,
                        clientStorage);
        permissionListModelTable.initTable();
        permissionContainer = new SimplePanel();
        permissionContainer.add(permissionListModelTable);
    }

    @Override
    protected Widget getWrappedWidget() {
        return WidgetUiBinder.uiBinder.createAndBindUi(this);
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<CpuProfile> nameColumn =
                new AbstractTextColumn<CpuProfile>() {
                    @Override
                    public String getValue(CpuProfile object) {
                        return object.getName();
                    }
                };
        getTable().addColumn(nameColumn, constants.profileNameLabel(), "200px"); //$NON-NLS-1$
        nameColumn.makeSortable();

        AbstractTextColumn<CpuProfile> descriptionColumn =
                new AbstractTextColumn<CpuProfile>() {
                    @Override
                    public String getValue(CpuProfile object) {
                        return object.getDescription();
                    }
                };
        getTable().addColumn(descriptionColumn, constants.profileDescriptionLabel(), "200px"); //$NON-NLS-1$
        descriptionColumn.makeSortable();

        AbstractTextColumn<CpuProfile> qosColumn = new AbstractTextColumn<CpuProfile>() {
            @Override
            public String getValue(CpuProfile object) {
                String name = constants.unlimitedQos();
                if (object.getQosId() != null) {
                    CpuQos cpuQos = getModel().getQos(object.getQosId());
                    if (cpuQos != null) {
                        name = cpuQos.getName();
                    }
                }
                return name;
            }
        };
        getTable().addColumn(qosColumn, constants.cpuQosName(), "200px"); //$NON-NLS-1$
        qosColumn.makeSortable();

        // Add selection listener
        getModel().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updatePermissionPanel());

        getModel().getItemsChangedEvent().addListener((ev, sender, args) -> updatePermissionPanel());
    }

    private void updatePermissionPanel() {
        final CpuProfile cpuProfile = getModel().getSelectedItem();
        Scheduler.get().scheduleDeferred(() -> {
            if (permissionPanelVisible && cpuProfile == null) {
                tableContainer.clear();
                tableContainer.add(getContainer());
                permissionPanelVisible = false;
            } else if (!permissionPanelVisible && cpuProfile != null) {
                tableContainer.clear();
                tableContainer.add(getContainer());
                tableContainer.add(permissionContainer);
                permissionPanelVisible = true;
            }
        });
    }

    @Override
    public void addModelListeners() {
        final SimpleActionTable<CpuProfile, Permission> table = permissionListModelTable.getTable();
        table.getSelectionModel().addSelectionChangeHandler(event -> cpuProfilePermissionModelProvider.setSelectedItems(table.getSelectedItems()));
    }
}
