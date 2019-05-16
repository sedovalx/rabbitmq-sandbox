# rabbitmq-sandbox
A sandbox for the RabbitMQ Java client

You may change the number of nodes connected to RabbitMQ and number of publisher and consumer threads on each node. 
A node holds a single connection to the message broker and creates a channel for each publisher/consumer thread.

## Run instrutions

It is expected that a `rabbitmq-server` is started on the `localhost:5672`. 

```
./gradlew run
```
