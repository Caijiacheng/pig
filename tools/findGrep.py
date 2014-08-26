

import os
import shutil
import socket
import re

def find(path):
    for root, dirs, files in os.walk(path):
        for name in files:
            yield os.path.join(root,name)
            pass
    
def match(dir, regex):
    try:
        print "begin to find"
        generator = find(dir)
        print "begin to generator"
        while True:
            path = generator.next()
#             print "path: " + path
            f = open(path)
            line = f.readline()
            while line:
                if line == "\0":
                    break
                if re.search(regex, line):
                    print("path: ", path, " line:", line)
                    pass
                line = f.readline()
    except Exception as err:
        print err

match(r"D:\work\android\android_sdk\sources\android-14\com", r".*android.*")
