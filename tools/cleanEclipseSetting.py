import os
import shutil

for root, dirs, files in os.walk('../'):
    for name in files:
        if name in ['.classpath', '.project']: 
            print('remove: ' + os.path.join(root, name));
            os.remove(os.path.join(root, name))
    for name in dirs:
        if name == '.settings':
            print('remove_dir: ' + os.path.join(root, name));
            shutil.rmtree(os.path.join(root, name));