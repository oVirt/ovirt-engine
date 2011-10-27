package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.core.common.config.Config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.ovirt.engine.api.restapi.types.MappingTestHelper.populate;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Test invertible mappings, by mapping the outward followed by the inverse
 * transform, then comparing the original model with the doubly transformed
 * instance.
 *
 * @param <F>
 *            from type
 * @param <T>
 *            to type
 * @param <I>
 *            inverse type (may be identical to T)
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Config.class })
public abstract class AbstractInvertibleMappingTest<F, T, I> extends Assert {

    private MappingLocator mappingLocator;
    private Class<F> fromClass;
    private Class<T> toClass;
    private Class<I> inverseClass;

    protected AbstractInvertibleMappingTest(Class<F> fromClass, Class<T> toClass, Class<I> inverseClass) {
        this.fromClass = fromClass;
        this.toClass = toClass;
        this.inverseClass = inverseClass;
    }

    @Before
    public void setUp() {
        mappingLocator = new MappingLocator();
        mappingLocator.populate();
    }

    @Test
    public void testRoundtrip() throws Exception {
        mockStatic(Config.class);
        setUpConfigExpectations();
        replayAll();

        F model = fromClass.cast(populate(fromClass));
        model = postPopulate(model);
        Mapper<F, T> out = mappingLocator.getMapper(fromClass, toClass);
        Mapper<I, F> back = mappingLocator.getMapper(inverseClass, fromClass);
        T to = out.map(model, null);
        I inverse = getInverse(to);
        F transform = back.map(inverse, null);
        verify(model, transform);
        verifyAll();
    }

    protected void setUpConfigExpectations() {
    }

    protected F postPopulate(F model) {
        return model;
    }

    protected I getInverse(T to) {
        return inverseClass.cast(to);
    }

    protected abstract void verify(F model, F transform);
}
