Performing coverage tests.
--------------------------

Run the following tests.

1. LoadAll

Alter the following settings:

args="./dist/tyrex-0.9.8.7.jar"
working_dir=".../tyrex"
classname="com.intalio.loadAll.JarLoad"

Change the args setting to the current version.
Change the "..." to the path leading up to tyrex.

Run the test.


2. Unit tests.

Clear the args setting.
Correct the working_dir.

Run the tests.


3. TestHarness

Change the following.

args="./domain.xml"
working_dir=".../tyrex"
classname="TestHarness"

Run the tests.

