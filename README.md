# TM - GRAMMAR

// FONCTIONS

def nom_de_la_fonction (argument1, argument2, ...){
    statement;
    return ();
}

//STRUCTURES

struct nom_de_la_structure {
    identifier1 = value1;
    identifier2 = value2;
    ...
    identifierN = valueN;
}

//VARIABLES

let nom_de_la_variable = something; //dynamic (integer, string, char, double, boolean)

INTEGER : 0 ... 999
    OR INTEGER : parseInt(STRING) throw Exception

DOUBLE : 0.0

STRING : "abc"

//STATEMENTS

something;

IF : if (conditions) {statement;}
(ELSE : else {statement;})
WHILE : while (conditions) {statement;}
RETURN : return (something or nothing);
VARIABLE : let nom_de_la_variable = something;

//CONDITIONS

EQUIV : ==
NOT EQUIV : !=
AND : &&
OR : ||
NOT : !!

//ARRAYS

let nom_du_tableau = [longueur_du_tableau]
nom_du_tableau.index(INTEGER)

array nom_du_tableau [longueur_du_tableau]

//COMMENTAIRES

# commentaires #

//OPERATIONS

modulo : %
addition : +
soustraction : -
multiplication : *
division : /
(exposants : exp)

//PRINT

print(something);
rprint(error);

//ERRORS


//MAIN

Main(arg1, arg2, arg3)
