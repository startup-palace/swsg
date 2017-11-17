<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class CreateRegistration implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $ctx->add('registration', ['name' => $ctx->get('name'), 'email' => $ctx->get('email')]);
        return $ctx;
    }
}
