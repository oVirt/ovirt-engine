package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.NetworkCluster;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlWithSafeHtmlTooltipColumn;
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
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkClusterStatusColumn;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;


public class SubTabNetworkClusterView extends AbstractSubTabTableView<NetworkView, PairQueryable<VDSGroup, NetworkCluster>, NetworkListModel, NetworkClusterListModel>
        implements SubTabNetworkClusterPresenter.ViewDef {

    private final ApplicationConstants constants;
    private final ApplicationTemplates templates;

    private final SafeHtml dispalyImage;

    @Inject
    public SubTabNetworkClusterView(SearchableDetailModelProvider<PairQueryable<VDSGroup, NetworkCluster>, NetworkListModel, NetworkClusterListModel> modelProvider, ApplicationConstants constants, ApplicationTemplates templates, ApplicationResources resources) {
        super(modelProvider);
        this.constants = constants;
        this.templates = templates;
        dispalyImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkMonitor()).getHTML());
        initTable();
        initWidget(getTable());
    }

    void initTable() {

        TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>> nameColumn = new TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>>() {
            @Override
            public String getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                return object.getFirst().getname();
            }
        };
        getTable().addColumn(nameColumn, constants.nameCluster());

        TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>> versionColumn = new TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>>() {
            @Override
            public String getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                return object.getFirst().getcompatibility_version().getValue();
            }
        };
        getTable().addColumn(versionColumn, constants.comptVersCluster(), "130px"); //$NON-NLS-1$

        CheckboxColumn<PairQueryable<VDSGroup, NetworkCluster>> attachedColumn =
                new CheckboxColumn<PairQueryable<VDSGroup, NetworkCluster>>(true) {
            @Override
            public Boolean getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                return object.getSecond() != null;
            }

            @Override
            protected boolean canEdit(PairQueryable<VDSGroup, NetworkCluster> object) {
                return false;
            }
        };

        getTable().addColumn(attachedColumn, constants.attachedNetworkCluster(), "120px"); //$NON-NLS-1$

        getTable().addColumn(new NetworkClusterStatusColumn(), constants.networkStatus(), "120px"); //$NON-NLS-1$

        CheckboxColumn<PairQueryable<VDSGroup, NetworkCluster>> netRequiredColumn =
                new CheckboxColumn<PairQueryable<VDSGroup, NetworkCluster>>(true) {
            @Override
            public Boolean getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                if (object.getSecond() != null)
                {
                    return object.getSecond().isRequired();
                }
                return false;
            }

            @Override
            protected boolean canEdit(PairQueryable<VDSGroup, NetworkCluster> object) {
                return false;
            }
        };
        getTable().addColumn(netRequiredColumn, constants.requiredNetCluster(), "120px"); //$NON-NLS-1$

        SafeHtmlWithSafeHtmlTooltipColumn<PairQueryable<VDSGroup, NetworkCluster>> netRoleColumn = new SafeHtmlWithSafeHtmlTooltipColumn<PairQueryable<VDSGroup, NetworkCluster>>(){

            @Override
            public SafeHtml getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                if (object.getSecond() != null){
                    if (object.getSecond().getis_display()){
                        return templates.image(dispalyImage);
                    }
                    return null;
                }

                return templates.image(SafeHtmlUtils.fromTrustedString("")); //$NON-NLS-1$
            }

            @Override
            public SafeHtml getTooltip(PairQueryable<VDSGroup, NetworkCluster> object) {
                if (object.getSecond() != null && object.getSecond().getis_display()){
                    return (templates.imageTextSetupNetwork(dispalyImage, constants.displayItemInfo()));
                }

                return SafeHtmlUtils.fromTrustedString(""); //$NON-NLS-1$
            }
        };

        getTable().addColumn(netRoleColumn, constants.roleNetCluster(), "120px"); //$NON-NLS-1$

        TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>> dsecriptionColumn = new TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>>() {
            @Override
            public String getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                return object.getFirst().getdescription();
            }
        };
        getTable().addColumn(dsecriptionColumn, constants.descriptionCluster());

        getTable().addActionButton(new WebAdminButtonDefinition<PairQueryable<VDSGroup, NetworkCluster>>(constants.assignUnassignNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageCommand();
            }
        });
    }

}
