#!/bin/bash

# Directives
#PBS -N sample-job
#PBS -M bo.moon@st-annes.ox.ac.uk
#PBS -m abe
#PBS -q parallel


# Set output and error directories
#PBS -o localhost:/scratch/bhmoon/Documents/Reports/results_${PBS_JOBID}.out
#PBS -e localhost:/scratch/bhmoon/Documents/Reports/results_${PBS_JOBID}.err
cd /scratch/bhmoon/4YP/misc

javac HelloWorld.java
java HelloWorld
