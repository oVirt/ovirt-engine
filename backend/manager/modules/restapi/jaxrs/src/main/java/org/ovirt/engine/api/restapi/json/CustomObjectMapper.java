/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.json;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

public class CustomObjectMapper extends ObjectMapper {
    public static CustomObjectMapper get() {
        CustomObjectMapper mapper = new CustomObjectMapper();
        mapper.setSerializer(new CustomBeanFactory())
                .includeDefaults(false)
                .indent(true)
                .addSerializationConfig();
        return mapper;
    }

    protected CustomObjectMapper setSerializer(CustomSerializerFactory ser) {
        setSerializerFactory(ser);
        getSerializationConfig().setSerializationView(String.class);
        return this;
    }

    protected CustomObjectMapper includeDefaults(boolean include) {
        getSerializationConfig().setSerializationInclusion(
            include ? JsonSerialize.Inclusion.ALWAYS : JsonSerialize.Inclusion.NON_DEFAULT);
        return this;
    }

    protected CustomObjectMapper indent(boolean indent) {
        configure(SerializationConfig.Feature.INDENT_OUTPUT, indent);
        return this;
    }

    protected CustomObjectMapper addSerializationConfig() {
        // We need the instrospector that takes into account the JAXB annotations,
        // both for the serializer and for the deserializer:
        JaxbAnnotationIntrospector introspector = new JaxbAnnotationIntrospector();

        // Configure the serializer:
        SerializationConfig serCfg = getSerializationConfig()
                .withAnnotationIntrospector(introspector);
        setSerializationConfig(serCfg);

        // Configure the deserializer:
        DeserializationConfig deserCfg = getDeserializationConfig()
                .withAnnotationIntrospector(introspector);
        setDeserializationConfig(deserCfg);

        return this;
    }

}
