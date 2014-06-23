package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.instancetypes.InstanceTypesPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class InstanceTypeModelProvider extends SearchableTabModelProvider<InstanceType, InstanceTypeListModel> {

    private final Provider<InstanceTypesPopupPresenterWidget> instanceTypePopupProvider;

    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public InstanceTypeModelProvider(EventBus eventBus,
                                     final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
                                     final Provider<InstanceTypesPopupPresenterWidget> instanceTypePopupProvider,
                                     final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
        this.instanceTypePopupProvider = instanceTypePopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(InstanceTypeListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand == getModel().getNewInstanceTypeCommand() || lastExecutedCommand == getModel().getEditInstanceTypeCommand()) {
            return instanceTypePopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(InstanceTypeListModel source,
            UICommand lastExecutedCommand) {
            if (lastExecutedCommand == getModel().getDeleteInstanceTypeCommand()) {
                return removeConfirmPopupProvider.get();
            } else {
                return super.getConfirmModelPopup(source, lastExecutedCommand);
            }
    }

}
