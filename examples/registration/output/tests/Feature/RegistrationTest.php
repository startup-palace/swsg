<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class Registration extends DBTestCase
{

    public function testWrongEmail()
    {
        $response = $this->json('POST', '/register/name/notanemail');
        $response->assertStatus(422);
    }

    public function testAlreadyRegistered()
    {
        $existingData = \RegistrationSeeder::registrationData();
        $response = $this->json('POST', '/register/'.$existingData[0]['name'].'/'.$existingData[0]['email']);
        $response->assertStatus(403);
    }

    public function testInsertNewItem()
    {
        $newItem = ['name' => 'Name', 'email' => 'email@localhost.local'];
        $response = $this->json('POST', '/register/'.$newItem['name'].'/'.$newItem['email']);
        $response
            ->assertStatus(200)
            ->assertJson($newItem);
        $this->assertDatabaseHas('registration', $newItem);
    }
}
