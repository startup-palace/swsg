@(m: swsg.Model)<?php
// @Backend.header

use \Illuminate\Http\Request;
use \Illuminate\Support\Facades\Validator;
use @{Laravel.swsgNamespace}\Ctx;
use @{Laravel.swsgNamespace}\Params;

@for(s <- m.services) {
Route::match(['@{s.method.toLowerCase}'], '@s.path', function (Request $req) {
    $initialContext = new Ctx([@{s.params.map(Laravel.getParameters).mkString(", ")}]);

    $validatorRules = [@{s.params.flatMap(Laravel.genValidatorRules(m.entities)).mkString(", ")}];
    $validator = Validator::make($initialContext->dump(), $validatorRules);
    if ($validator->fails()) {
        return response($validator->errors()->all(), 400);
    } else {
        return @{Laravel.instantiate(m.components, s.component, "$initialContext")};
    }
});
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
