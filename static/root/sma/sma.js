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

    angular.module("sma", ["ui.router"]).config(["$stateProvider", "$locationProvider", config])


    function HomeController($stateParams, $http, users, trackingTerms) {
        var $$ = this;

        //
        // user @ network init
        //
        $$.userAtNetwork = $stateParams.userAtNetwork;
        $$.user = $stateParams.userAtNetwork.split("@")[0]
        $$.network = $stateParams.userAtNetwork.split("@")[1]
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
        //retrieveTweets();

        var tweetsAddress = "ws://localhost:8080/" + $stateParams.userAtNetwork + "/tweets";
        var socket = new WebSocket(tweetsAddress);
        socket.onopen = function () {
            console.info("connected to" + tweetsAddress);

            socket.onmessage = function (event) {
                console.info("received " + event.data);
            }

            socket.send($$.hashTrackingTerms);
        }

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
                retrieveTweets();
            });
        }

        $$.tapForgetTerm = function (term) {
            $http.delete($stateParams.userAtNetwork + "/" + term).then(function () {
                removeTrackingTerm(term);
                retrieveTweets();
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

    angular.module("sma").controller("HomeController", ["$stateParams", "$http", 'users', 'trackingTerms', HomeController]);

})();