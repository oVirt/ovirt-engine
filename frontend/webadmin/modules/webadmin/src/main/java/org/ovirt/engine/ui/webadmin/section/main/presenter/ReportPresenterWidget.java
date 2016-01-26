package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.common.presenter.ModelBoundPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ReportPresenterWidget extends PresenterWidget<ReportPresenterWidget.ViewDef> implements ModelBoundPresenterWidget<ReportModel> {

    public interface ViewDef extends View {

        /**
         * Set the Frame URL
         */
        void setFrameUrl(String url);

        /**
         * Set the Frame parameters
         */
        void setFrameParams(Map<String, List<String>> params);

        /**
         * POST the Frame Data
         */
        void postFrame();
    }

    private ReportModel model = null;
    private final ErrorPopupManager errorPopupManager;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ReportPresenterWidget(EventBus eventBus,
            ViewDef view, ErrorPopupManager errorPopupManager) {
        super(eventBus, view);
        this.errorPopupManager = errorPopupManager;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        updateReportUrl();
    }

    private void updateReportUrl() {
        getView().setFrameParams(getModel().getReportParams());
        getView().setFrameUrl(getModel().getReportUrl());
        getView().postFrame();
    }

    public ReportModel getModel() {
        return model;
    }

    @Override
    public void init(ReportModel model) {
        this.model = model;

        if (model.isDifferntDcError()) {
            errorPopupManager.show(constants.entitiesFromDifferentDCsError());
        } else {
            updateReportUrl();
        }
    }
}
