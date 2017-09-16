package org.ovirt.engine.benchmarks;

import static java.nio.file.attribute.PosixFilePermissions.asFileAttribute;
import static java.nio.file.attribute.PosixFilePermissions.fromString;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;

/**
 * <p> Benchmark's ovirt engine's encryption utility {@link EngineEncryptionUtils}.</p>
 * <p> There is a separate benchmarks per functionality - one for <b>encrypt</b> and one for <b>decrypt</b>.
 * <p> The methods are as close as they can to a real invocation, meaning no extra objects,<br/>
 * aside for {@link Blackhole}, or loops or preparations in the block, just the method invocation.<br/>
 * The benchmarks is avoiding loops on purpose, to not get optimized by the jvm in any way.<br/>
 * The result should be a good index for the actual runtime performance of a code block.</p>
 *
 * @see EngineEncryptionUtils
 */
@BenchmarkMode(Mode.All)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class EncryptionDecryptionBenchmark {

    @Benchmark
    public void encryption(BenchmarkState state, Blackhole blackhole) throws Exception {
        blackhole.consume(EngineEncryptionUtils.encrypt(state.password));
    }

    @Benchmark
    public void decryption(BenchmarkState state, Blackhole blackhole) throws Exception {
        blackhole.consume(EngineEncryptionUtils.decrypt(state.encryptedPassword));
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        private String password = "%A2A&wQX8xdx1hTg";
        private String encryptedPassword;
        private Path tmpKey;

        @Setup
        public void setup() throws IOException, GeneralSecurityException {
            Map<String, String> config = new HashMap<>();
            URL systemResource = ClassLoader.getSystemResource("key.p12");
            tmpKey = Files.createTempFile("benchmarktmp", "", asFileAttribute(fromString("rw-------")));
            Files.copy(systemResource.openStream(), tmpKey, StandardCopyOption.REPLACE_EXISTING);
            config.put("ENGINE_PKI_TRUST_STORE", tmpKey.toString());
            config.put("ENGINE_PKI_TRUST_STORE_TYPE", "JKS");
            config.put("ENGINE_PKI_TRUST_STORE_PASSWORD", "NoSoup4U");
            config.put("ENGINE_PKI_ENGINE_STORE", tmpKey.toString());
            config.put("ENGINE_PKI_ENGINE_STORE_TYPE", "PKCS12");
            config.put("ENGINE_PKI_ENGINE_STORE_PASSWORD", "NoSoup4U");
            config.put("ENGINE_PKI_ENGINE_STORE_ALIAS", "1");
            EngineLocalConfig.getInstance(config);

            // paranoia check
            encryptedPassword = EngineEncryptionUtils.encrypt(password);
            if (!password.equals(EngineEncryptionUtils.decrypt(encryptedPassword))) {
                throw new IllegalStateException("This benchmark is illegal because "
                        + " encrypt/decrypt yields a string which is different than the source");
            }
        }

        @TearDown
        public void teardown() throws IOException {
            Files.delete(tmpKey);
            EngineLocalConfig.clearInstance();
        }
    }
}
