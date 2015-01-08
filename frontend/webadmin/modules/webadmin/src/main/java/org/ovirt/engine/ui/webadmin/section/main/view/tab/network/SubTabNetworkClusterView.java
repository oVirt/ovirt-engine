package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlWithSafeHtmlTooltipColumn;
import org.ovirt.engine.ui.common.widget.table.column.SimpleStatusColumnComparator;
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
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkRoleColumnHelper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;


public class SubTabNetworkClusterView extends AbstractSubTabTableView<NetworkView, PairQueryable<VDSGroup, NetworkCluster>, NetworkListModel, NetworkClusterListModel>
        implements SubTabNetworkClusterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkClusterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ApplicationConstants constants;
    private final ApplicationTemplates templates;

    private final SafeHtml displayImage;
    private final SafeHtml migrationImage;
    private final SafeHtml emptyImage;

    @Inject
    public SubTabNetworkClusterView(SearchableDetailModelProvider<PairQueryable<VDSGroup, NetworkCluster>, NetworkListModel, NetworkClusterListModel> modelProvider, ApplicationConstants constants, ApplicationTemplates templates, ApplicationResources resources) {
        super(modelProvider);
        this.constants = constants;
        this.templates = templates;
        displayImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkMonitor()).getHTML());
        migrationImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.migrationNetwork()).getHTML());
        emptyImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkEmpty()).getHTML());
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>> nameColumn = new TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>>() {
            @Override
            public String getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                return object.getFirst().getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameCluster(), "400px"); //$NON-NLS-1$

        TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>> versionColumn = new TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>>() {
            @Override
            public String getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                return object.getFirst().getCompatibilityVersion().getValue();
            }
        };
        versionColumn.makeSortable();
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
        attachedColumn.makeSortable();
        getTable().addColumn(attachedColumn, constants.attachedNetworkCluster(), "120px"); //$NON-NLS-1$

        NetworkClusterStatusColumn statusColumn = new NetworkClusterStatusColumn();
        statusColumn.makeSortable(new SimpleStatusColumnComparator<PairQueryable<VDSGroup, NetworkCluster>>(statusColumn));
        getTable().addColumn(statusColumn, constants.networkStatus(), "120px"); //$NON-NLS-1$

        CheckboxColumn<PairQueryable<VDSGroup, NetworkCluster>> netRequiredColumn =
                new CheckboxColumn<PairQueryable<VDSGroup, NetworkCluster>>(true) {
            @Override
            public Boolean getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                if (object.getSecond() != null) {
                    return object.getSecond().isRequired();
                }
                return false;
            }

            @Override
            protected boolean canEdit(PairQueryable<VDSGroup, NetworkCluster> object) {
                return false;
            }
        };
        netRequiredColumn.makeSortable();
        getTable().addColumn(netRequiredColumn, constants.requiredNetCluster(), "120px"); //$NON-NLS-1$

        SafeHtmlWithSafeHtmlTooltipColumn<PairQueryable<VDSGroup, NetworkCluster>> netRoleColumn =
                new SafeHtmlWithSafeHtmlTooltipColumn<PairQueryable<VDSGroup, NetworkCluster>>() {

                    @Override
                    public SafeHtml getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                        List<SafeHtml> images = new LinkedList<SafeHtml>();

                        if (object.getSecond() != null) {
                            if (object.getSecond().isDisplay()) {
                                images.add(displayImage);
                            } else {
                                images.add(emptyImage);

                            }
                            if (object.getSecond().isMigration()) {
                                images.add(migrationImage);
                            } else {
                                images.add(emptyImage);

                            }
                        }
                        return NetworkRoleColumnHelper.getValue(images);
                    }

                    @Override
                    public SafeHtml getTooltip(PairQueryable<VDSGroup, NetworkCluster> object) {
                        Map<SafeHtml, String> imagesToText = new LinkedHashMap<SafeHtml, String>();
                        if (object.getSecond() != null) {
                            if (object.getSecond().isDisplay()) {
                                imagesToText.put(displayImage, constants.displayItemInfo());
                            }

                            if (object.getSecond().isMigration()) {
                                imagesToText.put(migrationImage, constants.migrationItemInfo());
                            }
                        }

                        return NetworkRoleColumnHelper.getTooltip(imagesToText);
                    }
                };
        netRoleColumn.makeSortable(new Comparator<PairQueryable<VDSGroup, NetworkCluster>>() {

            private int calculateValue(NetworkCluster networkCluster) {
                int res = 0;
                if (networkCluster != null) {
                    if (networkCluster.isDisplay()) {
                        res += 2;
                    }
                    if (networkCluster.isMigration()) {
                        res += 1;
                    }
                }
                return res;
            }

            @Override
            public int compare(PairQueryable<VDSGroup, NetworkCluster> o1, PairQueryable<VDSGroup, NetworkCluster> o2) {
                return calculateValue(o1.getSecond()) - calculateValue(o2.getSecond());
            }
        });
        getTable().addColumn(netRoleColumn, constants.roleNetCluster(), "120px"); //$NON-NLS-1$

        TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>> descriptionColumn = new TextColumnWithTooltip<PairQueryable<VDSGroup, NetworkCluster>>() {
            @Override
            public String getValue(PairQueryable<VDSGroup, NetworkCluster> object) {
                return object.getFirst().getDescription();
            }
        };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.descriptionCluster(), "400px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<PairQueryable<VDSGroup, NetworkCluster>>(constants.assignUnassignNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageCommand();
            }
        });
    }

}
