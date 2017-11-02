# remys

A Clojure library designed to use REST APIs with MySQL databases.

## Usage

``` clojure
lein run -- -u <username> -p <password> -d <database> start
```

The server will be listening on port `3000`.

You can specify the hostname and the port of the your MySQL database:

``` clojure
lein run -- -H <hostname> -P <port> -u <username> -p <password> -d <database> start
```

## License

Copyright © 2017 7bridges s.r.l.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
