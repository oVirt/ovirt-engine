package org.ovirt.engine.core.bll;

import java.util.stream.Stream;

import javax.transaction.TransactionManager;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.aaa.SsoSessionUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.EngineSessionDao;
import org.ovirt.engine.core.utils.ExecutorServiceExtension;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.MockEngineLocalConfigExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class, ExecutorServiceExtension.class, MockEngineLocalConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class BaseCommandTest {
    @Mock
    protected EngineSessionDao engineSessionDao;

    @Spy
    @InjectMocks
    protected SessionDataContainer sessionDataContainer;

    @Mock
    protected SsoSessionUtils ssoSessionUtils;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    @InjectedMock
    public TransactionManager transactionManager;

    @SuppressWarnings("unused")
    // used by MockEngineLocalConfigExtension
    public static Stream<Pair<String, String>> mockEngineLocalConfiguration() {
        return Stream.of(
                new Pair<>("ENGINE_PKI", "/etc/pki/ovirt-engine/"),
                new Pair<>("ENGINE_USR", "/usr/share/ovirt-engine/"),
                new Pair<>("ENGINE_VAR", "/var/lib/ovirt-engine/"),
                new Pair<>("ENGINE_LOG", "/var/log/ovirt-engine/")
        );
    }
}
