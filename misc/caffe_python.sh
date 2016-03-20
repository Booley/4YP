#!/bin/bash

# Directives
#PBS -N test-python-caffe
#PBS -M bo.moon@st-annes.ox.ac.uk
#PBS -m abe
#PBS -q parallel

# Set output and error directories
#PBS -o localhost:/scratch/bhmoon/Documents/Reports/caffe_train/results_${PBS_JOBID}.out
#PBS -e localhost:/scratch/bhmoon/Documents/Reports/caffe_train/results_${PBS_JOBID}.err

cd /scratch/bhmoon/caffe/python
python /scratch/bhmoon/4YP/misc/run_caffe.py