package org.opencloudengine.dfm.services.configuration;


import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AWSUtils {

    public static AmazonS3 getS3(String accessKey, String secretKey) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AwsClientBuilder.EndpointConfiguration configuration
                = new AwsClientBuilder.EndpointConfiguration("s3.ap-northeast-2.amazonaws.com", "ap-northeast-2");
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProxyProtocol(Protocol.HTTP);
        clientConfiguration.setMaxErrorRetry(0); // 0로 하지 않으면 여러번 호출한다.
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.setCredentials(new AWSStaticCredentialsProvider(awsCreds));
        builder.setEndpointConfiguration(configuration);
        builder.setClientConfiguration(clientConfiguration);
        return builder.build();
    }

}