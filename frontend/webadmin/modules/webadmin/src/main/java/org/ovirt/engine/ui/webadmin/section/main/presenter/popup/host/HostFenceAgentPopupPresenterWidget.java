package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceAgentModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.uicommon.model.FenceAgentModelProvider;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostFenceAgentPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<FenceAgentModel, HostFenceAgentPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<FenceAgentModel> {
        HasUiCommandClickHandlers getTestButton();
        void updatePmSlotLabelText(boolean ciscoUcsSelected);
    }

    final FenceAgentModelProvider provider;

    @Inject
    public HostFenceAgentPopupPresenterWidget(EventBus eventBus, ViewDef view, FenceAgentModelProvider provider) {
        super(eventBus, view);
        this.provider = provider;
    }

    @Override
    public void init(final FenceAgentModel model) {
        super.init(model);
        provider.initializeModel(model);
        addTestButtonListener();
        addCiscoUcsPmTypeListener(model);
    }

    private void addTestButtonListener() {
        registerHandler(getView().getTestButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().flush();
                getView().getTestButton().getCommand().execute();
            }
        }));
    }

    private void addCiscoUcsPmTypeListener(final FenceAgentModel model) {
        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
                if ("IsCiscoUcsPrimaryPmTypeSelected".equals(propName)) { //$NON-NLS-1$
                    getView().updatePmSlotLabelText(model.isCiscoUcsPrimaryPmTypeSelected());
                }
            }
        });
    }
}
