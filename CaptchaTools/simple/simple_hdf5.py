import numpy as np
import h5py
import sys

file_labels = 'simple_labels.txt'
outputH5_prefix = 'simple'

X = np.zeros((100*100, 1, 1, 2))
y = np.zeros((100*100))
for i in range(100):
	for j in range(100):
		f = 3 * i + 5 * j

		# X.append([i / 100.0, j / 100.0])
		# y.append([f / 8.0])
		X[100*i + j,0,0,:] = [i/100.0, j/100.0]
		y[100*i + j] = f/8.0

outputH5 = outputH5_prefix + ".h5"

with h5py.File(outputH5,'w') as H:
    H['data'] = X
    H['label'] = y


with open(outputH5_prefix + '_h5_list.txt','w') as L:
    L.write(outputH5)


