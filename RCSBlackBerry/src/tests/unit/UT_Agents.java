package tests.unit;

import java.util.Vector;

import net.rim.device.api.util.DataBuffer;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;
import blackberry.AgentManager;
import blackberry.Conf;
import blackberry.EventManager;
import blackberry.Status;
import blackberry.action.Action;
import blackberry.action.SubAction;
import blackberry.agent.Agent;
import blackberry.event.Event;
import blackberry.event.TimerEvent;
import blackberry.utils.Utils;

public class UT_Agents extends TestUnit {

    public UT_Agents(final String name, final Tests tests) {
        super(name, tests);
    }

    private void AgentSnapshot() throws AssertException {
        // #debug
        debug.info("-- AgentSnapshot --");

        final Status status = Status.getInstance();
        status.clear();
        final AgentManager agentManager = AgentManager.getInstance();

        final byte[] conf = new byte[8];
        final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length,
                false);
        databuffer.writeInt(10000);
        databuffer.writeInt(0);

        final Agent agent = Agent.factory(Agent.AGENT_SNAPSHOT, true, conf);
        AssertNotNull(agent, "Agent");

        status.addAgent(agent);

        AssertThat(agent.isEnabled(), "Agent not Enabled 1");

        agentManager.startAll();
        Utils.sleep(400);

        agentManager.stopAll();

