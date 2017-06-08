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


    function HomeController($stateParams) {
        this.userAtNetwork = $stateParams.userAtNetwork;
    }

    angular.module("sma").controller("HomeController", ["$stateParams", HomeController]);

})();