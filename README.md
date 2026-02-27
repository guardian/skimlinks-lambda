# skimlinks-lambda

A lambda function to periodically update a file in S3 with a list of domains supported by skimlinks.com, fetched from the skimlinks API.

## Alarm and Alerting

This Lambda includes a number of checks which will trigger the alarm:
- **Drop-off in merchants:** If the number of domains fetched from the Skimlinks API drops by more than 20% compared to the previous run (as stored in S3). This error does not prevent the upload of the file so we should flag this to Skimlinks to see if it was expected.
- **Empty Domain List:** If the Skimlinks API returns an empty domain list. This may indicate an upstream failure or misconfiguration.
- **Access Token Failure:** If the Lambda fails to obtain an access token from the Skimlinks API. This may indicate credential or API issues.

If we are unable to upload the S3 file, because it is empty, or we haven't been able to fetch it we keep the previous version of the file so Skimlinks can continue to work.

## Running Locally

You can run the lambda locally in your terminal by running `sbt run <skimlinks apikey> <skimlinks publisher id> <skimlinks client id> <skimlinks client secret> gu-skimlinks-store skimlinks/skimlinks-domains.csv`