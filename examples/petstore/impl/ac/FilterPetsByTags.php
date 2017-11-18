<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

class FilterPetsByTags implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $pets = $ctx->get('pets');
        $tags = $ctx->get('tags');

        if ($tags === null) {
            $ctx->add('filteredPets', $pets);
        } else {
            $filteredPets = array_filter($pets, function ($pet) use ($tags) {
                if ($pet->tag === null) return false;
                else return in_array($pet->tag, $tags);
            });
            $ctx->add('filteredPets', array_values($filteredPets));
        }
        return $ctx;
    }
}
