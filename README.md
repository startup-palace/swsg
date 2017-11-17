Safe Web Services Generator
===========================

SWSG is a tool that verifies the consitency of web services models and generate executable web services from them.

## How to use

- install Java (JRE/JDK 8)
- run the precompiled JAR: `java -jar swsg.jar --help`
- **or** run from SBT: `./sbt "run --help"`

You can check the [example model](examples/registration/registration.model) by running:

```text
java -jar swsg.jar check --model examples/registration/registration.model
```

You can generate web services from the [example model](examples/registration/registration.model) and the [example implementation](examples/registration/impl/) by running:

```text
java -jar swsg.jar gen --model examples/registration/registration.model --implementation examples/registration/impl/ --backend laravel --output examples/registration/output/
```

*The generated app can be run following the same step as done in the `test_gen_registration_example` in [.gitlab-ci.yml](.gitlab-ci.yml).*

## How to compile

- install Java (JRE/JDK 8)
- run SBT: `./sbt`
- compile: `swsgJVM/compile`
- run tests: `swsgJVM/test`
- generate a JAR: `swsgJVM/assembly` (the JAR will be in `jvm/target/scala-2.12/swsg.jar`)

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
