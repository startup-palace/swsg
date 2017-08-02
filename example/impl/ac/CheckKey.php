<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class CheckKey implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        if ($ctx->get('key') !== $params->get('correctKey')->value) {
            return response('Invalid key', 401);
        } else {
            return $ctx;
        }
    }
}
