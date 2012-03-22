package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVolumePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabVolumeView extends AbstractMainTabWithDetailsTableView<GlusterVolumeEntity, VolumeListModel> implements MainTabVolumePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabVolumeView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabVolumeView(MainModelProvider<GlusterVolumeEntity, VolumeListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>("Create Volume") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCreateVolumeCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveVolumeCommand();
            }
        });
    }

}
