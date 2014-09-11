package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ProviderPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ProviderModel, ProviderPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ProviderModel> {
        HasUiCommandClickHandlers getTestButton();
        void setTestResultImage(String errorMessage);
        void setAgentTabVisibility(boolean visible);
    }

    @Inject
    public ProviderPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final ProviderModel model) {
        super.init(model);

        registerHandler(getView().getTestButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                model.getTestCommand().execute();
            }
        }));

        model.getTestResult().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                getView().setTestResultImage(model.getTestResult().getEntity());
            }
        });
        model.getNeutronAgentModel()
                .isPluginConfigurationAvailable()
                .getEntityChangedEvent()
                .addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                getView().setAgentTabVisibility(model.getNeutronAgentModel()
                        .isPluginConfigurationAvailable()
                        .getEntity());
            }
        });
    }

}
