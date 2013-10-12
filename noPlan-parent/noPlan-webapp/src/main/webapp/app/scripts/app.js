'use strict';

console.log('init app');
angular.module('mytodoApp', ['ui','ui.bootstrap','LocalStorageModule'])
  .config(function ($routeProvider) {
    console.log('routeProvider');
    var baseUrl="app/"
    $routeProvider
    .when('/', {
            templateUrl: 'views/main.html',
            controller: 'MainCtrl'
        })
    .when('/users', {
            templateUrl: 'views/users.html',
            controller: 'UserCtrl'
    })
    .when('/conferences', {
            templateUrl: 'views/conferences.html',
            controller: 'ConferencesCtrl'
    })
    .when('/todo', {
        templateUrl: 'views/todo.html',
        controller: 'TodoCtrl'
    })

    .otherwise({
        redirectTo: '/'
    });
  });