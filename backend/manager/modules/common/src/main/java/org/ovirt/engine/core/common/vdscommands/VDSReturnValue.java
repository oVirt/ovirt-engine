package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.asynctasks.*;
import org.ovirt.engine.core.common.errors.*;

//@XmlSeeAlso({
//    DiskImage.class,
//    VMStatus.class,
//    Guid[].class,
//    //java.util.ArrayList<String>.class,
//    IrsStatus.class,
//    VmStatic.class,
//    //java.util.ArrayList<storage_server_connections>.class,
//    storage_domain_static.class,
//    storage_domain_dynamic.class,
//    storage_domains.class,
//    storage_pool.class,
//    //java.util.HashMap<storage_pool, java.util.ArrayList<storage_domain_static>>.class,
//    VDSError.class,
//    VdcBllErrors.class})

//    @XmlAccessorType(XmlAccessType.NONE)
//    @XmlType(name="VDSReturnValue")
public class VDSReturnValue {
    private boolean _succeeded;
    private String _exceptionString;
    private Object _returnValue;
    private RuntimeException _exceptionObject;

    // @XmlElement
    public Object getReturnValue() {
        return _returnValue;
    }

    public void setReturnValue(Object value) {
        _returnValue = value;
    }

    // @XmlElement
    public String getExceptionString() {
        return _exceptionString;
    }

    public void setExceptionString(String value) {
        _exceptionString = value;
    }

    // @XmlElement
    public boolean getSucceeded() {
        return _succeeded;
    }

    public void setSucceeded(boolean value) {
        _succeeded = value;
    }

    public RuntimeException getExceptionObject() {
        return _exceptionObject;
    }

    public void setExceptionObject(RuntimeException value) {
        _exceptionObject = value;
    }

    // @XmlElement(name="CreationInfo")
    private AsyncTaskCreationInfo privateCreationInfo;

    public AsyncTaskCreationInfo getCreationInfo() {
        return privateCreationInfo;
    }

    public void setCreationInfo(AsyncTaskCreationInfo value) {
        privateCreationInfo = value;
    }

    // @XmlElement(name="VdsError")
    private VDSError privateVdsError;

    public VDSError getVdsError() {
        return privateVdsError;
    }

    public void setVdsError(VDSError value) {
        privateVdsError = value;
    }

    public VDSReturnValue() {
    }
}
