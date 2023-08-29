# Order Value Constraint

## Inner Working

* given a problem grid.
    ex: a 4x4 grid.

* chose an order using a comparator (>,<,<=,>=,==).
    ex: >=

* apply this constraint only on a subset of cell.
    ex: the second column.

* Make it so that to cell that follow each other position in the subset have to respect the order.
    ex: in the subset [1,_,3,4] the second cell can only take the value can take 1, 2 or 3 because if it take the value 4 for exemple [1,4,3,4] then the 2nd and 3rd cell don't respect the order.

* If a cell is out of the subset it is not touched by the constraint.
    ex: the col 4 isn't touched by this constrain and so can be [3,4,2,1] without any problem.

* there can be multiple instance each with their own subset.
    ex: you could also apply it on the col 1 [4,3,,2] and the col 1 and the col 2 [1,,3,4] their internal ordering are independent and you could even have the col1 use < ordering instead

* multiple subset can contain the same cell.
