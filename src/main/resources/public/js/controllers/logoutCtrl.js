(function() {
  'use strict';

  app.controller('LogoutCtrl', function($scope, $http) {

    $scope.logoutSuccess = false;
    $scope.logoutFailure = false;

    /**
     * Logs the user out, setting success and failure variables appropriately.
     */
    $http.get('/api/v1/logout').success(function(data) {
      $scope.logoutSuccess = true;
    }).error(function(data) {
      $scope.logoutFailure = true;
    });
  });
})();
