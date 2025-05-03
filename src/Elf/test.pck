var age = 67;

var isAdult = age >= 18;

if (isAdult) { print "eligible for voting: true"; }
else { print "eligible for voting: false"; }


if (age < 16) { print "eligible for driving: false"; }
else if (age < 18) { print "eligible for driving: learner's permit"; }
else { print "eligible for driving: full license"; }

if (age < 21) { print "eligible for drinking (US): false"; }
else { print "eligible for drinking (US): true"; }


var quz = "after";
{
  var quz = "before";

  for (var quz = 0; quz < 1; quz = quz + 1) {
    print quz;
    var quz = -1;
    print quz;
    print "Halellujiah";
  }
}

{
  for (var quz = 0; quz > 0; quz = quz + 1) {}

  var quz = "after";
  print quz;

  for (quz = 0; quz < 1; quz = quz + 1) {
    print quz;
  }
}

print "Hearn is equation";


