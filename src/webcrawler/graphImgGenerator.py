import networkx as nx
import csv
import matplotlib.pyplot as plt

graph = nx.Graph() #use DIGraph() for directed graph

csvfile = open('mapping.csv')
rows = csv.reader(csvfile)
rowKey = {}
numRows = 0

# setup mapping dictionary
for row in rows:
    print ("Created edges: ", numRows)
    rowKey[row[0]] = numRows
    numRows += 1

csvfile = open('mapping.csv')
rows = csv.reader(csvfile)

workingRow = 0

for row in rows:
    print ("Eval edges for row ", workingRow, " of ", numRows)
    for col in row:
        if not (col in rowKey):
            rowKey[col] = numRows
            numRows += 1
        graph.add_edge(workingRow, rowKey[col])
    workingRow += 1

print ("Num rows: ", numRows)
print ("Saving row key to imgKey.csv")
for k, v in rowKey.items():
    print (v, k)

with open('imgKey.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerows(rowKey.items())

print("Generating size nodes")
node_size = [float(50 * graph.degree(v)) for v in graph]
print("Generating node Colors")
node_color = [float(graph.degree(v)) for v in graph]
#pos=nx.spring_layout(graph,iterations=graph.size())

print("Printing pictures")
print("Printing norm picture")
plt.clf()
plt.figure(figsize=(200,200))
print("nxdraw")
nx.draw(graph, node_color=node_color, node_size=node_size)
print("plt save")
plt.savefig("path_full.png")

print("Printing cir picture")
plt.clf()
print("nxdraw")
nx.draw_circular(graph, node_color=node_color, node_size=node_size)
print("plt save")
plt.savefig("path_full_cir.png")

print("Printing rand picture")
plt.clf()
print("nxdraw")
nx.draw_random(graph, node_color=node_color, node_size=node_size)
print("plt save")
plt.savefig("path_full_rand.png")

"""print("Printing spectral picture")
plt.clf()
nx.draw_spectral(graph, node_color=node_color, node_size=node_size)
plt.savefig("path_full_spectral.png")

print("Printing spring picture")
plt.clf()
nx.draw_spring(graph, node_color=node_color, node_size=node_size)
plt.savefig("path_full_spring.png")

print("Printing shell picture")
plt.clf()
nx.draw_shell(graph, node_color=node_color, node_size=node_size)
plt.savefig("path_full_shell.png")"""
