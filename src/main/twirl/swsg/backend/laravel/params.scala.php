<?php
// @Backend.header

namespace @Laravel.swsgNamespace;

class Params
{
    protected $params;

    public function __construct($params = [])
    {
        $this->params = $params;
    }

    public function __toString()
    {
        return print_r($this->dump(), true);
    }

    public function get($name, $dataType)
    {
        return array_filter($this->params, function ($p) use ($name, $dataType) {
            return $p->name === $name;
        })[0];
    }

    public function dump()
    {
        return $this->params;
    }
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
