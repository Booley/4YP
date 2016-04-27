import numpy as np
import caffe

net = caffe.Net("simple_deploy.prototxt", "snapshots/simple_net_iter_40000.caffemodel", caffe.TEST)

net.blobs['data'].reshape(1, 1, 1, 2)
net.blobs['data'].data[...] = [10, 100]

output = net.forward()
print output