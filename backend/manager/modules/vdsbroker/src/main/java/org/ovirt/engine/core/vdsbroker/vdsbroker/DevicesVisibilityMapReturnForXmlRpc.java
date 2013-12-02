package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;

public class DevicesVisibilityMapReturnForXmlRpc extends StatusReturnForXmlRpc {

    private static final String DEVICES_VISIBILITY = "visible";

    private Map<String, String> devicesVisibilityResult;

    public DevicesVisibilityMapReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        devicesVisibilityResult = (Map<String, String>)innerMap.get(DEVICES_VISIBILITY);
    }

    public Map<String, String> getDevicesVisibilityResult() {
        return devicesVisibilityResult;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.toStringBuilder(devicesVisibilityResult, builder);
        return builder.toString();
    }

}
