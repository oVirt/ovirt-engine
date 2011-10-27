package org.ovirt.engine.core.common.backendinterfaces;

import org.ovirt.engine.core.common.utils.IObjectDescriptorContainer;

public class BaseHandler implements IObjectDescriptorContainer {
    /**
     * Returns a boolean indication regarding whether it is allowed to update a specified field of a specified object in
     * a specified status. Example for overriding this function:
     *
     * public override bool CanUpdateField(object obj, string fieldName, Enum status) { VDS vds = obj as VDS; VDSStatus
     * vdsStatus = (VDSStatus)status; switch (vdsStatus) { case Maintenance: ... ... ... default: return
     * base.CanUpdateField(....) } }
     *
     * @param obj
     *            The object to update field in.
     * @param fieldName
     *            The field to update.
     * @param status
     *            The status to consider.
     * @return True if fieldName is allowed for update, false otherwise.
     */
    @Override
    public boolean CanUpdateField(Object obj, String fieldName, Enum<?> status) {
        return true;
    }

}
