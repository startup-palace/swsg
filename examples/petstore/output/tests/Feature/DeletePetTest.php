<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class DeletePetTest extends DBTestCase
{
    public function testValidId()
    {
        $this->json('GET', '/pets/1')
            ->assertStatus(200);

        $response = $this->json('DELETE', '/pets/1');
        $response
            ->assertStatus(204);

        $this->json('GET', '/pets/1')
            ->assertStatus(404);
    }
}
