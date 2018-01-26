<?php

namespace Tests\Feature;

use Tests\DBTestCase;
use Illuminate\Foundation\Testing\WithoutMiddleware;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Foundation\Testing\DatabaseTransactions;

class BodyParametersTest extends DBTestCase
{
    public function testNoStrBody()
    {
        $response = $this->call('POST', '/test/body/str', [], [], [], [], null);
        $response
            ->assertStatus(200)
            ->assertExactJson(['p1' => '']);
    }

    public function testStrP1()
    {
        $response = $this->call('POST', '/test/body/str', [], [], [], [], 'test');
        $response
            ->assertStatus(200)
            ->assertExactJson(['p1' => 'test']);
    }

    public function testNoJsonBody()
    {
        $response = $this->json('POST', '/test/body/json/entity');
        $response
            ->assertStatus(400)
            ->assertExactJson(['The p1.name field is required.']);
    }

    public function testJsonP1()
    {
        $response = $this->json('POST', '/test/body/json/entity', ['name' => 'test']);
        $response
            ->assertStatus(200)
            ->assertExactJson(['p1' => ['name' => 'test']]);
    }

    public function testJsonInvalidP1()
    {
        $response = $this->json('POST', '/test/body/json/entity', ['name' => 'test', 'email' => 'test2']);
        $response
            ->assertStatus(400)
            ->assertExactJson(['The p1.email must be an integer.']);
    }

    public function testJsonP1WithMoreData()
    {
        $response = $this->json('POST', '/test/body/json/entity', ['name' => 'test', 'email' => 5]);
        $response
            ->assertStatus(200)
            ->assertExactJson(['p1' => ['name' => 'test', 'email' => 5]]);
    }

    public function testNoArrayBody()
    {
        $response = $this->json('POST', '/test/body/json/array');
        $response
            ->assertStatus(200)
            ->assertExactJson(['p1' => []]);
    }

    public function testArrayP1()
    {
        $response = $this->json('POST', '/test/body/json/array', [['name' => 'test'], ['name' => 'test2']]);
        $response
            ->assertStatus(200)
            ->assertExactJson(['p1' => [['name' => 'test'], ['name' => 'test2']]]);
    }

    public function testArrayInvalidP1()
    {
        $response = $this->json('POST', '/test/body/json/array', [['name' => 'test'], ['name' => 'test2', 'email' => 'test3']]);
        $response
            ->assertStatus(400)
            ->assertExactJson(['The p1.1.email must be an integer.']);
    }

    public function testArrayP1WithMoreData()
    {
        $response = $this->json('POST', '/test/body/json/array', [['name' => 'test'], ['name' => 'test2', 'email' => 5]]);
        $response
            ->assertStatus(200)
            ->assertExactJson(['p1' => [['name' => 'test'], ['name' => 'test2', 'email' => 5]]]);
    }
}
