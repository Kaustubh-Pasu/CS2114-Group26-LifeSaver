# LifeSaver: Organ Donor Waitlist (CS 2114, Group 26)

A console app that keeps one first-in, first-out waitlist per organ, registers and removes recipients, matches a donor organ to the first blood-compatible person in line, and estimates how long a waiting patient has left.

**Deliverable 4: Second Prototype + Tests (November 18)**

---

## How to Compile and Run

All classes are in the package `project3`. You need Java 11 or newer.

### Eclipse

1. Import the folder as an existing project (File > Import > Existing Projects into Workspace).
2. Add JUnit 5: right-click the project > Build Path > Add Libraries > JUnit > JUnit 5.
3. Run `OutputViewer.java` as a Java Application.

### Command line (from the project folder)

```bash
javac -d bin FakeClock.java OutputViewer.java Recipient.java StubWaitlistManager.java WaitlistDeque.java WaitlistManager.java
java -cp bin project3.OutputViewer
```

To run the view against fixed fake data instead of the real model:

```bash
java -cp bin project3.OutputViewer stub
```

### Menu

| # | Option | Controller method it calls |
|---|---|---|
| 1 | Add recipient | `isRegistered`, `isKnownOrgan`, `isValidBloodType`, then `addRecipient` |
| 2 | Remove recipient (by ID, or dequeue the front of a line) | `removeRecipient` / `dequeueRecipient` |
| 3 | Match donor organ | `matchOrgan` |
| 4 | View one organ's waitlist | `getWaitlist` |
| 5 | View all waitlists | `getWaitlist` for each organ |
| 6 | Estimate wait time (**new feature**) | `estimateWaitTime` |
| 7 | Look up a patient (**new**) | `getRecipient`, `getPosition`, `estimateWaitTime` |
| 0 | Exit | none |

Type `cancel` at any prompt to go back to the menu.

---

## How to Run the Tests

The tests use JUnit 5 and call the model and controller directly. No console input is involved in the model tests.

### Eclipse

Right-click the project > Run As > JUnit Test.

### Command line

Download `junit-platform-console-standalone-1.10.2.jar` from Maven Central into the project folder, then:

```bash
javac -d bin -cp junit-platform-console-standalone-1.10.2.jar *.java
java -jar junit-platform-console-standalone-1.10.2.jar -cp bin --select-package project3
```

### Test files

| File | Targets | What it covers |
|---|---|---|
| `WaitlistDequeTest` | Model (Queue) | enqueue, dequeue, peek, isEmpty, size, getAllRecipients, remove; FIFO order after interleaved enqueues and dequeues; null and empty-queue errors |
| `RecipientTest` | Model | Constructor, every getter and setter |
| `WaitlistManagerTest` | Controller | addRecipient, removeRecipient, dequeueRecipient, matchOrgan, getWaitlist: normal cases, duplicate/unknown IDs, unknown/null organs, invalid blood types, full ABO compatibility matrix, order after removes and re-adds |
| `WaitTimeTest` | Model + controller (new feature) | 22 tests: FakeClock, time-at-front tracking, estimateWaitTime, getRecipient, getPosition, isRegistered, isKnownOrgan, isValidBloodType |
| `OutputViewerTest` | View, against `StubWaitlistManager` | Menu re-prompts, per-field add-recipient checks, cancel. Kept separate from the model tests on purpose. |

**Order after interleaved enqueues and dequeues** is checked in `WaitlistDequeTest.testDequeueFifoOrder` (enqueue 3, dequeue 2, dequeue last, enqueue again, check front and full order), `WaitlistManagerTest.testRemoveThenAddToBack`, `testRemoveAllThenAddAgain`, `testMatchMiddleThenAddKeepsOrder`, and `WaitTimeTest.testEstimateShrinksAsLineMoves`.

---

## The New Feature: Wait-Time Estimate

D3 Next Steps named "priority queue **or** wait time indicator" as the first stretch goal. We built the wait-time indicator.

