package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.UserSession;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.SessionListModel;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class SessionModule extends AbstractGinModule {

    @Provides
    @Singleton
    public MainModelProvider<UserSession, SessionListModel> getSessionListProvider(EventBus eventBus,
            final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<SessionListModel> modelProvider,
            final Provider<CommonModel> commonModelProvider) {
        MainTabModelProvider<UserSession, SessionListModel> mainTabSessionModelProvider =
                new MainTabModelProvider<>(eventBus, defaultConfirmPopupProvider, commonModelProvider);
        mainTabSessionModelProvider.setModelProvider(modelProvider);
        return mainTabSessionModelProvider;
    }

    @Override
    protected void configure() {
        bind(SessionListModel.class).in(Singleton.class);
    }
}
