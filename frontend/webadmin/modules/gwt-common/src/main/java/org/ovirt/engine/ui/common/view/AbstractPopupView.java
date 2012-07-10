package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
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

    private final CommonApplicationResources resources;

    private T widget;

    public AbstractPopupView(EventBus eventBus, CommonApplicationResources resources) {
        super(eventBus);
        this.resources = resources;
        resources.dialogBoxStyle().ensureInjected();
    }

    protected void initWidget(T widget) {
        this.widget = widget;

        // All popups are modal by default
        widget.setModal(true);

        // Enable background glass by default
        widget.setGlassEnabled(true);

        // Add popup widget style
        widget.addStyleName(resources.dialogBoxStyle().getName());
    }

    @Override
    public T asWidget() {
        return widget;
    }

}
