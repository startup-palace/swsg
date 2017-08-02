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
        DB::table('registration')->insert([
            ['name' => 'Batman', 'email' => 'batman@wayne-corp.com', 'date' => Carbon::create(2017, 07, 01, 20, 00, 00, 'Europe/Paris')],
        ]);
    }
}
