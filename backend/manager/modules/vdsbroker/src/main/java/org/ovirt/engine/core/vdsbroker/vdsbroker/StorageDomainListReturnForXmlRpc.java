package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public final class StorageDomainListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String DOMLIST = "domlist";

    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("domlist")]
    public String[] mStorageDomainList;

    public StorageDomainListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);

        Object[] temp = (Object[]) innerMap.get(DOMLIST);
        if (temp != null) {
            mStorageDomainList = new String[temp.length];
            for (int i = 0; i < temp.length; i++) {
                mStorageDomainList[i] = (String) temp[i];
            }
        }
    }
}
