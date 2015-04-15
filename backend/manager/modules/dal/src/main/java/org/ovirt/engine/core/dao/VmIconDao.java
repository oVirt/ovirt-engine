package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public interface VmIconDao extends GenericDao<VmIcon, Guid> {

    public List<VmIcon> getByDataUrl(String dataUrl);

    /**
     * It deletes specified icon if it is not 'default icon' for some os
     * and it is not use by any vm-like entity.
     * @param iconId id of icon to delete
     */
    public void removeIfUnused(Guid iconId);

    /**
     * If the icon is not stored, it stores the icon.
     * @param icon icon data url
     * @return id of the icon in database
     */
    public Guid ensureIconInDatabase(final String icon);

    public void removeAllUnusedIcons();
}
