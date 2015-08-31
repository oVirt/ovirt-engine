package org.ovirt.engine.core.bll.network.host.function;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.Function;

public class NetworkNameFromNetworkAttachmentTransformationFunction implements Function<NetworkAttachment, String> {
    /***
     *
     * @param networkAttachment
     * @return network name of the given {@code networkAttachment} in case it has one. otherwise, will return
     *         {@link NetworkAttachment#networkId} as string or null in case it doesn't exist.
     */
    @Override
    public String eval(NetworkAttachment networkAttachment) {
        final String networkName = networkAttachment.getNetworkName();
        if (!StringUtils.isEmpty(networkName)) {
            return networkName;
        }
        final Guid networkId = networkAttachment.getNetworkId();
        return networkId == null ? null : networkId.toString();
    }
}
