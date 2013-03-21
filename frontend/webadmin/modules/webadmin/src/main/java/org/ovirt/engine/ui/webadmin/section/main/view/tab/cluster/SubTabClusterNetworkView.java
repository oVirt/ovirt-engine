package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlWithSafeHtmlTooltipColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SubTabClusterNetworkView extends AbstractSubTabTableView<VDSGroup, Network, ClusterListModel, ClusterNetworkListModel>
        implements SubTabClusterNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterNetworkView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final SafeHtml displayImage;
    private final SafeHtml migrationImage;
    private final SafeHtml emptyImage;

    @Inject
    public SubTabClusterNetworkView(SearchableDetailModelProvider<Network, ClusterListModel, ClusterNetworkListModel> modelProvider,
            ApplicationConstants constants,
            ApplicationTemplates templates,
            ApplicationResources resources) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        displayImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkMonitor()).getHTML());
        migrationImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.migrationNetwork()).getHTML());
        emptyImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkEmpty()).getHTML());
        initTable(constants, templates);
        initWidget(getTable());
    }

    void initTable(final ApplicationConstants constants, final ApplicationTemplates templates) {
        getTable().addColumn(new NetworkStatusColumn(), "", "20px"); //$NON-NLS-1$ //$NON-NLS-2$

        TextColumnWithTooltip<Network> nameColumn = new TextColumnWithTooltip<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, constants.nameNetwork());

        TextColumnWithTooltip<Network> statusColumn = new EnumColumn<Network, NetworkStatus>() {
            @Override
            public NetworkStatus getRawValue(Network object) {
                return object.getCluster().getStatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusNetwork());

        SafeHtmlWithSafeHtmlTooltipColumn<Network> roleColumn =
                new SafeHtmlWithSafeHtmlTooltipColumn<Network>() {
                    @Override
                    public SafeHtml getValue(Network network) {

                        String images = ""; //$NON-NLS-1$

                        if (network.getCluster() != null && network.getCluster().isDisplay()) {

                            images = images.concat(displayImage.asString());
                        } else {
                            images = images.concat(emptyImage.asString());
                        }

                        if (network.getCluster() != null && network.getCluster().isMigration()) {
                            images = images.concat(migrationImage.asString());
                        } else {
                            images = images.concat(emptyImage.asString());
                        }

                        return templates.image(SafeHtmlUtils.fromTrustedString(images));
                    }

                    @Override
                    public SafeHtml getTooltip(Network network) {
                        String tooltip = ""; //$NON-NLS-1$
                        if (network.getCluster() != null && network.getCluster().isDisplay()) {
                            tooltip =
                                    tooltip.concat(templates.imageTextSetupNetwork(displayImage,
                                            constants.displayItemInfo()).asString());
                        }

                        if (network.getCluster() != null && network.getCluster().isMigration()) {
                            if (!"".equals(tooltip)) //$NON-NLS-1$
                            {
                                tooltip = tooltip.concat("<BR>"); //$NON-NLS-1$
                            }
                            tooltip =
                                    tooltip.concat(templates.imageTextSetupNetwork(migrationImage,
                                            constants.migrationItemInfo())
                                            .asString());

                        }

                        return SafeHtmlUtils.fromTrustedString(tooltip);
                    }
                };

        getTable().addColumn(roleColumn, constants.roleNetwork(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<Network> descColumn = new TextColumnWithTooltip<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getDescription();
            }
        };
        getTable().addColumn(descColumn, constants.descriptionNetwork());

        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.addNetworkNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewNetworkCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.assignDetatchNetworksNework()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.setAsDisplayNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSetAsDisplayCommand();
            }
        });
    }

}
