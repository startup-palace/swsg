<?php
// @Backend.header

namespace App\Providers;

use Illuminate\Support\Facades\Route;

class GeneratedRouteServiceProvider extends RouteServiceProvider
{
    /**
     * Define the routes for the application.
     *
     * @@return void
     */
    public function map()
    {
        $this->mapGeneratedRoutes();

        parent::map();
    }

    /**
     * Define the "generated" routes for the application.
     *
     * @@return void
     */
    protected function mapGeneratedRoutes()
    {
        Route::group([], function ($router) {
            require base_path('routes/generated.php');
        });
    }

}

@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
