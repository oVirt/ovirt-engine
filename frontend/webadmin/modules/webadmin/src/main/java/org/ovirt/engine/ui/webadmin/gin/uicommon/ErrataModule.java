package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainViewModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EngineErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.errata.ErrataMainSelectedItems;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Gin configuration module for Errata tabs and popups.
 */
public class ErrataModule extends AbstractGinModule {

    @Provides
    @Singleton
    public MainModelProvider<Erratum, EngineErrataListModel> getErrataListProvider(final EventBus eventBus,
            final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EngineErrataListModel> modelProvider) {

        MainViewModelProvider<Erratum, EngineErrataListModel> result =
                new MainViewModelProvider<>(eventBus, defaultConfirmPopupProvider);

        result.setModelProvider(modelProvider);

        return result;
    }

    @Provides
    @Singleton
    public DetailTabModelProvider<EngineErrataListModel, EntityModel<Erratum>> getErrataDetailProvider(final EventBus eventBus,
            final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<EngineErrataListModel> mainModelProvider,
            final Provider<EntityModel<Erratum>> modelProvider) {

        DetailTabModelProvider<EngineErrataListModel, EntityModel<Erratum>> result = new
                DetailTabModelProvider<>(eventBus, defaultConfirmPopupProvider);

        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);

        return result;
    }

    @Override
    protected void configure() {
        bind(EngineErrataListModel.class).in(Singleton.class);
        bind(ErrataMainSelectedItems.class).asEagerSingleton();
    }
}
