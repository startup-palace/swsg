<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class RegistrationsSerializer implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        return response($ctx->get('registrations')->toJson());
    }
}
