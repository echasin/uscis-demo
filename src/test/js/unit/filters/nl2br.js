(function() {
  'use strict';

  describe('Filters', function() {

    beforeEach(module('guestbook'));

    describe('nl2br', function() {
      it('should properly convert newlines to <br> tags',
        inject(function(nl2brFilter) {
          var filterResult = nl2brFilter('One\nTwo');
          expect(filterResult).toEqual('One<br>Two');
      }));
    });
  });
})();
