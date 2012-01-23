package org.ovirt.engine.ui.webadmin.view;

import java.util.Map;
import java.util.Set;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.presenter.ErrorPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.inject.Inject;

public class ErrorPopupView extends AbstractPopupView<DialogBox> implements ErrorPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<DialogBox, ErrorPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Label titleLabel;

    @UiField
    HTML messageLabel;

    @UiField
    PushButton closeButton;

    ApplicationMessages messages;
    ApplicationTemplates templates;

    @Inject
    public ErrorPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationMessages messages,
            ApplicationTemplates templates) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        this.messages = messages;
        this.templates = templates;
    }

    void localize(ApplicationConstants constants) {
        titleLabel.setText(constants.errorPopupCaption());
        closeButton.setText(constants.closeButtonLabel());
    }

    @Override
    public void setErrorMessage(Map<String, Set<String>> desc2msgs) {
        // Only one error- without description
        if ((desc2msgs.size() == 1)) {
            String desc = desc2msgs.keySet().iterator().next();
            if ((desc2msgs.get(desc).size() == 1) && ((desc == null) || (desc.equals("")))) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.append(SafeHtmlUtils.fromSafeConstant(desc2msgs.get(desc).iterator().next()));
                SafeHtml sh =
                        SafeHtmlUtils.fromSafeConstant(messages.uiCommonFrontendFailure(sb.toSafeHtml().asString()));
                messageLabel.setHTML(sh);
                return;
            }
        }

        SafeHtmlBuilder allSb = new SafeHtmlBuilder();

        allSb.append(SafeHtmlUtils.fromTrustedString("</br></br>"));

        // More then one error or one error with description
        for (Map.Entry<String, Set<String>> entry : desc2msgs.entrySet()) {
            SafeHtmlBuilder listSb = new SafeHtmlBuilder();
            String desc = entry.getKey();

            for (String msg : entry.getValue()) {
                listSb.append(templates.listItem(SafeHtmlUtils.fromSafeConstant(msg)));
            }

            SafeHtml sh = templates.unsignedList(listSb.toSafeHtml());

            if (!desc.equals("")) {
                allSb.append(SafeHtmlUtils.fromString(desc + ":"));
            }

            allSb.append(sh);
        }

        SafeHtml sh = SafeHtmlUtils.fromSafeConstant(messages.uiCommonFrontendFailure(allSb.toSafeHtml().asString()));
        messageLabel.setHTML(sh);
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        messageLabel.setText(errorMessage);
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return closeButton;
    }

}
