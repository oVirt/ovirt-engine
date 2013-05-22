package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.types.MappingTestHelper.populate;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.utils.MockConfigRule;

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
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractInvertibleMappingTest<F, T, I> extends Assert {

    @Rule
    public MockConfigRule mcr = new MockConfigRule();

    private MappingLocator mappingLocator;
    private Class<F> fromClass;
    private Class<T> toClass;
    private Class<I> inverseClass;

    protected AbstractInvertibleMappingTest(Class<F> fromClass, Class<T> toClass, Class<I> inverseClass) {
        this.fromClass = fromClass;
        this.toClass = toClass;
        this.inverseClass = inverseClass;
    }

    @Mock
    OsRepository osRepository;

    @Before
    public void setUp() {
        mappingLocator = new MappingLocator();
        mappingLocator.populate();
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);
        HashMap<Integer, String> osNames = new HashMap<>(1);
        osNames.put(0, "Unassigned");
        Mockito.when(osRepository.getOsNames()).thenReturn(osNames);
        Mockito.when(osRepository.osNameUpperCasedAndUnderscored("Unassigned")).thenReturn("UNASSIGNED");
    }

    @Test
    public void testRoundtrip() throws Exception {
        setUpConfigExpectations();

        F model = fromClass.cast(populate(fromClass));
        model = postPopulate(model);
        Mapper<F, T> out = mappingLocator.getMapper(fromClass, toClass);
        Mapper<I, F> back = mappingLocator.getMapper(inverseClass, fromClass);
        T to = out.map(model, null);
        I inverse = getInverse(to);
        F transform = back.map(inverse, null);
        verify(model, transform);
    }

    protected void setUpConfigExpectations() {
    }

    protected F postPopulate(F model) {
        return model;
    }

    protected I getInverse(T to) {
        return inverseClass.cast(to);
    }

    protected MappingLocator getMappingLocator() {
        return mappingLocator;
    }

    protected abstract void verify(F model, F transform);
}
