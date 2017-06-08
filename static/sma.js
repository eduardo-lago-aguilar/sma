(function () {

    function config($stateProvider, $locationProvider) {
        $locationProvider.html5Mode({
            enabled: true,
            requireBase: false
        });

        $stateProvider.state("home", {
                url: "/:userAtNetwork",
                templateUrl: "home.html",
                controller: 'HomeController',
                controllerAs: 'hc'
            }
        );
    }

    angular.module("sma", ["ui.router"]).config(["$stateProvider", "$locationProvider", config])


    function HomeController($stateParams, $http) {
        var $$ = this;

        $$.userAtNetwork = $stateParams.userAtNetwork;

        //
        // tracking terms init
        //
        $$.trackingTerms = [];
        updateHashTrackingTerms();

        //
        // tweets init
        //
        $$.tweets = [];

        // bring tracking terms
        retriveTrackingTerms();


        //
        // tracking terms
        //
        function retriveTrackingTerms() {
            $http.get($stateParams.userAtNetwork + "/terms").then(onRetrieveTrackingTermsSuccess);
        }

        function onRetrieveTrackingTermsSuccess(response) {

            function unwrapTrackingTerms(trackingTermsObjects) {
                return _.map(trackingTermsObjects, function (term) {
                    return term.term;
                });
            }

            var terms = unwrapTrackingTerms(response.data).sort();

            if(!_($$.trackingTerms).isEqual(terms)) {
                updateTrackingTerms(terms);
            }
        }

        function updateTrackingTerms(terms) {
            $$.trackingTerms.length = 0;
            Array.prototype.push.apply($$.trackingTerms, terms);
            updateHashTrackingTerms();
            retrieveTweets();
        }

        function updateHashTrackingTerms() {
            $$.hashTrackingTerms = CryptoJS.SHA256($$.trackingTerms.join(", "));
        }

        //
        // tweets
        //
        function retrieveTweets() {
            $http.get($stateParams.userAtNetwork + "/board/" + $$.hashTrackingTerms).then(onRetrieveTweetsSuccess);
        }

        function onRetrieveTweetsSuccess(response) {

            function parseTweets(tweets) {
                return _.map(tweets, function (tweet) {
                    return JSON.parse(tweet.body);
                });
            }
            $$.tweets.length = 0;
            Array.prototype.push.apply($$.tweets, parseTweets(response.data));
        }
    }

    angular.module("sma").controller("HomeController", ["$stateParams", "$http", HomeController]);

})();