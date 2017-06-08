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
* Idempotent `HTTP` verbs are used

## Fast Data Architecture Use Cases

1. UI issues `PUT` or `DELETE` to `/<user@network>/<term>` indicating that `<user>` wants to track/untrack a given term at `<network>`, minimal acknowledgment is returned. For instance:

  - `PUT /ed@twitter/rockmusic` -> follow
  - `DELETE /ed@twitter/war`    -> forget

2. Command routes (see [CQRS](https://martinfowler.com/bliki/CQRS.html)) receive the request and forward it form of `Digging` message to `Digger` actor. `Digging` message is one of the types `follow` or `forget`

![alt text](https://raw.githubusercontent.com/eduardo-lago-aguilar/sma/master/doc/sma_arch.png "Social Media Aggregator Architecture")


3. `Digger` actor streams `follow` or `forget` message to user corresponding [Kafka Topic](https://kafka.apache.org/documentation/) at the specified network, topic name matches `ed@twitter`. Message in topic is `JSON` serialized on top of binary array

4. A `Profiling` ([Reactive Kafka](https://github.com/akka/reactive-kafka)) actor consumes the `follow` or `forget` messages from user topic (`ed@twitter`)

5. `Profiling` actor stores/removes those terms in/from Redis persistent storage

6. UI might request tracking terms whenever, issuing for instance `GET /ed@twitter/terms` to queries routes (see [CQRS](https://martinfowler.com/bliki/CQRS.html))

7. Queries routes receives the reques and fetches the tracking terms from Redis

8. Queries routes send tracking terms back to UI

9. `TwitterNetworker` ([Reactive Kafka](https://github.com/akka/reactive-kafka)) actor consumes the `follow` or `forget` messages from user topic (`ed@twitter`) as well as `Profiling` actor did, every message arrives both actors

10. Since `TwitterNetworker` actor updates own snapshot of tracking terms, and starts streaming tweets from Twitter using own tracking terms

11. `TwitterNetworker` streams tweets to corresponding reply topic, for instance: `ed@twitter_reply`, tweets are serialized using same `JSON` marshallers. Every tweet messages is store along with the corresponding tracking terms

12. `TwitterFeeder` ([Reactive Kafka](https://github.com/akka/reactive-kafka)) actor consumes the tweet stream from reply topic (`ed@twitter_reply`). A `sha256` hash is produced for every tweet's tracking terms (sorted), ensuring a short and low probality collision query representation

13. `TwitterFeeder` uses the tweet's tracking terms hash and the tweet `id`, to check if the tweet is already on the list for that query, so tweets are never duplicated for a given set of tracking terms. A `<hash_of_tracking_terms>_<tweet_id>` is used as key in Redis

14. If tweet is not stored already then, `TwitterFeeder` stores the tweet the the corresponding query list, using the `<hash_of_tracking_terms>` as key

15. `TwitterFeeder` signals the tweet as stored using `<hash_of_tracking_terms>_<tweet_id>` as key

16. Since UI keeps its own collection of tracking terms, then a `GET /ed@twittter/board/<hash_of_tracking_terms>` is issued

17. Query routes (see [CQRS](https://martinfowler.com/bliki/CQRS.html)) receive the request and stream tweets message back from the corresponding Redis list using `<hash_of_tracking_terms>` as key

18. Query routes send tweets corresponding to tracking terms back to UI