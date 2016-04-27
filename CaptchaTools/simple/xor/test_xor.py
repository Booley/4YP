import numpy as np
import caffe

net = caffe.Net("xor_deploy.prototxt", "snapshot_iter_30000.caffemodel", caffe.TEST)

net.blobs['data'].reshape(1, 1, 1, 2)
net.blobs['data'].data[...] = [1, 0]

output = net.forward()
print output