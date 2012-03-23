package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.CommonApplicationResources;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;

/**
 * Base implementation of the LoginPopupView. Currently adds only the setAutoHideOnNavigationEventEnabled() to
 * initWidget. TODO: check if bigger portion of the LoginPopupView can not be moved to this class
 */
public class AbstractLoginPopupView extends AbstractPopupView<DecoratedPopupPanel> {

    public AbstractLoginPopupView(EventBus eventBus, CommonApplicationResources resources) {
        super(eventBus, resources);
    }

    protected void initWidget(DecoratedPopupPanel widget) {
        super.initWidget(widget);

        setAutoHideOnNavigationEventEnabled(true);
    }

}
