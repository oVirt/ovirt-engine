package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterCpuQosListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.event.shared.EventBus;

public class CpuQosListModelTable extends AbstractModelBoundTableWidget<CpuQos, DataCenterCpuQosListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public CpuQosListModelTable(SearchableDetailModelProvider<CpuQos,
            DataCenterListModel, DataCenterCpuQosListModel> modelProvider, EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
        initTable();
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<CpuQos> nameColumn = new AbstractTextColumn<CpuQos>() {
            @Override
            public String getValue(CpuQos object) {
                return object.getName() == null ? "" : object.getName(); //$NON-NLS-1$
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.cpuQosName(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<CpuQos> descColumn = new AbstractTextColumn<CpuQos>() {
            @Override
            public String getValue(CpuQos object) {
                return object.getDescription() == null ? "" : object.getDescription(); //$NON-NLS-1$
            }
        };
        descColumn.makeSortable();
        getTable().addColumn(descColumn, constants.cpuQosDescription(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<CpuQos> cpuLimitColumn = new AbstractTextColumn<CpuQos>() {
            @Override
            public String getValue(CpuQos object) {
                return object.getCpuLimit() == null ? constants.unlimitedQos()
                        : object.getCpuLimit().toString();
            }
        };
        cpuLimitColumn.makeSortable();
        getTable().addColumn(cpuLimitColumn, constants.cpuQosCpuLimit(), "105px"); //$NON-NLS-1$

        addButtonToActionGroup(
        getTable().addActionButton(new WebAdminButtonDefinition<CpuQos>(constants.newCpuQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        }));

        addButtonToActionGroup(
        getTable().addActionButton(new WebAdminButtonDefinition<CpuQos>(constants.editCpuQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        }));

        addButtonToActionGroup(
        getTable().addActionButton(new WebAdminButtonDefinition<CpuQos>(constants.removeQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        }));
    }

    @Override
    public void addModelListeners() {
        getTable().getSelectionModel().addSelectionChangeHandler(event -> {
            getModelProvider().setSelectedItems(getTable().getSelectedItems());
        });
    }
}
