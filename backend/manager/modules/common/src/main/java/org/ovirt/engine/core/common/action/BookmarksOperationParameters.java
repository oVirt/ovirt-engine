package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "BookmarksOperationParameters", namespace = "http://service.engine.ovirt.org")
public class BookmarksOperationParameters extends BookmarksParametersBase {
    private static final long serialVersionUID = 904048653429089175L;
    @XmlElement
    @Valid
    private bookmarks _bookmark;

    public BookmarksOperationParameters(bookmarks bookmark) {
        super(bookmark.getbookmark_id());
        _bookmark = bookmark;
    }

    public bookmarks getBookmark() {
        return _bookmark;
    }

    public BookmarksOperationParameters() {
    }
}
