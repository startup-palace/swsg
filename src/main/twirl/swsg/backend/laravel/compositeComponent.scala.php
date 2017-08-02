@import swsg.Model.CompositeComponent
@(c: CompositeComponent)<?php
// @Backend.header

namespace @Laravel.componentNamespace;

use @Laravel.swsgNamespace\Component;
use @Laravel.swsgNamespace\Ctx;
use @Laravel.swsgNamespace\Params;

class @c.name implements Component
{
    public static function @{Laravel.executeMethod}(Ctx $ctx, Params $params)
    {
        @for(ci <- c.components.zipWithIndex) {
        $ctx@ci._2 = @{Laravel.instantiate(ci._1, "$ctx" ++ previous(ci._2).toString)};
        }
        return $ctx@previous(c.components.size);
    }
}

@previous(i: Int) = @{
    if (i - 1 >= 0) i - 1 else ""
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
