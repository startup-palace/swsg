@(cs: Set[swsg.Model.Component], services: Seq[swsg.Model.Service])<?php
// @Backend.header

use @{Laravel.swsgNamespace}\Ctx;
use @{Laravel.swsgNamespace}\Params;

@for(s <- services) {
Route::match(['@{s.method.toLowerCase}'], '{url}', function (string $url) {
    preg_match_all('/@{s.url.stripPrefix("\\/")}/', $url, $params);
    $expectedParams = [@{s.params.map(p => s"'${p.name}'").mkString(", ")}];
    $diff = array_diff($expectedParams, array_keys($params));
    if (!empty($diff)) {
        $missing = implode(', ', array_map(function ($p) {
            return "'$p'";
        }, $diff));
        throw new Symfony\Component\HttpKernel\Exception\NotFoundHttpException('Several URL parameters are missing: '.$missing.'. Please fix URL pattern (\'@{s.url}\') in the model.');
    }
    $intersection = array_map(function ($v) {
        return $v[0];
    }, array_filter($params, function ($v, $k) use ($expectedParams) {
        return in_array($k, $expectedParams, true);
    }, ARRAY_FILTER_USE_BOTH));
    @*return @{Laravel.componentNamespace}\@{s.component.component.target}::@{Laravel.executeMethod}(new Ctx($intersection), new Params());*@
    return @{Laravel.instantiate(cs, s.component, "(new Ctx($intersection))")};
})->where('url', '@{s.url.stripPrefix("\\/")}');
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
