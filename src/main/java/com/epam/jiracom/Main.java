package com.epam.jiracom;

import com.beust.jcommander.JCommander;

/**
 * Created by Andrei_Pauliukevich1 on 4/18/2016.
 */
public class Main {
    public static void main(String[] args) throws Exception {

       JiraProp jiraProp = new JiraProp();

        JCommander jc = new JCommander(jiraProp);
        jc.setCaseSensitiveOptions(true);

        try {
            jc.parse(args);
            if (jiraProp.isHelp()) {
                jc.usage();
                System.exit(0);
            }
            jiraProp.execute();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Fail");
            System.exit(-1);
        }

        System.out.println("Success");
        System.exit(0);

    }
}
