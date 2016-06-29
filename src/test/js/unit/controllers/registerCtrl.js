(function() {
  'use strict';

  describe('Controllers', function() {

    beforeEach(module('guestbook'));

    describe('registerCtrl', function() {

      var $q, $httpBackend, $scope, $controller, $location, deferred, Alerts;
      beforeEach(inject(function(
        _$q_, _$httpBackend_, _$rootScope_, _$controller_, _$location_
      ) {

        $q = _$q_;
        $scope = _$rootScope_.$new();
        $httpBackend = _$httpBackend_;
        $location = _$location_;

        deferred = _$q_.defer();

        spyOn($location, 'path').and.callThrough();
        Alerts = jasmine.createSpyObj('Alerts', ['push']);

        var controller = _$controller_('RegisterCtrl', {
          $scope: $scope,
          Alerts: Alerts
        });
      }));

      afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
      });

      it('should set the RegisterCtrl initial variables correctly', function() {
        expect($scope.user.username).toEqual('');
        expect($scope.user.password).toEqual('');
      });

      describe('#registerUser', function() {
        it('should push a new alert', function() {
          $scope.user = {
            username: 'foo', password: 'bar'
          };

          $scope.registerUser();

          $httpBackend.expectPOST('/api/v1/register', $scope.user).respond(201);
          $httpBackend.flush();

          expect(Alerts.push).toHaveBeenCalledTimes(1);
          expect(Alerts.push).toHaveBeenCalledWith({
            type: 'alert-success',
            msg: 'You have successfully registered with username foo!'
          });
        })
      });

    });

  });

})();
