package org.ovirt.engine.core.utils;

import java.util.function.Predicate;

/**
 * A dummy predicate that's allows setting the return value of {@link Predicate#test(Object)}.
 *
 * This class is needed since Mockito cannot properly handle interfaces with default implementation, and requiring the
 * tester to mock {@link Predicate#negate()} or {@link Predicate#and(Predicate)} is unreasonable, and will usually
 * result in duplicating the logic that resides in the class being tested.
 */
public class DummyPredicate<T> implements Predicate<T> {
    private boolean testResult;

    public DummyPredicate() {
        this(false);
    }

    public DummyPredicate(boolean testResult) {
        this.testResult = testResult;
    }

    public void setTestResult(boolean testResult) {
        this.testResult = testResult;
    }

    @Override
    public boolean test(T t) {
        return testResult;
    }
}
