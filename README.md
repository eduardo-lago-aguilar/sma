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

## Use Cases

1. UI issues `PUT` or `DELETE` to `/<user@network>/<term>` indicating that `<user>` wants to track/untrack a given term at `<network>`, minimal acknowledgment is returned. For instance:

  - `PUT /ed@twitter/rockmusic` -> _meaning `ed` wants to `follow` `rockmusic` at `twitter`_
  - `DELETE /ed@twitter/war`    -> _meaning `ed` wants to `forget` about `war` at `twitter`_

2. Command routes (see [CQRS](https://martinfowler.com/bliki/CQRS.html)) receive the request and forward it form of `Digging` message to `Digger` actor. `Digging` message is one of the types `follow` or `forget`

![alt text](https://raw.githubusercontent.com/eduardo-lago-aguilar/sma/master/doc/sma_arch.png "Social Media Aggregator Architecture")


3. `Digger` actor streams `follow` or `forget` message to user corresponding [Kafka Topic](https://kafka.apache.org/documentation/) at the specified network, topic name matches `ed@twitter`. Message in topic is `JSON` serialized on top of binary array

4. A `Profiling` ([Reactive Kafka](https://github.com/akka/reactive-kafka)) actor consumes the `follow` or `forget` messages from user topic (`ed@twitter`)

5. `Profiling` actor stores/removes those terms in/from Redis persistent storage, using `ed@twitter` as key. Tracking terms are store in a Redis set to avoid duplications

6. UI might request tracking terms whenever, issuing for instance `GET /ed@twitter/terms` to queries routes (see [CQRS](https://martinfowler.com/bliki/CQRS.html))

7. Queries routes receives the request and fetches the tracking terms from Redis, using `ed@twitter` as key

8. Queries routes send tracking terms back to UI

9. `TwitterNetworker` ([Reactive Kafka](https://github.com/akka/reactive-kafka)) actor consumes the `follow` or `forget` messages from user topic (`ed@twitter`) as well as `Profiling` actor did, every message arrives to both actors

10. `TwitterNetworker` actor updates its own snapshot of tracking terms, and starts streaming tweets from Twitter

11. `TwitterNetworker` streams tweets to corresponding reply topic, for instance: `ed@twitter_reply`, tweets are serialized using same `JSON` marshallers. Every tweet messages is store along with the corresponding tracking terms

12. `TwitterFeeder` ([Reactive Kafka](https://github.com/akka/reactive-kafka)) actor consumes the tweet stream from reply topic (`ed@twitter_reply`). A `sha256` hash is produced for every tweet's tracking terms (sorted), ensuring a short (and low probability collision) query representation

13. `TwitterFeeder` uses the tweet's `hash_of_tracking_terms` and the tweet `id`, to check if the tweet is already on the list for that query, so tweets are never duplicated for a given set of tracking terms. `<hash_of_tracking_terms>_<tweet_id>` is used as key in Redis

14. `TwitterFeeder` marks the tweet as stored using `<hash_of_tracking_terms>_<tweet_id>` as key

15. If tweet is not stored already then, `TwitterFeeder` stream the tweet to kafka topic named `<hash_of_tracking_terms>`

16. Since UI keeps its own collection of tracking terms, then a Websocket tracking message is sent to `/ed@twittter/tweets` is issued with content = `<hash_of_tracking_terms>`

17. Query routes (see [CQRS](https://martinfowler.com/bliki/CQRS.html)) receive the request and creates 2 actors. A `SocketTracking` representing the Websocket and `ReactiveTweetTracker` that consumers the tweets from the query corresponding kafka topic, that is `<hash_of_tracking_terms>`

18. `ReactiveTweetTracker` consumes the stream of tweet from the kafka topic named `<hash_of_tracking_terms>`

19. `ReactiveTracker` forwards the tweet to `SocketTracker`

20. `SocketTracker` forwards the tweet to the real Websocket, during the process tweets are JSON string encoded

21. Query routes send tweets corresponding to tracking terms back to UI, tweets are JSON decoded and shown

## Install Guide

1. In `application.conf` edit Twitter settings:

```
twitter {
  consumer_key = ""
  consumer_secret = ""
  token = ""
  token_secret = ""
}
```

2. Install Redis

3. Install Kafka and Zookeeper

4. Start Zookeeper and Kafka:

```
$ cd ~/kafka_2.11-0.10.2.0/
$ bin/zookeeper-server-start.sh config/zookeeper.properties
$ bin/kafka-server-start.sh config/server.properties
```

5. Run the application:

```
sbt run
```

6. Open browser at `http://localhost:8080`

## More settings
Go to `application.conf` to tune more settings

## About UI
* AngularJS + Angular UI Router
* CryptoJS
* HTML5 Websocket
* JS-Sorted-Set
* UnderscoreJS
* JQuery

## Thanks to
* [Examples using Akka HTTP with Streaming](https://github.com/calvinlfer/akka-http-streaming-response-examples)
* [ Processing Tweets with Kafka Streams in Scala](https://github.com/jpzk/twitterstream)
