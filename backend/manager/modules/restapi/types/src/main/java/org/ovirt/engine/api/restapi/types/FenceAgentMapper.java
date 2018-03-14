package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.compat.Guid;

public class FenceAgentMapper {

    private static final String PORT_OPTION = "port";


    @Mapping(from = Agent.class, to = FenceAgent.class)
    public static FenceAgent map(Agent model, FenceAgent template) {
        FenceAgent entity = template != null ? template : new FenceAgent();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetHost() && model.getHost().isSetId()) {
            entity.setHostId(new Guid(model.getHost().getId()));
        }
        if (model.isSetAddress()) {
            entity.setIp(model.getAddress());
        }
        if (model.isSetOrder()) {
            entity.setOrder(model.getOrder());
        }
        if (model.isSetType()) {
            entity.setType(model.getType());
        }
        if (model.isSetUsername()) {
            entity.setUser(model.getUsername());
        }
        if (model.isSetPassword()) {
            entity.setPassword(model.getPassword());
        }
        if (model.isSetPort()) {
            entity.setPort(model.getPort());
            addPortToOptions(model);
        }
        if (model.isSetOptions()) {
            entity.setOptions(HostMapper.map(model.getOptions(), null));
        }
        if (model.isSetEncryptOptions()) {
            entity.setEncryptOptions(model.isEncryptOptions());
        }
        return entity;
    }


    /**
     * Adds 'port' to options. The engine requires it this way, but conceptually this is wrong and the engine should
     * take care of it and not delegate this responsibility to clients (TODO).
     */
    private static void addPortToOptions(Agent model) {
        if (!model.isSetOptions()) {
            model.setOptions(new Options());
        }
        Option option = new Option();
        option.setName(PORT_OPTION);
        option.setValue(String.valueOf(model.getPort()));
        model.getOptions().getOptions().add(option);
    }


    @Mapping(from = FenceAgent.class, to = Agent.class)
    public static Agent map(FenceAgent entity, Agent template) {
        Agent model = template != null ? template : new Agent();
        model.setId(entity.getId().toString());
        model.setAddress(entity.getIp());
        model.setOptions(entity.getOptions() == null || entity.getOptions().isEmpty() ? null
                : HostMapper.map(entity.getOptionsMap(), null));
        model.setEncryptOptions(entity.getEncryptOptions());
        model.setOrder(entity.getOrder());
        // The password isn't mapped for security reasons:
        // model.setPassword(entity.getPassword());
        model.setUsername(entity.getUser());
        model.setType(entity.getType());
        model.setPort(entity.getPort());
        model.setHost(new Host());
        model.getHost().setId(entity.getHostId().toString());
        return model;
    }
}
