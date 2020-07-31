/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under
 * this license, please see the license as well as any agreement you’ve
 * entered into with WSO2 governing the purchase of this software and any
 */
package org.ballerinalang.datamapper.utils;

import java.util.Map;

/**
 * This class is a simple representation of an HTTP response.
 */
public class HttpResponse {
    private String data;
    private int responseCode;

    public HttpResponse(String data, int responseCode) {
        this.data = data;
        this.responseCode = responseCode;
    }

    HttpResponse(String data, int responseCode, Map<String, String> headers) {
        this.data = data;
        this.responseCode = responseCode;
    }

    public String getData() {
        return data;
    }

    public int getResponseCode() {
        return responseCode;
    }
}

