# Introduction
Confidential computing protects your data while it's being used (not just when stored or sent), by running it in 
a secure, isolated part of the computer called a trusted execution environment (TEE). 
This keeps it safe even from cloud providers or system administrators.

Edgeless Systems has created a Kubernetes Cluster for us.
We had to provide SSH keys to be able to login via SSH:
```shell
ssh -i [key] root@[cluster-name].confidential.cloud
```

# Preparation
It is important to retrieve the cluster-name from Edgeless colleagues and to replace in the whole project all occurrences of:
* [cluster-name]

with the real cluster-name.

# Deploying confidential app
0. Make sure contrast runtime and CLI versions are compliant:
```shell
contrast --version
```
(in our case we had to use 1.9.0)

1. Download Coordinator resource (coordinator.yml):
 ```shell
curl -fLO https://github.com/edgelesssys/contrast/releases/download/v1.9.0/coordinator.yml --output-dir deployment
```
   
2. Create resources from ```app/kubernetes/resources``` inside new directory in the host machine and put the coordinator.yml next to it.
3. Run generate:
 ```shell
 contrast generate --reference-values k3s-qemu-snp resources/
 ```
   
4. Fill missing TCB values in manifest.json (two places in the file):
 ```json
 "MinimumTCB": {
     "BootloaderVersion": 9,
     "TEEVersion": 0,
     "SNPVersion": 23,
     "MicrocodeVersion": 72
 },
 ```

**ATTENTION!**
> On bare-metal SEV-SNP, contrast generate is unable to fill in the MinimumTCB values as they can vary between platforms. 
> They will have to be filled in manually. If you don't know the correct values use 
> {"BootloaderVersion":255,"TEEVersion":255,"SNPVersion":255,"MicrocodeVersion":255} 
> and observe the real values in the error messages in the following steps. 
> This should only be done in a secure environment. Note that the values will differ between CPU models.

5. Run apply:
 ```shell
 kubectl apply -f resources/coordinator.yml
 ```
   
6. Get LB address:
 ```shell
 coordinator=$(kubectl get svc coordinator -o=jsonpath='{.status.loadBalancer.ingress[0].ip}')
 ```
   
7. Set manifest:
 ```shell
 contrast set -c "${coordinator}:1313" resources/
 ```
   
8. Run apply:
 ```shell
 kubectl apply -f resources/
 ```

# Pushing Docker image
1. Build Docker image here in the project:
 ```shell
 docker build -t my-confidential-app .
 ```

2. Copy Docker image to SSH host:
 ```shell
 docker save -o demo.tar my-confidential-app:latest
 ```
   
 ```shell
 scp demo.tar root@[cluster-name].confidential.cloud:/tmp/
 ```
3. Load image on the cluster (host):
 ```shell
 docker load -i demo.tar
 ```

4. Set proper tag:
 ```shell
 docker tag my-confidential-app:latest registry.[cluster-name].confidential.cloud:5000/demo:latest
 ```
   
5. Push to repository:
 ```shell
 docker push registry.[cluster-name].confidential.cloud:5000/demo:latest
 ```

# Get & Investigate Memory Dump
First, you can simply send request to the application load balancer like this:
```shell
curl --location 'https://[cluster-name].confidential.cloud/client/v1' \
--header 'Content-Type: application/json' \
--data '{
    "name": "Name",
    "surname": "SecretString"
}'
```

And then, to get memory dumps and see that the data is not available in plain text:

1. Get container id of the confidential app:
 ```shell
 kubectl get pod [pod-name] -n default -o jsonpath='{.status.containerStatuses[0].containerID}'
 ```

2. Get PID of the process:
 ```shell
 crictl inspect [container-id] | grep pid
 ```

3. Get memory dump:
 ```shell
 gcore [pid]
 ```

4. Investigate memory dump
 ```shell
 strings core.[pid] | grep SecretString
 ```
   
# Important Notes
* More instruction can be found in the official documentation:
  * https://docs.edgeless.systems/contrast/deployment?platform=k3s-qemu-snp
* Contract runtime and CLI versions must match!
* TLS must be active between the end-user and our application.
  * For that, the easiest way is to configure service-mesh.
  * Service mesh is created using **lb-service.yml** resource.
* You can use ```app/kubernetes/plain_resources``` to  demonstrate also plain app, without confidential computing, and compare both memory dumps.
* Make sure that there is enough RAM set in the **deployment.yml** of our app for the confidential container (minimum 2x the size of the Docker image).
  * The image will be loaded to the memory in the runtime.