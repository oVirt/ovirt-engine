package org.ovirt.engine.ui.common.widget.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class AbstractVmBasedPopupPresenterWidget<V extends AbstractVmBasedPopupPresenterWidget.ViewDef> extends AbstractModelBoundPopupPresenterWidget<UnitVmModel, V>  {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<UnitVmModel> {
        void switchMode(boolean isAdvanced);
    }

    @Inject
    public AbstractVmBasedPopupPresenterWidget(EventBus eventBus, V view) {
        super(eventBus, view);
    }

    @Override
    public void init(UnitVmModel model) {
        super.init(model);

        initListeners(model);
        swithAccordingToMode(model);
    }

    private void initListeners(final UnitVmModel model) {
        model.getAdvancedMode().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(org.ovirt.engine.ui.uicompat.Event ev, Object sender, EventArgs args) {
                swithAccordingToMode(model);
            }

        });
    }

    private void swithAccordingToMode(final UnitVmModel model) {
        getView().switchMode((Boolean) model.getAdvancedMode().getEntity());
    }

}
