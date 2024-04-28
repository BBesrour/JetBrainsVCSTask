## Description
This is a simple project for the JetBrainsInternship. It is a simple web application
that allows users to view the repositories of a GitHub Organization. The user can
enter the name of the organization and an access token to view the repositories.
The repositories with a README file which contains the word "Hello" are highlighted.
The user can also select whether or no the search should be case-sensitive.

As a demonstration for the actual problem, the user can view and create webhooks for the
organization (Make sure that the access token has the necessary permissions). With
some additional setup, the page gets updated in real-time when a webhook is triggered.

## How to Use
1. Enter the name of the organization and the access token (with the necessary permissions)
2. Click on "Go" to view the repositories of the organization. The repositories with a README file that contains the word "Hello" are highlighted.
### Bonus:
3. You can click on "Get Webhooks" to view the webhooks of the organization.
4. To forward the webhooks to your local machine, you can use [smee.io](https://smee.io/). Follow the instructions below to set it up.
5. To create a webhook, enter the payload URL (from smee.io), the secret, and the events you want to listen to. Click on "Create Webhook" to create the webhook.
Note that the secret is hardcoded in the application for demonstration purposes and has to be the same as the one you enter. The secret can be changed in application.properties or can be left empty.
6. Click on "Connect" on right side of the page to connect to the web socket. The page will update in real-time when a webhook pointing to the application is triggered.

## Setup

The application is a Spring Boot application that uses Thymeleaf for the frontend.
It runs on port 8080 by default.

### Requirements

- Java 17+
- Maven

### Application Setup

1. Clone the repository
2. Run the following command in the root directory of the project:
```shell
mvn spring-boot:run
```

```Note: On IntelliJ IDEA, you can run the application by running JetBrainsVcsTaskApplication```


### Forwards Webhooks to Local Machine
If you are using the application on a server, you can skip this and simply enter the payload URL of the server in the webhook creation form.


The application can listen to GitHub Webhooks on the `/webhook` endpoint. To test this functionality
locally, you can use [smee.io](https://smee.io/). This will allow you to receive the webhooks on your local machine by
creating a tunnel between GitHub and your local machine.

Here is how you can set it up: (Source: [GitHub](https://docs.github.com/en/webhooks/testing-and-troubleshooting-webhooks/testing-webhooks#start-a-local-server))


1. Navigate to [smee.io](https://smee.io/)
2. Click on "Start a new channel"
3. Copy the URL that is generated
4. Install the smee client by running the following command:
```shell
npm install --global smee-client
```
5. Run the following command to start the smee client:
```shell
smee --url <URL-COPIED-FROM-SMEE> --path /webhook --port 8080
```
6. Webhooks sent to the URL will be forwarded to your local machine on port 8080. You can stop the client by pressing `Ctrl+C`