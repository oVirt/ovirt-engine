package org.ovirt.engine.api.restapi.json;

import java.util.List;

import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.map.ser.BeanSerializerBuilder;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;

/*
 * xjc generated classes contain isSetFoo() methods which get interpreted
 * by Jackson as "setFoo" properties which we don't want serialized.
 */
public class CustomBeanFactory extends CustomSerializerFactory {
    @Override
    protected void processViews(SerializationConfig config, BeanSerializerBuilder builder) {
        super.processViews(config, builder);

        List<BeanPropertyWriter> writersList = builder.getProperties();
        BeanPropertyWriter[] writersArray = writersList.toArray(new BeanPropertyWriter[writersList.size()]);

        for (int i = 0; i < writersArray.length; i++) {
            if (writersArray[i].getName().startsWith("set") &&
                writersArray[i].getPropertyType() == boolean.class) {
                writersArray[i] = null;
            }
        }

        builder.setFilteredProperties(writersArray);
    }
}
