<?php

namespace App\Components;

use App\SWSG\Component;
use App\SWSG\Ctx;
use App\SWSG\Params;

use DB;

class CreateOrUpdatePet implements Component
{
    public static function execute(Params $params, Ctx $ctx)
    {
        $newData = $ctx->get('newPet');
        $id = $ctx->get('id', true);

        if ($params->get('createOnly')->value || $id === null) {
            $id = DB::table('pet')->insertGetId($newData);
        } else {
            $oldPet = DB::table('pet')
                ->where('id', $id)
                ->first();

            if ($oldPet === null) {
                DB::table('pet')->insert(array_merge(['id' => $id], $newData));
            } else {
                DB::table('pet')
                    ->where('id', $id)
                    ->update($newData);
            }
        }

        $pet = array_merge(['id' => $id], $newData);
        $ctx->add('pet', $pet);

        return $ctx;
    }
}
