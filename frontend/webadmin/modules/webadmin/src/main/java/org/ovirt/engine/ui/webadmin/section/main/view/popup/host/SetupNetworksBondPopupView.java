package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBondInterfaceModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksBondPopupPresenterWidget;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SetupNetworksBondPopupView extends HostBondPopupView implements SetupNetworksBondPopupPresenterWidget.ViewDef {

    @Inject
    public SetupNetworksBondPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void edit(final HostBondInterfaceModel object) {
        super.edit(object);

        bondSuggestEditor.setVisible(true);
        bondEditor.setVisible(false);

        // hide widgets
        info.setVisible(false);
        message.setVisible(false);
        // resize
        layoutPanel.remove(infoPanel);
        layoutPanel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
        asPopupPanel().setPixelSize(400, 275);
    }
}
