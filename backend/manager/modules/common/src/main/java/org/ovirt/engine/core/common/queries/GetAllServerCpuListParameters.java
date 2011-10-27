package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//[JCommentDataContract]
//public class CanDoActionParameters : VdcQueryParametersBase
//{
//    public CanDoActionParameters(VdcActionType action, object id)
//    {
//        _action = action;
//        _id = id;
//    }

//[JCommentDataMember]
//    private readonly VdcActionType _action;
//    public VdcActionType Action
//    {
//        get { return _action; }
//    }

//[JCommentDataMember]
//    private readonly object _id;
//    public object Id
//    {
//        get { return _id; }
//    }
//}

//[JCommentDataContract]
//public class CanDoActionWithParametersParameters : CanDoActionParameters
//{
//    public CanDoActionWithParametersParameters(VdcActionType action, object id, params object [] additionalParameters)
//        : base (action, id)
//    {
//        _additionalParameters = additionalParameters;
//    }

//[JCommentDataMember]
//    private readonly object[] _additionalParameters;
//    public object[] AdditionalParameters
//    {
//        get { return _additionalParameters; }
//    }
//}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllServerCpuListParameters")
public class GetAllServerCpuListParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -6048741913142095068L;

    public GetAllServerCpuListParameters(Version version) {
        _version = version;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Version")
    private Version _version;

    public Version getVersion() {
        return _version;
    }

    public GetAllServerCpuListParameters() {
    }
}
