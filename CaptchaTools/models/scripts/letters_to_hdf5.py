import numpy as np
import h5py
import sys
import caffe

SIZE = 64 # fixed size to all images

# be sure to change for every net
img_path = '../../images/letters/'
file_labels = 'letter_labels.txt'
outputH5_prefix = 'train_letters'


# transformer = caffe.io.Transformer({'data': net.blobs['data'].data.shape})
# transformer.set_transpose('data', (2,0,1))

y = np.loadtxt(file_labels)
NUM_IMGS = y.shape[0]
X = np.zeros( (NUM_IMGS, 1,SIZE, SIZE), dtype='f4' )


for i in range(NUM_IMGS):
	if i % 10 == 0:
		print i

	img = caffe.io.load_image(img_path + "image%06d.png" % (i+1))
	img = caffe.io.resize_image( img, (SIZE, SIZE), interp_order=3 ) # resize to fixed size
	# img = img.transpose((2,0,1))

	# you may apply other input transformations here...
	X[i] = img[:,:,0]

# X = np.multiply(X, 255) 

outputH5 = outputH5_prefix + ".h5"

with h5py.File(outputH5,'w') as H:
    H.create_dataset( 'data', data=X ) 
    H.create_dataset( 'label', data=y )


with open(outputH5_prefix + '_h5_list.txt','w') as L:
    L.write(outputH5)


