package org.ovirt.engine.ui.webadmin.section.main.view.tab.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractUptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.core.client.GWT;

public class SubTabPoolVmView extends AbstractSubTabTableView<VmPool, VM, PoolListModel, PoolVmListModel>
        implements SubTabPoolVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabPoolVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabPoolVmView(SearchableDetailModelProvider<VM, PoolListModel, PoolVmListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable(ApplicationConstants constants) {
        getTable().showPagingButtons();

        getTable().enableColumnResizing();

        getTable().addColumn(new VmStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VM> nameColumn = new AbstractTextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, constants.nameVm(), "200px"); //$NON-NLS-1$

        getTable().addColumn(new VmTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VM> hostColumn = new AbstractTextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getRunOnVdsName();
            }
        };
        getTable().addColumn(hostColumn, constants.hostVm(), "200px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VM> ipColumn = new AbstractTextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getVmIp();
            }
        };
        getTable().addColumn(ipColumn, constants.ipVm(), "200px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VM> fqdnColumn = new AbstractTextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getVmFQDN();
            }
        };
        getTable().addColumn(fqdnColumn, constants.fqdn(), "200px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VM> statusColumn = new AbstractEnumColumn<VM, VMStatus>() {
            @Override
            protected VMStatus getRawValue(VM object) {
                return object.getStatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusVm(), "200px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VM> uptimeColumn = new AbstractUptimeColumn<VM>() {
            @Override
            protected Double getRawValue(VM object) {
                return object.getRoundedElapsedTime();
            }
        };
        getTable().addColumn(uptimeColumn, constants.uptimeVm(), "200px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VM> consoleConnectedUserColumn = new AbstractTextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getConsoleCurentUserName();
            }
        };
        getTable().addColumn(consoleConnectedUserColumn, constants.consoleConnectedUserVm(), "200px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VM> loggedInUserColumn = new AbstractTextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getGuestCurentUserName();
            }
        };
        getTable().addColumn(loggedInUserColumn, constants.loggedInUserVm(), "200px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<VM>(constants.detachVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDetachCommand();
            }
        });
    }

}
