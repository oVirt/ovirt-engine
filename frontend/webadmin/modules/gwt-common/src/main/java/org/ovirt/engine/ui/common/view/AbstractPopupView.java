package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
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
    private static final String GWT_POPUPPANEL = "gwt-PopupPanel"; // $NON-NLS-1$

    public AbstractPopupView(EventBus eventBus) {
        super(eventBus);
        setPopupPositioner(new OvirtCenterPopupPositioner());
    }

    @Override
    protected final void initWidget(IsWidget widget) {
        throw new IllegalArgumentException("Use initWidget(PopupPanel) instead of initWidget(Widget)"); //$NON-NLS-1$
    }

    /**
     * TODO-GWT return false to work around GWTP memory leak
     * The resize handler implementation is broken, so turn it off.
     * See: https://github.com/ArcBees/GWTP/issues/823
     * Once fixed, remove {@link #getRepositionOnWindowResizeHandler} workaround.
     */
    @Override
    protected boolean repositionOnWindowResize() {
        return false;
    }

    protected void initWidget(T widget) {
        super.initWidget(widget);

        // All popups are modal by default
        widget.setModal(true);

        // Enable background glass by default
        widget.setGlassEnabled(true);

        // Add popup widget style
        widget.addStyleName(POPUP_CONTENT_STYLE_NAME);

        widget.removeStyleName(GWT_POPUPPANEL);
        widget.getElement().getStyle().setZIndex(PatternflyConstants.ZINDEX_MODAL);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T asWidget() {
        return (T) super.asWidget();
    }

    @Override
    public HandlerRegistration getRepositionOnWindowResizeHandler() {
        return Window.addResizeHandler(event -> {
            if (asPopupPanel().isShowing()) {
                showAndReposition();
            }
        });
    }

}
