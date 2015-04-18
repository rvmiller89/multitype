# multitype
This plug-in for the Eclipse IDE offers software developers the ability to edit the same source file simultaneously, much like Google Docs. The tool facilitates collaboration on team projects in which editing the same file is a necessity. Multitype uses the Client-Server architecture with a single central server that manages connections between clients. In addition, there must be a host client, which provides the file(s) that will be shared with the other clients. The host client also stores the final copy 

## Editor Plug-in
The system consists of an editor plug-in that supports multiple files to be shared. The back-end of the plug-in will process the information provided by the front-end and send updates to the server which will forward the information to the clients with the file open. The front-end provides a user interface within an editor. After the user installs our plug-in in a compatible IDE, the user will be able to connect to the central server. Upon a successful connection, collaborative editing may begin. 

## Server
Communication and synchronization between clients will be handled by a dedicated central server. Updates will be received by the server and placed in a queue then forwarded individually to each client editing the file referenced in that particular edit. The server will modify updates in the rest of the queue before forwarding them to ensure they stay in order even with varying network lag among clients.

