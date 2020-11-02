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