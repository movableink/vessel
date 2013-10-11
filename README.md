Vessel
======

> “Thou art too damned jolly. Sail on.”

> Herman Melville, Moby-Dick


Background
----------

Deploys an EC2 instance (a "Vessel") to ship and process CSV files from one S3
keyspace to another S3 keyspace. Vessel routes CSV input rows to configurable
directory structures (keyspaces).


Configuration
-------------

[Pallet](http://palletops.com/) is used to deploy the EC2 Vessel.


```bash
lein pallet add-service my-aws aws-ec2 "your-aws-key" "your-aws-secret-key"
```

This will create a configuration file in `~/.pallet/services/my-aws.clj` with
your AWS credentials.


Main entry point
----------------

With [Leiningen](https://github.com/technomancy/leiningen)

```bash
lein run -m vessel.deploy path/to/manifest.edn 201310
```

Or as a standalone JAR:

```bash
java -cp vessel-0.4.0-SNAPSHOT-standalone.jar clojure.main -m vessel.deploy path/to/manifest.edn 201310
```

This deploys an EC2 instance that:

  1. Downloads compressed reports from `s3://bucket/201310/part-r-*.gz`
  2. Processes every file, line by line, routing input lines to output files
     based on rules defined in `manifest.edn`
  3. Compresses and ships results to S3
  4. Decommissions itself

There are no S3 transfer or bandwidth fees because the EC2 Vessel is deployed in
the same AWS region as the S3 buckets.


Process steps
-------------

Running the **Main entry point** command is equivalent to running five steps.


### Step 1 of 5: Provision & launch EC2 Vessel


```bash
lein run -m vessel.launch path/to/manifest.edn 201310
```
or

```bash
java -cp vessel-0.4.0-SNAPSHOT-standalone.jar clojure.main -m vessel.launch path/to/manifest.edn 201310
```


### Step 2 of 5: Fetch input from S3 directories


```bash
lein run -m vessel.input path/to/manifest.edn 201310
```

or

```bash
java -cp vessel-0.4.0-SNAPSHOT-standalone.jar clojure.main -m vessel.input path/to/manifest.edn 201310
```

This will fetch all files from

`s3://my-s3-bucket/path/to/files/201310/part-*.gz`

and place them locally in

`data/input/path/to/files/201310/part-*.gz`


### Step 3 of 5: Process input


```bash
lein run -m vessel.process path/to/manifest.edn 201310
```

or

```bash
java -cp vessel-0.4.0-SNAPSHOT-standalone.jar clojure.main -m vessel.process path/to/manifest.edn 201310
```

This will process & transform all lines from files in

`data/input/path/to/input/201310/part-*.gz`

and place them into files of the form

`data/output/paths/:column-01/:column-02/:yyyymmdd/:yyyymmdd_:yyyymmdd-000NN.csv.gz`


### Step 4 of 5: Put output to other S3 directories


```bash
lein run -m vessel.output path/to/manifest.edn 201310
```

or

```bash
java -cp vessel-0.4.0-SNAPSHOT-standalone.jar clojure.main -m vessel.output path/to/manifest.edn 201310
```

This will put all files from

`data/output/paths/:column-01/:column-02/:yyyymmdd/:yyyymmdd_:yyyymmdd-000NN.csv.gz`

to S3 keys of the form

`s3://my-s3-bucket/paths/:column-01/:column-02/:yyyymmdd/:yyyymmdd_:yyyymmdd-000NN.csv.gz`


### Step 5 of 5: Decommission EC2 Vessel


```bash
lein run -m vessel.decommission path/to/manifest.edn 201310
```

or

```bash
java -cp vessel-0.4.0-SNAPSHOT-standalone.jar clojure.main -m vessel.decommission path/to/manifest.edn 201310
```

License
-------

Copyright © 2013 Movable, Inc.

Distributed under the [Eclipse Public License](https://raw.github.com/movableink/vessel/master/LICENSE), the same as Clojure.
