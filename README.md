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

==============================================================================================
Nodes                             3
Publishers/consumers per node     5/15
Auto ack                          false
Messages persistence              true
Publishing time                   30000 ms
Consuming time                    35822 ms
Consumed messages                 839937
Time to consume, avg              2215.24 ms
Time to consume, 90pct            2808.17 ms
==============================================================================================


==============================================================================================
Nodes                             3
Publishers/consumers per node     3/6
Auto ack                          false
Messages persistence              true
Publishing time                   30000 ms
Consuming time                    32561 ms
Consumed messages                 756238
Time to consume, avg              2200.55 ms
Time to consume, 90pct            2642.89 ms
==============================================================================================


==============================================================================================
Nodes                             3
Publishers/consumers per node     1/3
Auto ack                          false
Messages persistence              true
Publishing time                   30000 ms
Consuming time                    33588 ms
Consumed messages                 795318
Time to consume, avg              2630.24 ms
Time to consume, 90pct            3644.41 ms
==============================================================================================


==============================================================================================
Nodes                             3
Publishers/consumers per node     5/15
Auto ack                          true
Messages persistence              false
Publishing time                   30000 ms
Consuming time                    31325 ms
Consumed messages                 1590910
Time to consume, avg              1362.23 ms
Time to consume, 90pct            1640.26 ms
==============================================================================================


==============================================================================================
Nodes                             3
Publishers/consumers per node     3/6
Auto ack                          true
Messages persistence              false
Publishing time                   30000 ms
Consuming time                    31815 ms
Consumed messages                 1771966
Time to consume, avg              1100.73 ms
Time to consume, 90pct            1377.93 ms
==============================================================================================


==============================================================================================
Nodes                             3
Publishers/consumers per node     1/3
Auto ack                          true
Messages persistence              false
Publishing time                   30000 ms
Consuming time                    30572 ms
Consumed messages                 339922
Time to consume, avg              2729.25 ms
Time to consume, 90pct            5831.12 ms
==============================================================================================


==============================================================================================
Nodes                             1
Publishers/consumers per node     1/3
Auto ack                          false
Messages persistence              true
Publishing time                   30000 ms
Consuming time                    31091 ms
Consumed messages                 1007675
Time to consume, avg              692.30 ms
Time to consume, 90pct            847.67 ms
==============================================================================================


==============================================================================================
Nodes                             1
Publishers/consumers per node     1/3
Auto ack                          true
Messages persistence              false
Publishing time                   30000 ms
Consuming time                    30823 ms
Consumed messages                 2101670
Time to consume, avg              333.61 ms
Time to consume, 90pct            404.87 ms
==============================================================================================
```