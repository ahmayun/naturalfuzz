# Artificat: NaturalFuzz: Natural Input Generation for Big Data Analytics
## 1. Pre-requisites
This manual assumes `docker` is installed and set up properly on your device.\
These instructions have been tested with the following configurations: 
* **Ubuntu:** 20.04.6 LTS\
  **Docker:** 24.0.2, build cb74dfc
## 2. Creating the Docker Image
Clone this repository:
```
git clone https://github.com/ahmayun/naturalfuzz.git
```
Inside the repository folder build the docker image using:
> **_NOTE:_** Depending on how docker is configured, you may need to `sudo` this
```
docker build . -t naturalfuzz --no-cache
```
> **_TROUBLESHOOTING:_** If the above command fails try restarting the docker service using: `sudo systemctl restart docker`

## 3. Running the Container
Obtain a shell instance inside the docker container:
```
docker run -v ./graphs:/NaturalFuzz/graphs -it naturalfuzz bash
```
## 4. Running the Experiments
Navigate into the repository folder:
```
cd naturalfuzz
```
The following is the template command for running any of the benchmark programs:
```
./run-fuzzer.sh <PROGRAM_NAME> <DURATION> <DATASETPATH_1> ... <DATASETPATH_N>
------------
PROGRAM_NAME = Q1|Q3|Q6|Q
```
For this example we will run Q1

## 5. Computing Perplexity Scores

