
while IFS= read -r -d '' file
do
  (( count++ ))
  # Example full path
  full_path=$file

# Get the directory path (removes filename if it's a file)
  dir_path=$(dirname "$full_path")


  projectFolder=$(basename "$(dirname "$full_path")")

  mvn clean install -f $dir_path/pom.xml

  #echo $dir_path
  #echo $projectFolder
  #echo "$file"

  docker build . -t $projectFolder -f "$file"
done <   <(find "./" -type f -name 'Dockerfile' -print0)

