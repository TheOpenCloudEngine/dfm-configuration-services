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

import java.util.*;
import java.util.stream.Collectors;

import org.apache.nifi.annotation.behavior.SupportsSensitiveDynamicProperties;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.expression.AttributeExpression;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.annotation.behavior.DynamicProperty;

@Tags({"dfm", "configuration"})
@SupportsSensitiveDynamicProperties
@CapabilityDescription("Configuration Controller Service implementation of ConfigurationService.")
@DynamicProperty(
        name = "Configuration property name",
        value = "Configuration property value (object path)",
        expressionLanguageScope = ExpressionLanguageScope.VARIABLE_REGISTRY,
        description = ""
)
public class CloudConfigurationService extends AbstractControllerService implements ConfigurationService {

    protected static final String CONFIGURATION_PROPERTY_PREFIX = "configuration.path.";

    public static final PropertyDescriptor PROPERTY_ACCESS_KEY = new PropertyDescriptor
            .Builder().name("S3 Access Key")
            .displayName("AWS S3 Access Key")
            .description("AWS S3 Access Key를 입력합니다.")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROPERTY_SECRET_KEY = new PropertyDescriptor
            .Builder().name("S3 Secret Key")
            .displayName("AWS S3 Secret Key")
            .description("AWS S3 Secret Key를 입력합니다.")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROPERTY_BUCKET_NAME = new PropertyDescriptor
            .Builder().name("S3 Bucket Name")
            .displayName("AWS S3 Bucket Name")
            .description("AWS S3 Bucket Name을 입력합니다.")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    private static final List<PropertyDescriptor> properties;

    static {
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(PROPERTY_ACCESS_KEY);
        props.add(PROPERTY_SECRET_KEY);
        props.add(PROPERTY_BUCKET_NAME);
        properties = Collections.unmodifiableList(props);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    protected PropertyDescriptor getSupportedDynamicPropertyDescriptor(final String propertyDescriptorName) {
        final PropertyDescriptor.Builder builder = new PropertyDescriptor.Builder()
                .name(propertyDescriptorName)
                .required(false)
                .dynamic(true)
                .addValidator(StandardValidators.createAttributeExpressionLanguageValidator(AttributeExpression.ResultType.STRING, true))
                .addValidator(StandardValidators.ATTRIBUTE_KEY_PROPERTY_NAME_VALIDATOR)
                .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY);
        return builder.build();
    }

    Map<String, ConfigurationObject> configurationObjectMap = new HashMap();

    /**
     * @param context the configuration context
     * @throws InitializationException if unable to create a database connection
     */
    @OnEnabled
    public void onEnabled(final ConfigurationContext context) throws InitializationException {
        // 캐슁되어 있는 모든 정보를 삭제한다.
        configurationObjectMap.clear();

        // Enable시 AWS S3에서 로딩하기 위한 가장 기본적인 정보를 획득한다.
        final String bucketName = context.getProperty(PROPERTY_BUCKET_NAME).evaluateAttributeExpressions().getValue();
        final String accessKey = context.getProperty(PROPERTY_ACCESS_KEY).evaluateAttributeExpressions().getValue();
        final String secretKey = context.getProperty(PROPERTY_SECRET_KEY).evaluateAttributeExpressions().getValue();

        // 사용자가 로딩할 Configuration 파일의 위치 정보를 동적 속성을 획득한다.
        final List<PropertyDescriptor> dynamicProperties = context.getProperties()
                .keySet()
                .stream()
                .filter(PropertyDescriptor::isDynamic)
                .collect(Collectors.toList());

        if (dynamicProperties.size() > 0) {
            throw new InitializationException("AWS S3에서 로딩할 Configuration File을 지정하십시오.");
        }


    }

    @OnDisabled
    public void shutdown() {
        // Shutdown시 캐슁되어 있는 모든 정보를 삭제한다.
        configurationObjectMap.clear();
    }

    @Override
    public String get(String key) {
        return "";
    }

}
