<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

use DB;

class CreatePet implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $newPet = $ctx->get('newPet');
        $id = DB::table('pet')->insertGetId($newPet);
        $pet = array_merge(['id' => $id], $newPet);
        $ctx->add('pet', $pet);
        return $ctx;
    }
}
