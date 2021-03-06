#!/bin/bash
#PBS -N caffe-train-ocr
#PBS -M bo.moon@st-annes.ox.ac.uk
#PBS -m abe
#PBS -q parallel
#PBS -V
#PBS -o localhost:/scratch/bhmoon/Documents/Reports/caffe_train/results_${PBS_JOBID}.out
#PBS -e localhost:/scratch/bhmoon/Documents/Reports/caffe_train/results_${PBS_JOBID}.err
export LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu/
export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:/lib:/scratch/bhmoon/Documents/libs/lib/x86_64-linux-gnu:/scratch/bhmoon/Documents/libs/lib/atlas-base/:/scratch/bhmoon/Documents/libs/lib/


cd /scratch/bhmoon/4YP/CaptchaTools/models
/scratch/bhmoon/caffe/build/tools/caffe.bin train -solver=letter_solver.prototxt
