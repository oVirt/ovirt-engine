package org.ovirt.engine.ui.webadmin.section.main.view;

import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractOverlayPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class MainContentView extends AbstractView implements MainContentPresenter.ViewDef {

    private final FlowPanel contentContainer = new FlowPanel();

    private IsWidget nonOverlayContent = null;

    @Inject
    public MainContentView(final ClientStorage clientStorage) {
        initWidget(contentContainer);
    }

    @Override
    public void removeFromSlot(Object slot, IsWidget content) {
        super.removeFromSlot(slot, content);
        if (content instanceof AbstractOverlayPresenter && nonOverlayContent != null) {
            // restore non-overlay content
            setPanelContent(contentContainer, nonOverlayContent);
        }
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MainContentPresenter.TYPE_SetContent) {
            if (content instanceof AbstractOverlayPresenter) {
                IsWidget currentContent = getCurrentContent();
                // prevent overlay content stacking, overlays views are containers
                if (!(currentContent instanceof Container)) {
                    // remember non-overlay content
                    nonOverlayContent = currentContent;
                }
            }
            setPanelContent(contentContainer, content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    private IsWidget getCurrentContent() {
        assert contentContainer.getWidgetCount() <= 1 : "MainContentView holds more than one content"; // $NON-NLS-1$
        return contentContainer.getWidgetCount() == 1 ? contentContainer.getWidget(0) : null;
    }
}
