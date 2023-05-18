#!/bin/bash

# Build the Docker image
docker build -t basik-site-spark .

# Run the Docker container
docker run -d -p 4567:4567 --name basik-spark --rm basik-site-spark