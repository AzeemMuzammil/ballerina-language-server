/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.architecturemodelgenerator.extension;

/**
 * Utils for component generation testings.
 *
 * @since 2201.3.4
 */
public class TestUtils {
    public static String replaceStdLibVersionStrings(String source) {
        return source
                .replaceAll("ballerina/http:[0-9].[0-9].[0-9]", "")
                .replaceAll("ballerina/http:http:[0-9].[0-9].[0-9]", "")
                .replaceAll("ballerina/grpc:[0-9].[0-9].[0-9]", "");
    }
}
