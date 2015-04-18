# Introduction #

Every client wishing to collaboratively share files will need the `MultiType` plug-in installed in an Eclipse 3.6+ environment.  The server can be run on a separate computer with a port-forwarded connection.


# Installation #

## `MultiType` Plugin ##

  1. Download the `MultiType` JAR file available in the Downloads section.
  1. Locate your Eclipse directory on your computer.
    * Linux: This will be where you unzipped your Eclipse download
    * Mac: This will be in `/Applications`
    * Windows: This will be where you unzipped your Eclipse download
  1. Drag the JAR file into the **dropins** folder in your Eclipse directory.
  1. Restart Eclipse.
  1. Load the `MultiType` Perspective (Window > Open Perspective > Other...) or manually open a File List and User List view (Window > Show View > Other...)


## Server ##

  1. Download the Server JAR file available in the Downloads section.
  1. Execute `java -jar MultiType_Server_[x.x.x].jar [port #]`
    * (e.g. `java -jar MultiType_Server_0.3.1.jar 1337`)