JFLAGS = -g -Xlint:unchecked

CLASSPATH=lib/commons-logging-1.2.jar:lib/javax.servlet-api-4.0.1.jar:/usr/share/java/xmlrpc-client.jar:/usr/share/java/xmlrpc-server.jar:/usr/share/java/xmlrpc-common.jar:/usr/share/java/ws-commons-util.jar:/usr/share/java/commons-logging.jar:.


JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

# This uses the line continuation character (\) for readability
# You can list these all on a single line, separated by a space instead.
# If your version of make can't handle the leading tabs on each
# line, just remove them (these are also just added for readability).
CLASSES = \
        DroneServlet.java \
	Drone.java \

default: classes

DroneServlet.class: DroneServlet.java
	javac $(JFLAGS) -cp $(CLASSPATH) DroneServlet.java
Drone.class: Drone.java
	javac $(JFLAGS) -cp $(CLASSPATH) Drone.java
classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

