#!/bin/bash

# Store the first and second argument
option=$1
fileName=$2

exec java -jar Elf.jar "$option" "$fileName"
