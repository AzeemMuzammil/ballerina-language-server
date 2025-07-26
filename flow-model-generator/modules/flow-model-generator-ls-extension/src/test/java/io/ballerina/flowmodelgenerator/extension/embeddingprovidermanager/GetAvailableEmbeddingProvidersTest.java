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

package io.ballerina.flowmodelgenerator.extension.embeddingprovidermanager;

import com.google.gson.JsonObject;
import io.ballerina.flowmodelgenerator.extension.request.FlowModelAvailableNodesRequest;
import io.ballerina.modelgenerator.commons.AbstractLSTest;
import io.ballerina.tools.text.LinePosition;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static io.ballerina.flowmodelgenerator.extension.TestUtils.assertJsonEqualsIgnoringKey;

/**
 * Test for listing available embedding providers.
 *
 * @since 1.1.0
 */
public class GetAvailableEmbeddingProvidersTest extends AbstractLSTest {

    @DataProvider(name = "data-provider")
    @Override
    protected Object[] getConfigsList() {
        return new Object[][]{
                {Path.of("get_embedding_providers.json")}
        };
    }

    @Override
    @Test(dataProvider = "data-provider")
    public void test(Path config) throws IOException {
        Path configJsonPath = configDir.resolve(config);
        TestConfig testConfig = gson.fromJson(Files.newBufferedReader(configJsonPath), TestConfig.class);
        String filePath = sourceDir.resolve(testConfig.source()).toAbsolutePath().toString();
        FlowModelAvailableNodesRequest request = new FlowModelAvailableNodesRequest(filePath, LinePosition.from(1, 1));
        JsonObject availableEmbeddingProviders = getResponse(request);
        Set<String> ignoredKeys = Set.of("version", "icon");
        if (!assertJsonEqualsIgnoringKey(availableEmbeddingProviders,
                testConfig.expectedEmbeddingProviders(), ignoredKeys)) {
            TestConfig updatedConfig = new TestConfig(testConfig.source(), availableEmbeddingProviders);
            // updateConfig(configJsonPath, updatedConfig);
            Assert.fail(String.format("Failed test: '%s'", configJsonPath));
        }
    }

    @Override
    protected String getResourceDir() {
        return "embedding_provider_manager";
    }

    @Override
    protected Class<? extends AbstractLSTest> clazz() {
        return GetAvailableEmbeddingProvidersTest.class;
    }

    @Override
    protected String getApiName() {
        return "getAvailableEmbeddingProviders";
    }

    /**
     * Represents the test configuration for the flow model getAvailableEmbeddingProviders API.
     *
     * @param source                     The source file path
     * @param expectedEmbeddingProviders The expected set of embedding providers
     */
    private record TestConfig(String source, JsonObject expectedEmbeddingProviders) {

    }
}
