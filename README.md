# rabbitmq-sandbox
A sandbox for the RabbitMQ Java client

You may change the number of nodes connected to RabbitMQ and number of publisher and consumer threads on each node. 
A node holds two connections to the message broker - one for publishing, another for consuming, and creates a channel 
for each publisher/consumer thread.

Publishing threads work without any delays, as fast as they can. The only work that they do is to convert `Long` 
timestamps into byte arrays. Consuming threads do the opposite conversion and a little bit more work - appending 
received timestamps into a `ConcurrentLinkedDeque`.

## Run instructions

It is expected that a `rabbitmq-server` is started on the `localhost:5672`. 

```
./gradlew run
```

## Results

Measurements were done on the next hardware
```
JRE: 1.8.0_202-release-1483-b49 x86_64
JVM: OpenJDK 64-Bit Server VM by JetBrains s.r.o
macOS 10.14.4
Processor Name:	Intel Core i7
Processor Speed:	2,2 GHz
Number of Processors:	1
Total Number of Cores:	6
Memory:	32 GB
``` 

Results
```
Enabled persistence, manual ack
==============================================================================================
Nodes                             3
Publishers/consumers per node     3/6
Auto ack                          false
Messages persistence              true
Publishing time                   30000 ms
Consuming time                    33042 ms
Consumed messages                 780206
Time to consume, avg              2343.23 ms
Time to consume, 90pct            3175.99 ms


Enabled persistence, manual ack
==============================================================================================
Nodes                             3
Publishers/consumers per node     1/3
Auto ack                          false
Messages persistence              true
Publishing time                   30000 ms
Consuming time                    33957 ms
Consumed messages                 791117
Time to consume, avg              3308.57 ms
Time to consume, 90pct            4343.35 ms


Enabled persistence, manual ack
==============================================================================================
Nodes                             1
Publishers/consumers per node     5/15
Auto ack                          false
Messages persistence              true
Publishing time                   30000 ms
Consuming time                    31428 ms
Consumed messages                 714052
Time to consume, avg              953.50 ms
Time to consume, 90pct            1151.74 ms


Enabled persistence, manual ack
==============================================================================================
Nodes                             1
Publishers/consumers per node     3/6
Auto ack                          false
Messages persistence              true
Publishing time                   30000 ms
Consuming time                    30956 ms
Consumed messages                 725003
Time to consume, avg              898.60 ms
Time to consume, 90pct            1068.28 ms


Enabled persistence, manual ack
==============================================================================================
Nodes                             1
Publishers/consumers per node     1/3
Auto ack                          false
Messages persistence              true
Publishing time                   30000 ms
Consuming time                    31022 ms
Consumed messages                 858458
Time to consume, avg              812.38 ms
Time to consume, 90pct            969.71 ms


Disabled persistence, auto ack
==============================================================================================
Nodes                             3
Publishers/consumers per node     3/6
Auto ack                          true
Messages persistence              false
Publishing time                   30000 ms
Consuming time                    31413 ms
Consumed messages                 1727756
Time to consume, avg              1189.73 ms
Time to consume, 90pct            1442.25 ms


Disabled persistence, auto ack
==============================================================================================
Nodes                             3
Publishers/consumers per node     1/3
Auto ack                          true
Messages persistence              false
Publishing time                   30000 ms
Consuming time                    30755 ms
Consumed messages                 375060
Time to consume, avg              2364.48 ms
Time to consume, 90pct            6186.21 ms


Disabled persistence, auto ack
==============================================================================================
Nodes                             1
Publishers/consumers per node     5/15
Auto ack                          true
Messages persistence              false
Publishing time                   30000 ms
Consuming time                    30138 ms
Consumed messages                 1178964
Time to consume, avg              0.48 ms
Time to consume, 90pct            0.98 ms


Disabled persistence, auto ack
==============================================================================================
Nodes                             1
Publishers/consumers per node     3/6
Auto ack                          true
Messages persistence              false
Publishing time                   30000 ms
Consuming time                    30132 ms
Consumed messages                 1060578
Time to consume, avg              0.41 ms
Time to consume, 90pct            0.85 ms


Disabled persistence, auto ack
==============================================================================================
Nodes                             1
Publishers/consumers per node     1/3
Auto ack                          true
Messages persistence              false
Publishing time                   30000 ms
Consuming time                    30563 ms
Consumed messages                 2141849
Time to consume, avg              296.26 ms
Time to consume, 90pct            350.35 ms
```