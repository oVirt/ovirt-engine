package org.ovirt.engine.ui.webadmin.uicommon.model;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeListModel;

public class InstanceTypeGeneralModelProvider extends DetailTabModelProvider<InstanceTypeListModel, InstanceTypeGeneralModel> {

    @Inject
    public InstanceTypeGeneralModelProvider(EventBus eventBus, Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider, InstanceTypeListModel.class, InstanceTypeGeneralModel.class);
    }
}
