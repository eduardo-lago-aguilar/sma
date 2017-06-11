(function () {

    var unwrap = function (response) {
        return response.data;
    };

    function users($http) {
        return $http.get("/users").then(unwrap);
    }

    function trackingTerms($http, $stateParams) {
        return $http.get($stateParams.userAtNetwork + "/terms").then(unwrap);
    }

    function config($stateProvider, $locationProvider) {
        $locationProvider.html5Mode({
            enabled: true,
            requireBase: false
        });

        $stateProvider.state("home", {
                url: "/:userAtNetwork",
                templateUrl: "root/sma/home.html",
                controller: 'HomeController',
                controllerAs: 'hc',
                resolve: {
                    users: ["$http", users],
                    trackingTerms: ["$http", '$stateParams', trackingTerms]
                }
            }
        );
    }

    angular.module("sma", ["ui.router", "ngSanitize"]).config(["$stateProvider", "$locationProvider", config]);


    function HomeController($rootScope, $timeout, $stateParams, $http, users, trackingTerms) {
        var $$ = this;

        //
        // user @ network init
        //
        $$.userAtNetwork = $stateParams.userAtNetwork;
        $$.user = $stateParams.userAtNetwork.split("@")[0];
        $$.network = $stateParams.userAtNetwork.split("@")[1];
        $$.users = _.map(users, function (user) {
            return user.user;
        });

        //
        // tracking terms init
        //
        $$.trackingTermsSet = new SortedSet();
        $$.trackingTerms = $$.trackingTermsSet.toArray();
        insertTrackingTerms(_.map(trackingTerms, function (term) {
            return term.term;
        }));

        //
        // tweets init
        //
        $$.tweets = [];
        $$.tweetsHash = {};
        $$.count = $$.tweets.length;

        var socket;
        var timer = null;

        startTracking();

        function startTracking() {
            socket = createSocket();
        }

        function websocketAddress(s) {
            var l = window.location;
            return ((l.protocol === "https:") ? "wss://" : "ws://") + l.host + l.pathname + s;
        }

        function createSocket() {
            var socket = new WebSocket(websocketAddress($stateParams.userAtNetwork + "/tweets"));
            socket.onopen = function () {
                console.info("connection opened, sending tracking request " + $$.hashTrackingTerms);
                socket.onmessage = receiveTweet;
                socket.send($$.hashTrackingTerms);
            };
            socket.onclose = function () {
                console.error("connection closed, scheduling tracking request in a few seconds")
                if (timer != null) {
                    $timeout.cancel(timer);
                }
                timer = $timeout(startTracking(), 8000);
            };
            return socket;
        };

        function receiveTweet(event) {
            var encodedTweet = JSON.parse(event.data);
            var tweet = JSON.parse(encodedTweet.body);

            tweet.created_at = Date.parse(tweet.created_at);
            tweet.text = twttr.txt.autoLink(twttr.txt.htmlEscape(tweet.text));

            if ($$.tweetsHash[tweet.id] == undefined) {
                $$.tweets.unshift(tweet);
                $$.count = $$.tweets.length;
                $$.tweetsHash[tweet.id] = true;
                $rootScope.$applyAsync();
            }
        };

        //
        // tracking terms
        //
        function insertTrackingTerms(trackingTerms) {
            trackingTerms.forEach(function (term) {
                $$.trackingTermsSet.insert(term);
            });
            updateTrackingTerms();
        }

        function removeTrackingTerm(term) {
            $$.trackingTermsSet.remove(term);
            updateTrackingTerms();
        }

        function updateTrackingTerms() {
            $$.trackingTerms.length = 0;
            Array.prototype.push.apply($$.trackingTerms, $$.trackingTermsSet.toArray().sort());
            $$.hashTrackingTerms = CryptoJS.SHA256($$.trackingTerms.join(", "));
        }

        $$.tapFollowTerm = function () {
            var term = $$.term;
            $http.put($stateParams.userAtNetwork + "/" + term).then(function () {
                insertTrackingTerms([term]);
                $$.term = "";
                socket = createSocket();
            });
        }

        $$.tapForgetTerm = function (term) {
            $http.delete($stateParams.userAtNetwork + "/" + term).then(function () {
                removeTrackingTerm(term);
                socket = createSocket();
            });
        }

        //
        // tweets
        //
        function retrieveTweets() {
            $http.get($stateParams.userAtNetwork + "/board/" + $$.hashTrackingTerms).then(unwrap).then(onRetrieveTweetsSuccess);
        }

        function onRetrieveTweetsSuccess(response) {
            $$.tweets.length = 0;
            Array.prototype.push.apply($$.tweets, _.map(response, function (tweet) {
                return JSON.parse(tweet.body);
            }));
        }
    }

    angular.module("sma").controller("HomeController", ["$rootScope", "$timeout", "$stateParams", "$http", 'users', 'trackingTerms', HomeController]);

})();