**What it does.** Pick option 6 (or 7, Look up a patient), enter a patient ID, and the app prints an estimate such as `Estimated wait: 12 days, 4 hours, 0 minutes`.

**How it works (model state and logic added).**

1. When a recipient reaches the front of their organ's line, `WaitlistDeque` stamps `Recipient.frontSince` with the current time.
2. When the front person leaves by being dequeued or matched, `WaitlistManager.recordFrontWait` adds how long they spent at the front to a per-organ total (`totalFrontSeconds`) and bumps a per-organ count (`frontCount`). Removal by ID does not count, because it is not the line moving.
3. `estimateWaitTime(id)` returns `average time at front × the patient's position in line`. If nobody has left that organ's line yet, it returns `null` (no history to average).

**How the user reaches it.** OutputViewer reads the ID, calls `manager.estimateWaitTime(id)`, and prints the result. It never touches a queue or a node.

**Testing days of waiting in milliseconds.** `WaitlistManager` and `WaitlistDeque` read the time from a `java.time.Clock`. The app uses the real clock. Tests pass a `FakeClock` and call `advance(Duration.ofDays(10))` to make ten days pass instantly.

---

## Bad Input

Every case from the scope, plus the new ones the feature introduced. None of them crash the program.

| Bad input | Caught by | What happens | Test |
|---|---|---|---|
| Menu choice that is text, negative, or out of range | `OutputViewer.getMenuChoice` | Re-prompts | `OutputViewerTest.testMenuChoiceRepromptsOnText`, `testMenuChoiceRepromptsOnOutOfRange` |
| Patient ID typed as text | `OutputViewer` | Re-prompts that field only | `OutputViewerTest.testAddRecipientRepromptsOnNonNumberId` |
| Duplicate patient ID | `WaitlistManager.addRecipient` (view pre-checks with `isRegistered`) | Throws `IllegalArgumentException`, nothing added; view re-asks for ID only | `WaitlistManagerTest.testAddRecipientDuplicateIdSameOrgan`, `...DifferentOrgan`; `OutputViewerTest.testAddRecipientDuplicateIdAsksOnlyForId` |
| Unknown, null, or empty organ | `WaitlistManager` (view pre-checks with `isKnownOrgan`) | Throws; view re-asks for organ only | `WaitlistManagerTest.testAddRecipientUnknownOrgan`, `NullOrgan`, `EmptyOrgan`, `testGetWaitlistUnknownOrgan`, `testMatchOrganUnknownOrgan` |
| Invalid or null blood type | `WaitlistManager.aboGroup` (view pre-checks with `isValidBloodType`) | Throws; view re-asks for blood type only | `WaitlistManagerTest.testAddRecipientInvalidBloodTypes`, `NullBloodType`, `testMatchOrganInvalidDonorBloodType` |
| Blank name | `OutputViewer.promptName` | Re-prompts | `OutputViewerTest.testAddRecipientBlankNameAsksAgain` |
| Urgency outside 1-10, or not a number | `OutputViewer.promptUrgency` | Re-prompts | `OutputViewerTest.testAddRecipientUrgencyOutOfRangeAsksAgain`, `UrgencyNotNumberAsksAgain` |
| Removing an ID that does not exist | `WaitlistManager.removeRecipient` | Returns `false`; view says no such patient | `WaitlistManagerTest.testRemoveRecipientUnknownId`, `testRemoveRecipientTwice` |
| Dequeue from an empty line | `WaitlistManager.dequeueRecipient` | Returns `null` | `WaitlistManagerTest.testDequeueRecipientEmptyAndUnknown` |
| No compatible recipient for a donor | `WaitlistManager.matchOrgan` | Returns `null`; waitlist unchanged | `WaitlistManagerTest.testMatchOrganNoCompatible`, `testMatchOrganEmptyWaitlist` |
| **New:** wait estimate for an unknown ID | `WaitlistManager.estimateWaitTime` | Throws; view prints the message | `WaitTimeTest.testUnknownIdThrows` |
| **New:** wait estimate with no history yet | `WaitlistManager.estimateWaitTime` | Returns `null`; view says there is not enough history | `WaitTimeTest.testNoHistoryReturnsNull` |
| **New:** null clock | `WaitlistManager` / `WaitlistDeque` constructors | Throws | `WaitTimeTest.testNullClockRejected` |
| **New:** look up an unknown ID | `WaitlistManager.getRecipient` / `getPosition` | Returns `null` / `-1`; view says no such patient | `WaitTimeTest.testGetRecipient`, `testGetPosition` |

