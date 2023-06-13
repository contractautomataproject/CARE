package io.github.contractautomata.care.runnableOrchestration;

import io.github.contractautomata.care.runnableOrchestration.actions.CentralisedOrchestratorAction;
import io.github.contractautomata.care.runnableOrchestration.choice.DictatorialChoiceRunnableOrchestration;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.converters.AutDataConverter;
import io.github.contractautomata.catlib.requirements.Agreement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.util.Arrays;

import static org.junit.Assert.*;

public class DictatorialCentralisedRunnableOrchestrationTest {
    String dir = System.getProperty("user.dir")+ File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator;

    AutDataConverter<CALabel> adc = new AutDataConverter<>(CALabel::new);

    Thread alice;

    Thread bob;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        alice.interrupt();
        bob.interrupt();
    }



    //* the executed trace is (!euro,-)(?coffee,!coffee)[(!euro,-)^*]
    private void uppaal_alice( ObjectInputStream oin, ObjectOutputStream oout) throws IOException, ClassNotFoundException {
        String msg;
//centralised offerer 0
        msg = (String) oin.readObject();
        assertEquals(msg, "euro");
        msg = (String) oin.readObject(); // reading payload centralised action
        assertEquals(msg,null);
        oout.writeObject(null);
        oout.flush();
//requester: 0
        msg = (String) oin.readObject();
        assertEquals(msg, "coffee");
        msg = (String) oin.readObject(); // reading payload centralised action
        assertEquals(msg,null);
        oout.writeObject("request payload");
        oout.flush();


        //requester: 0
//choice to: 0
        msg = (String) oin.readObject(); //receiving the matching offer
        assertEquals(msg, "offer payload");

        msg = (String) oin.readObject();
        assertEquals(msg,RunnableOrchestration.choice_msg);


        //centralised offerer 0
        msg = (String) oin.readObject();

        //if the orchestrator decides not to terminate
        while (msg.equals("euro")) {
            msg = (String) oin.readObject(); // reading payload centralised action
            assertEquals(msg, null);
            oout.writeObject(null);
            oout.flush();
            msg = (String) oin.readObject();
            assertEquals(msg, RunnableOrchestration.choice_msg);
            msg = (String) oin.readObject();
        }

        assertEquals(msg,RunnableOrchestration.stop_msg);

    }

    private void uppaal_bob(ObjectInputStream oin, ObjectOutputStream oout) throws IOException, ClassNotFoundException {
        String msg;

        //matching offerer: 1
        msg = (String) oin.readObject();
        assertEquals(msg, "coffee");
        msg = (String) oin.readObject(); // reading payload centralised action
        assertEquals(msg,"request payload");
        oout.writeObject("offer payload");
        oout.flush();

//choice to: 1
        msg = (String) oin.readObject();
        assertEquals(msg,RunnableOrchestration.choice_msg);

        //in case the orchestrator decides not to terminate
        do {
            msg = (String) oin.readObject();
        } while (msg.equals(RunnableOrchestration.choice_msg));

        assertEquals(msg,RunnableOrchestration.stop_msg);


    }


    private void uppaal_check( ObjectInputStream oin, ObjectOutputStream oout) throws IOException, ClassNotFoundException {
        String choiceType = "Dictatorial";
        String actionType = "Centralised";
        String msg;
        msg = (String) oin.readObject();
        assertEquals(msg,RunnableOrchestration.check_msg);
        String received_choiceType = (String) oin.readObject();
        String received_actionType = (String) oin.readObject();
        assertEquals(received_actionType,actionType);
        assertEquals(received_choiceType,choiceType);
        oout.writeObject(RunnableOrchestration.ack_msg);
        oout.flush();
    }

    private void check(int port){
        try (ServerSocketChannel server = ServerSocketChannel.open()){
                        server.bind(new InetSocketAddress(port));
            Socket socket = server.accept().socket();
            ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
            oout.flush();
            uppaal_check(oin,oout);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
        @Test
    public void test() throws IOException, ClassNotFoundException, InterruptedException {
        alice = new Thread(()->{
            check(8080);
            try (ServerSocketChannel server = ServerSocketChannel.open()){
                server.bind(new InetSocketAddress(8080));
                Socket socket = server.accept().socket();
                ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
                oout.flush();
                uppaal_alice(oin,oout);

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        bob = new Thread(()->{
            check(8081);
            try (ServerSocketChannel server = ServerSocketChannel.open()){
                server.bind(new InetSocketAddress(8081));
                Socket socket = server.accept().socket();
                ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
                oout.flush();
                uppaal_bob(oin,oout);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        alice.start();
        bob.start();
        RunnableOrchestration ror = new DictatorialChoiceRunnableOrchestration(new Agreement(),
                adc.importMSCA(dir+"Orchestration.data"),
                Arrays.asList(null,null),
                Arrays.asList(8080,8081),
                new CentralisedOrchestratorAction());

        Thread tror = new Thread(ror);
        tror.start();
        tror.join();
    }

}