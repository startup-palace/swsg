<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

use Illuminate\Support\Facades\DB;

class FetchRegistrations implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $registrations = DB::table('registration')->get();
        $ctx->add('registrations', $registrations);
        return $ctx;
    }
}
