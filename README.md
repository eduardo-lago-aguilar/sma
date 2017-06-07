# Social Media Aggregator

An running demo of applied [Event-Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html) and [CQRS](https://martinfowler.com/bliki/CQRS.html) architectural patterns. Social Media Aggregator (SMA) collects several social APIs entries into a composed stream visualized via user’s boards. Requirements are described in [Challenge](https://raw.githubusercontent.com/eduardo-lago-aguilar/sma/master/doc/redbee-ChallengeSocialmediaaggregator.pdf) document.


## Architecure Guidelines / Tech Cocktail

* [Event-Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html) & [CQRS](https://martinfowler.com/bliki/CQRS.html)
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
* Complementary storage for fast fetching of Twitter feed (Redis)
* JSON serialization for persistent topics (Jackson JSON)
* JSON streaming to UI with Akka Streams (Akka Http Spray Json)

## Fast Data Architecture Use Cases

1. UI issues `PUT/DELETE /<user@network>/<term>` indicating that `<user>` wants to track/untrack a given term at `<network>`, idempotent `HTTP` verbs are used, minimal acknowledgment is returned. For instance:

  - `PUT /ed@twitter/rockmusic`
  - `DELETE /ed@twitter/war`

2. Command routes (see [CQRS](https://martinfowler.com/bliki/CQRS.html)) receive the request and forward it form of `Digging` message to `Digger` actor (`follow` or `forget`)
![alt text](https://raw.githubusercontent.com/eduardo-lago-aguilar/sma/master/doc/sma_arch.png "Social Media Aggregator Architecture")


3. `Digger` actor streams `follow`/`forget` messages to user corresponding [Kafka Topic](https://kafka.apache.org/documentation/), for instance: `ed@twitter`, messages in topic are `JSON` serialized on top of binary array

4. A `Profiling` [Reactive Kafka](https://github.com/akka/reactive-kafka) actor consumes `follow`/`forget` messages from user topic (`ed@twitter`) and stores/removes those terms in/from Redis persistent storage


