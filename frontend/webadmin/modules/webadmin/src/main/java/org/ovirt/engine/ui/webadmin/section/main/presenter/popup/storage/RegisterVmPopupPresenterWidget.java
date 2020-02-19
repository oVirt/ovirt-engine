package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.RegisterVmData;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.register.VnicProfileMappingPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.RegisterVmInfoPanel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class RegisterVmPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<RegisterVmModel, RegisterVmPopupPresenterWidget.ViewDef> {

    private final Provider<VnicProfileMappingPopupPresenterWidget> vnicProfileMappingPopupProvider;

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<RegisterVmModel> {
        RegisterVmInfoPanel getInfoPanel();
    }

    @Inject
    public RegisterVmPopupPresenterWidget(EventBus eventBus,
            ViewDef view,
            Provider<VnicProfileMappingPopupPresenterWidget> vnicProfileMappingPopupProvider) {
        super(eventBus, view);
        this.vnicProfileMappingPopupProvider = vnicProfileMappingPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(RegisterVmModel source,
            UICommand lastExecutedCommand,
            Model windowModel) {
        if (lastExecutedCommand == source.getVnicProfileMappingCommand()) {
            return vnicProfileMappingPopupProvider.get();
        }
        return super.getModelPopup(source, lastExecutedCommand, windowModel);
    }

    @Override
    public void init(final RegisterVmModel model) {
        super.init(model);

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args.propertyName.equals("InvalidVm")) { //$NON-NLS-1$
                setGeneralTabValidity(model);
            }
        });
    }

    private void setGeneralTabValidity(RegisterVmModel model) {
        RegisterVmData selectedItem = model.getEntities().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        EntityModel<String> nameEntityInPanel = getView().getInfoPanel().getVmGeneralModel().getName();
        nameEntityInPanel.setInvalidityReasons(selectedItem.getInvalidityReasons());
        nameEntityInPanel.setIsValid(selectedItem.getIsValid());
    }
}
