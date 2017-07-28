# Background
Track users (devices) online to determine which web site contributed to a particular conversion (sale) in a web shop. Thousands of these events are generated every second, and they need to be processed in real-time.

The event types includes:

* __Impression__: Ad is shown on a web site.
* __Click__: User clicked on ad.
* __Conversion__: User bought something in web shop.

Web sites get paid when they refer a user to a web shop and a purshase has been made. This is called performance marketing, i.e. the web shop only pays for conversions, not for impressions or clicks. 

The most common commission rule is that the site with the last click will get 100% of the commission. This is called last-click. If there is no last-click, the last impression will get the commission. This is called last-imp.

* The same click/impression event may not be used as referrer more than once.
* Only click/impression events within the specified `timeWindow` (set to 12 hours) are eligible to be a referrer.

# Project

This project is to process `Event` messages from STDIN, in delimited (varint) protobuf format. 

The protobuf messages are defined in the file [src/main/resources/event.proto].

For each _conversion_ tracking event received, find the previous most recent _click_ or _impression_ (if no click) event for the same `device_id` (user). Tracking events with no prior click or impression should be ignored. 

Write each found click-conversion or impression-conversion as `Result` messages to STDOUT in delimited (varint) protobuf format.

# Design
Keywords: Distributed and Asynchronous.

the project contains:

1: Manager, it handle the input and output stream. Maintain a cluster of worker node, 
 FIND the right worker node to process the event and send to it
2: Worker, it process events that come from the manager 
 and send the results back to the manager if necessary.
3: SimpleMemStorage, it uses HashTable and Stack to keep it in memory.
 Stack is used because of the event steam is ordered by time ascendingly(LIFO).
 It also auto clean old data(timeWindow)
4: Partitioner, it find the Node index in the cluster according to the device id(UUID)



# How it works?

Manager get an envent, it uses partitioner to decide which worker node in the cluster send to. 
By sending it in a ASYNC way, it goes back to read next event. 
Manager is also work as one of the worker node in the cluster(masterWorker). 
Besides the normal workers' job, it also register new workers by handle requests from them.

Worker implements a AYSNC server. 
Once get an event, if the event is click or impression, save it to storage. 
If the event is a convention, it tries to find a referrer from the saved clicks and impressiions. 
Since clicks has higher priority, it tries find validate(12h timeWindow) last_click first then impression. 


# How to run?

1. compile,test and package.
mvn package
2. the above step created an executable fat jar in target folder.
java -jar target/tracker-0.1.0-jar-with-dependencies.jar < src/main/resources/inputevents.pb




