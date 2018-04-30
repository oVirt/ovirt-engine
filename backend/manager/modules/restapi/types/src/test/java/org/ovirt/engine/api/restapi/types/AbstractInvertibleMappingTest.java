package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.types.MappingTestHelper.populate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.RandomUtilsSeedingExtension;

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
@ExtendWith({MockConfigExtension.class, RandomUtilsSeedingExtension.class})
public abstract class AbstractInvertibleMappingTest<F, T, I> {
    private MappingLocator mappingLocator;
    private Class<F> fromClass;
    private Class<T> toClass;
    private Class<I> inverseClass;

    protected AbstractInvertibleMappingTest(Class<F> fromClass, Class<T> toClass, Class<I> inverseClass) {
        this.fromClass = fromClass;
        this.toClass = toClass;
        this.inverseClass = inverseClass;
    }

    @BeforeEach
    public void setUp() {
        mappingLocator = new MappingLocator();
        mappingLocator.populate();
    }

    @Test
    public void testRoundtrip() throws Exception {
        F model = fromClass.cast(populate(fromClass));
        model = postPopulate(model);
        Mapper<F, T> out = mappingLocator.getMapper(fromClass, toClass);
        Mapper<I, F> back = mappingLocator.getMapper(inverseClass, fromClass);
        T to = out.map(model, null);
        I inverse = getInverse(to);
        F transform = back.map(inverse, getModel(null));
        verify(model, transform);
    }

    protected F getModel(F model) {
        return model;
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
