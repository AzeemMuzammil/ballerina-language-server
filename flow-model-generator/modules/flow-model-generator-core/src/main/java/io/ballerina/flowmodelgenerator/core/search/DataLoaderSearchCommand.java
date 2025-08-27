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

package io.ballerina.flowmodelgenerator.core.search;

import io.ballerina.flowmodelgenerator.core.LocalIndexCentral;
import io.ballerina.flowmodelgenerator.core.model.AvailableNode;
import io.ballerina.flowmodelgenerator.core.model.Category;
import io.ballerina.flowmodelgenerator.core.model.Item;
import io.ballerina.modelgenerator.commons.SearchResult;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LineRange;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Handles the search command for data loaders.
 *
 * @since 1.1.1
 */
public class DataLoaderSearchCommand extends SearchCommand {
    private List<Item> cachedDataLoaders;

    public DataLoaderSearchCommand(Project project, LineRange position, Map<String, String> queryMap) {
        super(project, position, queryMap);
    }

    @Override
    protected List<Item> defaultView() {
        return getDataLoaders();
    }

    @Override
    protected List<Item> search() {
        List<Item> dataLoaders = getDataLoaders();
        if (dataLoaders.isEmpty()) {
            return dataLoaders;
        }

        Category dataLoaderCategory = (Category) dataLoaders.getFirst();
        List<Item> loaders = dataLoaderCategory.items();
        List<Item> matchingLoaders = loaders.stream()
                .filter(item -> item instanceof AvailableNode availableNode &&
                        availableNode.codedata().module().contains(query)).toList();

        loaders.clear();
        loaders.addAll(matchingLoaders);
        return List.of(dataLoaderCategory);
    }

    @Override
    protected Map<String, List<SearchResult>> fetchPopularItems() {
        return Collections.emptyMap();
    }

    private List<Item> getDataLoaders() {
        if (cachedDataLoaders == null) {
            cachedDataLoaders = List.copyOf(LocalIndexCentral.getInstance().getDataLoaders());
        }
        return cachedDataLoaders;
    }
}

