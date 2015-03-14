package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.ModelListTreeViewModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.SimpleSelectionTreeNodeModel;

import com.google.gwt.event.shared.EventBus;

public abstract class AbstractModelBoundTreePopupView<T extends Model> extends AbstractModelBoundPopupView<T> {

    public AbstractModelBoundTreePopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void hide() {
        super.hide();
        getTreeViewModel().removeHandlers();
    }

    protected abstract ModelListTreeViewModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> getTreeViewModel();
}
