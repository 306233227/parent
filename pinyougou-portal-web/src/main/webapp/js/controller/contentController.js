//广告控制层（运营商后台）
app.controller("contentController",function($scope,$location,contentService){
    $scope.contentList=[];//广告集合
    $scope.findByCategoryId=function(categoryId){
        contentService.findByCategoryId(categoryId).success(
            function(response){
                $scope.contentList[categoryId]=response;
            }
        );
    }
    //搜索跳转
    $scope.search=function(){
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
    //加载查询字符串
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=  $location.search()['keywords'];
        $scope.search();
    }
});