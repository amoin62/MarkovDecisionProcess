**************** Requirements ****************
* Java 1.8 for compilation
* MySql database
* Import the MovieLens database from 'data/movielens1m.7z' into MySql database.


**************** What is in the code ****************
*The BaseLib project is a peer to peer simulator I had coded for previous research. 
BaseLib is multi-threaded and can simulate collaborative filtering. baselib.jar is used as library by MarkovDecisionProcess.
* MarkovDecisionProcess is the main code of this project. It has two major sub-packages: matrixFactorization and mdp.
* matrixFactorization package is about matrix factorization based recommendations (oracle)
and also contains the data model in matrixFactorization.data package, an implementation of the stochastic gradient descent,
RMSE and prediction functions and an implementation of Cartesian coordinate functions. Classes in matrixFactorization.action
and matrixFactorization.measure extends their subclasses from BaseLib. 
Also, matrixFactorization.coordinate and matrixFactorization.util are re-used from a previous research project.
* dataProvider package loads data from the database into the data model entities from matrixFactorization.data
* mdp package contains the implementation of MDP, Value Iteration and Policy Playback described in the final report.
* mdp.movie.ratingsDist contains implementation of mapping functions from ratings to rating categories described 
in Section 4 of the report.

**************** How to compile the code ****************
An executable jar file called "mdp.jar" is already provided in "code/MarkovDecisionProcess/dist/mdp.jar". 
If all you need is to run the code go to "How to run the code" section directly.
If you like to compile the code and build the jar file for yourself, there is already a build.xml file 
provided in MarkovDecisionProcess project. You need Ant to compile the project.
BaseLib should exist at the same directory as MarkovDecisionProcess. The working directory should be from
within MarkovDecisionProcess. 
If you are using Eclipse, just open the two projects in your workspace and run build.xml of MarkovDecisionProcess. 

**************** How to run the code ****************
In config.properties set
* the database URL, username and password
* reportToFile=[ifReportToFile: boolean] # true if you want to export the optimal policy to a file. reportFilePath must be provided.
* reportFilePath=[reportPath: String] # must be provided if reportToFile=true
* K=Maximum number of questions the user should answer (skipping is not considered an answer)
* L=Total number of movies in a state (do not put larger than around 10 or you will see OutOfMemoryError after some time.)
* ratingDist = [1 or 2] one of the two mapping functions from ratings to rating categories described in the report.

Then, run:
java -jar -Xmx4096m mdp.jar 

**************** What to expect from an execution ****************
You should first see the data being fetched from the database:
Starting to fetch movies with more than 0 ratings.
Fetched 1000 movies with more than 0 ratings
Fetched 2000 movies with more than 0 ratings
Fetched 3000 movies with more than 0 ratings
Fetched 3706 movies with more than 0 ratings
Creating users.
Created 500 users.
Created 1000 users.
...

Then, matrix factorization is run to compute model parameters. 
using 4 threads
Number of Predicted Ratings		Standard Deviation		Mean Absolute Error 
50010					0.9813077011996033		0.7830772409149569
simulator cycle 1
Number of Predicted Ratings		Standard Deviation		Mean Absolute Error 
50010					0.9502798127931253		0.7556982451913179
simulator cycle 2
Number of Predicted Ratings		Standard Deviation		Mean Absolute Error 
50010					0.936376812935557		0.743471692812461
...

Then, the list of movies is printed out, the MDP is solved, some statistics are printed out and the optimal policy is computed.
0	Lethal Weapon 2 (1989)	960
1	Cocoon (1985)	960
2	Mary Poppins (1964)	959
3	Mummy, The (1999)	959
4	Star Trek VI The Undiscovered Country (1991)	958
5	Time Bandits (1981)	957
6	Top Gun (1986)	954
7	Unforgiven (1992)	954
Computing MDP states.
Number of states so far = 10000
Number of states so far = 20000
Number of MDP states = 29056
Iteration 1 diff = 0.3969379428947893
Iteration 2 diff = 0.2460100910055355
Iteration 3 diff = 0.1673024304390801
Iteration 4 diff = 0.11782182453756107
Iteration 5 diff = 0.10553997818347993
Iteration 6 diff = 0.09677109711206366
Iteration 7 diff = 0.08560207942379028
Iteration 8 diff = 0.06783006430186633
Iteration 9 diff = 0.0
Computing the optimal policy
Optimal policy computed.
MDP runtime = 62 seconds.
Average number of users per state = 104.8953744493392
Exporting the optimal policy to results/optimalPolicy.txt
Optimal policy exported to results/optimalPolicy.txt successfully.
Average size of simulated profile = 0.4205298013245033
...
Once the optimal policy is known, it is played back on each user's profile to simulate a new profile for them. 
Matrix factorization is run again on the simulated profile.
Number of Predicted Ratings		Standard Deviation		Mean Absolute Error 
50010					1.0145033004927289		0.8095814644669016
simulator cycle 1
Number of Predicted Ratings		Standard Deviation		Mean Absolute Error 
50010					1.0140600273273566		0.8092176332306377
simulator cycle 2
Number of Predicted Ratings		Standard Deviation		Mean Absolute Error 
50010					1.0136299841134617		0.8088611284532484
...
