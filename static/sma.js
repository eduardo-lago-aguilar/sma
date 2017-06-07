(function () {

    function config($stateProvider) {
        var st = $stateProvider.state;

        st("home", {
                url: "",
                templateUrl: "home.html",
                controller: 'HomeController',
                controllerAs: 'hc'
            }
        );
    }

    angular.module("sma", ["ui.router"]).config(["$stateProvider", config])


    function HomeController() {
        this.data = "Hellooooo!";
    }

    angular.module("sma").controller("HomeController", [HomeController]);

})();