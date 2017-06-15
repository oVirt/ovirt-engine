package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.utils.ValidationTabSwitchHelper;
import org.ovirt.engine.ui.common.view.TabbedView;
import org.ovirt.engine.ui.uicommonweb.models.HasValidatedTabs;
import org.ovirt.engine.ui.uicommonweb.models.Model;

import com.google.gwt.event.shared.EventBus;

public abstract class AbstractTabbedModelBoundPopupPresenterWidget<T extends Model & HasValidatedTabs,
    V extends AbstractTabbedModelBoundPopupPresenterWidget.ViewDef<T>>
        extends AbstractModelBoundPopupPresenterWidget<T, V> {

    public interface ViewDef<T extends Model> extends AbstractModelBoundPopupPresenterWidget.ViewDef<T>, TabbedView {
    }

    public AbstractTabbedModelBoundPopupPresenterWidget(EventBus eventBus, V view) {
        super(eventBus, view);
    }

    @Override
    public void onBind() {
        super.onBind();
        registerHandler(ValidationTabSwitchHelper.registerValidationHandler((EventBus) getEventBus(), this,
                getView()));
    }
}
