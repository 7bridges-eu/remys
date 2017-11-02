# remys

A Clojure library designed to use REST APIs with MySQL databases.

## Usage

``` clojure
lein run -- -u <username> -p <password> -d <database> start
```

The server will be listening on port `3000`.

You can specify the hostname and the port of the MySQL database you wish to
interact with:

``` clojure
lein run -- -H <hostname> -P <port> -u <username> -p <password> -d <database> start
```

Now you can interact with your database via REST. Examples:

- get all the records from `departments` table:
``` shell
$ curl -i -X GET 'http://localhost:3000/api/departments'
```

- get a specific record from `departments` table:
``` shell
$ curl -i -X GET 'http://localhost:3000/api/departments/d009'
```

- get only some columns from `departments` table:
``` shell
$ curl -i -X GET 'http://localhost:3000/api/departments?fields=dept-no,dept-name'
```

- run a custom query:
``` shell
$ curl -i -X POST 'http://localhost:3000/api/dynamic'\
   -H "Content-Type: application/json"\
   -d '{"query":"select * from salaries limit 100"}'
```

remys has been tested using the freely available
[test_db](https://github.com/datacharmer/test_db).

## License

Copyright © 2017 7bridges s.r.l.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
