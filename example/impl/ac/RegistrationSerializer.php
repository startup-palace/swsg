<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class RegistrationSerializer implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        return response(json_encode($ctx->get('registration')));
    }
}
