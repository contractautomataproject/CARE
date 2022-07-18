## Formal Model of the Contract Automata Runtime Environment

This folder contains three versions of the Uppaal model (version of Uppal 4.1.26-1, February 2022).

The file `model.xml` is the base version of the model,  without delays and used for standard model checking. 
All other models are variations of this base version. 
In  `model_configurable`  the configuration options are also parameters of the templates. 
In `model_realtime`  each location has  delays that are exponentially distributed, and this model is used for statistical model checking. 
In `model_allbuffers`  each pair of services has its own buffers for communicating.

It is also included a version `model_traceability.xml` that is the identical to `model.xml` but  each transition is traced back to the corresponding source code instructions.
In this case the used source code version is b1a6954, navigable here https://github.com/contractautomataproject/CARE/tree/b1a695469fbb5a4d212a96dd95bffa0ed39b71b8. 




### RunnableOrchestratedContract

The `RunnableOrchestratedContract` automaton with traceability is displayed below.

[RunnableOrchestratedContract](https://github.com/contractautomataproject/CARE/raw/master/src/spec/uppaal/RunnableOrchestratedContract.svg?sanitize=true)
<img src="https://raw.githubusercontent.com/contractautomataproject/CARE/master/src/spec/uppaal/RunnableOrchestratedContract.svg?sanitize=true">



## RunnableOrchestration


The `RunnableOrchestration` automaton with traceability is displayed below.

[RunnableOrchestration](https://github.com/contractautomataproject/CARE/raw/master/src/spec/uppaal/RunnableOrchestration.svg?sanitize=true)
<img src="https://raw.githubusercontent.com/contractautomataproject/CARE/9b9ebda1528394b868272c8672e4d96b5ce6ee09/src/spec/uppaal/RunnableOrchestration.svg?sanitize=true">
