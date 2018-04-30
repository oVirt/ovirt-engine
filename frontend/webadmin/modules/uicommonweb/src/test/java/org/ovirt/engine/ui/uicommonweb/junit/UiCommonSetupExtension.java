package org.ovirt.engine.ui.uicommonweb.junit;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * JUnit extension used to set up the necessary UiCommon infrastructure in order to test individual UiCommon models.
 * <p>
 * This extension ensures that hidden expectations of UiCommon models (represented by static method calls) are satisfied
 * and provides an {@linkplain UiCommonSetupExtension.Mocks} interface for stubbing the infrastructure behavior, if necessary.
 * </p>
 * <p>
 * Example usage - UiCommon infrastructure setup <b>per test method</b>:
 * </p>
 *
 * <pre>
 * %40ExtendWith(UiCommonSetupExtension.class)
 * public class MyTest {
 *
 *     // This is optional, but often necessary
 *     &#064;Before
 *     public void stubUiCommonInfra() {
 *         AsyncDataProvider adp = AsyncDataProvider.getInstance();
 *         when(adp.isWindowsOsType(anyInt())).thenReturn(true);
 *     }
 *
 *     // Actual test code to exercise model instance(s)
 *
 * }
 * </pre>
 */
public class UiCommonSetupExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        AsyncDataProvider.setInstance(mock(AsyncDataProvider.class));
        Frontend.setInstance(mock(Frontend.class, RETURNS_DEEP_STUBS));
        TypeResolver.setInstance(mock(TypeResolver.class));
        ConstantsManager.setInstance(mock(ConstantsManager.class, RETURNS_DEEP_STUBS));
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        AsyncDataProvider.setInstance(null);
        Frontend.setInstance(null);
        TypeResolver.setInstance(null);
        ConstantsManager.setInstance(null);
    }
}
