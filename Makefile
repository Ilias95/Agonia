all:
	javac agonia/*.java
run:
	java agonia/Agonia
clean:
	rm -rf agonia/*.class
