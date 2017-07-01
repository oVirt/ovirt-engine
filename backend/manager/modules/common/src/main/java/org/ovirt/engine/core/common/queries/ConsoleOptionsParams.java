package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.console.ConsoleOptions;

public class ConsoleOptionsParams extends QueryParametersBase {

    ConsoleOptions options;

    public ConsoleOptionsParams() { }

    public ConsoleOptionsParams(ConsoleOptions options) {
        this.options = options;
    }

    public ConsoleOptions getOptions() {
        return options;
    }
}
