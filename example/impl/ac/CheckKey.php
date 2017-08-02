<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class CheckKey implements Component
{
    public static function execute(Ctx $ctx, Params $params)
    {
        var_dump($ctx, $params);
        return $ctx;
    }
}
