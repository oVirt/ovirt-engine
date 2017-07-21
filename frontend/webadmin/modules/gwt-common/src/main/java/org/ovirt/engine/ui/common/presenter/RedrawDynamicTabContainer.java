package org.ovirt.engine.ui.common.presenter;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.RequestTabsHandler;

/**
 * Event triggered when {@link DynamicTabContainerPresenter} should redraw (remove and re-add) its tabs.
 */
@GenEvent
public class RedrawDynamicTabContainer {

    Type<RequestTabsHandler> requestTabsEventType;

}
