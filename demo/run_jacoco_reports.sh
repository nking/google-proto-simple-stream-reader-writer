#!/bin/bash

########################################################################
#  script to create source code analysis reports using jacoco
#
#  Required Parameters:
#      -projectBaseDir   <path to project base directory>
#
########################################################################

usage="Usage: $0 -projectBaseDir <project.base.dir> "

if [[ $# -gt 0 && $1 = "-help" ]]; then
    echo "Required Parameters:"
    echo "    -projectBaseDir   path to project base directory "
    echo ""
    echo "$usage"
    exit 1
fi

echo "$0 $1 $2"

a=$1

while test -n "$1"; do
  echo "Read: $1"
  case $1 in
    -projectBaseDir) shift
        projectBaseDir=$1
        shift
        continue;;

    *) 
        shift
        continue;;
  esac
done

echo ""
echo "projectBaseDir=$projectBaseDir"
echo ""

if [ ${#projectBaseDir} -eq 0 ]; then
    echo ""
    echo "Error: missing -projectBaseDir"
    echo "$usage"
    echo ""
    exit 1;
fi

mkdir ${projectBaseDir}/target
mkdir ${projectBaseDir}/target/quality
mkdir ${projectBaseDir}/target/quality/sources
mkdir ${projectBaseDir}/target/quality/classes
mkdir ${projectBaseDir}/target/quality/jacoco

## copy source files over
echo "copying source jars to ${projectBaseDir}/target/quality/sources/"
files=`find . -path "*/target/*" -name "*-sources.jar" -type f | grep -v "test-sources"`
for file in $files; do
    cp -f ${file} ${projectBaseDir}/target/quality/sources/ 
done
 
echo "unpacking source jars"
cdir=`pwd`
cd ${projectBaseDir}/target/quality/sources
files=`find . -name "*jar" -type f`
for file in $files; do 
    jar -xf ${file}
done
rm *.jar
cd $cdir 

echo "copy class files"
dirs=`find . -path "*/target/classes/*" -type d | sed "s/\(.*target\/classes\).*/\1/" | uniq`
for cdir in $dirs; do
    cp -rf ${cdir}/* ${projectBaseDir}/target/quality/classes/
done

echo "copy exec files"
files=`find . -name "jacoco.exec"`
count=0
for fl in $files; do
    flname="jacoco_${count}.exec"
    cp -f ${fl} ${projectBaseDir}/target/quality/jacoco/${flname}
    let count=count+1
done

ant -f ant_run_jacoco.xml

