package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;

@SuppressWarnings("unchecked")
public final class VGListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String VG_LIST = "vglist";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    public Map<String, Object>[] vgList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(vgList, builder);
        return builder.toString();
    }

    public VGListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] temp = (Object[]) innerMap.get(VG_LIST);
        if (temp != null) {
            vgList = new HashMap[temp.length];
            for (int i = 0; i < temp.length; i++) {
                vgList[i] = (Map<String, Object>) temp[i];
            }
        }
    }

}
