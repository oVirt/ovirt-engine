package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImagesContainterParametersBase")
public class ImagesContainterParametersBase extends ImagesActionsParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = 7926330624136664726L;

    @XmlElement
    private String _drive;

    @XmlElement
    private Guid _containerId = new Guid();

    @XmlElement
    private static final String _defaultDrive = "1";

    public ImagesContainterParametersBase(Guid imageId, String drive, Guid containerId) {
        super(imageId);
        _drive = StringHelper.isNullOrEmpty(drive) ? _defaultDrive : drive;
        _containerId = containerId;
    }

    public String getDrive() {
        return _drive;
    }

    public Guid getContainerId() {
        return _containerId;
    }

    @XmlElement(name = "WipeAfterDelete")
    private boolean privateWipeAfterDelete;

    public boolean getWipeAfterDelete() {
        return privateWipeAfterDelete;
    }

    public void setWipeAfterDelete(boolean value) {
        privateWipeAfterDelete = value;
    }

    public ImagesContainterParametersBase() {
    }
}
