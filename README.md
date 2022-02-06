# Contract Automata Runtime Environment (CARE)

<tt>CARE</tt> is a library for implementing applications specified via contract automata. 

 <tt>CARE</tt>  provides a runtime environment to coordinate the <tt>CARE</tt>  services that are implementing the
  contracts of the synthesised orchestration. 
  
 Thus, <tt>CARE</tt> is the missing piece between specifications through contract automata  and their implementations, so making explicit the low-level interactions realising the prescribed actions.


### Structure

Two main elements of <tt>CARE</tt> are the abstract classes  <tt>RunnableOrchestratedContract</tt> and   <tt>RunnableOrchestration</tt>. 
This last one is a special service that reads the synthesised orchestration and orchestrates the  <tt>RunnableOrchestratedContract</tt> to realise the overall application. 
Each <tt>RunnableOrchestratedContract</tt> is a wrapper responsible for pairing the specification of a service (the contract) with its implementation.
This wrapper is listening for invocation commands from the orchestrator, and replies by invoking the corresponding method. In case the invocation is not allowed by the contract, a <tt>ContractViolationException</tt> is raised. This guarantees the adherence of the implementation to the specification.


### Class Diagram

This is the class diagram of CARE (click on the image to enlarge).

<img src="https://raw.githubusercontent.com/ContractAutomataProject/CARE/master/doc/RunnableOrchestration.png"/>

### Usage

Check https://github.com/ContractAutomataProject/CARE_Example  for an example of usage of <tt>CARE</tt>.
