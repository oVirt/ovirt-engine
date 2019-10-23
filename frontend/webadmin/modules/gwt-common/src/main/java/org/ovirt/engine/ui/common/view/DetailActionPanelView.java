package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;

import com.google.gwt.core.shared.GWT;

public class DetailActionPanelView<E, T> extends ActionPanelView<E, T>
    implements DetailActionPanelPresenterWidget.ViewDef<E, T> {

    interface ViewIdHandler extends ElementIdHandler<DetailActionPanelView<?, ?>> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    DetailActionPanelView() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
