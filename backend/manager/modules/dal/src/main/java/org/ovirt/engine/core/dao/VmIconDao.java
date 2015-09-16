package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.compat.Guid;

public interface VmIconDao extends GenericDao<VmIcon, Guid> {

    /**
     * Retrieves the list of all vm icons with optional permission filtering.
     *
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     *
     * @return if not filtered: the list of all icons, if filtered: union of predefined icons and icons that are
     *         associated with some entity the user has permission to (vm or template)
     */
    List<VmIcon> getAll(Guid userID, boolean isFiltered);

    List<VmIcon> getByDataUrl(String dataUrl);

    /**
     * It deletes specified icon if it is not 'default icon' for some os
     * and it is not use by any vm-like entity.
     * @param iconId id of icon to delete
     */
    void removeIfUnused(Guid iconId);

    /**
     * If the icon is not stored, it stores the icon.
     * @param icon icon data url
     * @return id of the icon in database
     */
    Guid ensureIconInDatabase(final String icon);

    void removeAllUnusedIcons();

    boolean exists(Guid id);
}
