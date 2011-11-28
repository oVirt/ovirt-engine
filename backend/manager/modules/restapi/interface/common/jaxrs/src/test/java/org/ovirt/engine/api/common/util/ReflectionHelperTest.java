package org.ovirt.engine.api.common.util;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class ReflectionHelperTest {
    @Test
    public void getClasses() throws ClassNotFoundException, IOException {
        List<Class<?>> classes = ReflectionHelper.getClasses(ReflectionHelper.class.getPackage().getName());

        Assert.assertFalse("ReflectionHelper's package can not be empty since at least Reflectionhelper is in it",
                classes.isEmpty());
        Assert.assertTrue("ReflectionHelper's should be in it's own package", classes.contains(ReflectionHelper.class));
    }
}
