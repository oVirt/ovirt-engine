package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;

public class StatusForXmlRpc {

    private static final String CODE = "code";
    private static final String MESSAGE = "message";

    // [XmlRpcMember("code")]
    public int mCode;
    // [XmlRpcMember("message")]
    public String mMessage;

    @Override
    public String toString() {
        return ObjectDescriptor.toString(this);
    }

    public StatusForXmlRpc(Map<String, Object> innerMap) {
        mCode = (Integer) innerMap.get(CODE);
        mMessage = (String) innerMap.get(MESSAGE);
    }

    // used for backwards compatibility with c#.
    public StatusForXmlRpc() {
    }

}
