package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 * Base class for views meant to be displayed as popups.
 * <p>
 * Similar to {@link AbstractView}, holds the reference to the actual UI widget.
 *
 * @param <T>
 *            Popup view widget type.
 */
public abstract class AbstractPopupView<T extends PopupPanel> extends PopupViewImpl implements AbstractPopupPresenterWidget.ViewDef {

    public static final String POPUP_CONTENT_STYLE_NAME = "popup-content"; //$NON-NLS-1$

    public AbstractPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected final void initWidget(IsWidget widget) {
        throw new IllegalArgumentException("Use initWidget(PopupPanel) instead of initWidget(Widget)"); //$NON-NLS-1$
    }

    protected void initWidget(T widget) {
        super.initWidget(widget);

        // All popups are modal by default
        widget.setModal(true);

        // Enable background glass by default
        widget.setGlassEnabled(true);

        // Add popup widget style
        widget.addStyleName(POPUP_CONTENT_STYLE_NAME);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T asWidget() {
        return (T) super.asWidget();
    }

}
