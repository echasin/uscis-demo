'use strict';

module.exports = function(grunt) {
  require('load-grunt-tasks')(grunt);

  // declare some constants
  grunt.initConfig({
    constants: {
      app: 'src/main/resources',
      vendor: 'src/main/resources/public/js/lib',
    },

    // configure jshint for code linting
    jshint: {
      files: [
        '<%= constants.app %>/public/js/**/*.js',
        '!<%= constants.vendor %>/**/*.js'
      ],
      options: {
        strict: 'global',
        curly: true,
        devel: true,
        eqnull: true,
        evil: true,
        immed: true,
        maxcomplexity: 8,
        newcap: true,
        noarg: true,
        sub: true,
        trailing: true,
        globals: { angular: true }
      }
    },

    // configure karma for unit tests
    karma: {
      options: {
        basePath: '.',
        frameworks: [
          'jasmine',
        ],
        browsers: ['PhantomJS'],
        exclude: ['<%= constants.mocks %>/mocks.js'],
        files: [
          '<%= constants.vendor %>/angular/angular.js',
          '<%= constants.vendor %>/angular/angular-*.js',
          '<%= constants.vendor %>/angular/angular-mocks.js',
          '<%= constants.app %>/public/js/**/*.js',

          'src/test/js/unit/**/*.js'
        ]
      },
      single: {
        singleRun: true,
        reporters: ['spec']
      },
      continuous: {
        singleRun: false,
        autoWatch: true,
        reporters: ['spec']
      }
    }

  });

  grunt.registerTask('test', [
    'jshint',
    'karma:single'
  ])
};
