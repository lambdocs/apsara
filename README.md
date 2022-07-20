# Apsara

Editor macros in [ClojureScript](http://github.com/clojure/clojurescript).

Apsara is a ClojureScript library for building editors. It has been tested
in browsers, but should work in any environment that exposes a
[DOM](https://developer.mozilla.org/en-US/docs/Web/API/Document_Object_Model)
like tree (like [React Native](https://reactnative.dev/)).

[![Clojars Project](https://img.shields.io/clojars/v/net.clojars.ajaym/apsara.svg?include_prereleases)](https://clojars.org/net.clojars.ajaym/apsara)

## Yet Another ...
There are many editors out there, so why another one? While other editors are
stand alone applications Apsara is a library meant to be embedded in an
application. Spreadsheets, application consoles, REPL interfaces and [Literate
Programming documents](https://www-cs-faculty.stanford.edu/~knuth/lp.html)
to name a few.

## Usage
The simplest way to use Apsara is install it with
[Reagent](https://github.com/reagent-project/reagent#usage).
``` sh
lein new reagent myproject
```
Then add Apsara as a dependecy in project.clj.
``` clojure
[net.clojars.ajaym/apsara "0.1.0"]
```

## Design
### REDL - Read Evaluate Display Loop
If a [REPL](https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop)
loop only appends data to the bottom of the screen, then a REDL loop
is capable of updating any part of the screen.

#### READ.
As text is entered, input is read. This includes handling of non-printable
characters like delete, or input from a mouse. The meaning of these
inputs is determined in the next phase.

#### EVALUATE.
There are at least two levels of evaluation. First the evaluation of the
particular input operation - like appending or deleting a character. Second,
evaluation of the string input. Consider this JavaScript snippet.
``` javascript
ar.map((t) => t.true ? 1 : 0).reduce((total, num) => total + num);
```
If the editor needs to highlight the syntax then it is required to
_understand_ JavaScript in some way. Ususally this is done by matching
the string against a set of regular expressions. But this is not the only
way. On a console, for example, it could mean dispatching the string to
a remote server. So whatever needs to be done here is context specific.

#### DISPLAY.
After the input is evaluated the display needs to be updated. Apsara's
display component, also called the renderer, exposes a
[DOM](https://developer.mozilla.org/en-US/docs/Web/API/Document_Object_Model)
like model called [hiccup](https://reactnative.dev/) that can be
manipulated with the
[VDOM](https://github.com/lambdocs/apsara/blob/main/src/apsara/vdom.cljs)
APIs. In the browser, the hiccup model is rendered as
[React](https://reactjs.org/) components using the excellent
[Reagent]() library.

#### LOOP.
The loop ties everything together. Since Apsara is part of a larger
application the loop is usually run by the hosting platform. For
browser based applications the loop is part of the JavaScript
evaluation.

### A Shell
All parts of REDL can be swapped making Apsara's core very small. If
there is one thing that is novel about Apsara, it is the formalization
of the idea that editors can be built from these four parts. 

## License

Copyright © 2022 Ajay Mendez

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