---

## Reflection

### Classes the feature touched vs. the D3 prediction

D3 predicted: Recipient and WaitlistDeque would "likely go untouched," and WaitlistDeque and WaitlistManager would need to change for the wait-time goal, through a new `calculateWaittime(int id)` method plus file I/O to log how long each patient waited.

| Class | Predicted to change? | Actually changed? | What changed |
|---|---|---|---|
| `WaitlistManager` | Yes | Yes | `estimateWaitTime`, `recordFrontWait`, per-organ totals, `Clock` constructor, plus `getRecipient`, `getPosition`, `isRegistered`, `isKnownOrgan`, `isValidBloodType` |
| `WaitlistDeque` | Yes | Yes | `Clock` constructor, `markFront`, `positionOf` |
| `Recipient` | No | **Yes** | New `frontSince` field with getter and setter |
| `OutputViewer` | Not discussed | Yes | Menu options 6 and 7, duration formatting, per-field checks |
| `FakeClock` | No (not planned) | New class | Test-only clock |

**What matched:** the change stayed on the WaitlistManager/WaitlistDeque side, and the method we predicted (`calculateWaittime(int id)`) became `estimateWaitTime(int id)` with the same idea: find the patient by ID, use their place in line, and multiply by how fast the line moves.

**What surprised us:**

- Recipient had to change. Each person needs to remember when they reached the front, and the Recipient is the only object that travels with the person through the queue. That means the "Recipient stays unchanged" prediction missed.
- Testing was the hard part, not the formula. Estimates are in days, and a JUnit test cannot wait days. That forced the `Clock` parameter into two constructors and created `FakeClock`, which we never planned.
- The view needed more model queries than expected (`isRegistered`, `isKnownOrgan`, `isValidBloodType`, `getPosition`) so it could re-ask one field at a time instead of rejecting the whole form at the end.

### Did the change cross the model/view line?

Yes, but only through the controller's public methods, as intended. The new state (front-of-line timestamps, per-organ totals) lives entirely in the model. The view only gained menu options that call `estimateWaitTime`, `getRecipient`, and `getPosition`, and it prints a `Duration`. It ripples into the view in one place: `OutputViewer.formatDuration` turns a `Duration` into "X days, Y hours, Z minutes", because how to word a time span is a display decision.

OutputViewer still keeps no copy of model state. Its only fields are the manager, the scanner, and the output stream, and it calls `getWaitlist` again every time it draws a list. The one thing it duplicates is the fixed list of organ names used in prompts; `WaitlistManager` already has `isKnownOrgan`, so that list could come from the model instead.

### What changed from the D3 plan, and why

- **No file I/O.** D3 planned to write each patient's wait to a file. We kept running totals in two `HashMap`s inside WaitlistManager. It is simpler, needs no file-path handling, and is enough for one session. The cost: history resets when the program closes.
- **We measure time at the front of the line, not total time on the list.** Total time on the list would count everyone ahead of you twice. Time at the front is how fast the line actually moves.
- **Remove-by-ID does not count toward the average.** Someone who opts out or dies is not the line moving, so including them would skew the estimate.
- **Wait time first, priority queue later.** D3 listed both. Wait time touched fewer classes, so we did it first.
- **Per-field input checks** were a D3 next step for the view and are now done.

### What doesn't work yet

