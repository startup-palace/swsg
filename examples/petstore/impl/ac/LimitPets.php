<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class LimitPets implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $pets = $ctx->get('pets');
        $limit = $ctx->get('limit');

        if ($limit === null) {
            $ctx->add('limitedPets', $pets);
        } else {
            $limitedPets = array_slice($pets, 0, $limit);
            $ctx->add('limitedPets', $limitedPets);
        }
        return $ctx;
    }
}
