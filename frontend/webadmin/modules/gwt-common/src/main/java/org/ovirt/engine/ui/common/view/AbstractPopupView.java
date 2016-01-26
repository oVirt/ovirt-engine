package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
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

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    public AbstractPopupView(EventBus eventBus) {
        super(eventBus);
        resources.dialogBoxStyle().ensureInjected();
    }

    @Override
    protected void initWidget(Widget widget) {
        throw new IllegalArgumentException("Use initWidget(PopupPanel) instead of initWidget(Widget)"); //$NON-NLS-1$
    }

    protected void initWidget(T widget) {
        super.initWidget(widget);

        // All popups are modal by default
        widget.setModal(true);

        // Enable background glass by default
        widget.setGlassEnabled(true);

        // Add popup widget style
        widget.addStyleName(resources.dialogBoxStyle().getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public T asWidget() {
        return (T) super.asWidget();
    }

}
