package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportsListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ReportsModule extends AbstractGinModule {

    @Provides
    @Singleton
    public MainModelProvider<Void, ReportsListModel> getDashboardReportsListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new MainTabModelProvider<Void, ReportsListModel>(
                eventBus, defaultConfirmPopupProvider,
                ReportsListModel.class);
    }

    @Override
    protected void configure() {
    }

}
