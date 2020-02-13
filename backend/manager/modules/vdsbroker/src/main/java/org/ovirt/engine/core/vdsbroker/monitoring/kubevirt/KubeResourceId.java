package org.ovirt.engine.core.vdsbroker.monitoring.kubevirt;

import org.ovirt.engine.core.common.utils.Pair;

import io.kubernetes.client.models.V1ObjectMeta;

public class KubeResourceId extends Pair<String, String> {

    public KubeResourceId(String namespace, String name) {
        super(namespace, name);
    }

    public KubeResourceId(V1ObjectMeta metadata) {
        super(metadata.getNamespace(), metadata.getName());
    }

    public String getNamespace() {
        return super.getFirst();
    }

    public String getName() {
        return super.getSecond();
    }
}
