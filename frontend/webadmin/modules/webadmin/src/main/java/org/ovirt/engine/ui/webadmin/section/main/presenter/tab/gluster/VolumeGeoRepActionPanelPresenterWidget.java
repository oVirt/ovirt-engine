package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeoRepListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class VolumeGeoRepActionPanelPresenterWidget
    extends DetailActionPanelPresenterWidget<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VolumeGeoRepActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<GlusterGeoRepSession> view,
            SearchableDetailModelProvider<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<GlusterGeoRepSession>(constants.newGeoRepSession()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewSessionCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterGeoRepSession>(constants.removeGeoRepSession()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveSessionCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterGeoRepSession>(constants.startGeoRepSession()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStartSessionCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterGeoRepSession>(constants.stopGeoRepSession()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStopSessionCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterGeoRepSession>(constants.pauseGeoRepSession()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getPauseSessionCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterGeoRepSession>(constants.resumeGeoRepSession()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getResumeSessionCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterGeoRepSession>(constants.geoRepSessionsOptions()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSessionOptionsCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterGeoRepSession>(constants.geoRepSessionDetails()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getViewSessionDetailsCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterGeoRepSession>(constants.geoRepSessionSync()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRefreshSessionsCommand();
            }
        });
    }

}
