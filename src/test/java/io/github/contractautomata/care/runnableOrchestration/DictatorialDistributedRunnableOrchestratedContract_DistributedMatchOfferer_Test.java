package io.github.contractautomata.care.runnableOrchestration;

import io.github.contractautomata.care.runnableOrchestration.actions.DistributedOrchestratedAction;
import io.github.contractautomata.care.runnableOrchestration.choice.DictatorialChoiceRunnableOrchestratedContract;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.converters.AutDataConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

/**
 * in this test, one ROC services (the offerer in a match)
 * is tested against a mocked ROC service (the requester bob) and the orchestrator
 */

public class DictatorialDistributedRunnableOrchestratedContract_DistributedMatchOfferer_Test {

    String dir = System.getProperty("user.dir")+ File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator;

    AutDataConverter<CALabel> adc = new AutDataConverter<>(CALabel::new);

    Thread threadalice;
    Thread threadbob;
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
        threadalice.interrupt();
    }

    //* the executed trace is (!euro,?euro)
//
    @Test
    public void uppaal_dictatorial_distributed() throws IOException, ClassNotFoundException {
        RunnableOrchestratedContract  alice = new DictatorialChoiceRunnableOrchestratedContract(adc.importMSCA(dir+"Alice2.data"),
                8080, new Alice(), new DistributedOrchestratedAction());
        threadalice= new Thread(alice);
        threadalice.start();


        String choiceType = "Dictatorial";
        String actionType = "Distributed";
        String msg;
        String action = "";
        int offerer;
        final int N=2;

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

        try (AutoCloseableList<Socket> sockets = new AutoCloseableList<>();
             AutoCloseableList<ObjectOutputStream> oout = new AutoCloseableList<>();
             AutoCloseableList<ObjectInputStream> oin = new AutoCloseableList<>())
        {
            for (int i=0;i<1;i++) {
                sockets.add(new Socket(InetAddress.getByName("localhost"), 8080));
                oout.add(new  ObjectOutputStream(sockets.get(i).getOutputStream()));
                oout.get(i).flush();
                oin.add(new ObjectInputStream(sockets.get(i).getInputStream()));
            }
            action = "coffee";
            offerer = 0;
            oout.get(offerer).writeObject(action);
            oout.get(offerer).flush();
            oout.get(offerer).writeObject("match");
            oout.get(offerer).flush();
            final int port=(Integer) oin.get(offerer).readObject();

            threadbob = new Thread(()->{
                try (Socket socket = new Socket(InetAddress.getByName("localhost"), port);
                     ObjectOutputStream osout = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream osin = new ObjectInputStream(socket.getInputStream());){

                    osout.flush();

                    //sending request
                    osout.writeObject("request details");
                    osout.flush();
                    int coffee = (Integer) osin.readObject();
                    //send ack
                    osout.writeObject(null);
                    osout.flush();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }});
            threadbob.start();
            threadbob.join();

            oout.get(0).writeObject(RunnableOrchestration.stop_msg);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

