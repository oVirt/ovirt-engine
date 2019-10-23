package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling;

import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.ClusterPolicyClusterModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.ClusterPolicyModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;

public class ClusterPolicyView extends Composite {

    interface ViewUiBinder extends UiBinder<Container, ClusterPolicyView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    FlowPanel clusterPolicyPanel;

    @UiField
    FlowPanel clusterPanel;

    private SimpleActionTable<Void, ClusterPolicy> clusterPolicyTable;
    private SimpleActionTable<ClusterPolicy, Cluster> clusterTable;

    private final ClusterPolicyModelProvider clusterPolicyModelProvider;
    private final ClusterPolicyClusterModelProvider clusterPolicyClusterModelProvider;

    private final EventBus eventBus;
    private final ClientStorage clientStorage;

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ClusterPolicyView(EventBus eventBus, ClientStorage clientStorage,
            ClusterPolicyModelProvider clusterPolicyModelProvider,
            ClusterPolicyClusterModelProvider clusterPolicyClusterModelProvider,
            ClusterPolicyActionPanelPresenterWidget actionPanel) {
        this.eventBus = eventBus;
        this.clientStorage = clientStorage;
        this.clusterPolicyModelProvider = clusterPolicyModelProvider;
        this.clusterPolicyClusterModelProvider = clusterPolicyClusterModelProvider;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initClusterPolicyTable(actionPanel);
        initClustersTable();

        setSubTabVisibility(false);
    }

    public void setSubTabVisibility(boolean visible) {
        splitLayoutPanel.clear();
        if (visible) {
            splitLayoutPanel.addSouth(clusterPanel, 150);
        }
        splitLayoutPanel.add(clusterPolicyPanel);
    }

    private void initClusterPolicyTable(ClusterPolicyActionPanelPresenterWidget policyActionPanel) {
        clusterPolicyTable = new SimpleActionTable<>(clusterPolicyModelProvider,
                getTableResources(), eventBus, clientStorage);

        clusterPolicyTable.addColumn(new AbstractImageResourceColumn<ClusterPolicy>() {
            @Override
            public ImageResource getValue(ClusterPolicy object) {
                if (object.isLocked()) {
                    return resources.lockImage();
                }
                return null;
            }
        }, constants.empty(), "20px"); //$NON-NLS-1$

        AbstractTextColumn<ClusterPolicy> nameColumn = new AbstractTextColumn<ClusterPolicy>() {
            @Override
            public String getValue(ClusterPolicy object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable((n1, n2) -> n1.getName().compareTo(n2.getName()));
        clusterPolicyTable.addColumn(nameColumn, constants.clusterPolicyNameLabel(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<ClusterPolicy> descColumn = new AbstractTextColumn<ClusterPolicy>() {
            @Override
            public String getValue(ClusterPolicy object) {
                return object.getDescription();
            }
        };
        clusterPolicyTable.addColumn(descColumn, constants.clusterPolicyDescriptionLabel(), "300px"); //$NON-NLS-1$

        clusterPolicyTable.getSelectionModel().addSelectionChangeHandler(event -> {
            clusterPolicyModelProvider.setSelectedItems(clusterPolicyTable.getSelectionModel().getSelectedObjects());
            if (clusterPolicyTable.getSelectionModel().getSelectedObjects().size() > 0) {
                setSubTabVisibility(true);
            } else {
                setSubTabVisibility(false);
            }
        });

        clusterPolicyPanel.add(policyActionPanel);
        clusterPolicyPanel.add(clusterPolicyTable);
    }

    private void initClustersTable() {
        clusterTable = new SimpleActionTable<>(clusterPolicyClusterModelProvider,
                getTableResources(), eventBus, clientStorage);

        AbstractTextColumn<Cluster> clusterColumn = new AbstractTextColumn<Cluster>() {
            @Override
            public String getValue(Cluster object) {
                return object.getName();
            }
        };
        clusterTable.addColumn(clusterColumn, constants.clusterPolicyAttachedCluster());

        clusterPanel.add(clusterTable);
    }

    protected Resources getTableResources() {
        return (Resources) GWT.create(MainTableResources.class);
    }

}
