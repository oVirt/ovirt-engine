package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.interfaces.*;

public final class HandlersFactory {

    public static VmPoolHandler createVmPoolHandler() {
        return new VmPoolHandler();
    }

    public static ITagsHandler createTagsHandler() {
        return new TagsHandler();
    }

}
