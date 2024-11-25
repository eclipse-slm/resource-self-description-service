package org.eclipse.slm.self_description_service.templating.tags;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.TagNode;

public class TimestampTag implements Tag {
    @Override
    public String getName() { return "timestamp"; }

    @Override
    public String getEndTagName() { return null; }

    @Override
    public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
        return String.valueOf(System.currentTimeMillis());
    }
}
