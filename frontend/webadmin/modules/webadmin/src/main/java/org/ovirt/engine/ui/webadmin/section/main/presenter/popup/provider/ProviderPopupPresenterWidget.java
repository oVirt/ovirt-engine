package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ProviderPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ProviderModel, ProviderPopupPresenterWidget.ViewDef> {

    private static final String IS_AVAILABLE = "IsAvailable"; //$NON-NLS-1$

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ProviderModel> {
        HasUiCommandClickHandlers getTestButton();
        void setTestResultImage(String errorMessage);
        void setAgentTabVisibility(boolean visible);
        void setCurrentActiveProviderWidget();
    }

    @Inject
    public ProviderPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final ProviderModel model) {
        super.init(model);

        registerHandler(getView().getTestButton().addClickHandler(event -> model.getTestCommand().execute()));

        model.getTestResult().getEntityChangedEvent().addListener((ev, sender, args) ->
                getView().setTestResultImage(model.getTestResult().getEntity()));
        model.getNeutronAgentModel()
                .isPluginConfigurationAvailable()
                .getEntityChangedEvent()
                .addListener((ev, sender, args) -> getView().setAgentTabVisibility(model.getNeutronAgentModel()
                        .isPluginConfigurationAvailable()
                        .getEntity()));
        model.getDataCenter().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (IS_AVAILABLE.equals(args.propertyName)) {
                getView().setCurrentActiveProviderWidget();
            }
        });
        model.getKvmPropertiesModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (IS_AVAILABLE.equals(args.propertyName)) {
                getView().setCurrentActiveProviderWidget();
            }
        });
        model.getVmwarePropertiesModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (IS_AVAILABLE.equals(args.propertyName)) {
                getView().setCurrentActiveProviderWidget();
            }
        });
        model.getXenPropertiesModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (IS_AVAILABLE.equals(args.propertyName)) {
                getView().setCurrentActiveProviderWidget();
            }
        });
    }

}
