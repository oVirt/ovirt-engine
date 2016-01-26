package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractBooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.NicActivateStatusColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class VmInterfaceListModelTable extends AbstractModelBoundTableWidget<VmNetworkInterface, VmInterfaceListModel> {

    interface WidgetUiBinder extends UiBinder<Widget, VmInterfaceListModelTable> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    SimplePanel interfaceTableContainer;

    @UiField
    SimplePanel interfaceInfoContainer;

    private final VmInterfaceInfoPanel vmInterfaceInfoPanel;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public VmInterfaceListModelTable(
            SearchableTableModelProvider<VmNetworkInterface, VmInterfaceListModel> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);

        // Create Interfaces table
        SimpleActionTable<VmNetworkInterface> table = getTable();
        interfaceTableContainer.add(table);

        // Create Interface information tab panel
        vmInterfaceInfoPanel = new VmInterfaceInfoPanel(getModel());
        interfaceInfoContainer.add(vmInterfaceInfoPanel);
    }

    @Override
    protected Widget getWrappedWidget() {
        return WidgetUiBinder.uiBinder.createAndBindUi(this);
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();

        NicActivateStatusColumn<VmNetworkInterface> statusColumn = new NicActivateStatusColumn<>();
        statusColumn.setContextMenuTitle(constants.vnicStatusNetworkVM());
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> nameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameInterface(), "150px"); //$NON-NLS-1$

        AbstractCheckboxColumn<VmNetworkInterface> pluggedColumn = new AbstractCheckboxColumn<VmNetworkInterface>() {
            @Override
            public Boolean getValue(VmNetworkInterface object) {
                return object.isPlugged();
            }

            @Override
            protected boolean canEdit(VmNetworkInterface object) {
                return false;
            }
        };
        pluggedColumn.makeSortable();
        getTable().addColumn(pluggedColumn, constants.plugged(), "60px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> networkNameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        networkNameColumn.makeSortable();
        getTable().addColumn(networkNameColumn, constants.networkNameInterface(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> profileNameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getVnicProfileName();
            }
        };
        profileNameColumn.makeSortable();
        getTable().addColumn(profileNameColumn, constants.profileNameInterface(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> qosName = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getQosName();
            }
        };
        qosName.makeSortable();
        getTable().addColumn(qosName, constants.vmNetworkQosName(), "150px"); //$NON-NLS-1$

        AbstractBooleanColumn<VmNetworkInterface> linkStateColumn =
                new AbstractBooleanColumn<VmNetworkInterface>(constants.linkedNetworkInterface(),
                        constants.unlinkedNetworkInterface()) {
                    @Override
                    protected Boolean getRawValue(VmNetworkInterface object) {
                        return object.isLinked();
                    }
                };
        linkStateColumn.makeSortable();
        getTable().addColumn(linkStateColumn, constants.linkStateNetworkInterface(), "65px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> typeColumn = new AbstractEnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        typeColumn.makeSortable();
        getTable().addColumn(typeColumn, constants.typeInterface(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> macColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        macColumn.makeSortable();
        getTable().addColumn(macColumn, constants.macInterface(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> speedColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().toString();
                } else {
                    return null;
                }
            }
        };
        speedColumn.makeSortable();
        getTable().addColumn(speedColumn,
                templates.sub(constants.speedInterface(), constants.mbps()).asString(), "150px"); //$NON-NLS-1$

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(),
                constants.newInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(),
                constants.editInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(),
                constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        // Add selection listener
        getModel().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateInfoPanel();
            }
        });

        getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateInfoPanel();
            }
        });
    }

    private void updateInfoPanel() {
        final VmNetworkInterface vmNetworkInterface = getModel().getSelectedItem();
        if (vmNetworkInterface != null && !getTable().getSelectionModel().isSelected(vmNetworkInterface)) {
            // first let list of items get updated, only then select item
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    getTable().getSelectionModel().setSelected(vmNetworkInterface, true);
                }
            });
        }
        vmInterfaceInfoPanel.updatePanel(vmNetworkInterface);
    }

}
