(function () {
    function config($stateProvider) {
        var st = $stateProvider.state;

        st("home", {
                url: "",
                templateUrl: "home.html"
            }
        );
    }

    angular.module("sma", ["ui.router"]).config(["$stateProvider", config])
})();