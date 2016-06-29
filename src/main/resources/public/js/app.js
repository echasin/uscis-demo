'use strict';

var app = angular.module('guestbook', [
  'ngSanitize',
  'ngRoute'
]);

/**
 * Maps URL paths to Angular controllers and html templates.
 */
app.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/logout', {
      controller: 'LogoutCtrl',
      templateUrl: 'partials/logout.html'
    })
    .when('/login', {
      controller: 'LoginCtrl',
      templateUrl: 'partials/login.html'
    })
    .when('/register', {
      controller: 'RegisterCtrl',
      templateUrl: 'partials/register.html'
    })
    .when('/guestbook/:guestbookName', {
      controller: 'GuestbookCtrl',
      templateUrl: 'partials/guestbook.html'
    })
    .when('/', {
      redirectTo: 'guestbook/default'
    })
    .otherwise({ redirectTo: 'guestbook/default' });
}]);
