# Flexi GMSS 1.7 Install to local Maven .m2
+ Rename flexi-gmss-1.7p1-jar to flexi-gmss-1.7p1.jar
+ Install flexi-gmss-1.7p1.jar to local Maven .m2 directory by copying the following command in a terminal and executing:
``` console
mvn install:install-file -Dfile=flexi-gmss-1.7p1.jar -DgroupId=de.flexi -DartifactId=gmss -Dversion=1.7p1 -Dpackaging=jar
```