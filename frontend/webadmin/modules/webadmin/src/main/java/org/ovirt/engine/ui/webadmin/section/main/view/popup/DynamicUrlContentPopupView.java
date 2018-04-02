package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.view.AbstractPopupView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.DynamicUrlContentPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class DynamicUrlContentPopupView extends AbstractPopupView<SimpleDialogPanel> implements DynamicUrlContentPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DynamicUrlContentPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    IFrameElement iframeElement;

    @UiField
    SimpleDialogPanel dialogPanel;

    @Inject
    public DynamicUrlContentPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return null;
    }

    @Override
    public HasClickHandlers getCloseIconButton() {
        return asWidget().getCloseIconButton();
    }

    @Override
    public HandlerRegistration setPopupKeyPressHandler(PopupNativeKeyPressHandler keyPressHandler) {
        return asWidget().setKeyPressHandler(keyPressHandler);
    }

    @Override
    public void init(String title, String width, String height,
            boolean closeIconVisible) {
        dialogPanel.setHeader(title);
        asWidget().setWidth(width);
        asWidget().setHeight(height);
        asWidget().setCloseIconButtonVisible(closeIconVisible);
        // Let the plugins set margins and other things.
        dialogPanel.getContent().getParent().removeStyleName(Styles.MODAL_BODY);
    }

    @Override
    public void setContentUrl(String contentUrl) {
        iframeElement.setSrc(contentUrl);
    }

    @Override
    public HasClickHandlers addFooterButton(String label) {
        UiCommandButton button = new UiCommandButton(label);
        asWidget().addFooterButton(button);
        return button;
    }

}
