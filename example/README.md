Vessel Example
==============

Usage
-----

### Local processing

```bash
rsync -avv --delete example/data-input/ example/data/
```

```bash
lein run -m vessel.process example/manifest.edn 201310
```

```bash
ls example/data/output/classes/
```

### S3/EC2 processing


#### Configure your AWS credentials


```bash
lein pallet add-service test-aws aws-ec2 "your-aws-key" "your-aws-secret-key"
```

This will create a configuration file in `~/.pallet/services/test-aws.clj` with
your AWS credentials.


#### Edit your S3 bucket in examples/manifest.edn

#### Create directories in your S3 bucket

#### Put example input files in your S3 bucket

#### Deploy your Vessel

```bash
lein uberjar
cd target/
java -cp vessel-0.4.0-SNAPSHOT-standalone.jar clojure.main -m vessel.deploy ../example/manifest.edn 201310
```
