# Iroha sample app

This is a warm-up exercise for Hyperledger Hackfest - June 2018\
You have to clone this repository. It has one test that fails, your task is to change the class that it is testing, in order to make the test pass.

The class that you should change is located here:\
`src/main/kotlin/IrohaClient.kt`

The test itself:\
`src/test/kotlin/LedgerStatusTest.kt`

Please read carefully the whole code and understand what it does, this way it will be much easier to get the right result.\
You are free to change any file in this repository except the test.
Hint: only `genesis.block` and `IrohaClient.kt` have to be changed.

To run the test just use:\
`./gradlew clean test --info`
 
## Important
Please use develop version of [iroha](https://github.com/hyperledger/iroha) \
On each run of the test, it will change the state of the ledger, please consider restarting it when you want to test your program.
## Documentation

Check [getting started](http://iroha.readthedocs.io/en/latest/getting_started/) section in your version of localized docs to start exploring the system.

For more information, such as how to use client libraries in your target programming language, or how to deploy Iroha in a network check the rest of the documentation.

## Need help?

* Join [telegram chat](https://t.me/hyperledgeriroha) where the maintainers team is able to help you
* Communicate in Gitter chat with our development community [![Join the chat at https://gitter.im/hyperledger-iroha/Lobby](https://badges.gitter.im/hyperledger-iroha/Lobby.svg)](https://gitter.im/hyperledger-iroha/Lobby)
* Join [HyperLedger RocketChat](https://chat.hyperledger.org) #iroha channel to discuss your concerns 
* Contact me directly through telegram: [@x3medima17](https://t.me/x3medima17)
