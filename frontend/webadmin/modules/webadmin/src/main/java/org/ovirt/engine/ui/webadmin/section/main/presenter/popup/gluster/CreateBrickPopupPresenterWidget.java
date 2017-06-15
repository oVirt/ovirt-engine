package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.core.common.businessentities.RaidType;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.CreateBrickModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class CreateBrickPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<CreateBrickModel, CreateBrickPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<CreateBrickModel> {
        public void setRaidInfoMessages(String raidType, int stripeSize);
        void setRaidParamsVisibility(boolean isVisiable);
    }

    @Inject
    public CreateBrickPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final CreateBrickModel model) {
        super.init(model);
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("raidTypeChanged".equals(propName)) { //$NON-NLS-1$
                if (model.getRaidTypeList().getSelectedItem() != RaidType.NONE
                        && model.getRaidTypeList().getSelectedItem() != RaidType.RAID0) {
                    getView().setRaidInfoMessages(model.getRaidTypeList().getSelectedItem().name(),
                            model.getStripeSize().getEntity());
                    getView().setRaidParamsVisibility(true);
                } else {
                    getView().setRaidParamsVisibility(false);
                }
            }
        });
    }
}
