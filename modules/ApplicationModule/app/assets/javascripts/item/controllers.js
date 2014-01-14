// item module

angular.module('ItemModule.controllers', ['ItemModule.services', 'angularFileUpload']).
  controller('NewItemController',[
    '$scope', '$upload', 'createNewItemService', '$routeParams', '$location', 'getVirtualCurrenciesService',
    function ($scope, $upload, createNewItemService, $routeParams, $location, getVirtualCurrenciesService) {

    $scope.currencyOptions = ["Real","Virtual"];
    $scope.showCurrencyInputs = {
      "real": true,
      "virtual": false
    };

    $scope.bootstrapModule = function(){
      $scope.itemForm = {
        "applicationName": encodeURI($routeParams.applicationId),
        "name": "",
        "description": "",
        "store": 1,
        "metadata": {
          "osType": "",
          "title": "",
          "description": "",
          "publishedState": "published",
          "purchaseType": 0,
          "autoTranslate": true,
          "locale": [],
          "autofill": true,
          "language": "English", //TODO: get default lang
          "price": 0.0
        },
        "currency": {
          "typeOf": "Real",
          "value": 0.0,
          "virtualCurrency": ""
        }
      };
      $scope.showCurrencyInputs.real = true;
      $scope.errors = false;
      $scope.formErrors = [];
      $scope.moneyCurrency = "$"; /*+ TODO: in the future get this via API **/
      $scope.currentCurrency = "$";
      $scope.virtualCurrencies = [];
      getVirtualCurrenciesService.execute($scope.itemForm.applicationName)
        .then(
          function(success){
            $scope.handleVirtualCurrencyRequestSuccess(success);
          },
          function(error){
            $scope.handleVirtualCurrencyRequestError(error);
          }
        );
      $scope.$watch('itemForm.currency.typeOf', function(newValue, oldValue, scope){
        if (newValue == "Real") {
          $scope.showCurrencyInputs.real = true;
          $scope.showCurrencyInputs.virtual = false;
        } else {
          $scope.showCurrencyInputs.real = false;
          $scope.showCurrencyInputs.virtual = true;
        }
      });
    };
    $scope.bootstrapModule();

    $scope.handleVirtualCurrencyRequestSuccess = function(success){
      _.each(success.data, function(value, key, list){
        $scope.virtualCurrencies.push(value.name);
      });

      /**Removes virtual currency option if application has not virtual currencies **/
      if(_.size($scope.virtualCurrencies) == 0) {
        $scope.currencyOptions = _.filter($scope.currencyOptions, function(value){
          return value != "Virtual";
        });
      }
    };

    $scope.handleVirtualCurrencyRequestError = function(error){
      $scope.currencyOptions = _.filter($scope.currencyOptions, function(value){
          return value != "Virtual";
      });
    };

    $scope.handleSuccess = function(){
      $scope.errors = false;
      $scope.formErrors = [];
      $location.path("/home");
    }

    $scope.handleErrors = function(errors){
      $scope.errors = true;
      _.each(angular.fromJson(errors.data.errors), function(error){
        $scope.formErrors.push(error);
      });
    }

    $scope.createItem = function(){
      createNewItemService.send($scope.itemForm, $scope.myFile)
        .then(
          function(){
            $scope.handleSuccess();
          },
          function(errors){
            $scope.handleErrors(errors);
          }
        );
    };
  }])
;
