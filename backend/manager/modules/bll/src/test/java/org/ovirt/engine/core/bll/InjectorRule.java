package org.ovirt.engine.core.bll;

import org.junit.rules.TestWatcher;
import org.ovirt.engine.core.di.Injector;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InjectorRule extends TestWatcher {

    // create a new injector instance
    private Injector mockedInjector = mock(Injector.class);

    public InjectorRule() {
        try {
            // set the internal injector
            Field holdingMember = Injector.class.getDeclaredField("injector");
            holdingMember.setAccessible(true);
            holdingMember.set(Injector.class, mockedInjector);
        } catch (Exception e) {
            // if something bad happened the test shouldn't run
            throw new RuntimeException(e);
        }
    }

    public <T> void bind(Class<T> pureClsType, T instance) {
        when(mockedInjector.instanceOf(pureClsType)).thenReturn(instance);
    }

}
