package com.epam.jiracom.jira;

/**
 * Created by Andrei_Pauliukevich1 on 4/22/2016.
 */
public class IssueLinkType {
    private int id;
    private String name;
    private String inward;
    private String outward;

    public IssueLinkType(int id, String name, String inward, String outward) {
        this.id = id;
        this.name = name;
        this.inward = inward;
        this.outward = outward;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInward() {
        return inward;
    }

    public String getOutward() {
        return outward;
    }
}
