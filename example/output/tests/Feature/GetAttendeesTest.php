<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class GetAttendeesTest extends DBTestCase
{
    protected $key = "mykey";

    public function testNoKey()
    {
        $response = $this->json('GET', '/attendees');
        $response->assertStatus(401);
    }

    public function testWrongKey()
    {
        $response = $this->json('GET', '/attendees?key=wrongkey');
        $response->assertStatus(401);
    }

    public function testAttendees()
    {
        $expectedData = \RegistrationSeeder::registrationData();
        $response = $this->json('GET', '/attendees?key='.$this->key);
        $response
            ->assertStatus(200)
            ->assertJson($expectedData);
    }
}
