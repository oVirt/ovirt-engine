package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.SimpleStatusColumnComparator;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.MultiImageColumnHelper;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkClusterStatusColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;

public class SubTabNetworkClusterView extends AbstractSubTabTableView<NetworkView, PairQueryable<Cluster, NetworkCluster>, NetworkListModel, NetworkClusterListModel>
        implements SubTabNetworkClusterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkClusterView> {
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
    public SubTabNetworkClusterView(SearchableDetailModelProvider<PairQueryable<Cluster, NetworkCluster>, NetworkListModel, NetworkClusterListModel> modelProvider) {
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

        AbstractTextColumn<PairQueryable<Cluster, NetworkCluster>> nameColumn =
                new AbstractLinkColumn<PairQueryable<Cluster, NetworkCluster>>(
                        new FieldUpdater<PairQueryable<Cluster, NetworkCluster>, String>() {
            @Override
            public void update(int index, PairQueryable<Cluster, NetworkCluster> cluster, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), cluster.getFirst().getName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.clusterGeneralSubTabPlace, parameters);
            }
        }) {
            @Override
            public String getValue(PairQueryable<Cluster, NetworkCluster> object) {
                return object.getFirst().getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameCluster(), "400px"); //$NON-NLS-1$

        AbstractTextColumn<PairQueryable<Cluster, NetworkCluster>> versionColumn = new AbstractTextColumn<PairQueryable<Cluster, NetworkCluster>>() {
            @Override
            public String getValue(PairQueryable<Cluster, NetworkCluster> object) {
                return object.getFirst().getCompatibilityVersion().getValue();
            }
        };
        versionColumn.makeSortable();
        getTable().addColumn(versionColumn, constants.comptVersCluster(), "130px"); //$NON-NLS-1$

        AbstractCheckboxColumn<PairQueryable<Cluster, NetworkCluster>> attachedColumn =
                new AbstractCheckboxColumn<PairQueryable<Cluster, NetworkCluster>>(true) {
            @Override
            public Boolean getValue(PairQueryable<Cluster, NetworkCluster> object) {
                return object.getSecond() != null;
            }

            @Override
            protected boolean canEdit(PairQueryable<Cluster, NetworkCluster> object) {
                return false;
            }
        };
        attachedColumn.makeSortable();
        getTable().addColumn(attachedColumn, constants.attachedNetworkCluster(), "120px"); //$NON-NLS-1$

        NetworkClusterStatusColumn statusColumn = new NetworkClusterStatusColumn();
        statusColumn.makeSortable(new SimpleStatusColumnComparator<>(statusColumn));
        getTable().addColumn(statusColumn, constants.networkStatus(), "120px"); //$NON-NLS-1$

        AbstractCheckboxColumn<PairQueryable<Cluster, NetworkCluster>> netRequiredColumn =
                new AbstractCheckboxColumn<PairQueryable<Cluster, NetworkCluster>>(true) {
            @Override
            public Boolean getValue(PairQueryable<Cluster, NetworkCluster> object) {
                if (object.getSecond() != null) {
                    return object.getSecond().isRequired();
                }
                return false;
            }

            @Override
            protected boolean canEdit(PairQueryable<Cluster, NetworkCluster> object) {
                return false;
            }
        };
        netRequiredColumn.makeSortable();
        getTable().addColumn(netRequiredColumn, constants.requiredNetCluster(), "120px"); //$NON-NLS-1$

        AbstractSafeHtmlColumn<PairQueryable<Cluster, NetworkCluster>> netRoleColumn = createNetRoleColumn();
        netRoleColumn.makeSortable(new NetRoleColumnComparator());
        getTable().addColumn(netRoleColumn, constants.roleNetCluster(), "120px"); //$NON-NLS-1$

        AbstractTextColumn<PairQueryable<Cluster, NetworkCluster>> descriptionColumn = new AbstractTextColumn<PairQueryable<Cluster, NetworkCluster>>() {
            @Override
            public String getValue(PairQueryable<Cluster, NetworkCluster> object) {
                return object.getFirst().getDescription();
            }
        };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.descriptionCluster(), "400px"); //$NON-NLS-1$
    }

    private SafeHtml thisOrEmptyImage(boolean useFollowingImage, SafeHtml givenImage) {
        return useFollowingImage ? givenImage : emptyImage;
    }

    private AbstractSafeHtmlColumn<PairQueryable<Cluster, NetworkCluster>> createNetRoleColumn() {
        return new AbstractSafeHtmlColumn<PairQueryable<Cluster, NetworkCluster>>() {

            @Override
            public SafeHtml getValue(PairQueryable<Cluster, NetworkCluster> object) {
                List<SafeHtml> images = new LinkedList<>();

                NetworkCluster networkCluster = object.getSecond();
                if (networkCluster != null) {
                    images.add(thisOrEmptyImage(networkCluster.isManagement(), managementImage));
                    images.add(thisOrEmptyImage(networkCluster.isDisplay(), displayImage));
                    images.add(thisOrEmptyImage(networkCluster.isMigration(), migrationImage));
                    images.add(thisOrEmptyImage(networkCluster.isGluster(), glusterNwImage));
                    images.add(thisOrEmptyImage(networkCluster.isDefaultRoute(), defaultRouteImage));
                }
                return MultiImageColumnHelper.getValue(images);
            }

            @Override
            public SafeHtml getTooltip(PairQueryable<Cluster, NetworkCluster> object) {
                Map<SafeHtml, String> imagesToText = new LinkedHashMap<>();
                NetworkCluster networkCluster = object.getSecond();
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

                    if (networkCluster.isGluster()) {
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

    private static class NetRoleColumnComparator implements Comparator<PairQueryable<Cluster, NetworkCluster>> {

        private int calculateValue(NetworkCluster networkCluster) {
            int res = 0;
            if (networkCluster != null) {
                if (networkCluster.isManagement()) {
                    res += 16;
                }
                if (networkCluster.isDefaultRoute()) {
                    res += 8;
                }
                if (networkCluster.isDisplay()) {
                    res += 4;
                }
                if (networkCluster.isMigration()) {
                    res += 2;
                }
                if (networkCluster.isGluster()) {
                    res += 1;
                }
            }
            return res;
        }

        @Override
        public int compare(PairQueryable<Cluster, NetworkCluster> o1, PairQueryable<Cluster, NetworkCluster> o2) {
            return calculateValue(o1.getSecond()) - calculateValue(o2.getSecond());
        }
    }
}
