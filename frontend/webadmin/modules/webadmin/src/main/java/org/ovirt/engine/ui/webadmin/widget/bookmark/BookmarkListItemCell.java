package org.ovirt.engine.ui.webadmin.widget.bookmark;

import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.idhandler.HasElementId;
import org.ovirt.engine.ui.webadmin.utils.ElementIdUtils;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;

public class BookmarkListItemCell extends AbstractCell<bookmarks> implements HasElementId {

    interface ViewIdHandler extends ElementIdHandler<BookmarkListItemCell> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ApplicationTemplates templates;

    private String elementId = DOM.createUniqueId();

    public BookmarkListItemCell(ApplicationTemplates templates) {
        this.templates = templates;
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void render(Context context, bookmarks value, SafeHtmlBuilder sb) {
        sb.append(templates.bookmarkItem(
                ElementIdUtils.createElementId(elementId, value.getbookmark_name()),
                value.getbookmark_name()));
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

}
