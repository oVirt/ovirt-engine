package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.logging.Logger;

import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ScrollPanel;

/*
 * Condition 1: The _nicList_ panel has a total height < the height of the scroll panel
 * Action 1:    Do nothing during a drag over
 *
 * Condition 2: The _nicList_ panel has a total height > the height of the scroll panel (> 6 nics)
 * Action 2:    Enable auto-scroll regions at the bottom and top of the scroll panel:
 *
 *                  - If drag pointer hovers over the upper auto-scroll region for >100ms?, and there
 *                    is _nicList_ content above the viewport, scroll up to see the above content
 *
 *                  - If drag pointer hovers over the lower auto-scroll region for >100ms?, and there
 *                    is _nicList_ content below the viewport, scroll down to see the lower content.
 *
 *                  - If the drag pointer hovers over the upper or lower auto-scroll region for <100ms,
 *                    just display its existence but don't scroll.
 */
public class AutoScrollAdapter implements
        AutoScrollEnableEvent.Handler, AutoScrollDisableEvent.Handler,
        AutoScrollOverEvent.Handler, MouseMoveHandler {
    public static final int EDGE_DELTA_FOR_SCROLLING = 20; // in pixels
    public static final int SCROLL_STEP = 20; // in pixels
    public static final int AUTO_SCROLL_CHECK_INTERVAL = 100; // in milliseconds



    protected final Logger log = Logger.getLogger(AutoScrollAdapter.class.getName());
    protected final ScrollPanel sp;

    protected final Timer scrollCheck = new Timer() {
        @Override
        public void run() {
            if (!enabled) {
                scrollCheck.cancel();
                return;
            }

            int top = sp.getAbsoluteTop();
            int height = sp.getOffsetHeight();

            int topEdgeDelta    = mouseY < top || mouseY > (top+height) ? -1 : mouseY - top;
            int bottomEdgeDelta = mouseY < top || mouseY > (top+height) ? -1 : top + height - mouseY;

            if (topEdgeDelta > -1 && topEdgeDelta < EDGE_DELTA_FOR_SCROLLING) {
                log.finer("inside the EDGE_DELTA_FOR_SCROLLING on the top"); //$NON-NLS-1$

                int minPos = sp.getMinimumVerticalScrollPosition();
                int currPosition = sp.getVerticalScrollPosition();
                if (currPosition > minPos) {
                    int newPosition = Math.max(minPos, currPosition - SCROLL_STEP);
                    sp.setVerticalScrollPosition(newPosition);
                }
            } else if (bottomEdgeDelta > -1 && bottomEdgeDelta < EDGE_DELTA_FOR_SCROLLING) {
                log.finer("inside the EDGE_DELTA_FOR_SCROLLING on the bottom"); //$NON-NLS-1$

                int maxPos = sp.getMaximumVerticalScrollPosition();
                int currPosition = sp.getVerticalScrollPosition();
                if (currPosition < maxPos) {
                    int newPosition = Math.min(maxPos, currPosition + SCROLL_STEP);
                    sp.setVerticalScrollPosition(newPosition);
                }
            }
        }
    };

    protected boolean enabled = false;
    protected int mouseY = -1;


    public AutoScrollAdapter(EventBus eventBus, ScrollPanel sp) {
        log.fine("attaching auto scroll handler to element "); //$NON-NLS-1$
        this.sp = sp;

        AutoScrollEnableEvent.register(eventBus, this);
        AutoScrollDisableEvent.register(eventBus, this);
        AutoScrollOverEvent.register(eventBus, this);
        sp.addBitlessDomHandler(this, MouseMoveEvent.getType());
    }

    @Override
    public void enableAutoScroll(AutoScrollEnableEvent event) {
        if (!enabled) {
            log.finer("auto-scrolling has been enabled"); //$NON-NLS-1$

            enabled = true;
            scrollCheck.scheduleRepeating(AUTO_SCROLL_CHECK_INTERVAL);
        }
    }

    @Override
    public void disableAutoScroll(AutoScrollDisableEvent event) {
        if (enabled) {
            log.finer("auto-scrolling has been disabled"); //$NON-NLS-1$

            enabled = false;
            scrollCheck.cancel();
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) { // mouse location normally
        mouseY = event.getClientY();
    }

    @Override
    public void overAutoScroll(AutoScrollOverEvent event) { // mouse location during a drag-and-drop
        mouseY = event.getClientY();
    }
}
