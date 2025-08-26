# skimlinks-lambda

A lambda function to periodically update a file in S3 with a list of domains supported by skimlinks.com, fetched from the skimlinks API.

You can run the lambda locally in your terminal by running `sbt run <skimlinks apikey> <skimlinks publisher id> <skimlinks client id> <skimlinks client secret> gu-skimlinks-store skimlinks/skimlinks-domains.csv`
