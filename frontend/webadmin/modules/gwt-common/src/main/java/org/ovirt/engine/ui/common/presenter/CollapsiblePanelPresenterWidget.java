package org.ovirt.engine.ui.common.presenter;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class CollapsiblePanelPresenterWidget extends
    PresenterWidget<CollapsiblePanelPresenterWidget.ViewDef> {

    public interface ViewDef extends View {
        /**
         * Add the content to the panel
         * @param widget The widget to add.
         */
        void addContentWidget(IsWidget widget);
        /**
         * Add the header widget, could be a label, or something more complex.
         * @param widget The widget to add.
         */
        void setTitleWidget(IsWidget widget);

        /**
         * Remove the content from the panel.
         */
        void clearContent();

        /**
         * Collapse the panel;
         */
        void collapsePanel();
    }

    @Inject
    public CollapsiblePanelPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    public void collapsePanel() {
        getView().collapsePanel();
    }

}
