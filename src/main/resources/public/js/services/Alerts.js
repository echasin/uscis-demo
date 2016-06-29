(function() {

  'use strict';

  app.factory('Alerts', function() {
    var alerts = [];

    return {
      push: function(obj) {
        alerts.push(obj);
      },
      close: function(index) {
        alerts.splice(index, 1);
      },
      list: function() {
        return alerts;
      },
      reset: function() {
        alerts = [];
      }
    };
  });

})();
