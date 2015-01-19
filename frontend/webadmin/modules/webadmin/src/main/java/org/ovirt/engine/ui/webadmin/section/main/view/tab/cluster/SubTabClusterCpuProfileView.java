package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterCpuProfilePresenter;
import org.ovirt.engine.ui.webadmin.uicommon.model.CpuProfilePermissionModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabClusterCpuProfileView extends AbstractSubTabTableWidgetView<VDSGroup, CpuProfile, ClusterListModel, CpuProfileListModel>
        implements SubTabClusterCpuProfilePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterCpuProfileView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabClusterCpuProfileView(SearchableDetailModelProvider<CpuProfile, ClusterListModel, CpuProfileListModel> modelProvider,
            CpuProfilePermissionModelProvider permissionModelProvider,
            EventBus eventBus,
            ClientStorage clientStorage,
            CommonApplicationConstants constants,
            CommonApplicationMessages messages,
            CommonApplicationTemplates templates) {
        super(new CpuProfilesListModelTable(modelProvider,
                permissionModelProvider,
                eventBus,
                clientStorage,
                constants));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getModelBoundTableWidget());
    }

    @Override
    public void addModelListeners() {
        getModelBoundTableWidget().addModelListeners();
    }
}
