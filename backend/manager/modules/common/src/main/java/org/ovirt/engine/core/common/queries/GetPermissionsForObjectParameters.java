package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetPermissionsForObjectParameters")
public class GetPermissionsForObjectParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 4719409151543629037L;

    @XmlElement(name = "ObjectId")
    private Guid objectId;

    /**
     * True to get only the direct permission of an object. False - get implicit permissions on an object example -
     * implicit VM permissions will return the VM, its Cluster, its Datacenter and System permissions.
     */
    @XmlElement(name = "DirectOnly")
    private boolean directOnly = true;

    @XmlElement(name = "VdcObjectType")
    private VdcObjectType vdcObjectType;

    public GetPermissionsForObjectParameters() {
    }

    public GetPermissionsForObjectParameters(Guid objectId) {
        this.objectId = objectId;
    }

    public Guid getObjectId() {
        return objectId;
    }

    public void setObjectId(Guid objectId) {
        this.objectId = objectId;
    }

    public void setDirectOnly(boolean directOnly) {
        this.directOnly = directOnly;
    }

    public boolean getDirectOnly() {
        return directOnly;
    }

    public void setVdcObjectType(VdcObjectType vdcObjectType) {
        this.vdcObjectType = vdcObjectType;
    }

    public VdcObjectType getVdcObjectType() {
        return vdcObjectType;
    }

}
