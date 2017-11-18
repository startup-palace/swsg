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

    public function testPetsWithLimit()
    {
        $expectedData = array_slice(\PetSeeder::petData(), 0, 2);
        $response = $this->json('GET', '/pets?limit=2');
        $response
            ->assertStatus(200)
            ->assertJson($expectedData);
    }

    public function testPetsWithOneTag()
    {
        $expectedData = array_values(array_filter(\PetSeeder::petData(), function ($pet) {
            return $pet['tag'] === 'pegasus';
        }));
        $response = $this->json('GET', '/pets?tags[]=pegasus');
        $response
            ->assertStatus(200)
            ->assertJson($expectedData);
    }

    public function testPetsWithSeveralTags()
    {
        $expectedData = array_values(array_filter(\PetSeeder::petData(), function ($pet) {
            return $pet['tag'] === 'pegasus' || $pet['tag'] === 'unicorn';
        }));
        $response = $this->json('GET', '/pets?tags[]=pegasus&tags[]=unicorn');
        $response
            ->assertStatus(200)
            ->assertJson($expectedData);
    }

    public function testPetsWithLimitAndTags()
    {
        $expectedData = array_slice(array_values(array_filter(\PetSeeder::petData(), function ($pet) {
            return $pet['tag'] === 'pegasus' || $pet['tag'] === 'unicorn';
        })), 0, 1);
        $response = $this->json('GET', '/pets?tags[]=pegasus&tags[]=unicorn&limit=1');
        $response
            ->assertStatus(200)
            ->assertJson($expectedData);
    }
}
