# Eggs in a box! [![Build Status](https://travis-ci.org/gfx/gradle-android-eggbox-plugin.svg)](https://travis-ci.org/gfx/gradle-android-eggbox-plugin)

[![Eggbox](eggbox-by-Lord-Inquisitor.jpg)](https://www.flickr.com/photos/13453980@N06/8372033532)

Eggbox plugin locks dependencies for Android applications,
just like as Ruby's [Bundler](http://bundler.io/) or Perl's [Carton](https://github.com/perl-carton/carton).

This is just a proof-of-concept. Do not use this plugin in the production!

## Project Structure

```
plugin/   - The main module of a Gradle plugin
example/  - An example android application that uses this plugin
buildSrc/ - A helper module to use this plugin in example modules
```

## Release Engineering

Copy `gradle.properties.sample` into `~/.gradle/gradle.properties` and set correct values.

Note that `PGP_KEY_ID` is the value that `gpg --list-secret-keys` shows.


## See Also

## Author and License

Copyright 2015, FUJI Goro (gfx) <goro-fuji@cookpad.com>. All rights reserved.

This library may be copied only under the terms of the Apache License 2.0, which may be found in the distribution.
