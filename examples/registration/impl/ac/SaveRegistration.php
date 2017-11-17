<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

use Carbon\Carbon;
use DB;

class SaveRegistration implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $newItem = $ctx->get('registration');
        $newItem['date'] = Carbon::now('Europe/Paris');
        DB::table('registration')->insert($newItem);
        return $ctx;
    }
}
