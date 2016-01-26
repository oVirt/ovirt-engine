package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.MultiImageColumnHelper;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkStatusColumn;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;

public class SubTabClusterNetworkView extends AbstractSubTabTableView<Cluster, Network, ClusterListModel<Void>, ClusterNetworkListModel>
        implements SubTabClusterNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterNetworkView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final SafeHtml displayImage;
    private final SafeHtml migrationImage;
    private final SafeHtml glusterNwImage;
    private final SafeHtml emptyImage;
    private final SafeHtml managementImage;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabClusterNetworkView(SearchableDetailModelProvider<Network, ClusterListModel<Void>, ClusterNetworkListModel> modelProvider) {
        super(modelProvider);
        displayImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkMonitor()).getHTML());
        migrationImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.migrationNetwork()).getHTML());
        glusterNwImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.glusterNetwork()).getHTML());
        emptyImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkEmpty()).getHTML());
        managementImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.mgmtNetwork()).getHTML());
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        NetworkStatusColumn statusIconColumn = new NetworkStatusColumn();
        statusIconColumn.setContextMenuTitle(constants.statusIconNetwork());
        getTable().addColumn(statusIconColumn, "", "20px"); //$NON-NLS-1$ //$NON-NLS-2$

        AbstractTextColumn<Network> nameColumn = new AbstractTextColumn<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameNetwork(), "400px"); //$NON-NLS-1$

        AbstractTextColumn<Network> statusColumn = new AbstractEnumColumn<Network, NetworkStatus>() {
            @Override
            public NetworkStatus getRawValue(Network object) {
                return object.getCluster().getStatus();
            }
        };
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusNetwork(), "100px"); //$NON-NLS-1$

        AbstractSafeHtmlColumn<Network> roleColumn =
                new AbstractSafeHtmlColumn<Network>() {
                    @Override
                    public SafeHtml getValue(Network network) {

                        List<SafeHtml> images = new LinkedList<>();

                        final NetworkCluster networkCluster = network.getCluster();
                        if (networkCluster != null) {

                            if (networkCluster.isManagement()) {
                                images.add(managementImage);
                            } else {
                                images.add(emptyImage);
                            }

                            if (networkCluster.isDisplay()) {
                                images.add(displayImage);
                            } else {
                                images.add(emptyImage);
                            }

                            if (networkCluster.isMigration()) {
                                images.add(migrationImage);
                            } else {
                                images.add(emptyImage);
                            }

                            if (network.getCluster().isGluster()) {
                                images.add(glusterNwImage);
                            } else {
                                images.add(emptyImage);
                            }
                        }

                        return MultiImageColumnHelper.getValue(images);
                    }

                    @Override
                    public SafeHtml getTooltip(Network network) {
                        Map<SafeHtml, String> imagesToText = new LinkedHashMap<>();
                        final NetworkCluster networkCluster = network.getCluster();
                        if (networkCluster != null) {
                            if (networkCluster.isManagement()) {
                                imagesToText.put(managementImage, constants.managementItemInfo());
                            }
                            if (networkCluster.isDisplay()) {
                                imagesToText.put(displayImage, constants.displayItemInfo());
                            }

                            if (networkCluster.isMigration()) {
                                imagesToText.put(migrationImage, constants.migrationItemInfo());
                            }

                            if (network.getCluster().isGluster()) {
                                imagesToText.put(glusterNwImage, constants.glusterNwItemInfo());
                            }
                        }
                        return MultiImageColumnHelper.getTooltip(imagesToText);
                    }
                };

        getTable().addColumn(roleColumn, constants.roleNetwork(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<Network> descColumn = new AbstractTextColumn<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getDescription();
            }
        };
        descColumn.makeSortable();
        getTable().addColumn(descColumn, constants.descriptionNetwork(), "400px"); //$NON-NLS-1$

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
