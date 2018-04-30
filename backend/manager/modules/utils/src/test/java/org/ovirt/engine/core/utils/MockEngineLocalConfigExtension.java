package org.ovirt.engine.core.utils;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;
import org.ovirt.engine.core.common.utils.Pair;

/**
 * An extension to mock {@link EngineLocalConfig} values in tests.
 *
 * In order to provide the configurations, you must implement a
 * {@code public static Stream<Pair<String, String>> mockEngineLocalConfiguration()} method.
 */
public class MockEngineLocalConfigExtension implements BeforeAllCallback, AfterAllCallback {
    private static final String CONFIG_PROVIDER_METHOD = "mockEngineLocalConfiguration";

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        Map<String, String> newValues = extensionContext
                .getTestClass()
                .flatMap(c -> ReflectionUtils.findMethod(c, CONFIG_PROVIDER_METHOD))
                .map(m -> {
                    try {
                        return (Stream<Pair<String, String>>) m.invoke(null);
                    } catch (Exception e) {
                        return Stream.empty();
                    }
                })
                .orElse(Stream.empty())
                .map(x -> (Pair<String, String>) x)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        EngineLocalConfig.getInstance(newValues);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        EngineLocalConfig.clearInstance();
    }
}