- **No priority queue.** Matching is still strict FIFO; urgency is stored but ignored.
- **Wait history is lost on exit**, and the first estimate for each organ returns "not enough history" until someone has been matched or dequeued from that line.
- **The estimate assumes the line moves at a steady rate.** It ignores donor arrivals, blood-type compatibility, and urgency.

### Design change that would make the next feature cheaper

The next feature is the priority queue. Today, `WaitlistDeque` is a concrete class and `WaitlistManager` relies on its node type (`DLNode`) for O(1) removal and for `positionOf`. Pulling those operations into a small `Waitlist` interface (`enqueue`, `dequeue`, `remove(handle)`, `positionOf(handle)`, `getAllRecipients`) would let us drop in a priority-ordered implementation and switch it in WaitlistManager's constructor, with no edits to `matchOrgan`, `estimateWaitTime`, the view, or the controller tests. Moving the duplicated organ list from OutputViewer into the model would also mean adding an organ touches one class.

---

## Next Steps

### What's left for the final build, in order

| # | Item | Owner | Estimate |
|---|---|---|---|
| 1 | Add validation to the Recipient constructor and setters (name, urgency 1-10, blood type, status) and update `RecipientTest` and the two `WaitlistManagerTest` cases that expect bad values to pass | Elijah | 1-2 hours |
| 2 | Rh factor (`+`/`-`) is now required by the model and blood types display in uppercase; update `WaitlistManagerTest.testMatchOrganNoRhAndCaseInsensitive` (it still expects `AB` with no Rh to be accepted) and add a test for the new rejection | Andrew | 30 minutes |
| 3 | Priority queue: order each line by urgency, then registration time as the tiebreaker; keep O(1) removal by ID | Andrew (WaitlistDeque), Elijah (WaitlistManager) | 4-6 hours |
| 4 | Tests for the priority queue: order after interleaved adds, matches, and removes; ties on urgency | Kaustubh | 2 hours |
| 5 | Edit a patient's urgency from the menu (re-sorts them in the priority queue) | Prateek (view), Elijah (controller) | 2 hours |
| 6 | Final README, demo rehearsal, and attribution update | Kaustubh | 1 hour |

### Stretch goals

| Stretch goal | Status |
|---|---|
| Wait-time estimate | **Done** in this prototype |
| Priority queue (urgency first, registration time as tiebreaker) | **Committed** for the final build |
| Edit a patient's urgency | **Committed**, only once the priority queue works |
| Saving wait history to a file between runs | **Dropped**: in-memory totals are enough for a single session |
| Moving a patient to the front by hand | **Dropped** (already cut in the Revised Scope): breaks fair ordering |
| Tissue type, body size, and distance matching | **Dropped**: beyond a console app |

### 3-minute demo path

| Time | Step | What it shows |
|---|---|---|
| 0:00-0:20 | Start the app; show the menu | View connected to the real controller |
| 0:20-0:55 | Add three kidney patients (A+, O-, B+). On the second, type a duplicate ID, then a bad blood type (`Q+`), then urgency `15` | Per-field bad-input handling; only the wrong field is re-asked; no crash |
| 0:55-1:15 | View all waitlists, then view the kidney list | One-line counts per organ; FIFO order |
| 1:15-1:40 | Match a kidney from an O- donor, then a B- donor | First compatible patient is matched; incompatible ones are skipped; null result prints "no compatible recipient" |
| 1:40-2:05 | Add one more patient, dequeue the front, then look up a patient (option 7) | **New feature:** position in line plus estimated wait |
| 2:05-2:25 | Estimate wait for an ID that does not exist | New bad input handled |
| 2:25-2:50 | Run the JUnit suite in Eclipse; point at `WaitTimeTest.testEstimateAfterTenDays` | FakeClock makes 10 days pass instantly; all tests green |
| 2:50-3:00 | Say what is next: priority queue | Plan for the final build |

During the live demo the estimate shows only minutes, because the real clock barely moves. The JUnit step at 2:25 shows the day-scale numbers.

---

## Attribution

