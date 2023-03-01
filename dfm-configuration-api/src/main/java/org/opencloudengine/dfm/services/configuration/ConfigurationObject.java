package org.opencloudengine.dfm.services.configuration;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
public class ConfigurationObject {

    public String objectName;
    public String extension;
    public String bucketName;
    public String objectKey;
    public String prefix;
    public String body;
}
