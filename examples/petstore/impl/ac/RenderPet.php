<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class RenderPet implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $pet = $ctx->get('pet');
        if ($pet === null) return response(null, 404);
        else return response()->json($pet, 200);
    }
}
