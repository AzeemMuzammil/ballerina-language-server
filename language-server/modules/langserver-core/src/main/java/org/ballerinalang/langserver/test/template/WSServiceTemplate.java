/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.langserver.test.template;

import org.ballerinalang.langserver.test.TestGeneratorException;
import org.ballerinalang.langserver.test.template.io.FileTemplate;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConstants;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;
import java.util.Optional;

import static io.netty.util.internal.StringUtil.LINE_FEED;
import static org.ballerinalang.langserver.test.AnnotationConfigsProcessor.isRecordValueExists;
import static org.ballerinalang.langserver.test.AnnotationConfigsProcessor.searchStringField;

/**
 * To represent a Service template.
 */
public class WSServiceTemplate extends AbstractTestTemplate {
    private final String serviceUri;
    private final boolean isSecure;
    private final String serviceUriStrName;
    private final String testServiceFunctionName;

    public WSServiceTemplate(BLangPackage bLangPackage, BLangService service) {
        this.serviceUriStrName = lowerCaseFirstLetter(service.name.value) + "Uri";
        this.testServiceFunctionName = "test" + upperCaseFirstLetter(service.name.value);
        String tempServiceUri = WS + DEFAULT_IP + ":" + DEFAULT_PORT;
        boolean isSecureTemp = false;

        // If Anonymous Endpoint bounded, get `port` from it
        BLangRecordLiteral anonEndpointBind = service.anonymousEndpointBind;
        if (anonEndpointBind != null) {
            Optional<String> optionalPort = searchStringField(HttpConstants.ANN_CONFIG_ATTR_PORT, anonEndpointBind);
            isSecureTemp = isRecordValueExists(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET, anonEndpointBind);
            String protocol = ((isSecureTemp) ? WSS : WS);
            tempServiceUri = optionalPort.map(port -> protocol + DEFAULT_IP + ":" + port).orElse(tempServiceUri);
        }

        // Check for the bounded endpoint to get `port` from it
        List<? extends SimpleVariableReferenceNode> boundEndpoints = service.getBoundEndpoints();
        EndpointNode endpoint = (boundEndpoints.size() > 0) ? bLangPackage.getGlobalEndpoints().stream()
                .filter(ep -> boundEndpoints.get(0).getVariableName().getValue()
                        .equals(ep.getName().getValue()))
                .findFirst().orElse(null) : null;
        if (endpoint != null && endpoint.getConfigurationExpression() instanceof BLangRecordLiteral) {
            BLangRecordLiteral configs = (BLangRecordLiteral) endpoint.getConfigurationExpression();
            Optional<String> optionalPort = searchStringField(HttpConstants.ANN_CONFIG_ATTR_PORT, configs);
            isSecureTemp = isRecordValueExists(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET, configs);
            String protocol = ((isSecureTemp) ? WSS : WS);
            tempServiceUri = optionalPort.map(port -> protocol + ":" + DEFAULT_IP + ":" + port).orElse(tempServiceUri);
        }
        this.isSecure = isSecureTemp;

        // Service base path
        String serviceBasePath = "/" + service.name.value;

        // If service base path overridden by annotations
        for (BLangAnnotationAttachment annotation : service.annAttachments) {
            Optional<String> optionalPath = searchStringField(WebSocketConstants.ANNOTATION_ATTR_PATH, annotation);
            serviceBasePath = optionalPath.orElse("");
        }
        this.serviceUri = tempServiceUri + serviceBasePath;
    }

    /**
     * Renders content into this file template.
     *
     * @param rootFileTemplate root {@link FileTemplate}
     * @throws TestGeneratorException when template population process fails
     */
    @Override
    public void render(FileTemplate rootFileTemplate) throws TestGeneratorException {
        String filename = (isSecure) ? "wssService.bal" : "wsService.bal";
        FileTemplate template = new FileTemplate(filename);
        template.put("testServiceFunctionName", testServiceFunctionName);
        template.put("serviceUriStrName", serviceUriStrName);

        //Append to root template
        rootFileTemplate.append(RootTemplate.PLACEHOLDER_ATTR_DECLARATIONS, getServiceUriDeclaration() + LINE_FEED);
        rootFileTemplate.append(RootTemplate.PLACEHOLDER_ATTR_CONTENT, template.getRenderedContent());
    }

    private String getServiceUriDeclaration() {
        return "string " + serviceUriStrName + " = \"" + serviceUri + "\";";
    }
}
