package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VolumePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VolumeModel, VolumePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VolumeModel> {
    }

    private AbstractModelBoundPopupPresenterWidget<ListModel, ?> popup = null;
    private Provider<AddBrickPopupPresenterWidget> popupProvider = null;
    private final EventBus eventBus;

    @Inject
    public VolumePopupPresenterWidget(EventBus eventBus,
            ViewDef view,
            final Provider<AddBrickPopupPresenterWidget> popupProvider) {
        super(eventBus, view);
        this.eventBus = eventBus;
        this.popupProvider = popupProvider;

    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VolumeModel source,
            UICommand lastExecutedCommand,
            Model windowModel) {
        if (lastExecutedCommand.equals(source.getAddBricksCommand())) {
            return popupProvider.get();
        }
        return super.getModelPopup(source, lastExecutedCommand, windowModel);
    }

}
