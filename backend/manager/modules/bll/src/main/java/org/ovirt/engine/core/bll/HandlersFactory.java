package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.interfaces.ITagsHandler;

public final class HandlersFactory {

    public static ITagsHandler createTagsHandler() {
        return new TagsHandler();
    }

}
