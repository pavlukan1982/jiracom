package com.epam.jiracom;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.epam.jiracom.commands.HelpCommand;
import com.epam.jiracom.commands.QuitCommand;
import com.epam.jiracom.commands.RunCommand;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andrei_Pauliukevich1 on 4/18/2016.
 */
public class Main {

    public static Pattern pattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");



    public static void main(String[] args) {

        HelpCommand helpCommand = new HelpCommand();
        QuitCommand quitCommand = new QuitCommand();
        RunCommand runCommand = new RunCommand();

        Scanner scanner = new Scanner(System.in);


        String[] argv = args;
        boolean interactive = false;


        do {

            JCommander jc = new JCommander();
            jc.setCaseSensitiveOptions(true);
            jc.addCommand(helpCommand);
            jc.addCommand(quitCommand);
            jc.addCommand(runCommand);

            try {
                jc.parse(argv);
            } catch (ParameterException e) {
                System.out.println("Wrong parameters:\n" + e.getMessage());
            }

            if ("run".equalsIgnoreCase(jc.getParsedCommand())) {
                if (!interactive) {
                    if (runCommand.isInteractive()) {
                        interactive = true;
                    } else {
                        break;
                    }
                }
            }

            if ("quit".equalsIgnoreCase(jc.getParsedCommand())) {
                break;
            }

            if ("help".equalsIgnoreCase(jc.getParsedCommand())) {
                jc.usage();
            }

            if (!interactive) {
                break;
            }
            System.out.print("Enter command : ");
            Matcher matcher = pattern.matcher(scanner.nextLine());
            List<String> paramList = new ArrayList<>();
            while (matcher.find()) {
                paramList.add(matcher.group(1));
            }
            argv = paramList.toArray(new String[paramList.size()]);

        } while (interactive);
    }
}
