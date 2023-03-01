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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.FilenameUtils;
import org.apache.nifi.annotation.behavior.DynamicProperty;
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
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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

    private final ReentrantLock lock = new ReentrantLock();

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
    protected static final String CONFIGURATION_PROPERTY_PREFIX = "configuration.path.";
    private static final List<PropertyDescriptor> properties;

    static {
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(PROPERTY_ACCESS_KEY);
        props.add(PROPERTY_SECRET_KEY);
        props.add(PROPERTY_BUCKET_NAME);
        properties = Collections.unmodifiableList(props);
    }

    Map<String, ConfigurationObject> configurationObjectMap = new HashMap();
    private ConfigurationContext context;

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

    /**
     * @param context the configuration context
     * @throws InitializationException if unable to create a database connection
     */
    @OnEnabled
    public void onEnabled(final ConfigurationContext context) throws InitializationException {
        this.context = context;

        try {
            reload();
        } catch (Exception e) {
            throw new InitializationException(e);
        }
    }

    @OnDisabled
    public void shutdown() {
        // Shutdown시 캐슁되어 있는 모든 정보를 삭제한다.
        configurationObjectMap.clear();
    }

    @Override
    public ConfigurationObject get(String key) {
        lock.lock();  // block until condition holds
        try {
            return configurationObjectMap.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void reload() {
        lock.lock();  // block until condition holds
        try {
            loadConfiguration();
        } finally {
            lock.unlock();
        }
    }

    private void loadConfiguration() {
        // 캐슁되어 있는 모든 정보를 삭제한다.
        configurationObjectMap.clear();

        // Enable시 AWS S3에서 로딩하기 위한 가장 기본적인 정보를 획득한다.
        final String bucketName = context.getProperty(PROPERTY_BUCKET_NAME).evaluateAttributeExpressions().getValue();
        final String accessKey = context.getProperty(PROPERTY_ACCESS_KEY).evaluateAttributeExpressions().getValue();
        final String secretKey = context.getProperty(PROPERTY_SECRET_KEY).evaluateAttributeExpressions().getValue();

        getLogger().info("AWS S3 ==> Access Key : %s, Secret Key : %s, Bucket Name : %s", new Object[]{
                accessKey, secretKey, bucketName
        });

        // 사용자가 로딩할 Configuration 파일의 위치 정보를 동적 속성을 획득한다.
        final List<PropertyDescriptor> dynamicProperties = context.getProperties()
                .keySet()
                .stream()
                .filter(PropertyDescriptor::isDynamic)
                .collect(Collectors.toList());

        if (dynamicProperties.size() > 0) {
            throw new RuntimeException("AWS S3에서 로딩할 Configuration File을 지정하십시오.");
        }

        AmazonS3 s3 = AWSUtils.getS3(accessKey, secretKey);

        for (PropertyDescriptor prop : dynamicProperties) {
            try {
                if (prop.getName().startsWith(CONFIGURATION_PROPERTY_PREFIX)) {
                    final String objectPath = context.getProperty(prop).evaluateAttributeExpressions().getValue();
                    getLogger().info("Object Path to load : %s", objectPath);

                    S3Object object = s3.getObject(bucketName, objectPath);
                    String body = IOUtils.readAsString(object.getObjectContent(), false);

                    String extension = FilenameUtils.getExtension(objectPath);
                    String objectName = FilenameUtils.getName(objectPath);
                    String prefix = FilenameUtils.getPath(objectPath);

                    ConfigurationObject co = ConfigurationObject.builder()
                            .objectName(objectName)
                            .prefix(prefix)
                            .objectKey(objectPath)
                            .extension(extension)
                            .bucketName(bucketName)
                            .body(body)
                            .build();
                    configurationObjectMap.put(prop.getName(), co);
                }

            } catch (Exception e) {
                throw new RuntimeException("Configuration File Loading Error!", e);
            }
        }
    }
}
