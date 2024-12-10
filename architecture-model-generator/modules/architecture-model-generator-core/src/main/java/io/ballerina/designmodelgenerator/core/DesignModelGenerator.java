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

package io.ballerina.designmodelgenerator.core;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.designmodelgenerator.core.model.Automation;
import io.ballerina.designmodelgenerator.core.model.DesignModel;
import io.ballerina.designmodelgenerator.core.model.Function;
import io.ballerina.designmodelgenerator.core.model.ResourceFunction;
import io.ballerina.designmodelgenerator.core.model.Service;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generate the design model for the default package.
 *
 * @since 2.0.0
 */
public class DesignModelGenerator {

    private final SemanticModel semanticModel;
    private final Module defaultModule;
    private final Path rootPath;
    public static final String MAIN_FUNCTION_NAME = "main";

    public DesignModelGenerator(Package ballerinaPackage) {
        this.defaultModule = ballerinaPackage.getDefaultModule();
        this.semanticModel = this.defaultModule.getCompilation().getSemanticModel();
        this.rootPath = ballerinaPackage.project().sourceRoot();
    }

    public DesignModel generate() {
        IntermediateModel intermediateModel = new IntermediateModel();
        this.defaultModule.documentIds().forEach(d -> {
            ModulePartNode rootNode =  this.defaultModule.document(d).syntaxTree().rootNode();
            CodeAnalyzer codeAnalyzer = new CodeAnalyzer(semanticModel, intermediateModel, rootPath);
            codeAnalyzer.visit(rootNode);
        });

        DesignModel.DesignModelBuilder builder = new DesignModel.DesignModelBuilder();

        if (intermediateModel.functionModelMap.containsKey(MAIN_FUNCTION_NAME)) {
            IntermediateModel.FunctionModel main = intermediateModel.functionModelMap.get(MAIN_FUNCTION_NAME);
            buildConnectionGraph(intermediateModel, main);
            builder.setAutomation(new Automation(MAIN_FUNCTION_NAME, main.displayName, main.location,
                    main.allDependentConnections.stream().toList()));
        }

        for (Map.Entry<String, IntermediateModel.ServiceModel> serviceEntry :
                intermediateModel.serviceModelMap.entrySet()) {
            IntermediateModel.ServiceModel serviceModel = serviceEntry.getValue();
            Set<String> connections = new HashSet<>();
            List<Function> functions = new ArrayList<>();
            serviceModel.otherFunctions.forEach(otherFunction -> {
                buildConnectionGraph(intermediateModel, otherFunction);
                functions.add(new Function(otherFunction.name, otherFunction.location));
                connections.addAll(otherFunction.allDependentConnections);
            });

            List<Function> remoteFunctions = new ArrayList<>();
            serviceModel.remoteFunctions.forEach(remoteFunction -> {
                buildConnectionGraph(intermediateModel, remoteFunction);
                remoteFunctions.add(new Function(remoteFunction.name, remoteFunction.location));
                connections.addAll(remoteFunction.allDependentConnections);
            });

            List<ResourceFunction> resourceFunctions = new ArrayList<>();
            serviceModel.resourceFunctions.forEach(resourceFunction -> {
                buildConnectionGraph(intermediateModel, resourceFunction);
                resourceFunctions.add(new ResourceFunction(resourceFunction.name, resourceFunction.path,
                        resourceFunction.location));
                connections.addAll(resourceFunction.allDependentConnections);
            });
            builder.addService(new Service(serviceEntry.getKey(), serviceModel.location, serviceModel.listener,
                    connections.stream().toList(), functions, remoteFunctions, resourceFunctions));
        }
        return builder
                .setConnections(intermediateModel.connections)
                .setListeners(intermediateModel.listeners)
                .build();
    }

    private void buildConnectionGraph(IntermediateModel intermediateModel,
                                      IntermediateModel.FunctionModel functionModel) {
        Set<String> connections = new HashSet<>();
        if (!functionModel.visited && !functionModel.analyzed) {
            functionModel.visited = true;
            functionModel.dependentFuncs.forEach(dependentFunc -> {
                IntermediateModel.FunctionModel dependentFunctionModel = intermediateModel.functionModelMap
                        .get(dependentFunc);
                if (!dependentFunctionModel.analyzed) {
                    buildConnectionGraph(intermediateModel, dependentFunctionModel);
                }
                connections.addAll(dependentFunctionModel.allDependentConnections);
            });
        }
        functionModel.visited = true;
        functionModel.allDependentConnections.addAll(functionModel.connections);
        functionModel.allDependentConnections.addAll(connections);
        functionModel.analyzed = true;
    }
}
