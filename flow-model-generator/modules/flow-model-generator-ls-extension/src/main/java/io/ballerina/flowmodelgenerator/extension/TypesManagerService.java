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

package io.ballerina.flowmodelgenerator.extension;

import com.google.gson.JsonElement;
import io.ballerina.flowmodelgenerator.core.TypesManager;
import io.ballerina.flowmodelgenerator.extension.request.TypeListGetRequest;
import io.ballerina.flowmodelgenerator.extension.response.TypeListResponse;
import io.ballerina.projects.Project;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("typesManager")
public class TypesManagerService implements ExtendedLanguageServerService {
    private WorkspaceManager workspaceManager;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @Override
    public Class<?> getRemoteInterface() {
        return null;
    }

    @JsonRequest
    public CompletableFuture<TypeListResponse> getTypes(TypeListGetRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            TypeListResponse response = new TypeListResponse();
            try {
                Path projectPath = Path.of(request.projectPath());
                Project project = this.workspaceManager.loadProject(projectPath);
                TypesManager typesManager = new TypesManager(project.currentPackage());
                JsonElement allTypes = typesManager.getAllTypes();
                response.setTypes(allTypes);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return response;
        });
    }
}
