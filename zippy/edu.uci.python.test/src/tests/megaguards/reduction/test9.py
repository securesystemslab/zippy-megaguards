from functools import reduce

def t(a, b):
    """ @MG:reduce-on """
    return a + b + 3

print(reduce(t, [ i+1 for i in range(100)]))
