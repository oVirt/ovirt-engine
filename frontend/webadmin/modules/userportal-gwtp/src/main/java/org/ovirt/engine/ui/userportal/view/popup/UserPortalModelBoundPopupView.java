package org.ovirt.engine.ui.userportal.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.AbstractUiCommandButton;
import org.ovirt.engine.ui.common.widget.IsProgressContentWidget;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.userportal.widget.UiCommandButton;
import org.ovirt.engine.ui.userportal.widget.dialog.ProgressPopupContent;

import com.google.gwt.event.shared.EventBus;

public abstract class UserPortalModelBoundPopupView<T extends Model> extends AbstractModelBoundPopupView<T> {

    public UserPortalModelBoundPopupView(EventBus eventBus, CommonApplicationResources resources) {
        super(eventBus, resources);
    }

    @Override
    protected IsProgressContentWidget createProgressContentWidget() {
        return new ProgressPopupContent();
    }

    @Override
    protected AbstractUiCommandButton createCommandButton(String label) {
        return new UiCommandButton(label);
    }

}
