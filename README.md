# peppol-processor [![CircleCI](https://circleci.com/gh/OpusCapita/peppol-processor.svg?style=svg)](https://circleci.com/gh/OpusCapita/peppol-processor)

Peppol OpusCapita Access Point processor service running on Andariel Platform.

The service reads files from the `peppol.processor.queue.in.name:peppol-processing` queue and processes them. The processing includes:

* Moving file to long-term storage
* Creating metadata if it doesn't exists in the container message
* Validating metadata of the file
* Fetching route config and setting it to the container message

After processing, it sends the container message to the next service in the route config.

Please check the wiki pages for more information:
* [Preprocessing](https://opuscapita.atlassian.net/wiki/spaces/IIPEP/pages/107806873/New+Peppol+solution+modules+description#NewPeppolsolutionmodulesdescription-preprocessing)
* [Internal Routing](https://opuscapita.atlassian.net/wiki/spaces/IIPEP/pages/107806873/New+Peppol+solution+modules+description#NewPeppolsolutionmodulesdescription-internal-routing)