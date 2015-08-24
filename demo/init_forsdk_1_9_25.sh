#!/bin/bash

export td=`pwd`
mkdir TMP
cd TMP

export flnDir=`pwd`
export sdkVer=1.9.25

export fln="appengine-tools-api.jar"
#curl -L http://googleappengine.googlecode.com/svn/trunk/java/lib/${fln} -o ${fln}
cp $GOOGLE_APPS_ENGINE_HOME/lib/${fln} ${fln}
mvn install:install-file -DgroupId=com.google.appengine -DartifactId=appengine-tools-api -Dversion=${sdkVer} -Dpackaging=jar -Dfile=${flnDir}/${fln} -DgeneratePom=true -DcreateChecksum=true

export fln="appengine-api-1.0-sdk-${sdkVer}.jar"
#curl -L http://googleappengine.googlecode.com/svn/trunk/java/lib/user/${fln} -o ${fln}
cp $GOOGLE_APPS_ENGINE_HOME/lib/user/${fln} ${fln}
mvn install:install-file -DgroupId=com.google.appengine -DartifactId=appengine-api-1.0-sdk -Dversion=${sdkVer} -Dpackaging=jar -Dfile=${flnDir}/${fln} -DgeneratePom=true -DcreateChecksum=true
 
export fln="appengine-testing.jar"
#curl -L http://googleappengine.googlecode.com/svn/trunk/java/lib/user/${fln} -o ${fln}
cp $GOOGLE_APPS_ENGINE_HOME/lib/testing/${fln} ${fln}
mvn install:install-file -DgroupId=com.google.appengine -DartifactId=appengine-testing -Dversion=${sdkVer} -Dpackaging=jar -Dfile=${flnDir}/${fln} -DgeneratePom=true -DcreateChecksum=true
 
export fln="appengine-testing.jar"
cp $GOOGLE_APPS_ENGINE_HOME/lib/testing/${fln} ${fln}
mvn install:install-file -DgroupId=com.google.appengine.tools.development.testing -DartifactId=appengine-testing -Dversion=${sdkVer} -Dpackaging=jar -Dfile=${flnDir}/${fln} -DgeneratePom=true -DcreateChecksum=true
 
export fln="appengine-api-stubs.jar"
cp $GOOGLE_APPS_ENGINE_HOME/lib/impl/${fln} ${fln}
mvn install:install-file -DgroupId=com.google.appengine -DartifactId=appengine-api-stubs -Dversion=${sdkVer} -Dpackaging=jar -Dfile=${flnDir}/${fln} -DgeneratePom=true -DcreateChecksum=true
 
export fln="appengine-api-labs-${sdkVer}.jar"
#curl -L http://googleappengine.googlecode.com/svn/trunk/java/lib/user/${fln} -o ${fln}
cp $GOOGLE_APPS_ENGINE_HOME/lib/user/${fln} ${fln}
mvn install:install-file -DgroupId=com.google.appengine -DartifactId=appengine-api-labs -Dversion=${sdkVer} -Dpackaging=jar -Dfile=${flnDir}/${fln} -DgeneratePom=true -DcreateChecksum=true
 
export fln="appengine-jsr107cache-${sdkVer}.jar"
cp $GOOGLE_APPS_ENGINE_HOME/lib/user/${fln} ${fln}
mvn install:install-file -DgroupId=com.google.appengine -DartifactId=appengine-jsr107cache -Dversion=${sdkVer} -Dpackaging=jar -Dfile=${flnDir}/${fln} -DgeneratePom=true -DcreateChecksum=true

export fln="appengine-local-runtime.jar"
cp $GOOGLE_APPS_ENGINE_HOME/lib/impl/${fln} ${fln}
mvn install:install-file -DgroupId=com.google.appengine -DartifactId=appengine-local-runtime -Dversion=${sdkVer} -Dpackaging=jar -Dfile=${flnDir}/${fln} -DgeneratePom=true -DcreateChecksum=true

#### and the runtime library, needed for protocol buffers for example
export fln="appengine-api.jar"
cp $GOOGLE_APPS_ENGINE_HOME/lib/impl/${fln} ${fln}
mvn install:install-file -DgroupId=com.google.appengine -DartifactId=appengine-api -Dversion=${sdkVer} -Dpackaging=jar -Dfile=${flnDir}/${fln} -DgeneratePom=true -DcreateChecksum=true

cd $td
rm -rf TMP
