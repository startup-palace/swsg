<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class SaveRegistration implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        var_dump($ctx, $params);
        return $ctx;
    }
}
