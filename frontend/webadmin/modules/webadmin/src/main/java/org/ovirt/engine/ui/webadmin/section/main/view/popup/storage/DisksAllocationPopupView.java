package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.uicommon.storage.DisksAllocationView;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class DisksAllocationPopupView extends AbstractModelBoundPopupView<DisksAllocationModel> implements DisksAllocationPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DisksAllocationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    final CommonApplicationTemplates templates = GWT.create(CommonApplicationTemplates.class);

    SafeHtml warningImage;

    @UiField
    FlowPanel messagePanel;

    @UiField(provided = true)
    @Ignore
    DisksAllocationView disksAllocationView;

    DisksAllocationModel disksAllocationModel;

    @Inject
    public DisksAllocationPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);

        warningImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(
                resources.logWarningImage()).getHTML());

        disksAllocationView = new DisksAllocationView(constants);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void edit(DisksAllocationModel object) {
        disksAllocationView.edit(object);
        disksAllocationModel = object;
    }

    @Override
    public DisksAllocationModel flush() {
        return disksAllocationView.flush();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);

        if (message != null && !message.isEmpty()) {
            messagePanel.add(new HTML(templates.iconWithText(warningImage, message)));
        }

        messagePanel.setVisible(messagePanel.iterator().hasNext());
    }

}
