# Best practices to write better integration testing with spring

## Description

It's a booking application with 3 main features:
* Book a ticket (saved in Mongo database)
* Cancel booking
* Send via Kafka reminder to user 1 hour before departure

A side application called userAccountApplication is used to retrieve user information.

## Technologies used in test:
* TestContainers : For running docker container
* Wiremock server : For configuring external web service behavior