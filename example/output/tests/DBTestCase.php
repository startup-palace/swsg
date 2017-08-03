<?php

namespace Tests;

abstract class DBTestCase extends TestCase
{
    protected function setUp() {
        parent::setUp();

        $this->artisan('migrate:refresh');
        $this->artisan('db:seed');
    }
}
