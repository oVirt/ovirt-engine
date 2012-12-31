package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;

public class SubTabNetworkTemplateView extends AbstractSubTabTableView<NetworkView, PairQueryable<VmNetworkInterface, VmTemplate>, NetworkListModel, NetworkTemplateListModel>
        implements SubTabNetworkTemplatePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkTemplateView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabNetworkTemplateView(SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VmTemplate>, NetworkListModel, NetworkTemplateListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VmTemplate>> nameColumn = new TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VmTemplate>>() {
            @Override
            public String getValue(PairQueryable<VmNetworkInterface, VmTemplate> object) {
                return object.getSecond().getname();
            }
        };
        getTable().addColumn(nameColumn, constants.nameTemplate());

        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VmTemplate>> statusColumn = new EnumColumn<PairQueryable<VmNetworkInterface, VmTemplate>, VmTemplateStatus>() {
            @Override
            protected VmTemplateStatus getRawValue(PairQueryable<VmNetworkInterface, VmTemplate> object) {
                return object.getSecond().getstatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusTemplate(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VmTemplate>> clusterColumn = new TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VmTemplate>>() {
            @Override
            public String getValue(PairQueryable<VmNetworkInterface, VmTemplate> object) {
                return object.getSecond().getvds_group_name();
            }
        };
        getTable().addColumn(clusterColumn, constants.clusterTemplate(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VmTemplate>> vnicNameColumn = new TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VmTemplate>>() {
            @Override
            public String getValue(PairQueryable<VmNetworkInterface, VmTemplate> object) {
                return object.getFirst().getName();
            }
        };
        getTable().addColumn(vnicNameColumn, constants.vnicNetworkTemplate());

        getTable().addActionButton(new WebAdminButtonDefinition<PairQueryable<VmNetworkInterface, VmTemplate>>(constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}

