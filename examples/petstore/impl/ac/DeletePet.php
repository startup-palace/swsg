<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

use DB;

class DeletePet implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $id = $ctx->get('id');
        DB::table('pet')
            ->where('id', $id)
            ->delete();
        return response(null, 204);
    }
}
