[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![UPPAAL: formally verified](https://img.shields.io/badge/UPPAAL-formally%20verified-blue)](https://github.com/contractautomataproject/CARE/tree/master/src/spec/uppaal)


# Contract Automata Runtime Environment (CARE)

<tt>CARE</tt> is a library for implementing applications specified via contract automata. 

 <tt>CARE</tt>  provides a runtime environment to coordinate the <tt>CARE</tt>  services that are implementing the
  contracts of the synthesised orchestration. 
  
 Thus, <tt>CARE</tt> is the missing piece between specifications through contract automata  and their implementations, so making explicit the low-level interactions realising the prescribed actions.
 
### Scientific Publication

Basile, D., ter Beek, M.H. (2023). A Runtime Environment for Contract Automata. In: Chechik, M., Katoen, JP., Leucker, M. (eds) Formal Methods. FM 2023. Lecture Notes in Computer Science, vol 14000. Springer, Cham. https://doi.org/10.1007/978-3-031-27481-7_31



### Install

The Contract Automata Runtime Environment is released as a GitHub Package, simply add this dependency to the pom.xml of your Maven project.

```xml
<dependency>
  <groupId>io.github.contractautomataproject</groupId>
  <artifactId>care</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Usage

Check https://github.com/ContractAutomataProject/CARE_Examples  for examples of usage of <tt>CARE</tt>.


### Class Diagram

This is the  main class diagram of CARE.

<img src="https://raw.githubusercontent.com/ContractAutomataProject/CARE/master/doc/RunnableOrchestration.png"/>

<img src="https://raw.githubusercontent.com/ContractAutomataProject/CARE/master/doc/RunnableOrchestrated.png"/>



Two main elements of <tt>CARE</tt> are the abstract classes  <tt>RunnableOrchestratedContract</tt> and   <tt>RunnableOrchestration</tt>. 
This last one is a special service that reads the synthesised orchestration and orchestrates the  <tt>RunnableOrchestratedContract</tt> to realise the overall application. 
Each <tt>RunnableOrchestratedContract</tt> is a wrapper responsible for pairing the specification of a service (the contract) with its implementation.
This wrapper is listening for invocation commands from the orchestrator, and replies by invoking the corresponding method. In case the invocation is not allowed by the contract, or in case an interaction terminates prior to the fulfilment of the contract, a <tt>ContractViolationException</tt> is raised registering the remote host violating the contract. This guarantees the adherence of the implementation to the specification and allows for accountability.


Both <tt>RunnableOrchestratedContract</tt> and   <tt>RunnableOrchestration</tt> abstract from the way in which a choice is made, in the presence of branches or termination. 
This is abstracted by the method <tt>choice</tt>.  
Currently two implementations are <tt>DictatorialChoice</tt> (the orchestrator chooses alone) and <tt>MajoritarianChoice</tt> (services vote and the majority wins). 
The other aspect to concretize is the implementation of an action of a contract. 
This is abstracted by the interfaces  <tt>OrchestratedAction</tt> and <tt>OrchestratorAction</tt>, to which, respectively,  <tt>RunnableOrchestratedContract</tt> and   <tt>RunnableOrchestration</tt> depends upon. 
Currently, two implementations for the actions are <tt>CentralAction</tt> (the orchestrator acts as a proxy) and <tt>DistributedAction</tt> (the services involved in the action interacts with each other and the orchestrator).
When initialised,  <tt>RunnableOrchestration</tt> checks that all the involved <tt>RunnableOrchestratedContract</tt> have a compatible configuration (in terms of choice and action implementations).

A class <tt>TypedCALabel</tt> extending the class <tt>CALabel</tt> of <tt>CATLib</tt> is used to add the type of the parameter and the returned value to each action. 
Thus for a match between a request and an offer it is also required that their types are compatible (i.e. returned values are supertypes of the parameters of the complementary actions).

### Formal Model Verification

<tt>CARE</tt> has been formally modelled as a network of  stochastic timed automata and verified using the tool <tt>Uppaal</tt>. 
The formal model provides a detailed documentation of the behaviour of the software with traceability information relating the model to the source code. 
For more information check the README file located under the folder `src/spec/uppaal`.
