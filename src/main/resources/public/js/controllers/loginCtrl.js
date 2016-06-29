(function() {
  'use strict';

  app.controller('LoginCtrl', function($scope, $http, $location, Alerts) {

    $scope.user = {
      username: '',
      password: ''
    };

    /**
     * POSTs user credentials and redirects to the default guestbook upon success.
     */
    $scope.loginUser = function() {
      $http.post('/api/v1/login', $scope.user).success(function(data) {
        Alerts.push({
          type: 'alert-success',
          msg: 'Logged in successfully'
        });

        $location.path('/guestbook/default').replace();
      });
    };
  });
})();
