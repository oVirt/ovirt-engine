package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DestroyVmVDSCommandParameters")
public class DestroyVmVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    public DestroyVmVDSCommandParameters(Guid vdsId, Guid vmId, boolean force, boolean gracefully, int secondsToWait) {
        super(vdsId, vmId);
        _force = force;
        _gracefully = gracefully;
        _secondsToWait = secondsToWait;
    }

    @XmlElement
    private boolean _force;
    @XmlElement
    private boolean _gracefully;
    @XmlElement
    private int _secondsToWait;

    public boolean getForce() {
        return _force;
    }

    public int getSecondsToWait() {
        return _secondsToWait;
    }

    public boolean getGracefully() {
        return _gracefully;
    }

    public DestroyVmVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, force=%s, secondsToWait=%s, gracefully=%s",
                super.toString(),
                getForce(),
                getSecondsToWait(),
                getGracefully());
    }
}
