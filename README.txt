Anthony Colaiacovo


[ Build ]
From the directory that this README file is in, you should be able to type "make" and everything should work.

[ Run ]
To run the program do this command (make sure to replace filename.cm with the actual C- file you want to work with). 
You also must tag at least one (but can do more) of the following tags: -a -s  where -a is for "abstract syntax tree" and 
-s is for "semantic analysis" and -c is for "compiler"

java -cp /usr/share/java/cup.jar:. CM filename.cm -a -s

The -a flag will produce a ".abs" file with the prefix of the .cm filename you used. 
The -s flag will produce a ".sym" file with the prefix of the .cm filename you used. 
The -c flag will produce a ".tm" file with the prefix of the .cm filename you used. 

If you want to run the scanner only, just do (again, where filename.cm is the C- file you're working with):

java -cp /usr/share/java/cup.jar:. Scanner < filename.cm


[ Credits ]
Code from class sample files was used
