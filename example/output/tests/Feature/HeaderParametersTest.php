<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class HeaderParametersTest extends DBTestCase
{
    public function testNoHeaders()
    {
        $response = $this->json('POST', '/test/header');
        $response
            ->assertStatus(400)
            ->assertExactJson(['The p1 must be a string.']);

    }

    public function testP1()
    {
        $response = $this
            ->withHeaders(['p1' => 'test'])
            ->json('POST', '/test/header');
        $response
            ->assertStatus(200);

    }

    public function testP1AndP2()
    {
        $response = $this
            ->withHeaders(['p1' => 'test', 'p2' => 5])
            ->json('POST', '/test/header');
        $response
            ->assertStatus(200);

    }

    public function testP1AndInvalidP2()
    {
        $response = $this
            ->withHeaders(['p1' => 'test', 'p2' => 'test'])
            ->json('POST', '/test/header');
            $response
                ->assertStatus(400)
                ->assertExactJson(['The p2 must be an integer.']);

    }
}
