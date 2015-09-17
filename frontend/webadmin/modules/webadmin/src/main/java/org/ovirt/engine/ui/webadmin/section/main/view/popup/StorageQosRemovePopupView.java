package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.view.popup.RemoveConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.StorageQosRemovePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class StorageQosRemovePopupView extends RemoveConfirmationPopupView implements StorageQosRemovePopupPresenterWidget.ViewDef {

    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public StorageQosRemovePopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected void addItemText(Object item) {
        addItemLabel(SafeHtmlUtils.fromTrustedString(getItemTextFormatted(item.toString())));
    }

}
