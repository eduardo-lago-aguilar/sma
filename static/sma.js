(function () {

    function config($stateProvider, $locationProvider ) {
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
        $$.trackingTerms = [];

        retriveTrackingTerms();

        function retriveTrackingTerms() {

            function unwrapTrackingTerms(trackingTermsObjects) {
                return _.map(trackingTermsObjects, function (term) {
                    return term.term;
                });
            }

            $http.get($stateParams.userAtNetwork + "/terms").then(function(response){
                updateTrackingTerms(unwrapTrackingTerms(response.data).sort());
            });
        }

        function updateTrackingTerms(terms) {
            $$.trackingTerms.length = 0;
            Array.prototype.push.apply($$.trackingTerms, terms);
            $$.hashTrackingTerms = hashTrackingTerms();
        }

        function hashTrackingTerms() {
            return CryptoJS.SHA256($$.trackingTerms.join(", "));
        }
    }

    angular.module("sma").controller("HomeController", ["$stateParams", "$http", HomeController]);

})();