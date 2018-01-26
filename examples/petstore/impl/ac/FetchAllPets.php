<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

use DB;

class FetchAllPets implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $pets = DB::table('pet')
            ->select(['id', 'name', 'tag'])
            ->get()
            ->all();
        $ctx->add('pets', $pets);
        return $ctx;
    }
}
