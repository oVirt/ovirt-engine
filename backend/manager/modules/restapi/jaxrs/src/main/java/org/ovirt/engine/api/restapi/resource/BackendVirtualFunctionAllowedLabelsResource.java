package org.ovirt.engine.api.restapi.resource;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.NetworkLabelsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VfsConfigLabelParameters;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.compat.Guid;

public class BackendVirtualFunctionAllowedLabelsResource
    extends AbstractBaseHostNicLabelsResource
    implements NetworkLabelsResource {

    private Guid nicId;
    private String hostId;

    protected BackendVirtualFunctionAllowedLabelsResource(Guid nicId, String hostId) {
        super(nicId, hostId);

        this.nicId = nicId;
        this.hostId = hostId;
    }

    @Override
    protected List<NetworkLabel> getHostNicLabels(Guid hostNicId) {
        final BackendHostNicsResource hostNicsResource = inject(new BackendHostNicsResource(hostId));
        final HostNicVfsConfig vfsConfig = hostNicsResource.findVfsConfig(hostNicId);
        if (vfsConfig == null) {
            return Collections.emptyList();
        }
        final Set<String> networkLabelIds = vfsConfig.getNetworkLabels();
        return networkLabelIds.stream().map(NetworkLabel::new).collect(toList());
    }

    @Override
    protected Response performCreate(String labelId) {
        return performCreate(ActionType.AddVfsConfigLabel,
                new VfsConfigLabelParameters(nicId, labelId),
                new NetworkLabelIdResolver(nicId));
    }

    @Override
    protected AbstractBaseHostNicLabelResource createSingularResource(String labelId) {
        return new BackendVirtualFunctionAllowedLabelResource(labelId, this);
    }

}
