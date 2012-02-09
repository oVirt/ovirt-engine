package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportsListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ReportsModule extends AbstractGinModule {

    @Provides
    @Singleton
    public MainModelProvider<Void, ReportsListModel> getDashboardReportsListProvider(ClientGinjector ginjector) {
        return new MainTabModelProvider<Void, ReportsListModel>(ginjector, ReportsListModel.class);
    }

    @Override
    protected void configure() {
    }

}
