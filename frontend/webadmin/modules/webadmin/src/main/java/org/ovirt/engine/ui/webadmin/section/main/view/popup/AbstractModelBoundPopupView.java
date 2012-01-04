package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.idhandler.HasElementId;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.utils.ElementIdUtils;
import org.ovirt.engine.ui.webadmin.view.AbstractPopupView;
import org.ovirt.engine.ui.webadmin.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.webadmin.widget.UiCommandButton;
import org.ovirt.engine.ui.webadmin.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.webadmin.widget.dialog.ProgressPopupContent;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for popup views bound to a UiCommon Window model.
 * 
 * @param <T>
 *            Window model type.
 */
public abstract class AbstractModelBoundPopupView<T extends Model> extends AbstractPopupView<SimpleDialogPanel>
        implements AbstractModelBoundPopupPresenterWidget.ViewDef<T>, HasElementId {

    /**
     * Popup progress indicator widget
     */
    private final ProgressPopupContent progressContent = new ProgressPopupContent();

    /**
     * Actual popup content
     */
    private Widget popupContent;

    /**
     * Popup hash-name
     */
    private String hashName;

    private String elementId = DOM.createUniqueId();

    public AbstractModelBoundPopupView(EventBus eventBus, ApplicationResources resources) {
        super(eventBus, resources);
    }

    @Override
    protected void initWidget(SimpleDialogPanel widget) {
        super.initWidget(widget);
        this.popupContent = widget.getContent();
    }

    @Override
    public void setTitle(String title) {
        asWidget().setHeader(new Label(title));
    }

    @Override
    public void setMessage(String message) {
        // No-op, override as necessary
    }

    @Override
    public void setItems(Iterable<?> items) {
        // No-op, override as necessary
    }

    @Override
    public void setHashName(String name) {
        hashName = name;
    }

    @Override
    public HasUiCommandClickHandlers addFooterButton(String label) {
        UiCommandButton button = new UiCommandButton(label);
        asWidget().addFooterButton(button);

        // Set button element ID for better accessibility
        button.asWidget().getElement().setId(
                ElementIdUtils.createElementId(elementId, label));

        return button;
    }

    @Override
    public void removeButtons() {
        asWidget().removeFooterButtons();
    }

    @Override
    public void startProgress(String progressMessage) {
        // Set dialog content to the progress indicator widget
        progressContent.setProgressMessage(progressMessage);
        asWidget().setContent(progressContent);

        // Hide dialog buttons when starting progress
        asWidget().setFooterPanelVisible(false);
    }

    @Override
    public void stopProgress() {
        // Set dialog content to the actual popup content widget
        asWidget().setContent(popupContent);

        // Show dialog buttons when stopping progress
        asWidget().setFooterPanelVisible(true);
    }

    @Override
    public void focusInput() {
        // No-op, override as necessary
    }

    @Override
    public void setPopupKeyPressHandler(PopupNativeKeyPressHandler handler) {
        asWidget().setKeyPressHandler(handler);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    protected String getHashName() {
        return hashName;
    }

}
