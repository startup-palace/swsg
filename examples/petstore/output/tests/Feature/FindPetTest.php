<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class FindPetTest extends DBTestCase
{
    public function testExistingPet()
    {
        $expectedData = \PetSeeder::petData()[1];
        $response = $this->json('GET', '/pets/2');
        $response
            ->assertStatus(200)
            ->assertJson($expectedData);
    }

    public function testUnknownPet()
    {
        $response = $this->json('GET', '/pets/999');
        $response
            ->assertStatus(404);
    }
}
