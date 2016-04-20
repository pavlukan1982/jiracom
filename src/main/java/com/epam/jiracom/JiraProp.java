package com.epam.jiracom;

import com.beust.jcommander.Parameter;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by Andrei_Pauliukevich1 on 4/20/2016.
 */
public class JiraProp {

    @Parameter(names = "-help", help = true)
    private boolean help;

    @Parameter(names = "-path", description = "Path to config file")
    private String path = "src\\main\\resources\\jira.properties";

    @Parameter(names = "-password", description = "Jira password")
    private String password;

    @Parameter(names = "-host", description = "Jira host")
    private String host;

    @Parameter(names = "username", description = "Jira user name")
    private String userName;

    private List<Runnable> workFlow;

    public JiraProp() {
        this.workFlow = Arrays.asList(
                this::doInitConfig);
    }

    public void execute() {
        this.workFlow
                .stream()
                .forEach(Runnable::run);
    }

    private void doInitConfig() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(path));
            properties.keySet()
                    .stream()
                    .forEach(o -> {
                        String value = (String) properties.get(o);
                        try {
                            Field field = getClass().getDeclaredField((String) o);
                            if (null == field.get(this)) {
                                field.set(this, value);
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
