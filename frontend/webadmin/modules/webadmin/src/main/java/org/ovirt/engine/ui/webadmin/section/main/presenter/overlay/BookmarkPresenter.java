package org.ovirt.engine.ui.webadmin.section.main.presenter.overlay;

import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractOverlayPresenter;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Proxy;

public class BookmarkPresenter extends AbstractOverlayPresenter<BookmarkPresenter.ViewDef, BookmarkPresenter.ProxyDef> {

    public interface ViewDef extends AbstractOverlayPresenter.ViewDef {
        void updateBookmarks();
    }

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<BookmarkPresenter> {
    }

    private final BookmarkModelProvider bookmarkModelProvider;

    @Inject
    public BookmarkPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            BookmarkModelProvider bookmarkModelProvider) {
        super(eventBus, view, proxy);
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
