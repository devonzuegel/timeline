# TimelineJS

This project comes out of [this idea that I tweeted](https://twitter.com/devonzuegel/status/1322723232034680832):

>
> I'd love it if history books had little timelines on every page that displayed the entire time range discussed + dots corresponding to the date that events mentioned on that page occurred.
>
> This would really help with developing intuition for relative chronology + cause-effect!

You can find the mock for this project here: [🎨 Figma prototype](https://www.figma.com/proto/WCtjsMOhuRVIaJMnHtbKGx/Timeline.js?node-id=1%3A2&viewport=480%2C294%2C0.25218871235847473&scaling=contain)

This is just as a side project so I probably won't document this project that well, but contributions are certainly welcome if you can figure out what I'm trying to do!

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

## Things learned about Clojure while working on this project

I'm a Clojure newbie, and I don't like getting stuck on the same things twice. For the sake of my future self (and anyone else who might benefit from reading this list), here's a list of things I learned the hard way while working on this project.

- `for` is lazy, i.e. it only executes the values once you ask for them. `doseq` is like `for`, but it's not lazy.
- To access variables in your repl that are defined within a file, you need to enter that namespace via `(ns timeline.core)`.
- The shortcut form for `(fn )` is `#( )`
- You can import macros from a `clj` file → `cljs` file because they're run at compile time, but you cannot import functions from a `clj` file → `cljs` file because that would require running them at runtime.
- `utils.cljs?rel=1604213006322:71 ("http://localhost:3449/js/compiled/out/timeline/core.js")` (and the like) is printed in the console to log what files have been hot-reloaded.
- The figwheel repl executes Clojurescript, whereas `lein repl` executes Clojure.
- `macroexpand`

## Other things learned while working on this project
- CSS doesn't allow IDs to start with a number, but HTML does. It momentarily caused me confusion because HTML was more permissive, but CSS just didn't recognize the IDs I'd put in the HTML.