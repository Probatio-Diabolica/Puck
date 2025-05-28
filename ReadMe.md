## About
ELF is an interpreter for puck

## Basic Syntax:
Variables declarations:
```
 var variableName = value;
```
supported values include -> Numbers , Strings , Chars

Loops:
For now, puck supports two types of loops.
Yes, they work very intuitively.
>* while (condition based loop)
syntax:
```
while(conditiom)
{
    //code body
}
```
>* for (ranged based loop)
syntax:
```
for(declaration/initialization ; range conditiom ; incrementation)
{
    //code body
}
```
Scopes:
>* For puck, scopes are declared using ```{}```.
scopes just allow declaring a local space in your code.

Output:
To get the output, use the ```print``` statement.
syntax:
```print varName```
or,
```print value```

## Run
to run the interpreter type
```bash
./elf.sh run fileName.pck
```
