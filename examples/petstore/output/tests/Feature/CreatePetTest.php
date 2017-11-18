<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class CreatePetTest extends DBTestCase
{
    public function testValidPet()
    {
        $newPet = ['name' => 'Batman', 'tag' => 'dark'];
        $expectedData = ['id' => 5, 'name' => 'Batman', 'tag' => 'dark'];
        $response = $this->json('POST', '/pets', $newPet);
        $response
            ->assertStatus(200)
            ->assertJson($expectedData);
    }
}
