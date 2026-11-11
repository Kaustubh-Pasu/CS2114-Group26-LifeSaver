# CS2114-Group26-Organ-Donor-System

**Reflections**

Found errors for parameter types from the spec doc. Identified those errors and implemented correct parameter types in prototype 1. 

WaitlistDeque.java
- remove(Node node) was changed to remove(DLNode<T> node).
- While we always intended to use DLNode, there was a transcription mistake on the doc mentioning regular Node. 

Recipient.Java
- Initially was missing "getBloodType()" and "setBloodType()"
- Fixed this, otherwise not much changed. 

WaitlistManager.java
- nothing really significant changed from plans




**Next Steps**
- We will start a finalized view (not just a stubbed view)
- Once the mvp is established as working we will move to our stretch goals
- The first stretch goal to implement will be implementing a priority queue

**Attributions**

- Andrew Kim: WaitlistDeque + WaitlistDequeTest
- Kaustubh Pasumarthi: WaitlistManager
- Elijah O'Donoghue: Recipient + RecipientTest


**GenAI Notes**

- GenAI Found that in the original version of WaitlistManager did not properly call WaitlistDeque.
- GenAI was used to help import APIs needed for running JUnit Tests - WaitlistDequeTest, Andrew
