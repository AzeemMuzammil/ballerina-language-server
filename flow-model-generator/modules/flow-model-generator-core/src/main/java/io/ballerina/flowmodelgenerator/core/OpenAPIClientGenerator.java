/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.flowmodelgenerator.core;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.ballerina.flowmodelgenerator.core.utils.CommonUtils;
import io.ballerina.toml.syntax.tree.*;
import io.ballerina.toml.validator.SampleNodeGenerator;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.eclipse.lsp4j.TextEdit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates client from the OpenAPI contract.
 *
 * @since 2.0.0
 */
public class OpenAPIClientGenerator {
    private final Gson gson;
    private final Path oAContractPath;
    private final Path projectPath;
    private boolean isModuleExists = false;
    private static final String DEFAULT_CLIENT_ID = "oas_%s_%s";
    private static final String LS = System.lineSeparator();

    public OpenAPIClientGenerator(Path oAContractPath, Path projectPath) {
        this.gson = new Gson();
        this.oAContractPath = oAContractPath;
        this.projectPath = projectPath;
    }

    public JsonElement genClient(String module) throws IOException {
        Path tomlPath = this.projectPath.resolve("Ballerina.toml");
        TextDocument configDocument = TextDocuments.from(Files.readString(tomlPath));
        SyntaxTree syntaxTree = SyntaxTree.from(configDocument);
        DocumentNode rootNode = syntaxTree.rootNode();
        NodeList<DocumentMemberDeclarationNode> nodeList = rootNode.members();
        NodeList<TableArrayNode> moduleMembers = AbstractNodeFactory.createEmptyNodeList();
        OpenAPINode existingOpenAPINode = null;
        TableArrayNode existingOpenAPINodeX = null;
        for (DocumentMemberDeclarationNode node: nodeList) {
            if (node.kind() != SyntaxKind.TABLE_ARRAY) {
                continue;
            }
            TableArrayNode tableArrayNode = (TableArrayNode) node;
            if (!tableArrayNode.identifier().toSourceCode().equals("tool.openapi")) {
                continue;
            }
            // TODO: We may not need `OpenAPINode` record
            OpenAPINode openAPINode = convertToOpenAPINode(tableArrayNode);
            if (openAPINode.targetModule().equals(module)) {
                existingOpenAPINode = openAPINode;
                existingOpenAPINodeX = tableArrayNode;
                break;
            }
        }
        String id = genId(module);
        this.isModuleExists = existingOpenAPINode != null;
        if (this.isModuleExists) {

        } else {
            TableArrayNode tableArray = SampleNodeGenerator.createTableArray("[[tool.openapi]]", "");
            tableArray.fields().add(0, SampleNodeGenerator.createStringKV("id", genId(module), ""));
            tableArray.fields().add(0, SampleNodeGenerator.createStringKV("targetModule", module, ""));
            tableArray.fields().add(0, SampleNodeGenerator.createStringKV("filePath", oAContractPath.toAbsolutePath().toString(), ""));
            existingOpenAPINodeX = tableArray;
        }

        List<TextEdit> textEdits = new ArrayList<>();
        Map<Path, List<TextEdit>> textEditsMap = new HashMap<>();
        textEditsMap.put(tomlPath, textEdits);
        textEdits.add(new TextEdit(CommonUtils.toRange(rootNode.lineRange()), existingOpenAPINodeX.toSourceCode()));
        return gson.toJsonTree(textEditsMap);
    }

    public boolean isModuleExists() {
        return this.isModuleExists;
    }

    private OpenAPINode convertToOpenAPINode(TableArrayNode tableArrayNode) {
        String id = "";
        String module = "";
        for (KeyValueNode field : tableArrayNode.fields()) {
            String identifier = field.identifier().toSourceCode();
            if (identifier.trim().equals("targetModule")) {
                module = field.value().toSourceCode().trim();
            } else if (identifier.trim().equals("id")) {
                id = field.value().toSourceCode().trim();
            }
        }
        return new OpenAPINode(id, module, tableArrayNode.toSourceCode());
    }

    private String genId(String module) {
        String fileName = oAContractPath.getFileName().toString();
        return String.format(DEFAULT_CLIENT_ID, fileName, module);
    }

    private record OpenAPINode(String id, String targetModule, String source) {}
}
