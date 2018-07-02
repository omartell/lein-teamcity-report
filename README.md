# lein-teamcity-report

A Leiningen plugin to report test runs and artifacts to TeamCity.

## Usage

Put `[lein-teamcity-report "0.1.0"]` into the `:plugins` and `:dependencies` vector of your project.clj like so:

```clojure
{:test {:plugins      [[lein-teamcity-report "0.1.0"]]
        :dependencies [[lein-teamcity-report "0.1.0"]]}}
```

## Differences with other plugins

This plugins follows the same multi-method contract of `clojure.test/report`, so it's a good option to use when you need to extend one of the existing methods.

## Thanks

This plugin was inspired by http://github.com/nd/lein-teamcity.

## License

Copyright © 2018 Oliver Martell

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
