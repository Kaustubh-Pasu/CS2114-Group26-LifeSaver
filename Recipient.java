package project3;

import java.time.LocalDateTime;

/**
 * Recipient represents someone waiting on the organ wait list
 *
 * @author Elijah Jax O'Donoghue
 * @version 2026.11.9
 */
public class Recipient {
    private int id;
    private String name;
    private String organNeeded;
    private String bloodType;
    private LocalDateTime registrationDate;
    private int urgencyLevel;
    private String status;

    // ----------------------------------------------------------
    /**
     * Create a new Recipient object.
     * 
     * @param id
     *            int user id
     * @param name
     *            string name of recipient
     * @param organNeeded
     *            string organ needed
     * @param bloodType
     *            string blood type
     * @param registrationDate
     *            date they came in
     * @param urgencyLevel
     *            1-10 how urgent
     * @param status
     *            current status
     */
    public Recipient(
        int id,
        String name,
        String organNeeded,
        String bloodType,
        LocalDateTime registrationDate,
        int urgencyLevel,
        String status) {
        this.id = id;
        this.name = name;
        this.organNeeded = organNeeded;
        this.bloodType = bloodType;
        this.registrationDate = registrationDate;
        this.urgencyLevel = urgencyLevel;
        this.status = status;

    }


    public int getId() {
        return this.id;
    }


    public void setId(int identify) {
        this.id = identify;
    }


    public String getName() {
        return this.name;
    }


    public void setName(String names) {
        this.name = names;
    }


    public String getOrganNeeded() {
        return this.organNeeded;
    }


    public void setOrganNeeded(String organNeeded) {
        this.organNeeded = organNeeded;
    }


    public String getBloodType() {
        return this.bloodType;
    }


    public LocalDateTime getRegistrationDate() {
        return this.registrationDate;
    }


    public void setRegistrationDate(LocalDateTime date) {
        this.registrationDate = date;
    }


    public int getUrgencyLevel() {
        return this.urgencyLevel;
    }


    public void setUrgencyLevel(int urgency) {
        this.urgencyLevel = urgency;
    }


    public String getStatus() {
        return this.status;
    }


    public void setStatus(String statusCurrent) {
        this.status = statusCurrent;
    }
}
