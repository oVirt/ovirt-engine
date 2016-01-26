package org.ovirt.engine.ui.common.widget.uicommon.template;

import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.AbstractBooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.NicActivateStatusColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import com.google.gwt.event.shared.EventBus;

public class TemplateInterfaceListModelTable extends AbstractModelBoundTableWidget<VmNetworkInterface, TemplateInterfaceListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public TemplateInterfaceListModelTable(
            SearchableTableModelProvider<VmNetworkInterface, TemplateInterfaceListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {
        NicActivateStatusColumn<VmNetworkInterface> statusColumn = new NicActivateStatusColumn<>();
        statusColumn.setContextMenuTitle(constants.vnicStatusNetworkVM());
        getTable().addColumn(statusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> nameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, constants.nameInterface(), "200px"); //$NON-NLS-1$

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

        getTable().addColumn(pluggedColumn, constants.plugged(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> networkNameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        getTable().addColumn(networkNameColumn, constants.networkNameInterface(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> profileNameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getVnicProfileName();
            }
        };
        getTable().addColumn(profileNameColumn, constants.profileNameInterface(), "150px"); //$NON-NLS-1$

        AbstractBooleanColumn<VmNetworkInterface> linkStateColumn =
                new AbstractBooleanColumn<VmNetworkInterface>(constants.linkedNetworkInterface(),
                        constants.unlinkedNetworkInterface()) {
                    @Override
                    protected Boolean getRawValue(VmNetworkInterface object) {
                        return object.isLinked();
                    }
                };

        getTable().addColumn(linkStateColumn, constants.linkStateNetworkInterface(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> typeColumn = new AbstractEnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        getTable().addColumn(typeColumn, constants.typeInterface(), "100px"); //$NON-NLS-1$

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
    }

}
