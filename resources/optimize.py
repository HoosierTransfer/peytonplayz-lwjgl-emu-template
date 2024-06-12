import os
import hashlib
from PIL import Image
import shutil

def compute_hash(file_path):
    # Compute the hash of the file content
    with open(file_path, 'rb') as f:
        content = f.read()
        hash_value = hashlib.md5(content).hexdigest()
    return hash_value

def compress_and_save(source_dir, target_dir, hash_dir):
    # Ensure the target directory exists
    if not os.path.exists(target_dir):
        os.makedirs(target_dir)

    # Ensure the hash directory exists
    if not os.path.exists(hash_dir):
        os.makedirs(hash_dir)

    for root, dirs, files in os.walk(source_dir):
        for file_name in files:
            if file_name.lower().endswith('.png'):
                source_path = os.path.join(root, file_name)
                relative_path = os.path.relpath(source_path, source_dir)
                target_path = os.path.join(target_dir, relative_path)

                # Compute the hash of the original file
                original_hash = compute_hash(source_path)
                # Create the corresponding hash file path
                hash_file_path = os.path.join(hash_dir, relative_path + '.hash')
                # Check if the hash has changed
                if not os.path.exists(hash_file_path) or open(hash_file_path, 'r').read() != original_hash:
                    # Create the directory structure in the target directory if it doesn't exist
                    os.makedirs(os.path.dirname(target_path), exist_ok=True)
                    with Image.open(source_path) as img:
                        img = img.convert('RGBA')
                        img.save(target_path, format='PNG', optimize=True)
                        os.makedirs(os.path.dirname(hash_file_path), exist_ok=True)
                        with open(hash_file_path, 'w') as hash_file:
                            hash_file.write(original_hash)
            else:
                # Copy non-PNG files to the target directory
                source_path = os.path.join(root, file_name)
                relative_path = os.path.relpath(source_path, source_dir)
                target_path = os.path.join(target_dir, relative_path)
                os.makedirs(os.path.dirname(target_path), exist_ok=True)
                shutil.copyfile(source_path, target_path)

# Replace 'source_directory_path' with the path to your directory containing PNG files
source_directory_path = 'resources'

# Replace 'target_directory_path' with the path to the directory where compressed images will be saved
target_directory_path = 'optimizedResources'

# Replace 'hash_directory_path' with the path to the directory where hashes will be stored
hash_directory_path = 'hashes'

print("Optimizing images...")
compress_and_save(source_directory_path, target_directory_path, hash_directory_path)
