package org.eclipse.slm.selfdescriptionservice.datasources.template.methods;

import com.jayway.jsonpath.JsonPath;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


public class CommandValueMethod implements TemplateMethodModelEx {

    private final List<String> outputTypes = List.of("json", "xml", "regex");

    @Override
    public Object exec(List list) throws TemplateModelException {
        var commandConfigs = getCommandConfigs(list);

        try {
            Object commandResult = executeCommand(commandConfigs);

            if (commandConfigs.outputType().isPresent() && commandConfigs.outputCommand().isPresent()) {
                var outputType = commandConfigs.outputType().get();
                var outputCommand = commandConfigs.outputCommand().get();
                commandResult = parseCommandResult(outputType, outputCommand, commandResult.toString());
            }

            return commandResult;
        } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
            return "";
        }

    }

    private static Object parseCommandResult(String outputType, String outputCommand, String commandResult) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Object result = "";
        switch (outputType) {
            case "json":
                var json = JsonPath.parse(commandResult);
                result = JsonPathReader.readSingleValueFromPath(json, outputCommand);
                break;
            case "xml":

                commandResult = commandResult.trim();
                if (commandResult.startsWith("\"") && commandResult.endsWith("\"")) {
                    commandResult = commandResult.substring(1, commandResult.length() - 1);
                }

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                var builder = dbf.newDocumentBuilder();
                var xmlDocument = builder.parse(new InputSource(new StringReader(commandResult)));
                var xPath = XPathFactory.newInstance().newXPath();
                var nodeList = (NodeList) xPath.compile(outputCommand).evaluate(xmlDocument, XPathConstants.NODESET);
                if (nodeList.getLength() > 0){
                    result = nodeList.item(0).getTextContent();
                }
                break;
            case "regex":
                var pattern = Pattern.compile(outputCommand);
                var matcher = pattern.matcher(commandResult);
                var match = matcher.results().findFirst();
                if (match.isPresent()) {
                    result = match.get().group(0);
                }
                break;
            default:
                break;
        }
        return result;
    }

    private static String executeCommand(CommandConfigs commandConfigs) throws IOException {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        ProcessBuilder processBuilder;
        if (isWindows) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", commandConfigs.command());
        } else {
            processBuilder = new ProcessBuilder("/bin/sh", "-c", commandConfigs.command());
        }
        processBuilder.redirectErrorStream(true);
        var p = processBuilder.start();

        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        String result = "";

        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            result = line;
        }
        return result;
    }

    private CommandConfigs getCommandConfigs(List<?> list) throws TemplateModelException {
        String command;
        Optional<String> outputType = Optional.empty();
        Optional<String> outputCommand = Optional.empty();
        if (list.size() == 1) {
            command = list.get(0).toString();
        } else if (list.size() == 3) {
            command = list.get(0).toString();
            outputType = Optional.of(list.get(1).toString());
            outputCommand = Optional.of(list.get(2).toString());

            if (!isOutputTypeValid(outputType.get())) {
                throw new TemplateModelException("Wrong output type parameter.");
            }

            if (outputCommand.get().isEmpty()) {
                throw new TemplateModelException("Wrong output command parameter.");
            }


        } else {
            throw new TemplateModelException("Wrong number of arguments");
        }
        return new CommandConfigs(command, outputType, outputCommand);
    }

    private record CommandConfigs(String command, Optional<String> outputType, Optional<String> outputCommand) {
    }

    public boolean isOutputTypeValid(String type) {
        return this.outputTypes.contains(type);
    }


}
