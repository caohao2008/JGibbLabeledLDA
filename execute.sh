#!/bin/bash

java -mx4096M -cp lib/args4j-2.0.6.jar:./:./src/:lib/trove-3.0.3.jar jgibblda.LDA -est -alpha 0.5 -beta 0.1 -ntopics
130 -niters 500 -twords 20 -dfile head.poi.segment.txt.index.gz -dir .

java -mx4096M -cp lib/args4j-2.0.6.jar:./:./src/:lib/trove-3.0.3.jar  jgibblda.LDA -inf -dir . -model model -dfile
poi.segment.txt.indexed.gz

java -mx4096M -cp lib/args4j-2.0.6.jar:./:./src/:lib/trove-3.0.3.jar  jgibblda.LDA -inf -dir . -model model -dfile
head.poi.segment.txt.index.gz
