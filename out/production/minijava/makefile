all:clean compile run

compile:
	java -jar ../jtb132di.jar -te minjava.jj
	java -jar ../javacc5.jar minjava-jtb.jj
	javac Main.java

run:
	java Main ./test_files/tests/$(FILE).java
	cat ./test_files/$(FILE).ll

clean:
	rm -f *.class *~ ./test_files/*.ll