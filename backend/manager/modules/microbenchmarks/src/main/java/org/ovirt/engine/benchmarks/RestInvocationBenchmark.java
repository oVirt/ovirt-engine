package org.ovirt.engine.benchmarks;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.ConnectionBuilder;
import org.ovirt.engine.sdk4.builders.EventBuilder;
import org.ovirt.engine.sdk4.services.SystemService;
import org.ovirt.engine.sdk4.types.LogSeverity;

@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.SECONDS)
public class RestInvocationBenchmark {

    private static AtomicInteger id = new AtomicInteger();

    @Benchmark
    public void getVms(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(state.systemService.vmsService().list().send());
    }

    @Benchmark
    public void getHosts(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(state.systemService.hostsService().list().send());
    }

    @Benchmark
    public void addExternalEvent(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(state.systemService.eventsService()
                .add()
                .event(new EventBuilder()
                        .origin("benchmark" + System.nanoTime())
                        .severity(LogSeverity.NORMAL)
                        .description("benchmark")
                        .customId(id.incrementAndGet()))
                .send());
    }

    @State(Scope.Thread)
    public static class BenchmarkState {

        private SystemService systemService;
        private Connection connection;

        @Setup
        public void setup() throws IOException, GeneralSecurityException {
            String user = System.getProperty("benchmarks.api.user", "admin@internal");
            String pass = System.getProperty("benchmarks.api.pass", "123");
            String engineUrl = System.getProperty("benchmarks.api.engineUrl", "http://localhost:8080");

            connection = ConnectionBuilder.connection()
                    .url(engineUrl + "/" + "ovirt-engine/api")
                    .user(user)
                    .password(pass)
                    .insecure(true)
                    .build();

            systemService = connection.systemService();
        }

        @TearDown
        public void teardown() throws Exception {
            connection.close();
        }
    }
}
