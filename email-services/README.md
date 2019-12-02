# email-services

This micro-service based application enables the users to send emails to users via multiple email providers.
 
This service app uses the following email providers.

- [Mail Gun](https://www.mailgun.com/) 
- [Spark Post](https://app.sparkpost.com/dashboard)

Unfortunately, my account got blocked with [Send Grid](https://sendgrid.com/docs/API_Reference/Web_API_v3/index.html)
mail service provider.

This application would be able to fail over between multiple email providers in the event of failure occurs at a single
service provider. If all of the services failed, the pending reference Id of the message would be
moved to a DLC channel.

This app can be tried out at the following [URL](http://ec2-3-15-184-85.us-east-2.compute.amazonaws.com:9001/swagger-ui.html).

***http://ec2-3-15-184-85.us-east-2.compute.amazonaws.com:9001/swagger-ui.html***
#### Note :- Before trying out with the code, users should have been registered at the Mail Service Providrs side.


## Local Set Up

As a prerequisite, an AWS account is required. Afterwards, we have to create 3 different Simple Queue Services (SQS).

- MAIL_PROD_QUEUE - For the production purpose
- MAIL_UAT_QUEUE - Integration Testing Queue
- MAIL_DLC_QUEUE - Dead Letter Channel Queue.

Couple of AWS configuration files should be created under home directory.
Those files are config and credentials files. 
Those files have to be placed under ~/.aws directory.
- [~/.aws/credentials](https://github.com/firzhan/email-services-repo/blob/master/.aws/credentials) - Holds the AWS credentials.
- [~/.aws/config](https://github.com/firzhan/email-services-repo/blob/master/.aws/config) - Holds the configuration information

Afterwards, the code could be checked out from following [location](https://github.com/firzhan/email-services-repo/tree/master/email-services).

The code could be built using the ```mvn clean install``` command.

The integration test could be done by executing the command ```mvn verify -Pintegration```

Eventually the application could be run either as executing the JAR inside the
target directory ```java -jar target/email-services-1.0.jar```or by simply running the command ```mvn spring-boot:run```

## Design

This service is implemented using Spring Boot framework. The users would be able to
try it out via the swagger UI exposed over the link provided above.

The Spring Boot is deployed in the AWS environment and leverages the AWS's
ActiveMQ based Simple Queue Service messaging infrastructure. The service uses
the H2 database as the persistence storage and this can be easily replaced with
a desired RDS. The below diagram details the entire solution design of the system.

![](https://github.com/firzhan/email-services-repo/blob/master/solution.png "Logo Title Text 1")


- Once a new request reaches the services, it persists the message in the database.
- Stores the unique reference ID of the message in the queue.
- Later that message will be picked up by listener for further processing.
- If the processing failed, the message would be stored back in a Dead Letter Channel queue.

AWS's SQS service would be able to auto scale and handle any load of traffic. However, if the
mail generation rate is exponentially high, we could go for Kafka as well.

This architecture could be horizontally scaled as well. 

## REST API.

This service exposes couple of APIs. The real requests that I used to test would be sent in a separate email.

### API POST email/submit

- Request is submitted as a json body to perform the mail sending operation.
- In order to the mail sending to be successful, the intended recipients( To, CC and BCC) should have been registered 
  and verified over their email addresses. Some service providers have the prerequisite of the domain to be verified as 
  well.
  
##### Request

- **to** - (**Mandatory Field**). Array of registered users whom are we addressing directly.
- **subject** - (**Mandatory Field**). Subject of the mail.
- **content** - (**Mandatory Field**). Plain text content of the mail.
- **cc** -  Array of registered users whom are we going to **CC** in the mail.
- **bcc** - Array of registered users whom are we going to **BCC** in the mail.

```
    curl -X POST \
    http://localhost:9001/email/submit \
    -H 'Accept: application/json' \
    -H 'Content-Type: application/json' \
    -d '{
         "to": [{"id": "xxx@xxx.com", "name":"xxx"}, {"id": "test.user3@xxxx.xxx", "name":"xxxxx"}],
          "cc": [{"id": "test.user4@xxxx.com", "name":"xxxxxx"}],
          "bcc": [{"id": "test.user2@xxxx.com", "name":"xxxxxx"}],
          "subject":"Testing",
          "content":"Hello World"
     }'
```








   
  
   
   
    
 **The JSON Payload would be as follow to be tested on a REST client.**

```
{
    "to": [{"id": "admin@firzhan.com", "name":"firzhan007"}, {"id": "test.user3@firzhan.com", "name":"firzhan"}],
    "bcc": [{"id": "test.user2@firzhan.com", "name":"firzhan007"}],
    "subject":"Testing",
    "content":"Hello World"
}

```    
     
##### Response

- **email-store-id** The value of the field is used to check the status of the email in the subsequent API calls. 
- **email-status** Succesful submission would be having the **ENQUEUED** status.
```
 {
    "message": "Email correctly enqueued.",
    "http-status-code": 200,
    "email-store-id": 1,
    "email-status": "ENQUEUED"
}
```


The system has the following set of Email Statuses.
- **PENDING** - The state where the messages are stored before pushing it to a queue.
- **ENQUEUED** - Indicates the messages are pushed to the queue and awaiting to be processed by a listener.
- **SENT** - Indicates that the mail request has been successfully posted to a service provider.
- **DLC** - Indicates that all the service providers have failed to post the message request. Hence has been moved to DLC queue.



### API GET /email/status/{refId}
   
This is used to check the current database status of an already posted mail request.

##### Request

- **refId** - (**Mandatory URL Param**). Value obtained from **email-store-id** field should be used here.


```
    curl -X GET \
    http://localhost:9001/email/status/1 \
     -H 'Accept: application/json' \
```





     
##### Response

```
{
    "message": "Email Status at the time 2019-12-02T13:21:14.931+11:00[Australia/ACT]",
    "http-status-code": 200,
     "email-store-id": 1,
     "email-status": "SENT",
    "original-request": "{\n  \"to\" : [ {\n    \"id\" : \"admin@firzhan.com\",\n    \"name\" : \"firzhan007\"\n  }, {\n    \"id\" : \"test.user3@firzhan.com\",\n    \"name\" : \"firzhan\"\n  } ],\n  \"cc\" : null,\n  \"bcc\" : [ {\n    \"id\" : \"test.user2@firzhan.com\",\n    \"name\" : \"firzhan007\"\n  } ],\n  \"subject\" : \"Testing\",\n  \"content\" : \"Hello World\"\n}"
}
```
 
    
    

    

## TO DO.

- Need to set up the micro-service against a RDS or NoSQL database.

- Need to have a retry mechanism of the messages stored in the DLC queue.

- Need to post check the status of mails after successfully submitting to a mail service provider. This can be achieved,
  by continuously polling the mail service providers endpoint. 
  This would allow us to identify the failures due to other reasons like sending to unauthorized recipients or suspected spamming attempt etc ...

- Improve the test cases and add more integration test cases to test the scenario with DLC as well.

- Having mechanism to determine the availability of the email service providers by continuously polling. 
  Unavailable Service Providers could be removed from the client's list or the circuit breaker state can be
  changed.
