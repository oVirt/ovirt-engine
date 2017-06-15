package org.ovirt.engine.ui.webadmin.section.main.view.popup.quota;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.uicommon.popup.quota.ChangeQuotaView;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.ChangeQuotaPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class ChangeQuotaPopupView extends AbstractModelBoundPopupView<ChangeQuotaModel> implements ChangeQuotaPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ChangeQuotaPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @Ignore
    ChangeQuotaView changeQuotaView;

    @Inject
    public ChangeQuotaPopupView(EventBus eventBus) {
        super(eventBus);

        changeQuotaView = new ChangeQuotaView();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void edit(ChangeQuotaModel object) {
        changeQuotaView.edit(object);
    }

    @Override
    public ChangeQuotaModel flush() {
        return changeQuotaView.flush();
    }

    @Override
    public void cleanup() {
        changeQuotaView.cleanup();
    }
}
