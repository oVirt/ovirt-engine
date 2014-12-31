package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.core.common.businessentities.RaidType;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.CreateBrickModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class CreateBrickPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<CreateBrickModel, CreateBrickPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<CreateBrickModel> {
        void setDeviceInfoText(String raidType);
        void setDeviceInfoVisibility(boolean isVisiable);
    }

    @Inject
    public CreateBrickPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final CreateBrickModel model) {
        super.init(model);
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev,
                    Object sender,
                    EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).propertyName;
                if ("raidTypeChanged".equals(propName)) { //$NON-NLS-1$
                    if (model.getRaidTypeList().getSelectedItem() != RaidType.None
                            && model.getRaidTypeList().getSelectedItem() != RaidType.Raid0) {
                        getView().setDeviceInfoText(model.getRaidTypeList().getSelectedItem().name());
                        getView().setDeviceInfoVisibility(true);
                    } else {
                        getView().setDeviceInfoVisibility(false);
                    }
                }
            }

        });
    }
}
