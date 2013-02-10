package org.ovirt.engine.ui.common.widget.uicommon.template;

import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.BooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.NicActivateStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;

import com.google.gwt.event.shared.EventBus;

public class TemplateInterfaceListModelTable extends AbstractModelBoundTableWidget<VmNetworkInterface, TemplateInterfaceListModel> {

    public TemplateInterfaceListModelTable(
            SearchableTableModelProvider<VmNetworkInterface, TemplateInterfaceListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable(CommonApplicationConstants constants) {
        getTable().addColumn(new NicActivateStatusColumn<VmNetworkInterface>(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> nameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, constants.nameInterface());

        CheckboxColumn<VmNetworkInterface> pluggedColumn = new CheckboxColumn<VmNetworkInterface>() {
            @Override
            public Boolean getValue(VmNetworkInterface object) {
                return object.isPlugged();
            }

            @Override
            protected boolean canEdit(VmNetworkInterface object) {
                return false;
            }
        };

        getTable().addColumnWithHtmlHeader(pluggedColumn, constants.plugged(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> networkNameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        getTable().addColumn(networkNameColumn, constants.networkNameInterface());

        BooleanColumn<VmNetworkInterface> linkStateColumn =
                new BooleanColumn<VmNetworkInterface>(constants.linkedNetworkInteface(),
                        constants.unlinkedNetworkInteface()) {
                    @Override
                    protected Boolean getRawValue(VmNetworkInterface object) {
                        return object.isLinked();
                    }
                };

        getTable().addColumnWithHtmlHeader(linkStateColumn, constants.linkStateNetworkInteface(), "65px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> typeColumn = new EnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        getTable().addColumn(typeColumn, constants.typeInterface());

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(), constants.newInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(), constants.editInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(), constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}

