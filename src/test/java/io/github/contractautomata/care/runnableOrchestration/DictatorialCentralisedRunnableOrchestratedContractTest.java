package io.github.contractautomata.care.runnableOrchestration;

import io.github.contractautomata.care.runnableOrchestration.actions.CentralisedOrchestratedAction;
import io.github.contractautomata.care.runnableOrchestration.choice.DictatorialChoiceRunnableOrchestratedContract;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.converters.AutDataConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.Strict.class)
public class DictatorialCentralisedRunnableOrchestratedContractTest {

    String dir = System.getProperty("user.dir")+ File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator;

    AutDataConverter<CALabel> adc = new AutDataConverter<>(CALabel::new);

    Thread threadalice;
    Thread threadbob;
    @Before
    public void setUp() throws Exception {
        RunnableOrchestratedContract alice = new DictatorialChoiceRunnableOrchestratedContract(adc.importMSCA(dir+"Alice.data"),
                8080, new Alice(), new CentralisedOrchestratedAction());
        RunnableOrchestratedContract bob = new DictatorialChoiceRunnableOrchestratedContract(adc.importMSCA(dir+"Bob.data"),
                8081, new Bob(), new CentralisedOrchestratedAction());

        threadalice = new Thread(alice);
        threadbob = new Thread(bob);
        threadalice.start();
        threadbob.start();
    }

    @After
    public void tearDown() {
        threadalice.interrupt();
        threadbob.interrupt();
    }

    //* the executed trace is (!euro,-,-)(-,!euro,-)(?coffee,-,!coffee)(-,?coffee,!coffee)
    //
    @Test
    public void uppaal_dictatorial_centralised() throws IOException, ClassNotFoundException {

        String choiceType = "Dictatorial";
        String actionType = "Centralised";
        String msg;
        String action = "";
        int offerer;
        int requester;
        final int N=3;

        try (Socket s = new Socket("localhost", 8080);
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(s.getInputStream()))
        {
            oos.writeObject(RunnableOrchestration.check_msg);
            oos.writeObject(choiceType);
            oos.writeObject(actionType);
            oos.flush();
            msg = (String)ois.readObject();
            assertEquals(msg,RunnableOrchestration.ack_msg);
        }
        try (Socket s = new Socket("localhost", 8080);
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(s.getInputStream()))
        {
            oos.writeObject(RunnableOrchestration.check_msg);
            oos.writeObject(choiceType);
            oos.writeObject(actionType);
            oos.flush();
            msg = (String)ois.readObject();
            assertEquals(msg,RunnableOrchestration.ack_msg);
        }
        try (Socket s = new Socket("localhost", 8081);
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(s.getInputStream()))
        {
            oos.writeObject(RunnableOrchestration.check_msg);
            oos.writeObject(choiceType);
            oos.writeObject(actionType);
            oos.flush();
            msg = (String)ois.readObject();
            assertEquals(msg,RunnableOrchestration.ack_msg);
        }
        try (AutoCloseableList<Socket> sockets = new AutoCloseableList<>();
             AutoCloseableList<ObjectOutputStream> oout = new AutoCloseableList<>();
             AutoCloseableList<ObjectInputStream> oin = new AutoCloseableList<>())
        {
            for (int i=0;i<N;i++) {
                sockets.add(new Socket(InetAddress.getByName("localhost"), i<2?8080:8081));
                oout.add(new  ObjectOutputStream(sockets.get(i).getOutputStream()));
                oout.get(i).flush();
                oin.add(new ObjectInputStream(sockets.get(i).getInputStream()));
            }
            oout.get(0).writeObject(RunnableOrchestration.choice_msg);
            oout.get(0).flush();
            oout.get(1).writeObject(RunnableOrchestration.choice_msg);
            oout.get(1).flush();
            oout.get(2).writeObject(RunnableOrchestration.choice_msg);
            oout.get(2).flush();
            action = "euro";
            offerer = 0;
            oout.get(offerer).writeObject(action);
            oout.get(offerer).flush();
            oout.get(offerer).writeObject(null);
            oout.get(offerer).flush();
            oin.get(offerer).readObject();
            action = "euro";
            offerer = 1;
            oout.get(offerer).writeObject(action);
            oout.get(offerer).flush();
            oout.get(offerer).writeObject(null);
            oout.get(offerer).flush();
            oin.get(offerer).readObject();
            action = "coffee";
            requester = 0;
            oout.get(requester).writeObject(action);
            oout.get(requester).flush();
            oout.get(requester).writeObject(null);
            oout.get(requester).flush();
            oin.get(requester).readObject();
            offerer = 2;
            oout.get(offerer).writeObject(action);
            oout.get(offerer).flush();
            oout.get(offerer).writeObject(null);
            oout.get(offerer).flush();
            oin.get(offerer).readObject();
            oout.get(requester).writeObject(null);
            oout.get(requester).flush();
            action = "coffee";
            requester = 1;
            oout.get(requester).writeObject(action);
            oout.get(requester).flush();
            oout.get(requester).writeObject(null);
            oout.get(requester).flush();
            oin.get(requester).readObject();
            offerer = 2;
            oout.get(offerer).writeObject(action);
            oout.get(offerer).flush();
            oout.get(offerer).writeObject(null);
            oout.get(offerer).flush();
            oin.get(offerer).readObject();
            oout.get(requester).writeObject(null);
            oout.get(requester).flush();
            oout.get(0).writeObject(RunnableOrchestration.stop_msg);
            oout.get(1).writeObject(RunnableOrchestration.stop_msg);
            oout.get(2).writeObject(RunnableOrchestration.stop_msg);
        }
    }
}

