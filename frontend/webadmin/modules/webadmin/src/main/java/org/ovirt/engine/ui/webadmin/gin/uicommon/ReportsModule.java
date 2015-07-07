package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportsListModel;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class ReportsModule extends AbstractGinModule {

    @Provides
    @Singleton
    public ReportsListModel getReportsListModel(EventBus eventBus, final Provider<CommonModel> commonModelProvider) {
        ReportsListModel result = new ReportsListModel(ReportInit.getInstance().getReportBaseUrl(),
                ReportInit.getInstance().getSsoToken(), commonModelProvider);
        result.setEventBus(eventBus);
        return result;
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<MainModelProvider<Void, ReportsListModel>>(){})
            .to(new TypeLiteral<MainTabModelProvider<Void, ReportsListModel>>(){}).in(Singleton.class);
    }

}
