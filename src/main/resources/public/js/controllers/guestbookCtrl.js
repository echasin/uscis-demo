(function() {
  'use strict';

  app.controller('GuestbookCtrl', function($scope, $http, $location, $routeParams, Alerts) {

    function init() {
      $scope.guestbookName = $routeParams['guestbookName'];
      retrieveGuestbook($routeParams['guestbookName']);
      $scope.alerts = Alerts.list();
    }

    /**
     * GETs a list of messages from the server for the given guestbook and sets appropriate scope
     * variables.
     * @param {string} guestbookName The name of the guestbook to get messages for.
     */
    function retrieveGuestbook(guestbookName) {
      var url = '/api/v1/messages/' + encodeURIComponent(guestbookName);
      $http.get(url).success(function(data) {
        var resultName = data.results.length > 0 ? data.results[0].guestbook : guestbookName;
        $scope.greetings = data.results;
        $scope.guestbookName = resultName;
        $scope.currentGuestbookName = resultName;
      });
    }

    init();

    /**
     * POSTs the entered message into the guestbook and loads the guestbook messages on success.
     */
    $scope.submitForm = function () {
      var url = '/api/v1/messages/' + encodeURIComponent($scope.guestbookName);
      $http.post(url, {
        'message': $scope.content, 'author': $scope.guestbookUser,
        'date': null, 'guestbook': $scope.guestbookName
      }).success(function(data) {
        var resultName = data.results.length > 0 ? data.results[0].guestbook : $scope.guestbookName;
        $location.path('guestbook/' + resultName);
        retrieveGuestbook(resultName);
        $scope.content = '';
      });
    };
  });

})();
