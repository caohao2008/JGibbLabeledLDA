mvn dependency:copy-dependencies -DoutputDirectory=target/lib
java -cp target/JGibbLabeledLDA-release-1.7-SNAPSHOT.jar:target/lib/* util.LDAHelper

