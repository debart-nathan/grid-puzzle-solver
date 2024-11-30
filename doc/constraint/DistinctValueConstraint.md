
# Distinct Value Constraint

## Inner Working

* given a problem grid.
    ex: in a 4x4 grid.

* apply this constraint only on a subset of cell.
    ex: the second column.

* make it so that no one cell in this subset have the same value.
    ex: in the subset  [1,_,4,2] the second cell can only take the value 3. because if it take the value 2 for exemple [1,2,4,2] then the 2nd and 4th cell have the same value in the subset.

* if a cell is out of the subset it is not touched by the constraint.
    ex: while the second column [1,_,4,2] is affected and can't have 2 value the same the third column [4,4,2,1] is not an so can have 2 cell with the same value (here 4) without problem.

* there can be multiple instance each with their own subset.
    ex: you could also apply it on the col 1 .[4,3,_,2] and the col 1 and the col 2  [1,_,4,2] can have multiple time the same value between both of them (4 and 2),  but can't have 2 value the same internally.

* also note that multiple subset can contain the same Cell.
