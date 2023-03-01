/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencloudengine.dfm.services.configuration;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

@Tags({"mdf", "configuration"})
@CapabilityDescription("Configuration Service API.")
public interface ConfigurationService extends ControllerService {

    /**
     * Configuration 정보를 반환한다.
     *
     * @param key Configuration 정보를 제공할 Key
     * @return 로딩한 Configuration 정보
     */
    org.opencloudengine.dfm.services.configuration.ConfigurationObject get(String key);

    /**
     * Configuration 정보를 리로딩한다.
     */
    void reload();

}
