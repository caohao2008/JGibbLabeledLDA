#!/usr/bin/python
import sys

cateidMap = {}
cateid_index=0
cate_index_file = open(sys.argv[1]+".cate_index","w")
for line in open(sys.argv[1]):
    line = line.strip()
    cols = line.split(" ")
    if(len(cols)>=2):
        length = len(cols[0])
        cateid = cols[0][1:length-1]
        if(not cateidMap.has_key(cateid)):
            cateidMap[cateid] = cateid_index
            cateid_index = cateid_index + 1        
            print >> cate_index_file,cateid+"\t"+str(cateid_index)
        cur_cateid_index = cateidMap[cateid]
        print "["+str(cur_cateid_index)+"]"+" "+" ".join(cols[1:])
