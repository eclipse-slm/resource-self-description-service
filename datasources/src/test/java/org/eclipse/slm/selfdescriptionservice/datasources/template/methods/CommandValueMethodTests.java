package org.eclipse.slm.selfdescriptionservice.datasources.template.methods;

import freemarker.template.TemplateModelException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandValueMethodTests {

    @Test
    public void ExecuteCommandAndReadOutput_Successful() throws TemplateModelException {
        var method = new CommandValueMethod();

        var command = "echo test";
        var expectedOutput = "test";

        var result = method.exec(List.of(command));

        assertThat(result).isEqualTo(expectedOutput);
    }

    @Test
    public void ExecuteCommandAndReadJsonOutput_Successful() throws TemplateModelException {
        var method = new CommandValueMethod();

        var command = "echo {\"price\":{\"cheap\":10}}";
        var outputType = "json";
        var outputPath = "$.['price'].cheap";
        var expectedOutput = 10;

        var result = method.exec(List.of(command, outputType, outputPath));

        assertThat(result).isEqualTo(expectedOutput);
    }

    @Test
    public void ExecuteCommandAndReadXMLOutput_Successful() throws TemplateModelException {
        var method = new CommandValueMethod();

        var command = "echo \"<root><element>Some data</element></root>\"";
        var outputType = "xml";
        var outputPath = "/root/element";
        var expectedOutput = "Some data";

        var result = method.exec(List.of(command, outputType, outputPath));

        assertThat(result).isEqualTo(expectedOutput);
    }

    @Test
    public void ExecuteCommandAndReadRegexOutput_Successful() throws TemplateModelException {
        var method = new CommandValueMethod();

        var command = "echo some element";
        var outputType = "regex";
        var outputPath = "element";
        var expectedOutput = "element";

        var result = method.exec(List.of(command, outputType, outputPath));

        assertThat(result).isEqualTo(expectedOutput);
    }

}
