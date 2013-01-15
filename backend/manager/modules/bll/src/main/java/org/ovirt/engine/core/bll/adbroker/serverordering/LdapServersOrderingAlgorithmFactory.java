package org.ovirt.engine.core.bll.adbroker.serverordering;

import java.util.HashMap;
import java.util.Map;

public class LdapServersOrderingAlgorithmFactory {

    private static LdapServersOrderingAlgorithmFactory instance = new LdapServersOrderingAlgorithmFactory();

    public static LdapServersOrderingAlgorithmFactory getInstance() {
        return instance;
    }

    private Map<OrderingAlgorithmType, LdapServersOrderingAlgorithm> orderingAlgorithms;

    private LdapServersOrderingAlgorithmFactory() {
        orderingAlgorithms = new HashMap<OrderingAlgorithmType, LdapServersOrderingAlgorithm>();
        orderingAlgorithms.put(OrderingAlgorithmType.NO_OP, new LdapServersNoOpOrderingAlgorithm());
        orderingAlgorithms.put(OrderingAlgorithmType.PUT_LAST, new LdapServersPutAtLastPlaceAlgorithm());
    }

    public LdapServersOrderingAlgorithm getOrderingAlgorithm(OrderingAlgorithmType algorithm) {
        return orderingAlgorithms.get(algorithm);
    }
}
