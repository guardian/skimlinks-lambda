# skimlinks-lambda

A lambda function to periodically update a file in S3 with a list of domains supported by skimlinks.com, fetched from the skimlinks API.

You can run the lambda locally with `sbt run <skimlinks apikey> <skimlinks accountid> <bucket to store domains in> <key for file>`