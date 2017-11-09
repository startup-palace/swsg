@(cs: Set[swsg.Model.Component], services: Seq[swsg.Model.Service])<?php
// @Backend.header

use \Illuminate\Http\Request;
use \Illuminate\Support\Facades\Validator;
use @{Laravel.swsgNamespace}\Ctx;
use @{Laravel.swsgNamespace}\Params;

@for(s <- services) {
Route::match(['@{s.method.toLowerCase}'], '@s.path', function (Request $req) {
    $queryParams = [@{s.params.filter(_.location == swsg.Model.Query).map(p => s"'${p.variable.name}' => $$req->query('${p.variable.name}')").mkString(", ")}];
    $headerParams = [@{s.params.filter(_.location == swsg.Model.Header).map(p => s"'${p.variable.name}' => $$req->header('${p.variable.name}')").mkString(", ")}];
    $pathParams = [@{s.params.filter(_.location == swsg.Model.Path).map(p => s"'${p.variable.name}' => $$req->route()->parameter('${p.variable.name}')").mkString(", ")}];
    $cookieParams = [@{s.params.filter(_.location == swsg.Model.Cookie).map(p => s"'${p.variable.name}' => $$req->cookie('${p.variable.name}')").mkString(", ")}];
    $initialContext = new Ctx(array_merge($queryParams, $headerParams, $pathParams, $cookieParams));

    $validatorRules = [@{s.params.flatMap(Laravel.genValidatorRules).mkString(", ")}];
    $validator = Validator::make($initialContext->dump(), $validatorRules);
    if ($validator->fails()) {
        return response($validator->errors()->all(), 400);
    } else {
        return @{Laravel.instantiate(cs, s.component, "$initialContext")};
    }
});
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
