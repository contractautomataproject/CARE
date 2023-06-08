## Formal Model of the Contract Automata Runtime Environment

This folder contains different versions of the Uppaal model of CARE (we used the version of Uppal 4.1.26-1, February 2022).

The files `model_exhaustive.xml` and `model_statistical.xml` contain the same model. 
They contain, respectively, the formulae for exhaustive and statistical model checking (with different set-ups). 
The other models are variations of this base version. 
In  `model_configurable`  the configuration options are also parameters of the templates. 
In `model_allbuffers`  each pair of services has its own buffers for communicating.

It is also included a version `model_traceability.xml` where each transition is traced back to the corresponding source code instructions.
The model is slightly polished to improve readability. 

In this case the used source code version is b1a6954, navigable here https://github.com/contractautomataproject/CARE/tree/b1a695469fbb5a4d212a96dd95bffa0ed39b71b8. 




### Traceability


Traceability information is related to the source code version b1a6954, navigable here https://github.com/contractautomataproject/CARE/tree/b1a695469fbb5a4d212a96dd95bffa0ed39b71b8. 

The `RunnableOrchestratedContract` automaton with traceability is displayed below.

[RunnableOrchestratedContract](https://raw.githubusercontent.com/contractautomataproject/CARE/7bdcef1ddb529af51fabbb65ad31358e41e64a2b/src/spec/uppaal/RunnableOrchestratedContract.svg)
<img src="https://raw.githubusercontent.com/contractautomataproject/CARE/master/src/spec/uppaal/RunnableOrchestratedContract.svg?sanitize=true">



The `RunnableOrchestration` automaton with traceability is displayed below.

[RunnableOrchestration](https://github.com/contractautomataproject/CARE/raw/master/src/spec/uppaal/RunnableOrchestration.svg?sanitize=true)
<img src="https://raw.githubusercontent.com/contractautomataproject/CARE/7bdcef1ddb529af51fabbb65ad31358e41e64a2b/src/spec/uppaal/RunnableOrchestration.svg">


The `SocketTimeout` automaton with traceability is displayed below.


[SocketTimeout](https://github.com/contractautomataproject/CARE/raw/master/src/spec/uppaal/SocketTimeout.svg?sanitize=true)
<img src="https://raw.githubusercontent.com/contractautomataproject/CARE/7bdcef1ddb529af51fabbb65ad31358e41e64a2b/src/spec/uppaal/SocketTimeout.svg">
