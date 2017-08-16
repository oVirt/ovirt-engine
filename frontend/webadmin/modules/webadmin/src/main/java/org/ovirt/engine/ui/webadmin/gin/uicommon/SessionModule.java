package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.UserSession;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainViewModelProvider;
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
            final Provider<SessionListModel> modelProvider) {
        MainViewModelProvider<UserSession, SessionListModel> mainTabSessionModelProvider =
                new MainViewModelProvider<>(eventBus, defaultConfirmPopupProvider);
        mainTabSessionModelProvider.setModelProvider(modelProvider);
        return mainTabSessionModelProvider;
    }

    @Override
    protected void configure() {
        bind(SessionListModel.class).in(Singleton.class);
    }
}
