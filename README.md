# Introduction
Confidential computing protects your data while it's being used (not just when stored or sent), by running it in 
a secure, isolated part of the computer called a trusted execution environment (TEE). 
This keeps it safe even from cloud providers or system administrators.

More and more clients are turning to Confidential Computing due to growing concerns about data privacy, security, and compliance. This technology allows data to remain encrypted even during processing, reducing the risk of unauthorized access—even from cloud providers themselves. In countries like Germany, strict regulations such as GDPR and the impact of the Schrems II ruling make it risky to store or process sensitive data on US-based cloud platforms like AWS. The fear of exposure to the US Cloud Act and lack of control over data location drives clients to seek alternatives.

Here you can read more about the Contrast solution from one of our partners - Edgeless Systems:
* https://docs.edgeless.systems/contrast/

Edgeless Systems has created a Kubernetes Cluster for us **on the Hertzner machine that supports the confidential computing** (this must be a very specific hardware!). The demo was deployed on Hertzner dedicated machine, however, it can also be deployed to e.g. Azure AKS (Kubernetes service) or AWS EKS, with some limitations (eg. harder access to the physical host to take heap dumps).

The Hetzner Machine that was provided for the Demo was an AX162-R:
* https://www.hetzner.com/dedicated-rootserver/ax162-r/

We can request for the machine on our own, and just install Edgeless solutions there.

As the colleagues created the machine for us, we had to provide only the SSH keys to the Edgeless colleagues, to be able to login via SSH, and proceed with the demo:
```shell
ssh -i [key] root@[cluster-name].confidential.cloud
```

# The Demo Idea
The idea of the demo is to deploy two clusters - one standard, and one confidential - and present that taking heap dumps from the Java process on the standard cluster results in a plain text data displayed in the console, while taking heap dumps from the confidential cluster results in an encrypted data.


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

**ATTENTION!**
> You can do the same thing for the **plain_resources**, to run (in parallel) the non-confidential application.
> Then, you can later take heap dumps from both Java processes and present the difference between confidential & non-confidential setup.

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

The same image will be used for both confidential and non-confidential apps.

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

Here make sure to target both load balancers (confidential & non-confidential) to be able to take both heap dumps later.

And then, to get memory dumps:

1. Get container id of the confidential app (or the plain app):
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
