package org.ovirt.engine.ui.uicommonweb.junit;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetup.Mocks;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Enums;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

/**
 * JUnit TestRule used to set up the necessary UiCommon infrastructure in order to test individual UiCommon models.
 * <p>
 * This TestRule ensures that hidden expectations of UiCommon models (represented by static method calls) are satisfied
 * and provides an {@linkplain UiCommonSetup.Mocks interface} for stubbing the infrastructure behavior, if necessary.
 * <p>
 * Example usage - UiCommon infrastructure setup <b>per test class</b>:
 *
 * <pre>
 * public class MyTest {
 *
 *     &#064;ClassRule
 *     public static UiCommonSetup setup = new UiCommonSetup();
 *
 *     // This is optional, but often necessary
 *     &#064;BeforeClass
 *     public static void stubUiCommonInfra() {
 *         AsyncDataProvider adp = setup.getMocks().asyncDataProvider();
 *         when(adp.isWindowsOsType(anyInt())).thenReturn(true);
 *     }
 *
 *     // Actual test code to exercise model instance(s)
 *
 * }
 * </pre>
 *
 * Example usage - UiCommon infrastructure setup <b>per test method</b>:
 *
 * <pre>
 * public class MyTest {
 *
 *     &#064;Rule
 *     public UiCommonSetup setup = new UiCommonSetup();
 *
 *     // This is optional, but often necessary
 *     &#064;Before
 *     public void stubUiCommonInfra() {
 *         AsyncDataProvider adp = setup.getMocks().asyncDataProvider();
 *         when(adp.isWindowsOsType(anyInt())).thenReturn(true);
 *     }
 *
 *     // Actual test code to exercise model instance(s)
 *
 * }
 * </pre>
 */
public class UiCommonSetup implements TestRule {

    /**
     * Interface exposing UiCommon infrastructure components as mock objects.
     */
    public interface Mocks {

        AsyncDataProvider asyncDataProvider();
        Frontend frontend();
        TypeResolver typeResolver();
        UIConstants uiConstants();
        UIMessages uiMessages();
        Enums enums();

    }

    private Env env;

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                env = new Env();
                env.update(true);
                base.evaluate();
                env.update(false);
                env = null;
            }
        };
    }

    public Mocks getMocks() {
        if (env == null) {
            throw new IllegalStateException("getMocks() is scoped to test rule execution context"); //$NON-NLS-1$
        }
        return env.mocks;
    }

}

class Env {

    private final AsyncDataProvider asyncDataProvider = mock(AsyncDataProvider.class);
    private final Frontend frontend = mock(Frontend.class, RETURNS_DEEP_STUBS);
    private final TypeResolver typeResolver = mock(TypeResolver.class);

    private final ConstantsManager constantsManager = mock(ConstantsManager.class);
    private final UIConstants uiConstants = mock(UIConstants.class);
    private final UIMessages uiMessages = mock(UIMessages.class);
    private final Enums enums = mock(Enums.class);

    final Mocks mocks = new Mocks() {
        @Override
        public AsyncDataProvider asyncDataProvider() {
            return asyncDataProvider;
        }

        @Override
        public Frontend frontend() {
            return frontend;
        }

        @Override
        public TypeResolver typeResolver() {
            return typeResolver;
        }

        @Override
        public UIConstants uiConstants() {
            return uiConstants;
        }

        @Override
        public UIMessages uiMessages() {
            return uiMessages;
        }

        @Override
        public Enums enums() {
            return enums;
        }
    };

    Env() {
        when(constantsManager.getConstants()).thenReturn(uiConstants);
        when(constantsManager.getMessages()).thenReturn(uiMessages);
        when(constantsManager.getEnums()).thenReturn(enums);
    }

    void update(boolean init) {
        AsyncDataProvider.setInstance(init ? asyncDataProvider : null);
        Frontend.setInstance(init ? frontend : null);
        TypeResolver.setInstance(init ? typeResolver : null);
        ConstantsManager.setInstance(init ? constantsManager : null);
    }

}
