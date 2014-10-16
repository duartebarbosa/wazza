dashboardServices.factory('BootstrapDashboardService', ['$http', '$q',
    function ($http, $q) {
        var service = {};

        service.execute = function () {
            var request = $http({
                url: '/dashboard/bootstrap',
                method: 'GET'
            });

            var deferred = $q.defer();
            deferred.resolve(request);
            return deferred.promise;
        };

        return service;
}])
