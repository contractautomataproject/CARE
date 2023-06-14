package io.github.contractautomata.care.runnableOrchestration;

import io.github.contractautomata.care.runnableOrchestration.actions.DistributedOrchestratorAction;
import io.github.contractautomata.care.runnableOrchestration.choice.MajoritarianChoiceRunnableOrchestration;
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

import static org.junit.Assert.assertEquals;

public class MajoritarianDistributedRunnableOrchestrationTest {

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



    //* the executed trace is (!euro,-)(?coffee,!coffee)
    private void uppaal_alice( ObjectInputStream oin, ObjectOutputStream oout) throws IOException, ClassNotFoundException {
        String msg;
        //offer type to offerer: 0
        msg = (String) oin.readObject();
        assertEquals(msg, "euro");
        msg = (String) oin.readObject();
        assertEquals(msg,"offer"); // reading  distributed action type
        msg = (String) oin.readObject(); // reading payload distributed action
        assertEquals(msg,null);
        oout.writeObject(null);
        oout.flush();
        // action to offerer: 0
        msg = (String) oin.readObject();
        assertEquals(msg, "coffee");
        msg = (String) oin.readObject();
        assertEquals(msg,"match"); // reading  distributed action type
        oout.writeObject(8080); //port
        oout.flush();
        msg = (String) oin.readObject();
        assertEquals(msg,RunnableOrchestration.stop_msg);
    }

    private void uppaal_bob(ObjectInputStream oin, ObjectOutputStream oout) throws IOException, ClassNotFoundException {
        String msg;
        msg = (String) oin.readObject();
        assertEquals(msg, "coffee");
        msg = (String) oin.readObject();
        assertEquals(msg,null); // reading  address (null)
        int port = (Integer) oin.readObject(); // reading  port
        assertEquals(port,8080);
        oout.writeObject(null);
        oout.flush();

        msg = (String) oin.readObject();
        assertEquals(msg,RunnableOrchestration.stop_msg);

    }


    private void uppaal_check( ObjectInputStream oin, ObjectOutputStream oout) throws IOException, ClassNotFoundException {
        String choiceType = "Majoritarian";
        String actionType = "Distributed";
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
        RunnableOrchestration ror = new MajoritarianChoiceRunnableOrchestration(new Agreement(),
                adc.importMSCA(dir+"Orchestration2.data"),
                Arrays.asList(null,null),
                Arrays.asList(8080,8081),
                new DistributedOrchestratorAction());

        Thread tror = new Thread(ror);
        tror.start();
        tror.join();

    }

}