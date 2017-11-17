<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class QueryParametersTest extends DBTestCase
{
    public function testNoQueryString()
    {
        $response = $this->json('GET', '/test/query');
        $response
            ->assertStatus(400)
            ->assertExactJson(['The p1 must be a string.', 'The p2 must be an array.']);
    }

    public function testP1AndNoP2()
    {
        $response = $this->json('GET', '/test/query?p1=test');
        $response
            ->assertStatus(400)
            ->assertExactJson(['The p2 must be an array.']);
    }

    public function testP1AndEmptyP2()
    {
        $response = $this->json('GET', '/test/query?p1=test&p2[]=');
        $response
            ->assertStatus(400)
            ->assertExactJson(['The p2.0 must be an integer.']);
    }

    public function testP1AndP2()
    {
        $response = $this->json('GET', '/test/query?p1=test&p2[]=1&p2[]=2');
        $response
            ->assertStatus(200)
            ->assertExactJson(['p1' => 'test', 'p2' => ['1', '2'], 'p3' => null]);
    }

    public function testP1AndInvalidP2()
    {
        $response = $this->json('GET', '/test/query?p1=test&p2[]=1&p2[]=test');
        $response
            ->assertStatus(400)
            ->assertExactJson(['The p2.1 must be an integer.']);
    }

    public function testP1AndP2AndP3()
    {
        $response = $this->json('GET', '/test/query?p1=test&p2[]=1&p2[]=2&p3=10');
        $response
            ->assertStatus(200)
            ->assertExactJson(['p1' => 'test', 'p2' => ['1', '2'], 'p3' => '10']);
    }

    public function testP1AndP2AndInvalidP3()
    {
        $response = $this->json('GET', '/test/query?p1=test&p2[]=1&p2[]=2&p3=test');
        $response
            ->assertStatus(400)
            ->assertExactJson(['The p3 must be an integer.']);
    }
}
