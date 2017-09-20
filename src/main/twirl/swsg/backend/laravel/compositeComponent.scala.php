@import swsg.Model.{Component, CompositeComponent}
@(cs: Set[Component], c: CompositeComponent)<?php
// @Backend.header

namespace @Laravel.componentNamespace;

use @Laravel.swsgNamespace\Component;
use @Laravel.swsgNamespace\Ctx;
use @Laravel.swsgNamespace\Params;

class @c.name implements Component
{
    public static function @{Laravel.executeMethod}(Params $params, Ctx $ctx)
    {
        @for(ci <- c.components.zipWithIndex) {
        $ctx@{ci._2} = @{Laravel.instantiate(cs, ci._1, "$ctx" ++ previous(ci._2).toString)};
        if ($ctx@{ci._2} instanceof \Illuminate\Http\Response) {
            return $ctx@{ci._2};
        }
        if ($ctx@{ci._2} instanceof \SWSG\Ctx) {
            $ctx@{ci._2} = $ctx@{ci._2}@{Laravel.postInstanciation(cs, ci._1)};
        }
        }
        return $ctx@previous(c.components.size);
    }
}

@previous(i: Int) = @{
    if (i - 1 >= 0) i - 1 else ""
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
