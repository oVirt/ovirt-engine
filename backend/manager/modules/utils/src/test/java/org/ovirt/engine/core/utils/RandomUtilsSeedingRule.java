package org.ovirt.engine.core.utils;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rule to improve the work with {@link RandomUtils} in unit tests.
 *
 * First, it logs the random seed used in the test.
 * Second, it allows setting it externally by setting the environment variable test.random.seed.
 * E.g., you could run:
 * <code>
 * mvn clean install -Dtest.random.seed=123
 * </code>
 */
public class RandomUtilsSeedingRule extends TestWatcher {
    private static final Logger log = LoggerFactory.getLogger(RandomUtilsSeedingRule.class);

    private static final String RANDOM_SEED_PROPERTY = "test.random.seed";

    @Override
    public void starting(Description description) {
        String seedProperty = System.getProperty(RANDOM_SEED_PROPERTY);
        Long seed;
        try {
            seed = Long.parseLong(seedProperty);
        } catch (NumberFormatException e) {
            log.info("Property '{}' was not set, using System.currentTimeMillis() as a seed.",
                    RANDOM_SEED_PROPERTY);
            seed = System.currentTimeMillis();
        }
        log.info("Running test with random seed '{}'", seed);
        RandomUtils.instance().setSeed(seed);
    }
}
