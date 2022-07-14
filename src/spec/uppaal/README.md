### Formal Model of the Contract Automata Runtime Environment in Uppaal

This folder contains three versions of the Uppaal model.
It has been used the current last version of Uppal 4.1.26-1, February 2022.

The file `model.xml` is the version without delays, used for standard model checking.
The file `model_configurable` is the version of the model where the configurations options are also parameters of the templates. 
The file `model_realtime` is the version where in each location delays are exponentially distributed and is used for statistical model checking. 

It is also included a version `model_traceability.xml` where each transition is traced back to the corresponding source code instructions.
In this case the used source code version is b1a6954, navigable here https://github.com/contractautomataproject/CARE/tree/b1a695469fbb5a4d212a96dd95bffa0ed39b71b8. 

The `RunnableOrchestration` automaton with traceability can be viewed here:
https://github.com/contractautomataproject/CARE/raw/master/src/spec/uppaal/Runnable%20Orchestration%20with%20Traceability.pdf

The `RunnableOrchestratedContract` automaton with traceability can be viewed here:
https://github.com/contractautomataproject/CARE/raw/master/src/spec/uppaal/Runnable%20Orchestrated%20Contract%20with%20Traceability.pdf
