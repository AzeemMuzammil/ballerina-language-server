/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
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

package io.ballerina.flowmodelgenerator.core.model.node;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.flowmodelgenerator.core.model.Codedata;
import io.ballerina.flowmodelgenerator.core.model.NodeBuilder;
import io.ballerina.flowmodelgenerator.core.model.NodeKind;
import io.ballerina.flowmodelgenerator.core.model.Property;
import io.ballerina.flowmodelgenerator.core.model.SourceBuilder;
import io.ballerina.modelgenerator.commons.CommonUtils;
import io.ballerina.modelgenerator.commons.FunctionData;
import io.ballerina.modelgenerator.commons.FunctionDataBuilder;
import io.ballerina.modelgenerator.commons.ModuleInfo;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents vector knowledge base node in the flow model.
 *
 * @since 1.1.0
 */
public class VectorKnowledgeBaseBuilder extends CallBuilder {
    public static final String LABEL = "Vector Knowledge Base";
    public static final String DESCRIPTION = "Vector knowledge bases available in the integration";

    private static final String VECTOR_KNOWLEDGE_BASE_NAME_LABEL = "Vector Knowledge Base Name";
    private static final String VECTOR_KNOWLEDGE_BASE_NAME_LABEL_DOC = "Vector knowledge-base instance name";
    private static final String CHECK_ERROR_DOC = "Terminate on error";

    @Override
    public void setConcreteConstData() {
        metadata().label(LABEL);
    }

    @Override
    protected NodeKind getFunctionNodeKind() {
        return NodeKind.VECTOR_KNOWLEDGE_BASE;
    }

    @Override
    protected FunctionData.Kind getFunctionResultKind() {
        return FunctionData.Kind.VECTOR_KNOWLEDGE_BASE;
    }

    @Override
    public Map<Path, List<TextEdit>> toSource(SourceBuilder sourceBuilder) {
        return sourceBuilder.token().keyword(SyntaxKind.FINAL_KEYWORD).stepOut().newVariable()
                .token().keyword(SyntaxKind.NEW_KEYWORD).stepOut().functionParameters(sourceBuilder.flowNode,
                        Set.of(Property.VARIABLE_KEY, Property.TYPE_KEY, Property.SCOPE_KEY, Property.CHECK_ERROR_KEY))
                .textEdit().acceptImport().build();
    }

    @Override
    public void setConcreteTemplateData(NodeBuilder.TemplateContext context) {
        Codedata codedata = context.codedata();
        ModuleInfo codedataModuleInfo = new ModuleInfo(codedata.org(), codedata.packageName(),
                codedata.module(), codedata.version());

        FunctionData functionData = new FunctionDataBuilder().parentSymbolType(codedata.object())
                .name(codedata.symbol()).moduleInfo(codedataModuleInfo).userModuleInfo(moduleInfo)
                .lsClientLogger(context.lsClientLogger()).functionResultKind(FunctionData.Kind.VECTOR_KNOWLEDGE_BASE)
                .build();

        metadata().label(functionData.packageName()).description(functionData.description())
                .icon(CommonUtils.generateIcon(functionData.org(), functionData.packageName(), functionData.version()));

        codedata().node(NodeKind.VECTOR_KNOWLEDGE_BASE).org(functionData.org()).module(functionData.moduleName())
                .packageName(functionData.packageName()).object(functionData.name())
                .version(functionData.version());

        if (CommonUtils.hasReturn(functionData.returnType())) {
            setReturnTypeProperties(functionData, context, VECTOR_KNOWLEDGE_BASE_NAME_LABEL,
                    VECTOR_KNOWLEDGE_BASE_NAME_LABEL_DOC, false);
        }
        setParameterProperties(functionData);
        properties().scope(Property.GLOBAL_SCOPE).checkError(true, CHECK_ERROR_DOC, false);
    }
}
