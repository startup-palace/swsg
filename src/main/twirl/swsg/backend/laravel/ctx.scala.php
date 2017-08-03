<?php
// @Backend.header

namespace @Laravel.swsgNamespace;

class Ctx
{
    protected $context;

    public function __construct(array $context = [])
    {
        $this->context = $context;
    }

    public function __toString()
    {
        return print_r($this->dump(), true);
    }

    public function add(string $name, $value)
    {
        $this->context[$name] = $value;

        return true;
    }

    public function get(string $name)
    {
        return $this->context[$name];
    }

    public function rem(string $name)
    {
        $this->context = array_filter($this->context, function ($key) use ($name) {
            return $key !== $name;
        }, ARRAY_FILTER_USE_KEY);

        return true;
    }

    public function dump()
    {
        return $this->context;
    }
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
