package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmInitNetwork;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;

public class VmInitUtils {
    @SuppressWarnings("unchecked")
    public static List<VmInitNetwork> jsonNetworksToList(String jsonNetworks) {
        return (List<VmInitNetwork>)new JsonObjectDeserializer().deserializeOrCreateNew(jsonNetworks, ArrayList.class);
    }

    public static String networkListToJson(List<VmInitNetwork> networkList) {
        return new JsonObjectSerializer().serialize(networkList);
    }
}
