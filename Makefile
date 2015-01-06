JCC = javac

JFLAGS =  #-g

default: sender receiver packet

#name dependencies
sender: sender.java
	$(JCC) $(JFLAGS) sender.java		
receiver: receiver.java
	$(JCC) $(JFLAGS) receiver.java
packet: packet.java
	$(JCC) $(JFLAGS) packet.java		

clean:
	rm *.class *~ *#* 

