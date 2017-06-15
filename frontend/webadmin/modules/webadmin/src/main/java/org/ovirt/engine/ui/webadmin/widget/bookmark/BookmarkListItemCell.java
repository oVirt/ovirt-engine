package org.ovirt.engine.ui.webadmin.widget.bookmark;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;

public class BookmarkListItemCell extends AbstractCell<Bookmark> implements HasElementId {

    interface ViewIdHandler extends ElementIdHandler<BookmarkListItemCell> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    private String elementId = DOM.createUniqueId();

    public BookmarkListItemCell() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void render(Context context, Bookmark value, SafeHtmlBuilder sb) {
        sb.append(templates.bookmarkItem(
                ElementIdUtils.createElementId(elementId, value.getName()),
                value.getName()));
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

}
