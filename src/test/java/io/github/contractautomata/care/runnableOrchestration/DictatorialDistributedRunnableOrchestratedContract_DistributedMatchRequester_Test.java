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
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

/**
 * in this test, one ROC services (the offerer in a match)
 * is tested against a mocked ROC service (the offerer alice) and the orchestrator
 */

public class DictatorialDistributedRunnableOrchestratedContract_DistributedMatchRequester_Test {

    String dir = System.getProperty("user.dir")+ File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator;

    AutDataConverter<CALabel> adc = new AutDataConverter<>(CALabel::new);

    Thread threadalice;
    Thread threadbob;
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
        threadbob.interrupt();
    }

    //* the executed trace is (!euro,?euro)
//
    @Test
    public void uppaal_dictatorial_distributed() throws IOException, ClassNotFoundException {
        RunnableOrchestratedContract  bob = new DictatorialChoiceRunnableOrchestratedContract(adc.importMSCA(dir+"Bob2.data"),
                8081, new Bob(), new DistributedOrchestratedAction());
        threadbob = new Thread(bob);
        threadbob.start();


        String choiceType = "Dictatorial";
        String actionType = "Distributed";
        String msg;
        String action = "";
        final int N=2;

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
            for (int i=0;i<1;i++) {
                sockets.add(new Socket(InetAddress.getByName("localhost"), 8081));
                oout.add(new  ObjectOutputStream(sockets.get(i).getOutputStream()));
                oout.get(i).flush();
                oin.add(new ObjectInputStream(sockets.get(i).getInputStream()));
            }
            int requester = 0; //the requester is bob
            oout.get(requester).writeObject("coffee");
            oout.get(requester).flush();

            final int[] port = new int[1];
            //code of the offerer of a distributed match
            threadalice = new Thread(()->{
                ServerSocket ss = null;//find a port available
                try {
                    ss = new ServerSocket(0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                port[0] = ss.getLocalPort();//communicate the port to the orchestrator

                try (	Socket socket = ss.accept();
                         ObjectInputStream osin = new ObjectInputStream(socket.getInputStream());
                         ObjectOutputStream osout = new ObjectOutputStream(socket.getOutputStream())){

                    osout.flush();
//receive request
                    String coffee = (String) osin.readObject();
//
//reply to the requester
                    osout.writeObject(10);
                    osout.flush();
//--
//wait ack
                    osin.readObject();
//---

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }});
            threadalice.start();
            oout.get(requester).writeObject("localhost");
            oout.get(requester).flush();
            Thread.sleep(1000); //give time to alice to set the port
            oout.get(requester).writeObject(port[0]);
            oout.get(requester).flush();

            oin.get(requester).readObject();

            threadalice.join();
            oout.get(0).writeObject(RunnableOrchestration.stop_msg);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

