# KafkaSearch
KafkaSearch allows you search your messages in Kafka feed.

By default, Kafka doesn't allows you to search messages in the queue. So, we need to build things for ourselves to cater to our needs.

KafkaSearch is being built to handle the following usecases:

 1. Check whether a message has arrived in the Kafka queue or 
 2. Get a message from Kafka based on the message id/key or
 3. Get all messages arrived in Kafka between a timerange.

---------------

###Compatible With

 - Apache Kafka 0.9
 - Scala 10.4
 - Java 1.8
