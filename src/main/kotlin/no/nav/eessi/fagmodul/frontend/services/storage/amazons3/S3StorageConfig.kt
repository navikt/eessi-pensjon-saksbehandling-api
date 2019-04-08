package no.nav.eessi.fagmodul.frontend.services.storage.amazons3

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = LoggerFactory.getLogger(S3StorageConfig::class.java)

@Configuration
class S3StorageConfig {

    @Value("\${eessi_pensjon_frontend_api_s3_creds_username}")
    lateinit var accessKey: String

    @Value("\${eessi_pensjon_frontend_api_s3_creds_password}")
    lateinit var secretKey: String

    @Value("\${s3_url}")
    lateinit var s3Endpoint: String

    @Value("\${S3_REGION:us-east-1}")
    lateinit var s3Region: String

    @Bean
    fun s3(): AmazonS3 {
        logger.info("Creating AmazonS3 standard client with endpoint: $s3Endpoint")
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(s3Endpoint, s3Region))
                .enablePathStyleAccess()
                .withCredentials(AWSStaticCredentialsProvider(credentials))
                .build()
    }
}