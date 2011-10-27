package org.ovirt.engine.ui.webadmin.widget.tab;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelChangeEvent.CommonModelChangeHandler;
import org.ovirt.engine.ui.webadmin.uicommon.model.ModelProvider;

public class ModelBoundTab extends SimpleTab {

    public ModelBoundTab(final ModelBoundTabData tabData, AbstractTabPanel tabPanel) {
        super(tabData, tabPanel);
        setAlign(tabData.getAlign());

        // Update tab accessibility
        setAccessible(tabData.getModelProvider().getModel().getIsAvailable());

        // Tab widgets are created as part of the corresponding TabView,
        // at this point CommonModelChangeEvent has already been fired
        registerModelEventListeners(tabData.getModelProvider());

        // Add CommonModel change handler to be notified when the CommonModel instance changes
        ClientGinjectorProvider.instance().getEventBus()
                .addHandler(CommonModelChangeEvent.getType(), new CommonModelChangeHandler() {
                    @Override
                    public void onCommonModelChange(CommonModelChangeEvent event) {
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
                if ("IsAvailable".equals(pcArgs.PropertyName)) {
                    boolean isAvailable = modelProvider.getModel().getIsAvailable();
                    setAccessible(isAvailable);
                }
            }
        });
    }

}
