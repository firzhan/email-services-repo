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

This app can be tried out at the following URL.

####Note


