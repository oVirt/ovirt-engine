package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.system.HeaderOffsetChangeEvent;
import org.ovirt.engine.ui.common.widget.tab.TabAccessibleChangeEvent;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Scroll-able tab bar widget that presents scrolling buttons when the window is not large enough to contain all the
 * tabs. The scroll buttons allow the user to scroll so they can see tabs that they could not see otherwise. The
 * widget also provide a drop down menu list of all the tabs.
 */
public class ScrollableTabBarPresenterWidget extends PresenterWidget<ScrollableTabBarPresenterWidget.ViewDef>
    implements TabWidgetHandler {

    /**
     * View definition.
     */
    public interface ViewDef extends View, TabWidgetHandler {
        /**
         * recalculate the needed sizes.
         */
        void recalculateSize();
        /**
         * Show/hide the scroll buttons.
         */
        void showScrollButtons();
        /**
         * Allow the caller to set the offset of the {@code ScrollableTabBar} relative to its container.
         * @param left How far to the left in pixels.
         * @param wantsLeft If the left offset needs to be added to the container.
         */
        void setOffset(int left, boolean wantsLeft);
        /**
         * Set how many pixels the scroll panel should scroll when a button is clicked.
         * @param scrollDistance The distance in pixels.
         */
        void setScrollDistance(int scrollDistance);
    }

    /**
     * The default number of pixels the scrollbar will scroll.
     */
    public static final int DEFAULT_SCROLL_DISTANCE = 20; //pixels.
    private HandlerRegistration resizeHandlerRegistration;
    private boolean wantsOffset = true;

    /**
     * Constructor.
     * @param eventBus The GWT event bus.
     * @param view The view associated with this presenter widget.
     */
    @Inject
    public ScrollableTabBarPresenterWidget(EventBus eventBus, ScrollableTabBarPresenterWidget.ViewDef view) {
        super(eventBus, view);
        setScrollDistance(DEFAULT_SCROLL_DISTANCE);
    }

    @Override
    protected void onBind() {
        super.onBind();
        //This handler is called when tab accessibility changes.
        registerHandler(getEventBus().addHandler(TabAccessibleChangeEvent.getType(),
                new TabAccessibleChangeEvent.TabAccessibleChangeHandler() {

            @Override
            public void onTabAccessibleChange(TabAccessibleChangeEvent event) {
                getView().recalculateSize();
                getView().showScrollButtons();
            }
        }));
        registerHandler(getEventBus().addHandler(HeaderOffsetChangeEvent.getType(),
                new HeaderOffsetChangeEvent.HeaderOffsetChangeHandler() {

            @Override
            public void onHeaderOffsetChange(HeaderOffsetChangeEvent event) {
                getView().setOffset(event.getWidth(), wantsOffset);
                //This may seem a little strange, removing the handler for the resize event after the first
                //offset change event. But the splitter also generates a resize event. So we would be handling the
                //same thing twice. Until the offset events are properly registered and working, we need the resize
                //event to calculate the proper sizes. Once it is working, we don't need to handle the resize events
                //anymore as the splitter will generate offset events based on window resizes.
                if (resizeHandlerRegistration != null) {
                    resizeHandlerRegistration.removeHandler();
                    resizeHandlerRegistration = null;
                }
            }
        }));
        resizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent resizeEvent) {
                getView().recalculateSize();
                getView().showScrollButtons();
            }
        });
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().recalculateSize();
        getView().showScrollButtons();
    }

    @Override
    public void addTabWidget(IsWidget tabWidget, int index) {
        getView().addTabWidget(tabWidget, index);
    }

    @Override
    public void removeTabWidget(IsWidget tabWidget) {
        getView().removeTabWidget(tabWidget);
    }

    /**
     * Allow the caller to set the offset of the {@code ScrollableTabBar} relative to its container.
     * @param left How far to the left in pixels.
     */
    public void setOffset(int left) {
        getView().setOffset(left, wantsOffset);
    }

    /**
     * Set how many pixels the scroll panel should scroll when a button is clicked.
     * @param scrollDistance The distance in pixels.
     */
    public void setScrollDistance(int scrollDistance) {
        getView().setScrollDistance(scrollDistance);
    }

    /**
     * Lets the presenter know if we should pass offsets to the scrollablle tab bar.
     * @param wantsOffset true if you want offsets passed, false otherwise.
     */
    public void setWantsOffset(boolean wantsOffset) {
        this.wantsOffset = wantsOffset;
    }

}
