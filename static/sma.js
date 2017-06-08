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
        this.userAtNetwork = $stateParams.userAtNetwork;

        retriveTrackingTerms();

        function retriveTrackingTerms() {
            $http.get($stateParams.userAtNetwork + "/terms").then(function(response){
                this.trackingTerms = _.map(response.data, function(term){
                    return term.term;
                });
            });
        }
    }

    angular.module("sma").controller("HomeController", ["$stateParams", "$http", HomeController]);

})();