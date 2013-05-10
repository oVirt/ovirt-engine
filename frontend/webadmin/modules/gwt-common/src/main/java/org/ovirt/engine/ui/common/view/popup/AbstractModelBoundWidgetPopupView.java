package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.Model;

import com.google.gwt.event.shared.EventBus;

/**
 * Base class for popup views that use {@linkplain AbstractModelBoundPopupWidget model-bound widgets} to represent their
 * content.
 *
 * @param <T>
 *            Window model type.
 */
public abstract class AbstractModelBoundWidgetPopupView<T extends Model> extends AbstractModelBoundPopupView<T> {

    private final SimpleDialogPanel dialogPanel = new SimpleDialogPanel();

    private final AbstractModelBoundPopupWidget<T> popupWidget;

    public AbstractModelBoundWidgetPopupView(EventBus eventBus, CommonApplicationResources resources,
            AbstractModelBoundPopupWidget<T> popupWidget, String dialogWidth, String dialogHeight) {
        super(eventBus, resources);
        this.popupWidget = popupWidget;
        initDialogPanel(dialogWidth, dialogHeight);
        initWidget(dialogPanel);
    }

    void initDialogPanel(String dialogWidth, String dialogHeight) {
        dialogPanel.setWidth(dialogWidth);
        dialogPanel.setHeight(dialogHeight);
        dialogPanel.setContent(popupWidget);
    }

    @Override
    public void edit(T object) {
        popupWidget.edit(object);
    }

    @Override
    public T flush() {
        return popupWidget.flush();
    }

    @Override
    public void focusInput() {
        popupWidget.focusInput();
    }

    @Override
    public void stopProgress() {
        // we set the focus traversal order right before showing the popup in its final state
        // because then we are absolutely sure that the popup contains all items it should include
        setFocusTraversalOrder();
        super.stopProgress();
    }

    private void setFocusTraversalOrder() {
        dialogPanel.setTabIndexes(popupWidget.setTabIndexes(1));
    }

    protected AbstractModelBoundPopupWidget<T> getContentWidget() {
       return popupWidget;
    }
}
