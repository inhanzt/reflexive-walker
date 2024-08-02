This project comes out of a need to figure out if an ID exists and matches something in an arbitary message that can be captured from a message bus.

Somewhere in the object created by deserializing the message, there may exist a class that can store a particular kind of ID that marks it as related to other, existing messages with that same type of ID and the same ID value.  If the match occurs, we can group those messages together.

Finally, there exist some messages that lack the capacity for this ID in their specified deserialization classes.  For those kinds of message, configuration allowed the system to either group or reject those messages depending on message type.

Of course, it would have been easier to work with a better designed system (e.g. some request id that tied related messages together), but I did not design the system, I was just tasked with using it and sorting out messages.
