package io.github.contractautomata.care.runnableOrchestration;

import io.github.contractautomata.care.runnableOrchestration.actions.CentralisedOrchestratedAction;
import io.github.contractautomata.care.runnableOrchestration.choice.MajoritarianChoiceRunnableOrchestratedContract;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;
import io.github.contractautomata.catlib.converters.AutDataConverter;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;


@RunWith(MockitoJUnitRunner.Strict.class)
public class MajoritarianChoiceRunnableOrchestratedContractTest {

    String dir = System.getProperty("user.dir")+ File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator;

    AutDataConverter<CALabel> adc = new AutDataConverter<>(CALabel::new);

    Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, CALabel>> contract;

    Alice service;

    RunnableOrchestratedContract alice;

    @Before
    public void setUp() throws Exception {
        contract= adc.importMSCA(dir+"Alice.data");
        service = new Alice();
        alice = new MajoritarianChoiceRunnableOrchestratedContract(contract,
                8080, service, new CentralisedOrchestratedAction());

    }

    private <T> void send(AutoCloseableList<ObjectOutputStream> oout, List<T> msg) throws IOException {
        for (ObjectOutputStream o : oout) {
            for (T m : msg) {
                o.writeObject(m);
                o.flush();
            }
        }
    }
    private <T> List<T> receive(AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException {
        List<T> choices = new ArrayList<>();
        for (ObjectInputStream o : oin) {
            choices.add((T) o.readObject());
        }
        return choices;
    }
    @Test
    public void testAllChoice() throws IOException, ClassNotFoundException {
        new Thread(alice).start();
        final int N=3;
        try (AutoCloseableList<Socket> sockets = new AutoCloseableList<>();
             AutoCloseableList<ObjectOutputStream> oout = new AutoCloseableList<>();
             AutoCloseableList<ObjectInputStream> oin = new AutoCloseableList<>())
        {
            for (int i=0;i<N;i++) {
                sockets.add(new Socket(InetAddress.getByName("localhost"), 8080));
                oout.add(new ObjectOutputStream(sockets.get(i).getOutputStream()));
                oout.get(i).flush();
                oin.add(new ObjectInputStream(sockets.get(i).getInputStream()));
            }
            send(oout,List.of(RunnableOrchestration.choice_msg));
            send(oout,List.of(RunnableOrchestration.choice_msg));

            List<String[]> l = new ArrayList<>();
            l.add(new String[]{"!euro", "!dollar"});
            send(oout,l);

            List<String> choices = receive(oin);

            for (int i=0;i<N;i++) {
                assertTrue(choices.get(i).equals("!euro") || choices.get(i).equals("!dollar"));
//                final int ind=i;
//                assertThrows("Not in a final state!", RuntimeException.class, () -> {
//                    oout.get(ind).writeObject(RunnableOrchestration.stop_msg);
//                    oout.get(ind).flush();
//                });
            }

            send(oout, List.of("euro",""));
            send(oout, List.of("coffee",""));
            send(oout, List.of("RunnableOrchestration.stop_msg"));
        }
    }


    @Test
    public void testChoiceSkip() throws IOException, ClassNotFoundException {
        new Thread(alice).start();
        final int N=3;
        try (AutoCloseableList<Socket> sockets = new AutoCloseableList<>();
             AutoCloseableList<ObjectOutputStream> oout = new AutoCloseableList<>();
             AutoCloseableList<ObjectInputStream> oin = new AutoCloseableList<>())
        {
            for (int i=0;i<N;i++) {
                sockets.add(new Socket(InetAddress.getByName("localhost"), 8080));
                oout.add(new ObjectOutputStream(sockets.get(i).getOutputStream()));
                oout.get(i).flush();
                oin.add(new ObjectInputStream(sockets.get(i).getInputStream()));
            }
            send(oout,List.of(RunnableOrchestration.choice_msg));

            ObjectOutputStream oout_skipped = oout.remove(N-1);
            oin.remove(N-1);

            send(oout,List.of(RunnableOrchestration.choice_msg));

            oout_skipped.writeObject(null);//skipping, not involved in the choice
            oout_skipped.flush();

            List<String[]> l = new ArrayList<>();
            l.add(new String[]{"!euro", "!dollar"});
            send(oout,l);

            List<String> choices = receive(oin);

            for (int i=0;i<N-1;i++) {
                assertTrue(choices.get(i).equals("!euro") || choices.get(i).equals("!dollar"));
            }

            oout.add(oout_skipped);//reinserting the service not involved in the choice

            send(oout, List.of("euro",""));
            send(oout, List.of("coffee",""));
            send(oout, List.of("RunnableOrchestration.stop_msg"));
        }
    }
}