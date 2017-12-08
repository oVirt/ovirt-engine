package org.ovirt.engine.ui.common.view.popup;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.view.AbstractPopupView;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.IsProgressContentWidget;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.AbstractDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.common.widget.dialog.ProgressPopupContent;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for popup views bound to a UiCommon Window model.
 *
 * @param <T>
 *            Window model type.
 */
public abstract class AbstractModelBoundPopupView<T extends Model> extends AbstractPopupView<AbstractDialogPanel>
        implements AbstractModelBoundPopupPresenterWidget.ViewDef<T>, HasElementId, FocusableComponentsContainer {

    /**
     * Popup progress indicator widget
     */
    private final IsProgressContentWidget progressContent;

    /**
     * Actual popup content
     */
    private Widget popupContent;

    /**
     * Popup hash-name
     */
    private String hashName;

    private String elementId = DOM.createUniqueId();

    private final List<FocusableComponentsContainer> focusableButtons = new ArrayList<>();

    public AbstractModelBoundPopupView(EventBus eventBus) {
        super(eventBus);
        this.progressContent = createProgressContentWidget();
    }

    @Override
    protected void initWidget(AbstractDialogPanel widget) {
        super.initWidget(widget);
        this.popupContent = widget.getContent();
    }

    protected UiCommandButton createCommandButton(String label, String uniqueId) {
        return new UiCommandButton(label);
    }

    protected IsProgressContentWidget createProgressContentWidget() {
        return new ProgressPopupContent();
    }

    /**
     * Hook bound to "Title" property change on given model.
     */
    @Override
    public void setTitle(String title) {
        asWidget().setHeader(title);
    }

    /**
     * Hook bound to "Message" property change on given model.
     */
    @Override
    public void setMessage(String message) {
        // No-op, override as necessary
    }

    /**
     * Hook bound to "Items" property change on given model.
     * <p>
     * Called only if the model is a {@code ListModel}.
     */
    @Override
    public void setItems(Iterable<?> items) {
        // No-op, override as necessary
    }

    /**
     * Hook bound to "HashName" property change on given model.
     */
    @Override
    public void setHashName(String name) {
        this.hashName = name;
    }

    /**
     * Hook bound to "HelpTag" property change on given model.
     * <p>
     * Called only if the model's {@linkplain Model#getOpenDocumentationCommand Open
     * Documentation command} (open context-sensitive help) is available, passing it
     * as a parameter.
     */
    @Override
    public void setHelpCommand(UICommand command) {
        asWidget().setHelpCommand(command);
    }

    @Override
    public HasUiCommandClickHandlers addFooterButton(String label, String uniqueId, boolean isPrimary) {
        UiCommandButton button = createCommandButton(label, uniqueId);
        asWidget().addFooterButton(button);
        focusableButtons.add(0, button);

        // Set button element ID for better accessibility
        button.asWidget().getElement().setId(
                ElementIdUtils.createElementId(elementId, uniqueId));

        if (isPrimary) {
            button.setAsPrimary();
        }
        return button;
    }

    public void addStatusWidget(Widget widget) {
        asWidget().addStatusWidget(widget);
    }

    @Override
    public void removeButtons() {
        asWidget().removeFooterButtons();
        focusableButtons.clear();
    }

    @Override
    public void startProgress(String progressMessage) {
        // Set dialog content to the progress indicator widget
        progressContent.setProgressMessage(progressMessage);
        asWidget().setContent(progressContent.asWidget());

        // Hide dialog buttons when starting progress
        asWidget().setFooterPanelVisible(false);
    }

    @Override
    public void stopProgress() {
        // Set dialog content to the actual popup content widget
        asWidget().setContent(popupContent);

        // Show dialog buttons when stopping progress
        asWidget().setFooterPanelVisible(true);

        // Update tab index values
        updateTabIndexes();

        // Now that the panel is visible we can try to focus
        focusInput();
    }

    /**
     * Hook for setting initial focus when the View becomes visible.
     * <p>
     * For example:
     *
     * <pre>
     *  userNameWidget.setFocus(true);
     * </pre>
     */
    @Override
    public void focusInput() {
        // No-op, override as necessary
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
    public HandlerRegistration setPopupKeyPressHandler(PopupNativeKeyPressHandler handler) {
        return asWidget().setKeyPressHandler(handler);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    protected String getHashName() {
        return hashName;
    }

    /**
     * Hook for assigning tab index to widgets contained in the View.
     * <p>
     * For example:
     *
     * <pre>
     *  userNameWidget.setTabIndex(nextTabIndex++);
     *  passwordWidget.setTabIndex(nextTabIndex++);
     *  return nextTabIndex;
     * </pre>
     *
     * @param nextTabIndex
     *            Currently available tab index value.
     * @return Next available tab index value.
     */
    @Override
    public int setTabIndexes(int nextTabIndex) {
        // No-op, override as necessary
        return nextTabIndex;
    }

    @Override
    public void updateTabIndexes() {
        // Update tab indexes for popup view's content
        int nextTabIndex = setTabIndexes(1);

        // Update tab indexes for popup view's footer buttons
        for (FocusableComponentsContainer button : focusableButtons) {
            nextTabIndex = button.setTabIndexes(nextTabIndex);
        }
    }

    /**
     * Hook for initializing the View from given model.
     * <p>
     * Called once per model instance, before the initial {@link #edit}
     * call that fills the View with model's current data.
     * <p>
     * This is the right place for any model-specific (one-time) event
     * handler registration or initialization logic, for example:
     *
     * <pre>
     *  model.getFoo().getPropertyChangedEvent().addListener( ... );
     *  model.getBar().getEntityChangedEvent().addListener( ... );
     * </pre>
     *
     * @param model
     *            Model instance assigned to the View.
     */
    @Override
    public void init(T model) {
        // No-op, override as necessary
    }

}
