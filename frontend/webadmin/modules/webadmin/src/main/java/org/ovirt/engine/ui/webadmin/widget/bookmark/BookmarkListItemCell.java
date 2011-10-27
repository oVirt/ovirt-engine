package org.ovirt.engine.ui.webadmin.widget.bookmark;

import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class BookmarkListItemCell extends AbstractCell<bookmarks> {

    private final ApplicationTemplates templates;

    public BookmarkListItemCell(ApplicationTemplates templates) {
        this.templates = templates;
    }

    @Override
    public void render(Context context, bookmarks value, SafeHtmlBuilder sb) {
        sb.append(templates.bookmarkItem(value.getbookmark_name()));
    }

}
