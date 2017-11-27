package org.ovirt.engine.ui.webadmin.section.main.presenter.overlay;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractOverlayPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.RevealOverlayContentEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class BookmarkPresenterWidget extends AbstractOverlayPresenterWidget<BookmarkPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractOverlayPresenterWidget.ViewDef {
        void clearBookmarks();
        HandlerRegistration addBookmark(Bookmark bookmark, BookmarkListModel model, ClickHandler handler);
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
        bookmarkModelProvider.getModel().getItemsChangedEvent().addListener((ev, sender, args) -> updateBookmarks());
    }

    private void updateBookmarks() {
        BookmarkListModel model = bookmarkModelProvider.getModel();
        Collection<Bookmark> items = model.getItems();
        AbstractMainWithDetailsPresenter<?, ?, ?, ?> presenter = null;
        if (getCurrentPlaceWidget() instanceof AbstractMainWithDetailsPresenter) {
            presenter = (AbstractMainWithDetailsPresenter<?, ?, ?, ?>) getCurrentPlaceWidget();
        }
        final AbstractMainWithDetailsPresenter<?, ?, ?, ?> mainPresenter = presenter;
        getView().clearBookmarks();
        if (items != null && !items.isEmpty()) {
            for (Bookmark bookmark : items) {
                if (mainPresenter != null && isBookmarkForCurrentPlace(bookmark, mainPresenter)) {
                    registerHandler(getView().addBookmark(bookmark, model, event -> {
                        mainPresenter.applySearchString(bookmark.getValue());
                        // Close the overlay.
                        RevealOverlayContentEvent.fire(this, new RevealOverlayContentEvent(null));
                        event.preventDefault();
                    }));
                } else {
                    getView().addBookmark(bookmark, model, null);
                }
            }
        }
    }

    private boolean isBookmarkForCurrentPlace(Bookmark bookmark,
            AbstractMainWithDetailsPresenter<?, ?, ?, ?> currentPlacePresenter) {
        String searchString = bookmark.getValue();
        if (searchString != null) {
            String[] split = searchString.split(":"); //$NON-NLS-1$
            String result = null;
            if (split.length > 0) {
                String defaultSearchString = split[0];
                result = SearchStringMapping.getPlace(defaultSearchString);
            }
            if (currentPlacePresenter != null && currentPlacePresenter.placeMatches(result)) {
                return true;
            }
        }
        return false;
    }

}
