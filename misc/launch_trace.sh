#!/bin/bash

# Directives
#PBS -N neuron-tracing
#PBS -M andrew.warrington@keble.ox.ac.uk
#PBS -m abe
#PBS -q parallel


# Set output and error directories
#PBS -o localhost:/scratch/andreww/4yp/neuron_creation/koene_method/koene_method/koene_method/Reports/results_${PBS_JOBID}.out
#PBS -e localhost:/scratch/andreww/4yp/neuron_creation/koene_method/koene_method/koene_method/Reports/results_${PBS_JOBID}.err
cd /scratch/andreww/4yp/neuron_creation/koene_method/koene_method/koene_method 




chmod +x ./neuron_trace_unix 

./neuron_trace_unix 
