@(cs: Set[swsg.Model.Component], services: Seq[swsg.Model.Service])<?php
// @Backend.header

use @{Laravel.swsgNamespace}\Ctx;
use @{Laravel.swsgNamespace}\Params;

@for(s <- services) {
Route::match(['@{s.method.toLowerCase}'], '@s.path', function (@{s.params.map(p => s"$$${p.variable.name}").mkString(", ")}) {
    $pathParams = [@{s.params.map(p => s"'${p.variable.name}' => $$${p.variable.name}").mkString(", ")}];
    $initialContext = new Ctx($pathParams);
    return @{Laravel.instantiate(cs, s.component, "$initialContext")};
});
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
