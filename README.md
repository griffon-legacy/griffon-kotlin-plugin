
Brings the Kotlin language compiler and libraries
-------------------------------------------------

Plugin page: [http://artifacts.griffon-framework.org/plugin/kotlin](http://artifacts.griffon-framework.org/plugin/kotlin)


The Kotlin plugin enables compiling and running [Kotlin]][1] code on your Griffon application. Kotlin is a statically
typed language for the JVM that has a great level of interoperability with Java, and so with Groovy too.

Usage
-----
You must place all Kotlin code under `$appdir/src/kotlin`, it will be compiled first before any sources available on 
`griffon-app` or `src/main` which means you can't reference any of those sources from your Kotlin code, while the
Groovy sources can.


[1]: http://jetbrains.com/kotlin
[2]: /plugin/lang-bridge

