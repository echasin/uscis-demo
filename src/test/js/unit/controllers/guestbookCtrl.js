(function() {
  'use strict';

  describe('Controllers', function() {

    beforeEach(module('guestbook'));

    describe('guestbookCtrl', function() {
      var guestbookName = 'foo';
      var defaultURL = '/api/v1/messages/' + guestbookName;
      var defaultResponse = [
        200, {results: [{guestbook: guestbookName}]}
      ];

      var $q, $httpBackend, $scope, $controller, $location, $routeParams, deferred;
      beforeEach(inject(function(
        _$q_, _$httpBackend_, _$rootScope_, _$controller_, _$location_, _$routeParams_
      ) {

        $q = _$q_;
        $httpBackend = _$httpBackend_
        $scope = _$rootScope_.$new();
        $location = _$location_;
        $routeParams = _$routeParams_;

        deferred = _$q_.defer();

        spyOn($location, 'path').and.callThrough();

        var controller = _$controller_('GuestbookCtrl', {
          $scope: $scope,
          $routeParams: {'guestbookName': guestbookName},
        });
      }));

      beforeEach(function() {
        $httpBackend.expectGET(defaultURL).respond(
          function(method, url, data) { return defaultResponse; }
        )
        $httpBackend.flush();
      })

      afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
      });

      it('should set proper scope variables', function() {
        // resolve our promise and return data
        expect($scope.guestbookName).toEqual(guestbookName);
        expect($scope.currentGuestbookName).toEqual(guestbookName);
      });

      describe('#submitForm', function() {
        it('should post data to server', function() {
          $scope.content = 'a test post';
          $scope.submitForm();

          $httpBackend.expectPOST(defaultURL).respond(
            function(method, url, data) { return defaultResponse; }
          )
          $httpBackend.expectGET(defaultURL).respond(
            function(method, url, data) { return defaultResponse; }
          )
          $httpBackend.flush();
        });
        
        it('should clear the message field', function() {
        	$scope.content = 'a test post';
        	$scope.submitForm();
        	
            $httpBackend.expectPOST(defaultURL).respond(
              function(method, url, data) { return defaultResponse; }
            )
            $httpBackend.expectGET(defaultURL).respond(
              function(method, url, data) { return defaultResponse; }
            )
            $httpBackend.flush();
            
            expect($scope.content).toEqual('');
        });

        it('should update the path', function() {
          var newGuestbookName = 'updated'

          $scope.content = 'a test post';
          $scope.guestbook = newGuestbookName;
          $scope.submitForm();

          $httpBackend.expectPOST(defaultURL).respond(
            function(method, url, data) { return defaultResponse; }
          )
          $httpBackend.expectGET(defaultURL).respond(
            function(method, url, data) { return defaultResponse; }
          )
          $httpBackend.flush();

          expect($location.path).toHaveBeenCalledTimes(1);
          expect($location.path).toHaveBeenCalledWith('guestbook/' + guestbookName);
        });
      });

    });

  });

})();
