

Python Code Explination
The following python code is compiled in python 3.4 64 bit. We found it necessary to use a 64-bit version of python because of the large size of the graphs. There were not enough address locations in 32-bit.

The standard csv library is used to read in the mapping file. The file is first read and each node is added to the mapping file. This file will serve as a key to the picture. It maps the numbers to the URLs.

Following the creation of the mapping file. Each node and edge are created using the networkx library. We found this to be the easiest way to create a graph. The graph really should be directed, but to avoid clutter in the image we use an undirected graph. The code then generates the node size and node color of each node in the graph. This is done by evaluating the number of connections to each node. The more connections (or degrees) the larger the node.

The networkx library uses the matplotlib library to plot all of its graphs. After specifiying parameters we print a default graph, circle graph, random graph, spectral graph, a spring graph and a shell graph. The generation and saving of these images is what takes the longest amount of time. Each image was around 150 megabytes.
