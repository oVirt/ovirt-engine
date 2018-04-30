package org.ovirt.engine.core.utils;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;

/**
 * An extension to mock Configuration values in tests.
 *
 * In order to provide the configurations, you must implement a
 * {@code public static Stream<MockConfigDescriptor<?>> mockConfiguration()} method.
 *
 * If you need more than one set of configurations, or which to use a differently named method, you can override the
 * method's name using the {@link MockedConfig} annotation, either in the class or method level.
 */
public class MockConfigExtension implements BeforeEachCallback, AfterEachCallback {
    private IConfigUtilsInterface mockConfigUtils = mock(IConfigUtilsInterface.class);
    private IConfigUtilsInterface origConfUtils;

    private <T> void mockConfigValue(ConfigValues value, String version, T returnValue) {
        doReturn(returnValue).when(mockConfigUtils).getValue(value, version);
    }

    private <T> void mockConfigValue(MockConfigDescriptor<T> mcd) {
        mockConfigValue(mcd.getValue(), mcd.getVersion(), mcd.getReturnValue());
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        origConfUtils = Config.getConfigUtils();
        Config.setConfigUtils(mockConfigUtils);

        String configDescriptorMethodName =  findAnnotation(extensionContext.getElement(), MockedConfig.class)
                .map(MockedConfig::value)
                .orElse(MockedConfig.DEFAULT_METHOD_NAME);

        extensionContext
                .getTestClass()
                .flatMap(c -> ReflectionUtils.findMethod(c, configDescriptorMethodName))
                .map(m -> {
                    try {
                        return (Stream<MockConfigDescriptor>) m.invoke(null);
                    } catch (Exception e) {
                        return Stream.empty();
                    }
                })
                .orElse(Stream.empty())
                .map(MockConfigDescriptor.class::cast)
                .forEach(this::mockConfigValue);

    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        reset(mockConfigUtils);
        Config.setConfigUtils(origConfUtils);
    }
}
