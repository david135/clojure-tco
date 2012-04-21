Copyright (c) 2012 Chris Frisz, Daniel P. Friedman

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

* * * * *

## Bouncing Baby Clojure (BBC)

An embedded source-to-source compiler for Clojure that provides the
benefit of full tail-call optimization (TCO) to Clojure code.

Due to Clojure's adherence to Java calling conventions, Clojure is
unable to provide full support for constant-space tail calls as is
guaranteed by languages like Scheme or Standard ML. Standard Clojure
provides some support the `recur` form and the `trampoline`
function. The `recur` form is limited to self-recursion and using
`trampoline` requires manual code modification such that each
`trampoline`d piece of code returns a function of no arguments (or a
"thunk"). Additionally, `trampoline` doesn't allow functions to be
return values.

BBC aims to expand support for constant-space tail calls to include
self-recursion and arbitrary n-way mutual recursion returning any
value type, including function expressions disallowed by
`trampoline`.

BBC works by applying a first-order one-pass CPS algorithm (via
[Danvy](http://www.cs.au.dk/~danvy/index-previous.html) 2007), then
transforming the code to return thunks, and finally creating a custom
trampoline to be used when the code is executed. Thanks to the
properties of the CPS transformation, BBC will make all function calls
into tail calls, thereby even making non-tail code compiled by BBC use
constant space.

**Note**: the subset of Clojure currently accepted by BBC is very
small and will continue to grow.

## Usage

The key component of BBC is the `tco` macro. With BBC on your
classpath, include it with the following code:

```clojure
(use '(bbc :only (tco)))
```

Then simply wrap any piece of code that you want transformed with
`(tco ...)`. 

For example, consider the following (non-tail recursive) definition of
factorial:

```clojure
(defn fact
  [n]
  (if (zero? n)
      1
	  (* n (fact (dec n)))))
```

This can be compiled to use constant stack space recursive calls by
simply wrapping it in a call to `tco`:

```clojure
(tco
  (defn fact
    [n]
    (if (zero? n)
        1
  	    (* n (fact (dec n))))))
```

This will define `fact` in terms of the code transformations used by
BBC. Simply call `fact` as you would have without the BBC
transformations, and the rest is done for you.

* * * * *

## Contributing

Simply fork and use pull requests.

* * * * *

## Resources

A list of the resources for BBC transformations is as follows:

*
  [A First-Order, One-Pass CPS Algorithm](http://www.brics.dk/RS/01/49/BRICS-RS-01-49.pdf)
  
*
  [Using ParentheC to Transform Scheme Programs to C or How to Write Interesting Recurive Programs in a Spartan Host (Program Counter)](https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&ved=0CCUQFjAA&url=https%3A%2F%2Fwww.cs.indiana.edu%2Fcgi-pub%2Fc311%2Flib%2Fexe%2Ffetch.php%3Fmedia%3Dparenthec.pdf&ei=LNaST93BO4i46QHnyMCcBA&usg=AFQjCNG-Chb76N9lNVHO2ymtnAjo9Fvt0g&sig2=SR2itLI00reGEjRCrw-edQ&cad=rja)