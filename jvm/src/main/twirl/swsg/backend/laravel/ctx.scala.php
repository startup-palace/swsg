<?php
// @Backend.header

namespace @Laravel.swsgNamespace;

class Ctx
{
    protected $context;
    protected $shadowedContext;

    public function __construct(array $context = [])
    {
        $this->context = $context;
        $this->shadowedContext = [];
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

    public function get(string $name, bool $optional=false)
    {
        if (!$this->has($name)) {
            if ($optional) {
                return null;
            } else {
                throw new \Exception("Variable '$name' does not exist in context!");
            }
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

    public function unsafeShadow(string $source, string $target)
    {
        $value = $this->get($source);
        $this->rem($source);

        if ($this->has($target)) {
            $shadowed = $this->get($target);
            $this->shadowedContext[$target] = $shadowed;
            $this->rem($target);
        }

        $this->add($target, $value);

        return $this;
    }

    public function unsafeUnshadow(string $source, string $target)
    {
        $value = $this->get($source);
        $this->rem($source);
        $this->add($target, $value);

        if (array_key_exists($source, $this->shadowedContext)) {
            $shadowed = $this->shadowedContext[$source];
            $this->add($source, $shadowed);
            $this->shadowedContext = array_filter($this->shadowedContext, function ($key) use ($source) {
                return $key !== $source;
            }, ARRAY_FILTER_USE_KEY);
        }

        return $this;
    }

    public function dump()
    {
        return $this->context;
    }
}


@* "https://github.com/playframework/twirl/issues/105" *@@if(List.empty[Txt]){}
