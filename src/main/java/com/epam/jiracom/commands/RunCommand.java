package com.epam.jiracom.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.Properties;

/**
 * Created by Andrei_Pauliukevich1 on 4/19/2016.
 */
@Parameters(commandNames = "run", commandDescription = "Run program")
public class RunCommand {

    @Parameter(names = {"-interactive", "-i"}, description = "Run program in interactive mode")
    private boolean interactive = false;

    @Parameter(names = {"-config", "-c"}, description = "Use settings from config file")
    public Properties config;

    public boolean isInteractive() {
        return interactive;
    }
}
