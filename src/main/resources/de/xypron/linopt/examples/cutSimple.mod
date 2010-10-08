# This program solves a small cutting stock problem.
# For solving usual size problems column generation should be used.
# @author Heinrich Schuchardt

# demand: size, quantity
set Demand, dimen 2;
# stock: index, size
set Stock, dimen 2;

set StockPosition := setof{(i, l) in Stock} i;
param length{i in StockPosition} := sum{(i, l) in Stock} l;

set Product := setof{(p, q) in Demand} p;
param demand{p in Product} := sum{(p, q) in Demand} q;

var u{StockPosition}, binary;
var x{StockPosition, Product}, integer, >= 0;

minimize waste :
    sum{i in StockPosition} length[i] * u[i]
  - sum {i in StockPosition, p in Product} p * x[i, p];

s.t. d{p in Product} :
  demand[p] = sum{i in StockPosition} x[i, p];

s.t. s{i in StockPosition} :
  length[i] * u[i] >= sum{p in Product} p * x[i, p];

solve;

printf "\nProduction plan\n";
for {i in StockPosition : sum{p in Product} x[i, p] > 0 } {
   printf "%i >= ", length[i];
   printf  {p in Product: x[i, p] > 0} " + %i * %i", x[i, p], p;
   printf "\n";
}
printf "Waste = %i\n", waste;

data;

# demand: size, quantity
set Demand :=
   6 20
  10 10
  16  3
  25  7
  40  2
  63  1;

# stock: index, size
set Stock :=
   1  50
   2  50
   3 100
   4 100
   5 100
   6 200
   7 200;
end;