app.controller('cartController',function($scope,cartService){
    //查询购物车列表
    $scope.findCartList=function(){
            cartService.findCartList().success(
                function(response){
                    $scope.cartList=response;
                }
            );
    }
    $scope.addGoodsToCartList=function(itemId,num){
        cartService.addGoodsToCartList(itemId,num).success(
            function(response){
              if (response.success) {
                  $scope.findCartList();
                  $scope.totalValue=cartService.sum($scope.cartList);
              }else {
                  alert(response.message);
              }
            }
        );
    }
    $scope.findAddressList=function(){
        cartService.findAddressList().success(
            function(response){
                $scope.addressList=response;
            }
        );
    }

    $scope.save=function(){
        var serviceObject;//服务层对象
        if($scope.address.id!=null){//如果有ID
            serviceObject=cartService.update( $scope.address ); //修改
        }else{
            serviceObject=cartService.add( $scope.address  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    //重新查询
                    $scope.findCartList();//重新加载
                }else{
                    alert(response.message);
                }
            }
        );
    }
});