all: compile

compile:
	java -jar ../jtb132di.jar -te minjava.jj
	java -jar ../javacc5.jar minjava-jtb.jj
	javac Main.java

clean:
	rm -f *.class *~