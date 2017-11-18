<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class RenderPets implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $pets = $ctx->get('pets');
        return response()->json($pets, 200);
    }
}
