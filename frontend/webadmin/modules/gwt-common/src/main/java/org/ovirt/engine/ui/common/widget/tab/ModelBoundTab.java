package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent.UiCommonInitHandler;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.shared.EventBus;

public class ModelBoundTab extends SimpleTab {

    public ModelBoundTab(final ModelBoundTabData tabData, AbstractTabPanel tabPanel, EventBus eventBus) {
        super(tabData, tabPanel);
        setAlign(tabData.getAlign());

        // Update tab accessibility
        setAccessible(tabData.getModelProvider().getModel().getIsAvailable());

        // Tab widgets are created as part of the corresponding TabView,
        // at this point UiCommonInitEvent has already been fired
        registerModelEventListeners(tabData.getModelProvider());

        // Add handler to be notified when UiCommon models are (re)initialized
        eventBus.addHandler(UiCommonInitEvent.getType(), new UiCommonInitHandler() {
            @Override
            public void onUiCommonInit(UiCommonInitEvent event) {
                setAccessible(tabData.getModelProvider().getModel().getIsAvailable());
                registerModelEventListeners(tabData.getModelProvider());
            }
        });
    }

    void registerModelEventListeners(final ModelProvider<? extends EntityModel> modelProvider) {
        modelProvider.getModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs) args;

                // Update tab accessibility when 'IsAvailable' property changes
                if ("IsAvailable".equals(pcArgs.PropertyName)) { //$NON-NLS-1$
                    boolean isAvailable = modelProvider.getModel().getIsAvailable();
                    setAccessible(isAvailable);
                }
            }
        });
    }

}
