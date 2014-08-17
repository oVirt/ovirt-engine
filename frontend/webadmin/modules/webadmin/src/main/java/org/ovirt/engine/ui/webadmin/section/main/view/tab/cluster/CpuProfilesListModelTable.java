package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.common.widget.uicommon.permissions.PermissionWithInheritedPermissionListModelTable;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.uicommon.model.CpuProfilePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class CpuProfilesListModelTable extends AbstractModelBoundTableWidget<CpuProfile, CpuProfileListModel> {

    interface WidgetUiBinder extends UiBinder<Widget, CpuProfilesListModelTable> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private final PermissionWithInheritedPermissionListModelTable<PermissionListModel> permissionListModelTable;

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    SimplePanel tableContainer;

    SimplePanel permissionContainer;

    private boolean permissionPanelVisible = false;

    public CpuProfilesListModelTable(SearchableTableModelProvider<CpuProfile, CpuProfileListModel> modelProvider,
            CpuProfilePermissionModelProvider cpuProfilePermissionModelProvider,
            EventBus eventBus,
            ClientStorage clientStorage,
            CommonApplicationConstants constants) {
        super(modelProvider, eventBus, clientStorage, false);
        // Create cpu profile table
        tableContainer.add(getTable());

        // Create permission panel
        permissionListModelTable =
                new PermissionWithInheritedPermissionListModelTable<PermissionListModel>(cpuProfilePermissionModelProvider,
                        eventBus,
                        clientStorage);
        permissionListModelTable.initTable(constants);
        permissionContainer = new SimplePanel();
        permissionContainer.add(permissionListModelTable);
    }

    @Override
    protected Widget getWrappedWidget() {
        return WidgetUiBinder.uiBinder.createAndBindUi(this);
    }

    @Override
    public void initTable(final CommonApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<CpuProfile> nameColumn =
                new TextColumnWithTooltip<CpuProfile>() {
                    @Override
                    public String getValue(CpuProfile object) {
                        return object.getName();
                    }
                };
        getTable().addColumn(nameColumn, constants.profileNameLabel(), "200px"); //$NON-NLS-1$
        nameColumn.makeSortable();

        TextColumnWithTooltip<CpuProfile> descriptionColumn =
                new TextColumnWithTooltip<CpuProfile>() {
                    @Override
                    public String getValue(CpuProfile object) {
                        return object.getDescription();
                    }
                };
        getTable().addColumn(descriptionColumn, constants.profileDescriptionLabel(), "200px"); //$NON-NLS-1$
        descriptionColumn.makeSortable();

        TextColumnWithTooltip<CpuProfile> qosColumn = new TextColumnWithTooltip<CpuProfile>() {
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

        getTable().addActionButton(new WebAdminButtonDefinition<CpuProfile>(constants.newProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<CpuProfile>(constants.editProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<CpuProfile>(constants.removeProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        // Add selection listener
        getModel().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updatePermissionPanel();
            }
        });

        getModel().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updatePermissionPanel();
            }
        });
    }

    private void updatePermissionPanel() {
        final CpuProfile cpuProfile = (CpuProfile) getModel().getSelectedItem();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                if (permissionPanelVisible && cpuProfile == null) {
                    splitLayoutPanel.clear();
                    splitLayoutPanel.add(tableContainer);
                    permissionPanelVisible = false;
                } else if (!permissionPanelVisible && cpuProfile != null) {
                    splitLayoutPanel.clear();
                    splitLayoutPanel.addEast(permissionContainer, 600);
                    splitLayoutPanel.add(tableContainer);
                    permissionPanelVisible = true;
                }
            }
        });
    }
}
