package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * This class is used to represent the updated data that is used in the register query logic on queries that return a
 * List of IVdcQueryable (for example: Search, GetAllDisksByVmID).
 */

// @XmlAccessorType(XmlAccessType.NONE)
@XmlType(namespace = "http://service.engine.ovirt.org", name = "ListIVdcQueryableUpdatedData")
public class ListIVdcQueryableUpdatedData extends IRegisterQueryUpdatedData {
    private static final long serialVersionUID = -2939208206199375205L;

    // @XmlElement(name="Added")
    private java.util.Map<Object, IVdcQueryable> privateAdded;

    public java.util.Map<Object, IVdcQueryable> getAdded() {
        return privateAdded;
    }

    public void setAdded(java.util.Map<Object, IVdcQueryable> value) {
        privateAdded = value;
    }

    // @XmlElement(name="Removed")
    private Collection<Object> privateRemoved;

    public Collection<Object> getRemoved() {
        return privateRemoved;
    }

    public void setRemoved(Collection<Object> value) {
        privateRemoved = value;
    }

    // @XmlElement(name="Updated")
    private java.util.Map<Object, IVdcQueryable> privateUpdated;

    public java.util.Map<Object, IVdcQueryable> getUpdated() {
        return privateUpdated;
    }

    public void setUpdated(java.util.Map<Object, IVdcQueryable> value) {
        privateUpdated = value;
    }

    public ListIVdcQueryableUpdatedData() {
        setAdded(new java.util.HashMap<Object, IVdcQueryable>());
        setRemoved(new java.util.ArrayList<Object>());
        setUpdated(new java.util.HashMap<Object, IVdcQueryable>());
    }

    public ListIVdcQueryableUpdatedData(java.util.Map<Object, IVdcQueryable> added, Collection<Object> removed,
            java.util.Map<Object, IVdcQueryable> updated) {
        setAdded(added);
        setRemoved(removed);
        setUpdated(updated);
    }

    // @XmlElement(name="Faulted")
    private ValueObjectPair privateFaulted;

    public ValueObjectPair getFaulted() {
        return privateFaulted;
    }

    public void setFaulted(ValueObjectPair value) {
        privateFaulted = value;
    }

}
