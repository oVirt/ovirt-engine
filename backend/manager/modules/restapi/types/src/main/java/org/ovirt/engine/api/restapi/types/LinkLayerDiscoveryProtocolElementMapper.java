package org.ovirt.engine.api.restapi.types;

import java.util.Map;

import org.ovirt.engine.api.model.LinkLayerDiscoveryProtocolElement;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.Property;
import org.ovirt.engine.core.common.businessentities.network.Tlv;

public class LinkLayerDiscoveryProtocolElementMapper {
    @Mapping(from = Tlv.class, to = LinkLayerDiscoveryProtocolElement.class)
    public static LinkLayerDiscoveryProtocolElement map(Tlv entity, LinkLayerDiscoveryProtocolElement template) {
        LinkLayerDiscoveryProtocolElement model = template != null ? template :
                new LinkLayerDiscoveryProtocolElement();
        model.setName(entity.getName());
        model.setType(entity.getType());
        model.setOui(entity.getOui());
        model.setSubtype(entity.getSubtype());

        Properties properties = new Properties();

        for (Map.Entry<String, String> entry : entity.getProperties().entrySet()) {
            Property property = new Property();
            property.setName(entry.getKey());
            property.setValue(entry.getValue());
            properties.getProperties().add(property);
        }
        model.setProperties(properties);
        return model;
    }
}
