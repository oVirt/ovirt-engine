package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ovirt.engine.core.di.Injector;

public class InjectorRule extends TestWatcher {

    // create a new injector instance
    private final Injector mockedInjector;

    public InjectorRule() {
        mockedInjector = mock(Injector.class);
    }

    private void overrideInjector(Injector mockedInjector) {
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

    @Override
    protected void finished(Description description) {
        super.finished(description);
        overrideInjector(null);
        reset(mockedInjector);
    }

    public <T> void bind(Class<T> pureClsType, T instance) {
        overrideInjector(mockedInjector);
        when(mockedInjector.instanceOf(pureClsType)).thenReturn(instance);
    }

}
