
import matplotlib.pyplot as plt

log_file = "results_76175.headnode.err"
subsample = 10
start = 100

losses = []
name = ""
for line in open(log_file):
	tail = line.split(" ")[-3:]
	if tail[0] == "loss" and tail[1] == "=":
		losses.append(float(tail[2]))
	elif tail[0] == "name:":
		name = tail[1][1:-2]

plt.plot(losses[start:len(losses):subsample])
plt.ylabel("Losses")
plt.xlabel(name)
plt.show()