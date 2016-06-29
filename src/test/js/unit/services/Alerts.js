(function() {
  'use strict';

  describe('Services', function() {

    beforeEach(module('guestbook'));

    var multiValue = [1,2];

    var Alerts;
    beforeEach(inject(function(_Alerts_) {
      Alerts = _Alerts_;
    }));

    afterEach(function() {
      Alerts.reset();
    })

    describe('Alerts', function() {

      describe('#push', function() {
        it('should add single alerts', function() {
          Alerts.push(multiValue[0])
          expect(Alerts.list().length).toBe(1);
          expect(Alerts.list()).toContain(multiValue[0]);
        });

        it('should be able to contain multiple alerts', function() {
          Alerts.push(multiValue[0])
          Alerts.push(multiValue[1])
          expect(Alerts.list().length).toBe(2);
          expect(Alerts.list()).toContain(multiValue[0]);
          expect(Alerts.list()).toContain(multiValue[1]);
        })

      });

      describe('#close', function() {
        it('should remove the specified alert from the alerts array', function() {
          Alerts.push(multiValue[0]);
          Alerts.push(multiValue[1]);
          expect(Alerts.list()).toContain(multiValue[0]);
          Alerts.close(0);
          expect(Alerts.list()).not.toContain(multiValue[0]);
          expect(Alerts.list()).toContain(multiValue[1]);
        });
      });

      describe('#reset', function() {
        it('should make the alerts array have length zero', function() {
          Alerts.push(multiValue[0]);
          expect(Alerts.list().length).toBe(1);
          Alerts.reset()
          expect(Alerts.list().length).toBe(0);
        });
      });

    })

  })
}());
