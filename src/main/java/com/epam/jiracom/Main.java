package com.epam.jiracom;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Created by Andrei_Pauliukevich1 on 4/18/2016.
 */
public class Main {
    public static void main(String[] args) {

        JiraProp jiraProp = new JiraProp();

        JCommander jc = new JCommander(jiraProp);
        jc.setCaseSensitiveOptions(true);

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.out.println("Wrong parameters:\n" + e.getMessage());
        }

        jiraProp.execute();


    }
}
