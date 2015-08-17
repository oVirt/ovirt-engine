package org.ovirt.engine.ui.common.widget.editor.generic;

import java.text.ParseException;

import org.ovirt.engine.ui.common.widget.parser.MemorySizeParser;
import org.ovirt.engine.ui.common.widget.renderer.MemorySizeRenderer;

public class MemorySizeEntityModelTextBox extends EntityModelTextBox<Integer> {

    private final MemorySizeParser parser;

    public MemorySizeEntityModelTextBox() {
        this(new MemorySizeRenderer<Integer>(), new MemorySizeParser());
    }

    public MemorySizeEntityModelTextBox(MemorySizeRenderer renderer, MemorySizeParser parser) {
        super(renderer, parser);
        this.parser = parser;
    }

    @Override
    public Integer getValueOrThrow() throws ParseException {
        // return the parsed value regardless the text is empty or not
        return parser.parse(getText());
    }
}
