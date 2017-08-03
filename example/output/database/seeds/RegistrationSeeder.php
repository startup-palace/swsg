<?php

use Illuminate\Database\Seeder;

use Carbon\Carbon;

class RegistrationSeeder extends Seeder
{
    /**
     * Run the database seeds.
     *
     * @return void
     */
    public function run()
    {
        DB::table('registration')->insert(self::registrationData());
    }

    public static function registrationData() {
        return [
            ['name' => 'Batman', 'email' => 'batman@wayne-corp.com', 'date' => Carbon::create(2017, 07, 01, 20, 00, 00, 'Europe/Paris')],
            ['name' => 'Superman', 'email' => 'clark.kent@thedailyplanet.com', 'date' => Carbon::create(2017, 07, 8, 7,30, 00, 'Europe/Paris')],
        ];
    }
}
