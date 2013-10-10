/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.resteasy.json;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonProvider extends JacksonJsonProvider {
    public JsonProvider() {
        // Note that we can't specify here the use of JAXB annotations as it is ignored if the mapper is changed later,
        // thus we need to call the parent constructor first, without a mapper, and then create the mapper and pass it
        // to the parent class once it is fully configured:
        super();

        // Create a mapper that uses our custom bean factory:
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializerFactory(new CustomBeanFactory());

        // We need the instrospector that takes into account the JAXB annotations, both for the serializer and for the
        // deserializer:
        JaxbAnnotationIntrospector introspector = new JaxbAnnotationIntrospector();

        // Configure the serializer:
        SerializationConfig serCfg = mapper.getSerializationConfig()
            .withView(String.class)
            .withAnnotationIntrospector(introspector)
            .with(SerializationConfig.Feature.INDENT_OUTPUT)
            .withSerializationInclusion(JsonSerialize.Inclusion.NON_DEFAULT);
        mapper.setSerializationConfig(serCfg);

        // Configure the deserializer:
        DeserializationConfig deserCfg = mapper.getDeserializationConfig()
            .withAnnotationIntrospector(introspector);
        mapper.setDeserializationConfig(deserCfg);

        // Pass the configured mapper to the parent class:
        setMapper(mapper);
    }
}
