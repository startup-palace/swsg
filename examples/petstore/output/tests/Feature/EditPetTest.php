<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class EditPetTest extends DBTestCase
{
    public function testEditInvalidPet()
    {
        $newPet = ['name' => 'Batman', 'tag' => 'dark'];
        $id = 5;
        $expectedData = ['id' => $id, 'name' => 'Batman', 'tag' => 'dark'];
        $response = $this->json('PUT', '/pets/'.$id, $newPet);
        $response
            ->assertStatus(200)
            ->assertJson($expectedData);
    }

    public function testEditValidPet()
    {
        $newPet = ['name' => 'Batman', 'tag' => 'dark'];
        $id = 3;
        $expectedData = ['id' => $id, 'name' => 'Batman', 'tag' => 'dark'];
        $response = $this->json('PUT', '/pets/'.$id, $newPet);
        $response
            ->assertStatus(200)
            ->assertJson($expectedData);
    }
}
