package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkClusterStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.SafeHtmlWithSafeHtmlTooltipColumn;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;


public class SubTabNetworkClusterView extends AbstractSubTabTableView<NetworkView, Pair<network_cluster, VDSGroup>, NetworkListModel, NetworkClusterListModel>
        implements SubTabNetworkClusterPresenter.ViewDef {

    private final ApplicationConstants constants;
    private final ApplicationTemplates templates;

    private final SafeHtml dispalyImage;

    @Inject
    public SubTabNetworkClusterView(SearchableDetailModelProvider<Pair<network_cluster, VDSGroup>, NetworkListModel, NetworkClusterListModel> modelProvider, ApplicationConstants constants, ApplicationTemplates templates, ApplicationResources resources) {
        super(modelProvider);
        this.constants = constants;
        this.templates = templates;
        dispalyImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkMonitor()).getHTML());
        initTable();
        initWidget(getTable());
    }

    void initTable() {

        TextColumnWithTooltip<Pair<network_cluster, VDSGroup>> nameColumn = new TextColumnWithTooltip<Pair<network_cluster, VDSGroup>>() {
            @Override
            public String getValue(Pair<network_cluster, VDSGroup> object) {
                return object.getSecond().getname();
            }
        };
        getTable().addColumn(nameColumn, constants.nameCluster());

        TextColumnWithTooltip<Pair<network_cluster, VDSGroup>> versionColumn = new TextColumnWithTooltip<Pair<network_cluster, VDSGroup>>() {
            @Override
            public String getValue(Pair<network_cluster, VDSGroup> object) {
                return object.getSecond().getcompatibility_version().getValue();
            }
        };
        getTable().addColumn(versionColumn, constants.comptVersCluster(), "130px"); //$NON-NLS-1$

        CheckboxColumn<Pair<network_cluster,VDSGroup>> attachedColumn = new CheckboxColumn<Pair<network_cluster,VDSGroup>>() {
            @Override
            public Boolean getValue(Pair<network_cluster, VDSGroup> object) {
                return object.getSecond() != null;
            }

            @Override
            protected boolean canEdit(Pair<network_cluster, VDSGroup> object) {
                return false;
            }
        };

        getTable().addColumn(attachedColumn, constants.attachedNetworkCluster(), "120px"); //$NON-NLS-1$

        getTable().addColumn(new NetworkClusterStatusColumn(), constants.networkStatus(), "120px"); //$NON-NLS-1$

        CheckboxColumn<Pair<network_cluster, VDSGroup>> netRequiredColumn = new CheckboxColumn<Pair<network_cluster, VDSGroup>>() {
            @Override
            public Boolean getValue(Pair<network_cluster, VDSGroup> object) {
                return object.getFirst().isRequired();
            }

            @Override
            protected boolean canEdit(Pair<network_cluster, VDSGroup> object) {
                return false;
            }
        };
        getTable().addColumn(netRequiredColumn, constants.requiredNetCluster(), "120px"); //$NON-NLS-1$

        SafeHtmlWithSafeHtmlTooltipColumn<Pair<network_cluster, VDSGroup>> netRoleColumn = new SafeHtmlWithSafeHtmlTooltipColumn<Pair<network_cluster, VDSGroup>>(){

            @Override
            public SafeHtml getValue(Pair<network_cluster, VDSGroup> object) {
                if (object.getFirst().getis_display()){
                    return templates.image(dispalyImage);
                }

                return templates.image(SafeHtmlUtils.fromTrustedString("")); //$NON-NLS-1$
            }

            @Override
            public SafeHtml getTooltip(Pair<network_cluster, VDSGroup> object) {
                if (object.getFirst().getis_display()){
                    return (templates.imageTextSetupNetwork(dispalyImage, constants.displayItemInfo()));
                }

                return SafeHtmlUtils.fromTrustedString(""); //$NON-NLS-1$
            }
        };

        getTable().addColumn(netRoleColumn, constants.roleNetCluster(), "120px"); //$NON-NLS-1$

        TextColumnWithTooltip<Pair<network_cluster, VDSGroup>> dsecriptionColumn = new TextColumnWithTooltip<Pair<network_cluster, VDSGroup>>() {
            @Override
            public String getValue(Pair<network_cluster, VDSGroup> object) {
                return object.getSecond().getdescription();
            }
        };
        getTable().addColumn(dsecriptionColumn, constants.descriptionCluster());

        getTable().addActionButton(new WebAdminButtonDefinition<Pair<network_cluster, VDSGroup>>(constants.assignUnassignNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageCommand();
            }
        });
    }

}
