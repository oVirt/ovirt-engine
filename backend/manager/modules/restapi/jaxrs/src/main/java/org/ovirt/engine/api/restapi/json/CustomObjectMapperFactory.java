/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.json;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class CustomObjectMapperFactory {

    public static ObjectMapper create() {
        return new ObjectMapper()
                .configure(INDENT_OUTPUT, true)
                .setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()))
                .setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS)
                .registerModule(new SimpleModule() {
                    @Override
                    public void setupModule(SetupContext context) {
                        super.setupModule(context);
                        context.addBeanSerializerModifier(new BeanSerializerModifier() {
                            @Override
                            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                                /*
                                 * xjc generated classes contain isSetFoo() methods which get interpreted
                                 * by Jackson as "setFoo" properties which we don't want serialized.
                                 */
                                return beanProperties.stream()
                                        .filter(property -> !property.getName().startsWith("set")
                                                || !property.getType().isPrimitive()
                                                || !property.getType().getRawClass().isAssignableFrom(boolean.class)
                                        ).collect(Collectors.toList());
                            }
                        });
                    }
                });
    }
}
