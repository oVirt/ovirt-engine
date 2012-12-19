package org.ovirt.engine.core.dal.dbbroker.generic;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.OptionBehaviour;
import org.ovirt.engine.core.common.config.OptionBehaviourAttribute;
import org.ovirt.engine.core.common.config.TypeConverterAttribute;

public class DBConfigUtilsTest {
    private DBConfigUtils config;

    @Before
    public void setup() {
        config = new DBConfigUtils(false);
    }

    @Test
    public void testDefaultValues() {
        ConfigValues[] values = ConfigValues.values();

        for (ConfigValues curConfig : values) {
            if (curConfig == ConfigValues.Invalid)
                continue;

            Field configField = null;
            try {
                configField = ConfigValues.class.getField(curConfig.name());
            } catch (Exception e) {
                Assert.fail("Failed to look up" + curConfig.name());
                e.printStackTrace();
            }

            OptionBehaviourAttribute behaviourAttr = configField.getAnnotation(OptionBehaviourAttribute.class);
            if (behaviourAttr != null
                    && (behaviourAttr.behaviour() == OptionBehaviour.Password ||
                            behaviourAttr.behaviour() == OptionBehaviour.DomainsPasswordMap)) {
                continue; // no cert available for password decrypt
            }

            TypeConverterAttribute typeAttr = configField.getAnnotation(TypeConverterAttribute.class);
            assertNotNull("The following field is missing the " + TypeConverterAttribute.class.getSimpleName()
                    + " annotation: " + curConfig.name(), typeAttr);
            Class<?> c = typeAttr.value();

            Object obj = config.GetValue(curConfig, ConfigCommon.defaultConfigurationVersion);

            Assert.assertTrue("null return for " + curConfig.name(), obj != null);
            Assert.assertTrue(
                    curConfig.name() + " is a " + obj.getClass().getName() + " but should be a " + c.getName(),
                    c.isInstance(obj));
        }
    }
}
