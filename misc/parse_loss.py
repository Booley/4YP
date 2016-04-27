
import matplotlib.pyplot as plt
import sys

log_file = sys.argv[1]
subsample = 1
start = 1

losses = []
name = ""
for line in open(log_file):
	tail = line.split(" ")[-3:]
	if tail[0] == "loss" and tail[1] == "=":
		losses.append(float(tail[2]))
	elif tail[0] == "name:":
		name = tail[1][1:-2]

print "Initial loss:", losses[0]
print "Final loss:", losses[-1]
print "Lowest loss:", min(losses)

plt.plot(losses[start:len(losses):subsample])
plt.ylabel("Losses")
plt.xlabel(name)
plt.show()

