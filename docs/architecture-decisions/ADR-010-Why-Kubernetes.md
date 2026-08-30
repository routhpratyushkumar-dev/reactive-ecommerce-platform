# ADR-010 — Why Kubernetes?
## Status

Accepted

## Decision

Use Kubernetes for production-like orchestration.

For local development, use Kind.

## Architecture
           Kubernetes Cluster
                   |
     +-------------+-------------+
     |             |             |
     v             v             v
    Auth Pod     Product Pod    Order Pod
     |             |             |
     +-------------+-------------+
                   |
                Services

## Kubernetes Concepts to Learn
* Pods
* Deployments
* Services
* ConfigMaps
* Secrets
* Ingress
* Horizontal Pod Autoscaler
* Rolling deployments
* Health probes
* Resource requests and limits

## Docker Compose vs Kubernetes
### Docker Compose

Best suited for:

* Local development
* Infrastructure dependencies
* Quick integration environments

### Kubernetes

Best suited for:

* Container orchestration
* Scaling
* Service management
* Self-healing
* Production-like deployments

Both technologies will be used for different purposes.