# CS2114-Group26-Organ-Donor-System

**Reflections**

Found errors for parameter types from the spec doc and our plans. Identified those errors and implemented correct parameter types in prototype 1. We were missing some methods needed in our spec doc as well. Fortunately, not that much needed to be changed.

WaitlistDeque.java
- remove(Node node) was changed to remove(DLNode<T> node).
- While we always intended to use DLNode, there was a transcription mistake on the doc mentioning regular Node hence the need for a change.

Recipient.Java
- Initially our planning was missing "getBloodType()" and "setBloodType()" despite mentioning bloodType as a feature 
- Fixed this, otherwise not much changed. 

WaitlistManager.java
- nothing really significant changed from plans
- more will need to be added and modified with stretch goal addition

The model was able to stand alone. We still haven't completely figured how to implement all of our stretch goals (which was one of
the unknowns from our spec doc). The waittime stretch goal was mentioned specifically in the spec doc. The finalized view and MVP does not work yet. But the back-end stuff works well. 


**Next Steps**
- We will start a finalized view (not just a stubbed view)
- Surprisingly, not much on the back-end is missing from the actually MVP, we really need to create a finalized view, allowing for an easy user-friendly experience when using our application.
- Recipient.java and WaitlistDeque.java will likely go untouched unless there are notable errors for the mvp. WaitlistManager.java may need some minor adjustments depending on the view.
- Once the mvp is established as working we will move to our stretch goals
- The first stretch goal to implement will be implementing a priority queue or wait time indicator 
- WaitlistDeque and WaitlistManager will need to be adjusted when the priority queue or wait time indicator stretch goal is added.
- PriorityQueue will be a particularly invasive change, it will  involve changing our current enqueue(Recipient recipient) method. Remove methods likely will go unchanged because the remove method is logically the same across regular and priority queues.
- Waittime will likely add and additional calculateWaittime(int id) method where the id is used to find the specific recipient waitng. Based on where they are in the queue and the current rate of people getting of the queue it calculates the waittime (assuming no donor that directly gives to them like a family member). It would also need an additional way to track how quickly people in front are getting off. We are still unsure of how to implement this, perhaps using file i/o writing we could track java DateTimes in the file i/o (since DateTimes are already used in the recipient class). From the we would need to add a file writing to the dequeue(), so it reports how long a patient was on the list. Again, we aren't sure the specifics, but from these values it wouldn't be too difficult to get an approximate wait time for any given recipient. 
- We are optimistic and think both currently planned goals could be added. 

**Attributions**

- Andrew Kim: WaitlistDeque + WaitlistDequeTest
- Kaustubh Pasumarthi: WaitlistManager + WaitlistManagerTest
- Elijah O'Donoghue: Recipient + RecipientTest
- Prateek: Stubbed View + revising other classes

**GenAI Notes**

- GenAI Found that in the original version of WaitlistManager did not properly call WaitlistDeque.
- GenAI was used to help import APIs needed for running JUnit Tests - WaitlistDequeTest, Andrew
