<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;
use Validator;

class ValidateEmail implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        Validator::validate(['email' => $ctx->get('email')], ['email' => 'email']);
        return $ctx;
    }
}
