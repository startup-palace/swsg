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

    protected function has(string $name)
    {
        return array_key_exists($name, $this->context);
    }

    public function add(string $name, $value)
    {
        if ($this->has($name)) {
            throw new \Exception("Variable '$name' already exists in context!");
        }

        $this->context[$name] = $value;

        return $this;
    }

    public function get(string $name)
    {
        if (!$this->has($name)) {
            throw new \Exception("Variable '$name' does not exist in context!");
        }

        return $this->context[$name];
    }

    public function rem(string $name)
    {
        if (!$this->has($name)) {
            throw new \Exception("Variable '$name' does not exist in context!");
        }

        $this->context = array_filter($this->context, function ($key) use ($name) {
            return $key !== $name;
        }, ARRAY_FILTER_USE_KEY);

        return $this;
    }

    public function unsafeRename(string $source, string $target)
    {
        $value = $this->get($source);
        $this->rem($source);
        $this->add($target, $value);

        return $this;
    }

    public function dump()
    {
        return $this->context;
    }
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
