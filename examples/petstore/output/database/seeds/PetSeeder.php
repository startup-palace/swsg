<?php

use Illuminate\Database\Seeder;

class PetSeeder extends Seeder
{
    /**
     * Run the database seeds.
     *
     * @return void
     */
    public function run()
    {
        DB::table('pet')->insert(self::petData());
    }

    public static function petData() {
         return [
            ['id' => 1, 'name' => 'Twilight Sparkle', 'tag' => null],
            ['id' => 2, 'name' => 'Rainbow Dash', 'tag' => 'pegasus'],
            ['id' => 3, 'name' => 'Fluttershy', 'tag' => 'pegasus'],
            ['id' => 4, 'name' => 'Rarity', 'tag' => 'unicorn'],
        ];
    }
}
