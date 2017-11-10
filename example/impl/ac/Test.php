<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class Test implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        return response($ctx->dump());
    }
}
