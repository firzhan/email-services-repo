package com.siteminder.email.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailQueueServicePubSubConfig {

	@Bean
	public QueueMessagingTemplate queueMessagingTemplate(){
		return new QueueMessagingTemplate(amazonSQSAsync());
	}

	public AmazonSQSAsync amazonSQSAsync() {

		return AmazonSQSAsyncClientBuilder.standard().
				withCredentials(new AWSStaticCredentialsProvider(DefaultAWSCredentialsProviderChain.getInstance().getCredentials())).build();

	}
}
