<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

use DB;

class GetPetById implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $pet = DB::table('pet')
            ->where('id', $ctx->get('id'))
            ->first();
        $ctx->add('pet', $pet);
        return $ctx;
    }
}
