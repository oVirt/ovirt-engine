package org.ovirt.engine.ui.uicommonweb.builders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor.BuilderExecutionFinished;

@SuppressWarnings("unchecked")
public class BuilderExecutorTest {

    private static final String be2 = "be2"; //$NON-NLS-1$

    private static final String be1 = "be1"; //$NON-NLS-1$

    private static final String fe2 = "fe2"; //$NON-NLS-1$

    private static final String fe1 = "fe1"; //$NON-NLS-1$

    private TestingFrontendModel frontendModel;

    private TestingBackendModel backendModel;

    @BeforeEach
    public void setup() {
        frontendModel = new TestingFrontendModel(fe1, fe2);
        backendModel = new TestingBackendModel(be1, be2);
    }

    @Test
    public void bothEmptyBuilderListShouldDoNothing() {
        new BuilderExecutor<>(new FrontendAssert(fe1, fe2)).build(frontendModel, backendModel);

        new BuilderExecutor<>(new BackendAssert(be1, be2)).build(frontendModel, backendModel);

    }

    @Test
    public void frontendToBackendOneBuilder() {
        new BuilderExecutor<>(new BackendAssert(fe1, be2), new Property1Builder()).build(frontendModel, backendModel);
    }

    @Test
    public void frontendToBackendTwoBuilders() {
        new BuilderExecutor<>(new BackendAssert(fe1, fe2),
                new Property1Builder(),
                new Property2Builder())
                .build(frontendModel, backendModel);
    }

    @Test
    public void oneCompositeOneInnerFromBackendToFrontend() {
        BuilderExecutor<TestingFrontendModel, TestingBackendModel> builderExecutor =
                new BuilderExecutor<>(new BackendAssert(fe1, be2), new CompositeBuilder<>(new Property1Builder()));

        builderExecutor.build(frontendModel, backendModel);
    }

    @Test
    public void oneCompositeTwoInnersFromBackendToFrontend() {
        BuilderExecutor<TestingFrontendModel, TestingBackendModel> builderExecutor =
                new BuilderExecutor<>(new BackendAssert(fe1, fe2),
                        new CompositeBuilder<>(new Property1Builder(), new Property2Builder()));

        builderExecutor.build(frontendModel, backendModel);
    }

    @Test
    public void twoCompositesOneInnerFromBackendToFrontend() {

        CompositeBuilder<TestingFrontendModel, TestingBackendModel> composite1 =
                new CompositeBuilder<>(new Property1Builder());

        CompositeBuilder<TestingFrontendModel, TestingBackendModel> composite2 =
                new CompositeBuilder<>(new Property2Builder());

        BuilderExecutor<TestingFrontendModel, TestingBackendModel> builderExecutor =
                new BuilderExecutor<>(new BackendAssert(fe1, fe2), composite1, composite2);

        builderExecutor.build(frontendModel, backendModel);
    }

    @Test
    public void compositeInCompositeFromBackendToFrontend() {
        CompositeBuilder<TestingFrontendModel, TestingBackendModel> composite1 =
                new CompositeBuilder<>(new Property1Builder());

        CompositeBuilder<TestingFrontendModel, TestingBackendModel> composite2 = new CompositeBuilder<>(composite1);

        new BuilderExecutor<>(new BackendAssert(fe1, be2),
                composite2).build(frontendModel, backendModel);
    }

    @Test
    public void compositeInCompositeInsideCompositeFromBackendToFrontend() {
        CompositeBuilder<TestingFrontendModel, TestingBackendModel> composite0 =
                new CompositeBuilder<>(new Property1Builder(), new Property2Builder());

        CompositeBuilder<TestingFrontendModel, TestingBackendModel> composite1 = new CompositeBuilder<>(composite0);

        CompositeBuilder<TestingFrontendModel, TestingBackendModel> composite2 = new CompositeBuilder<>(composite1);

        new BuilderExecutor<>(new BackendAssert(fe1, fe2),
                composite2).build(frontendModel, backendModel);
    }

    @Test
    public void compositeAndThanNormalFromBackendToFrontend() {
        CompositeBuilder<TestingFrontendModel, TestingBackendModel> composite =
                new CompositeBuilder<>(new Property1Builder());

        new BuilderExecutor<>(new BackendAssert(fe1, fe2),
                composite,
                new Property2Builder()).build(frontendModel, backendModel);
    }

    private static class Property1Builder extends BaseSyncBuilder<TestingFrontendModel, TestingBackendModel> {

        @Override
        protected void build(TestingFrontendModel frontend, TestingBackendModel backend) {
            backend.setProperty1(frontend.getProperty1());
        }

    }

    private static class FrontendAssert implements BuilderExecutionFinished<TestingFrontendModel, TestingBackendModel> {

        private String prop1;

        private String prop2;

        public FrontendAssert(String prop1, String prop2) {
            super();
            this.prop1 = prop1;
            this.prop2 = prop2;
        }

        @Override
        public void finished(TestingFrontendModel frontendModel, TestingBackendModel backendModel) {
            assertThat(frontendModel, is(equalTo(new TestingFrontendModel(prop1, prop2))));
        }

    }

    private static class BackendAssert implements BuilderExecutionFinished<TestingFrontendModel, TestingBackendModel> {

        private String prop1;

        private String prop2;

        public BackendAssert(String prop1, String prop2) {
            super();
            this.prop1 = prop1;
            this.prop2 = prop2;
        }

        @Override
        public void finished(TestingFrontendModel frontendModel, TestingBackendModel backendModel) {
            assertThat(backendModel, is(equalTo(new TestingBackendModel(prop1, prop2))));
        }

    }

    private static class Property2Builder extends BaseSyncBuilder<TestingFrontendModel, TestingBackendModel> {

        @Override
        protected void build(TestingFrontendModel frontend, TestingBackendModel backend) {
            backend.setProperty2(frontend.getProperty2());
        }

    }
}