| Class / test file | Who worked on it | How |
|---|---|---|
| `Recipient` | Elijah, Kaustubh | Elijah wrote it and added `frontSince` for wait time. Kaustubh added `getBloodType`/`setBloodType` in D3. |
| `RecipientTest` | Elijah | Wrote. |
| `WaitlistDeque` | Andrew, Elijah, Prateek | Andrew wrote the doubly linked queue. Elijah added `markFront` and `positionOf` for wait time. Prateek added the `Clock` constructor. |
| `WaitlistDequeTest` | Andrew, Kaustubh | Andrew wrote. Kaustubh revised it: added `@Test`/`@BeforeEach` so the tests run, fixed `testEnqueue` (enqueue returns the node), and added the interleaved-order and `remove` tests (GenAI-assisted). |
| `WaitlistManager` | Kaustubh, Elijah, Prateek | Kaustubh wrote it and fixed `dequeueRecipient` to call WaitlistDeque correctly (GenAI-assisted review). Elijah added `estimateWaitTime` and `recordFrontWait` (GenAI-assisted planning). Prateek added the `Clock` constructor, `getRecipient`, `getPosition`, `isRegistered`, `isKnownOrgan`, `isValidBloodType`. |
| `WaitlistManagerTest` | Kaustubh | Wrote. |
| `WaitTimeTest` | Prateek | Wrote all 22 tests (GenAI-assisted). |
| `FakeClock` | Prateek | Wrote (GenAI-assisted design). |
| `OutputViewer` | Prateek, Elijah, Andrew | Prateek wrote the view, per-field checks, Look up a patient, and the View-all summary. Elijah added the Estimate wait time option. Andrew worked on blood type Rh/uppercase handling (GenAI used to confirm `toUpperCase()` behavior). |
| `OutputViewerTest` | Prateek | Wrote; added wait-time, lookup, and input-check tests (GenAI-assisted). |
| `StubWaitlistManager` | Prateek | Wrote (the D3 stubbed controller); updated for the new methods. |
| `README.md` | All four | Each member wrote their own section; Kaustubh revised it into this format with GenAI help. |

### GenAI Note

We used GenAI as a helper, and every change was read and run by the person listed above.

- **Elijah:** planned how to implement wait time and which files would need to change.
- **Andrew:** confirmed how `toUpperCase()` behaves on strings with non-letters such as `+` and `-`.
- **Prateek:** came up with the FakeClock approach for testing wait times without waiting, and drafted the new tests.
- **Kaustubh:** checked that WaitlistManager calls WaitlistDeque correctly, fixed `WaitlistDequeTest` so it runs, and restructured this README against the D4 rubric.
- **D3 (earlier):** GenAI found that the original WaitlistManager did not call WaitlistDeque correctly, helped set up JUnit imports for `WaitlistDequeTest`, and was used to draw the system diagram.

---

## Appendix: D3 Reflection and Next Steps (November 11)

Kept for comparison with the D4 Reflection above.

**Reflection.** We found parameter-type errors between the spec doc and our plans and fixed them in prototype 1. `WaitlistDeque.remove(Node)` became `remove(DLNode<T>)` (a transcription mistake in the doc). `Recipient` was missing `getBloodType()`/`setBloodType()`. `WaitlistManager` matched the plan. The model stood alone; the finalized view did not work yet.

**Next Steps (D3).**

- Build a finalized view (not just the stub).
- Recipient.java and WaitlistDeque.java will likely go untouched unless there are errors for the MVP; WaitlistManager may need minor adjustments for the view.
- First stretch goal: priority queue or wait-time indicator. Both would change WaitlistDeque and WaitlistManager.
- Priority queue would change `enqueue(Recipient)`; remove would stay the same.
- Wait time would add `calculateWaittime(int id)`, using the patient's place in line and the rate people leave the line, possibly logging wait times with file I/O.

**Attributions (D3).** Andrew: WaitlistDeque + WaitlistDequeTest. Kaustubh: WaitlistManager + WaitlistManagerTest. Elijah: Recipient + RecipientTest. Prateek: Stubbed view + revising other classes.
