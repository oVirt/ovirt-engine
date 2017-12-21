package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.MultiImageColumnHelper;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkStatusColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
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
    private final SafeHtml defaultRouteImage;

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabClusterNetworkView(SearchableDetailModelProvider<Network, ClusterListModel<Void>, ClusterNetworkListModel> modelProvider) {
        super(modelProvider);
        displayImage = safeHtmlFromTrustedString(resources.networkMonitor());
        migrationImage = safeHtmlFromTrustedString(resources.migrationNetwork());
        glusterNwImage = safeHtmlFromTrustedString(resources.glusterNetwork());
        emptyImage = SafeHtmlUtils.fromTrustedString(constants.empty());
        managementImage = safeHtmlFromTrustedString(resources.mgmtNetwork());
        defaultRouteImage = safeHtmlFromTrustedString(resources.defaultRouteNetwork());

        initTable();
        initWidget(getTableContainer());
    }

    private SafeHtml safeHtmlFromTrustedString(ImageResource resource) {
        return SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resource).getHTML());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initTable() {
        getTable().enableColumnResizing();

        NetworkStatusColumn statusIconColumn = new NetworkStatusColumn();
        statusIconColumn.setContextMenuTitle(constants.statusIconNetwork());
        getTable().addColumn(statusIconColumn, "", "20px"); //$NON-NLS-1$ //$NON-NLS-2$

        AbstractTextColumn<Network> nameColumn = new AbstractLinkColumn<Network>(
                new FieldUpdater<Network, String>() {
            @Override
            public void update(int index, Network network, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), network.getName());
                parameters.put(FragmentParams.DATACENTER.getName(),
                        getModelProvider().getMainModel().getSelectedItem().getStoragePoolName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.networkGeneralSubTabPlace, parameters);
            }
        }) {
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

        getTable().addColumn(createNetRoleColumn(), constants.roleNetwork(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<Network> descColumn = new AbstractTextColumn<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getDescription();
            }
        };
        descColumn.makeSortable();
        getTable().addColumn(descColumn, constants.descriptionNetwork(), "400px"); //$NON-NLS-1$
    }

    private SafeHtml thisOrEmptyImage(boolean useFollowingImage, SafeHtml givenImage) {
        return useFollowingImage ? givenImage : emptyImage;
    }

    private AbstractSafeHtmlColumn<Network> createNetRoleColumn() {
        return new AbstractSafeHtmlColumn<Network>() {
            @Override
            public SafeHtml getValue(Network network) {

                List<SafeHtml> images = new LinkedList<>();

                final NetworkCluster networkCluster = network.getCluster();
                if (networkCluster != null) {

                    images.add(thisOrEmptyImage(networkCluster.isManagement(), managementImage));
                    images.add(thisOrEmptyImage(networkCluster.isDisplay(), displayImage));
                    images.add(thisOrEmptyImage(networkCluster.isMigration(), migrationImage));
                    images.add(thisOrEmptyImage(network.getCluster().isGluster(), glusterNwImage));
                    images.add(thisOrEmptyImage(networkCluster.isDefaultRoute(), defaultRouteImage));
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

                    if (networkCluster.isDefaultRoute()) {
                        imagesToText.put(defaultRouteImage, constants.defaultRouteItemInfo());
                    }
                }
                return MultiImageColumnHelper.getTooltip(imagesToText);
            }
        };
    }

}
