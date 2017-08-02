<?php
// @Backend.header

namespace @Laravel.swsgNamespace;

class Variable
{
    public $name;
    public $dataType;
    public $value;

    public function __construct($name, $dataType, $value)
    {
        $this->name = $name;
        $this->dataType = $dataType;
        $this->value = $value;
    }
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
