@(cs: Set[swsg.Model.Component], services: Seq[swsg.Model.Service])<?php
// @Backend.header

use \Illuminate\Http\Request;
use @{Laravel.swsgNamespace}\Ctx;
use @{Laravel.swsgNamespace}\Params;

@for(s <- services) {
Route::match(['@{s.method.toLowerCase}'], '@s.path', function (Request $req) {
    $queryParams = [@{s.params.filter(_.location == swsg.Model.Query).map(p => s"'${p.variable.name}' => $$req->query('${p.variable.name}')").mkString(", ")}];
    $headerParams = [@{s.params.filter(_.location == swsg.Model.Header).map(p => s"'${p.variable.name}' => $$req->header('${p.variable.name}')").mkString(", ")}];
    $pathParams = [@{s.params.filter(_.location == swsg.Model.Path).map(p => s"'${p.variable.name}' => $$req->route()->parameter('${p.variable.name}')").mkString(", ")}];
    $cookieParams = [@{s.params.filter(_.location == swsg.Model.Cookie).map(p => s"'${p.variable.name}' => $$req->cookie('${p.variable.name}')").mkString(", ")}];
    $initialContext = new Ctx(array_merge($queryParams, $headerParams, $pathParams, $cookieParams));
    return @{Laravel.instantiate(cs, s.component, "$initialContext")};
});
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
