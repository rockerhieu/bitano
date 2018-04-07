'use strict';

const vision = require('@google-cloud/vision');

exports.http = (request, response) => {
  console.log(request.query);
  const uri = request.query.uri;
  if (uri == null) {
    response.status(400).json({ error: "Must include a 'uri' query parameter." });
  };

  // Creates a client
  const client = new vision.ImageAnnotatorClient();

  const image = {
    source: {
      imageUri: uri
    }
  };

  client.labelDetection(uri)
    .then((results) => {
      const annotations = results[0].labelAnnotations;
      const labels = annotations.map(a => a.description);
      const resp = {
        image: uri,
        labels
      };
      response.status(200).json(resp);
    })
    .catch((err) => {
      console.log(err);
      response.status(200).send(err)
    });
};