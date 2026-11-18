# CS2114-Group26-Organ-Donor-System

**November 18**

**One non-trivial feature added**

- Wait time stretch goal

**Other features added**

- Each field is checked as soon as it is typed when adding a recipient (only the wrong field is asked again)
- "Look up a patient" menu option (details, place in line, estimated wait)
- "View all waitlists" now shows a one-line count per organ and only prints organs that have people waiting

**Reflections** (need expansion)

- still dont have priority queue
- changes with waittime impacted most files but not dramatic restructuring, just adding new methods
- user was able to add recipient without indicating blood type's RH factor; now rejects entry when the user did not indicate RH factor (+ or -).
- blood type group was shown in lowercase; all blood type groups are now indicated as uppercase. 
- testing wait time was a problem because estimates are measured in days, and we can't wait days for a JUnit test. We fixed this by having WaitlistManager and WaitlistDeque read the time from a java.time.Clock. The app uses the real clock; tests pass in a FakeClock and call advance(Duration.ofDays(10)) to make 10 days "pass" instantly.
- the add-recipient form used to only report bad input after all five fields were typed, then make the user start over. The view now asks WaitlistManager (isRegistered, isKnownOrgan, isValidBloodType) right after each field, and also checks that the name isn't blank and urgency is 1-10.
- urgency outside 1-10 is now rejected by the view, but the model still accepts it (WaitlistManagerTest has a test expecting -1 to work). We need to decide whether the model should reject it too.
- found that WaitlistDequeTest's methods have no @Test annotations, so none of its tests actually run. Once they do, testEnqueue's assertEquals(r, deque.enqueue(r)) will fail because enqueue returns the node, not the Recipient.

**Next Steps** (need expansion)

- Priority Queue implementation 
- ~~When user enters invalid input, rejects adding it to the list after user enters all other fields. Might add a feature that prompts user to enter valid entry for each fields, not all at once at the end.~~ Done (per-field checks).
- Add @Test annotations to WaitlistDequeTest and fix testEnqueue
- Decide whether the model should also reject urgency outside 1-10
- Edit a patient's urgency (useful once the priority queue exists)


**Attributions**

Elijah: Wait time implementation

Andrew: Prevented user entering blood type without indicating RH type, now shows blood type group in uppercase.

Prateek: Per-field input checks in the add-recipient form, "Look up a patient" menu option, "View all" summary, FakeClock + Clock support in WaitlistManager/WaitlistDeque so wait time can be tested, WaitTimeTest (22 tests) and new OutputViewerTest tests for wait time, lookup, and input checks.

Kaustubh: Fixed WaitlistManager to properly call dequeue in WaitlistDeque.

**GenAI Notes**

Elijah: Used Gen AI to plan out implementation of wait time and which files would need changing

Andrew: Used Gen AI to confirm the use of toUpperCase() on an entry with non-alphabet letters. 

Prateek: Used Gen AI  to come up with the fake clock approach for testing wait times without waiting, and write the new tests.

Kaustubh: Used Gen AI to ensure WaitlistMangager calls WaitlisteDeque properly.

-End of work by November 18-

----------------------------------------------------------------------------------------------------------------------------------
**November 11**

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

--End of work by November 11-
