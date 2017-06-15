package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabEventView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;

public class SubTabVolumeEventView extends AbstractSubTabEventView<GlusterVolumeEntity, VolumeListModel, VolumeEventListModel>
        implements SubTabVolumeEventPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVolumeEventView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVolumeEventView(SearchableDetailModelProvider<AuditLog, VolumeListModel, VolumeEventListModel> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
