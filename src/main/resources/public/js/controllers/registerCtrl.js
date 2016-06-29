(function() {
  'use strict';

  app.controller('RegisterCtrl', function($scope, $http, $location, Alerts) {

    $scope.user = {
      username: '',
      password: ''
    };

    /**
     * POSTs desired credentials for a new user, redirecting to default guestbook on succcess.
     */
    $scope.registerUser = function () {
      $http.post('/api/v1/register', $scope.user).success(function(data) {
        Alerts.push({
          type: 'alert-success',
          msg: 'You have successfully registered with username' + $scope.user.username + '!'
        });

        $location.path('/guestbook/default').replace();
      });
    };
  });
})();