      //#debug
        debug.trace("-- AgentSnapshot OK --");
    }

    boolean ExecuteAction(final Action action) {
        final Vector subActions = action.getSubActionsList();
        action.setTriggered(false, null);

        for (int j = 0; j < subActions.size(); j++) {

            final SubAction subAction = (SubAction) subActions.elementAt(j);
            final boolean ret = subAction.execute(null);

            if (ret == false) {
                break;
            }

            if (subAction.wantUninstall()) {
                // #debug
                debug.warn("CheckActions() uninstalling");
                return false;
            }

            if (subAction.wantReload()) {
                // #debug
                debug.warn("CheckActions() reloading");
                return true;
            }
        }

        return true;
    }

    private void RestartAll() throws AssertException {
        // #debug
        debug.info("-- RestartAll --");
        final Status status = Status.getInstance();
        status.clear();
        final AgentManager agentManager = AgentManager.getInstance();

        final byte[] conf = new byte[8];
        final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length,
                false);
        databuffer.writeInt(10000);
        databuffer.writeInt(0);

        final Agent agent1 = Agent.factory(Agent.AGENT_SNAPSHOT, false, conf);
        AssertNotNull(agent1, "AGENT_SNAPSHOT");
        status.addAgent(agent1);

        final Agent agent2 = Agent.factory(Agent.AGENT_CAM, true, null);
        AssertNotNull(agent2, "AGENT_CAM");
        status.addAgent(agent2);

        final Agent agent3 = Agent.factory(Agent.AGENT_URL, true, null);
        AssertNotNull(agent3, "AGENT_URL");
        status.addAgent(agent3);

        AssertEquals(agent1.getRunningLoops(), 0,
                "Agent1.runningLoops should be 0");
        AssertEquals(agent2.getRunningLoops(), 0,
                "Agent2.runningLoops should be 0");
        AssertEquals(agent3.getRunningLoops(), 0,
                "Agent3.runningLoops should be 0");

        // partenza di tutti e tre gli agenti, il primo e' disabilitato

      //#debug
        debug.trace("1");
        boolean ret = agentManager.startAll();
        AssertThat(ret, "cannot start all");

        Utils.sleep(1000);

        AssertEquals(agent1.getRunningLoops(), 0,
                "Agent1.runningLoops should be 0");
        AssertEquals(agent2.getRunningLoops(), 1,
                "Agent2.runningLoops should be 1");
        AssertEquals(agent3.getRunningLoops(), 1,
                "Agent3.runningLoops should be 1");

        // verifico che solo due siano running e enabled
      //#debug
        debug.trace("2");
        AssertThat(!agent1.isRunning(), "agent1 should not run");
        AssertThat(agent2.isRunning(), "agent2 should run");
        AssertThat(agent3.isRunning(), "agent3 should run");

        AssertThat(!agent1.isEnabled(), "agent1 should not be enabled");
        AssertThat(agent2.isEnabled(), "agent2 should be enabled");
        AssertThat(agent3.isEnabled(), "agent3 should be enabled");

        // restartAgent1
        agentManager.reStart(agent1.agentId);
        // restartAgent2
        agentManager.reStart(agent2.agentId);

        Utils.sleep(2000);

      //#debug
        debug.trace("3");
        AssertEquals(agent1.getRunningLoops(), 0,
                "Agent1.runningLoops should be 0");
        AssertEquals(agent2.getRunningLoops(), 2,
                "Agent2.runningLoops should be 2");
        AssertEquals(agent3.getRunningLoops(), 1,
                "Agent3.runningLoops should be 1");

        AssertThat(!agent1.isRunning(), "agent1 should not run");
        AssertThat(agent2.isRunning(), "agent2 should run");
        AssertThat(agent3.isRunning(), "agent3 should run");

        AssertThat(!agent1.isEnabled(), "agent1 should not be enabled");
        AssertThat(agent2.isEnabled(), "agent2 should be enabled");
        AssertThat(agent3.isEnabled(), "agent3 should be enabled");

        // restartAgent3
        agentManager.reStart(agent3.agentId);

        Utils.sleep(2000);
      //#debug
        debug.trace("4");

        AssertEquals(agent1.getRunningLoops(), 0,
                "Agent1.runningLoops should be 0");
        AssertEquals(agent2.getRunningLoops(), 2,
                "Agent2.runningLoops should be 2");
        AssertEquals(agent3.getRunningLoops(), 2,
                "Agent3.runningLoops should be 2");

        AssertThat(!agent1.isRunning(), "agent1 should not run");
        AssertThat(agent2.isRunning(), "agent2 should run");
        AssertThat(agent3.isRunning(), "agent3 should run");

        AssertThat(!agent1.isEnabled(), "agent1 should not be enabled");
        AssertThat(agent2.isEnabled(), "agent2 should be enabled");
        AssertThat(agent3.isEnabled(), "agent3 should be enabled");

        // stop all
      //#debug
        debug.trace("5");
        ret = agentManager.stopAll();
        AssertThat(ret, "cannot stop all");

        AssertThat(!agent1.isRunning(), "agent1 should not run");
        AssertThat(!agent2.isRunning(), "agent2 should not run");
        AssertThat(!agent3.isRunning(), "agent3 should not run");

      //#debug
        debug.trace("-- RestartAll OK --");
    }

    public boolean run() throws AssertException {

        StartAndStop();
        RestartAll();

        StartStopAgent();
        AgentSnapshot();

        return true;
    }

    public boolean StartAndStop() throws AssertException {
        // #debug
        debug.info("-- StartAndStop --");

        final Status status = Status.getInstance();
        status.clear();
        final AgentManager agentManager = AgentManager.getInstance();

        final Agent agent = Agent.factory(Agent.AGENT_DEVICE, true, null);
        AssertNotNull(agent, "Agent");

        status.addAgent(agent);

        AssertThat(agent.isEnabled(), "Agent not Enabled 1");

        // start all
        agentManager.startAll();
        Utils.sleep(1000);

        AssertThat(agent.isRunning(), "Agent not Running 1");

        // stop all
        agentManager.stopAll();

        Utils.sleep(1000);
        AssertThat(agent.isEnabled(), "Agent not Enabled 2");
        AssertThat(!agent.isRunning(), "Agent still running");

      //#debug
        debug.trace("-- StartAndStop OK --");
        return true;
    }

    public boolean StartStopAgent() throws AssertException {
        // #debug
        debug.info("-- StartStopAgent --");

        final Status status = Status.getInstance();
        status.clear();
        final AgentManager agentManager = AgentManager.getInstance();
        final EventManager eventManager = EventManager.getInstance();

        // genero due agenti, di cui uno disabled
        // #debug
        debug.trace("agent");
        final Agent agentDevice = Agent.factory(Agent.AGENT_DEVICE, true, null);
        status.addAgent(agentDevice);
        final Agent agentPosition = Agent.factory(Agent.AGENT_POSITION, false,
                null);
        status.addAgent(agentPosition);

        // eseguo gli agenti
        // #debug
        debug.trace("start agent");
        agentManager.startAll();
        Utils.sleep(400);

        // verifico che uno solo parta
        // #debug
        debug.trace("one running");
        AssertThat(agentDevice.isRunning(), "Agent not Running 2");
        AssertThat(!agentPosition.isEnabled(), "Agent not disabled 1");

        // Creo azione 0 che fa partire l'agent position
        // #debug
        debug.trace("action 0");
        final Action action0 = new Action(0);
        final byte[] confParams = new byte[4];
        final DataBuffer databuffer = new DataBuffer(confParams, 0,
                confParams.length, false);
        databuffer.writeInt(Agent.AGENT_POSITION);

        action0.addNewSubAction(SubAction.ACTION_START_AGENT, confParams);
        status.addAction(action0);

        // Creo azione 1 che fa ferma l'agent position
        // #debug
        debug.trace("action 1");
        final Action action1 = new Action(1);
        action1.addNewSubAction(SubAction.ACTION_STOP_AGENT, confParams);
        status.addAction(action1);

        // Creo l'evento timer che esegue azione 0
        // #debug
        debug.trace("event 0");
        final Event event0 = new TimerEvent(0, Conf.CONF_TIMER_SINGLE, 2000, 0);
        status.addEvent(0, event0);

        // Creo eveneto timer che esegue azione 1
        // #debug
        debug.trace("event 1");
        final Event event1 = new TimerEvent(1, Conf.CONF_TIMER_SINGLE, 4000, 0);
        status.addEvent(1, event1);

        AssertThat(!event0.isRunning(), "Event0 running");
        AssertThat(!event1.isRunning(), "Event1 running");

        // lancio i thread degli eventi
        // #debug
        debug.trace("start event");
        eventManager.startAll();

        // verifico che gli eventi siano partiti.
        Utils.sleep(500);
        // #debug
        debug.trace("event running");
        AssertThat(event0.isScheduled(), "Event0 not scheduled 1");
        AssertThat(event1.isScheduled(), "Event1 not scheduled 1");
        AssertThat(!event0.isRunning(), "Event0 running");
        AssertThat(!event1.isRunning(), "Event1 running");

        // verifica che dopo 2 secondo l'azione sia triggered
        Utils.sleep(2000);
        // #debug
        debug.trace("triggered 0");
        AssertThat(action0.isTriggered(), "action0 not triggered 1");
        AssertThat(!action1.isTriggered(), "action1 triggered 1");

        // #debug
        debug.trace("action 0");
        ExecuteAction(action0);
        Utils.sleep(500);

        AssertThat(agentDevice.isRunning(), "Agent not Running 3");
        AssertThat(agentPosition.isRunning(), "Agent not Running 4");

        // verifica che dopo 2 secondi l'azione 1 sia triggered
        Utils.sleep(2000);
        // #debug
        debug.trace("triggered 1");
        AssertThat(action1.isTriggered(), "action1 not triggered 2");
        AssertThat(!action0.isTriggered(), "action0 triggered 2");

        // #debug
        debug.trace("action 1");
        ExecuteAction(action1);
        Utils.sleep(500);

        AssertThat(agentDevice.isRunning(), "Agent not Running 5");
        AssertThat(agentPosition.isEnabled(), "Agent not enabled 1");

        AssertThat(event0.isRunning(), "Event0 running");
        AssertThat(event1.isRunning(), "Event1 running");
        AssertThat(!action0.isTriggered(), "action0 triggered");
        AssertThat(!action1.isTriggered(), "action1 triggered");

        // fermo gli eventi
        // #debug
        debug.trace("stop event");
        eventManager.stopAll();

        AssertThat(!event0.isRunning(), "Event0 running");
        AssertThat(!event1.isRunning(), "Event1 running");

        agentManager.stopAll();
      //#debug
        debug.trace("-- StartStopAgent OK --");
        return true;
    }

}