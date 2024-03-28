




In order to run my implementation you need to:
 - Compile all the classes

In order to run Full Node:
 - Run CmdLineFullNode with following arguments:
   - Starting node name
   - Starting node address in the correct format(e.g: 127.0.0.1:10000)
   - Ip address on which current node will listen for the connections
   - Port number on which current node will listen for the connections
   - note that if you want to create a starting node for the network, address of the starting node and address on which current node will listen for the connection should be the same. (e.g: artem.korniienko@city.ac.uk:testStartingNode 127.0.0.1:10000 127.0.0.1 10000)

To run temporary node:
 - If you want to get info out of network Run CmdLineGet with following arguments
   - Starting node name
   - Starting node address in the correct format(e.g: 127.0.0.1:10000)
   - Key of the value that you want to get
 - If you want to put info inside the network Run CmdLineStore with following arguments
  - Starting node name
  - Starting node address in the correct format(e.g: 127.0.0.1:10000)
  - Key of the value that you want to put
  - Value that you want to put
 - If you want to find the closest node in the network to certain hashID Run CmdLineFindClosestNode with following arguments
  - Starting node name
  - Starting node address in the correct format(e.g: 127.0.0.1:10000)
  - HashID


My code provides full functionality of the 2D#4 network.
It can:
– Act as a temporary node as described in the RFC.
– Connect to the 2D#4 network using the name and address of a starting
node.
– Search the network to find the full node with a hashID closest to a
given hashID.
– Can store a (key, value) pair in the network.
– Given a key, can find the value from the network.
– Act as a full node as described in the RFC.
– Connect to the 2D#4 network using the name and address of a starting
node and notify other full nodes of its address.
– Handle inbound connections.
– Handle all requests and respond appropriately, including storing (key,
value) pairs
– Build and maintain a network map.
- Provide robustness and elasticity of the network.

