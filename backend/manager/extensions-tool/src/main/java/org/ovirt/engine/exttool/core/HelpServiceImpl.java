package org.ovirt.engine.exttool.core;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ovirt.engine.api.extensions.ExtMap;

public class HelpServiceImpl implements ModuleService {

    private ExtMap context;

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Display help.";
    }

    @Override
    public void setContext(ExtMap context) {
        this.context = context;
    }

    @Override
    public ExtMap getContext() {
        return context;
    }

    @Override
    public void parseArguments(List<String> args) throws Exception {
        System.out.println("Available modules:");
        for(Map.Entry<String, ModuleService> entry : new TreeMap<>(getContext().<Map<String, ModuleService>>get(ContextKeys.MODULES)).entrySet()) {
            System.out.printf(
                String.format("  %-10s - %s%n",
                entry.getKey(),
                entry.getValue().getDescription())
            );
        }
        throw new ExitException("Usage", 0);
    }

    @Override
    public void run() throws Exception {
    }
}
