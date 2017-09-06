package org.ovirt.engine.ui.webadmin.section.main.presenter.overlay;

import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractOverlayPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class BookmarkPresenterWidget extends AbstractOverlayPresenterWidget<BookmarkPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractOverlayPresenterWidget.ViewDef {
        void updateBookmarks();
    }

    private final BookmarkModelProvider bookmarkModelProvider;

    @Inject
    public BookmarkPresenterWidget(EventBus eventBus, ViewDef view,
            BookmarkModelProvider bookmarkModelProvider) {
        super(eventBus, view);
        this.bookmarkModelProvider = bookmarkModelProvider;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        bookmarkModelProvider.getModel().search();
    }

    @Override
    public void onBind() {
        super.onBind();
        bookmarkModelProvider.getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                getView().updateBookmarks();
            }
        });
    }
}
