package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.ovirt.engine.core.common.errors.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VDSExceptionBase")
public class VDSExceptionBase extends ApplicationException {
    private VDSError privateVdsError;

    public VDSError getVdsError() {
        return privateVdsError;
    }

    public void setVdsError(VDSError value) {
        privateVdsError = value;
    }

    // public VDSExceptionBase(SerializationInfo info, StreamingContext context)
    // {
    // super(info, context);
    // }
    public VDSExceptionBase(String errMessage, RuntimeException baseException) {
        super(errMessage, baseException);
    }

    public VDSExceptionBase(String errMessage) {
        super(errMessage);
    }

    public VDSExceptionBase(VdcBllErrors errCode) {
        VDSError tempVar = new VDSError();
        tempVar.setCode(errCode);
        setVdsError(tempVar);
    }
}
