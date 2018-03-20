package org.ovirt.engine.ui.common.presenter;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.widget.HasToggle;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ToggleButtonPresenterWidget extends PresenterWidget<ToggleButtonPresenterWidget.ViewDef> {

    public interface ViewDef extends View {
        HasClickHandlers getButton();

        void switchToDefault();

        void switchToSecondary();
    }

    private HasToggle hasToggle;
    private boolean toggled;

    @Inject
    public ToggleButtonPresenterWidget(EventBus eventBus, ToggleButtonPresenterWidget.ViewDef view) {
        super(eventBus, view);
    }

    public void setTarget(HasToggle hasToggle) {
        this.hasToggle = hasToggle;
    }

    @Override
    public void onBind() {
        super.onBind();
        registerHandler(getView().getButton().addClickHandler(event -> {
            if (toggled) {
                getView().switchToDefault();
            } else {
                getView().switchToSecondary();
            }
            toggled = !toggled;
            hasToggle.onToggle(toggled);
        }));
    }
}

