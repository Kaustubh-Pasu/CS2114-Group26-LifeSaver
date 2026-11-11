# CS2114-Group26-Organ-Donor-System

**Reflections**

Found errors for parameter types from the spec doc. Identified those errors and implemented correct parameter types in prototype 1. We were missing some methods needed in our spec doc as well. 

WaitlistDeque.java
- remove(Node node) was changed to remove(DLNode<T> node).
- While we always intended to use DLNode, there was a transcription mistake on the doc mentioning regular Node. 

Recipient.Java
- Initially was missing "getBloodType()" and "setBloodType()"
- Fixed this, otherwise not much changed. 

WaitlistManager.java
- nothing really significant changed from plans

The model was able to stand alone. We still haven't completely figured how to implement all of our stretch goals (which was one of
the unknowns from our spec doc). The waittime stretch goal was mentioned specifically in the spec doc. The finalized view and MVP does not work yet. But the backend stuff works well. 


**Next Steps**
- We will start a finalized view (not just a stubbed view)
- Surprisingly, not much on the backend is missing from the actually MVP, we really need to create a finalized view, allowing for an easy user-friendly experience when using our application.
- Recipient.java and WaitlistDeque.java will likely go untouched unless there are notable errors for the mvp. WaitlistManager.java may need some minor adjustments depending on the view.
- Once the mvp is established as working we will move to our stretch goals
- The first stretch goal to implement will be implementing a priority queue or wait time indicator 
- WaitlistDeque and WaitlistManager will need to be adjusted when the priority queue or wait time indicator stretch goal is added.
- We are optimistic and think both currently planned goals could be added. 

**Attributions**

- Andrew Kim: WaitlistDeque + WaitlistDequeTest
- Kaustubh Pasumarthi: WaitlistManager
- Elijah O'Donoghue: Recipient + RecipientTest


**GenAI Notes**

- GenAI Found that in the original version of WaitlistManager did not properly call WaitlistDeque.
- GenAI was used to help import APIs needed for running JUnit Tests - WaitlistDequeTest, Andrew
