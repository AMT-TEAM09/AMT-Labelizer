package ch.heigvd.amt.team09.simpleclient;

import ch.heigvd.amt.team09.simpleclient.scenario.Scenario;
import ch.heigvd.amt.team09.simpleclient.scenario.Scenario1;
import ch.heigvd.amt.team09.simpleclient.scenario.Scenario2;
import ch.heigvd.amt.team09.simpleclient.scenario.Scenario3;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class SimpleClient {
    private static final Scenario[] scenarios = new Scenario[]{
            new Scenario1(),
            new Scenario2(),
            new Scenario3()
    };

    @Parameter(names = {"--help", "-h"}, help = true, order = -3)
    private boolean help;
    @Parameter(description = "<no scenario> (omit to run all scenarios)")
    private String noScenario;

    public static void main(String[] args) {
        var app = new SimpleClient();
        var jcommander = JCommander.newBuilder()
                .addObject(app)
                .programName(SimpleClient.class.getSimpleName())
                .build();

        try {
            jcommander.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            jcommander.usage();
            return;
        }

        if (app.help) {
            jcommander.usage();
            return;
        }

        app.run();
    }

    public void run() {
        if (noScenario == null) {
            run(scenarios);
            return;
        }

        var no = parseNo(noScenario);
        if (no == 0 || no > scenarios.length) {
            System.out.println("Invalid scenario no, must be between 1 and " + scenarios.length);
            return;
        }

        run(scenarios[no - 1]);
    }

    private int parseNo(String no) {
        try {
            return Integer.parseInt(no);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void run(Scenario... scenarios) {
        for (var scenario : scenarios) {
            scenario.start();
        }
    }
}