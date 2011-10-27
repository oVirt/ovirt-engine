package org.ovirt.engine.api.resteasy.json;

import java.util.List;

import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.map.ser.BeanSerializer;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;

/*
 * xjc generated classes contain isSetFoo() methods which get interpreted
 * by Jackson as "setFoo" properties which we don't want serialized.
 */
public class CustomBeanFactory extends CustomSerializerFactory
{
    @Override
    protected BeanSerializer processViews(SerializationConfig config,
                                          BasicBeanDescription beanDesc,
                                          BeanSerializer ser,
                                          List<BeanPropertyWriter> props) {
        ser = super.processViews(config, beanDesc, ser, props);

        BeanPropertyWriter[] writers = props.toArray(new BeanPropertyWriter[props.size()]);

        for (int i = 0; i < writers.length; i++) {
            if (writers[i].getName().startsWith("set") &&
                writers[i].getPropertyType() == boolean.class) {
                writers[i] = null;
            }
        }

        return ser.withFiltered(writers);
    }
}
