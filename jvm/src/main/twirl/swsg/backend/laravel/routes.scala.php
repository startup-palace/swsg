@(cs: Set[swsg.Model.Component], services: Seq[swsg.Model.Service])<?php
// @Backend.header

use \Illuminate\Http\Request;
use @{Laravel.swsgNamespace}\Ctx;
use @{Laravel.swsgNamespace}\Params;

@for(s <- services) {
Route::match(['@{s.method.toLowerCase}'], '@s.path', function (Request $req) {
    $pathParams = [@{s.params.map(p => s"'${p.variable.name}' => $$req->route()->parameter('${p.variable.name}')").mkString(", ")}];
    $initialContext = new Ctx($pathParams);
    return @{Laravel.instantiate(cs, s.component, "$initialContext")};
});
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
