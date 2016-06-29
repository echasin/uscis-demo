(function() {
  'use strict';

  describe('Controllers', function() {

    beforeEach(module('guestbook'));

    describe('loginCtrl', function() {

      var $q, $httpBackend, $scope, $controller, $location, deferred, Alerts;
      beforeEach(inject(function(
        _$q_, _$rootScope_, _$controller_, _$location_, _$httpBackend_
      ) {

        $q = _$q_;
        $httpBackend = _$httpBackend_;
        $scope = _$rootScope_.$new();
        $location = _$location_;

        deferred = _$q_.defer();

        spyOn($location, 'path').and.callThrough();
        Alerts = jasmine.createSpyObj('Alerts', ['push']);

        var controller = _$controller_('LoginCtrl', {
          $scope: $scope,
          Alerts: Alerts
        });
      }));

      afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
      });

      it('should set the LoginCtrl initial variables correctly', function() {
        expect($scope.user.username).toEqual('');
        expect($scope.user.password).toEqual('');
      });

      describe('#loginUser', function() {
        it('should send a post request', function() {
          $scope.user = { username: 'user', password: 'pass' }
          $scope.loginUser();

          $httpBackend.expectPOST('/api/v1/login', {
            username: 'user', password: 'pass'
          }).respond(function(method, url, data) {
            return [ 201, {} ];
          });
          $httpBackend.flush();
        });

        it('should push a new alert', function() {
          $scope.user = {
            username: 'foo', password: 'bar'
          };

          $scope.loginUser();

          $httpBackend.expectPOST('/api/v1/login', {
            username: 'foo', password: 'bar'
          }).respond(function(method, url, data) {
            return [ 201, {} ];
          });
          $httpBackend.flush();

          expect(Alerts.push).toHaveBeenCalledTimes(1);
          expect(Alerts.push).toHaveBeenCalledWith({
            type: 'alert-success',
            msg: 'Logged in successfully'
          });
        })
      })

    });

  });

})();
