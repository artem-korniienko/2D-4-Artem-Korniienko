# Distributed 2D#4 Network Protocol (Java)

This project is an implementation of a distributed network protocol based on the **2D#4 topology** as described in the RFC. The protocol enables nodes to form a robust, scalable, and fault-tolerant distributed hash network capable of storing and retrieving key–value pairs.  
The system supports both **temporary nodes** and **full nodes**, each contributing differently to the network.

---

## Features

### Temporary Node Capabilities
A temporary node is a lightweight participant in the network. It can:

- Act as a **temporary node** as defined in the RFC.  
- **Connect** to the 2D#4 network using a starting node’s name and address.  
- **Search** the network to find the full node whose **hashID is closest** to a given hashID.  
- **Store a (key, value) pair** in the network by routing it to the appropriate full node.  
- **Retrieve a value** from the network given a key.  

### Full Node Capabilities
A full node contributes to network operations, stability, and storage. It can:

- Act as a **full node** as defined in the RFC.  
- **Connect to the 2D#4 network** using a starting node and **announce** its presence to other full nodes.  
- **Handle inbound connections** from both temporary and full nodes.  
- **Respond to all request types**, including lookup, routing, and store operations.  
- **Store (key, value) pairs** assigned to it.  
- **Build and maintain a network map** for routing and topology awareness.  
- Provide **robustness and elasticity**, allowing the network to handle churn and failures gracefully.  

---

### Technologies Used
- **Java 17+**  
- Java **Concurrency API** (Standard thread library)  
- **TCP/UDP** sockets  
