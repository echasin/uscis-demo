(function() {
  'use strict';

  describe('Controllers', function() {

    beforeEach(module('guestbook'));

    describe('logoutCtrl', function() {

      var $q, $scope, $httpBackend, $controller, $location, deferred, Alerts;
      beforeEach(inject(function(
        _$q_, _$rootScope_, _$controller_, _$location_, _$httpBackend_
      ) {

        $q = _$q_;
        $scope = _$rootScope_.$new();
        $location = _$location_;
        $httpBackend = _$httpBackend_;

        deferred = _$q_.defer();

        spyOn($location, 'path').and.callThrough();
        Alerts = jasmine.createSpyObj('Alerts', ['push']);

        var controller = _$controller_('LogoutCtrl', {
          $scope: $scope,
        });
      }));

      afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
      });

      it('should show the successful logout message on success', function() {
        $httpBackend.expectGET('/api/v1/logout').respond(200);
        $httpBackend.flush();
        expect($scope.logoutSuccess).toBe(true);
        expect($scope.logoutFailure).toBe(false);
      });

      it('should show the unsuccesful logout message on failure', function() {
        $httpBackend.expectGET('/api/v1/logout').respond(500);
        $httpBackend.flush();
        expect($scope.logoutSuccess).toBe(false);
        expect($scope.logoutFailure).toBe(true);
      });

    });

  });

})();
