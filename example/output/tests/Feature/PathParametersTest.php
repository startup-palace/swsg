<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class PathParametersTest extends DBTestCase
{
    public function testNoPath()
    {
        $response = $this->json('GET', '/test/path');
        $response->assertStatus(404);

    }

    public function testP1AndP2()
    {
        $response = $this->json('GET', '/test/path/test/path/5');
        $response->assertStatus(200);

    }

    public function testP1AndInvalidP2()
    {
        $response = $this->json('GET', '/test/path/test/path/test');
        $response
            ->assertStatus(400)
            ->assertExactJson(['The p2 must be an integer.']);
    }
}
