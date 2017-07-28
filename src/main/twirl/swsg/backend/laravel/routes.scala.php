@(services: Seq[swsg.Model.Service])<?php
// @Backend.header

@for(s <- services) {
Route::match(['@{s.method.toLowerCase}'], '{url}', function ($url) {
    dd($url);
})->where('url', '@{s.url}');
}

@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
