package org.ovirt.engine.core.bll.kubevirt;

import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.util.CallGeneratorParams;
import openshift.io.OpenshiftApi;
import openshift.io.V1Template;
import openshift.io.V1TemplateList;

public class TemplatesMonitoring {
    private static final Logger log = LoggerFactory.getLogger(TemplatesMonitoring.class);

    private Guid clusterId;
    private OpenshiftApi api;
    private final TemplateUpdater templateUpdater;

    public TemplatesMonitoring(ApiClient client, Guid clusterId, TemplateUpdater templateUpdater) {
        this.clusterId = clusterId;
        this.templateUpdater = templateUpdater;
        api = new OpenshiftApi(client);
    }

    public void monitor(SharedInformerFactory sharedInformerFactory) {
        SharedIndexInformer<V1Template> templatesInformer =
                sharedInformerFactory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            return api.listKubevirtTemplateForAllNamespacesCall(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    params.resourceVersion,
                                    params.timeoutSeconds,
                                    params.watch,
                                    null,
                                    null);
                        },
                        V1Template.class,
                        V1TemplateList.class);

        templatesInformer.addEventHandler(
                new ResourceEventHandler<V1Template>() {
                    @Override
                    public void onAdd(V1Template template) {
                        if (templateUpdater.addVmTemplate(template, clusterId)) {
                            log.info("template {} (namespace {}) added!",
                                    template.getMetadata().getName(),
                                    template.getMetadata().getNamespace());
                        }
                    }

                    @Override
                    public void onUpdate(V1Template oldTemplate, V1Template newTemplate) {
                    }

                    @Override
                    public void onDelete(V1Template template, boolean deletedFinalStateUnknown) {
                        if (templateUpdater.removeVmTemplate(template, clusterId)) {
                            log.info("template {} (namespace {}) deleted!",
                                    template.getMetadata().getName(),
                                    template.getMetadata().getNamespace());
                        }
                    }
                });
    }
}
