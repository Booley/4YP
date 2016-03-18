import numpy as np
import h5py
import sys
import caffe

SIZE = 30 # fixed size to all images
NUM_IMGS = 21204
file_labels = '../letter_labels.txt'

# transformer = caffe.io.Transformer({'data': net.blobs['data'].data.shape})
# transformer.set_transpose('data', (2,0,1))

X = np.zeros( (NUM_IMGS, 3,SIZE, SIZE), dtype='f4' )
y = np.loadtxt(file_labels)

for i in range(NUM_IMGS):
	if i % 10 == 0:
		print i

	img = caffe.io.load_image("../images/image%06d.png" % (i))
	img = caffe.io.resize_image( img, (SIZE, SIZE), interp_order=3 ) # resize to fixed size
	img = img.transpose((2,0,1))

	# you may apply other input transformations here...
	X[i] = img

# X = np.multiply(X, 255)

outputH5 = 'train_letters.h5'

with h5py.File(outputH5,'w') as H:
    H.create_dataset( 'data', data=X ) 
    H.create_dataset( 'label', data=y )


with open('train_letters_h5_list.txt','w') as L:
    L.write(outputH5)


