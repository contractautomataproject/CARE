//package io.github.contractautomata.RunnableChoreography;
//
//import java.util.List;
//
//
//
///**
// * not currently implemented
// *
// * @author Davide Basile
// *
// */
//public class RunnableChoreographedContract implements Runnable {
//	public final static String stop_msg = "ORC_STOP";
////	private final MSCA contract;
////	private final Integer port;
////	private final List<Integer> ports;
////	private final List<String> hosts;
////	private CAState currentState;
////	private static final Random generator = new Random();
//
//	public RunnableChoreographedContract(MSCA contract, Integer port, List<String> hosts, List<Integer> ports) {
//		super();
//		if (hosts.size()!=ports.size())
//			throw new IllegalArgumentException();
////		this.port=port;
////		this.contract = contract;
////		this.hosts = hosts;
////		this.ports = ports;
////		this.currentState = contract.getStates().parallelStream()
////				.filter(State::isInitial)
////				.findAny()
////				.orElseThrow(IllegalArgumentException::new);
//	}
//
//
//	@Override
//	public void run() {
////		try (
////				AutoCloseableList<Socket> sockets = IntStream.range(0,ports.size())
////				.mapToObj(i->{
////					try {
////						if (ports.get(i)==port)
////							return null;
////						else
////							return new Socket(InetAddress.getByName(hosts.get(i)), ports.get(i));
////					}
////					catch (Exception e) {
////						e.printStackTrace();
////						throw new IllegalArgumentException(e.getMessage());
////					}
////				})
////				.collect(Collectors.toCollection(AutoCloseableList::new));
////				AutoCloseableList<BufferedReader> in = sockets.stream()
////						.filter(Objects::nonNull)
////						.map(s->getIn(s))
////						.collect(Collectors.toCollection(AutoCloseableList::new));
////				AutoCloseableList<PrintWriter> out = sockets.stream()
////						.filter(Objects::nonNull)
////						.map(s->getOut(s))
////						.collect(Collectors.toCollection(AutoCloseableList::new));
////				ServerSocket s = new ServerSocket(port);
////				Socket socket = s.accept();
////				)
////		{
////			while(true)
////			{
////				System.out.println("Current state at port " + port + " is "+currentState.toString());
////				//check final state
////				if (currentState.isFinalstate() &&
////						(contract.getForwardStar(currentState).isEmpty()||
////								this.choice(2)==0))
////				{
////					out.forEach(o->o.println(stop_msg));
////					System.out.println("Port " + port + " sending termination messages.");
////					return;
////				}
////
////				List<MSCATransition> fs = new ArrayList<>(contract.getForwardStar(currentState));
////				if (fs.isEmpty())
////					throw new RuntimeException("Deadlock!");
////
////				MSCATransition t = fs.get(this.choice(fs.size()));
////
////				System.out.println("Orchestrator, selected transition is "+t.toString());
////
////				if (t.getLabel().isRequest())
////					throw new RuntimeException("The orchestration has unmatched requests!");
////
////				if (t.getLabel().isOffer())
////					interactWith(t,in,out,null,"offerer");
////				else {
////					// match, firstly interact with the requester, forwarding the request payload to the offerer, and finally the offerer
////					// reply payload to the requester
////					String rep_req  = interactWith(t,in,out,null,"requester");
////					String rep_off = interactWith(t,in,out,rep_req.substring(rep_req.indexOf("(")),"offerer");
////					interactWith(t,in,out,rep_off.substring(rep_off.indexOf("(")),"requester");
////				}
////				currentState = t.getTarget();
////			}
////
////		}catch (IOException e) {
////			e.printStackTrace();
////			throw new RuntimeException(e.getMessage());
////		}
//
//	}
//
////	private String interactWith(MSCATransition t, AutoCloseableList<BufferedReader> in, AutoCloseableList<PrintWriter> out, String payload, String partner) throws IOException {
////		payload = (payload==null)?"()":payload;
////		Integer ind_partner = (partner.equals("offerer"))?t.getLabel().getOfferer():t.getLabel().getRequester();
////		out.get(ind_partner).println(t.getLabel().getUnsignedAction()
////				+payload);
////		String rep = in.get(ind_partner).readLine();
////		checkAnswer(rep,
////				(partner.equals("offerer"))?t.getLabel().getAction():t.getLabel().getCoAction(),
////						partner);
////		return rep;
////	}
////
////	private void checkAnswer(String rep, String expected, String partner) {
////		if (!rep.substring(0, rep.indexOf("(")).equals(expected))
////			new RuntimeException("Expecting \"" +expected+"\""+ System.lineSeparator()+
////					"Received  answer \""+rep.substring(0, rep.indexOf("(")) +
////					" from "+partner);
////	}
////
////	private BufferedReader getIn(Socket s)  {
////		try {
////			return new BufferedReader(
////					new InputStreamReader(
////							s.getInputStream()));
////		} catch (IOException e) {
////			e.printStackTrace();
////			throw new RuntimeException(e.getMessage());
////		}
////
////	}
////
////	private PrintWriter getOut(Socket s) {
////		try {
////			return	new PrintWriter(
////					new BufferedWriter(
////							new OutputStreamWriter(
////									s.getOutputStream())),true);
////		} catch (IOException e) {
////			e.printStackTrace();
////			throw new RuntimeException(e.getMessage());
////		}
////	}
////
////	public int choice(int options) {
////		return generator.nextInt(options);
////	}
//}
