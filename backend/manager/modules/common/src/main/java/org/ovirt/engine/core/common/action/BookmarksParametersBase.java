package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "BookmarksParametersBase")
public class BookmarksParametersBase extends VdcActionParametersBase {
    private static final long serialVersionUID = 2719098594290719344L;
    @XmlElement
    private Guid _bookmarkId;

    public BookmarksParametersBase(Guid bookmarkId) {
        _bookmarkId = bookmarkId;
    }

    public Guid getBookmarkId() {
        return _bookmarkId;
    }

    public BookmarksParametersBase() {
    }
}
