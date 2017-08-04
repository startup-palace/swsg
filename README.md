Safe Web Services Generator
===========================

SWSG is a tool that verifies the consitency of web services models and generate executable web services from them.

## How to use

- install Java (JRE/JDK 8)
- run the precompiled JAR: `java -jar swsg.jar --help`
- **or** run from SBT: `./sbt "run --help"`

You can check the [example model](example/registration.model) by running:

```text
java -jar swsg.jar check --model example/registration.model
```

You can generate web services from the [example model](example/registration.model) and the [example implementation](example/impl/) by running:

```text
java -jar swsg.jar gen --model example/registration.model --implementation example/impl/ --backend laravel --output example/output/
```

*The generated app can be run following the same step as done in the `test_gen_example` in [.gitlab-ci.yml](.gitlab-ci.yml).*

## How to compile

- install Java (JRE/JDK 8)
- run SBT: `./sbt`
- compile: `compile`
- run tests: `test`
- generate a JAR: `assembly` (the JAR will be in `target/scala-2.12/swsg.jar`)

## License

See [the LICENSE file](LICENSE).

```text
Copyright 2017 David Sferruzza / Startup Palace

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
