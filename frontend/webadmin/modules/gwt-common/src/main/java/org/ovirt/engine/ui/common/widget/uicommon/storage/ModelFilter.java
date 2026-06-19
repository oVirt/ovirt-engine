package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.Collection;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

/**
 * Interface of filter.
 */
public interface ModelFilter<M extends EntityModel<?>> {

    Collection<M> filter(Collection<M> items);
}
