package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;

public class SubTabVolumePermissionView extends AbstractSubTabPermissionsView<GlusterVolumeEntity, VolumeListModel>
        implements SubTabVolumePermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVolumePermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVolumePermissionView(SearchableDetailModelProvider<Permission, VolumeListModel,
            PermissionListModel<GlusterVolumeEntity>> modelProvider, EventBus eventBus,
            PermissionActionPanelPresenterWidget<GlusterVolumeEntity, VolumeListModel, PermissionListModel<GlusterVolumeEntity>> actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, actionPanel);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
