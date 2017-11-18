<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class GetAllPetsTest extends DBTestCase
{
    public function testAllPets()
    {
        $expectedData = \PetSeeder::petData();
        $response = $this->json('GET', '/pets');
        $response
            ->assertStatus(200)
            ->assertJson($expectedData);
    }
}
