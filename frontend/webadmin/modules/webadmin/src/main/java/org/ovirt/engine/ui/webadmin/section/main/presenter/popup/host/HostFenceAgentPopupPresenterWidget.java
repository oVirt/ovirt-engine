package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.DynamicMessages;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceAgentModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.FenceAgentModelProvider;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;


public class HostFenceAgentPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<FenceAgentModel, HostFenceAgentPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<FenceAgentModel> {
        HasUiCommandClickHandlers getTestButton();
        HasClickHandlers getFencingOptionsAnchor();
        void updatePmSlotLabelText(boolean ciscoUcsSelected);
    }

    final FenceAgentModelProvider provider;
    private final DynamicMessages dynamicMessages;

    @Inject
    public HostFenceAgentPopupPresenterWidget(EventBus eventBus,
            ViewDef view,
            FenceAgentModelProvider provider,
            DynamicMessages dynamicMessages) {
        super(eventBus, view);
        this.provider = provider;
        this.dynamicMessages = dynamicMessages;
    }

    @Override
    public void init(final FenceAgentModel model) {
        super.init(model);
        provider.initializeModel(model);
        addTestButtonListener();
        addCiscoUcsPmTypeListener(model);
        registerHandler(getView().getFencingOptionsAnchor().addClickHandler(event -> {
            Window.open(dynamicMessages.fencingOptionsUrl(), "_blank", null); //$NON-NLS-1$
        }));
    }

    private void addTestButtonListener() {
        registerHandler(getView().getTestButton().addClickHandler(event -> {
            getView().flush();
            getView().getTestButton().getCommand().execute();
        }));
    }

    private void addCiscoUcsPmTypeListener(final FenceAgentModel model) {
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("IsCiscoUcsPrimaryPmTypeSelected".equals(propName)) { //$NON-NLS-1$
                getView().updatePmSlotLabelText(model.isCiscoUcsPrimaryPmTypeSelected());
            }
        });
    }
}
