<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

use DB;

class CheckDupRegistration implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $dups = DB::table('registration')
            ->where('name', $ctx->get('name'))
            ->where('email', $ctx->get('email'))
            ->count();
        if ($dups > 0) {
            return response('Already registered!', 403);
        }
        return $ctx;
    }
}
