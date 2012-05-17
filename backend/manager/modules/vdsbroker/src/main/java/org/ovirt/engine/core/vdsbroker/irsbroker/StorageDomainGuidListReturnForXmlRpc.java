package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;

public final class StorageDomainGuidListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String DOMAINS_LIST = "domainslist";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("domainslist")]
    public String[] mStorageDomainGuidList = new String[0];

    @SuppressWarnings("unchecked")
    public StorageDomainGuidListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        if (innerMap.containsKey(DOMAINS_LIST)) {
            List<String> list = new LinkedList<String>();
            for (Object object : (Object[])innerMap.get(DOMAINS_LIST)) {
                list.add((String) object);
            }
            mStorageDomainGuidList = list.toArray(new String[0]);
        }
    }

}
