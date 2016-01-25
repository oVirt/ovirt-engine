/*
Copyright (c) 2010-2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.json;

import java.util.List;

import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.map.ser.BeanSerializerBuilder;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;

/*
 * xjc generated classes contain isSetFoo() methods which get interpreted
 * by Jackson as "setFoo" properties which we don't want serialized.
 */
public class V3CustomBeanFactory extends CustomSerializerFactory {
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
