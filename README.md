# Social Media Aggregator

An running demo of applied Event-Sourcing and CQRS architectural patterns. Social Media Aggregator (SMA) collects several social APIs entries into a composed stream visualized via user’s boards. Requirements are described in [Challenge](https://raw.githubusercontent.com/eduardo-lago-aguilar/sma/master/doc/redbee-ChallengeSocialmediaaggregator.pdf) document.


## Architecure Guidelines

* Event-Sourcing & CQRS
* Scalable with HA storage (Kafka)
* Resource manager (Zookeeper)
* Not a big deal to have a delay between a tweet post & visualization
* Eventual consistency embraced in favor of Availability and Partitioning Tolerance (CAP)
* Streaming architecture for social media feed gets ingested for scalable & durable store (Akka Streams)
* REST data ingestion via Microservices: follow/forget tracking terms (Akka Http + Actor Model)
* REST data polling/streaming board
* Low latency processing
* Fault isolation, Actor Model & Microservices: minimizes concurrency issues, decouples processing steps via Message-Driven (Akka Actor Model)
* Expressive programming language & framework with appropiate DSL (Scala + Typesafe Ecosystem: Akka, Akka Streams, Akka Http)
* Reactive Actors aware of commits in persistent topics (Kafka + Reactive Kafka)

## Fast Data Architecture Use Cases

![alt text](https://raw.githubusercontent.com/eduardo-lago-aguilar/sma/master/doc/sma_arch.png "Social Media Aggregator Architecture")

